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

package org.citrusframework.http.vertx;

import java.nio.file.Path;
import java.util.Objects;
import java.util.Set;
import io.vertx.core.http.ClientAuth;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.net.JdkSSLEngineOptions;
import io.vertx.core.net.PfxOptions;

/** Immutable PKCS12 TLS configuration; store contents are loaded at server startup. */
public final class VertxHttpTls {
    private final String keyStore;
    private final String password;
    private final String trustStore;
    private final String trustPassword;
    private final Set<String> protocols;

    public VertxHttpTls(Path keyStore, String password) {
        this(keyStore.toAbsolutePath().toString(), password, null, null, Set.of("TLSv1.2", "TLSv1.3"));
    }
    private VertxHttpTls(String keyStore, String password, String trustStore, String trustPassword, Set<String> protocols) {
        this.keyStore = Objects.requireNonNull(keyStore);
        this.password = Objects.requireNonNull(password);
        this.trustStore = trustStore;
        this.trustPassword = trustPassword;
        this.protocols = Set.copyOf(protocols);
        if (protocols.isEmpty() || !Set.of("TLSv1.2", "TLSv1.3").containsAll(protocols))
            throw new IllegalArgumentException("Choose TLSv1.2 and/or TLSv1.3");
    }
    public VertxHttpTls withClientAuthentication(Path trustStore, String password) {
        return new VertxHttpTls(keyStore, this.password, trustStore.toAbsolutePath().toString(),
                Objects.requireNonNull(password), protocols);
    }
    public VertxHttpTls withProtocols(Set<String> protocols) {
        return new VertxHttpTls(keyStore, password, trustStore, trustPassword, protocols);
    }
    HttpServerOptions apply(HttpServerOptions options) {
        options.setSsl(true).setKeyCertOptions(new PfxOptions().setPath(keyStore).setPassword(password))
                .setJdkSslEngineOptions(new JdkSSLEngineOptions()).setUseAlpn(false)
                .setEnabledSecureTransportProtocols(protocols).setSslHandshakeTimeout(10);
        if (trustStore != null) {
            options.setTrustOptions(new PfxOptions().setPath(trustStore).setPassword(trustPassword))
                    .setClientAuth(ClientAuth.REQUIRED);
        }
        return options;
    }
}
