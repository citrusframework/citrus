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

import java.util.HashMap;
import java.util.Map;

import org.citrusframework.poc.mcp.CitrusPocEndpoint;
import org.citrusframework.poc.mcp.ExchangeRegistry;
import org.citrusframework.poc.mcp.LifecycleState;
import org.citrusframework.poc.mcp.PocConfig;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Outer Spring Java-bean lifecycle for the Quarkus candidate (T14).
 *
 * <p>Mirrors the existing Citrus {@code ComponentLifecycleProcessor} semantics:
 * bean name/resolution precedes initialization, {@code autoStart=false} requires
 * explicit start, outer-context destruction converges on the same terminal cleanup
 * path as explicit stop (including destruction after explicit stop, without double
 * cleanup), and an unrelated context is never closed. If the candidate cannot
 * coexist with the outer Spring runtime that is a compatibility finding, not a pass.
 */
class QuarkusSpringBeanIT {

    /** Minimal outer-context double with Citrus lifecycle-processor semantics. */
    static final class OuterContext implements AutoCloseable {
        private final Map<String, Object> beans = new HashMap<>();
        private final String name;
        private boolean closed;

        OuterContext(String name) {
            this.name = name;
        }

        void registerBean(String beanName, Object bean) {
            beans.put(beanName, bean);
        }

        @SuppressWarnings("unchecked")
        <T> T resolve(String beanName, Class<T> type) {
            Object bean = beans.get(beanName);
            assertThat(bean).as("bean %s in %s", beanName, name).isNotNull();
            return (T) bean;
        }

        void destroyBean(String beanName) throws Exception {
            Object bean = beans.remove(beanName);
            if (bean instanceof QuarkusPocHost host) {
                host.stop();
            }
        }

        @Override
        public void close() {
            closed = true;
            beans.clear();
        }

        boolean isClosed() {
            return closed;
        }
    }

    private static PocConfig config() {
        return PocConfig.builder(PocConfig.Candidate.QUARKUS, PocConfig.Transport.HTTP)
                .endpointId("poc-bean")
                .port(0)
                .scenarioId("spring-bean")
                .build();
    }

    @Test
    void bean_resolves_before_init_and_requires_explicit_start() throws Exception {
        OuterContext outer = new OuterContext("outer");
        QuarkusPocHost host = new QuarkusPocHost();
        outer.registerBean("mcpPocEndpoint", host);

        QuarkusPocHost resolved = outer.resolve("mcpPocEndpoint", QuarkusPocHost.class);
        assertThat(resolved.state()).isEqualTo(LifecycleState.NEW);

        CitrusPocEndpoint bridge = new CitrusPocEndpoint(new ExchangeRegistry());
        resolved.start(config(), bridge);
        assertThat(resolved.state()).isEqualTo(LifecycleState.RUNNING);
        resolved.stop();
        outer.close();
        assertThat(outer.isClosed()).isTrue();
    }

    @Test
    void destruction_while_running_converges_on_stop() throws Exception {
        OuterContext outer = new OuterContext("outer");
        QuarkusPocHost host = new QuarkusPocHost();
        outer.registerBean("mcpPocEndpoint", host);
        host.start(config(), new CitrusPocEndpoint(new ExchangeRegistry()));

        outer.destroyBean("mcpPocEndpoint");
        assertThat(host.state()).isEqualTo(LifecycleState.STOPPED);
    }

    @Test
    void destruction_after_explicit_stop_has_no_double_cleanup() throws Exception {
        OuterContext outer = new OuterContext("outer");
        QuarkusPocHost host = new QuarkusPocHost();
        outer.registerBean("mcpPocEndpoint", host);
        host.start(config(), new CitrusPocEndpoint(new ExchangeRegistry()));
        host.stop();

        outer.destroyBean("mcpPocEndpoint");
        host.stop();
        assertThat(host.state()).isEqualTo(LifecycleState.STOPPED);
    }

    @Test
    void unrelated_context_survives() throws Exception {
        OuterContext outer = new OuterContext("outer");
        OuterContext unrelated = new OuterContext("unrelated");
        QuarkusPocHost host = new QuarkusPocHost();
        outer.registerBean("mcpPocEndpoint", host);
        host.start(config(), new CitrusPocEndpoint(new ExchangeRegistry()));

        unrelated.close();
        assertThat(unrelated.isClosed()).isTrue();
        assertThat(host.state()).isEqualTo(org.citrusframework.poc.mcp.LifecycleState.RUNNING);
        assertThat(outer.isClosed()).isFalse();
        host.stop();
    }
}
