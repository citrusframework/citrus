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

package com.consol.citrus.endpoint;

import com.consol.citrus.channel.ChannelEndpoint;
import com.consol.citrus.channel.ChannelEndpointComponent;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import org.mockito.Mockito;
import org.springframework.context.ApplicationContext;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.*;

import static org.mockito.Mockito.*;

/**
 * @author Christoph Deppisch
 */
public class DefaultEndpointFactoryTest {

    private ApplicationContext applicationContext = Mockito.mock(ApplicationContext.class);

    @Test
    public void testResolveDirectEndpoint() throws Exception {
        reset(applicationContext);
        when(applicationContext.getBean("myEndpoint", Endpoint.class)).thenReturn(Mockito.mock(Endpoint.class));
        TestContext context = new TestContext();
        context.setApplicationContext(applicationContext);

        DefaultEndpointFactory factory = new DefaultEndpointFactory();
        Endpoint endpoint = factory.create("myEndpoint", context);

        Assert.assertNotNull(endpoint);
    }

    @Test
    public void testResolveChannelEndpoint() throws Exception {
        reset(applicationContext);
        when(applicationContext.getBeansOfType(EndpointComponent.class)).thenReturn(Collections.<String, EndpointComponent>emptyMap());
        TestContext context = new TestContext();
        context.setApplicationContext(applicationContext);

        DefaultEndpointFactory factory = new DefaultEndpointFactory();
        Endpoint endpoint = factory.create("channel:channel.name", context);

        Assert.assertEquals(endpoint.getClass(), ChannelEndpoint.class);
        Assert.assertEquals(((ChannelEndpoint)endpoint).getEndpointConfiguration().getChannelName(), "channel.name");
    }

    @Test
    public void testResolveCustomEndpoint() throws Exception {
        Map<String, EndpointComponent> components = new HashMap<String, EndpointComponent>();
        components.put("custom", new ChannelEndpointComponent());

        reset(applicationContext);
        when(applicationContext.getBeansOfType(EndpointComponent.class)).thenReturn(components);
        TestContext context = new TestContext();
        context.setApplicationContext(applicationContext);

        DefaultEndpointFactory factory = new DefaultEndpointFactory();
        Endpoint endpoint = factory.create("custom:custom.channel", context);

        Assert.assertEquals(endpoint.getClass(), ChannelEndpoint.class);
        Assert.assertEquals(((ChannelEndpoint)endpoint).getEndpointConfiguration().getChannelName(), "custom.channel");
    }

    @Test
    public void testOverwriteEndpointComponent() throws Exception {
        Map<String, EndpointComponent> components = new HashMap<String, EndpointComponent>();
        components.put("jms", new ChannelEndpointComponent());

        reset(applicationContext);
        when(applicationContext.getBeansOfType(EndpointComponent.class)).thenReturn(components);
        TestContext context = new TestContext();
        context.setApplicationContext(applicationContext);

        DefaultEndpointFactory factory = new DefaultEndpointFactory();
        Endpoint endpoint = factory.create("jms:custom.channel", context);

        Assert.assertEquals(endpoint.getClass(), ChannelEndpoint.class);
        Assert.assertEquals(((ChannelEndpoint)endpoint).getEndpointConfiguration().getChannelName(), "custom.channel");
    }

    @Test
    public void testResolveUnknownEndpointComponent() throws Exception {
        reset(applicationContext);
        when(applicationContext.getBeansOfType(EndpointComponent.class)).thenReturn(Collections.<String, EndpointComponent>emptyMap());
        TestContext context = new TestContext();
        context.setApplicationContext(applicationContext);

        DefaultEndpointFactory factory = new DefaultEndpointFactory();
        try {
            factory.create("unknown:unknown", context);
            Assert.fail("Missing exception due to unknown endpoint component");
        } catch (CitrusRuntimeException e) {
            Assert.assertTrue(e.getMessage().startsWith("Unable to create endpoint component"));
        }
    }

    @Test
    public void testResolveInvalidEndpointUri() throws Exception {
        reset(applicationContext);
        TestContext context = new TestContext();
        context.setApplicationContext(applicationContext);

        DefaultEndpointFactory factory = new DefaultEndpointFactory();
        try {
            factory.create("jms:", context);
            Assert.fail("Missing exception due to invalid endpoint uri");
        } catch (CitrusRuntimeException e) {
            Assert.assertTrue(e.getMessage().startsWith("Invalid endpoint uri"));
        }
    }
}
