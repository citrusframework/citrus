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
import java.util.Optional;

import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.http.interceptor.HttpServerInterceptor;
import org.citrusframework.http.security.BasicAuthentication;
import org.citrusframework.http.security.HttpAuthentication;
import org.citrusframework.http.security.HttpSecureConnection;
import org.citrusframework.http.security.SSLConnection;
import org.citrusframework.testng.AbstractTestNGUnitTest;
import org.springframework.web.servlet.HandlerInterceptor;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Verifies the fail fast validation of the Vert.x based Http server. Configuration that is bound to
 * a specific server technology must be rejected with an error naming the offending configuration
 * and the replacement to use.
 */
public class VertxHttpServerValidationTest extends AbstractTestNGUnitTest {

    @Test
    public void shouldRejectInterceptorThatIsNotACitrusInterceptor() {
        VertxHttpServer server = new VertxHttpServer();
        HandlerInterceptor interceptor = mock(HandlerInterceptor.class);
        server.setConfiguredInterceptors(List.of(interceptor));

        assertThatThrownBy(server::initialize)
                .isInstanceOf(CitrusRuntimeException.class)
                .hasMessageContaining(interceptor.getClass().getName())
                .hasMessageContaining(HttpServerInterceptor.class.getName());
    }

    @Test
    public void shouldRejectAuthenticationWithoutNeutralDescription() {
        VertxHttpServer server = new VertxHttpServer();
        HttpAuthentication authentication = mock(HttpAuthentication.class);
        when(authentication.getServerAuthentication(anyString())).thenReturn(Optional.empty());
        server.setAuthentication(authentication);

        assertThatThrownBy(server::initialize)
                .isInstanceOf(CitrusRuntimeException.class)
                .hasMessageContaining(HttpAuthentication.class.getSimpleName())
                .hasMessageContaining(BasicAuthentication.class.getName());
    }

    @Test
    public void shouldRejectSecureConnectionWithoutNeutralTlsSettings() {
        VertxHttpServer server = new VertxHttpServer();
        HttpSecureConnection secureConnection = mock(HttpSecureConnection.class);
        when(secureConnection.getServerTlsSettings()).thenReturn(Optional.empty());
        server.setSecureConnection(secureConnection);

        assertThatThrownBy(server::initialize)
                .isInstanceOf(CitrusRuntimeException.class)
                .hasMessageContaining(HttpSecureConnection.class.getSimpleName())
                .hasMessageContaining(SSLConnection.class.getName());
    }

    @Test
    public void shouldRejectInvalidWorkerPoolSize() {
        VertxHttpServer server = new VertxHttpServer();
        server.setWorkerPoolSize(0);

        assertThatThrownBy(server::initialize)
                .isInstanceOf(CitrusRuntimeException.class)
                .hasMessageContaining("worker pool size");
    }

    @Test
    public void shouldRejectContextPathWithoutLeadingSlash() {
        VertxHttpServer server = new VertxHttpServer();
        server.setContextPath("api");

        assertThatThrownBy(server::initialize)
                .isInstanceOf(CitrusRuntimeException.class)
                .hasMessageContaining("context path");
    }

    @Test
    public void shouldAcceptValidConfiguration() {
        VertxHttpServer server = new VertxHttpServer();
        server.setWorkerPoolSize(10);
        server.setContextPath("/api");
        server.setAuthentication(HttpAuthentication.basic("citrus", "secr3t"));
        server.setSecureConnection(HttpSecureConnection.ssl());

        assertThatCode(server::initialize).doesNotThrowAnyException();
    }
}
