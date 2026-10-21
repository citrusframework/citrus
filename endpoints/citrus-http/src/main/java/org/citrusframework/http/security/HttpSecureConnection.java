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

import java.util.Optional;

import org.apache.hc.client5.http.io.HttpClientConnectionManager;
import org.eclipse.jetty.server.ServerConnector;

/**
 * Secure Http connection able to create proper connection manager for clients.
 */
public interface HttpSecureConnection {

    /**
     * Create server connector representing the secure connection.
     * @param securePort
     * @return
     * @deprecated Jetty specific, use {@link #getServerTlsSettings()} instead.
     */
    @Deprecated(since = "5.1.0")
    ServerConnector getServerConnector(int securePort);

    /**
     * Create proper secure connection manager to be used on custom Http clients.
     * @return
     */
    HttpClientConnectionManager getClientConnectionManager();

    /**
     * Transport neutral description of the server side TLS settings. Returns an empty
     * optional when the secure connection is not able to describe itself in a transport
     * neutral way.
     *
     * @return the server side TLS settings.
     */
    default Optional<HttpServerTlsSettings> getServerTlsSettings() {
        return Optional.empty();
    }

    static SSLConnection ssl() {
        return new SSLConnection();
    }
}
