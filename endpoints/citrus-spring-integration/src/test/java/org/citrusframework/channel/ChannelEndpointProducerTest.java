/*
 * Copyright 2006-2010 the original author or authors.
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

import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.message.DefaultMessage;
import org.citrusframework.message.Message;
import org.citrusframework.testng.AbstractTestNGUnitTest;
import org.mockito.Mockito;
import org.springframework.integration.core.MessagingTemplate;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.messaging.core.DestinationResolver;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.mockito.Mockito.*;

/**
 * @author Christoph Deppisch
 */
public class ChannelEndpointProducerTest extends AbstractTestNGUnitTest {

    private MessagingTemplate messagingTemplate = Mockito.mock(MessagingTemplate.class);
    private MessageChannel channel = Mockito.mock(MessageChannel.class);
    private DestinationResolver channelResolver = Mockito.mock(DestinationResolver.class);
    
    @Test
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void testSendMessage() {
        ChannelEndpoint endpoint = new ChannelEndpoint();
        endpoint.getEndpointConfiguration().setMessagingTemplate(messagingTemplate);

        endpoint.getEndpointConfiguration().setChannel(channel);
        
        final Message message = new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>");
        
        reset(messagingTemplate, channel);

        endpoint.createProducer().send(message, context);

        verify(messagingTemplate).send(eq(channel), any(org.springframework.messaging.Message.class));
    }
    
    @Test
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void testSendMessageChannelNameResolver() {
        ChannelEndpoint endpoint = new ChannelEndpoint();
        endpoint.getEndpointConfiguration().setMessagingTemplate(messagingTemplate);

        endpoint.getEndpointConfiguration().setChannelName("testChannel");

        endpoint.getEndpointConfiguration().setChannelResolver(channelResolver);
        
        final Message message = new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>");
        
        reset(messagingTemplate, channel, channelResolver);
        
        when(channelResolver.resolveDestination("testChannel")).thenReturn(channel);

        endpoint.createProducer().send(message, context);

        verify(messagingTemplate).send(eq(channel), any(org.springframework.messaging.Message.class));
    }
    
    @Test
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void testSendMessageFailed() {
        ChannelEndpoint endpoint = new ChannelEndpoint();
        endpoint.getEndpointConfiguration().setMessagingTemplate(messagingTemplate);

        endpoint.getEndpointConfiguration().setChannel(channel);
        
        final Message message = new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>");
        
        reset(messagingTemplate, channel);

        doThrow(new MessageDeliveryException("Internal error!")).when(messagingTemplate).send(eq(channel), any(org.springframework.messaging.Message.class));

        try {
            endpoint.createProducer().send(message, context);
        } catch(CitrusRuntimeException e) {
            Assert.assertTrue(e.getLocalizedMessage().startsWith("Failed to send message to channel: "));
            Assert.assertNotNull(e.getCause());
            Assert.assertEquals(e.getCause().getClass(), MessageDeliveryException.class);
            Assert.assertEquals(e.getCause().getLocalizedMessage(), "Internal error!");
            return;
        }
        
        Assert.fail("Missing " + CitrusRuntimeException.class + " because no message was received");    
    }
    
}
