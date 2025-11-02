/*
 * Copyright the original author or authors.
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
import org.citrusframework.util.StringUtils;
import org.citrusframework.yaml.SchemaProperty;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.support.destination.DestinationResolver;

/**
 * @since 2.5
 */
public class JmsSyncEndpointBuilder extends AbstractEndpointBuilder<JmsSyncEndpoint> {

    /** Endpoint target */
    private final JmsSyncEndpoint endpoint = new JmsSyncEndpoint();

    private String connectionFactory;
    private String jmsTemplate;
    private String messageConverter;
    private String correlator;
    private String destinationResolver;
    private String destinationNameResolver;

    @Override
    public JmsSyncEndpoint build() {
        if (referenceResolver != null) {
            if (StringUtils.hasText(connectionFactory)) {
                connectionFactory(referenceResolver.resolve(connectionFactory, ConnectionFactory.class));
            }

            if (StringUtils.hasText(jmsTemplate)) {
                jmsTemplate(referenceResolver.resolve(jmsTemplate, JmsTemplate.class));
            }

            if (StringUtils.hasText(messageConverter)) {
                messageConverter(referenceResolver.resolve(messageConverter, JmsMessageConverter.class));
            }

            if (StringUtils.hasText(correlator)) {
                correlator(referenceResolver.resolve(correlator, MessageCorrelator.class));
            }

            if (StringUtils.hasText(destinationResolver)) {
                destinationResolver(referenceResolver.resolve(destinationResolver, DestinationResolver.class));
            }

            if (StringUtils.hasText(destinationNameResolver)) {
                destinationNameResolver(referenceResolver.resolve(destinationNameResolver, EndpointUriResolver.class));
            }
        }

        return super.build();
    }

    @Override
    protected JmsSyncEndpoint getEndpoint() {
        return endpoint;
    }

    /**
     * Sets the destinationName property.
     */
    public JmsSyncEndpointBuilder destination(String destinationName) {
        endpoint.getEndpointConfiguration().setDestinationName(destinationName);
        return this;
    }

    @SchemaProperty(description = "The JMS destination name.")
    public void setDestination(String destinationName) {
        destination(destinationName);
    }

    /**
     * Sets the destination property.
     */
    public JmsSyncEndpointBuilder destination(Destination destination) {
        endpoint.getEndpointConfiguration().setDestination(destination);
        return this;
    }

    /**
     * Sets the reply destinationName property.
     */
    public JmsSyncEndpointBuilder replyDestination(String destinationName) {
        endpoint.getEndpointConfiguration().setReplyDestinationName(destinationName);
        return this;
    }

    @SchemaProperty(description = "Sets the reply destination name.")
    public void setReplyDestination(String replyDestination) {
        replyDestination(replyDestination);
    }

    /**
     * Sets the reply destination property.
     */
    public JmsSyncEndpointBuilder replyDestination(Destination destination) {
        endpoint.getEndpointConfiguration().setReplyDestination(destination);
        return this;
    }

    /**
     * Sets the connectionFactory property.
     */
    public JmsSyncEndpointBuilder connectionFactory(ConnectionFactory connectionFactory) {
        endpoint.getEndpointConfiguration().setConnectionFactory(connectionFactory);
        return this;
    }

