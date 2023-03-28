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

package org.citrusframework.http.client;

import java.io.IOException;
import java.net.URI;

import org.citrusframework.testng.AbstractTestNGUnitTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

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
    }

    @Test
    public void testCreateRequestWithAuthScope() throws IOException {
        ClientHttpRequest request = requestFactory.createRequest(URI.create("http://localhost:8088"), HttpMethod.GET);
        Assert.assertNotNull(request);
    }
}
