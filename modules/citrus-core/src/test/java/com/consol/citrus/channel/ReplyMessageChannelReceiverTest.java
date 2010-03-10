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

import java.util.HashMap;
import java.util.Map;

import org.springframework.integration.core.Message;
import org.springframework.integration.message.MessageBuilder;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.consol.citrus.message.DefaultReplyMessageCorrelator;

/**
 * @author Christoph Deppisch
 */
public class ReplyMessageChannelReceiverTest {

    @Test
    public void testOnReplyMessage() {
        ReplyMessageChannelReceiver replyMessageReceiver = new ReplyMessageChannelReceiver();
        
        Map<String, Object> headers = new HashMap<String, Object>();
        final Message<String> message = MessageBuilder.withPayload("<TestRequest><Message>Hello World!</Message></TestRequest>")
                                .copyHeaders(headers)
                                .build();
        
        replyMessageReceiver.onReplyMessage(message);
        
        Assert.assertEquals(replyMessageReceiver.receive(), message);
    }
    
    @Test
    public void testOnReplyMessageWithCorrelatorKey() {
        ReplyMessageChannelReceiver replyMessageReceiver = new ReplyMessageChannelReceiver();
        
        Map<String, Object> headers = new HashMap<String, Object>();
        final Message<String> message = MessageBuilder.withPayload("<TestRequest><Message>Hello World!</Message></TestRequest>")
                                .copyHeaders(headers)
                                .build();
        
        replyMessageReceiver.onReplyMessage(message, new DefaultReplyMessageCorrelator().getCorrelationKey(message));
        
        Assert.assertEquals(replyMessageReceiver.receiveSelected(new DefaultReplyMessageCorrelator().getCorrelationKey(message)), message);
    }
}
