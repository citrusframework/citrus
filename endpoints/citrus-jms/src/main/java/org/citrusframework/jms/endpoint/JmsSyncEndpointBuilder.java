/*
 * Copyright 2006-2016 the original author or authors.
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

package org.citrusframework.jms.endpoint;

import jakarta.jms.ConnectionFactory;
import jakarta.jms.Destination;

import org.citrusframework.endpoint.AbstractEndpointBuilder;
import org.citrusframework.endpoint.resolver.EndpointUriResolver;
import org.citrusframework.jms.message.JmsMessageConverter;
import org.citrusframework.message.MessageCorrelator;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.support.destination.DestinationResolver;

/**
 * @author Christoph Deppisch
 * @since 2.5
 */
public class JmsSyncEndpointBuilder extends AbstractEndpointBuilder<JmsSyncEndpoint> {

    /** Endpoint target */
    private JmsSyncEndpoint endpoint = new JmsSyncEndpoint();

    @Override
    protected JmsSyncEndpoint getEndpoint() {
        return endpoint;
    }

    /**
     * Sets the destinationName property.
     * @param destinationName
     * @return
     */
    public JmsSyncEndpointBuilder destination(String destinationName) {
        endpoint.getEndpointConfiguration().setDestinationName(destinationName);
        return this;
    }

    /**
     * Sets the destination property.
     * @param destination
     * @return
     */
    public JmsSyncEndpointBuilder destination(Destination destination) {
        endpoint.getEndpointConfiguration().setDestination(destination);
        return this;
    }

    /**
     * Sets the reply destinationName property.
     * @param destinationName
     * @return
     */
    public JmsSyncEndpointBuilder replyDestination(String destinationName) {
        endpoint.getEndpointConfiguration().setReplyDestinationName(destinationName);
        return this;
    }

    /**
     * Sets the reply destination property.
     * @param destination
     * @return
     */
    public JmsSyncEndpointBuilder replyDestination(Destination destination) {
        endpoint.getEndpointConfiguration().setReplyDestination(destination);
        return this;
    }

    /**
     * Sets the connectionFactory property.
     * @param connectionFactory
     * @return
     */
    public JmsSyncEndpointBuilder connectionFactory(ConnectionFactory connectionFactory) {
        endpoint.getEndpointConfiguration().setConnectionFactory(connectionFactory);
        return this;
    }

    /**
     * Sets the jmsTemplate property.
     * @param jmsTemplate
     * @return
     */
    public JmsSyncEndpointBuilder jmsTemplate(JmsTemplate jmsTemplate) {
        endpoint.getEndpointConfiguration().setJmsTemplate(jmsTemplate);
        return this;
    }

    /**
     * Sets the messageConverter property.
     * @param messageConverter
     * @return
     */
    public JmsSyncEndpointBuilder messageConverter(JmsMessageConverter messageConverter) {
        endpoint.getEndpointConfiguration().setMessageConverter(messageConverter);
        return this;
    }

    /**
     * Sets the destination resolver.
     * @param resolver
     * @return
     */
    public JmsSyncEndpointBuilder destinationResolver(DestinationResolver resolver) {
        endpoint.getEndpointConfiguration().setDestinationResolver(resolver);
        return this;
    }

    /**
     * Sets the destination name resolver.
     * @param resolver
     * @return
     */
    public JmsSyncEndpointBuilder destinationNameResolver(EndpointUriResolver resolver) {
        endpoint.getEndpointConfiguration().setDestinationNameResolver(resolver);
        return this;
    }

    /**
     * Sets the pubSubDomain property.
     * @param pubSubDomain
     * @return
     */
    public JmsSyncEndpointBuilder pubSubDomain(boolean pubSubDomain) {
        endpoint.getEndpointConfiguration().setPubSubDomain(pubSubDomain);
        return this;
    }

    /**
     * Sets the useObjectMessages property.
     * @param useObjectMessages
     * @return
     */
    public JmsSyncEndpointBuilder useObjectMessages(boolean useObjectMessages) {
        endpoint.getEndpointConfiguration().setUseObjectMessages(useObjectMessages);
        return this;
    }

    /**
     * Sets the filterInternalHeaders property.
     * @param filterInternalHeaders
     * @return
     */
    public JmsSyncEndpointBuilder filterInternalHeaders(boolean filterInternalHeaders) {
        endpoint.getEndpointConfiguration().setFilterInternalHeaders(filterInternalHeaders);
        return this;
    }

    /**
     * Sets the polling interval.
     * @param pollingInterval
     * @return
     */
    public JmsSyncEndpointBuilder pollingInterval(int pollingInterval) {
        endpoint.getEndpointConfiguration().setPollingInterval(pollingInterval);
        return this;
    }

    /**
     * Sets the message correlator.
     * @param correlator
     * @return
     */
    public JmsSyncEndpointBuilder correlator(MessageCorrelator correlator) {
        endpoint.getEndpointConfiguration().setCorrelator(correlator);
        return this;
    }

    /**
     * Sets the default timeout.
     * @param timeout
     * @return
     */
    public JmsSyncEndpointBuilder timeout(long timeout) {
        endpoint.getEndpointConfiguration().setTimeout(timeout);
        return this;
    }
}
