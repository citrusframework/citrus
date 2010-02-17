/*
 * Copyright 2006-2010 ConSol* Software GmbH.
 * 
 * This file is part of Citrus.
 * 
 * Citrus is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Citrus is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Citrus. If not, see <http://www.gnu.org/licenses/>.
 */

package com.consol.citrus.channel;

import static org.easymock.classextension.EasyMock.*;

import java.util.HashMap;
import java.util.Map;

import org.easymock.classextension.EasyMock;
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
            Assert.assertEquals(e.getLocalizedMessage(), "Failed to send message to channel 'testChannel'");
            verify(messageChannelTemplate, channel);
            return;
        }
        
        Assert.fail("Missing " + CitrusRuntimeException.class + " because no message was received");    
    }
}
