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

package org.citrusframework.http.security;

import java.lang.reflect.Method;
import java.util.Optional;

import org.apache.hc.client5.http.io.HttpClientConnectionManager;
import org.citrusframework.spi.Resource;
import org.citrusframework.spi.Resources;
import org.eclipse.jetty.server.ServerConnector;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class SSLConnectionTest {

    private static final Resource KEY_STORE = Resources.create("classpath:org/citrusframework/http/integration/http-server.jks");
    private static final Resource TRUST_STORE = Resources.create("classpath:org/citrusframework/http/server/contract/http-server.p12");

    @Test
    public void shouldDescribeServerTlsSettings() {
        SSLConnection connection = HttpSecureConnection.ssl()
                .keyStore(KEY_STORE, "secret");

        Optional<HttpServerTlsSettings> tlsSettings = connection.getServerTlsSettings();

        assertThat(tlsSettings).isPresent();

        HttpServerTlsSettings settings = tlsSettings.get();
        assertThat(settings.keyStore()).isSameAs(KEY_STORE);
        assertThat(settings.keyStorePassword()).isEqualTo("secret");
        assertThat(settings.trustStore()).isNull();
        assertThat(settings.trustStorePassword()).isNull();
    }

    @Test
    public void shouldDescribeTrustStoreInServerTlsSettings() {
        SSLConnection connection = HttpSecureConnection.ssl()
                .keyStore(KEY_STORE, "secret")
                .trustStore(TRUST_STORE, "trustsecret");

        HttpServerTlsSettings settings = connection.getServerTlsSettings().orElseThrow();

        assertThat(settings.trustStore()).isSameAs(TRUST_STORE);
        assertThat(settings.trustStorePassword()).isEqualTo("trustsecret");
    }

    @Test
    public void shouldNotDescribeServerTlsSettingsByDefault() {
        HttpSecureConnection connection = new HttpSecureConnection() {
            @Override
            public ServerConnector getServerConnector(int securePort) {
                throw new UnsupportedOperationException();
            }

            @Override
            public HttpClientConnectionManager getClientConnectionManager() {
                throw new UnsupportedOperationException();
            }
        };

        assertThat(connection.getServerTlsSettings()).isEmpty();
    }

    @Test
    public void shouldStillCreateJettyServerConnector() {
        SSLConnection connection = HttpSecureConnection.ssl().keyStore(KEY_STORE, "secret");

        ServerConnector connector = connection.getServerConnector(8443);

        assertThat(connector).isNotNull();
        assertThat(connector.getPort()).isEqualTo(8443);
    }

    @Test
    public void shouldDeprecateJettyServerConnectorFactory() throws Exception {
        Method method = HttpSecureConnection.class.getMethod("getServerConnector", int.class);

        assertThat(method.isAnnotationPresent(Deprecated.class)).isTrue();
        assertThat(method.getAnnotation(Deprecated.class).since()).isEqualTo("5.1.0");
    }
}
