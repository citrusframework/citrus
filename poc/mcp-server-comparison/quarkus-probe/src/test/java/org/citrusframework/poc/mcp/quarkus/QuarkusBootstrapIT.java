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

import java.net.ServerSocket;

import org.citrusframework.poc.mcp.CitrusPocEndpoint;
import org.citrusframework.poc.mcp.ExchangeRegistry;
import org.citrusframework.poc.mcp.HostHandle;
import org.citrusframework.poc.mcp.LifecycleState;
import org.citrusframework.poc.mcp.PocConfig;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Ordinary-Java same-JVM bootstrap probe for the Quarkus candidate (T4).
 * No {@code @QuarkusTest} or framework test bootstrap: an ordinary JUnit entry
 * point owns the Citrus bridge and the host in this JVM.
 */
class QuarkusBootstrapIT {

    private static CitrusPocEndpoint bridge() {
        return new CitrusPocEndpoint(new ExchangeRegistry());
    }

    private static PocConfig httpConfig(int port) {
        return PocConfig.builder(PocConfig.Candidate.QUARKUS, PocConfig.Transport.HTTP)
                .endpointId("poc-quarkus")
                .port(port)
                .scenarioId("bootstrap")
                .build();
    }

    @Test
    void construction_opens_no_listener_and_start_is_same_jvm() throws Exception {
        long pid = ProcessHandle.current().pid();
        QuarkusPocHost host = new QuarkusPocHost();

        assertThat(host.state()).isEqualTo(LifecycleState.NEW);
        assertThat(host.ownerPid()).isEqualTo(-1);

        HostHandle handle = host.start(httpConfig(0), bridge());

        assertThat(host.state()).isEqualTo(LifecycleState.RUNNING);
        assertThat(host.ownerPid()).isEqualTo(pid);
        assertThat(handle.getCandidate()).isEqualTo(PocConfig.Candidate.QUARKUS);
        assertThat(handle.getGeneration()).isEqualTo(0);
        assertThat(handle.getTransportIdentity()).startsWith("127.0.0.1:").endsWith("/mcp");

        // Normal caller survives after stop; repeated stop is safe.
        host.stop();
        assertThat(host.state()).isEqualTo(LifecycleState.STOPPED);
        host.stop();
        assertThat(host.state()).isEqualTo(LifecycleState.STOPPED);
        assertThat(ProcessHandle.current().pid()).isEqualTo(pid);
    }

    @Test
    void repeated_start_while_running_is_noop_and_restart_needs_fresh_host() throws Exception {
        QuarkusPocHost host = new QuarkusPocHost();
        HostHandle first = host.start(httpConfig(0), bridge());
        HostHandle second = host.start(httpConfig(0), bridge());
        assertThat(second).isSameAs(first);

        host.stop();
        assertThatThrownBy(() -> host.start(httpConfig(0).nextGeneration(), bridge()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("fresh host instance");

        QuarkusPocHost next = new QuarkusPocHost();
        HostHandle restarted = next.start(httpConfig(0).nextGeneration(), bridge());
        assertThat(restarted.getGeneration()).isEqualTo(1);
        next.stop();
    }

    @Test
    void failed_bind_cleans_up_and_preserves_cause() throws Exception {
        int occupied;
        try (ServerSocket blocker = new ServerSocket(0, 50, java.net.InetAddress.getByName("127.0.0.1"))) {
            occupied = blocker.getLocalPort();
            QuarkusPocHost host = new QuarkusPocHost();
            assertThatThrownBy(() -> host.start(httpConfig(occupied), bridge()))
                    .isInstanceOf(java.net.BindException.class);
            assertThat(host.state()).isEqualTo(LifecycleState.FAILED);
            host.stop();
            assertThat(host.state()).isEqualTo(LifecycleState.STOPPED);
        }
        // Port reusable after the blocker is gone.
        QuarkusPocHost reuse = new QuarkusPocHost();
        HostHandle handle = reuse.start(httpConfig(occupied), bridge());
        assertThat(handle.getTransportIdentity()).contains(String.valueOf(occupied));
        reuse.stop();
    }
}
