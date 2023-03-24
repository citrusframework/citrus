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

package org.citrusframework.channel;

import java.util.Map;

import org.citrusframework.context.TestContext;
import org.citrusframework.endpoint.Endpoint;
import org.citrusframework.endpoint.EndpointComponent;
import org.citrusframework.endpoint.direct.DirectEndpointComponent;
import org.citrusframework.spi.ReferenceResolver;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.messaging.core.DestinationResolver;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;


/**
 * @author Christoph Deppisch
 */
public class ChannelEndpointComponentTest {

    @Mock
    private ReferenceResolver referenceResolver;
    @Mock
    private DestinationResolver<?> channelResolver;

    private TestContext context = new TestContext();

    @BeforeClass
    public void setup() {
        MockitoAnnotations.openMocks(this);
        context.setReferenceResolver(referenceResolver);
    }

    @Test
    public void testCreateChannelEndpoint() throws Exception {
        ChannelEndpointComponent component = new ChannelEndpointComponent();

        reset(referenceResolver);
        Endpoint endpoint = component.createEndpoint("channel:channelName", context);

        Assert.assertEquals(endpoint.getClass(), ChannelEndpoint.class);

        Assert.assertEquals(((ChannelEndpoint)endpoint).getEndpointConfiguration().getChannelName(), "channelName");
        Assert.assertNull(((ChannelEndpoint) endpoint).getEndpointConfiguration().getBeanFactory());
        Assert.assertNotNull(((ChannelEndpoint) endpoint).getEndpointConfiguration().getChannelResolver());
        Assert.assertEquals(((ChannelEndpoint) endpoint).getEndpointConfiguration().getTimeout(), 5000L);

    }

    @Test
    public void testCreateSyncChannelEndpoint() throws Exception {
        ChannelEndpointComponent component = new ChannelEndpointComponent();

        reset(referenceResolver);
        Endpoint endpoint = component.createEndpoint("channel:sync:channelName", context);

        Assert.assertEquals(endpoint.getClass(), ChannelSyncEndpoint.class);

        Assert.assertEquals(((ChannelSyncEndpoint)endpoint).getEndpointConfiguration().getChannelName(), "channelName");
        Assert.assertNull(((ChannelEndpoint) endpoint).getEndpointConfiguration().getBeanFactory());
        Assert.assertNotNull(((ChannelEndpoint) endpoint).getEndpointConfiguration().getChannelResolver());

    }

    @Test
    public void testCreateChannelEndpointWithParameters() throws Exception {
        ChannelEndpointComponent component = new ChannelEndpointComponent();

        reset(referenceResolver);
        when(referenceResolver.isResolvable("myChannelResolver")).thenReturn(true);
        when(referenceResolver.resolve("myChannelResolver", DestinationResolver.class)).thenReturn(channelResolver);
        Endpoint endpoint = component.createEndpoint("channel:channelName?timeout=10000&channelResolver=myChannelResolver", context);

        Assert.assertEquals(endpoint.getClass(), ChannelEndpoint.class);

        Assert.assertEquals(((ChannelEndpoint)endpoint).getEndpointConfiguration().getChannelName(), "channelName");
        Assert.assertNull(((ChannelEndpoint) endpoint).getEndpointConfiguration().getBeanFactory());
        Assert.assertEquals(((ChannelEndpoint) endpoint).getEndpointConfiguration().getChannelResolver(), channelResolver);
        Assert.assertEquals(((ChannelEndpoint) endpoint).getEndpointConfiguration().getTimeout(), 10000L);

    }

    @Test
    public void testLookupAll() {
        Map<String, EndpointComponent> validators = EndpointComponent.lookup();
        Assert.assertEquals(validators.size(), 2L);
        Assert.assertNotNull(validators.get("direct"));
        Assert.assertEquals(validators.get("direct").getClass(), DirectEndpointComponent.class);
        Assert.assertNotNull(validators.get("channel"));
        Assert.assertEquals(validators.get("channel").getClass(), ChannelEndpointComponent.class);
    }

    @Test
    public void testLookupByQualifier() {
        Assert.assertTrue(EndpointComponent.lookup("channel").isPresent());
    }
}
