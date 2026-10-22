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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.http.security.HttpServerTlsSettings;
import org.citrusframework.spi.Resources;
import org.citrusframework.testng.AbstractTestNGUnitTest;

import io.vertx.core.Vertx;
import io.vertx.core.net.KeyStoreOptions;
import io.vertx.core.net.TrustOptions;
import org.testng.annotations.Test;

/**
 * @since 5.2
 */
public class VertxTlsOptionsTest extends AbstractTestNGUnitTest {

    private static final String KEY_STORE_PASSWORD = "secret";

    private static final org.citrusframework.spi.Resource JKS_KEY_STORE =
            Resources.create("classpath:org/citrusframework/http/integration/http-server.jks");

    private static final org.citrusframework.spi.Resource PKCS12_KEY_STORE =
            Resources.create("classpath:org/citrusframework/http/server/contract/http-server.p12");

    @Test
    public void shouldCreateKeyCertOptionsFromJksKeyStore() {
        HttpServerTlsSettings settings = new HttpServerTlsSettings(JKS_KEY_STORE, KEY_STORE_PASSWORD, null, null);

        KeyStoreOptions keyStoreOptions = (KeyStoreOptions) VertxTlsOptions.createKeyCertOptions(settings);

        assertThat(keyStoreOptions.getType()).isEqualTo("JKS");
        assertThat(keyStoreOptions.getPassword()).isEqualTo(KEY_STORE_PASSWORD);
        assertThat(keyStoreOptions.getValue()).isNotNull();
        assertThat(keyStoreOptions.getValue().length()).isGreaterThan(0);
    }

    @Test
    public void shouldCreateKeyCertOptionsFromPkcs12KeyStore() {
        HttpServerTlsSettings settings = new HttpServerTlsSettings(PKCS12_KEY_STORE, KEY_STORE_PASSWORD, null, null);

        KeyStoreOptions keyStoreOptions = (KeyStoreOptions) VertxTlsOptions.createKeyCertOptions(settings);

        assertThat(keyStoreOptions.getType()).isEqualTo("PKCS12");
        assertThat(keyStoreOptions.getPassword()).isEqualTo(KEY_STORE_PASSWORD);
        assertThat(keyStoreOptions.getValue()).isNotNull();
    }

    @Test
    public void shouldCreateLoadableKeyStoreOptions() throws Exception {
        HttpServerTlsSettings settings = new HttpServerTlsSettings(PKCS12_KEY_STORE, KEY_STORE_PASSWORD, null, null);

        KeyStoreOptions keyStoreOptions = (KeyStoreOptions) VertxTlsOptions.createKeyCertOptions(settings);

        Vertx vertx = Vertx.vertx();
        try {
            assertThat(keyStoreOptions.loadKeyStore(vertx)).isNotNull();
        } finally {
            vertx.close().toCompletionStage().toCompletableFuture().get();
        }
    }

    @Test
    public void shouldCreateTrustOptionsFromTrustStore() {
        HttpServerTlsSettings settings = new HttpServerTlsSettings(PKCS12_KEY_STORE, KEY_STORE_PASSWORD, JKS_KEY_STORE, KEY_STORE_PASSWORD);

        TrustOptions trustOptions = VertxTlsOptions.createTrustOptions(settings);

        assertThat(trustOptions).isNotNull();
        assertThat(((KeyStoreOptions) trustOptions).getType()).isEqualTo("JKS");
        assertThat(((KeyStoreOptions) trustOptions).getPassword()).isEqualTo(KEY_STORE_PASSWORD);
    }

    @Test
    public void shouldNotCreateTrustOptionsWithoutTrustStore() {
        HttpServerTlsSettings settings = new HttpServerTlsSettings(PKCS12_KEY_STORE, KEY_STORE_PASSWORD, null, null);

        assertThat(VertxTlsOptions.createTrustOptions(settings)).isNull();
    }

    @Test
    public void shouldFailForMissingKeyStore() {
        HttpServerTlsSettings settings = new HttpServerTlsSettings(null, KEY_STORE_PASSWORD, null, null);

        assertThatThrownBy(() -> VertxTlsOptions.createKeyCertOptions(settings))
                .isInstanceOf(CitrusRuntimeException.class)
                .hasMessageContaining("key store");
    }

    @Test
    public void shouldFailForInvalidKeyStorePassword() {
        HttpServerTlsSettings settings = new HttpServerTlsSettings(PKCS12_KEY_STORE, "wrong", null, null);

        assertThatThrownBy(() -> VertxTlsOptions.createKeyCertOptions(settings))
                .isInstanceOf(CitrusRuntimeException.class)
                .hasMessageContaining("key store");
    }
}
