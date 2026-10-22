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

package org.citrusframework.http.server;

import java.util.List;

import org.citrusframework.http.interceptor.HttpServerInterceptor;
import org.citrusframework.http.message.HttpMessageConverter;
import org.citrusframework.http.security.HttpAuthentication;
import org.citrusframework.http.security.HttpSecureConnection;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.HandlerInterceptor;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/**
 * Unit test for the transport neutral Http server base classes.
 */
public class AbstractHttpServerTest {

    @Test
    public void shouldProvideNeutralServerBaseClass() {
        assertThat(HttpServer.class.getSuperclass()).isEqualTo(AbstractHttpServer.class);
        assertThat(AbstractHttpServer.class.getSuperclass().getName())
                .isEqualTo("org.citrusframework.base.server.AbstractServer");
    }

    @Test
    public void shouldProvideNeutralBuilderBaseClass() {
        assertThat(AbstractHttpServerBuilder.class.getSuperclass()).isEqualTo(AbstractCommonHttpServerBuilder.class);
    }

    @Test
    public void shouldUseNeutralDefaults() {
        AbstractHttpServer server = new HttpServer();

        assertThat(server.getPort()).isEqualTo(8080);
        assertThat(server.getSecurePort()).isEqualTo(8443);
        assertThat(server.getContextPath()).isEqualTo("/");
        assertThat(server.isHandleCookies()).isFalse();
        assertThat(server.isRemoveSemicolonPathContent()).isTrue();
        assertThat(server.getDefaultStatusCode()).isEqualTo(HttpStatus.OK.value());
        assertThat(server.isUseDefaultFilters()).isTrue();
        assertThat(server.getBinaryMediaTypes()).containsExactly(
                MediaType.APPLICATION_OCTET_STREAM,
                MediaType.APPLICATION_PDF,
                MediaType.IMAGE_GIF,
                MediaType.IMAGE_JPEG,
                MediaType.IMAGE_PNG,
                MediaType.valueOf("application/zip"));
        assertThat(server.getMessageConverter()).isNotNull();
        assertThat(server.getAuthentication()).isNull();
        assertThat(server.getSecuredPath()).isEqualTo("/*");
        assertThat(server.getSecureConnection()).isNull();
    }

    @Test
    public void shouldSetNeutralOptionsOnEndpoint() {
        HttpMessageConverter messageConverter = new HttpMessageConverter();

        HttpServer server = new HttpServerBuilder()
                .port(9090)
                .securePort(9443)
                .contextPath("/api")
                .handleCookies(true)
                .removeSemicolonPathContent(false)
                .defaultStatus(HttpStatus.ACCEPTED)
                .useDefaultFilters(false)
                .messageConverter(messageConverter)
                .binaryMediaTypes(List.of(MediaType.APPLICATION_JSON))
                .timeout(1000L)
                .build();

        assertThat(server.getPort()).isEqualTo(9090);
        assertThat(server.getSecurePort()).isEqualTo(9443);
        assertThat(server.getContextPath()).isEqualTo("/api");
        assertThat(server.isHandleCookies()).isTrue();
        assertThat(server.isRemoveSemicolonPathContent()).isFalse();
        assertThat(server.getDefaultStatusCode()).isEqualTo(HttpStatus.ACCEPTED.value());
        assertThat(server.isUseDefaultFilters()).isFalse();
        assertThat(server.getMessageConverter()).isSameAs(messageConverter);
        assertThat(server.getBinaryMediaTypes()).containsExactly(MediaType.APPLICATION_JSON);
        assertThat(server.getDefaultTimeout()).isEqualTo(1000L);
    }

    @Test
    public void shouldSetNeutralAuthenticationOnEndpoint() {
        HttpAuthentication authentication = mock(HttpAuthentication.class);

        HttpServer server = new HttpServerBuilder()
                .authentication("/api/*", authentication)
                .build();

        assertThat(server.getAuthentication()).isSameAs(authentication);
        assertThat(server.getSecuredPath()).isEqualTo("/api/*");
    }

    @Test
    public void shouldSetNeutralSecureConnectionOnEndpoint() {
        HttpSecureConnection secureConnection = mock(HttpSecureConnection.class);

        HttpServer server = new HttpServerBuilder()
                .secured(9443, secureConnection)
                .build();

        assertThat(server.getSecureConnection()).isSameAs(secureConnection);
        assertThat(server.getSecurePort()).isEqualTo(9443);
    }

    @Test
    public void shouldAddCitrusInterceptorsToServerInterceptors() {
        HttpServerInterceptor citrusInterceptor = mock(HttpServerInterceptor.class);
        HandlerInterceptor handlerInterceptor = mock(HandlerInterceptor.class);

        HttpServer server = new HttpServer();
        server.setInterceptors(List.of(citrusInterceptor, handlerInterceptor));

        assertThat(server.getServerInterceptors()).containsExactly(citrusInterceptor);
        assertThat(server.getInterceptors()).containsExactly(handlerInterceptor);
    }

    @Test
    public void shouldKeepRegisteredServerInterceptorsWhenSettingInterceptors() {
        HttpServerInterceptor registeredInterceptor = mock(HttpServerInterceptor.class);
        HttpServerInterceptor configuredInterceptor = mock(HttpServerInterceptor.class);

        HttpServer server = new HttpServer();
        server.setServerInterceptors(List.of(registeredInterceptor));
        server.setInterceptors(List.of(configuredInterceptor));

        assertThat(server.getServerInterceptors()).containsExactly(registeredInterceptor, configuredInterceptor);
    }
}
