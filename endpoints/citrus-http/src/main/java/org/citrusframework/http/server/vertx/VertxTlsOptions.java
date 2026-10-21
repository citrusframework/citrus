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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;

import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.http.security.HttpServerTlsSettings;
import org.citrusframework.spi.Resource;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.KeyCertOptions;
import io.vertx.core.net.KeyStoreOptions;
import io.vertx.core.net.TrustOptions;

/**
 * Creates the Vert.x TLS options from the transport neutral server TLS settings. The key store
 * is read from the resource stream so that key stores from the classpath or from a jar file are
 * supported as well. The store type is detected automatically, both the Java key store format
 * and the PKCS12 format are supported.
 *
 * @since 5.2
 */
class VertxTlsOptions {

    /** The magic bytes of a Java key store (JKS) file. */
    private static final int[] JKS_MAGIC = { 0xFE, 0xED, 0xFE, 0xED };

    /**
     * Creates the key and certificate options holding the server certificate.
     *
     * @param settings the transport neutral server TLS settings.
     * @return the key and certificate options.
     */
    static KeyCertOptions createKeyCertOptions(HttpServerTlsSettings settings) {
        return createKeyStoreOptions(settings.keyStore(), settings.keyStorePassword(), "key store");
    }

    /**
     * Creates the trust options holding the trusted certificates. Returns null when no trust
     * store is configured.
     *
     * @param settings the transport neutral server TLS settings.
     * @return the trust options or null.
     */
    static TrustOptions createTrustOptions(HttpServerTlsSettings settings) {
        if (settings.trustStore() == null) {
            return null;
        }

        return createKeyStoreOptions(settings.trustStore(), settings.trustStorePassword(), "trust store");
    }

    /**
     * Creates the key store options for the given resource.
     *
     * @param resource the resource holding the key store.
     * @param password the password of the key store.
     * @param description the description of the key store used in error messages.
     * @return the key store options.
     */
    private static KeyStoreOptions createKeyStoreOptions(Resource resource, String password, String description) {
        if (resource == null) {
            throw new CitrusRuntimeException("Invalid secure connection configuration for Vert.x Http server: the %s is not set. Use a %s implementation with a key store instead."
                    .formatted(description, org.citrusframework.http.security.SSLConnection.class.getName()));
        }

        byte[] content = readContent(resource, description);
        String type = detectKeyStoreType(content, password, description);

        return new KeyStoreOptions()
                .setType(type)
                .setPassword(password)
                .setValue(Buffer.buffer(content));
    }

    /**
     * Reads the content of the given resource.
     *
     * @param resource the resource to read.
     * @param description the description of the key store used in error messages.
     * @return the content of the resource.
     */
    private static byte[] readContent(Resource resource, String description) {
        try (InputStream inputStream = resource.getInputStream()) {
            return inputStream.readAllBytes();
        } catch (IOException e) {
            throw new CitrusRuntimeException("Failed to read the %s of the secure connection: %s".formatted(description, e.getMessage()), e);
        }
    }

    /**
     * Detects the type of the given key store content. The Java key store format is identified by
     * its magic bytes because the Java key store implementation is able to read both formats and
     * would therefore always detect a PKCS12 key store. The detected type is verified by loading
     * the key store with the given password.
     *
     * @param content the content of the key store.
     * @param password the password of the key store.
     * @param description the description of the key store used in error messages.
     * @return the detected key store type.
     */
    private static String detectKeyStoreType(byte[] content, String password, String description) {
        String type = isJksKeyStore(content) ? "JKS" : "PKCS12";
        char[] secret = password != null ? password.toCharArray() : new char[0];

        try {
            KeyStore keyStore = KeyStore.getInstance(type);
            keyStore.load(new ByteArrayInputStream(content), secret);
            return type;
        } catch (Exception e) {
            throw new CitrusRuntimeException("Invalid secure connection configuration for Vert.x Http server: unable to read the %s with the given password."
                    .formatted(description));
        }
    }

    /**
     * Checks if the given content is a Java key store (JKS) file.
     *
     * @param content the content of the key store.
     * @return true if the content is a Java key store.
     */
    private static boolean isJksKeyStore(byte[] content) {
        if (content.length < JKS_MAGIC.length) {
            return false;
        }

        for (int i = 0; i < JKS_MAGIC.length; i++) {
            if ((content[i] & 0xFF) != JKS_MAGIC[i]) {
                return false;
            }
        }

        return true;
    }
}
