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
import org.citrusframework.channel.ChannelMessageConverter;
import org.citrusframework.channel.ChannelSyncEndpoint;
import org.citrusframework.endpoint.direct.annotation.DirectEndpointConfigParser;
import org.citrusframework.endpoint.direct.annotation.DirectSyncEndpointConfigParser;
import org.citrusframework.endpoint.resolver.EndpointUriResolver;
import org.citrusframework.message.DefaultMessageCorrelator;
import org.citrusframework.message.MessageCorrelator;
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
public class ChannelSyncEndpointConfigParserTest extends AbstractTestNGUnitTest {

    @CitrusEndpoint
    @ChannelSyncEndpointConfig(channelName="testChannel")
    private ChannelSyncEndpoint channelSyncEndpoint1;

    @CitrusEndpoint
    @ChannelSyncEndpointConfig(timeout=10000L,
            channel="channelQueue",
            correlator="replyMessageCorrelator")
    private ChannelSyncEndpoint channelSyncEndpoint2;

    @CitrusEndpoint
    @ChannelSyncEndpointConfig(messagingTemplate="messagingTemplate",
            correlator="replyMessageCorrelator")
    private ChannelSyncEndpoint channelSyncEndpoint3;

    @CitrusEndpoint
    @ChannelSyncEndpointConfig(channelName="testChannel",
            actor="testActor")
    private ChannelSyncEndpoint channelSyncEndpoint4;

    @CitrusEndpoint
    @ChannelSyncEndpointConfig(channelName="testChannel")
    private ChannelSyncEndpoint channelSyncEndpoint5;

    @CitrusEndpoint
    @ChannelSyncEndpointConfig(timeout=10000L,
            channel="channelQueue",
            channelResolver="channelResolver",
            filterInternalHeaders = false,
            correlator="replyMessageCorrelator")
    private ChannelSyncEndpoint channelSyncEndpoint6;

    @CitrusEndpoint
    @ChannelSyncEndpointConfig(messagingTemplate="messagingTemplate",
            correlator="replyMessageCorrelator")
    private ChannelSyncEndpoint channelSyncEndpoint7;

    @CitrusEndpoint
    @ChannelSyncEndpointConfig(channelName="testChannel",
            pollingInterval=250,
            actor="testActor")
    private ChannelSyncEndpoint channelSyncEndpoint8;

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
    private MessageCorrelator messageCorrelator;
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
        when(referenceResolver.resolve("replyMessageCorrelator", MessageCorrelator.class)).thenReturn(messageCorrelator);
        when(referenceResolver.resolve("testActor", TestActor.class)).thenReturn(testActor);
    }

    @BeforeMethod
    public void setMocks() {
        context.setReferenceResolver(referenceResolver);
    }

    @Test
    public void testChannelSyncEndpointAsConsumerParser() {
        CitrusAnnotations.injectEndpoints(this, context);

        // 1st message receiver
        Assert.assertEquals(channelSyncEndpoint1.getEndpointConfiguration().getChannelName(), "testChannel");
        Assert.assertNull(channelSyncEndpoint1.getEndpointConfiguration().getChannel());
        Assert.assertEquals(channelSyncEndpoint1.getEndpointConfiguration().getTimeout(), 5000L);
        Assert.assertEquals(channelSyncEndpoint1.getEndpointConfiguration().getCorrelator().getClass(), DefaultMessageCorrelator.class);
        Assert.assertTrue(channelSyncEndpoint1.getEndpointConfiguration().isFilterInternalHeaders());

        // 2nd message receiver
        Assert.assertNull(channelSyncEndpoint2.getEndpointConfiguration().getChannelName());
        Assert.assertNotNull(channelSyncEndpoint2.getEndpointConfiguration().getChannel());
        Assert.assertEquals(channelSyncEndpoint2.getEndpointConfiguration().getTimeout(), 10000L);
        Assert.assertEquals(channelSyncEndpoint2.getEndpointConfiguration().getCorrelator(), messageCorrelator);

        // 3rd message receiver
        Assert.assertNull(channelSyncEndpoint3.getEndpointConfiguration().getChannelName());
        Assert.assertNull(channelSyncEndpoint3.getEndpointConfiguration().getChannel());
        Assert.assertEquals(channelSyncEndpoint3.getEndpointConfiguration().getCorrelator(), messageCorrelator);

        // 4th message receiver
        Assert.assertNotNull(channelSyncEndpoint4.getActor());
        Assert.assertEquals(channelSyncEndpoint4.getActor(), testActor);

        // 5th message receiver
        Assert.assertEquals(channelSyncEndpoint5.getEndpointConfiguration().getChannelName(), "testChannel");
        Assert.assertNull(channelSyncEndpoint5.getEndpointConfiguration().getChannel());
        Assert.assertEquals(channelSyncEndpoint5.getEndpointConfiguration().getTimeout(), 5000L);
        Assert.assertEquals(channelSyncEndpoint5.getEndpointConfiguration().getPollingInterval(), 500L);
        Assert.assertEquals(channelSyncEndpoint5.getEndpointConfiguration().getCorrelator().getClass(), DefaultMessageCorrelator.class);

        // 6th message sender
        Assert.assertNull(channelSyncEndpoint6.getEndpointConfiguration().getChannelName());
        Assert.assertNotNull(channelSyncEndpoint6.getEndpointConfiguration().getChannel());
        Assert.assertEquals(channelSyncEndpoint6.getEndpointConfiguration().getTimeout(), 10000L);
        Assert.assertEquals(channelSyncEndpoint6.getEndpointConfiguration().getCorrelator(), messageCorrelator);
        Assert.assertEquals(channelSyncEndpoint6.getEndpointConfiguration().getChannelResolver(), channelResolver);
        Assert.assertFalse(channelSyncEndpoint6.getEndpointConfiguration().isFilterInternalHeaders());

        // 7th message sender
        Assert.assertNull(channelSyncEndpoint7.getEndpointConfiguration().getChannelName());
        Assert.assertNull(channelSyncEndpoint7.getEndpointConfiguration().getChannel());
        Assert.assertEquals(channelSyncEndpoint7.getEndpointConfiguration().getCorrelator(), messageCorrelator);

        // 8th message sender
        Assert.assertNotNull(channelSyncEndpoint8.getEndpointConfiguration().getPollingInterval());
        Assert.assertEquals(channelSyncEndpoint8.getEndpointConfiguration().getPollingInterval(), 250L);
        Assert.assertNotNull(channelSyncEndpoint8.getActor());
        Assert.assertEquals(channelSyncEndpoint8.getActor(), testActor);
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
        Assert.assertTrue(AnnotationConfigParser.lookup("channel.sync").isPresent());
    }
}
