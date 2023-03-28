/*
 * Copyright 2006-2014 the original author or authors.
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
import org.citrusframework.http.client.HttpEndpointComponent;
import org.citrusframework.http.client.HttpsEndpointComponent;
import org.citrusframework.jms.endpoint.JmsEndpointComponent;
import org.citrusframework.message.ErrorHandlingStrategy;
import org.citrusframework.spi.ReferenceResolver;
import org.mockito.Mockito;
import org.springframework.ws.WebServiceMessageFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;


/**
 * @author Christoph Deppisch
 */
public class WebServiceEndpointComponentTest {

    private ReferenceResolver referenceResolver = Mockito.mock(ReferenceResolver.class);
    private WebServiceMessageFactory messageFactory = Mockito.mock(WebServiceMessageFactory.class);
    private TestContext context = new TestContext();

    @BeforeClass
    public void setup() {
        context.setReferenceResolver(referenceResolver);
    }

    @Test
    public void testCreateClientEndpoint() throws Exception {
        WebServiceEndpointComponent component = new WebServiceEndpointComponent();

        reset(referenceResolver);
        Endpoint endpoint = component.createEndpoint("http://localhost:8088/test", context);

        Assert.assertEquals(endpoint.getClass(), WebServiceClient.class);

        Assert.assertEquals(((WebServiceClient)endpoint).getEndpointConfiguration().getDefaultUri(), "http://localhost:8088/test");
        Assert.assertEquals(((WebServiceClient) endpoint).getEndpointConfiguration().getErrorHandlingStrategy(), ErrorHandlingStrategy.THROWS_EXCEPTION);
        Assert.assertEquals(((WebServiceClient) endpoint).getEndpointConfiguration().getTimeout(), 5000L);

    }

    @Test
    public void testCreateClientEndpointWithParameters() throws Exception {
        WebServiceEndpointComponent component = new WebServiceEndpointComponent();

        reset(referenceResolver);
        when(referenceResolver.isResolvable("myMessageFactory")).thenReturn(true);
        when(referenceResolver.resolve("myMessageFactory", WebServiceMessageFactory.class)).thenReturn(messageFactory);
        Endpoint endpoint = component.createEndpoint("http:localhost:8088?timeout=10000&errorHandlingStrategy=propagateError&messageFactory=myMessageFactory", context);

        Assert.assertEquals(endpoint.getClass(), WebServiceClient.class);

        Assert.assertEquals(((WebServiceClient)endpoint).getEndpointConfiguration().getDefaultUri(), "http://localhost:8088");
        Assert.assertEquals(((WebServiceClient) endpoint).getEndpointConfiguration().getMessageFactory(), messageFactory);
        Assert.assertEquals(((WebServiceClient) endpoint).getEndpointConfiguration().getErrorHandlingStrategy(), ErrorHandlingStrategy.PROPAGATE);
        Assert.assertEquals(((WebServiceClient) endpoint).getEndpointConfiguration().getTimeout(), 10000L);
    }

    @Test
    public void testLookupAll() {
        Map<String, EndpointComponent> validators = EndpointComponent.lookup();
        Assert.assertEquals(validators.size(), 5L);
        Assert.assertNotNull(validators.get("direct"));
        Assert.assertEquals(validators.get("direct").getClass(), DirectEndpointComponent.class);
        Assert.assertNotNull(validators.get("jms"));
        Assert.assertEquals(validators.get("jms").getClass(), JmsEndpointComponent.class);
        Assert.assertNotNull(validators.get("http"));
        Assert.assertEquals(validators.get("http").getClass(), HttpEndpointComponent.class);
        Assert.assertNotNull(validators.get("https"));
        Assert.assertEquals(validators.get("https").getClass(), HttpsEndpointComponent.class);
        Assert.assertNotNull(validators.get("soap"));
        Assert.assertEquals(validators.get("soap").getClass(), WebServiceEndpointComponent.class);
    }

    @Test
    public void testLookupByQualifier() {
        Assert.assertTrue(EndpointComponent.lookup("soap").isPresent());
    }
}
