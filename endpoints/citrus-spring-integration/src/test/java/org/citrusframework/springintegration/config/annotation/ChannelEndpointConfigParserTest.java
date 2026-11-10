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
import org.citrusframework.springintegration.channel.ChannelEndpoint;
import org.citrusframework.springintegration.channel.ChannelMessageConverter;
import org.citrusframework.endpoint.direct.annotation.DirectEndpointConfigParser;
import org.citrusframework.endpoint.direct.annotation.DirectSyncEndpointConfigParser;
import org.citrusframework.endpoint.resolver.EndpointUriResolver;
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
public class ChannelEndpointConfigParserTest extends AbstractUnitTest {

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
        lenient().when(referenceResolver.resolve("testActor", TestActor.class)).thenReturn(testActor);
    }

    @Test
    public void testChannelEndpointParser() {
        CitrusAnnotations.injectEndpoints(this, context);

        // 1st message receiver
        Assertions.assertEquals(ChannelMessageConverter.class, channelEndpoint1.getEndpointConfiguration().getMessageConverter().getClass());
        Assertions.assertEquals("testChannel", channelEndpoint1.getEndpointConfiguration().getChannelName());
        Assertions.assertNull(channelEndpoint1.getEndpointConfiguration().getChannel());
        Assertions.assertEquals(5000L, channelEndpoint1.getEndpointConfiguration().getTimeout());
        Assertions.assertFalse(channelEndpoint1.getEndpointConfiguration().isUseObjectMessages());
        Assertions.assertTrue(channelEndpoint1.getEndpointConfiguration().isFilterInternalHeaders());

        // 2nd message receiver
        Assertions.assertEquals(messageConverter, channelEndpoint2.getEndpointConfiguration().getMessageConverter());
        Assertions.assertEquals(channelResolver, channelEndpoint2.getEndpointConfiguration().getChannelResolver());
        Assertions.assertNull(channelEndpoint2.getEndpointConfiguration().getChannelName());
        Assertions.assertNotNull(channelEndpoint2.getEndpointConfiguration().getChannel());
        Assertions.assertEquals(10000L, channelEndpoint2.getEndpointConfiguration().getTimeout());

        // 3rd message receiver
        Assertions.assertNull(channelEndpoint3.getEndpointConfiguration().getChannelName());
        Assertions.assertNull(channelEndpoint3.getEndpointConfiguration().getChannel());
        Assertions.assertTrue(channelEndpoint3.getEndpointConfiguration().isUseObjectMessages());
        Assertions.assertFalse(channelEndpoint3.getEndpointConfiguration().isFilterInternalHeaders());

        // 4th message receiver
        Assertions.assertNotNull(channelEndpoint4.getActor());
        Assertions.assertEquals(testActor, channelEndpoint4.getActor());
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
        Assertions.assertTrue(AnnotationConfigParser.lookup("channel.async").isPresent());
    }
}
