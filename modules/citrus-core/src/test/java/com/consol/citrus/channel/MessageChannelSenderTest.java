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

package com.consol.citrus.channel;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.reset;
import static org.easymock.EasyMock.verify;

import java.util.HashMap;
import java.util.Map;

import org.easymock.EasyMock;
import org.springframework.integration.channel.MessageChannelTemplate;
import org.springframework.integration.core.Message;
import org.springframework.integration.core.MessageChannel;
import org.springframework.integration.message.MessageBuilder;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.consol.citrus.exceptions.CitrusRuntimeException;

/**
 * @author Christoph Deppisch
 */
public class MessageChannelSenderTest {

    private MessageChannelTemplate messageChannelTemplate = EasyMock.createMock(MessageChannelTemplate.class);
    
    private MessageChannel channel = org.easymock.EasyMock.createMock(MessageChannel.class);
    
    @Test
    @SuppressWarnings("unchecked")
    public void testSendMessage() {
        MessageChannelSender messageChannelSender = new MessageChannelSender();
        messageChannelSender.setMessageChannelTemplate(messageChannelTemplate);
        
        messageChannelSender.setChannel(channel);
        
        Map<String, Object> headers = new HashMap<String, Object>();
        final Message message = MessageBuilder.withPayload("<TestRequest><Message>Hello World!</Message></TestRequest>")
                                .copyHeaders(headers)
                                .build();
        
        reset(messageChannelTemplate, channel);
        
        expect(channel.getName()).andReturn("testChannel").anyTimes();
        
        expect(messageChannelTemplate.send(message, channel)).andReturn(true).once();
        
        replay(messageChannelTemplate, channel);
        
        messageChannelSender.send(message);
        
        verify(messageChannelTemplate, channel);
    }
    
    @Test
    @SuppressWarnings("unchecked")
    public void testSendMessageFailed() {
        MessageChannelSender messageChannelSender = new MessageChannelSender();
        messageChannelSender.setMessageChannelTemplate(messageChannelTemplate);
        
        messageChannelSender.setChannel(channel);
        
        Map<String, Object> headers = new HashMap<String, Object>();
        final Message message = MessageBuilder.withPayload("<TestRequest><Message>Hello World!</Message></TestRequest>")
                                .copyHeaders(headers)
                                .build();
        
        reset(messageChannelTemplate, channel);
        
        expect(channel.getName()).andReturn("testChannel").anyTimes();
        
        expect(messageChannelTemplate.send(message, channel)).andReturn(false).once();
        
        replay(messageChannelTemplate, channel);

        try {
            messageChannelSender.send(message);
        } catch(CitrusRuntimeException e) {
            Assert.assertEquals(e.getLocalizedMessage(), "Failed to send message to channel: 'testChannel'");
            verify(messageChannelTemplate, channel);
            return;
        }
        
        Assert.fail("Missing " + CitrusRuntimeException.class + " because no message was received");    
    }
    
}
