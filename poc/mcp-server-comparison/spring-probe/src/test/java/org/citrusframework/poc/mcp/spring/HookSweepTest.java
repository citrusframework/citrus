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
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Exhaustive whole-list hook sweep over the pinned Spring AI / SDK surface (T7).
 *
 * <p>Loads every loadable class in {@code io.modelcontextprotocol.server.**} (SDK 2.0.0
 * on this classpath) and {@code org.springframework.ai.mcp.**} (AI 2.0.1) and lists
 * every public method suggesting list handling, interception, overriding,
 * customization, handling or dispatch. The pinned whitelist below is the complete
 * result: additive specifications, getters and notification consumers only — no
 * reply-capable list interception. Any upstream addition fails this test and reopens
 * the investigation with a positive probe.
 */
class HookSweepTest {

    /**
     * Complete method surface for SDK 2.0.0 + AI 2.0.1: per-feature call/read handlers,
     * additive specifications, validation toggles, notification consumers, transport
     * internals (handleRequest/setMcpHandler) and client-side roots listing — nothing
     * reply-capable for server-side list operations. Transport-level replacement would
     * be a boundary-changing custom layer, not this PoC.
     */
    private static final List<String> KNOWN_HOOK_SURFACE = List.of(
    "AsyncCompletionSpecification#completionHandler[]",
    "AsyncPromptSpecification#promptHandler[]",
    "AsyncResourceSpecification#readHandler[]",
    "AsyncResourceTemplateSpecification#readHandler[]",
    "AsyncSpecification#jsonSchemaValidator[JsonSchemaValidator]",
    "AsyncSpecification#rootsChangeHandler[BiFunction]",
    "AsyncSpecification#rootsChangeHandlers[BiFunction[]]",
    "AsyncSpecification#rootsChangeHandlers[List]",
    "AsyncSpecification#strictToolNameValidation[boolean]",
    "AsyncSpecification#validateToolInputs[boolean]",
    "AsyncToolSpecification#callHandler[]",
    "Builder#callHandler[BiFunction]",
    "Builder#securityValidator[ServerTransportSecurityValidator]",
    "DefaultMcpStatelessServerHandler#handleRequest[McpTransportContext, JSONRPCRequest]",
    "DefaultServerTransportSecurityValidator#validateHeaders[Map]",
    "HttpServletStatelessServerTransport#setMcpHandler[McpStatelessServerHandler]",
    "McpAsyncServer#listPrompts[]",
    "McpAsyncServer#listResourceTemplates[]",
    "McpAsyncServer#listResources[]",
    "McpAsyncServer#listTools[]",
    "McpAsyncServer#notifyPromptsListChanged[]",
    "McpAsyncServer#notifyResourcesListChanged[]",
    "McpAsyncServer#notifyToolsListChanged[]",
    "McpAsyncServerExchange#listRoots[String]",
    "McpAsyncServerExchange#listRoots[]",
    "McpStatelessAsyncServer#listPrompts[]",
    "McpStatelessAsyncServer#listResourceTemplates[]",
    "McpStatelessAsyncServer#listResources[]",
    "McpStatelessAsyncServer#listTools[]",
    "McpStatelessServerHandler#handleRequest[McpTransportContext, JSONRPCRequest]",
    "McpStatelessSyncServer#listPrompts[]",
    "McpStatelessSyncServer#listResourceTemplates[]",
    "McpStatelessSyncServer#listResources[]",
    "McpStatelessSyncServer#listTools[]",
    "McpSyncServer#listPrompts[]",
    "McpSyncServer#listResourceTemplates[]",
    "McpSyncServer#listResources[]",
    "McpSyncServer#listTools[]",
    "McpSyncServer#notifyPromptsListChanged[]",
    "McpSyncServer#notifyResourcesListChanged[]",
    "McpSyncServer#notifyToolsListChanged[]",
    "McpSyncServerExchange#listRoots[String]",
    "McpSyncServerExchange#listRoots[]",
    "ServerTransportSecurityValidator#validateHeaders[Map]",
    "SingleSessionAsyncSpecification#jsonSchemaValidator[JsonSchemaValidator]",
    "SingleSessionAsyncSpecification#rootsChangeHandler[BiFunction]",
    "SingleSessionAsyncSpecification#rootsChangeHandlers[BiFunction[]]",
    "SingleSessionAsyncSpecification#rootsChangeHandlers[List]",
    "SingleSessionAsyncSpecification#strictToolNameValidation[boolean]",
    "SingleSessionAsyncSpecification#validateToolInputs[boolean]",
    "SingleSessionSyncSpecification#jsonSchemaValidator[JsonSchemaValidator]",
    "SingleSessionSyncSpecification#rootsChangeHandler[BiConsumer]",
    "SingleSessionSyncSpecification#rootsChangeHandlers[BiConsumer[]]",
    "SingleSessionSyncSpecification#rootsChangeHandlers[List]",
    "SingleSessionSyncSpecification#strictToolNameValidation[boolean]",
    "SingleSessionSyncSpecification#validateToolInputs[boolean]",
    "StatelessAsyncSpecification#jsonSchemaValidator[JsonSchemaValidator]",
    "StatelessAsyncSpecification#strictToolNameValidation[boolean]",
    "StatelessAsyncSpecification#validateToolInputs[boolean]",
    "StatelessSyncSpecification#jsonSchemaValidator[JsonSchemaValidator]",
    "StatelessSyncSpecification#strictToolNameValidation[boolean]",
    "StatelessSyncSpecification#validateToolInputs[boolean]",
    "StreamableServerAsyncSpecification#jsonSchemaValidator[JsonSchemaValidator]",
    "StreamableServerAsyncSpecification#rootsChangeHandler[BiFunction]",
    "StreamableServerAsyncSpecification#rootsChangeHandlers[BiFunction[]]",
    "StreamableServerAsyncSpecification#rootsChangeHandlers[List]",
    "StreamableServerAsyncSpecification#strictToolNameValidation[boolean]",
    "StreamableServerAsyncSpecification#validateToolInputs[boolean]",
    "StreamableSyncSpecification#jsonSchemaValidator[JsonSchemaValidator]",
    "StreamableSyncSpecification#rootsChangeHandler[BiConsumer]",
    "StreamableSyncSpecification#rootsChangeHandlers[BiConsumer[]]",
    "StreamableSyncSpecification#rootsChangeHandlers[List]",
    "StreamableSyncSpecification#strictToolNameValidation[boolean]",
    "StreamableSyncSpecification#validateToolInputs[boolean]",
    "SyncCompletionSpecification#completionHandler[]",
    "SyncPromptSpecification#promptHandler[]",
    "SyncResourceSpecification#readHandler[]",
    "SyncResourceTemplateSpecification#readHandler[]",
    "SyncSpecification#jsonSchemaValidator[JsonSchemaValidator]",
    "SyncSpecification#rootsChangeHandler[BiConsumer]",
    "SyncSpecification#rootsChangeHandlers[BiConsumer[]]",
    "SyncSpecification#rootsChangeHandlers[List]",
    "SyncSpecification#strictToolNameValidation[boolean]",
    "SyncSpecification#validateToolInputs[boolean]",
    "SyncToolSpecification#callHandler[]"
    );

