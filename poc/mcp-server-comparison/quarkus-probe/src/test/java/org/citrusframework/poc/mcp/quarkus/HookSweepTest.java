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
 * Exhaustive whole-list hook sweep over the pinned Quarkus MCP surface (T6).
 *
 * <p>Loads every loadable class in {@code io.quarkiverse.mcp.server.**} from the
 * artifact on the test classpath and lists every public method whose name suggests
 * list handling, interception, overriding, customization, handling or dispatch. The
 * pinned whitelist below is the complete result for MCP 2.0.2: registration, getters
 * and notifications only — no reply-capable list interception. Any upstream addition
 * fails this test and reopens the investigation with a positive probe.
 */
class HookSweepTest {

    /**
     * Complete method surface for MCP 2.0.2: per-feature registration handlers,
     * list-changed notifications, deny-only validation, client-side roots listing,
     * subscription filters and schema-mapper customization — nothing reply-capable
     * for server-side list operations.
     */
    private static final List<String> KNOWN_HOOK_SURFACE = List.of(
    "CompletionDefinition#setAsyncHandler[Function]",
    "CompletionDefinition#setHandler[Function, boolean]",
    "CompletionDefinition#setHandler[Function]",
    "CompletionDefinitionImpl#setAsyncHandler[Function]",
    "CompletionDefinitionImpl#setHandler[Function, boolean]",
    "CompletionDefinitionImpl#setHandler[Function]",
    "FeatureDefinition#setAsyncHandler[Function]",
    "FeatureDefinition#setHandler[Function, boolean]",
    "FeatureDefinition#setHandler[Function]",
    "FeatureDefinitionBase#setAsyncHandler[Function]",
    "FeatureDefinitionBase#setHandler[Function, boolean]",
    "JsonRpc#validate[JsonObject, Sender]",
    "ListEncoderResultMapper#list[]",
    "ListEncoderResultMapper#uniList[]",
    "McpConnectionBase#supportsSubscriptionsListen[]",
    "McpJavaTypeConverter#convertResourceContentsList[List]",
    "McpObjectMapperCustomizer#customize[ObjectMapper]",
    "McpRequestValidator#validate[JsonObject, McpRequest, McpMethod]",
    "McpServersRuntimeConfig#autoListChangedStrategy[]",
    "NotificationDefinition#setAsyncHandler[Function]",
    "NotificationDefinition#setHandler[Function, boolean]",
    "NotificationDefinition#setHandler[Function]",
    "NotificationDefinitionImpl#setAsyncHandler[Function]",
    "NotificationDefinitionImpl#setHandler[Function, boolean]",
    "NotificationDefinitionImpl#setHandler[Function]",
    "PromptDefinition#setAsyncHandler[Function]",
    "PromptDefinition#setHandler[Function, boolean]",
    "PromptDefinition#setHandler[Function]",
    "PromptDefinitionImpl#setAsyncHandler[Function]",
    "PromptDefinitionImpl#setHandler[Function, boolean]",
    "PromptDefinitionImpl#setHandler[Function]",
    "PromptManager#notifyListChanged[Predicate]",
    "PromptManagerImpl#notifyListChanged[Predicate]",
    "ResourceContentsEncoderResultMapper#list[]",
    "ResourceContentsEncoderResultMapper#uniList[]",
    "ResourceDefinition#setAsyncHandler[Function]",
    "ResourceDefinition#setHandler[Function, boolean]",
    "ResourceDefinition#setHandler[Function]",
    "ResourceDefinitionImpl#setAsyncHandler[Function]",
    "ResourceDefinitionImpl#setHandler[Function, boolean]",
    "ResourceDefinitionImpl#setHandler[Function]",
    "ResourceManager#notifyListChanged[Predicate]",
    "ResourceManagerImpl#notifyListChanged[Predicate]",
    "ResourceTemplateDefinition#setAsyncHandler[Function]",
    "ResourceTemplateDefinition#setHandler[Function, boolean]",
    "ResourceTemplateDefinition#setHandler[Function]",
    "ResourceTemplateDefinitionImpl#setAsyncHandler[Function]",
    "ResourceTemplateDefinitionImpl#setHandler[Function, boolean]",
    "ResourceTemplateDefinitionImpl#setHandler[Function]",
    "ResourceTemplateManager#notifyListChanged[Predicate]",
    "ResourceTemplateManagerImpl#notifyListChanged[Predicate]",
    "Roots#listAndAwait[]",
    "Roots#list[]",
    "RootsImpl#listAndAwait[]",
    "RootsImpl#list[]",
    "SchemaGeneratorConfigCustomizer#customize[SchemaGeneratorConfigBuilder]",
    "SchemaGeneratorConfigCustomizerJackson#customize[SchemaGeneratorConfigBuilder]",
    "SchemaGeneratorConfigCustomizerJakartaValidation#customize[SchemaGeneratorConfigBuilder]",
    "SchemaGeneratorConfigCustomizerSwagger2#customize[SchemaGeneratorConfigBuilder]",
    "ServerRequests#hasResponseHandler[long]",
    "SubscriptionFilter#promptsListChanged[]",
    "SubscriptionFilter#resourcesListChanged[]",
    "SubscriptionFilter#toolsListChanged[]",
    "ToolDefinition#setAsyncHandler[Function]",
    "ToolDefinition#setHandler[Function, boolean]",
    "ToolDefinition#setHandler[Function]",
    "ToolDefinitionImpl#setAsyncHandler[Function]",
    "ToolDefinitionImpl#setHandler[Function, boolean]",
    "ToolDefinitionImpl#setHandler[Function]",
    "ToolEncoderResultMapper#list[]",
    "ToolEncoderResultMapper#uniList[]",
    "ToolManager#notifyListChanged[Predicate]",
    "ToolManagerImpl#notifyListChanged[Predicate]",
    "Tools#inputValidationError[]"
    );

    static List<String> scanSurface() throws Exception {
        Class<?> anchor = Class.forName("io.quarkiverse.mcp.server.McpMethod");
        URI jarUri = anchor.getProtectionDomain().getCodeSource().getLocation().toURI();
        List<String> hits = new ArrayList<>();
        try (FileSystem fs = FileSystems.newFileSystem(Path.of(jarUri), Map.of());
             Stream<Path> paths = Files.walk(fs.getPath("/"))) {
            for (Path path : (Iterable<Path>) paths.filter(p -> p.toString().endsWith(".class"))::iterator) {
                String dotted = path.toString().substring(1, path.toString().length() - ".class".length())
                        .replace('/', '.');
                if (!dotted.startsWith("io.quarkiverse.mcp.server.")) {
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
                    // Supertype outside the probe classpath (e.g. telemetry): not scannable here.
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
        return hits.stream().sorted().distinct().toList();
    }

    @Test
    void hook_surface_matches_pinned_baseline() throws Exception {
        List<String> actual = scanSurface();
        System.err.println("hook-sweep (quarkus-mcp-server): " + actual.size() + " candidates");
        actual.forEach(candidate -> System.err.println("hook-sweep: " + candidate));
        assertThat(actual).containsExactlyElementsOf(KNOWN_HOOK_SURFACE);
    }
}
