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

import org.citrusframework.http.client.HttpClient;
import org.eclipse.jetty.security.SecurityHandler;
import org.springframework.http.client.ClientHttpRequestFactory;

public interface HttpAuthentication {

    /**
     * Security handler able to set up server user authentication on given resource path.
     *
     * @param resourcePath
     * @return
     */
    SecurityHandler getSecurityHandler(String resourcePath);

    /**
     * Creates client request factory that uses the given authentication method.
     *
     * @param requestUrl
     * @param httpClient
     * @return
     */
    ClientHttpRequestFactory getRequestFactory(String requestUrl, HttpClient httpClient);

    static HttpAuthentication basic(String username, String password) {
        return new BasicAuthentication(username, password);
    }
}
