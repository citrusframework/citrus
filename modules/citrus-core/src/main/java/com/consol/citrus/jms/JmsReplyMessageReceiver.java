/*
 * Copyright 2006-2013 the original author or authors.
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

import com.consol.citrus.TestActor;
import com.consol.citrus.message.ReplyMessageReceiver;
import org.springframework.integration.Message;

/**
 * Intermediate class used for backward compatibility to old message sender and receiver patterns.
 * @author Christoph Deppisch
 * @since 1.4
 * @deprecated
 */
public class JmsReplyMessageReceiver extends ReplyMessageReceiver {

    /** Synchronous endpoint providing the reply messages */
    private JmsSyncEndpoint jmsEndpoint;

    /**
     * Default constructor.
     */
    public JmsReplyMessageReceiver() {
        this.jmsEndpoint  = new JmsSyncEndpoint();
    }

    /**
     * Default constructor with Jms endpoint.
     * @param jmsEndpoint
     */
    protected JmsReplyMessageReceiver(JmsSyncEndpoint jmsEndpoint) {
        this.jmsEndpoint = jmsEndpoint;
    }

    /**
     * Gets the Jms endpoint.
     * @return
     */
    public JmsSyncEndpoint getJmsEndpoint() {
        return jmsEndpoint;
    }

    /**
     * Sets the Jms endpoint
     * @param jmsEndpoint
     */
    public void setJmsEndpoint(JmsSyncEndpoint jmsEndpoint) {
        this.jmsEndpoint = jmsEndpoint;
    }

    /**
     * @see com.consol.citrus.message.MessageReceiver#receive()
     */
    public Message<?> receive() {
        return jmsEndpoint.createConsumer().receive("", jmsEndpoint.getTimeout());
    }

    /**
     * @see com.consol.citrus.message.MessageReceiver#receive(long)
     */
    public Message<?> receive(long timeout) {
        return jmsEndpoint.createConsumer().receive("", timeout);
    }

    /**
     * @see com.consol.citrus.message.MessageReceiver#receiveSelected(java.lang.String)
     */
    public Message<?> receiveSelected(String selector) {
        return jmsEndpoint.createConsumer().receive(selector, jmsEndpoint.getTimeout());
    }

    /**
     * @see com.consol.citrus.message.MessageReceiver#receiveSelected(java.lang.String, long)
     */
    public Message<?> receiveSelected(String selector, long timeout) {
        return jmsEndpoint.createConsumer().receive(selector, timeout);
    }

    /**
     * @see com.consol.citrus.message.ReplyMessageHandler#onReplyMessage(org.springframework.integration.Message, java.lang.String)
     */
    public void onReplyMessage(Message<?> replyMessage, String correlationKey) {
        ((JmsSyncProducer) jmsEndpoint.createProducer()).onReplyMessage(correlationKey, replyMessage);
    }

    /**
     * @see com.consol.citrus.message.ReplyMessageHandler#onReplyMessage(org.springframework.integration.Message)
     */
    public void onReplyMessage(Message<?> replyMessage) {
        ((JmsSyncProducer) jmsEndpoint.createProducer()).onReplyMessage("", replyMessage);
    }

    /**
     * Gets the actor.
     * @return the actor the actor to get.
     */
    public TestActor getActor() {
        return jmsEndpoint.getActor();
    }

    /**
     * Sets the actor.
     * @param actor the actor to set
     */
    public void setActor(TestActor actor) {
        jmsEndpoint.setActor(actor);
    }

    /**
     * Gets the pollingInterval.
     * @return the pollingInterval the pollingInterval to get.
     */
    public long getPollingInterval() {
        return jmsEndpoint.getPollingInterval();
    }

    /**
     * Sets the pollingInterval.
     * @param pollingInterval the pollingInterval to set
     */
    public void setPollingInterval(long pollingInterval) {
        jmsEndpoint.setPollingInterval(pollingInterval);
    }

    @Override
    public void setBeanName(String name) {
        jmsEndpoint.setBeanName(name);
    }

    @Override
    public String getName() {
        return jmsEndpoint.getName();
    }
}
