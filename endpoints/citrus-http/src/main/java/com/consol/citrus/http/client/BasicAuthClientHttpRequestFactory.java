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

import java.net.URI;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.HttpClient;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.*;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.util.Assert;

/**
 * Factory bean constructing a client request factory with 
 * user credentials for basic authentication.
 * 
 * @author Christoph Deppisch
 * @since 1.2
 */
public class BasicAuthClientHttpRequestFactory implements FactoryBean<HttpComponentsClientHttpRequestFactory>, InitializingBean {

    /** Custom Http params */
    private Map<String, Object> params;
    
    /** The target request factory */
    private HttpClient httpClient;
    
    /** User credentials for basic authentication */
    private Credentials credentials;
    
    /** Authentiacation scope */
    private AuthScope authScope = new AuthScope("localhost", 8080, AuthScope.ANY_REALM, AuthScope.ANY_SCHEME);
    
    /** Logger */
    private static Logger log = LoggerFactory.getLogger(BasicAuthClientHttpRequestFactory.class);

    /**
     * Construct the client factory bean with user credentials.
     */
    public HttpComponentsClientHttpRequestFactory getObject() throws Exception {
        Assert.notNull(credentials, "User credentials not set properly!");
        
        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory(httpClient) {
            @Override
            protected HttpContext createHttpContext(HttpMethod httpMethod, URI uri) {
                // we have to use preemptive authentication
                // therefore add some basic auth cache to the local context
                AuthCache authCache = new BasicAuthCache();
                BasicScheme basicAuth = new BasicScheme();
                
                authCache.put(new HttpHost(authScope.getHost(), authScope.getPort(), "http"), basicAuth);
                authCache.put(new HttpHost(authScope.getHost(), authScope.getPort(), "https"), basicAuth);
                
                BasicHttpContext localcontext = new BasicHttpContext();
                localcontext.setAttribute(ClientContext.AUTH_CACHE, authCache);
                
                return localcontext;
            }
        };
        
        if (httpClient instanceof AbstractHttpClient) {
            ((AbstractHttpClient)httpClient).getCredentialsProvider().setCredentials(authScope, credentials);
        } else {
            log.warn("Unable to set username password credentials for basic authentication, " +
            		"because nested HttpClient implementation does not support a credentials provider!");
        }
        
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
    
    /**
     * {@inheritDoc}
     */
    public void afterPropertiesSet() throws Exception {
        if (httpClient == null) {
            httpClient = new DefaultHttpClient();
        }
        
        if (params != null) {
            for (Entry<String, Object> param : params.entrySet()) {
                log.debug("Setting custom Http param on client: '" + param.getKey() + "'='" + param.getValue() + "'");
                httpClient.getParams().setParameter(param.getKey(), param.getValue());
            }
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
     * Sets the httpClient.
     * @param httpClient the httpClient to set
     */
    public void setHttpClient(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    /**
     * Sets the params.
     * @param params the params to set
     */
    public void setParams(Map<String, Object> params) {
        this.params = params;
    }
    
}
