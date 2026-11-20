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
 * Live Spring AI MCP server over subprocess stdio (T5-live/T11-live).
 *
 * <p>Topology per Stage 2: the SDK client (parent, pinned 2.0.1) launches one server
 * child containing the Citrus scenario runner and the Spring AI high-level server in
 * one JVM. The child runs on the server-resolved classpath (pinned SDK 2.0.0 — asserted
 * before launch, never the probe's 2.0.1 client). Client results and server-side Citrus
 * assertions stay separate: the child reports {@code CITRUS_ASSERTIONS} on stderr.
 */
class SpringLiveStdioIT {

    private static List<String> childCommand(String scenario, String serverClasspath) {
        // Child classpath: probe classes (scenario main) + host classes (live host/config) +
        // the server-resolved runtime graph (pinned SDK 2.0.0). The host's own output
        // directory is not part of build-classpath, so it is prepended explicitly.
        String base = System.getProperty("user.dir");
        String classpath = Path.of(base, "target", "classes") + java.io.File.pathSeparator
                + Path.of(base, "target", "test-classes") + java.io.File.pathSeparator
                + Path.of(base, "..", "spring-host", "target", "classes") + java.io.File.pathSeparator
                + serverClasspath;
        List<String> command = new ArrayList<>();
        command.add(javaExe());
        command.add("-cp");
        command.add(classpath);
        command.add("org.citrusframework.poc.mcp.spring.SpringLiveScenarioMain");
        command.add("--scenario");
        command.add(scenario);
        command.add("--endpoint");
        command.add("poc-spring-live");
        return command;
    }

    private static String javaExe() {
        String home = System.getProperty("java.home");
        String exe = home + java.io.File.separator + "bin" + java.io.File.separator + "java";
        return new java.io.File(exe).exists() ? exe : "java";
    }

    /** Server-resolved runtime classpath: proves the SDK/host selection before launch. */
    private static String serverClasspath() throws Exception {
        Path file = Path.of(System.getProperty("user.dir"), "..", "spring-host", "target", "server-classpath.txt");
        assertThat(file).as("server classpath file (run a build first)").exists();
        String classpath = Files.readString(file, StandardCharsets.UTF_8).trim();
        assertThat(classpath).as("server keeps pinned SDK 2.0.0").contains("mcp-core-2.0.0");
        assertThat(classpath).as("client SDK 2.0.1 must not leak into the server child").doesNotContain("mcp-core-2.0.1");
        assertThat(classpath).contains("spring-ai-starter-mcp-server");
        return classpath;
    }

    private static Map<String, Object> toolArgs() {
        // Same insertion order as the child scenario; explicit null omitted per LIVE-NULL-1.
        Map<String, Object> args = new LinkedHashMap<>();
        args.put("nested", Map.of("value", "12345"));
        args.put("items", List.of("a", "b"));
        args.put("count", 3);
        args.put("flag", true);
        return args;
    }

    @Test
    void live_tool_round_trip_is_citrus_controlled() throws Exception {
        String serverClasspath = serverClasspath();
        try (LiveMcpClient driver = LiveMcpClient.launchStdio(
                childCommand("echo-smoke", serverClasspath), Duration.ofSeconds(30))) {
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

            // The child exits by itself after serving; await the bounded natural exit first.
            driver.awaitChildExit(60, TimeUnit.SECONDS);
            assertThat(driver.childStderr()).anySatisfy(line ->
                    assertThat(line).contains("CITRUS_ASSERTIONS status=ok"));
        }
    }

    @Test
    void deliberate_mismatch_fails_tool_call_and_citrus_scenario() throws Exception {
        String serverClasspath = serverClasspath();
        boolean sawToolFailure = false;
        try (LiveMcpClient driver = LiveMcpClient.launchStdio(
                childCommand("mismatch", serverClasspath), Duration.ofSeconds(30))) {
            driver.initialize();
            try {
                McpSchema.CallToolResult result = driver.callTool("echo", toolArgs());
                sawToolFailure = Boolean.TRUE.equals(result.isError());
            } catch (Exception e) {
                // Characterized, not invented: record the exact failure below.
                driver.childStderr().forEach(line -> System.err.println("child-stderr: " + line));
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
}
