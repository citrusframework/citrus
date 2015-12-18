/*
 * Copyright 2006-2015 the original author or authors.
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

package com.consol.citrus.rmi.endpoint;

import com.consol.citrus.context.TestContext;
import com.consol.citrus.endpoint.Endpoint;
import com.consol.citrus.rmi.client.RmiClient;
import org.mockito.Mockito;
import org.springframework.context.ApplicationContext;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.mockito.Mockito.reset;

/**
 * @author Christoph Deppisch
 * @since 2.5
 */
public class RmiEndpointComponentTest {

    private ApplicationContext applicationContext = Mockito.mock(ApplicationContext.class);
    private TestContext context = new TestContext();

    @BeforeClass
    public void setup() {
        context.setApplicationContext(applicationContext);
    }

    @Test
    public void testCreateClientEndpoint() throws Exception {
        RmiEndpointComponent component = new RmiEndpointComponent();

        Endpoint endpoint = component.createEndpoint("rmi://localhost:2099", context);

        Assert.assertEquals(endpoint.getClass(), RmiClient.class);

        Assert.assertEquals(((RmiClient)endpoint).getEndpointConfiguration().getHost(), "localhost");
        Assert.assertEquals(((RmiClient)endpoint).getEndpointConfiguration().getPort(), 2099);
        Assert.assertEquals(((RmiClient) endpoint).getEndpointConfiguration().getTimeout(), 5000L);

        endpoint = component.createEndpoint("rmi://localhost:2099/news", context);

        Assert.assertEquals(endpoint.getClass(), RmiClient.class);

        Assert.assertEquals(((RmiClient)endpoint).getEndpointConfiguration().getHost(), "localhost");
        Assert.assertEquals(((RmiClient)endpoint).getEndpointConfiguration().getPort(), 2099);
        Assert.assertEquals(((RmiClient) endpoint).getEndpointConfiguration().getBinding(), "news");
        Assert.assertEquals(((RmiClient) endpoint).getEndpointConfiguration().getTimeout(), 5000L);

        endpoint = component.createEndpoint("rmi://localhost:2099/service/news", context);

        Assert.assertEquals(endpoint.getClass(), RmiClient.class);

        Assert.assertEquals(((RmiClient)endpoint).getEndpointConfiguration().getHost(), "localhost");
        Assert.assertEquals(((RmiClient)endpoint).getEndpointConfiguration().getPort(), 2099);
        Assert.assertEquals(((RmiClient) endpoint).getEndpointConfiguration().getBinding(), "service/news");
        Assert.assertEquals(((RmiClient) endpoint).getEndpointConfiguration().getTimeout(), 5000L);

        endpoint = component.createEndpoint("rmi:rmiserverhost", context);

        Assert.assertEquals(endpoint.getClass(), RmiClient.class);

        Assert.assertEquals(((RmiClient)endpoint).getEndpointConfiguration().getHost(), "rmiserverhost");
        Assert.assertEquals(((RmiClient)endpoint).getEndpointConfiguration().getPort(), 1099);
        Assert.assertEquals(((RmiClient) endpoint).getEndpointConfiguration().getTimeout(), 5000L);

        endpoint = component.createEndpoint("rmi:localhost/service", context);

        Assert.assertEquals(endpoint.getClass(), RmiClient.class);

        Assert.assertEquals(((RmiClient)endpoint).getEndpointConfiguration().getHost(), "localhost");
        Assert.assertEquals(((RmiClient)endpoint).getEndpointConfiguration().getPort(), 1099);
        Assert.assertEquals(((RmiClient) endpoint).getEndpointConfiguration().getBinding(), "service");
        Assert.assertEquals(((RmiClient) endpoint).getEndpointConfiguration().getTimeout(), 5000L);
    }

    @Test
    public void testCreateClientEndpointWithParameters() throws Exception {
        RmiEndpointComponent component = new RmiEndpointComponent();

        reset(applicationContext);
        Endpoint endpoint = component.createEndpoint("rmi://localhost:2099?binding=rmiBinding&timeout=10000", context);

        Assert.assertEquals(endpoint.getClass(), RmiClient.class);

        Assert.assertEquals(((RmiClient)endpoint).getEndpointConfiguration().getHost(), "localhost");
        Assert.assertEquals(((RmiClient)endpoint).getEndpointConfiguration().getPort(), 2099);
        Assert.assertEquals(((RmiClient) endpoint).getEndpointConfiguration().getBinding(), "rmiBinding");
        Assert.assertEquals(((RmiClient) endpoint).getEndpointConfiguration().getTimeout(), 10000L);

        endpoint = component.createEndpoint("rmi://localhost:2099/binding?binding=rmiBinding", context);

        Assert.assertEquals(endpoint.getClass(), RmiClient.class);

        Assert.assertEquals(((RmiClient)endpoint).getEndpointConfiguration().getHost(), "localhost");
        Assert.assertEquals(((RmiClient)endpoint).getEndpointConfiguration().getPort(), 2099);
        Assert.assertEquals(((RmiClient) endpoint).getEndpointConfiguration().getBinding(), "rmiBinding");
        Assert.assertEquals(((RmiClient) endpoint).getEndpointConfiguration().getTimeout(), 5000L);
    }

}