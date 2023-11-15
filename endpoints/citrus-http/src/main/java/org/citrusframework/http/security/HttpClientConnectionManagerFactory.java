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

import org.apache.hc.client5.http.io.HttpClientConnectionManager;
import org.springframework.beans.factory.FactoryBean;

/**
 * Spring bean factory for a secure SSL connection manager.
 * Factory may be used in Spring bean configuration to set connection manager on a Http client.
 */
public class HttpClientConnectionManagerFactory implements FactoryBean<HttpClientConnectionManager> {

    private final HttpSecureConnection secureConnection;

    public HttpClientConnectionManagerFactory(HttpSecureConnection secureConnection) {
        this.secureConnection = secureConnection;
    }

    @Override
    public HttpClientConnectionManager getObject() throws Exception {
        return secureConnection.getClientConnectionManager();
    }

    @Override
    public Class<?> getObjectType() {
        return HttpClientConnectionManager.class;
    }
}
