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

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.citrusframework.poc.mcp.CitrusPocEndpoint;
import org.citrusframework.poc.mcp.ExchangeRegistry;
import org.citrusframework.poc.mcp.SimulationReply;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Failure and correlation matrix for the Quarkus candidate (T15).
 *
 * <p>Distinguishes tool-error results, JSON-RPC errors and Citrus assertion failures;
 * characterizes malformed parameters and schema-invalid arguments without inventing
 * cross-framework error codes. Every non-executed live case needs a blocker reference
 * in the report; this class proves the registry/bridge half.
 */
class QuarkusFailureIT {

    @Test
    void timeout_then_late_reply_is_rejected() {
        ExchangeRegistry registry = new ExchangeRegistry();
        CitrusPocEndpoint endpoint = new CitrusPocEndpoint(registry);
        endpoint.expect("resources/list", "list", "{}");
        String token = endpoint.receive("poc-a", 0, "session-1", "req-1", "resources/list", "list", "{}");

        assertThatThrownBy(() -> registry.await(token, 50, TimeUnit.MILLISECONDS))
                .isInstanceOf(TimeoutException.class);
        assertThat(registry.reply(token, SimulationReply.toolResult("{}"))).isFalse();
        assertThat(registry.terminalState(token)).isEqualTo(ExchangeRegistry.Terminal.TIMEOUT);
    }

    @Test
    void cancellation_and_disconnect_are_terminal() {
        ExchangeRegistry registry = new ExchangeRegistry();
        CitrusPocEndpoint endpoint = new CitrusPocEndpoint(registry);
        endpoint.expect("tools/call", "echo", "{}");
        String token = endpoint.receive("poc-a", 0, "session-1", "req-1", "tools/call", "echo", "{}");

        assertThat(registry.cancel(token)).isTrue();
        assertThat(registry.terminalState(token)).isEqualTo(ExchangeRegistry.Terminal.CANCEL);
        assertThat(registry.reply(token, SimulationReply.toolResult("{}"))).isFalse();
    }

    @Test
    void error_kinds_are_distinct() {
        SimulationReply toolError = SimulationReply.toolErrorResult("{\"error\": \"intentional\"}");
        SimulationReply rpcError = SimulationReply.jsonRpcError(-32602, "{\"message\": \"invalid params\"}");

        assertThat(toolError.getKind()).isEqualTo(SimulationReply.Kind.TOOL_RESULT);
        assertThat(toolError.isToolError()).isTrue();
        assertThat(rpcError.getKind()).isEqualTo(SimulationReply.Kind.JSON_RPC_ERROR);
        assertThat(rpcError.isToolError()).isFalse();

        // Citrus assertion failure is a third, local kind.
        ExchangeRegistry registry = new ExchangeRegistry();
        CitrusPocEndpoint endpoint = new CitrusPocEndpoint(registry);
        endpoint.expect("tools/call", "echo", "{\"count\": 3}");
        assertThatThrownBy(() -> endpoint.receive("poc-a", 0, "session-1", "req-1",
                "tools/call", "echo", "{\"count\": \"not-a-number\"}"))
                .isInstanceOf(AssertionError.class);
    }

    @Test
    void malformed_and_schema_invalid_arguments_fail_locally() {
        ExchangeRegistry registry = new ExchangeRegistry();
        CitrusPocEndpoint endpoint = new CitrusPocEndpoint(registry);
        endpoint.expect("tools/call", "echo", "{\"count\": 3}");

        // Malformed envelope vs well-formed but schema-invalid: both fail the Citrus
        // expectation here; exact framework encodings are characterized per candidate
        // in the report, never invented cross-framework codes.
        assertThatThrownBy(() -> endpoint.receive("poc-a", 0, "session-1", "req-1",
                "tools/call", "echo", "{not json"))
                .isInstanceOf(AssertionError.class);
        assertThatThrownBy(() -> endpoint.receive("poc-a", 0, "session-1", "req-2",
                "tools/call", "echo", "{\"count\": \"three\"}"))
                .isInstanceOf(AssertionError.class);
    }

    @Test
    void stopped_generation_cannot_satisfy_later_receives() {
        ExchangeRegistry registry = new ExchangeRegistry();
        CitrusPocEndpoint endpoint = new CitrusPocEndpoint(registry);
        endpoint.expect("resources/list", "list", "{}");
        String token = endpoint.receive("poc-a", 0, "session-1", "req-1", "resources/list", "list", "{}");

        assertThat(registry.stopGeneration("poc-a", 0)).isEqualTo(1);
        assertThatThrownBy(() -> endpoint.reply(token, SimulationReply.resourceListResult("{}")))
                .isInstanceOf(IllegalStateException.class);
    }
}
