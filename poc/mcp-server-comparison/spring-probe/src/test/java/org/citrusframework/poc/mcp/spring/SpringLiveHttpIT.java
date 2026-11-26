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
import org.citrusframework.poc.mcp.CitrusPocEndpoint;
import org.citrusframework.poc.mcp.ExchangeRegistry;
import org.citrusframework.poc.mcp.HostHandle;
import org.citrusframework.poc.mcp.LifecycleState;
import org.citrusframework.poc.mcp.PocConfig;
import org.citrusframework.poc.mcp.client.LiveMcpClient;
import org.citrusframework.poc.mcp.client.ManagedChildProcess;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Live Spring AI MCP server over Streamable HTTP (T9-live).
 *
 * <p>Two hosting proofs: (1) the version-pure server child (pinned SDK 2.0.0, asserted
 * pre-launch) driven by the SDK 2.0.1 client over loopback HTTP; (2) an in-probe
 * same-JVM start/stop proving ordinary-Java HTTP embedding in the probe JVM itself
 * (lifecycle only — the probe JVM resolves SDK 2.0.1, recorded as an explicit variant,
 * so wire traffic stays in the child).
 */
class SpringLiveHttpIT {

    private static List<String> childCommand(String scenario, String serverClasspath) {
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
        command.add("--transport");
        command.add("http");
        command.add("--port");
        command.add("0");
        command.add("--scenario");
        command.add(scenario);
        command.add("--endpoint");
        command.add("poc-spring-http");
        return command;
    }

    private static String javaExe() {
        String home = System.getProperty("java.home");
        String exe = home + java.io.File.separator + "bin" + java.io.File.separator + "java";
        return new java.io.File(exe).exists() ? exe : "java";
    }

    private static String serverClasspath() throws Exception {
        Path file = Path.of(System.getProperty("user.dir"), "..", "spring-host", "target", "server-classpath.txt");
        assertThat(file).as("server classpath file (run a build first)").exists();
        String classpath = Files.readString(file, StandardCharsets.UTF_8).trim();
        assertThat(classpath).as("server keeps pinned SDK 2.0.0").contains("mcp-core-2.0.0");
        assertThat(classpath).as("client SDK 2.0.1 must not leak into the server child").doesNotContain("mcp-core-2.0.1");
        assertThat(classpath).contains("spring-ai-starter-mcp-server-webmvc");
        return classpath;
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
    void in_probe_http_start_stop_is_same_jvm_lifecycle_only() throws Exception {
        // Explicit variant: the probe JVM resolves the 2.0.1 client SDK, so this proves
        // same-JVM HTTP embedding mechanics only; wire traffic stays in the child below.
        SpringLiveHost.LiveSupplierHolder.offer((operation, selector, payload) ->
                org.citrusframework.poc.mcp.SimulationReply.toolResult("{}"));
        PocConfig config = PocConfig.builder(PocConfig.Candidate.SPRING, PocConfig.Transport.HTTP)
                .endpointId("poc-spring-http-inprobe")
                .port(0)
                .scenarioId("http-lifecycle")
                .build();
        SpringLiveHost host = new SpringLiveHost();
        CitrusPocEndpoint bridge = new CitrusPocEndpoint(new ExchangeRegistry());
        HostHandle handle = host.start(config, bridge);
        try {
            assertThat(host.state()).isEqualTo(LifecycleState.RUNNING);
            assertThat(host.ownerPid()).isEqualTo(ProcessHandle.current().pid());
            assertThat(handle.getTransportIdentity()).startsWith("127.0.0.1:").endsWith("/mcp");
            assertThat(host.serverProtocolOwner()).contains("McpSyncServer");
        } finally {
            host.stop();
            assertThat(host.state()).isEqualTo(LifecycleState.STOPPED);
        }
    }

    @Test
    void live_http_tool_round_trip_is_citrus_controlled() throws Exception {
        String serverClasspath = serverClasspath();
        try (ManagedChildProcess child =
                     ManagedChildProcess.launch(childCommand("echo-smoke", serverClasspath))) {
            String ready = child.awaitStdoutLine(line -> line.startsWith("HTTP_READY "), 60, TimeUnit.SECONDS);
            String baseUrl = ready.substring("HTTP_READY ".length()).trim();
            assertThat(baseUrl).startsWith("http://127.0.0.1:");

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
        String serverClasspath = serverClasspath();
        boolean sawToolFailure = false;
        try (ManagedChildProcess child =
                     ManagedChildProcess.launch(childCommand("mismatch", serverClasspath))) {
            String ready = child.awaitStdoutLine(line -> line.startsWith("HTTP_READY "), 60, TimeUnit.SECONDS);
            String baseUrl = ready.substring("HTTP_READY ".length()).trim();
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
