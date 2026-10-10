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

package org.citrusframework.poc.mcp.spring;

import java.util.Set;

import org.citrusframework.poc.mcp.ExchangeRegistry;
import org.citrusframework.poc.mcp.ScenarioCatalog;
import org.citrusframework.poc.mcp.SimulationReply;

/**
 * Controlled {@code resources/list} semantics for the Spring candidate (T7).
 *
 * <p>Same assertion half as the Quarkus path: whole unpaginated operation received
 * by Citrus, pending until its reply, without catalog mutation. The live T7 question
 * is whether pinned Spring AI / SDK 2.0.0 exposes a public high-level list customizer
 * preserving high-level server ownership; static resource registration alone is
 * insufficient. A low-level session-factory replacement is a boundary-changing
 * alternative for review, not this PoC.
 */
public final class ControlledResourceList {

    private static final Set<String> ALLOWED_FIXTURES =
            Set.of("empty", "alpha", "beta", "alpha-beta", "beta-alpha");

    private final ExchangeRegistry registry;

    public ControlledResourceList(ExchangeRegistry registry) {
        this.registry = registry;
    }

    public boolean isPending(String token) {
        return registry.isPending(token);
    }

    public void replyWithFixture(String token, String fixture) {
        if (!ALLOWED_FIXTURES.contains(fixture)) {
            throw new IllegalArgumentException("unsupported list fixture: " + fixture);
        }
        if (!registry.reply(token, SimulationReply.resourceListResult(ScenarioCatalog.resourceListFixture(fixture)))) {
            throw new IllegalStateException("list exchange already terminal: " + token);
        }
    }

    public void replyWithError(String token, int code, String message) {
        String payload = "{\"code\": " + code + ", \"message\": \"" + message + "\"}";
        if (!registry.reply(token, SimulationReply.jsonRpcError(code, payload))) {
            throw new IllegalStateException("list exchange already terminal: " + token);
        }
    }

    public String staticCatalog() {
        return ScenarioCatalog.staticResourceList();
    }
}
