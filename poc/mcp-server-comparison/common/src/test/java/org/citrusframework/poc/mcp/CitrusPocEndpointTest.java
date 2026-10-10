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

class CitrusPocEndpointTest {

    @Test
    void typed_tool_request_resolves_citrus_variable_and_replies() throws Exception {
        ExchangeRegistry registry = new ExchangeRegistry();
        CitrusPocEndpoint endpoint = new CitrusPocEndpoint(registry);
        endpoint.setVariable("id", "12345");

        String expectedArgs = ScenarioCatalog.sampleToolArguments("${id}");
        endpoint.expect("tools/call", "echo", expectedArgs);

        String token = endpoint.receive("poc-a", 0, "session-1", "req-1",
                "tools/call", "echo", ScenarioCatalog.sampleToolArguments("12345"));

        assertThat(registry.isPending(token)).isTrue();
        endpoint.reply(token, SimulationReply.toolResult("{\"echoed\": \"12345\"}"));
        assertThat(registry.await(token, 1, java.util.concurrent.TimeUnit.SECONDS).getJsonPayload())
                .contains("12345");
    }

    @Test
    void deliberate_expectation_mismatch_fails_scenario() {
        ExchangeRegistry registry = new ExchangeRegistry();
        CitrusPocEndpoint endpoint = new CitrusPocEndpoint(registry);
        endpoint.expect("tools/call", "echo", "{\"count\": 3}");

        assertThatThrownBy(() -> endpoint.receive("poc-a", 0, "session-1", "req-1",
                "tools/call", "echo", "{\"count\": 4}"))
                .isInstanceOf(AssertionError.class)
                .hasMessageContaining("payload mismatch");
    }

    @Test
    void wrong_operation_or_selector_fails() {
        ExchangeRegistry registry = new ExchangeRegistry();
        CitrusPocEndpoint endpoint = new CitrusPocEndpoint(registry);
        endpoint.expect("tools/call", "echo", "{}");

        assertThatThrownBy(() -> endpoint.receive("poc-a", 0, "session-1", "req-1",
                "resources/read", "poc://alpha", "{}"))
                .isInstanceOf(AssertionError.class)
                .hasMessageContaining("expected tools/call echo");
    }

    @Test
    void unknown_variable_fails_fast() {
        ExchangeRegistry registry = new ExchangeRegistry();
        CitrusPocEndpoint endpoint = new CitrusPocEndpoint(registry);

        assertThatThrownBy(() -> endpoint.expect("tools/call", "echo", "${missing}"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("unknown Citrus variable");
    }

    @Test
    void duplicate_reply_is_rejected() {
        ExchangeRegistry registry = new ExchangeRegistry();
        CitrusPocEndpoint endpoint = new CitrusPocEndpoint(registry);
        endpoint.expect("resources/read", "poc://alpha", "{}");

        String token = endpoint.receive("poc-a", 0, "session-1", "req-1",
                "resources/read", "poc://alpha", "{}");
        endpoint.reply(token, SimulationReply.resourceReadResult("{\"text\": \"alpha-content\"}"));

        assertThatThrownBy(() -> endpoint.reply(token, SimulationReply.resourceReadResult("{}")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already terminal");
    }
}
