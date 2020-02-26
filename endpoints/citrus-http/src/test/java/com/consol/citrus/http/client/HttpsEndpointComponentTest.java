/*
 * Copyright 2006-2016 the original author or authors.
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

import com.consol.citrus.spi.ReferenceResolver;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.endpoint.Endpoint;
import com.consol.citrus.message.ErrorHandlingStrategy;
import org.mockito.Mockito;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;


/**
 * @author Christoph Deppisch
 */
public class HttpsEndpointComponentTest {

    private ReferenceResolver referenceResolver = Mockito.mock(ReferenceResolver.class);
    private ClientHttpRequestFactory requestFactory = Mockito.mock(ClientHttpRequestFactory.class);
    private TestContext context = new TestContext();

    @BeforeClass
    public void setup() {
        context.setReferenceResolver(referenceResolver);
    }

    @Test
    public void testCreateClientEndpoint() throws Exception {
        HttpsEndpointComponent component = new HttpsEndpointComponent();

        Endpoint endpoint = component.createEndpoint("https://localhost:8088/test", context);

        Assert.assertEquals(endpoint.getClass(), HttpClient.class);

        Assert.assertEquals(((HttpClient)endpoint).getEndpointConfiguration().getRequestUrl(), "https://localhost:8088/test");
        Assert.assertEquals(((HttpClient) endpoint).getEndpointConfiguration().getRequestMethod(), HttpMethod.POST);
        Assert.assertEquals(((HttpClient) endpoint).getEndpointConfiguration().getErrorHandlingStrategy(), ErrorHandlingStrategy.PROPAGATE);
        Assert.assertEquals(((HttpClient) endpoint).getEndpointConfiguration().getTimeout(), 5000L);
    }

    @Test
    public void testCreateClientEndpointWithParameters() throws Exception {
        HttpsEndpointComponent component = new HttpsEndpointComponent();

        reset(referenceResolver);
        when(referenceResolver.isResolvable("sslRequestFactory")).thenReturn(true);
        when(referenceResolver.resolve("sslRequestFactory", ClientHttpRequestFactory.class)).thenReturn(requestFactory);
        Endpoint endpoint = component.createEndpoint("https:localhost:8088?requestFactory=sslRequestFactory", context);

        Assert.assertEquals(endpoint.getClass(), HttpClient.class);

        Assert.assertEquals(((HttpClient)endpoint).getEndpointConfiguration().getRequestUrl(), "https://localhost:8088");
        Assert.assertEquals(((HttpClient) endpoint).getEndpointConfiguration().getRequestFactory(), requestFactory);
        Assert.assertEquals(((HttpClient) endpoint).getEndpointConfiguration().getTimeout(), 5000L);

    }

    @Test
    public void testCreateClientEndpointWithCustomParameters() throws Exception {
        HttpsEndpointComponent component = new HttpsEndpointComponent();

        Endpoint endpoint = component.createEndpoint("https://localhost:8088/test?requestMethod=DELETE&customParam=foo", context);

        Assert.assertEquals(endpoint.getClass(), HttpClient.class);

        Assert.assertEquals(((HttpClient)endpoint).getEndpointConfiguration().getRequestUrl(), "https://localhost:8088/test?customParam=foo");
        Assert.assertEquals(((HttpClient) endpoint).getEndpointConfiguration().getRequestMethod(), HttpMethod.DELETE);
        Assert.assertEquals(((HttpClient) endpoint).getEndpointConfiguration().getTimeout(), 5000L);
    }
}
