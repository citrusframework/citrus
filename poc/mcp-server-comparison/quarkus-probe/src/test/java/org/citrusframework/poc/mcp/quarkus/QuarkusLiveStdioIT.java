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

/**
 * Live Quarkus MCP server over subprocess stdio (T4-live/T10-live).
 *
 * <p>Topology per Stage 2: the SDK client (parent, pinned 2.0.1) launches the
 * augmented Quarkus runner as the server child containing the Citrus scenario runner
 * and the Quarkus MCP host in one JVM — an ordinary-Java entry point
 * ({@code Quarkus.run} from {@code main}), no test-extension bootstrap. Same-JVM
 * ownership is proven functionally: the CDI tool method validates against the
 * expectation the main thread declared pre-boot. The child runs on the runner's
 * self-contained classpath (MCP 2.0.2), never the probe's 2.0.1 client.
 */
class QuarkusLiveStdioIT {

    private static List<String> childCommand(String scenario) {
        Path runner = Path.of(System.getProperty("user.dir"), "target", "quarkus-app", "quarkus-run.jar");
        assertThat(runner).as("augmented runner (package phase must have run)").exists();
        List<String> command = new ArrayList<>();
        command.add(javaExe());
        // Explicit stdio selection: the HTTP extension is installed in the same artifact.
        command.add("-Dquarkus.mcp.server.http.enabled=false");
        command.add("-jar");
        command.add(runner.toAbsolutePath().toString());
        command.add("--scenario");
        command.add(scenario);
        command.add("--endpoint");
        command.add("poc-quarkus-live");
        return command;
    }

    private static String javaExe() {
        String home = System.getProperty("java.home");
        String exe = home + java.io.File.separator + "bin" + java.io.File.separator + "java";
        return new java.io.File(exe).exists() ? exe : "java";
    }

    /** Runner classpath isolation: the augmented app carries the server stack. */
    private static void assertRunnerIsolation() throws Exception {
        Path deps = Path.of(System.getProperty("user.dir"), "target", "quarkus-app", "quarkus-app-dependencies.txt");
        assertThat(deps).exists();
        String list = Files.readString(deps, StandardCharsets.UTF_8);
        // Format is group:artifact::jar:version.
        assertThat(list).as("server keeps pinned MCP 2.0.2").contains("quarkus-mcp-server-stdio");
        assertThat(list).as("server keeps pinned MCP 2.0.2").contains(":2.0.2");
        assertThat(list).as("client SDK 2.0.1 must not leak into the server child")
                .doesNotContain("mcp-core-2.0.1");
    }

    private static Map<String, Object> toolArgs() {
        // Same insertion order as the child scenario; explicit null characterized per LIVE-NULL-1.
        Map<String, Object> args = new LinkedHashMap<>();
        args.put("nested", Map.of("value", "12345"));
        args.put("items", List.of("a", "b"));
        args.put("count", 3);
        args.put("flag", true);
        return args;
    }

    @Test
    void live_tool_round_trip_is_citrus_controlled() throws Exception {
        assertRunnerIsolation();
        try (LiveMcpClient driver = LiveMcpClient.launchStdio(
                childCommand("echo-smoke"), Duration.ofSeconds(30))) {
            McpSchema.InitializeResult init = driver.initialize();
            assertThat(init.protocolVersion()).isEqualTo("2025-11-25");

            McpSchema.ListToolsResult tools = driver.listTools();
            assertThat(tools.tools()).extracting(McpSchema.Tool::name).contains("echo");
            McpSchema.Tool echo = tools.tools().stream()
                    .filter(tool -> tool.name().equals("echo"))
                    .findFirst()
                    .orElseThrow();
            assertThat(echo.description()).isNotBlank();
            assertThat(echo.inputSchema()).containsKey("properties");

            McpSchema.ListResourcesResult resources = driver.listResources();
            assertThat(resources.resources()).extracting(McpSchema.Resource::uri)
                    .contains("poc://alpha", "poc://beta");

            McpSchema.CallToolResult result = driver.callTool("echo", toolArgs());
            assertThat(result.isError()).isFalse();
            assertThat(result.content()).hasSize(1);
            assertThat(result.content().get(0)).isInstanceOf(McpSchema.TextContent.class);
            assertThat(((McpSchema.TextContent) result.content().get(0)).text()).contains("12345");

            McpSchema.ReadResourceResult read = driver.readResource("poc://alpha");
            assertThat(read.contents()).hasSize(1);
            assertThat(read.contents().get(0)).isInstanceOf(McpSchema.TextResourceContents.class);
            assertThat(((McpSchema.TextResourceContents) read.contents().get(0)).text())
                    .contains("alpha-content");

            driver.awaitChildExit(60, TimeUnit.SECONDS);
            assertThat(driver.childStderr()).anySatisfy(line ->
                    assertThat(line).contains("CITRUS_ASSERTIONS status=ok"));
        }
    }

    @Test
    void deliberate_mismatch_fails_tool_call_and_citrus_scenario() throws Exception {
        assertRunnerIsolation();
        boolean sawToolFailure = false;
        try (LiveMcpClient driver = LiveMcpClient.launchStdio(
                childCommand("mismatch"), Duration.ofSeconds(30))) {
            driver.initialize();
            try {
                McpSchema.CallToolResult result = driver.callTool("echo", toolArgs());
                sawToolFailure = Boolean.TRUE.equals(result.isError());
            } catch (Exception e) {
                sawToolFailure = true;
            }
            try {
                driver.awaitChildExit(60, TimeUnit.SECONDS);
            } catch (java.util.concurrent.TimeoutException e) {
                throw new AssertionError("child did not exit after mismatch (forced kill = failure)", e);
            }
            assertThat(sawToolFailure).as("client must observe the Citrus mismatch as a tool failure").isTrue();
            assertThat(driver.childStderr()).anySatisfy(line ->
                    assertThat(line).contains("CITRUS_ASSERTIONS status=fail-as-designed"));
            assertThat(driver.childStderr()).anySatisfy(line ->
                    assertThat(line).contains("AssertionError"));
        }
    }

    @Test
    void stdio_launch_opens_no_http_listener() throws Exception {
        assertRunnerIsolation();
        try (LiveMcpClient driver = LiveMcpClient.launchStdio(
                childCommand("echo-smoke"), Duration.ofSeconds(30))) {
            driver.initialize();
            driver.callTool("echo", toolArgs());
            // Complete the smoke flow so the child exits bounded (tool + one read).
            driver.readResource("poc://alpha");
            driver.awaitChildExit(60, TimeUnit.SECONDS);
            assertThat(driver.childStderr()).noneSatisfy(line ->
                    assertThat(line).contains("Listening on:"));
        }
    }
}
