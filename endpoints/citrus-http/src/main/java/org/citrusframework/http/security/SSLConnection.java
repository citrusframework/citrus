/*
 *  Copyright 2023 the original author or authors.
 *
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements. See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License. You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.citrusframework.http.security;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Collections;
import javax.net.ssl.HostnameVerifier;

import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.io.HttpClientConnectionManager;
import org.apache.hc.client5.http.ssl.NoopHostnameVerifier;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.client5.http.ssl.TrustAllStrategy;
import org.apache.hc.client5.http.ssl.TrustSelfSignedStrategy;
import org.apache.hc.core5.ssl.SSLContextBuilder;
import org.apache.hc.core5.ssl.SSLContexts;
import org.citrusframework.spi.Resource;
import org.citrusframework.spi.Resources;
import org.eclipse.jetty.http.HttpVersion;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.SecureRequestCustomizer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.springframework.beans.factory.BeanCreationException;

/**
 * SSL secure connection to set up a proper SSL context and SSL connection socket factory.
 * Optionally uses provided keystore and truststore. If not set uses trust all strategy.
 */
public class SSLConnection implements HttpSecureConnection {

    private Resource keyStore;
    private String keyStorePassword;

    private Resource trustStore;

    private String trustStorePassword;

    private HostnameVerifier hostnameVerifier = NoopHostnameVerifier.INSTANCE;

    public SSLConnection() {
    }

    public SSLConnection(Resource keyStore, String password) {
        this.keyStore = keyStore;
        this.keyStorePassword = password;
    }

    public SSLConnection(Resource keyStore, String ksPassword, Resource trustStore, String tsPassword) {
        this.keyStore = keyStore;
        this.keyStorePassword = ksPassword;
        this.trustStore = trustStore;
        this.trustStorePassword = tsPassword;
    }

    @Override
    public ServerConnector getServerConnector(int securePort) {
        ServerConnector connector = new ServerConnector(new Server(),
                new SslConnectionFactory(sslContextFactory(), HttpVersion.HTTP_1_1.asString()),
                new HttpConnectionFactory(httpConfiguration(securePort)));
        connector.setPort(securePort);

        return connector;
    }

    @Override
    public HttpClientConnectionManager getClientConnectionManager() {
        try {
            SSLContextBuilder sslContext;

            if (trustStore != null) {
                sslContext = SSLContexts.custom()
                    .loadTrustMaterial(trustStore.getFile(), trustStorePassword.toCharArray(),
                            new TrustSelfSignedStrategy());
            } else {
                sslContext = SSLContexts.custom()
                    .loadTrustMaterial(TrustAllStrategy.INSTANCE);
            }

            if (keyStore != null) {
                sslContext.loadKeyMaterial(KeyStore.getInstance(keyStore.getFile(), keyStorePassword.toCharArray()),
                        keyStorePassword.toCharArray());
            }

            SSLConnectionSocketFactory sslSocketFactory = new SSLConnectionSocketFactory(
                    sslContext.build(), hostnameVerifier);

            return PoolingHttpClientConnectionManagerBuilder.create()
                    .setSSLSocketFactory(sslSocketFactory)
                    .build();
        } catch (IOException | CertificateException | NoSuchAlgorithmException | KeyStoreException |
                 KeyManagementException | UnrecoverableKeyException e) {
            throw new BeanCreationException("Failed to create http client for ssl connection", e);
        }
    }

    public SSLConnection trustStore(String trustStore, String password) {
        return trustStore(Resources.create(trustStore), password);
    }

    public SSLConnection trustStore(Resource trustStore, String password) {
        this.trustStore = trustStore;
        this.trustStorePassword = password;
        return this;
    }

    public SSLConnection keyStore(String keyStore, String password) {
        return keyStore(Resources.create(keyStore), password);
    }

    public SSLConnection keyStore(Resource keyStore, String password) {
        this.keyStore = keyStore;
        this.keyStorePassword = password;
        return this;
    }

    public SSLConnection hostnameVerifier(HostnameVerifier verifier) {
        this.hostnameVerifier = verifier;
        return this;
    }

    private SslContextFactory.Server sslContextFactory() {
        SslContextFactory.Server contextFactory = new SslContextFactory.Server();

        if (trustStore != null) {
            contextFactory.setTrustStorePath(trustStore.getFile().getPath());
            contextFactory.setTrustStorePassword(trustStorePassword);
        } else {
            contextFactory.setTrustAll(true);
        }

        if (keyStore != null) {
            contextFactory.setKeyStorePath(keyStore.getFile().getPath());
            contextFactory.setKeyStorePassword(keyStorePassword);
        }

        return contextFactory;
    }

    private HttpConfiguration httpConfiguration(int securePort) {
        HttpConfiguration parent = new HttpConfiguration();
        parent.setSecureScheme("https");
        parent.setSecurePort(securePort);
        HttpConfiguration configuration = new HttpConfiguration(parent);
        SecureRequestCustomizer secureRequestCustomizer = new SecureRequestCustomizer();
        secureRequestCustomizer.setSniHostCheck(false);
        configuration.setCustomizers(Collections.singletonList(secureRequestCustomizer));
        return configuration;
    }
}
