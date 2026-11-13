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
 * Process isolation for two Quarkus HTTP servers (T12-live, AC6 variant).
 *
 * <p>One-JVM dual independent hosts are impossible by construction (a single augmented
 * Quarkus application owns the JVM; the 2.0.2 config model has no multi-server section,
 * only the {@code @McpServer} qualifier whose per-transport routing is unexecuted — see
 * {@code results/quarkus.md}). This separately labeled variant proves the weaker claim:
 * two independent runner processes serve concurrently with distinct sessions and no
 * cross-talk. It does NOT satisfy the one-JVM AC6 cell; that stays BLOCKED with cause.
 */
class QuarkusLiveABIT {

    private static final Pattern LISTENING = Pattern.compile("Listening on: http://([^ :]+):([0-9]+)");

    private static List<String> childCommand(String scenario, String endpoint) {
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
        command.add(endpoint);
        return command;
    }

    private static String javaExe() {
        String home = System.getProperty("java.home");
        String exe = home + java.io.File.separator + "bin" + java.io.File.separator + "java";
        return new java.io.File(exe).exists() ? exe : "java";
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
        throw new IllegalStateException("no Listening line within bound");
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
    void two_independent_processes_serve_concurrently() throws Exception {
        Path deps = Path.of(System.getProperty("user.dir"), "target", "quarkus-app", "quarkus-app-dependencies.txt");
        assertThat(Files.readString(deps, StandardCharsets.UTF_8)).contains("quarkus-mcp-server-http");
        try (ManagedChildProcess childA = ManagedChildProcess.launch(childCommand("echo-smoke", "poc-a"));
             ManagedChildProcess childB = ManagedChildProcess.launch(childCommand("echo-smoke", "poc-b"))) {
            String urlA = baseUrl(childA);
            String urlB = baseUrl(childB);
            assertThat(urlA).isNotEqualTo(urlB);
            try (LiveMcpClient driverA = LiveMcpClient.connectHttp(urlA, Duration.ofSeconds(30));
                 LiveMcpClient driverB = LiveMcpClient.connectHttp(urlB, Duration.ofSeconds(30))) {
                assertThat(driverA.initialize()).isNotNull();
                assertThat(driverB.initialize()).isNotNull();
                McpSchema.CallToolResult resultA = driverA.callTool("echo", toolArgs());
                McpSchema.CallToolResult resultB = driverB.callTool("echo", toolArgs());
                assertThat(resultA.isError()).isFalse();
                assertThat(resultB.isError()).isFalse();
                assertThat(((McpSchema.TextContent) resultA.content().get(0)).text()).contains("12345");
                assertThat(((McpSchema.TextContent) resultB.content().get(0)).text()).contains("12345");
                // Complete the smoke flow (tool + one read) so both children exit bounded.
                driverA.readResource("poc://alpha");
                driverB.readResource("poc://alpha");
            }
            assertThat(childA.awaitExit(60, TimeUnit.SECONDS)).isEqualTo(0);
            assertThat(childB.awaitExit(60, TimeUnit.SECONDS)).isEqualTo(0);
        }
    }
}
