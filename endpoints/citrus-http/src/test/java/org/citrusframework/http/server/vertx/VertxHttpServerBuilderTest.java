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

package org.citrusframework.http.server.vertx;

import java.util.List;

import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.http.interceptor.HttpServerInterceptor;
import org.springframework.web.servlet.HandlerInterceptor;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

/**
 * Unit test for the Vert.x based Http server builder.
 */
public class VertxHttpServerBuilderTest {

    @Test
    public void shouldKeepRegisteredServerInterceptors() {
        HttpServerInterceptor registeredInterceptor = mock(HttpServerInterceptor.class);
        HttpServerInterceptor configuredInterceptor = mock(HttpServerInterceptor.class);

        VertxHttpServer server = new VertxHttpServerBuilder()
                .interceptor(registeredInterceptor)
                .interceptors(List.<Object>of(configuredInterceptor))
                .build();

        assertThat(server.getServerInterceptors()).containsExactly(registeredInterceptor, configuredInterceptor);
        assertThat(server.getConfiguredInterceptors()).isEmpty();
    }

    @Test
    public void shouldRejectTechnologySpecificInterceptors() {
        HandlerInterceptor handlerInterceptor = mock(HandlerInterceptor.class);

        VertxHttpServer server = new VertxHttpServerBuilder()
                .interceptors(List.<Object>of(handlerInterceptor))
                .build();

        assertThat(server.getConfiguredInterceptors()).containsExactly(handlerInterceptor);
        assertThat(server.getServerInterceptors()).isEmpty();

        assertThatThrownBy(server::validateConfiguration)
                .isInstanceOf(CitrusRuntimeException.class)
                .hasMessageContaining("Invalid interceptor configuration for Vert.x Http server");
    }
}
