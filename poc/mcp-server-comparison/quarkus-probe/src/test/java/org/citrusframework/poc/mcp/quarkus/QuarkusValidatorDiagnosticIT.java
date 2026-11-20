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

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import io.modelcontextprotocol.spec.McpSchema;
import org.citrusframework.poc.mcp.client.LiveMcpClient;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Executes the validator diagnostic variant (T6): with
 * {@code -Dpoc.denyResourceList=true} the runner denies {@code resources/list} while
 * every other method keeps working. The recorded wire behavior (denial error shape,
 * no Citrus-supplied listing) is the evidence that the validator is deny-only — a
 * missing supported whole-list extension, not a solution.
 */
class QuarkusValidatorDiagnosticIT {

    private static List<String> childCommand() {
        Path runner = Path.of(System.getProperty("user.dir"), "target", "quarkus-app", "quarkus-run.jar");
        assertThat(runner).as("augmented runner (package phase must have run)").exists();
        List<String> command = new ArrayList<>();
        command.add(javaExe());
        command.add("-Dquarkus.mcp.server.http.enabled=false");
        command.add("-Dpoc.denyResourceList=true");
        command.add("-jar");
        command.add(runner.toAbsolutePath().toString());
        command.add("--scenario");
        command.add("echo-smoke");
        command.add("--endpoint");
        command.add("poc-quarkus-live");
        return command;
    }

    private static String javaExe() {
        String home = System.getProperty("java.home");
        String exe = home + java.io.File.separator + "bin" + java.io.File.separator + "java";
        return new java.io.File(exe).exists() ? exe : "java";
    }

    private static Map<String, Object> toolArgs() {
        Map<String, Object> args = new LinkedHashMap<>();
        args.put("nested", Map.of("value", "12345"));
        args.put("items", List.of("a", "b"));
        args.put("count", 3);
        args.put("flag", true);
        return args;
    }

    @Test
    void denied_list_yields_diagnostics_not_a_controlled_listing() throws Exception {
        Path deps = Path.of(System.getProperty("user.dir"), "target", "quarkus-app", "quarkus-app-dependencies.txt");
        assertThat(Files.readString(deps, StandardCharsets.UTF_8)).contains("quarkus-mcp-server-stdio");
        try (LiveMcpClient driver = LiveMcpClient.launchStdio(childCommand(), Duration.ofSeconds(30))) {
            assertThat(driver.initialize().protocolVersion()).isEqualTo("2025-11-25");

            // Other methods are unaffected by the denial: composition holds.
            assertThat(driver.listTools().tools()).extracting(McpSchema.Tool::name).contains("echo");

            // The denied list produces a framework error, NOT a Citrus-supplied listing.
            // Characterized, not invented: whatever the exact error shape, no controlled
            // listing may come back — that absence is the deny-only evidence.
            boolean sawControlledListing = false;
            try {
                McpSchema.ListResourcesResult listed = driver.listResources();
                System.err.println("validator-diagnostic: unexpected listing: " + listed);
                sawControlledListing = !listed.resources().isEmpty();
            } catch (Exception e) {
                System.err.println("validator-diagnostic: denial error: " + e);
            }
            assertThat(sawControlledListing)
                    .as("denied list must never yield a Citrus-controlled listing")
                    .isFalse();

            // Tool dispatch still reaches Citrus through the bridge.
            McpSchema.CallToolResult result = driver.callTool("echo", toolArgs());
            assertThat(result.isError()).isFalse();
            assertThat(((McpSchema.TextContent) result.content().get(0)).text()).contains("12345");
            driver.readResource("poc://alpha");

            driver.awaitChildExit(60, TimeUnit.SECONDS);
            assertThat(driver.childStderr()).anySatisfy(line ->
                    assertThat(line).contains("CITRUS_ASSERTIONS status=ok"));
            assertThat(driver.childStderr()).anySatisfy(line ->
                    assertThat(line).contains("validator-diagnostic: denying"));
        }
    }

    @Test
    void validator_api_stays_deny_only() {
        // Guard the boundary: if a future release adds a reply-capable validate overload,
        // this fails and the whole-list investigation reopens with a positive probe.
        long validateOverloads = java.util.Arrays.stream(
                io.quarkiverse.mcp.server.runtime.McpRequestValidator.class.getMethods())
                .filter(method -> method.getName().equals("validate"))
                .count();
        assertThat(validateOverloads).isEqualTo(1);
        assertThatThrownBy(() -> io.quarkiverse.mcp.server.runtime.McpRequestValidator.class
                .getMethod("validate", io.vertx.core.json.JsonObject.class,
                        io.quarkiverse.mcp.server.runtime.McpRequest.class,
                        io.quarkiverse.mcp.server.McpMethod.class,
                        io.vertx.core.Promise.class))
                .isInstanceOf(NoSuchMethodException.class);
    }
}
