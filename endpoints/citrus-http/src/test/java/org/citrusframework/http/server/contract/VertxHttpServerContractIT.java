/*
 * Copyright the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.citrusframework.http.server.contract;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

import org.citrusframework.http.client.HttpClient;
import org.citrusframework.http.interceptor.HttpServerInterceptor;
import org.citrusframework.http.interceptor.LoggingServerInterceptor;
import org.citrusframework.http.security.HttpAuthentication;
import org.citrusframework.http.security.HttpServerAuthentication;
import org.citrusframework.http.security.User;
import org.citrusframework.http.server.AbstractCommonHttpServerBuilder;
import org.citrusframework.http.server.AbstractHttpServer;
import org.citrusframework.http.server.vertx.VertxHttpServerBuilder;
import org.citrusframework.report.MessageListeners;
import org.eclipse.jetty.security.SecurityHandler;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Runs the shared Http server contract suite against the Vert.x based Http server.
 */
public class VertxHttpServerContractIT extends AbstractHttpServerContractIT {

    @Override
    protected AbstractHttpServer createServer(Consumer<AbstractCommonHttpServerBuilder<?, ?>> options) {
        VertxHttpServerBuilder builder = new VertxHttpServerBuilder();
        options.accept(builder);
        return builder.build();
    }

    @Override
    protected boolean isTraceAnsweredByServer() {
        return false;
    }

    @Override
    protected boolean isDuplicateSlashRejected() {
        return false;
    }

    @Override
    protected boolean isSupported(Feature feature) {
        return switch (feature) {
            case GZIP, AUTHENTICATION, TLS, TRACING, INTERCEPTORS -> true;
        };
    }

    @Override
    protected void configureAuthentication(AbstractHttpServer server) {
        server.setAuthentication(new HttpAuthentication() {

            @Override
            @SuppressWarnings("deprecation")
            public SecurityHandler getSecurityHandler(String resourcePath) {
                throw new UnsupportedOperationException("The Vert.x Http server does not support Jetty security handlers");
            }

            @Override
            public ClientHttpRequestFactory getRequestFactory(String requestUrl, HttpClient httpClient) {
                throw new UnsupportedOperationException("The Vert.x Http server does not support client side authentication");
            }

            @Override
            public Optional<HttpServerAuthentication> getServerAuthentication(String resourcePath) {
                List<User> users = List.of(
                        new User(AUTH_USER, AUTH_PASSWORD, new String[] { AUTH_ROLE }),
                        new User(AUTH_USER_WITHOUT_ROLE, AUTH_PASSWORD_WITHOUT_ROLE, new String[] {}));

                return Optional.of(new HttpServerAuthentication(AUTH_REALM, users,
                        Map.of(SECURED_PATH, List.of(AUTH_ROLE))));
            }
        });
    }

    @Override
    protected void configureTracing(AbstractHttpServer server, MessageListeners messageListeners) {
        LoggingServerInterceptor loggingInterceptor = new LoggingServerInterceptor();
        loggingInterceptor.setMessageListener(messageListeners);

        List<HttpServerInterceptor> interceptors = new ArrayList<>(server.getServerInterceptors());
        interceptors.add(loggingInterceptor);
        server.setServerInterceptors(interceptors);
    }

    /**
     * HSRV-024: the Vert.x server answers a request with malformed basic credentials with a
     * challenge instead of failing the request with an internal server error.
     */
    @Test
    public void shouldAnswerMalformedCredentialsWithChallenge() {
        AbstractHttpServer server = startSecuredServer();

        ContractClient.Response response = ContractClient.send(server.getPort(), "GET", "/secured/orders",
                Map.of("Authorization", "Basic not-base64!"), new byte[0]);

        assertThat(response.getStatusCode()).isEqualTo(401);
        assertThat(response.getHeader("WWW-Authenticate")).contains("Basic realm=\"" + AUTH_REALM + "\"");
        assertThat(getEndpointAdapter().hasReceivedRequests()).isFalse();
    }

    /**
     * HSRV-024: basic credentials without a colon separator are malformed as well.
     */
    @Test
    public void shouldAnswerCredentialsWithoutSeparatorWithChallenge() {
        AbstractHttpServer server = startSecuredServer();

        String credentials = Base64.getEncoder().encodeToString(AUTH_USER.getBytes(StandardCharsets.UTF_8));

        ContractClient.Response response = ContractClient.send(server.getPort(), "GET", "/secured/orders",
                Map.of("Authorization", "Basic " + credentials), new byte[0]);

        assertThat(response.getStatusCode()).isEqualTo(401);
        assertThat(response.getHeader("WWW-Authenticate")).contains("Basic realm=\"" + AUTH_REALM + "\"");
        assertThat(getEndpointAdapter().hasReceivedRequests()).isFalse();
    }
}
