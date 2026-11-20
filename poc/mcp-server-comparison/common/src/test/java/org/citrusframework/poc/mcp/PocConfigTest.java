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

package org.citrusframework.poc.mcp;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PocConfigTest {

    @Test
    void construction_performs_no_io_and_defaults_are_loopback() {
        PocConfig config = PocConfig.builder(PocConfig.Candidate.QUARKUS, PocConfig.Transport.HTTP)
                .port(0)
                .build();

        assertThat(config.getLoopbackHost()).isEqualTo("127.0.0.1");
        assertThat(config.getMcpPath()).isEqualTo("/mcp");
        assertThat(config.getStartupTimeoutSeconds()).isEqualTo(60);
        assertThat(config.getSimulationTimeoutSeconds()).isEqualTo(5);
        assertThat(config.getStopTimeoutSeconds()).isEqualTo(10);
        assertThat(config.nextGeneration().getGeneration()).isEqualTo(1);
    }

    @Test
    void non_loopback_host_is_rejected() {
        assertThatThrownBy(() -> PocConfig.builder(PocConfig.Candidate.SPRING, PocConfig.Transport.HTTP)
                .loopbackHost("example.com")
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("loopback");
    }

    @Test
    void base_uri_requires_http() {
        PocConfig http = PocConfig.builder(PocConfig.Candidate.QUARKUS, PocConfig.Transport.HTTP)
                .port(8080)
                .build();
        assertThat(http.baseUri()).isEqualTo("http://127.0.0.1:8080/mcp");

        PocConfig stdio = PocConfig.builder(PocConfig.Candidate.QUARKUS, PocConfig.Transport.STDIO)
                .build();
        assertThatThrownBy(stdio::baseUri).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void scenario_catalog_covers_shared_contract() {
        assertThat(ScenarioCatalog.TOOL_NAME).isEqualTo("echo");
        assertThat(ScenarioCatalog.toolInputSchema()).contains("\"nested\"");
        assertThat(ScenarioCatalog.sampleToolArguments("x")).contains("\"maybeNull\": null");
        assertThat(ScenarioCatalog.sampleToolArguments("x")).doesNotContain("optional");
        assertThat(ScenarioCatalog.resourceListFixture("empty")).contains("\"resources\": []");
        assertThat(ScenarioCatalog.resourceListFixture("beta-alpha").indexOf("beta"))
                .isLessThan(ScenarioCatalog.resourceListFixture("beta-alpha").indexOf("alpha"));
        assertThatThrownBy(() -> ScenarioCatalog.resourceListFixture("unknown"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void evidence_row_starts_not_run() {
        EvidenceRow row = new EvidenceRow("quarkus", "2.0.2", "same-JVM", "http",
                "http-roundtrip", "candidate-native", "17");
        assertThat(row.getOutcome()).isEqualTo(EvidenceRow.Outcome.NOT_RUN);
        assertThat(row.toMarkdownRow()).contains("NOT_RUN");
    }
}
