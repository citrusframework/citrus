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

import com.consol.citrus.endpoint.EndpointConfiguration;
import com.consol.citrus.message.MessageSender;
import com.consol.citrus.message.ReplyMessageCorrelator;
import com.consol.citrus.messaging.Consumer;
import com.consol.citrus.messaging.Producer;
import org.springframework.integration.Message;

/**
 * This JMS message sender is quite similar to Spring's AbstractJmsTemplateBasedAdapter that is 
 * already used in asynchronous JMS senders and receivers. But AbstractJmsTemplateBasedAdapter is
 * working with static default destinations.
 * 
 * In this class we rather operate with dynamic destinations. Therefore this adapter implementation has 
 * slight differences.
 * 
 * @author Christoph Deppisch
 * @deprecated
 */
public class JmsReplyMessageSender extends AbstractJmsAdapter implements MessageSender {
    /** Reply destination holder */
    private JmsReplyDestinationHolder replyDestinationHolder;

    public JmsReplyMessageSender() {
        super(new JmsSyncEndpoint());
    }

    @Override
    public JmsSyncEndpoint getJmsEndpoint() {
        return (JmsSyncEndpoint) super.getJmsEndpoint();
    }

    /**
     * @see com.consol.citrus.message.MessageSender#send(org.springframework.integration.Message)
     */
    public void send(Message<?> message) {
        ((JmsSyncConsumer) getJmsEndpoint().createConsumer()).send(message);
    }

    @Override
    public Consumer createConsumer() {
        return getJmsEndpoint().createConsumer();
    }

    @Override
    public Producer createProducer() {
        return getJmsEndpoint().createProducer();
    }

    @Override
    public EndpointConfiguration getEndpointConfiguration() {
        return getJmsEndpoint().getEndpointConfiguration();
    }

    /**
     * Set the reply destination.
     * @param replyDestinationHolder the replyDestinationHolder to set
     */
    public void setReplyDestinationHolder(
            JmsReplyDestinationHolder replyDestinationHolder) {
        this.replyDestinationHolder = replyDestinationHolder;

        if (replyDestinationHolder instanceof JmsSyncMessageReceiver) {
            setJmsEndpoint(((JmsSyncMessageReceiver) replyDestinationHolder).getJmsEndpoint());
        }
    }

    /**
     * Gets the replyDestinationHolder.
     * @return the replyDestinationHolder
     */
    public JmsReplyDestinationHolder getReplyDestinationHolder() {
        return replyDestinationHolder;
    }

    /**
     * Set the message correlator.
     * @param correlator the correlator to set
     */
    public void setCorrelator(ReplyMessageCorrelator correlator) {
        getJmsEndpoint().setCorrelator(correlator);
    }

    /**
     * Gets the correlator.
     * @return the correlator
     */
    public ReplyMessageCorrelator getCorrelator() {
        return getJmsEndpoint().getCorrelator();
    }
}
