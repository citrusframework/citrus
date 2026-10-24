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

import org.citrusframework.poc.mcp.CitrusPocEndpoint;
import org.citrusframework.poc.mcp.ExchangeRegistry;
import org.citrusframework.poc.mcp.HostHandle;
import org.citrusframework.poc.mcp.PocConfig;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Lifecycle and endpoint isolation for the Quarkus candidate (T12).
 *
 * <p>Two HTTP endpoints A/B with distinct catalogs and ports in one JVM; two client
 * sessions with equal request IDs; replies in reverse order. Stop and recreate A
 * while B keeps serving. A named-path shared host would be a separately labeled
 * result, never a pass for independent-host AC6. Global shutdown of an unrelated
 * host is never used to force cleanup success.
 */
class QuarkusLifecycleIT {

    private static PocConfig config(String endpointId, int port, int generation) {
        return PocConfig.builder(PocConfig.Candidate.QUARKUS, PocConfig.Transport.HTTP)
                .endpointId(endpointId)
                .port(port)
                .scenarioId("isolation-ab")
                .generation(generation)
                .build();
    }

    @Test
    void three_generations_start_stop_cleanly() throws Exception {
        CitrusPocEndpoint bridge = new CitrusPocEndpoint(new ExchangeRegistry());
        for (int generation = 0; generation < 3; generation++) {
            QuarkusPocHost host = new QuarkusPocHost();
            HostHandle handle = host.start(config("poc-a", 0, generation), bridge);
            assertThat(handle.getGeneration()).isEqualTo(generation);
            host.stop();
            assertThat(host.state()).isEqualTo(org.citrusframework.poc.mcp.LifecycleState.STOPPED);
        }
    }

    @Test
    void two_endpoints_isolate_catalogs_and_sessions() throws Exception {
        ExchangeRegistry registryA = new ExchangeRegistry();
        ExchangeRegistry registryB = new ExchangeRegistry();
        CitrusPocEndpoint bridgeA = new CitrusPocEndpoint(registryA);
        CitrusPocEndpoint bridgeB = new CitrusPocEndpoint(registryB);

        QuarkusPocHost hostA = new QuarkusPocHost();
        QuarkusPocHost hostB = new QuarkusPocHost();
        HostHandle handleA = hostA.start(config("poc-a", 0, 0), bridgeA);
        HostHandle handleB = hostB.start(config("poc-b", 0, 0), bridgeB);
        assertThat(handleA.getTransportIdentity()).isNotEqualTo(handleB.getTransportIdentity());

        // Equal request IDs in two sessions; distinct catalogs per endpoint.
        bridgeA.expect("resources/list", "list", "{}");
        bridgeB.expect("resources/list", "list", "{}");
        String tokenA = bridgeA.receive("poc-a", 0, "session-1", "req-1", "resources/list", "list", "{}");
        String tokenB = bridgeB.receive("poc-b", 0, "session-2", "req-1", "resources/list", "list", "{}");

        ControlledResourceList controlledA = new ControlledResourceList(registryA);
        ControlledResourceList controlledB = new ControlledResourceList(registryB);
        // Reverse order: B answered before A.
        controlledB.replyWithFixture(tokenB, "beta");
        controlledA.replyWithFixture(tokenA, "alpha");

        assertThat(registryA.await(tokenA, 1, java.util.concurrent.TimeUnit.SECONDS).getJsonPayload())
                .contains("poc://alpha");
        assertThat(registryB.await(tokenB, 1, java.util.concurrent.TimeUnit.SECONDS).getJsonPayload())
                .contains("poc://beta");

        // Stop and recreate A while B keeps serving.
        hostA.stop();
        assertThat(registryB.pendingCount()).isEqualTo(0);
        QuarkusPocHost hostA2 = new QuarkusPocHost();
        HostHandle handleA2 = hostA2.start(config("poc-a", 0, 1), bridgeA);
        assertThat(handleA2.getGeneration()).isEqualTo(1);

        bridgeB.expect("resources/list", "list", "{}");
        String stillB = bridgeB.receive("poc-b", 0, "session-2", "req-2", "resources/list", "list", "{}");
        controlledB.replyWithFixture(stillB, "beta-alpha");
        assertThat(registryB.await(stillB, 1, java.util.concurrent.TimeUnit.SECONDS).getJsonPayload())
                .contains("poc://alpha");

        hostA2.stop();
        hostB.stop();
    }

    @Test
    void stop_with_pending_work_marks_exchanges_stopped() throws Exception {
        ExchangeRegistry registry = new ExchangeRegistry();
        CitrusPocEndpoint bridge = new CitrusPocEndpoint(registry);
        QuarkusPocHost host = new QuarkusPocHost();
        host.start(config("poc-a", 0, 0), bridge);

        bridge.expect("resources/list", "list", "{}");
        String token = bridge.receive("poc-a", 0, "session-1", "req-1", "resources/list", "list", "{}");
        assertThat(registry.isPending(token)).isTrue();

        host.stop();
        int stopped = registry.stopGeneration("poc-a", 0);
        assertThat(stopped).isEqualTo(1);
        assertThat(registry.terminalState(token)).isEqualTo(ExchangeRegistry.Terminal.STOP);
        // Stale-generation reply rejected; cannot satisfy another receive.
        assertThat(registry.reply(token,
                org.citrusframework.poc.mcp.SimulationReply.resourceListResult("{}"))).isFalse();
    }
}
