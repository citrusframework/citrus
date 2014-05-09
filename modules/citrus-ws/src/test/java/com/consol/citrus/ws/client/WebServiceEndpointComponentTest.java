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

package com.consol.citrus.ws.client;

import com.consol.citrus.context.TestContext;
import com.consol.citrus.endpoint.Endpoint;
import com.consol.citrus.message.ErrorHandlingStrategy;
import org.easymock.EasyMock;
import org.springframework.context.ApplicationContext;
import org.springframework.ws.WebServiceMessageFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.easymock.EasyMock.*;

/**
 * @author Christoph Deppisch
 */
public class WebServiceEndpointComponentTest {

    private ApplicationContext applicationContext = EasyMock.createMock(ApplicationContext.class);
    private WebServiceMessageFactory messageFactory = EasyMock.createMock(WebServiceMessageFactory.class);
    private TestContext context = new TestContext();

    @BeforeClass
    public void setup() {
        context.setApplicationContext(applicationContext);
    }

    @Test
    public void testCreateClientEndpoint() throws Exception {
        WebServiceEndpointComponent component = new WebServiceEndpointComponent();

        reset(applicationContext);
        replay(applicationContext);

        Endpoint endpoint = component.createEndpoint("http://localhost:8088/test", context);

        Assert.assertEquals(endpoint.getClass(), WebServiceClient.class);

        Assert.assertEquals(((WebServiceClient)endpoint).getEndpointConfiguration().getDefaultUri(), "http://localhost:8088/test");
        Assert.assertEquals(((WebServiceClient) endpoint).getEndpointConfiguration().getErrorHandlingStrategy(), ErrorHandlingStrategy.THROWS_EXCEPTION);
        Assert.assertEquals(((WebServiceClient) endpoint).getEndpointConfiguration().getTimeout(), 5000L);

        verify(applicationContext);
    }

    @Test
    public void testCreateClientEndpointWithParameters() throws Exception {
        WebServiceEndpointComponent component = new WebServiceEndpointComponent();

        reset(applicationContext);
        expect(applicationContext.containsBean("myMessageFactory")).andReturn(true).once();
        expect(applicationContext.getBean("myMessageFactory")).andReturn(messageFactory).once();
        replay(applicationContext);

        Endpoint endpoint = component.createEndpoint("http:localhost:8088?timeout=10000&errorHandlingStrategy=propagateError&messageFactory=myMessageFactory", context);

        Assert.assertEquals(endpoint.getClass(), WebServiceClient.class);

        Assert.assertEquals(((WebServiceClient)endpoint).getEndpointConfiguration().getDefaultUri(), "http://localhost:8088");
        Assert.assertEquals(((WebServiceClient) endpoint).getEndpointConfiguration().getMessageFactory(), messageFactory);
        Assert.assertEquals(((WebServiceClient) endpoint).getEndpointConfiguration().getErrorHandlingStrategy(), ErrorHandlingStrategy.PROPAGATE);
        Assert.assertEquals(((WebServiceClient) endpoint).getEndpointConfiguration().getTimeout(), 10000L);

        verify(applicationContext);
    }
}
