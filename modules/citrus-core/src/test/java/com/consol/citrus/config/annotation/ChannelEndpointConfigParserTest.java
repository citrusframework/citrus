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

package com.consol.citrus.config.annotation;

import com.consol.citrus.TestActor;
import com.consol.citrus.annotations.CitrusAnnotations;
import com.consol.citrus.annotations.CitrusEndpoint;
import com.consol.citrus.channel.ChannelEndpoint;
import com.consol.citrus.channel.ChannelMessageConverter;
import com.consol.citrus.context.SpringBeanReferenceResolver;
import com.consol.citrus.endpoint.resolver.EndpointUriResolver;
import com.consol.citrus.testng.AbstractTestNGUnitTest;
import org.mockito.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.integration.core.MessagingTemplate;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.core.DestinationResolver;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
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
            messagingTemplate="messagingTemplate")
    private ChannelEndpoint channelEndpoint3;

    @CitrusEndpoint
    @ChannelEndpointConfig(channelName="testChannel",
            actor="testActor")
    private ChannelEndpoint channelEndpoint4;

    @Autowired
    private SpringBeanReferenceResolver referenceResolver;

    @Mock
    private MessagingTemplate messagingTemplate = Mockito.mock(MessagingTemplate.class);
    @Mock
    private MessageChannel channelQueue = Mockito.mock(MessageChannel.class);
    @Mock
    private ChannelMessageConverter messageConverter = Mockito.mock(ChannelMessageConverter.class);
    @Mock
    private DestinationResolver channelResolver = Mockito.mock(DestinationResolver.class);
    @Mock
    private EndpointUriResolver channelNameResolver = Mockito.mock(EndpointUriResolver.class);
    @Mock
    private TestActor testActor = Mockito.mock(TestActor.class);
    @Mock
    private ApplicationContext applicationContext = Mockito.mock(ApplicationContext.class);

    @BeforeClass
    public void setup() {
        MockitoAnnotations.initMocks(this);

        referenceResolver.setApplicationContext(applicationContext);

        when(applicationContext.getBean("messagingTemplate", MessagingTemplate.class)).thenReturn(messagingTemplate);
        when(applicationContext.getBean("channelQueue", MessageChannel.class)).thenReturn(channelQueue);
        when(applicationContext.getBean("messageConverter", ChannelMessageConverter.class)).thenReturn(messageConverter);
        when(applicationContext.getBean("channelResolver", DestinationResolver.class)).thenReturn(channelResolver);
        when(applicationContext.getBean("channelNameResolver", EndpointUriResolver.class)).thenReturn(channelNameResolver);
        when(applicationContext.getBean("testActor", TestActor.class)).thenReturn(testActor);
    }

    @Test
    public void testChannelEndpointParser() {
        CitrusAnnotations.injectEndpoints(this, context);

        // 1st message receiver
        Assert.assertEquals(channelEndpoint1.getEndpointConfiguration().getMessageConverter().getClass(), ChannelMessageConverter.class);
        Assert.assertEquals(channelEndpoint1.getEndpointConfiguration().getChannelName(), "testChannel");
        Assert.assertNull(channelEndpoint1.getEndpointConfiguration().getChannel());
        Assert.assertEquals(channelEndpoint1.getEndpointConfiguration().getTimeout(), 5000L);
        Assert.assertEquals(channelEndpoint1.getEndpointConfiguration().isUseObjectMessages(), false);

        // 2nd message receiver
        Assert.assertEquals(channelEndpoint2.getEndpointConfiguration().getMessageConverter(), messageConverter);
        Assert.assertEquals(channelEndpoint2.getEndpointConfiguration().getChannelResolver(), channelResolver);
        Assert.assertNull(channelEndpoint2.getEndpointConfiguration().getChannelName());
        Assert.assertNotNull(channelEndpoint2.getEndpointConfiguration().getChannel());
        Assert.assertEquals(channelEndpoint2.getEndpointConfiguration().getTimeout(), 10000L);

        // 3rd message receiver
        Assert.assertNull(channelEndpoint3.getEndpointConfiguration().getChannelName());
        Assert.assertNull(channelEndpoint3.getEndpointConfiguration().getChannel());
        Assert.assertEquals(channelEndpoint3.getEndpointConfiguration().isUseObjectMessages(), true);

        // 4th message receiver
        Assert.assertNotNull(channelEndpoint4.getActor());
        Assert.assertEquals(channelEndpoint4.getActor(), testActor);
    }
}
