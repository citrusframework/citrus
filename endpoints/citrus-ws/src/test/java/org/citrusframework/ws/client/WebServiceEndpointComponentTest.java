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

package org.citrusframework.ws.client;

import java.util.Map;

import org.citrusframework.context.TestContext;
import org.citrusframework.endpoint.Endpoint;
import org.citrusframework.endpoint.EndpointComponent;
import org.citrusframework.endpoint.direct.DirectEndpointComponent;
import org.citrusframework.endpoint.context.MessageStoreEndpointComponent;
import org.citrusframework.http.client.HttpEndpointComponent;
import org.citrusframework.http.client.HttpsEndpointComponent;
import org.citrusframework.jms.endpoint.JmsEndpointComponent;
import org.citrusframework.message.ErrorHandlingStrategy;
import org.citrusframework.spi.ReferenceResolver;
import org.mockito.Mockito;
import org.springframework.ws.WebServiceMessageFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

public class WebServiceEndpointComponentTest {

    private ReferenceResolver referenceResolver = Mockito.mock(ReferenceResolver.class);
    private WebServiceMessageFactory messageFactory = Mockito.mock(WebServiceMessageFactory.class);
    private TestContext context = new TestContext();

    @BeforeEach
    public void setup() {
        context.setReferenceResolver(referenceResolver);
    }

    @Test
    public void testCreateClientEndpoint() {
        WebServiceEndpointComponent component = new WebServiceEndpointComponent();

        reset(referenceResolver);
        Endpoint endpoint = component.createEndpoint("http://localhost:8088/test", context);

        Assertions.assertEquals(WebServiceClient.class, endpoint.getClass());

        Assertions.assertEquals("http://localhost:8088/test", ((WebServiceClient)endpoint).getEndpointConfiguration().getDefaultUri());
        Assertions.assertEquals(ErrorHandlingStrategy.THROWS_EXCEPTION, ((WebServiceClient) endpoint).getEndpointConfiguration().getErrorHandlingStrategy());
        Assertions.assertEquals(5000L, ((WebServiceClient) endpoint).getEndpointConfiguration().getTimeout());

    }

    @Test
    public void testCreateClientEndpointWithParameters() {
        WebServiceEndpointComponent component = new WebServiceEndpointComponent();

        reset(referenceResolver);
        when(referenceResolver.isResolvable("myMessageFactory")).thenReturn(true);
        when(referenceResolver.resolve("myMessageFactory", WebServiceMessageFactory.class)).thenReturn(messageFactory);
        Endpoint endpoint = component.createEndpoint("http:localhost:8088?timeout=10000&errorHandlingStrategy=propagateError&messageFactory=myMessageFactory", context);

        Assertions.assertEquals(WebServiceClient.class, endpoint.getClass());

        Assertions.assertEquals("http://localhost:8088", ((WebServiceClient)endpoint).getEndpointConfiguration().getDefaultUri());
        Assertions.assertEquals(messageFactory, ((WebServiceClient) endpoint).getEndpointConfiguration().getMessageFactory());
        Assertions.assertEquals(ErrorHandlingStrategy.PROPAGATE, ((WebServiceClient) endpoint).getEndpointConfiguration().getErrorHandlingStrategy());
        Assertions.assertEquals(10000L, ((WebServiceClient) endpoint).getEndpointConfiguration().getTimeout());
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
        Assertions.assertNotNull(validators.get("http"));
        Assertions.assertEquals(HttpEndpointComponent.class, validators.get("http").getClass());
        Assertions.assertNotNull(validators.get("https"));
        Assertions.assertEquals(HttpsEndpointComponent.class, validators.get("https").getClass());
        Assertions.assertNotNull(validators.get("soap"));
        Assertions.assertEquals(WebServiceEndpointComponent.class, validators.get("soap").getClass());
    }

    @Test
    public void testLookupByQualifier() {
        Assertions.assertTrue(EndpointComponent.lookup("soap").isPresent());
    }
}
