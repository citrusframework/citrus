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

package org.citrusframework.poc.mcp.quarkus;

import java.util.Set;

import org.citrusframework.poc.mcp.ExchangeRegistry;
import org.citrusframework.poc.mcp.ScenarioCatalog;
import org.citrusframework.poc.mcp.SimulationReply;

/**
 * Controlled {@code resources/list} semantics for the Quarkus candidate (T6).
 *
 * <p>Encodes the framework-independent assertion half: the whole unpaginated list
 * operation is received by Citrus and stays pending until its reply — empty list,
 * either resource, both in the requested test order, or a JSON-RPC error — without
 * mutating the declared static catalog and without a per-resource visibility filter.
 * The live T6 question is whether the pinned Quarkus 2.0.2 runtime exposes a
 * supported whole-operation extension for this; this class is what that hook must
 * satisfy. It performs no transport I/O and overrides no framework classes.
 */
public final class ControlledResourceList {

    private static final Set<String> ALLOWED_FIXTURES =
            Set.of("empty", "alpha", "beta", "alpha-beta", "beta-alpha");

    private final ExchangeRegistry registry;

    public ControlledResourceList(ExchangeRegistry registry) {
        this.registry = registry;
    }

    /** True while the list request is pending (no early default listing sent). */
    public boolean isPending(String token) {
        return registry.isPending(token);
    }

    /** Replies with one of the allowed ordered fixtures; catalog itself is untouched. */
    public void replyWithFixture(String token, String fixture) {
        if (!ALLOWED_FIXTURES.contains(fixture)) {
            throw new IllegalArgumentException("unsupported list fixture: " + fixture);
        }
        if (!registry.reply(token, SimulationReply.resourceListResult(ScenarioCatalog.resourceListFixture(fixture)))) {
            throw new IllegalStateException("list exchange already terminal: " + token);
        }
    }

    /** Replies with an explicit JSON-RPC error (not a tool result). */
    public void replyWithError(String token, int code, String message) {
        String payload = "{\"code\": " + code + ", \"message\": \"" + message + "\"}";
        if (!registry.reply(token, SimulationReply.jsonRpcError(code, payload))) {
            throw new IllegalStateException("list exchange already terminal: " + token);
        }
    }

    /** Declared static catalog (both resources, declaration order); never mutated by replies. */
    public String staticCatalog() {
        return ScenarioCatalog.staticResourceList();
    }
}
