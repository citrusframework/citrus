/*
 * Copyright 2006-2011 the original author or authors.
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

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.http.client.CommonsClientHttpRequestFactory;
import org.springframework.util.Assert;

/**
 * Factory bean constructing a commons client request factory with 
 * user credentials for basic authentication.
 * 
 * @author Christoph Deppisch
 * @deprecated since 1.2, in favor of using {@link BasicAuthClientHttpRequestFactory}
 */
@Deprecated
public class UserCredentialsClientHttpRequestFactory implements FactoryBean<CommonsClientHttpRequestFactory> {

    /** The target request factory */
    private CommonsClientHttpRequestFactory targetRequestFactory;
    
    /** User credentials for basic authentication */
    private Credentials credentials;
    
    /** Authentiacation scope */
    private AuthScope authScope = AuthScope.ANY;

    /**
     * Construct the client factory bean with user credentials.
     */
    public CommonsClientHttpRequestFactory getObject() throws Exception {
        Assert.notNull(credentials, "User credentials not set properly!");
        
        targetRequestFactory.getHttpClient().getState().setCredentials(authScope, credentials);
        return targetRequestFactory;
    }

    /**
     * Get the object type.
     */
    public Class<?> getObjectType() {
        return CommonsClientHttpRequestFactory.class;
    }

    /**
     * Is singleton bean?
     */
    public boolean isSingleton() {
        return false;
    }

    /**
     * Sets the targetRequestFactory.
     * @param targetRequestFactory the targetRequestFactory to set
     */
    public void setTargetRequestFactory(
            CommonsClientHttpRequestFactory targetRequestFactory) {
        this.targetRequestFactory = targetRequestFactory;
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
}
