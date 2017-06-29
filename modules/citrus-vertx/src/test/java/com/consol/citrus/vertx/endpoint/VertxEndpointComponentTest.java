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

package com.consol.citrus.vertx.endpoint;

import com.consol.citrus.context.TestContext;
import com.consol.citrus.endpoint.Endpoint;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.vertx.factory.VertxInstanceFactory;
import org.mockito.Mockito;
import org.springframework.context.ApplicationContext;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.mockito.Mockito.*;


/**
 * @author Christoph Deppisch
 * @since 1.4.1
 */
public class VertxEndpointComponentTest {

    private ApplicationContext applicationContext = Mockito.mock(ApplicationContext.class);
    private VertxInstanceFactory instanceFactory = Mockito.mock(VertxInstanceFactory.class);
    private TestContext context = new TestContext();

    @BeforeClass
    public void setup() {
        context.setApplicationContext(applicationContext);
    }

    @Test
    public void testCreateEndpoint() throws Exception {
        VertxEndpointComponent component = new VertxEndpointComponent();

        reset(applicationContext);
        when(applicationContext.containsBean("vertxInstanceFactory")).thenReturn(true);
        when(applicationContext.getBean("vertxInstanceFactory", VertxInstanceFactory.class)).thenReturn(instanceFactory);
        Endpoint endpoint = component.createEndpoint("vertx:news", context);

        Assert.assertEquals(endpoint.getClass(), VertxEndpoint.class);

        Assert.assertEquals(((VertxEndpoint)endpoint).getEndpointConfiguration().getAddress(), "news");
        Assert.assertEquals(((VertxEndpoint)endpoint).getEndpointConfiguration().isPubSubDomain(), false);
        Assert.assertEquals(((VertxEndpoint) endpoint).getVertxInstanceFactory(), instanceFactory);
        Assert.assertEquals(((VertxEndpoint) endpoint).getEndpointConfiguration().getTimeout(), 5000L);

        endpoint = component.createEndpoint("eventbus:news-feed", context);

        Assert.assertEquals(endpoint.getClass(), VertxEndpoint.class);

        Assert.assertEquals(((VertxEndpoint)endpoint).getEndpointConfiguration().getAddress(), "news-feed");
        Assert.assertEquals(((VertxEndpoint)endpoint).getEndpointConfiguration().isPubSubDomain(), false);
        Assert.assertEquals(((VertxEndpoint) endpoint).getVertxInstanceFactory(), instanceFactory);
        Assert.assertEquals(((VertxEndpoint) endpoint).getEndpointConfiguration().getTimeout(), 5000L);

    }

    @Test
    public void testCreateEndpointWithParameters() throws Exception {
        VertxEndpointComponent component = new VertxEndpointComponent();

        reset(applicationContext);
        when(applicationContext.containsBean("vertxInstanceFactory")).thenReturn(true);
        when(applicationContext.getBean("vertxInstanceFactory", VertxInstanceFactory.class)).thenReturn(instanceFactory);
        Endpoint endpoint = component.createEndpoint("vertx:news-feed?port=10105&timeout=10000&pubSubDomain=true", context);

        Assert.assertEquals(endpoint.getClass(), VertxEndpoint.class);

        Assert.assertEquals(((VertxEndpoint)endpoint).getEndpointConfiguration().getAddress(), "news-feed");
        Assert.assertEquals(((VertxEndpoint)endpoint).getEndpointConfiguration().getPort(), 10105);
        Assert.assertEquals(((VertxEndpoint)endpoint).getEndpointConfiguration().isPubSubDomain(), true);
        Assert.assertEquals(((VertxEndpoint) endpoint).getVertxInstanceFactory(), instanceFactory);
        Assert.assertEquals(((VertxEndpoint) endpoint).getEndpointConfiguration().getTimeout(), 10000L);

    }

    @Test
    public void testCreateEndpointCustomInstanceFactory() throws Exception {
        VertxEndpointComponent component = new VertxEndpointComponent();

        reset(applicationContext);
        when(applicationContext.getBean("vertxFactory", VertxInstanceFactory.class)).thenReturn(instanceFactory);
        Endpoint endpoint = component.createEndpoint("vertx:news?vertxInstanceFactory=vertxFactory", context);

        Assert.assertEquals(endpoint.getClass(), VertxEndpoint.class);

        Assert.assertEquals(((VertxEndpoint)endpoint).getEndpointConfiguration().getAddress(), "news");
        Assert.assertEquals(((VertxEndpoint)endpoint).getEndpointConfiguration().isPubSubDomain(), false);
        Assert.assertEquals(((VertxEndpoint) endpoint).getVertxInstanceFactory(), instanceFactory);
        Assert.assertEquals(((VertxEndpoint) endpoint).getEndpointConfiguration().getTimeout(), 5000L);

    }

    @Test
    public void testInvalidEndpointUri() throws Exception {
        VertxEndpointComponent component = new VertxEndpointComponent();

        try {
            reset(applicationContext);
            component.createEndpoint("vertx:news?param1=&param2=value2", context);
            Assert.fail("Missing exception due to invalid endpoint uri");
        } catch (CitrusRuntimeException e) {
            Assert.assertTrue(e.getMessage().startsWith("Unable to find parameter"), e.getMessage());
        }

    }
}
