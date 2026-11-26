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

import java.util.List;

import org.citrusframework.poc.mcp.client.ChildProcess;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Subprocess stdio probe for the Quarkus candidate (T10).
 *
 * <p>Topology: the client (parent) launches a server child containing the Citrus
 * scenario and the host. Stdout must stay protocol-only; diagnostics go to stderr.
 * A forced kill is shutdown failure, never success. Two independent children prove
 * process isolation. Live framework stdio (Quarkus transport config, stream
 * ownership, exit behavior) remains the T10 live verdict.
 */
class QuarkusStdioIT {

    private static List<String> childCommand(String scenario) {
        return List.of(
                javaExe(), "-cp", System.getProperty("java.class.path"),
                "org.citrusframework.poc.mcp.quarkus.QuarkusScenarioMain",
                "--scenario", scenario);
    }

    private static String javaExe() {
        String home = System.getProperty("java.home");
        String exe = home + java.io.File.separator + "bin" + java.io.File.separator + "java";
        return new java.io.File(exe).exists() ? exe : "java";
    }

    @Test
    void child_exits_bounded_with_protocol_only_stdout() throws Exception {
        ChildProcess.Result result = new ChildProcess(childCommand("stdio-smoke"), 60).run();

        assertThat(result.timedOut()).isFalse();
        assertThat(result.forcedKill()).isFalse();
        assertThat(result.exitCode()).isEqualTo(0);
        String stdout = result.stdout().trim();
        assertThat(stdout.lines().toList()).hasSize(1);
        assertThat(stdout).contains("\"status\": \"ok\"").contains("\"echoed\": \"12345\"");
        assertThat(result.stderr()).contains("quarkus-scenario");
        assertThat(stdout).doesNotContain("quarkus-scenario");
    }

    @Test
    void two_independent_children_demonstrate_isolation() throws Exception {
        ChildProcess.Result first = new ChildProcess(childCommand("stdio-smoke"), 60).run();
        ChildProcess.Result second = new ChildProcess(childCommand("stdio-smoke"), 60).run();

        assertThat(first.exitCode()).isEqualTo(0);
        assertThat(second.exitCode()).isEqualTo(0);
        assertThat(first.stdout()).contains("\"status\": \"ok\"");
        assertThat(second.stdout()).contains("\"status\": \"ok\"");
    }

    @Test
    void forced_kill_is_recorded_as_failure_not_success() throws Exception {
        ChildProcess.Result result =
                new ChildProcess(List.of("/bin/sleep", "30"), 1).run();

        assertThat(result.timedOut()).isTrue();
        assertThat(result.forcedKill()).isTrue();
    }
}
