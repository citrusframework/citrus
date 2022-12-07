/*
 * Copyright 2006-2012 the original author or authors.
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

package com.consol.citrus.http.client;

import com.consol.citrus.common.InitializingPhase;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import org.apache.hc.client5.http.auth.AuthCache;
import org.apache.hc.client5.http.auth.AuthScope;
import org.apache.hc.client5.http.auth.Credentials;
import org.apache.hc.client5.http.impl.auth.BasicAuthCache;
import org.apache.hc.client5.http.impl.auth.BasicCredentialsProvider;
import org.apache.hc.client5.http.impl.auth.BasicScheme;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.protocol.HttpClientContext;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.protocol.BasicHttpContext;
import org.apache.hc.core5.net.URIBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.util.Assert;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Factory bean constructing a client request factory with
 * user credentials for basic authentication.
 *
 * @author Christoph Deppisch
 * @since 1.2
 */
public class BasicAuthClientHttpRequestFactory implements FactoryBean<HttpComponentsClientHttpRequestFactory>, InitializingPhase {

    /** Custom Http params */
    private Map<String, Object> params;

    /** The target request factory */
    private HttpClientBuilder httpClientBuilder;

    /** User credentials for basic authentication */
    private Credentials credentials;

    /** Authentiacation scope */
    private AuthScope authScope = new AuthScope("localhost", 8080);

    /** Logger */
    private static Logger log = LoggerFactory.getLogger(BasicAuthClientHttpRequestFactory.class);

    /**
     * Construct the client factory bean with user credentials.
     */
    public HttpComponentsClientHttpRequestFactory getObject() throws Exception {
        Assert.notNull(credentials, "User credentials not set properly!");

        BasicCredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(authScope, credentials);
        httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);

        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory(httpClientBuilder.build()){

            @Override
            protected ClassicHttpRequest createHttpUriRequest(HttpMethod httpMethod, URI uri) {
                ClassicHttpRequest httpRequest = super.createHttpUriRequest(httpMethod,uri);
                URIBuilder builder = new URIBuilder(uri);
                if (params != null) {
                    for (Entry<String, Object> param : params.entrySet()) {
                        log.debug("Setting custom Http param on client: '" + param.getKey() + "'='" + param.getValue() + "'");
                        builder.addParameter(param.getKey(), param.getValue().toString());
                    }
                }
                try {
                    httpRequest.setUri(builder.build());
                } catch (URISyntaxException e) {
                    throw new CitrusRuntimeException(e);
                }
                return httpRequest;
            }
            } ;

        requestFactory.setHttpContextFactory((HttpMethod httpMethod, URI uri) ->{
                // we have to use preemptive authentication
                // therefore add some basic auth cache to the local context
                AuthCache authCache = new BasicAuthCache();
                BasicScheme basicAuth = new BasicScheme();

                authCache.put(new HttpHost("http",authScope.getHost(), authScope.getPort()), basicAuth);
                authCache.put(new HttpHost( "https",authScope.getHost(), authScope.getPort()), basicAuth);

                BasicHttpContext localcontext = new BasicHttpContext();
                localcontext.setAttribute(HttpClientContext.AUTH_CACHE, authCache);

                return localcontext;
            });

        return requestFactory;
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
        if (httpClientBuilder == null) {
            httpClientBuilder = HttpClientBuilder.create();
        }
    }

    /**
     * Sets the credentials.
     * @param credentials the credentials to set
     */
    public void setCredentials(Credentials credentials) {
        this.credentials = credentials;
    }

    /**
     * Sets the authScope.
     * @param authScope the authScope to set
     */
    public void setAuthScope(AuthScope authScope) {
        this.authScope = authScope;
    }

    /**
     * Sets the httpClientBuilder.
     * @param httpClientBuilder the httpClientBuilder to set
     */
    public void setHttpClientBuilder(HttpClientBuilder httpClientBuilder) {
        this.httpClientBuilder = httpClientBuilder;
    }

    /**
     * Sets the params.
     * @param params the params to set
     */
    public void setParams(Map<String, Object> params) {
        this.params = params;
    }
}
