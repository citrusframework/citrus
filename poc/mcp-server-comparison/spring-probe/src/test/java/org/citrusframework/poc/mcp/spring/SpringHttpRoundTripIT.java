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

import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.citrusframework.poc.mcp.CitrusPocEndpoint;
import org.citrusframework.poc.mcp.ExchangeRegistry;
import org.citrusframework.poc.mcp.ScenarioCatalog;
import org.citrusframework.poc.mcp.SimulationReply;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * HTTP tool/resource round trip for the Spring candidate (T9).
 *
 * <p>Static discovery is the control case independent of whole-list success.
 * Every dispatch goes through a real Citrus receive/send bridge pair: the test
 * declares the expectation, the feature dispatches, the scenario replies, and a
 * deliberate mismatch fails. Live transport framing (Streamable HTTP, loopback
 * binding, negotiation) remains the T8 live verdict.
 */
class SpringHttpRoundTripIT {

    @Test
    void discovery_metadata_is_complete() {
        SpringFeatures features = new SpringFeatures(new CitrusPocEndpoint(new ExchangeRegistry()));

        Map<String, String> tool = features.toolDescriptor();
        assertThat(tool.get("name")).isEqualTo("echo");
        assertThat(tool.get("description")).isNotBlank();
        assertThat(tool.get("inputSchema")).contains("\"nested\"").contains("\"maybeNull\"");

        assertThat(features.resourceDescriptor("poc://alpha").get("mimeType")).isEqualTo("text/plain");
        assertThat(features.resourceDescriptor("poc://beta").get("text")).isEqualTo("beta-content");
        assertThat(features.resourceDescriptor("poc://unknown").get("unknown")).isEqualTo("true");
    }

    @Test
    void typed_tool_call_resolves_variable_and_returns_result() throws Exception {
        ExchangeRegistry registry = new ExchangeRegistry();
        CitrusPocEndpoint endpoint = new CitrusPocEndpoint(registry);
        endpoint.setVariable("id", "12345");
        endpoint.expect("tools/call", "echo", ScenarioCatalog.sampleToolArguments("${id}"));
        SpringFeatures features = new SpringFeatures(endpoint);

        String args = ScenarioCatalog.sampleToolArguments("12345");
        java.util.concurrent.CompletableFuture<SimulationReply> call =
                java.util.concurrent.CompletableFuture.supplyAsync(() -> {
                    try {
                        return features.dispatchToolCall("poc-a", 0, "session-1", "req-1",
                                "echo", args, 5);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
        Thread.sleep(200);
        String token = registryStopgapToken(registry);
        endpoint.reply(token, SimulationReply.toolResult("{\"echoed\": \"12345\"}"));

        assertThat(call.get(5, TimeUnit.SECONDS).getJsonPayload()).contains("12345");
    }

    @Test
    void resource_read_returns_text_content() throws Exception {
        ExchangeRegistry registry = new ExchangeRegistry();
        CitrusPocEndpoint endpoint = new CitrusPocEndpoint(registry);
        endpoint.expect("resources/read", "poc://alpha", "{\"uri\": \"poc://alpha\"}");
        SpringFeatures features = new SpringFeatures(endpoint);

        java.util.concurrent.CompletableFuture<SimulationReply> call =
                java.util.concurrent.CompletableFuture.supplyAsync(() -> {
                    try {
                        return features.dispatchResourceRead("poc-a", 0, "session-1", "req-1",
                                "poc://alpha", 5);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
        Thread.sleep(200);
        endpoint.reply(registryStopgapToken(registry),
                SimulationReply.resourceReadResult("{\"text\": \"alpha-content\"}"));

        assertThat(call.get(5, TimeUnit.SECONDS).getJsonPayload()).contains("alpha-content");
    }

    @Test
    void tool_error_stays_a_tool_result_and_mismatch_fails() {
        SimulationReply error = SimulationReply.toolErrorResult("{\"error\": \"intentional\"}");
        assertThat(error.getKind()).isEqualTo(SimulationReply.Kind.TOOL_RESULT);
        assertThat(error.isToolError()).isTrue();

        ExchangeRegistry registry = new ExchangeRegistry();
        CitrusPocEndpoint endpoint = new CitrusPocEndpoint(registry);
        endpoint.expect("tools/call", "echo", "{\"count\": 3}");
        assertThatThrownBy(() -> endpoint.receive("poc-a", 0, "session-1", "req-1",
                "tools/call", "echo", "{\"count\": 4}"))
                .isInstanceOf(AssertionError.class);
    }

    private static String registryStopgapToken(ExchangeRegistry registry) {
        // Single pending exchange in these focused tests; the token is opaque to the
        // scenario but visible to the probe for correlation assertions.
        assertThat(registry.pendingTokens()).hasSize(1);
        return registry.pendingTokens().iterator().next();
    }
}
