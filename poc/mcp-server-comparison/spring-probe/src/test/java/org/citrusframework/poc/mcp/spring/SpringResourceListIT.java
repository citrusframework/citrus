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

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.citrusframework.poc.mcp.CitrusPocEndpoint;
import org.citrusframework.poc.mcp.ExchangeRegistry;
import org.citrusframework.poc.mcp.ScenarioCatalog;
import org.citrusframework.poc.mcp.SimulationReply;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Controlled {@code resources/list} matrix for the Spring candidate (T7).
 *
 * <p>Exercises the simulation semantics the live whole-operation hook must satisfy:
 * pending until reply, no early default, exactly one correlated response, no catalog
 * mutation. The live hook search in pinned Spring AI / SDK 2.0.0 remains the T7 verdict; these tests
 * prove the assertion half, not framework support.
 */
class SpringResourceListIT {

    private static CitrusPocEndpoint bridge(ExchangeRegistry registry) {
        return new CitrusPocEndpoint(registry);
    }

    @Test
    void empty_catalog_stays_pending_until_empty_reply() throws Exception {
        ExchangeRegistry registry = new ExchangeRegistry();
        CitrusPocEndpoint endpoint = bridge(registry);
        ControlledResourceList controlled = new ControlledResourceList(registry);
        endpoint.expect("resources/list", "list", "{}");

        String token = endpoint.receive("poc-a", 0, "session-1", "req-1", "resources/list", "list", "{}");

        // Barrier proves pending: the waiter blocks, the exchange stays pending, no early default.
        CompletableFuture<SimulationReply> waiter =
                CompletableFuture.supplyAsync(() -> {
                    try {
                        return registry.await(token, 5, TimeUnit.SECONDS);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
        Thread.sleep(200);
        assertThat(controlled.isPending(token)).isTrue();
        assertThat(waiter.isDone()).isFalse();

        controlled.replyWithFixture(token, "empty");
        SimulationReply reply = waiter.get(5, TimeUnit.SECONDS);
        assertThat(reply.getKind()).isEqualTo(SimulationReply.Kind.RESOURCE_LIST_RESULT);
        assertThat(reply.getJsonPayload()).contains("\"resources\": []");
        assertThat(registry.isPending(token)).isFalse();
    }

    @Test
    void ordered_subset_replies_exact_content() throws Exception {
        ExchangeRegistry registry = new ExchangeRegistry();
        CitrusPocEndpoint endpoint = bridge(registry);
        ControlledResourceList controlled = new ControlledResourceList(registry);
        endpoint.expect("resources/list", "list", "{}");

        String token = endpoint.receive("poc-a", 0, "session-1", "req-1", "resources/list", "list", "{}");
        controlled.replyWithFixture(token, "beta-alpha");

        SimulationReply reply = registry.await(token, 1, TimeUnit.SECONDS);
        String payload = reply.getJsonPayload();
        assertThat(payload.indexOf("poc://beta")).isLessThan(payload.indexOf("poc://alpha"));
    }

    @Test
    void explicit_json_rpc_error_is_not_a_tool_result() throws Exception {
        ExchangeRegistry registry = new ExchangeRegistry();
        CitrusPocEndpoint endpoint = bridge(registry);
        ControlledResourceList controlled = new ControlledResourceList(registry);
        endpoint.expect("resources/list", "list", "{}");

        String token = endpoint.receive("poc-a", 0, "session-1", "req-1", "resources/list", "list", "{}");
        controlled.replyWithError(token, -32001, "simulated list failure");

        SimulationReply reply = registry.await(token, 1, TimeUnit.SECONDS);
        assertThat(reply.getKind()).isEqualTo(SimulationReply.Kind.JSON_RPC_ERROR);
        assertThat(reply.getJsonRpcCode()).isEqualTo(-32001);
    }

    @Test
    void simulation_timeout_expires_and_cleans_up() {
        ExchangeRegistry registry = new ExchangeRegistry();
        CitrusPocEndpoint endpoint = bridge(registry);
        endpoint.expect("resources/list", "list", "{}");

        String token = endpoint.receive("poc-a", 0, "session-1", "req-1", "resources/list", "list", "{}");
        assertThatThrownBy(() -> registry.await(token, 50, TimeUnit.MILLISECONDS))
                .isInstanceOf(TimeoutException.class);
        assertThat(registry.terminalState(token)).isEqualTo(ExchangeRegistry.Terminal.TIMEOUT);
    }

    @Test
    void concurrent_requests_reply_in_reverse_order_without_cross_talk() throws Exception {
        ExchangeRegistry registry = new ExchangeRegistry();
        CitrusPocEndpoint endpoint = bridge(registry);
        ControlledResourceList controlled = new ControlledResourceList(registry);

        endpoint.expect("resources/list", "list", "{}");
        String first = endpoint.receive("poc-a", 0, "session-1", "req-1", "resources/list", "list", "{}");
        endpoint.expect("resources/list", "list", "{}");
        String second = endpoint.receive("poc-a", 0, "session-2", "req-1", "resources/list", "list", "{}");
        assertThat(first).isNotEqualTo(second);

        // Reverse order: second session answered first.
        controlled.replyWithFixture(second, "beta");
        controlled.replyWithFixture(first, "alpha");

        assertThat(registry.await(first, 1, TimeUnit.SECONDS).getJsonPayload()).contains("poc://alpha");
        assertThat(registry.await(second, 1, TimeUnit.SECONDS).getJsonPayload()).contains("poc://beta");
    }

    @Test
    void replies_never_mutate_the_static_catalog() throws Exception {
        ExchangeRegistry registry = new ExchangeRegistry();
        CitrusPocEndpoint endpoint = bridge(registry);
        ControlledResourceList controlled = new ControlledResourceList(registry);
        String before = controlled.staticCatalog();

        endpoint.expect("resources/list", "list", "{}");
        String token = endpoint.receive("poc-a", 0, "session-1", "req-1", "resources/list", "list", "{}");
        controlled.replyWithFixture(token, "empty");
        registry.await(token, 1, TimeUnit.SECONDS);

        assertThat(controlled.staticCatalog()).isEqualTo(before);
        assertThat(before).contains("poc://alpha").contains("poc://beta");
        assertThat(ScenarioCatalog.resourceContents()).containsKeys("poc://alpha", "poc://beta");
    }
}
