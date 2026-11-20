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

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.citrusframework.poc.mcp.CitrusPocEndpoint;
import org.citrusframework.poc.mcp.ExchangeRegistry;
import org.citrusframework.poc.mcp.HostHandle;
import org.citrusframework.poc.mcp.LifecycleState;
import org.citrusframework.poc.mcp.PocConfig;
import org.citrusframework.poc.mcp.SimulationReply;
import org.citrusframework.poc.mcp.client.RawStreamableClient;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Live endpoint isolation for the Spring candidate (T13-live, AC6).
 *
 * <p>Two HTTP endpoints A/B with distinct ports in ONE probe JVM, each with its own
 * owned Boot context, bridge and catalog marker. Two client sessions initialize with
 * EQUAL numeric request IDs; replies complete in reverse order (B before A) and must
 * not cross sessions. Stopping A while B keeps serving proves independent lifecycles.
 * Raw JSON-RPC IDs are used because the SDK client manages IDs itself.
 */
class SpringLiveABIT {

    private static final String ARGS;

    static {
        try {
            Map<String, Object> args = new LinkedHashMap<>();
            args.put("nested", Map.of("value", "12345"));
            args.put("items", java.util.List.of("a", "b"));
            args.put("count", 3);
            args.put("flag", true);
            ARGS = new ObjectMapper().writeValueAsString(args);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private record Host(CitrusPocEndpoint bridge, SpringLiveHost host, HostHandle handle, String baseUrl) {
    }

    private static Host startHost(String endpointId, String marker) throws Exception {
        ExchangeRegistry registry = new ExchangeRegistry();
        CitrusPocEndpoint bridge = new CitrusPocEndpoint(registry);
        bridge.expect("tools/call", "echo", ARGS);
        SpringLiveHost.LiveSupplierHolder.offer((operation, selector, payload) -> {
            // Re-arm the same tool expectation: every call in this test is a tool call.
            bridge.expect("tools/call", "echo", ARGS);
            return SimulationReply.toolResult("{\"servedBy\": \"" + marker + "\"}");
        });
        PocConfig config = PocConfig.builder(PocConfig.Candidate.SPRING, PocConfig.Transport.HTTP)
                .endpointId(endpointId)
                .port(0)
                .scenarioId("isolation-ab")
                .build();
        SpringLiveHost host = new SpringLiveHost();
        HostHandle handle = host.start(config, bridge);
        assertThat(host.ownerPid()).isEqualTo(ProcessHandle.current().pid());
        return new Host(bridge, host, handle, "http://" + handle.getTransportIdentity());
    }

    @Test
    void two_endpoints_isolate_sessions_and_lifecycles() throws Exception {
        Host hostA = startHost("poc-a", "a");
        Host hostB = startHost("poc-b", "b");
        try {
            assertThat(hostA.handle().getTransportIdentity())
                    .isNotEqualTo(hostB.handle().getTransportIdentity());

            RawStreamableClient clientA = new RawStreamableClient(hostA.baseUrl(), Duration.ofSeconds(30));
            RawStreamableClient clientB = new RawStreamableClient(hostB.baseUrl(), Duration.ofSeconds(30));
            String sessionA = clientA.initialize();
            String sessionB = clientB.initialize();
            assertThat(sessionA).isNotBlank();
            assertThat(sessionB).isNotBlank();
            assertThat(sessionA).isNotEqualTo(sessionB);

            // Equal request IDs on both sessions; complete B before A (reverse order).
            CompletableFuture<RawStreamableClient.Response> callA = CompletableFuture.supplyAsync(() -> {
                try {
                    return clientA.post(sessionA, "1", "tools/call",
                            "{\"name\": \"echo\", \"arguments\": " + ARGS + "}");
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
            CompletableFuture<RawStreamableClient.Response> callB = CompletableFuture.supplyAsync(() -> {
                try {
                    return clientB.post(sessionB, "1", "tools/call",
                            "{\"name\": \"echo\", \"arguments\": " + ARGS + "}");
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
            RawStreamableClient.Response responseB = callB.get(30, TimeUnit.SECONDS);
            RawStreamableClient.Response responseA = callA.get(30, TimeUnit.SECONDS);
            assertThat(responseA.isError()).isFalse();
            assertThat(responseB.isError()).isFalse();
            // Result payloads are JSON-escaped text content: {\"servedBy\": \"a\"}.
            assertThat(responseA.resultJson()).contains("servedBy").contains("\\\"a\\\"");
            assertThat(responseB.resultJson()).contains("servedBy").contains("\\\"b\\\"");

            // Stop A while B keeps serving.
            hostA.host().stop();
            assertThat(hostA.host().state()).isEqualTo(LifecycleState.STOPPED);
            assertThat(hostB.host().state()).isEqualTo(
                    org.citrusframework.poc.mcp.LifecycleState.RUNNING);

            RawStreamableClient.Response stillB = clientB.post(sessionB, "2", "tools/call",
                    "{\"name\": \"echo\", \"arguments\": " + ARGS + "}");
            assertThat(stillB.isError()).isFalse();
            assertThat(stillB.resultJson()).contains("servedBy").contains("\\\"b\\\"");

            // A's listener is gone: new sessions are refused, B is unaffected.
            RawStreamableClient deadA = new RawStreamableClient(hostA.baseUrl(), Duration.ofSeconds(5));
            assertThatThrownBy(deadA::initialize).isInstanceOf(Exception.class);
        } finally {
            try {
                hostA.host().stop();
            } finally {
                hostB.host().stop();
            }
        }
    }
}
