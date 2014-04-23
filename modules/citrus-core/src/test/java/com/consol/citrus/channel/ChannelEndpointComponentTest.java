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

package com.consol.citrus.channel;

import com.consol.citrus.endpoint.Endpoint;
import org.easymock.EasyMock;
import org.springframework.context.ApplicationContext;
import org.springframework.integration.support.channel.BeanFactoryChannelResolver;
import org.springframework.integration.support.channel.ChannelResolver;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.easymock.EasyMock.*;

/**
 * @author Christoph Deppisch
 */
public class ChannelEndpointComponentTest {

    private ApplicationContext applicationContext = EasyMock.createMock(ApplicationContext.class);
    private ChannelResolver channelResolver = EasyMock.createMock(ChannelResolver.class);

    @Test
    public void testCreateChannelEndpoint() throws Exception {
        ChannelEndpointComponent component = new ChannelEndpointComponent();
        component.setApplicationContext(applicationContext);

        reset(applicationContext);
        replay(applicationContext);

        Endpoint endpoint = component.createEndpoint("channel:channelName");

        Assert.assertEquals(endpoint.getClass(), ChannelEndpoint.class);

        Assert.assertEquals(((ChannelEndpoint)endpoint).getEndpointConfiguration().getChannelName(), "channelName");
        Assert.assertEquals(((ChannelEndpoint) endpoint).getEndpointConfiguration().getBeanFactory(), applicationContext);
        Assert.assertEquals(((ChannelEndpoint) endpoint).getEndpointConfiguration().getChannelResolver().getClass(), BeanFactoryChannelResolver.class);
        Assert.assertEquals(((ChannelEndpoint) endpoint).getEndpointConfiguration().getTimeout(), 5000L);

        verify(applicationContext);
    }

    @Test
    public void testCreateSyncChannelEndpoint() throws Exception {
        ChannelEndpointComponent component = new ChannelEndpointComponent();
        component.setApplicationContext(applicationContext);

        reset(applicationContext);
        replay(applicationContext);

        Endpoint endpoint = component.createEndpoint("channel:sync:channelName");

        Assert.assertEquals(endpoint.getClass(), ChannelSyncEndpoint.class);

        Assert.assertEquals(((ChannelSyncEndpoint)endpoint).getEndpointConfiguration().getChannelName(), "channelName");
        Assert.assertEquals(((ChannelSyncEndpoint) endpoint).getEndpointConfiguration().getBeanFactory(), applicationContext);
        Assert.assertEquals(((ChannelEndpoint) endpoint).getEndpointConfiguration().getChannelResolver().getClass(), BeanFactoryChannelResolver.class);

        verify(applicationContext);
    }

    @Test
    public void testCreateChannelEndpointWithParameters() throws Exception {
        ChannelEndpointComponent component = new ChannelEndpointComponent();
        component.setApplicationContext(applicationContext);

        reset(applicationContext);
        expect(applicationContext.containsBean("myChannelResolver")).andReturn(true).once();
        expect(applicationContext.getBean("myChannelResolver")).andReturn(channelResolver).once();
        replay(applicationContext);

        Endpoint endpoint = component.createEndpoint("channel:channelName?timeout=10000&channelResolver=myChannelResolver");

        Assert.assertEquals(endpoint.getClass(), ChannelEndpoint.class);

        Assert.assertEquals(((ChannelEndpoint)endpoint).getEndpointConfiguration().getChannelName(), "channelName");
        Assert.assertEquals(((ChannelEndpoint) endpoint).getEndpointConfiguration().getBeanFactory(), applicationContext);
        Assert.assertEquals(((ChannelEndpoint) endpoint).getEndpointConfiguration().getChannelResolver(), channelResolver);
        Assert.assertEquals(((ChannelEndpoint) endpoint).getEndpointConfiguration().getTimeout(), 10000L);

        verify(applicationContext);
    }
}
