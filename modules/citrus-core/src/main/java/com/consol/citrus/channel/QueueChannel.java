/*
 * Copyright 2006-2012 the original author or authors.
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

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.springframework.integration.Message;
import org.springframework.integration.core.MessageSelector;
import org.springframework.util.Assert;

/**
 * Added selective consumption of messages according to a message selector implementation.
 * 
 * @author Christoph Deppisch
 */
public class QueueChannel extends org.springframework.integration.channel.QueueChannel {
    /** Blocking in memory message store */
    private final BlockingQueue<Message<?>> queue;
    
    /**
     * Create a channel with the specified queue.
     */
    public QueueChannel(BlockingQueue<Message<?>> queue) {
        super(queue);
        
        this.queue = queue;
    }
    
    /**
     * Create a channel with the specified queue capacity.
     */
    public QueueChannel(int capacity) {
        this(new LinkedBlockingQueue<Message<?>>(capacity));
        
        Assert.isTrue(capacity > 0, "The capacity must be a positive integer. " +
                "For a zero-capacity alternative, consider using a 'RendezvousChannel'.");
    }
    
    /**
     * Default constructor.
     */
    public QueueChannel() {
        this(new LinkedBlockingQueue<Message<?>>());
    }
    
    /**
     * Supports selective consumption of messages on the channel. The first message 
     * to be accepted by given message selector is returned as result.
     * 
     * @param selector
     * @return
     */
    public Message<?> receiveSelected(MessageSelector selector) {
        Object[] array = this.queue.toArray();
        for (Object o : array) {
            Message<?> message = (Message<?>) o;
            if (selector.accept(message) && this.queue.remove(message)) {
                return message;
            }
        }
        
        return null;
    }
}
