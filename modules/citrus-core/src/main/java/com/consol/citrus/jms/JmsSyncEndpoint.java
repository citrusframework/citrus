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

import com.consol.citrus.message.ReplyMessageCorrelator;
import com.consol.citrus.messaging.*;
import org.springframework.beans.factory.DisposableBean;

import javax.jms.Destination;

/**
 * Synchronous Jms message endpoint. When sending messages endpoint sets replyTo message header and waits for synchronous response.
 * When receiving messages endpoint reads replyTo header from incoming request and sends synchronous response back.
 *
 * @author Christoph Deppisch
 * @since 1.4
 */
public class JmsSyncEndpoint extends JmsEndpoint implements DisposableBean {

    /** One of producer or consumer for this endpoint */
    private JmsSyncProducer jmsSyncMessageProducer;
    private JmsSyncConsumer jmsSyncMessageConsumer;

    public JmsSyncEndpoint() {
        super(new JmsSyncEndpointConfiguration());
    }

    @Override
    public JmsSyncEndpointConfiguration getEndpointConfiguration() {
        return (JmsSyncEndpointConfiguration) super.getEndpointConfiguration();
    }

    @Override
    public SelectiveConsumer createConsumer() {
        if (jmsSyncMessageProducer != null) {
            return jmsSyncMessageProducer;
        }

        if (jmsSyncMessageConsumer == null) {
            jmsSyncMessageConsumer = new JmsSyncConsumer(getEndpointConfiguration());
        }

        return jmsSyncMessageConsumer;
    }

    @Override
    public Producer createProducer() {
        if (jmsSyncMessageConsumer != null) {
            return jmsSyncMessageConsumer;
        }

        if (jmsSyncMessageProducer == null) {
            jmsSyncMessageProducer = new JmsSyncProducer(getEndpointConfiguration());
        }

        return jmsSyncMessageProducer;
    }

    /**
     * Destroy method closing JMS session and connection
     */
    public void destroy() throws Exception {
        if (jmsSyncMessageProducer != null) {
            jmsSyncMessageProducer.destroy();
        }
    }

    /**
     * Set the reply message correlator.
     * @param correlator the correlator to set
     */
    public void setCorrelator(ReplyMessageCorrelator correlator) {
        getEndpointConfiguration().setCorrelator(correlator);
    }

    /**
     * Gets the correlator.
     * @return the correlator
     */
    public ReplyMessageCorrelator getCorrelator() {
        return getEndpointConfiguration().getCorrelator();
    }

    /**
     * Gets the replyDestination.
     * @return the replyDestination
     */
    public Destination getReplyDestination() {
        return getEndpointConfiguration().getReplyDestination();
    }

    /**
     * Set the reply destination.
     * @param replyDestination the replyDestination to set
     */
    public void setReplyDestination(Destination replyDestination) {
        getEndpointConfiguration().setReplyDestination(replyDestination);
    }

    /**
     * Gets the replyDestinationName.
     * @return the replyDestinationName
     */
    public String getReplyDestinationName() {
        return getEndpointConfiguration().getReplyDestinationName();
    }

    /**
     * Set the reply destination name.
     * @param replyDestinationName the replyDestinationName to set
     */
    public void setReplyDestinationName(String replyDestinationName) {
        getEndpointConfiguration().setReplyDestinationName(replyDestinationName);
    }

    /**
     * Gets the pollingInterval.
     * @return the pollingInterval the pollingInterval to get.
     */
    public long getPollingInterval() {
        return getEndpointConfiguration().getPollingInterval();
    }

    /**
     * Sets the pollingInterval.
     * @param pollingInterval the pollingInterval to set
     */
    public void setPollingInterval(long pollingInterval) {
        getEndpointConfiguration().setPollingInterval(pollingInterval);
    }
}
