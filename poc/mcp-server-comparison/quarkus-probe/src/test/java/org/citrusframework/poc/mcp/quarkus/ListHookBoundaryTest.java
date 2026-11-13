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

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Whole-{@code resources/list} extension boundary for the Quarkus candidate (T6).
 *
 * <p>The only request-level hook in pinned MCP 2.0.2 is the diagnostic
 * {@code McpRequestValidator}: {@code validate} returns a confirm/deny future with NO
 * reply channel and runs before normal readiness/tracking — denying suppresses normal
 * dispatch and the framework emits validation-error diagnostics. There is no supported
 * whole-operation extension that lets Citrus receive the list and supply its reply;
 * overriding framework handler classes or reflective private-handler replacement is
 * excluded. If a future Quarkus MCP adds a list hook, the forbidden-name assertion
 * below fails and the investigation reopens.
 */
class ListHookBoundaryTest {

    @Test
    void request_validator_is_deny_only_with_no_reply_channel() throws Exception {
        Method validate = io.quarkiverse.mcp.server.runtime.McpRequestValidator.class.getMethod("validate",
                io.vertx.core.json.JsonObject.class,
                io.quarkiverse.mcp.server.runtime.McpRequest.class,
                io.quarkiverse.mcp.server.McpMethod.class);
        assertThat(validate.getReturnType()).isEqualTo(io.vertx.core.Future.class);
        assertThat(validate.getParameterCount()).isEqualTo(3);
        // No reply/exchange/session parameter: deny-only, diagnostic semantics.
        assertThat(validate.getParameterTypes()).doesNotContain(io.vertx.core.Promise.class);
    }

    @Test
    void no_supported_whole_list_extension_exists() {
        List<String> managerMethods = Arrays.stream(io.quarkiverse.mcp.server.ResourceManager.class.getMethods())
                .filter(method -> Modifier.isPublic(method.getModifiers()))
                .map(Method::getName)
                .distinct()
                .sorted()
                .toList();

        // Programmatic registration exists (control-case surface).
        assertThat(managerMethods).anySatisfy(name ->
                assertThat(name).containsIgnoringCase("resource"));

        // No list-operation interception point. If a future Quarkus MCP adds one, this
        // fails and the investigation reopens with a positive executable probe.
        List<String> forbidden = List.of("listHandler", "setListHandler", "onList", "interceptList",
                "listOverride", "resourcesListHandler", "setRequestHandler", "requestHandler");
        assertThat(managerMethods).doesNotContainAnyElementsOf(forbidden);
    }
}
