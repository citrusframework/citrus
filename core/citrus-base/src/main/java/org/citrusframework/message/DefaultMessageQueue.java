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
package org.citrusframework.message;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default message queue implementation. Holds queued messages in memory and adds selective consumption of messages
 * according to a message selector implementation.
 *
 * @author Christoph Deppisch
 */
public class DefaultMessageQueue implements MessageQueue {

    /** Logger */
    private static final Logger logger = LoggerFactory.getLogger(DefaultMessageQueue.class);

    /** Logger */
    private static final Logger RETRY_LOG = LoggerFactory.getLogger("org.citrusframework.RetryLogger");

    /** Blocking in memory message store */
    private final BlockingQueue<Message> queue = new LinkedBlockingQueue<>();

    /** Polling interval when waiting for synchronous reply message to arrive */
    private long pollingInterval = 500;

    /** Flag to enable/disable message logging */
    private boolean loggingEnabled = false;

    private final String name;

    public DefaultMessageQueue(String name) {
        this.name = name;
    }

    @Override
    public void send(Message message) {
        this.queue.add(message);
    }

    @Override
    public Message receive(MessageSelector selector) {
        Object[] array = this.queue.toArray();
        for (Object o : array) {
            Message message = (Message) o;
            if (selector.accept(message) && this.queue.remove(message)) {
                return message;
            }
        }

        return null;
    }

    @Override
    public Message receive(MessageSelector selector, long timeout) {
        long timeLeft = timeout;
        Message message = receive(selector);

        while (message == null && timeLeft > 0) {
            timeLeft -= pollingInterval;

            if (RETRY_LOG.isDebugEnabled()) {
                RETRY_LOG.debug("No message received with message selector - retrying in " +
                        (timeLeft > 0 ? pollingInterval : pollingInterval + timeLeft) + "ms");
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

    @Override
    public void purge(MessageSelector selector) {
        Object[] array = this.queue.toArray();
        for (Object o : array) {
            Message message = (Message) o;
            if (selector.accept(message)) {
                if (this.queue.remove(message)) {
                    if (logger.isDebugEnabled()) {
                        logger.debug(String.format("Purged message '%s' from in memory queue", message.getId()));
                    }
                } else {
                    logger.warn(String.format("Failed to purge message '%s' from in memory queue", message.getId()));
                }
            }
        }
    }

    /**
     * Gets the pollingInterval.
     * @return the pollingInterval to get.
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

    /**
     * Obtains the loggingEnabled.
     * @return
     */
    public boolean isLoggingEnabled() {
        return loggingEnabled;
    }

    /**
     * Specifies the loggingEnabled.
     * @param loggingEnabled
     */
    public void setLoggingEnabled(boolean loggingEnabled) {
        this.loggingEnabled = loggingEnabled;
    }

    @Override
    public String toString() {
        return name;
    }
}