    static List<String> scanSurface() throws Exception {
        List<String> hits = new ArrayList<>();
        scanArtifact("io.modelcontextprotocol.server.McpSyncServer",
                List.of("io.modelcontextprotocol.server."), hits);
        scanArtifact("org.springframework.ai.tool.ToolCallback",
                List.of("org.springframework.ai.mcp."), hits);
        return hits.stream().sorted().distinct().toList();
    }

    private static void scanArtifact(String anchorClass, List<String> prefixes, List<String> hits)
            throws Exception {
        Class<?> anchor = Class.forName(anchorClass);
        URI jarUri = anchor.getProtectionDomain().getCodeSource().getLocation().toURI();
        try (FileSystem fs = FileSystems.newFileSystem(Path.of(jarUri), Map.of());
             Stream<Path> paths = Files.walk(fs.getPath("/"))) {
            for (Path path : (Iterable<Path>) paths.filter(p -> p.toString().endsWith(".class"))::iterator) {
                String dotted = path.toString().substring(1, path.toString().length() - ".class".length())
                        .replace('/', '.');
                if (prefixes.stream().noneMatch(dotted::startsWith)) {
                    continue;
                }
                Class<?> candidate;
                try {
                    candidate = Class.forName(dotted, false, anchor.getClassLoader());
                } catch (NoClassDefFoundError | ExceptionInInitializerError e) {
                    continue;
                }
                Method[] methods;
                try {
                    methods = candidate.getMethods();
                } catch (NoClassDefFoundError e) {
                    continue;
                }
                for (Method method : methods) {
                    if (!Modifier.isPublic(method.getModifiers())) {
                        continue;
                    }
                    if (method.getName().matches("(?i).*(list|intercept|override|customi|handler|dispatch|validat).*")) {
                        hits.add(candidate.getSimpleName() + "#" + method.getName()
                                + Arrays.toString(Arrays.stream(method.getParameterTypes())
                                        .map(Class::getSimpleName).toArray()));
                    }
                }
            }
        }
    }

    @Test
    void hook_surface_matches_pinned_baseline() throws Exception {
        List<String> actual = scanSurface();
        System.err.println("hook-sweep (sdk-server+ai-mcp): " + actual.size() + " candidates");
        actual.forEach(candidate -> System.err.println("hook-sweep: " + candidate));
        assertThat(actual).containsExactlyElementsOf(KNOWN_HOOK_SURFACE);
    }
}
