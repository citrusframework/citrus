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

package org.citrusframework.poc.mcp.client;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ClientDriverTest {

    @Test
    void launch_descriptor_requires_exactly_one_server() {
        assertThatThrownBy(() -> LaunchDescriptor.http("quarkus", "http", "s", null, "r.json").build())
                .isInstanceOf(Exception.class);
        assertThatThrownBy(() -> LaunchDescriptor.child("quarkus", "stdio", "s", List.of(), "r.json").build())
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void launch_descriptor_rejects_shell_strings() {
        assertThatThrownBy(() -> LaunchDescriptor.child("quarkus", "stdio", "s",
                List.of("server && rm -rf /"), "r.json").build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("argument array");
    }

    @Test
    void child_process_captures_stdout_and_stderr_separately() throws Exception {
        // Local process fixture: prints to both streams with a bounded exit.
        List<String> args = List.of("/bin/sh", "-c", "echo out-msg; echo err-msg 1>&2");
        ChildProcess.Result result = new ChildProcess(args, 10).run();

        assertThat(result.timedOut()).isFalse();
        assertThat(result.forcedKill()).isFalse();
        assertThat(result.exitCode()).isEqualTo(0);
        assertThat(result.stdout()).contains("out-msg");
        assertThat(result.stderr()).contains("err-msg");
    }

    @Test
    void child_process_timeout_is_recorded_as_failure() throws Exception {
        List<String> args = List.of("/bin/sleep", "30");
        ChildProcess.Result result = new ChildProcess(args, 1).run();

        assertThat(result.timedOut()).isTrue();
        assertThat(result.forcedKill()).isTrue();
    }

    @Test
    void round_trip_runs_five_exchanges_and_writes_evidence(@TempDir Path temp) throws Exception {
        ScenarioDriver driver = new ScenarioDriver(
                (sessionId, requestId, method, requestJson) ->
                        new ScenarioDriver.Exchange(sessionId, requestId, method, requestJson, "{\"ok\": true}", false));

        List<ScenarioDriver.Exchange> exchanges = driver.runRoundTrip("session-1");
        assertThat(exchanges).hasSize(5);
        assertThat(exchanges).extracting(ScenarioDriver.Exchange::method)
                .containsExactly("initialize", "tools/list", "resources/list", "tools/call", "resources/read");

        Path result = temp.resolve("evidence.json");
        LaunchDescriptor descriptor = LaunchDescriptor
                .http("quarkus", "http", "http-roundtrip", "http://127.0.0.1:0/mcp", result.toString())
                .build();
        Path written = driver.writeEvidence(descriptor, exchanges, "probe asserts separately");
        assertThat(Files.readString(written)).contains("\"clientSdk\": \"2.0.1\"");
    }

    @Test
    void concurrent_equal_request_ids_are_issued_for_both_sessions() throws Exception {
        ScenarioDriver driver = new ScenarioDriver(
                (sessionId, requestId, method, requestJson) ->
                        new ScenarioDriver.Exchange(sessionId, requestId, method, requestJson, sessionId, false));

        List<ScenarioDriver.Exchange> exchanges = driver.runConcurrentEqualIds("session-a", "session-b");

        assertThat(exchanges).hasSize(2);
        assertThat(exchanges).extracting(ScenarioDriver.Exchange::requestId)
                .containsExactly("req-1", "req-1");
        assertThat(exchanges).extracting(ScenarioDriver.Exchange::sessionId)
                .containsExactlyInAnyOrder("session-a", "session-b");
    }

    @Test
    void cli_parses_http_and_child_modes(@TempDir Path temp) {
        String result = temp.resolve("r.json").toString();
        LaunchDescriptor http = PocClientMain.parse(new String[]{
                "--candidate", "quarkus", "--transport", "http", "--scenario", "http-roundtrip",
                "--http-uri", "http://127.0.0.1:8080/mcp", "--result-file", result});
        assertThat(http.isHttp()).isTrue();

        LaunchDescriptor child = PocClientMain.parse(new String[]{
                "--candidate", "spring", "--transport", "stdio", "--scenario", "stdio",
                "--child-arg", "java", "--child-arg", "-jar", "--child-arg", "server.jar",
                "--result-file", result});
        assertThat(child.isHttp()).isFalse();
        assertThat(child.getChildArgs()).containsExactly("java", "-jar", "server.jar");
    }
}
