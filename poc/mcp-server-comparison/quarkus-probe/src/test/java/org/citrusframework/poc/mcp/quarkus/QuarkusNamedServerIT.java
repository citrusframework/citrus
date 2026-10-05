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
 * Named-server declaration requirement spike (T12-live, AC6).
 *
 * <p>The runner carries a second {@code @McpServer("poc-b")} feature set
 * ({@code CitrusMcpFeaturesB}). Because no {@code servers.*} declaration binds from
 * configuration in pinned 2.0.2 (two syntaxes warned unrecognized; qualifier alone
 * fails validation), the runner sets {@code invalid-server-name-strategy=IGNORE} and
 * this test pins the outcome: the default server serves alone while poc-b features
 * are silently dropped. If a future release routes named servers, discovery changes
 * and this test fails — reopening the one-JVM A/B investigation with a positive probe.
 */
class QuarkusNamedServerIT {

    private static List<String> childCommand() {
        Path runner = Path.of(System.getProperty("user.dir"), "target", "quarkus-app", "quarkus-run.jar");
        assertThat(runner).as("augmented runner (package phase must have run)").exists();
        List<String> command = new ArrayList<>();
        command.add(javaExe());
        command.add("-Dquarkus.mcp.server.http.enabled=false");
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
    void undeclared_named_server_features_are_dropped() throws Exception {
        Path deps = Path.of(System.getProperty("user.dir"), "target", "quarkus-app", "quarkus-app-dependencies.txt");
        assertThat(Files.readString(deps, StandardCharsets.UTF_8)).contains("quarkus-mcp-server-stdio");
        try (LiveMcpClient driver = LiveMcpClient.launchStdio(childCommand(), Duration.ofSeconds(30))) {
            driver.initialize();
            McpSchema.ListToolsResult tools = driver.listTools();
            assertThat(tools.tools()).extracting(McpSchema.Tool::name).contains("echo");
            assertThat(tools.tools()).extracting(McpSchema.Tool::name).doesNotContain("echoB");
            McpSchema.ListResourcesResult resources = driver.listResources();
            assertThat(resources.resources()).extracting(McpSchema.Resource::uri)
                    .contains("poc://alpha", "poc://beta");
            assertThat(resources.resources()).extracting(McpSchema.Resource::uri)
                    .doesNotContain("poc://gamma", "poc://delta");

            // Complete the smoke flow so the child exits bounded.
            McpSchema.CallToolResult result = driver.callTool("echo", toolArgs());
            assertThat(result.isError()).isFalse();
            driver.readResource("poc://alpha");
            driver.awaitChildExit(60, TimeUnit.SECONDS);
            assertThat(driver.childStderr()).anySatisfy(line ->
                    assertThat(line).contains("CITRUS_ASSERTIONS status=ok"));
        }
    }
}
