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

package org.citrusframework.springintegration.config.annotation;

import java.util.Map;

import org.citrusframework.TestActor;
import org.citrusframework.base.annotations.CitrusAnnotations;
import org.citrusframework.annotations.CitrusEndpoint;
import org.citrusframework.config.annotation.AnnotationConfigParser;
import org.citrusframework.springintegration.channel.ChannelMessageConverter;
import org.citrusframework.springintegration.channel.ChannelSyncEndpoint;
import org.citrusframework.endpoint.direct.annotation.DirectEndpointConfigParser;
import org.citrusframework.endpoint.direct.annotation.DirectSyncEndpointConfigParser;
import org.citrusframework.endpoint.resolver.EndpointUriResolver;
import org.citrusframework.message.DefaultMessageCorrelator;
import org.citrusframework.message.MessageCorrelator;
import org.citrusframework.spi.ReferenceResolver;
import org.citrusframework.junit.jupiter.AbstractUnitTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.integration.core.MessagingTemplate;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.core.DestinationResolver;

import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
public class ChannelSyncEndpointConfigParserTest extends AbstractUnitTest {

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

    @BeforeEach
    @Override
    public void prepareTest() {
        super.prepareTest();
        context.setReferenceResolver(referenceResolver);
        lenient().when(referenceResolver.resolve("messagingTemplate", MessagingTemplate.class)).thenReturn(messagingTemplate);
        lenient().when(referenceResolver.resolve("channelQueue", MessageChannel.class)).thenReturn(channelQueue);
        lenient().when(referenceResolver.resolve("messageConverter", ChannelMessageConverter.class)).thenReturn(messageConverter);
        lenient().when(referenceResolver.resolve("channelResolver", DestinationResolver.class)).thenReturn(channelResolver);
        lenient().when(referenceResolver.resolve("channelNameResolver", EndpointUriResolver.class)).thenReturn(channelNameResolver);
        lenient().when(referenceResolver.resolve("replyMessageCorrelator", MessageCorrelator.class)).thenReturn(messageCorrelator);
        lenient().when(referenceResolver.resolve("testActor", TestActor.class)).thenReturn(testActor);
    }

    @Test
    public void testChannelSyncEndpointAsConsumerParser() {
        CitrusAnnotations.injectEndpoints(this, context);

        // 1st message receiver
        Assertions.assertEquals("testChannel", channelSyncEndpoint1.getEndpointConfiguration().getChannelName());
        Assertions.assertNull(channelSyncEndpoint1.getEndpointConfiguration().getChannel());
        Assertions.assertEquals(5000L, channelSyncEndpoint1.getEndpointConfiguration().getTimeout());
        Assertions.assertEquals(DefaultMessageCorrelator.class, channelSyncEndpoint1.getEndpointConfiguration().getCorrelator().getClass());
        Assertions.assertTrue(channelSyncEndpoint1.getEndpointConfiguration().isFilterInternalHeaders());

        // 2nd message receiver
        Assertions.assertNull(channelSyncEndpoint2.getEndpointConfiguration().getChannelName());
        Assertions.assertNotNull(channelSyncEndpoint2.getEndpointConfiguration().getChannel());
        Assertions.assertEquals(10000L, channelSyncEndpoint2.getEndpointConfiguration().getTimeout());
        Assertions.assertEquals(messageCorrelator, channelSyncEndpoint2.getEndpointConfiguration().getCorrelator());

        // 3rd message receiver
        Assertions.assertNull(channelSyncEndpoint3.getEndpointConfiguration().getChannelName());
        Assertions.assertNull(channelSyncEndpoint3.getEndpointConfiguration().getChannel());
        Assertions.assertEquals(messageCorrelator, channelSyncEndpoint3.getEndpointConfiguration().getCorrelator());

        // 4th message receiver
        Assertions.assertNotNull(channelSyncEndpoint4.getActor());
        Assertions.assertEquals(testActor, channelSyncEndpoint4.getActor());

        // 5th message receiver
        Assertions.assertEquals("testChannel", channelSyncEndpoint5.getEndpointConfiguration().getChannelName());
        Assertions.assertNull(channelSyncEndpoint5.getEndpointConfiguration().getChannel());
        Assertions.assertEquals(5000L, channelSyncEndpoint5.getEndpointConfiguration().getTimeout());
        Assertions.assertEquals(500L, channelSyncEndpoint5.getEndpointConfiguration().getPollingInterval());
        Assertions.assertEquals(DefaultMessageCorrelator.class, channelSyncEndpoint5.getEndpointConfiguration().getCorrelator().getClass());

        // 6th message sender
        Assertions.assertNull(channelSyncEndpoint6.getEndpointConfiguration().getChannelName());
        Assertions.assertNotNull(channelSyncEndpoint6.getEndpointConfiguration().getChannel());
        Assertions.assertEquals(10000L, channelSyncEndpoint6.getEndpointConfiguration().getTimeout());
        Assertions.assertEquals(messageCorrelator, channelSyncEndpoint6.getEndpointConfiguration().getCorrelator());
        Assertions.assertEquals(channelResolver, channelSyncEndpoint6.getEndpointConfiguration().getChannelResolver());
        Assertions.assertFalse(channelSyncEndpoint6.getEndpointConfiguration().isFilterInternalHeaders());

        // 7th message sender
        Assertions.assertNull(channelSyncEndpoint7.getEndpointConfiguration().getChannelName());
        Assertions.assertNull(channelSyncEndpoint7.getEndpointConfiguration().getChannel());
        Assertions.assertEquals(messageCorrelator, channelSyncEndpoint7.getEndpointConfiguration().getCorrelator());

        // 8th message sender
        Assertions.assertNotNull(channelSyncEndpoint8.getEndpointConfiguration().getPollingInterval());
        Assertions.assertEquals(250L, channelSyncEndpoint8.getEndpointConfiguration().getPollingInterval());
        Assertions.assertNotNull(channelSyncEndpoint8.getActor());
        Assertions.assertEquals(testActor, channelSyncEndpoint8.getActor());
    }

    @Test
    public void testLookupAll() {
        Map<String, AnnotationConfigParser<?, ?>> validators = AnnotationConfigParser.lookup();
        Assertions.assertEquals(4L, validators.size());
        Assertions.assertNotNull(validators.get("direct.async"));
        Assertions.assertEquals(DirectEndpointConfigParser.class, validators.get("direct.async").getClass());
        Assertions.assertNotNull(validators.get("direct.sync"));
        Assertions.assertEquals(DirectSyncEndpointConfigParser.class, validators.get("direct.sync").getClass());
        Assertions.assertNotNull(validators.get("channel.async"));
        Assertions.assertEquals(ChannelEndpointConfigParser.class, validators.get("channel.async").getClass());
        Assertions.assertNotNull(validators.get("channel.sync"));
        Assertions.assertEquals(ChannelSyncEndpointConfigParser.class, validators.get("channel.sync").getClass());
    }

    @Test
    public void testLookupByQualifier() {
        Assertions.assertTrue(AnnotationConfigParser.lookup("channel.sync").isPresent());
    }
}
