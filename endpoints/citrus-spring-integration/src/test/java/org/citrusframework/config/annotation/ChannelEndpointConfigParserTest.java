/*
 * Copyright 2006-2018 the original author or authors.
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

package org.citrusframework.config.annotation;

import java.util.Map;

import org.citrusframework.TestActor;
import org.citrusframework.annotations.CitrusAnnotations;
import org.citrusframework.annotations.CitrusEndpoint;
import org.citrusframework.channel.ChannelEndpoint;
import org.citrusframework.channel.ChannelMessageConverter;
import org.citrusframework.endpoint.direct.annotation.DirectEndpointConfigParser;
import org.citrusframework.endpoint.direct.annotation.DirectSyncEndpointConfigParser;
import org.citrusframework.endpoint.resolver.EndpointUriResolver;
import org.citrusframework.spi.ReferenceResolver;
import org.citrusframework.testng.AbstractTestNGUnitTest;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.integration.core.MessagingTemplate;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.core.DestinationResolver;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.Mockito.when;

/**
 * @author Christoph Deppisch
 */
public class ChannelEndpointConfigParserTest extends AbstractTestNGUnitTest {

    @CitrusEndpoint
    @ChannelEndpointConfig(channelName="testChannel")
    private ChannelEndpoint channelEndpoint1;

    @CitrusEndpoint
    @ChannelEndpointConfig(timeout=10000L,
            messageConverter="messageConverter",
            channelResolver="channelResolver",
            channel="channelQueue")
    private ChannelEndpoint channelEndpoint2;

    @CitrusEndpoint
    @ChannelEndpointConfig(useObjectMessages=true,
            filterInternalHeaders=false,
            messagingTemplate="messagingTemplate")
    private ChannelEndpoint channelEndpoint3;

    @CitrusEndpoint
    @ChannelEndpointConfig(channelName="testChannel",
            actor="testActor")
    private ChannelEndpoint channelEndpoint4;

    @Mock
    private ReferenceResolver referenceResolver;
    @Mock
    private MessagingTemplate messagingTemplate;
    @Mock
    private MessageChannel channelQueue;
    @Mock
    private ChannelMessageConverter messageConverter;
    @Mock
    private DestinationResolver<?> channelResolver;
    @Mock
    private EndpointUriResolver channelNameResolver;
    @Mock
    private TestActor testActor;

    @BeforeClass
    public void setup() {
        MockitoAnnotations.openMocks(this);

        when(referenceResolver.resolve("messagingTemplate", MessagingTemplate.class)).thenReturn(messagingTemplate);
        when(referenceResolver.resolve("channelQueue", MessageChannel.class)).thenReturn(channelQueue);
        when(referenceResolver.resolve("messageConverter", ChannelMessageConverter.class)).thenReturn(messageConverter);
        when(referenceResolver.resolve("channelResolver", DestinationResolver.class)).thenReturn(channelResolver);
        when(referenceResolver.resolve("channelNameResolver", EndpointUriResolver.class)).thenReturn(channelNameResolver);
        when(referenceResolver.resolve("testActor", TestActor.class)).thenReturn(testActor);
    }

    @BeforeMethod
    public void setMocks() {
        context.setReferenceResolver(referenceResolver);
    }

    @Test
    public void testChannelEndpointParser() {
        CitrusAnnotations.injectEndpoints(this, context);

        // 1st message receiver
        Assert.assertEquals(channelEndpoint1.getEndpointConfiguration().getMessageConverter().getClass(), ChannelMessageConverter.class);
        Assert.assertEquals(channelEndpoint1.getEndpointConfiguration().getChannelName(), "testChannel");
        Assert.assertNull(channelEndpoint1.getEndpointConfiguration().getChannel());
        Assert.assertEquals(channelEndpoint1.getEndpointConfiguration().getTimeout(), 5000L);
        Assert.assertFalse(channelEndpoint1.getEndpointConfiguration().isUseObjectMessages());
        Assert.assertTrue(channelEndpoint1.getEndpointConfiguration().isFilterInternalHeaders());

        // 2nd message receiver
        Assert.assertEquals(channelEndpoint2.getEndpointConfiguration().getMessageConverter(), messageConverter);
        Assert.assertEquals(channelEndpoint2.getEndpointConfiguration().getChannelResolver(), channelResolver);
        Assert.assertNull(channelEndpoint2.getEndpointConfiguration().getChannelName());
        Assert.assertNotNull(channelEndpoint2.getEndpointConfiguration().getChannel());
        Assert.assertEquals(channelEndpoint2.getEndpointConfiguration().getTimeout(), 10000L);

        // 3rd message receiver
        Assert.assertNull(channelEndpoint3.getEndpointConfiguration().getChannelName());
        Assert.assertNull(channelEndpoint3.getEndpointConfiguration().getChannel());
        Assert.assertTrue(channelEndpoint3.getEndpointConfiguration().isUseObjectMessages());
        Assert.assertFalse(channelEndpoint3.getEndpointConfiguration().isFilterInternalHeaders());

        // 4th message receiver
        Assert.assertNotNull(channelEndpoint4.getActor());
        Assert.assertEquals(channelEndpoint4.getActor(), testActor);
    }

    @Test
    public void testLookupAll() {
        Map<String, AnnotationConfigParser> validators = AnnotationConfigParser.lookup();
        Assert.assertEquals(validators.size(), 4L);
        Assert.assertNotNull(validators.get("direct.async"));
        Assert.assertEquals(validators.get("direct.async").getClass(), DirectEndpointConfigParser.class);
        Assert.assertNotNull(validators.get("direct.sync"));
        Assert.assertEquals(validators.get("direct.sync").getClass(), DirectSyncEndpointConfigParser.class);
        Assert.assertNotNull(validators.get("channel.async"));
        Assert.assertEquals(validators.get("channel.async").getClass(), ChannelEndpointConfigParser.class);
        Assert.assertNotNull(validators.get("channel.sync"));
        Assert.assertEquals(validators.get("channel.sync").getClass(), ChannelSyncEndpointConfigParser.class);
    }

    @Test
    public void testLookupByQualifier() {
        Assert.assertTrue(AnnotationConfigParser.lookup("channel.async").isPresent());
    }
}
