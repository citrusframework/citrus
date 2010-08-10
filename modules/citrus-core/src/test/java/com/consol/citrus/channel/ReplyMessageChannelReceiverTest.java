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
