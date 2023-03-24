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
package org.citrusframework.channel;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.integration.channel.QueueChannel;
import org.springframework.integration.core.MessageSelector;
import org.springframework.messaging.Message;
import org.springframework.util.Assert;

/**
 * Added selective consumption of messages according to a message selector implementation.
 *
 * @author Christoph Deppisch
 */
public class MessageSelectingQueueChannel extends QueueChannel {
    /** Logger */
    private static final Logger RETRY_LOG = LoggerFactory.getLogger("org.citrusframework.RetryLogger");

    /** Blocking in memory message store */
    private final BlockingQueue<Message<?>> queue;

    /** Polling interval when waiting for synchronous reply message to arrive */
    private long pollingInterval = 500;

    /**
     * Create a channel with the specified queue.
     */
    public MessageSelectingQueueChannel(BlockingQueue<Message<?>> queue) {
        super(queue);

        this.setLoggingEnabled(false);
        this.queue = queue;
    }

    /**
     * Create a channel with the specified queue capacity.
     */
    public MessageSelectingQueueChannel(int capacity) {
        this(new LinkedBlockingQueue<>(capacity));

        Assert.isTrue(capacity > 0, "The capacity must be a positive integer. " +
                "For a zero-capacity alternative, consider using a 'RendezvousChannel'.");
    }

    /**
     * Default constructor.
     */
    public MessageSelectingQueueChannel() {
        this(new LinkedBlockingQueue<>());
    }

    /**
     * Supports selective consumption of messages on the channel. The first message
     * to be accepted by given message selector is returned as result.
     *
     * @param selector
     * @return
     */
    public Message<?> receive(MessageSelector selector) {
        Object[] array = this.queue.toArray();
        for (Object o : array) {
            Message<?> message = (Message<?>) o;
            if (selector.accept(message) && this.queue.remove(message)) {
                return message;
            }
        }

        return null;
    }

    /**
     * Consume messages on the channel via message selector. Timeout forces several retries
     * with polling interval setting.
     *
     * @param selector
     * @param timeout
     * @return
     */
    public Message<?> receive(MessageSelector selector, long timeout) {
        long timeLeft = timeout;
        Message<?> message = receive(selector);

        while (message == null && timeLeft > 0) {
            timeLeft -= pollingInterval;

            if (RETRY_LOG.isDebugEnabled()) {
                RETRY_LOG.debug("No message received with message selector - retrying in " + (timeLeft > 0 ? pollingInterval : pollingInterval + timeLeft) + "ms");
            }

            try {
                Thread.sleep(timeLeft > 0 ? pollingInterval : pollingInterval + timeLeft);
            } catch (InterruptedException e) {
                RETRY_LOG.warn("Thread interrupted while waiting for retry", e);
            }

            message = receive(selector);
        }

        return message;
    }

    /**
     * Gets the pollingInterval.
     * @return the pollingInterval the pollingInterval to get.
     */
    public long getPollingInterval() {
        return pollingInterval;
    }

    /**
     * Sets the pollingInterval.
     * @param pollingInterval the pollingInterval to set
     */
    public void setPollingInterval(long pollingInterval) {
        this.pollingInterval = pollingInterval;
    }
}
