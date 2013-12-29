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
import com.consol.citrus.exceptions.ActionTimeoutException;
import com.consol.citrus.message.MessageReceiver;
import com.consol.citrus.messaging.Consumer;
import com.consol.citrus.messaging.Producer;
import org.springframework.integration.Message;

/**
 * {@link MessageReceiver} implementation consumes messages from aJMS destination. Destination
 * is given by injected instance or destination name.
 *  
 * @author Christoph Deppisch
 * @deprecated
 */
public class JmsMessageReceiver extends AbstractJmsAdapter implements MessageReceiver {

    /**
     * Default constructor.
     */
    public JmsMessageReceiver() {
        super();
    }

    /**
     * Default constructor using Jms endpoint.
     * @param jmsEndpoint
     */
    public JmsMessageReceiver(JmsEndpoint jmsEndpoint) {
        super(jmsEndpoint);
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
     * @see com.consol.citrus.message.MessageReceiver#receive(long)
     * @throws ActionTimeoutException
     */
    public Message<?> receive(long timeout) {
        return getJmsEndpoint().createConsumer().receive(timeout);
    }

    /**
     * @see com.consol.citrus.message.MessageReceiver#receiveSelected(java.lang.String, long)
     * @throws ActionTimeoutException
     */
    public Message<?> receiveSelected(String selector, long timeout) {
        return getJmsEndpoint().createConsumer().receive(selector, timeout);
    }

    /**
     * @see com.consol.citrus.message.MessageReceiver#receive()
     */
    public Message<?> receive() {
        return getJmsEndpoint().createConsumer().receive(getJmsEndpoint().getTimeout());
    }

    /**
     * @see com.consol.citrus.message.MessageReceiver#receiveSelected(java.lang.String)
     */
    public Message<?> receiveSelected(String selector) {
        return getJmsEndpoint().createConsumer().receive(selector, getJmsEndpoint().getTimeout());
    }

    /**
     * Sets the receive timeout.
     * @param receiveTimeout the receiveTimeout to set
     */
    public void setReceiveTimeout(long receiveTimeout) {
        getJmsEndpoint().setTimeout(receiveTimeout);
    }

    /**
     * Gets the receiveTimeout.
     * @return the receiveTimeout
     */
    public long getReceiveTimeout() {
        return getJmsEndpoint().getTimeout();
    }
}
