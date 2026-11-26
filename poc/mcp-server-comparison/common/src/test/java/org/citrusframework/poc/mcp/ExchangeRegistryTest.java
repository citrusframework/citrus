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

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ExchangeRegistryTest {

    @Test
    void reply_correlates_exactly_one_response_per_request() throws Exception {
        ExchangeRegistry registry = new ExchangeRegistry();

        String token = registry.register("poc-a", 0, "session-1", "req-1");

        assertThat(registry.isPending(token)).isTrue();
        SimulationReply reply = SimulationReply.resourceListResult(ScenarioCatalog.resourceListFixture("alpha-beta"));

        assertThat(registry.reply(token, reply)).isTrue();
        assertThat(registry.isPending(token)).isFalse();
        assertThat(registry.terminalState(token)).isEqualTo(ExchangeRegistry.Terminal.REPLY);
        assertThat(registry.await(token, 1, TimeUnit.SECONDS)).isSameAs(reply);
    }

    @Test
    void await_unknown_token_fails() {
        ExchangeRegistry registry = new ExchangeRegistry();

        assertThatThrownBy(() -> registry.await("poc-a/g0/s/r#deadbeef", 1, TimeUnit.SECONDS))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void duplicate_reply_is_rejected_as_late() throws Exception {
        ExchangeRegistry registry = new ExchangeRegistry();
        String token = registry.register("poc-a", 0, "session-1", "req-1");

        assertThat(registry.reply(token, SimulationReply.toolResult("{}"))).isTrue();
        assertThat(registry.reply(token, SimulationReply.toolResult("{}"))).isFalse();
    }

    @Test
    void equal_request_ids_across_sessions_do_not_cross_talk() throws Exception {
        ExchangeRegistry registry = new ExchangeRegistry();

        String first = registry.register("poc-a", 0, "session-1", "req-1");
        String second = registry.register("poc-a", 0, "session-2", "req-1");

        assertThat(first).isNotEqualTo(second);

        SimulationReply firstReply = SimulationReply.resourceListResult(ScenarioCatalog.resourceListFixture("alpha"));
        SimulationReply secondReply = SimulationReply.resourceListResult(ScenarioCatalog.resourceListFixture("beta"));

        // Reply in reverse order: second session first.
        assertThat(registry.reply(second, secondReply)).isTrue();
        assertThat(registry.reply(first, firstReply)).isTrue();

        assertThat(registry.await(first, 1, TimeUnit.SECONDS).getJsonPayload()).contains("alpha");
        assertThat(registry.await(second, 1, TimeUnit.SECONDS).getJsonPayload()).contains("beta");
    }

    @Test
    void expired_entries_are_removed_and_time_out() {
        ExchangeRegistry registry = new ExchangeRegistry();
        String token = registry.register("poc-a", 0, "session-1", "req-1");

        assertThatThrownBy(() -> registry.await(token, 50, TimeUnit.MILLISECONDS))
                .isInstanceOf(TimeoutException.class);
        assertThat(registry.isPending(token)).isFalse();
        assertThat(registry.terminalState(token)).isEqualTo(ExchangeRegistry.Terminal.TIMEOUT);
    }

    @Test
    void stop_generation_prevents_later_replies() {
        ExchangeRegistry registry = new ExchangeRegistry();
        String token = registry.register("poc-a", 0, "session-1", "req-1");

        assertThat(registry.stopGeneration("poc-a", 0)).isEqualTo(1);
        assertThat(registry.isPending(token)).isFalse();
        assertThat(registry.terminalState(token)).isEqualTo(ExchangeRegistry.Terminal.STOP);
        assertThat(registry.reply(token, SimulationReply.toolResult("{}"))).isFalse();
    }

    @Test
    void cancel_is_terminal() {
        ExchangeRegistry registry = new ExchangeRegistry();
        String token = registry.register("poc-a", 0, "session-1", "req-1");

        assertThat(registry.cancel(token)).isTrue();
        assertThat(registry.terminalState(token)).isEqualTo(ExchangeRegistry.Terminal.CANCEL);
        assertThat(registry.reply(token, SimulationReply.toolResult("{}"))).isFalse();
    }
}
