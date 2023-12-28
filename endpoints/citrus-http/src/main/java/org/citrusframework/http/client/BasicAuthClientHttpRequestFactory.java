/*
 * Copyright 2006-2024 the original author or authors.
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

package org.citrusframework.http.client;

import org.apache.hc.client5.http.auth.AuthCache;
import org.apache.hc.client5.http.auth.AuthScope;
import org.apache.hc.client5.http.auth.Credentials;
import org.apache.hc.client5.http.impl.auth.BasicAuthCache;
import org.apache.hc.client5.http.impl.auth.BasicCredentialsProvider;
import org.apache.hc.client5.http.impl.auth.BasicScheme;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.protocol.HttpClientContext;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.citrusframework.common.InitializingPhase;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;

import java.net.URI;

import static org.citrusframework.util.ObjectHelper.assertNotNull;

/**
 * Factory bean constructing a client request factory with
 * user credentials for basic authentication.
 *
 * @author Christoph Deppisch
 * @since 1.2
 */
public class BasicAuthClientHttpRequestFactory implements FactoryBean<HttpComponentsClientHttpRequestFactory>, InitializingPhase {

    /**
     * The target request factory
     */
    private HttpClientBuilder httpClient;

    /**
     * User credentials for basic authentication
     */
    private Credentials credentials;

    /**
     * Authentiacation scope
     */
    private AuthScope authScope = new AuthScope(new HttpHost("http", "localhost", 8080));

    /**
     * Construct the client factory bean with user credentials.
     */
    public HttpComponentsClientHttpRequestFactory getObject() throws Exception {
        assertNotNull(credentials, "User credentials not set properly!");

        return new HttpComponentsClientHttpRequestFactory(httpClient.build()) {

            @Override
            protected HttpContext createHttpContext(HttpMethod httpMethod, URI uri) {
                // we have to use preemptive authentication
                // therefore add some basic auth cache to the local context
                AuthCache authCache = new BasicAuthCache();
                BasicScheme basicAuth = new BasicScheme();
                basicAuth.initPreemptive(credentials);

                HttpHost httpTarget = new HttpHost(uri.getScheme(), authScope.getHost(), authScope.getPort());
                authCache.put(httpTarget, basicAuth);

                BasicCredentialsProvider credentialsProvider = new BasicCredentialsProvider();
                credentialsProvider.setCredentials(new AuthScope(httpTarget), credentials);

                HttpClientContext httpClientContext = HttpClientContext.create();
                httpClientContext.setAuthCache(authCache);
                httpClientContext.setCredentialsProvider(credentialsProvider);

                return httpClientContext;
            }
        };
    }

    /**
     * Get the object type.
     */
    public Class<?> getObjectType() {
        return HttpComponentsClientHttpRequestFactory.class;
    }

    /**
     * Is singleton bean?
     */
    public boolean isSingleton() {
        return false;
    }

    @Override
    public void initialize() {
        BasicCredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(authScope, credentials);

        if (httpClient == null) {
            httpClient = HttpClients.custom()
                    .setDefaultCredentialsProvider(credentialsProvider);
        } else {
            httpClient.setDefaultCredentialsProvider(credentialsProvider);
        }
    }

    /**
     * Sets the credentials.
     *
     * @param credentials the credentials to set
     */
    public void setCredentials(Credentials credentials) {
        this.credentials = credentials;
    }

    /**
     * Sets the authScope.
     *
     * @param authScope the authScope to set
     */
    public void setAuthScope(AuthScope authScope) {
        this.authScope = authScope;
    }

    /**
     * Sets the httpClient.
     *
     * @param httpClient the httpClient to set
     */
    public void setHttpClient(HttpClientBuilder httpClient) {
        this.httpClient = httpClient;
    }
}
