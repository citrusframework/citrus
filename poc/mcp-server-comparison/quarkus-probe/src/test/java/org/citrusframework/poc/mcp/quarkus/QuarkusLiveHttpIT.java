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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.modelcontextprotocol.spec.McpSchema;
import org.citrusframework.poc.mcp.client.LiveMcpClient;
import org.citrusframework.poc.mcp.client.ManagedChildProcess;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Live Quarkus MCP server over Streamable HTTP (T8-live).
 *
 * <p>The augmented runner (MCP 2.0.2 + HTTP extension) launches as the server child
 * with stdio explicitly disabled; the SDK 2.0.1 client drives init/list/call/read over
 * loopback HTTP on an ephemeral port parsed from the runtime's Listening line.
 * In-probe same-JVM boot is impossible without the test harness (documented asymmetry
 * vs Spring); same-JVM ownership is proven functionally by the bridge validation.
 */
class QuarkusLiveHttpIT {

    private static final Pattern LISTENING = Pattern.compile("Listening on: http://([^ :]+):([0-9]+)");

    private static List<String> childCommand(String scenario) {
        Path runner = Path.of(System.getProperty("user.dir"), "target", "quarkus-app", "quarkus-run.jar");
        assertThat(runner).as("augmented runner (package phase must have run)").exists();
        List<String> command = new ArrayList<>();
        command.add(javaExe());
        command.add("-Dquarkus.mcp.server.stdio.enabled=false");
        command.add("-Dquarkus.http.port=0");
        command.add("-Dquarkus.log.level=INFO");
        command.add("-jar");
        command.add(runner.toAbsolutePath().toString());
        command.add("--transport");
        command.add("http");
        command.add("--scenario");
        command.add(scenario);
        command.add("--endpoint");
        command.add("poc-quarkus-http");
        return command;
    }

    private static String javaExe() {
        String home = System.getProperty("java.home");
        String exe = home + java.io.File.separator + "bin" + java.io.File.separator + "java";
        return new java.io.File(exe).exists() ? exe : "java";
    }

    private static void assertRunnerIsolation() throws Exception {
        Path deps = Path.of(System.getProperty("user.dir"), "target", "quarkus-app", "quarkus-app-dependencies.txt");
        assertThat(deps).exists();
        String list = Files.readString(deps, StandardCharsets.UTF_8);
        assertThat(list).as("server keeps pinned MCP 2.0.2").contains("quarkus-mcp-server-http");
        assertThat(list).as("server keeps pinned MCP 2.0.2").contains(":2.0.2");
        assertThat(list).as("client SDK 2.0.1 must not leak into the server child")
                .doesNotContain("mcp-core-2.0.1");
    }

    private static String baseUrl(ManagedChildProcess child) throws Exception {
        child.awaitStdoutLine(line -> line.startsWith("HTTP_READY "), 60, TimeUnit.SECONDS);
        long deadline = System.currentTimeMillis() + 30_000;
        while (System.currentTimeMillis() < deadline) {
            for (String line : child.stderrLines()) {
                Matcher matcher = LISTENING.matcher(line);
                if (matcher.find()) {
                    return "http://" + matcher.group(1) + ":" + matcher.group(2) + "/mcp";
                }
            }
            Thread.sleep(200);
        }
        throw new IllegalStateException("no Listening line within bound; stderr=" + child.stderrLines());
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
    void live_http_tool_round_trip_is_citrus_controlled() throws Exception {
        assertRunnerIsolation();
        try (ManagedChildProcess child = ManagedChildProcess.launch(childCommand("echo-smoke"))) {
            String baseUrl = baseUrl(child);
            try (LiveMcpClient driver = LiveMcpClient.connectHttp(baseUrl, Duration.ofSeconds(30))) {
                McpSchema.InitializeResult init = driver.initialize();
                assertThat(init.protocolVersion()).isEqualTo("2025-11-25");

                McpSchema.ListToolsResult tools = driver.listTools();
                assertThat(tools.tools()).extracting(McpSchema.Tool::name).contains("echo");

                McpSchema.CallToolResult result = driver.callTool("echo", toolArgs());
                assertThat(result.isError()).isFalse();
                assertThat(((McpSchema.TextContent) result.content().get(0)).text()).contains("12345");

                McpSchema.ReadResourceResult read = driver.readResource("poc://alpha");
                assertThat(((McpSchema.TextResourceContents) read.contents().get(0)).text())
                        .contains("alpha-content");
            }
            assertThat(child.awaitExit(60, TimeUnit.SECONDS)).isEqualTo(0);
            assertThat(child.stderrLines()).anySatisfy(line ->
                    assertThat(line).contains("CITRUS_ASSERTIONS status=ok"));
        }
    }

    @Test
    void live_http_mismatch_fails_tool_call_and_citrus_scenario() throws Exception {
        assertRunnerIsolation();
        boolean sawToolFailure = false;
        try (ManagedChildProcess child = ManagedChildProcess.launch(childCommand("mismatch"))) {
            String baseUrl = baseUrl(child);
            try (LiveMcpClient driver = LiveMcpClient.connectHttp(baseUrl, Duration.ofSeconds(30))) {
                driver.initialize();
                try {
                    McpSchema.CallToolResult result = driver.callTool("echo", toolArgs());
                    sawToolFailure = Boolean.TRUE.equals(result.isError());
                } catch (Exception e) {
                    sawToolFailure = true;
                }
            }
            assertThat(child.awaitExit(60, TimeUnit.SECONDS)).isEqualTo(1);
            assertThat(sawToolFailure).as("client must observe the Citrus mismatch as a tool failure").isTrue();
            assertThat(child.stderrLines()).anySatisfy(line ->
                    assertThat(line).contains("CITRUS_ASSERTIONS status=fail-as-designed"));
            assertThat(child.stderrLines()).anySatisfy(line ->
                    assertThat(line).contains("AssertionError"));
        }
    }
}
