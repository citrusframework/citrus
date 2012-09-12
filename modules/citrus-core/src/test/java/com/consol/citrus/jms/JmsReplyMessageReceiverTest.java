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

package com.consol.citrus.jms;

import java.util.HashMap;
import java.util.Map;

import com.consol.citrus.message.ReplyMessageReceiver;
import org.springframework.integration.Message;
import org.springframework.integration.support.MessageBuilder;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.consol.citrus.message.DefaultReplyMessageCorrelator;

/**
 * @author Christoph Deppisch
 */
public class JmsReplyMessageReceiverTest {

    private int retryCount = 0;
    
    @Test
    public void testOnReplyMessage() {
        ReplyMessageReceiver replyMessageReceiver = new ReplyMessageReceiver();
        
        Map<String, Object> headers = new HashMap<String, Object>();
        final Message<String> message = MessageBuilder.withPayload("<TestRequest><Message>Hello World!</Message></TestRequest>")
                                .copyHeaders(headers)
                                .build();
        
        replyMessageReceiver.onReplyMessage(message);
        
        Assert.assertEquals(replyMessageReceiver.receive(), message);
    }
    
    @Test
    public void testOnReplyMessageWithCorrelatorKey() {
        ReplyMessageReceiver replyMessageReceiver = new ReplyMessageReceiver();
        
        Map<String, Object> headers = new HashMap<String, Object>();
        final Message<String> message = MessageBuilder.withPayload("<TestRequest><Message>Hello World!</Message></TestRequest>")
                                .copyHeaders(headers)
                                .build();
        
        replyMessageReceiver.onReplyMessage(message, new DefaultReplyMessageCorrelator().getCorrelationKey(message));
        
        Assert.assertEquals(replyMessageReceiver.receiveSelected(new DefaultReplyMessageCorrelator().getCorrelationKey(message)), message);
    }
    
    @Test
    public void testReplyMessageRetries() {
        retryCount = 0;
        
        final Message<String> message = MessageBuilder.withPayload("<TestRequest><Message>Hello World!</Message></TestRequest>")
                .build();
        
        ReplyMessageReceiver replyMessageReceiver = new ReplyMessageReceiver() {
            @Override
            public Message<?> receiveSelected(String selector) {
                retryCount++;
                if (retryCount == 5) {
                    return message;
                } else {
                    return null;
                }
            }
        };
        
        Assert.assertEquals(retryCount, 0);
        Assert.assertEquals(replyMessageReceiver.receive(3000), message);
        Assert.assertEquals(retryCount, 5);
    }
    
    @Test
    public void testReplyMessageRetriesExceeded() {
        retryCount = 0;
        
        ReplyMessageReceiver replyMessageReceiver = new ReplyMessageReceiver() {
            @Override
            public Message<?> receiveSelected(String selector) {
                retryCount++;
                return null;
            }
        };
        
        replyMessageReceiver.setMaxRetries(3);
        
        Assert.assertEquals(retryCount, 0);
        Assert.assertNull(replyMessageReceiver.receive(1000));
        Assert.assertEquals(retryCount, 3);
    }
}
