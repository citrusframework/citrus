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

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Whole-{@code resources/list} extension boundary for the Spring candidate (T7).
 *
 * <p>AC4 needs the entire unpaginated list operation received by Citrus with a
 * Citrus-supplied reply. This test pins the negative: the public
 * {@code McpSyncServerCustomizer} operates on the SDK {@code SyncSpecification}, which is
 * ADDITIVE ONLY (tools/resources/prompts/completions/handlers) — it exposes no
 * request-dispatch interception and no list-operation reply channel. Replacing the
 * transport session factory or hand-writing session policy would change the protocol
 * ownership boundary and is NOT the Spring PoC. If a future Spring AI/SDK adds a list
 * hook, this test fails and the investigation reopens (a new positive probe, not a
 * silent pass).
 */
class ListHookBoundaryTest {

    @Test
    void sync_specification_is_additive_only() {
        List<String> methods = Arrays.stream(
                io.modelcontextprotocol.server.McpServer.SyncSpecification.class.getMethods())
                .filter(method -> Modifier.isPublic(method.getModifiers()))
                .filter(method -> !method.getDeclaringClass().equals(Object.class))
                .map(Method::getName)
                .distinct()
                .sorted()
                .toList();

        // Additive surface that exists (tools, resources, prompts, completions, handlers).
        assertThat(methods).contains("tools", "resources", "prompts", "completions");

        // No list-operation interception point: any future hook name fails this test loudly.
        List<String> forbidden = List.of("resourcesListHandler", "listResourcesHandler", "listHandler",
                "requestHandler", "setRequestHandler", "listResourcesOverride", "resourcesListOverride",
                "onResourcesList", "interceptResourcesList");
        assertThat(methods).doesNotContainAnyElementsOf(forbidden);
    }

    @Test
    void customizer_contract_is_single_point() {
        Method[] methods = org.springframework.ai.mcp.customizer.McpSyncServerCustomizer.class.getMethods();
        assertThat(methods).hasSize(1);
        assertThat(methods[0].getName()).isEqualTo("customize");
        assertThat(methods[0].getParameterTypes())
                .containsExactly(io.modelcontextprotocol.server.McpServer.SyncSpecification.class);
    }
}
