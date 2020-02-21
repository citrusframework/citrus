/*
 * Copyright 2006-2013 the original author or authors.
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

import java.io.IOException;
import java.net.URI;

import org.apache.http.auth.AuthScope;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.AbstractHttpClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.consol.citrus.testng.AbstractTestNGUnitTest;

/**
 * @author Christoph Deppisch
 */
public class BasicAuthClientRequestFactoryTest extends AbstractTestNGUnitTest {

    @Autowired
    private HttpComponentsClientHttpRequestFactory requestFactory;
    
    @Test
    public void testFactory() {
        Assert.assertNotNull(requestFactory);
        Assert.assertNotNull(requestFactory.getHttpClient());
        Assert.assertNotNull(requestFactory.getHttpClient().getParams());
        
        CredentialsProvider credentialsProvider = ((AbstractHttpClient)requestFactory.getHttpClient()).getCredentialsProvider();
        AuthScope authScope = new AuthScope("localhost", 8088, "", "basic");
        Assert.assertNotNull(credentialsProvider);
        Assert.assertNotNull(credentialsProvider.getCredentials(authScope));
        Assert.assertNotNull(credentialsProvider.getCredentials(authScope).getUserPrincipal().getName(), "someUsername");
        Assert.assertNotNull(credentialsProvider.getCredentials(authScope).getPassword(), "somePassword");
        
        Assert.assertNotNull(requestFactory.getHttpClient().getParams().getParameter("http.socket.timeout"));
        Assert.assertEquals(requestFactory.getHttpClient().getParams().getIntParameter("http.socket.timeout", 0), 10000);
    }
    
    @Test
    public void testCreateRequestWithAuthScope() throws IOException {
        ClientHttpRequest request = requestFactory.createRequest(URI.create("http://localhost:8088"), HttpMethod.GET);
        Assert.assertNotNull(request);
    }
}