    @SchemaProperty(description = "The JMS connection factory.")
    public void setConnectionFactory(String connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    /**
     * Sets the jmsTemplate property.
     */
    public JmsSyncEndpointBuilder jmsTemplate(JmsTemplate jmsTemplate) {
        endpoint.getEndpointConfiguration().setJmsTemplate(jmsTemplate);
        return this;
    }

    @SchemaProperty(advanced = true, description = "Sets the JMS template.")
    public void setJmsTemplate(String jmsTemplate) {
        this.jmsTemplate = jmsTemplate;
    }

    /**
     * Sets the messageConverter property.
     */
    public JmsSyncEndpointBuilder messageConverter(JmsMessageConverter messageConverter) {
        endpoint.getEndpointConfiguration().setMessageConverter(messageConverter);
        return this;
    }

    @SchemaProperty(advanced = true, description = "Sets the message converter bean reference.")
    public void setMessageConverter(String messageConverter) {
        this.messageConverter = messageConverter;
    }

    /**
     * Sets the destination resolver.
     */
    public JmsSyncEndpointBuilder destinationResolver(DestinationResolver resolver) {
        endpoint.getEndpointConfiguration().setDestinationResolver(resolver);
        return this;
    }

    @SchemaProperty(advanced = true, description = "Sets the destination resolver.")
    public void setDestinationResolver(String resolver) {
        this.destinationResolver = resolver;
    }

    /**
     * Sets the destination name resolver.
     */
    public JmsSyncEndpointBuilder destinationNameResolver(EndpointUriResolver resolver) {
        endpoint.getEndpointConfiguration().setDestinationNameResolver(resolver);
        return this;
    }

    @SchemaProperty(advanced = true, description = "Sets the destination name resolver.")
    public void setDestinationNameResolver(String resolver) {
        this.destinationNameResolver = resolver;
    }

    /**
     * Sets the pubSubDomain property.
     */
    public JmsSyncEndpointBuilder pubSubDomain(boolean pubSubDomain) {
        endpoint.getEndpointConfiguration().setPubSubDomain(pubSubDomain);
        return this;
    }

    @SchemaProperty(advanced = true, description = "When enabled the endpoint uses publish/subscribe mode.")
    public void setPubSubDomain(boolean pubSubDomain) {
        pubSubDomain(pubSubDomain);
    }

    /**
     * Sets the useObjectMessages property.
     */
    public JmsSyncEndpointBuilder useObjectMessages(boolean useObjectMessages) {
        endpoint.getEndpointConfiguration().setUseObjectMessages(useObjectMessages);
        return this;
    }

    @SchemaProperty(advanced = true, description = "When enabled the endpoint uses object messages.")
    public void setUseObjectMessages(boolean useObjectMessages) {
        useObjectMessages(useObjectMessages);
    }

    /**
     * Sets the filterInternalHeaders property.
     */
    public JmsSyncEndpointBuilder filterInternalHeaders(boolean filterInternalHeaders) {
        endpoint.getEndpointConfiguration().setFilterInternalHeaders(filterInternalHeaders);
        return this;
    }

    @SchemaProperty(
            advanced = true,
            description = "When enabled the endpoint removes all internal headers before sending a message.")
    public void setFilterInternalHeaders(boolean filterInternalHeaders) {
        filterInternalHeaders(filterInternalHeaders);
    }

    /**
     * Sets the polling interval.
     */
    public JmsSyncEndpointBuilder pollingInterval(int pollingInterval) {
        endpoint.getEndpointConfiguration().setPollingInterval(pollingInterval);
        return this;
    }

    @SchemaProperty(advanced = true, description = "Sets the polling interval.")
    public void setPollingInterval(int pollingInterval) {
        pollingInterval(pollingInterval);
    }

    /**
     * Sets the message correlator.
     */
    public JmsSyncEndpointBuilder correlator(MessageCorrelator correlator) {
        endpoint.getEndpointConfiguration().setCorrelator(correlator);
        return this;
    }

    @SchemaProperty(advanced = true, description = "Sets the message correlator.")
    public void setCorrelator(String correlator) {
        this.correlator = correlator;
    }

    /**
     * Sets the default timeout.
     */
    public JmsSyncEndpointBuilder timeout(long timeout) {
        endpoint.getEndpointConfiguration().setTimeout(timeout);
        return this;
    }

    @SchemaProperty(description = "Sets the receive timeout when the consumer waits for messages to arrive.")
    public void setTimeout(long timeout) {
        timeout(timeout);
    }
}
