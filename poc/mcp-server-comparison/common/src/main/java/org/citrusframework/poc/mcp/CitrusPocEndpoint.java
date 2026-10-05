/*
 * Copyright the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.citrusframework.poc.mcp;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Minimal Citrus receive/send bridge. Carries operation name, selected tool/URI,
 * typed JSON payload and an opaque exchange token; resolves {@code ${var}}
 * placeholders from scenario variables at execution time.
 *
 * <p>This is the protocol-neutral simulation half. The framework adapter performs
 * MCP parsing/framing and must not block its event loop while waiting: it registers
 * the exchange, awaits the future on a bounded worker, and maps the
 * {@link SimulationReply}. A deliberate expectation mismatch fails the scenario;
 * static callbacks alone never satisfy AC2.
 */
public final class CitrusPocEndpoint {

    private final ExchangeRegistry registry;
    private final Map<String, String> variables = new HashMap<>();
    private volatile ExpectedOperation expected;

    public record ExpectedOperation(String operation, String selector, String expectedJson) {
    }

    public record ReceivedRequest(String operation, String selector, String jsonPayload, String token) {
    }

    public CitrusPocEndpoint(ExchangeRegistry registry) {
        this.registry = Objects.requireNonNull(registry, "registry");
    }

    /** Supplies one Citrus variable value resolved at execution time. */
    public void setVariable(String name, String value) {
        variables.put(Objects.requireNonNull(name), Objects.requireNonNull(value));
    }

    /** Resolves {@code ${name}} placeholders; unknown names fail fast. */
    public String resolve(String template) {
        StringBuilder result = new StringBuilder();
        int cursor = 0;
        while (true) {
            int start = template.indexOf("${", cursor);
            if (start < 0) {
                result.append(template.substring(cursor));
                return result.toString();
            }
            int end = template.indexOf('}', start);
            if (end < 0) {
                throw new IllegalArgumentException("unclosed placeholder in: " + template);
            }
            result.append(template, cursor, start);
            String name = template.substring(start + 2, end);
            String value = variables.get(name);
            if (value == null) {
                throw new IllegalArgumentException("unknown Citrus variable: " + name);
            }
            result.append(value);
            cursor = end + 1;
        }
    }

    /** Declares the next expected operation (a real Citrus receive equivalent). */
    public void expect(String operation, String selector, String expectedJsonTemplate) {
        this.expected = new ExpectedOperation(operation, selector, resolve(expectedJsonTemplate));
    }

    /**
     * Receives a framework dispatch: validates it against the expectation, registers
     * the exchange and returns the token the scenario reply must reference.
     */
    public String receive(String endpointId, int generation, String sessionId, String requestId,
            String operation, String selector, String jsonPayload) {
        ExpectedOperation current = expected;
        if (current == null) {
            throw new IllegalStateException("no expectation declared for " + operation + " " + selector);
        }
        if (!current.operation().equals(operation) || !current.selector().equals(selector)) {
            throw new AssertionError("expected " + current.operation() + " " + current.selector()
                    + " but received " + operation + " " + selector);
        }
        if (!normalize(current.expectedJson()).equals(normalize(jsonPayload))) {
            throw new AssertionError("payload mismatch for " + operation + " " + selector
                    + ": expected " + current.expectedJson() + " but received " + jsonPayload);
        }
        return registry.register(endpointId, generation, sessionId, requestId);
    }

    /** Supplies the scenario reply for a pending token (a real Citrus send equivalent). */
    public void reply(String token, SimulationReply reply) {
        if (!registry.reply(token, reply)) {
            throw new IllegalStateException("exchange already terminal (late/duplicate reply): " + token);
        }
    }

    public ExchangeRegistry registry() {
        return registry;
    }

    private static String normalize(String json) {
        StringBuilder collapsed = new StringBuilder();
        boolean inString = false;
        for (int i = 0; i < json.length(); i++) {
            char c = json.charAt(i);
            if (c == '"') {
                inString = !inString;
                collapsed.append(c);
            } else if (inString || !Character.isWhitespace(c)) {
                collapsed.append(c);
            }
        }
        return collapsed.toString();
    }
}
