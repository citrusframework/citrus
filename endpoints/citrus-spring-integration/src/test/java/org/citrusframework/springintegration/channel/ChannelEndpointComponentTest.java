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

package org.citrusframework.springintegration.channel;

import java.util.Map;

import org.citrusframework.context.TestContext;
import org.citrusframework.endpoint.Endpoint;
import org.citrusframework.endpoint.EndpointComponent;
import org.citrusframework.endpoint.direct.DirectEndpointComponent;
import org.citrusframework.endpoint.context.MessageStoreEndpointComponent;
import org.citrusframework.spi.ReferenceResolver;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.core.DestinationResolver;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ChannelEndpointComponentTest {

    @Mock
    private ReferenceResolver referenceResolver;
    @Mock
    private DestinationResolver<?> channelResolver;

    private TestContext context = new TestContext();

    @BeforeEach
    public void setup() {
        context.setReferenceResolver(referenceResolver);
    }

    @Test
    public void testCreateChannelEndpoint() {
        ChannelEndpointComponent component = new ChannelEndpointComponent();

        reset(referenceResolver);
        Endpoint endpoint = component.createEndpoint("channel:channelName", context);

        Assertions.assertEquals(ChannelEndpoint.class, endpoint.getClass());

        Assertions.assertEquals("channelName", ((ChannelEndpoint)endpoint).getEndpointConfiguration().getChannelName());
        Assertions.assertNull(((ChannelEndpoint) endpoint).getEndpointConfiguration().getBeanFactory());
        Assertions.assertNotNull(((ChannelEndpoint) endpoint).getEndpointConfiguration().getChannelResolver());
        Assertions.assertEquals(5000L, ((ChannelEndpoint) endpoint).getEndpointConfiguration().getTimeout());

    }

    @Test
    public void testCreateSyncChannelEndpoint() {
        ChannelEndpointComponent component = new ChannelEndpointComponent();

        reset(referenceResolver);
        Endpoint endpoint = component.createEndpoint("channel:sync:channelName", context);

        Assertions.assertEquals(ChannelSyncEndpoint.class, endpoint.getClass());

        Assertions.assertEquals("channelName", ((ChannelSyncEndpoint)endpoint).getEndpointConfiguration().getChannelName());
        Assertions.assertNull(((ChannelEndpoint) endpoint).getEndpointConfiguration().getBeanFactory());
        Assertions.assertNotNull(((ChannelEndpoint) endpoint).getEndpointConfiguration().getChannelResolver());

    }

    @Test
    public void testCreateChannelEndpointWithParameters() {
        ChannelEndpointComponent component = new ChannelEndpointComponent();

        reset(referenceResolver);
        when(referenceResolver.isResolvable("myChannelResolver")).thenReturn(true);
        when(referenceResolver.resolve("myChannelResolver", DestinationResolver.class)).thenReturn(channelResolver);
        Endpoint endpoint = component.createEndpoint("channel:channelName?timeout=10000&channelResolver=myChannelResolver", context);

        Assertions.assertEquals(ChannelEndpoint.class, endpoint.getClass());

        Assertions.assertEquals("channelName", ((ChannelEndpoint)endpoint).getEndpointConfiguration().getChannelName());
        Assertions.assertNull(((ChannelEndpoint) endpoint).getEndpointConfiguration().getBeanFactory());
        Assertions.assertEquals(channelResolver, ((ChannelEndpoint) endpoint).getEndpointConfiguration().getChannelResolver());
        Assertions.assertEquals(10000L, ((ChannelEndpoint) endpoint).getEndpointConfiguration().getTimeout());

    }

    @Test
    public void testLookupAll() {
        Map<String, EndpointComponent> validators = EndpointComponent.lookup();
        Assertions.assertEquals(3L, validators.size());
        Assertions.assertNotNull(validators.get("direct"));
        Assertions.assertEquals(DirectEndpointComponent.class, validators.get("direct").getClass());
        Assertions.assertNotNull(validators.get("message-store"));
        Assertions.assertEquals(MessageStoreEndpointComponent.class, validators.get("message-store").getClass());
        Assertions.assertNotNull(validators.get("channel"));
        Assertions.assertEquals(ChannelEndpointComponent.class, validators.get("channel").getClass());
    }

    @Test
    public void testLookupByQualifier() {
        Assertions.assertTrue(EndpointComponent.lookup("channel").isPresent());
    }
}
