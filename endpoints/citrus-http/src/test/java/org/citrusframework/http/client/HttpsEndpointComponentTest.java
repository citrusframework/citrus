/*
 * Copyright the original author or authors.
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

import java.util.Map;

import org.citrusframework.springintegration.channel.ChannelEndpointComponent;
import org.citrusframework.context.TestContext;
import org.citrusframework.endpoint.Endpoint;
import org.citrusframework.endpoint.EndpointComponent;
import org.citrusframework.endpoint.direct.DirectEndpointComponent;
import org.citrusframework.endpoint.context.MessageStoreEndpointComponent;
import org.citrusframework.jms.endpoint.JmsEndpointComponent;
import org.citrusframework.message.ErrorHandlingStrategy;
import org.citrusframework.spi.ReferenceResolver;
import org.mockito.Mockito;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.web.bind.annotation.RequestMethod;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

public class HttpsEndpointComponentTest {

    private ReferenceResolver referenceResolver = Mockito.mock(ReferenceResolver.class);
    private ClientHttpRequestFactory requestFactory = Mockito.mock(ClientHttpRequestFactory.class);
    private TestContext context = new TestContext();

    @BeforeEach
    public void setup() {
        context.setReferenceResolver(referenceResolver);
    }

    @Test
    public void testCreateClientEndpoint() {
        HttpsEndpointComponent component = new HttpsEndpointComponent();

        Endpoint endpoint = component.createEndpoint("https://localhost:8088/test", context);

        Assertions.assertEquals(HttpClient.class, endpoint.getClass());

        Assertions.assertEquals("https://localhost:8088/test", ((HttpClient)endpoint).getEndpointConfiguration().getRequestUrl());
        Assertions.assertEquals(RequestMethod.POST, ((HttpClient) endpoint).getEndpointConfiguration().getRequestMethod());
        Assertions.assertEquals(ErrorHandlingStrategy.PROPAGATE, ((HttpClient) endpoint).getEndpointConfiguration().getErrorHandlingStrategy());
        Assertions.assertEquals(5000L, ((HttpClient) endpoint).getEndpointConfiguration().getTimeout());
    }

    @Test
    public void testCreateClientEndpointWithParameters() {
        HttpsEndpointComponent component = new HttpsEndpointComponent();

        reset(referenceResolver);
        when(referenceResolver.isResolvable("sslRequestFactory")).thenReturn(true);
        when(referenceResolver.resolve("sslRequestFactory", ClientHttpRequestFactory.class)).thenReturn(requestFactory);
        Endpoint endpoint = component.createEndpoint("https:localhost:8088?requestFactory=sslRequestFactory", context);

        Assertions.assertEquals(HttpClient.class, endpoint.getClass());

        Assertions.assertEquals("https://localhost:8088", ((HttpClient)endpoint).getEndpointConfiguration().getRequestUrl());
        Assertions.assertEquals(requestFactory, ((HttpClient) endpoint).getEndpointConfiguration().getRequestFactory());
        Assertions.assertEquals(5000L, ((HttpClient) endpoint).getEndpointConfiguration().getTimeout());

    }

    @Test
    public void testCreateClientEndpointWithCustomParameters() {
        HttpsEndpointComponent component = new HttpsEndpointComponent();

        Endpoint endpoint = component.createEndpoint("https://localhost:8088/test?requestMethod=DELETE&customParam=foo", context);

        Assertions.assertEquals(HttpClient.class, endpoint.getClass());

        Assertions.assertEquals("https://localhost:8088/test?customParam=foo", ((HttpClient)endpoint).getEndpointConfiguration().getRequestUrl());
        Assertions.assertEquals(RequestMethod.DELETE, ((HttpClient) endpoint).getEndpointConfiguration().getRequestMethod());
        Assertions.assertEquals(5000L, ((HttpClient) endpoint).getEndpointConfiguration().getTimeout());
    }

    @Test
    public void testLookupAll() {
        Map<String, EndpointComponent> validators = EndpointComponent.lookup();
        Assertions.assertEquals(6L, validators.size());
        Assertions.assertNotNull(validators.get("direct"));
        Assertions.assertEquals(DirectEndpointComponent.class, validators.get("direct").getClass());
        Assertions.assertNotNull(validators.get("message-store"));
        Assertions.assertEquals(MessageStoreEndpointComponent.class, validators.get("message-store").getClass());
        Assertions.assertNotNull(validators.get("jms"));
        Assertions.assertEquals(JmsEndpointComponent.class, validators.get("jms").getClass());
        Assertions.assertNotNull(validators.get("channel"));
        Assertions.assertEquals(ChannelEndpointComponent.class, validators.get("channel").getClass());
        Assertions.assertNotNull(validators.get("http"));
        Assertions.assertEquals(HttpEndpointComponent.class, validators.get("http").getClass());
        Assertions.assertNotNull(validators.get("https"));
        Assertions.assertEquals(HttpsEndpointComponent.class, validators.get("https").getClass());
    }

    @Test
    public void testLookupByQualifier() {
        Assertions.assertTrue(EndpointComponent.lookup("https").isPresent());
    }
}
