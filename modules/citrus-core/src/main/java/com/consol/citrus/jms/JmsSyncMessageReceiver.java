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

import com.consol.citrus.message.ReplyMessageCorrelator;
import org.springframework.integration.Message;

import javax.jms.Destination;

/**
 * Synchronous message receiver implementation for JMS. Class receives messages on a JMS destination
 * and saves the reply destination. As class implements the {@link JmsReplyDestinationHolder} interface
 * synchronous reply message sender implementations may ask for the reply destination later in 
 * the test execution.
 * 
 * In case a reply message correlator is set in this class the reply destinations are stored with a given
 * correlation key. A synchronous reply message sender must ask with this specific correlation key in 
 * order to get the proper reply destination.
 * 
 * @author Christoph Deppisch
 * @deprecated
 */
public class JmsSyncMessageReceiver extends JmsMessageReceiver implements JmsReplyDestinationHolder {

    public JmsSyncMessageReceiver() {
        super(new JmsSyncEndpoint());
    }

    @Override
    public JmsSyncEndpoint getJmsEndpoint() {
        return (JmsSyncEndpoint) super.getJmsEndpoint();
    }

    @Override
    public Message<?> receive(long timeout) {
        return getJmsEndpoint().createConsumer().receive(timeout);
    }
    
    @Override
    public Message<?> receiveSelected(String selector, long timeout) {
        return getJmsEndpoint().createConsumer().receive(selector, timeout);
    }

    /**
     * @see com.consol.citrus.jms.JmsReplyDestinationHolder#getReplyDestination(java.lang.String)
     */
    public Destination getReplyDestination(String correlationKey) {
        return ((JmsSyncConsumer)getJmsEndpoint().createConsumer()).findReplyDestination(correlationKey);
    }

    /**
     * @see com.consol.citrus.jms.JmsReplyDestinationHolder#getReplyDestination()
     */
    public Destination getReplyDestination() {
        return ((JmsSyncConsumer)getJmsEndpoint().createConsumer()).findReplyDestination();
    }

    /**
     * Set the reply message correlator.
     * @param correlator the correlator to set
     */
    public void setCorrelator(ReplyMessageCorrelator correlator) {
        getJmsEndpoint().getEndpointConfiguration().setCorrelator(correlator);
    }

    /**
     * Gets the correlator.
     * @return the correlator
     */
    public ReplyMessageCorrelator getCorrelator() {
        return getJmsEndpoint().getEndpointConfiguration().getCorrelator();
    }
}
