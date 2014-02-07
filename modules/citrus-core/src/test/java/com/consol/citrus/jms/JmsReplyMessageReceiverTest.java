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

import com.consol.citrus.message.DefaultReplyMessageCorrelator;
import com.consol.citrus.messaging.SelectiveConsumer;
import org.springframework.integration.Message;
import org.springframework.integration.support.MessageBuilder;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Christoph Deppisch
 */
public class JmsReplyMessageReceiverTest {

    private int retryCount = 0;
    
    @Test
    public void testOnReplyMessage() {
        JmsReplyMessageReceiver replyMessageReceiver = new JmsReplyMessageReceiver();
        
        Map<String, Object> headers = new HashMap<String, Object>();
        final Message<String> message = MessageBuilder.withPayload("<TestRequest><Message>Hello World!</Message></TestRequest>")
                                .copyHeaders(headers)
                                .build();
        
        replyMessageReceiver.onReplyMessage(message);
        
        Assert.assertEquals(replyMessageReceiver.receive(), message);
    }
    
    @Test
    public void testOnReplyMessageWithCorrelatorKey() {
        JmsReplyMessageReceiver replyMessageReceiver = new JmsReplyMessageReceiver();
        
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

        JmsReplyMessageReceiver replyMessageReceiver = new JmsReplyMessageReceiver(new JmsSyncEndpoint() {
            @Override
            public SelectiveConsumer createConsumer() {
                return new JmsSyncProducer(getEndpointConfiguration(), getMessageListener(), getName()) {
                    @Override
                    public Message<?> findReplyMessage(String correlationKey) {
                        retryCount++;
                        if (retryCount == 5) {
                            return message;
                        } else {
                            return null;
                        }
                    }
                };
            }
        });
        
        Assert.assertEquals(retryCount, 0);
        Assert.assertEquals(replyMessageReceiver.receive(2500), message);
        Assert.assertEquals(retryCount, 5);
    }
    
    @Test
    public void testReplyMessageRetriesExceeded() {
        retryCount = 0;

        JmsReplyMessageReceiver replyMessageReceiver = new JmsReplyMessageReceiver(new JmsSyncEndpoint() {
            @Override
            public SelectiveConsumer createConsumer() {
                return new JmsSyncProducer(getEndpointConfiguration(), getMessageListener(), getName()) {
                    @Override
                    public Message<?> findReplyMessage(String correlationKey) {
                        retryCount++;
                        return null;
                    }
                };
            }
        });
        
        replyMessageReceiver.setPollingInterval(300L);
        
        Assert.assertEquals(retryCount, 0);
        Assert.assertNull(replyMessageReceiver.receive(800));
        Assert.assertEquals(retryCount, 4);
    }
    
    @Test
    public void testIntervalGreaterThanTimeout() {
        retryCount = 0;

        JmsReplyMessageReceiver replyMessageReceiver = new JmsReplyMessageReceiver(new JmsSyncEndpoint() {
            @Override
            public SelectiveConsumer createConsumer() {
                return new JmsSyncProducer(getEndpointConfiguration(), getMessageListener(), getName()) {
                    @Override
                    public Message<?> findReplyMessage(String correlationKey) {
                        retryCount++;
                        return null;
                    }
                };
            }
        });
        
        replyMessageReceiver.setPollingInterval(1000L);
        
        Assert.assertEquals(retryCount, 0);
        Assert.assertNull(replyMessageReceiver.receive(250));
        Assert.assertEquals(retryCount, 2);
    }
    
    @Test
    public void testZeroTimeout() {
        retryCount = 0;

        JmsReplyMessageReceiver replyMessageReceiver = new JmsReplyMessageReceiver(new JmsSyncEndpoint() {
            @Override
            public SelectiveConsumer createConsumer() {
                return new JmsSyncProducer(getEndpointConfiguration(), getMessageListener(), getName()) {
                    @Override
                    public Message<?> findReplyMessage(String correlationKey) {
                        retryCount++;
                        return null;
                    }
                };
            }
        });
        
        replyMessageReceiver.setPollingInterval(1000L);
        
        Assert.assertEquals(retryCount, 0);
        Assert.assertNull(replyMessageReceiver.receive(0));
        Assert.assertEquals(retryCount, 1);
    }
}
