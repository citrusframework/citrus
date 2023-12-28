/*
 *  Copyright 2023-2024 the original author or authors.
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

import org.apache.hc.client5.http.auth.AuthScope;
import org.apache.hc.client5.http.auth.UsernamePasswordCredentials;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.http.client.BasicAuthClientHttpRequestFactory;
import org.citrusframework.http.client.HttpClient;
import org.eclipse.jetty.security.SecurityHandler;
import org.eclipse.jetty.security.authentication.BasicAuthenticator;
import org.springframework.http.client.ClientHttpRequestFactory;

import java.net.URL;

import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.citrusframework.util.StringUtils.hasText;

/**
 * Basic authentication implementation able to create a proper request factory with basic auth client credentials.
 * Trying to read hostname and port from given request URL on the Http client in order to set a proper auth scope.
 */
public class BasicAuthentication implements HttpAuthentication {

    private final String username;
    private final String password;

    private String realm = "";
    private String[] userRoles = new String[]{"citrus"};

    public BasicAuthentication(String username, String password) {
        this.username = username;
        this.password = password;
    }

    @Override
    public SecurityHandler getSecurityHandler(String resourcePath) {
        try {
            SecurityHandlerFactory securityHandlerFactory = new SecurityHandlerFactory();
            securityHandlerFactory.setUsers(singletonList(new User(username, password, userRoles)));
            securityHandlerFactory.setConstraints(singletonMap(resourcePath, new BasicAuthConstraint(userRoles)));

            securityHandlerFactory.setAuthenticator(new BasicAuthenticator());
            securityHandlerFactory.setRealm(realm);

            securityHandlerFactory.initialize();

            return securityHandlerFactory.getObject();
        } catch (Exception e) {
            throw new CitrusRuntimeException(e);
        }
    }

    @Override
    public ClientHttpRequestFactory getRequestFactory(String requestUrl, HttpClient httpClient) {
        try {
            BasicAuthClientHttpRequestFactory requestFactory = new BasicAuthClientHttpRequestFactory();

            if (httpClient != null) {
                requestFactory.setHttpClient(httpClient.getEndpointConfiguration().getHttpClient());
            }

            URL url = null;
            if (hasText(requestUrl)) {
                url = new URL(requestUrl);
            } else if (httpClient != null && hasText(httpClient.getEndpointConfiguration().getRequestUrl())) {
                url = new URL(httpClient.getEndpointConfiguration().getRequestUrl());
            }

            if (url != null) {
                AuthScope authScope = new AuthScope(url.getProtocol(), url.getHost(), url.getPort(), realm, "basic");
                requestFactory.setAuthScope(authScope);
            }

            UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(username, password.toCharArray());
            requestFactory.setCredentials(credentials);

            requestFactory.initialize();
            return requestFactory.getObject();
        } catch (Exception e) {
            throw new CitrusRuntimeException("Failed to configure basic auth on Http client", e);
        }
    }

    public BasicAuthentication realm(String realm) {
        this.realm = realm;
        return this;
    }

    public BasicAuthentication userRoles(String... roles) {
        this.userRoles = roles;
        return this;
    }
}
