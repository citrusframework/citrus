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
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlType;
import org.citrusframework.base.endpoint.AbstractEndpointBuilder;
import org.citrusframework.endpoint.resolver.EndpointUriResolver;
import org.citrusframework.jms.message.JmsMessageConverter;
import org.citrusframework.util.StringUtils;
import org.citrusframework.api.yaml.SchemaProperty;
import org.citrusframework.api.yaml.SchemaType;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.support.destination.DestinationResolver;

@SchemaType(module = "citrus-jms")
@XmlType(name = "", propOrder = {})
@SuppressWarnings("unchecked")
public abstract class AbstractJmsEndpointBuilder<T extends JmsEndpoint, B extends AbstractJmsEndpointBuilder<T, B>> extends AbstractEndpointBuilder<T> {

    private String connectionFactory;
    private String jmsTemplate;
    private String messageConverter;
    private String destinationResolver;
    private String destinationNameResolver;

    @Override
    public T build() {
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

            if (StringUtils.hasText(destinationResolver)) {
                destinationResolver(referenceResolver.resolve(destinationResolver, DestinationResolver.class));
            }

            if (StringUtils.hasText(destinationNameResolver)) {
                destinationNameResolver(referenceResolver.resolve(destinationNameResolver, EndpointUriResolver.class));
            }
        }

        return super.build();
    }

    /**
     * Sets the destinationName property.
     */
    public B destination(String destinationName) {
        getEndpoint().getEndpointConfiguration().setDestinationName(destinationName);
        return (B) this;
    }

    @SchemaProperty(description = "The JMS destination name.")
    @XmlAttribute
    public void setDestination(String destinationName) {
        destination(destinationName);
    }

    /**
     * Sets the destination property.
     */
    public B destination(Destination destination) {
        getEndpoint().getEndpointConfiguration().setDestination(destination);
        return (B) this;
    }

    /**
     * Sets the connectionFactory property.
     */
    public B connectionFactory(ConnectionFactory connectionFactory) {
        getEndpoint().getEndpointConfiguration().setConnectionFactory(connectionFactory);
        return (B) this;
    }

    @SchemaProperty(description = "The JMS connection factory.")
    @XmlAttribute(name = "connection-factory")
    public void setConnectionFactory(String connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    /**
     * Sets the jmsTemplate property.
     */
    public B jmsTemplate(JmsTemplate jmsTemplate) {
        getEndpoint().getEndpointConfiguration().setJmsTemplate(jmsTemplate);
        return (B) this;
    }

    @SchemaProperty(advanced = true, description = "Sets the JMS template.")
    @XmlAttribute(name = "jms-template")
    public void setJmsTemplate(String jmsTemplate) {
        this.jmsTemplate = jmsTemplate;
    }

    /**
     * Sets the messageConverter property.
     */
    public B messageConverter(JmsMessageConverter messageConverter) {
        getEndpoint().getEndpointConfiguration().setMessageConverter(messageConverter);
        return (B) this;
    }

    @SchemaProperty(advanced = true, description = "Sets the message converter bean reference.")
    @XmlAttribute(name = "message-converter")
    public void setMessageConverter(String messageConverter) {
        this.messageConverter = messageConverter;
    }

    /**
     * Sets the destination resolver.
     */
    public B destinationResolver(DestinationResolver resolver) {
        getEndpoint().getEndpointConfiguration().setDestinationResolver(resolver);
        return (B) this;
    }

    @SchemaProperty(advanced = true, description = "Sets the destination resolver.")
    @XmlAttribute(name = "destination-resolver")
    public void setDestinationResolver(String resolver) {
        this.destinationResolver = resolver;
    }

    /**
     * Sets the destination name resolver.
     */
    public B destinationNameResolver(EndpointUriResolver resolver) {
        getEndpoint().getEndpointConfiguration().setDestinationNameResolver(resolver);
        return (B) this;
    }

    @SchemaProperty(advanced = true, description = "Sets the destination name resolver.")
    @XmlAttribute(name = "destination-name-resolver")
    public void setDestinationNameResolver(String resolver) {
        this.destinationNameResolver = resolver;
    }

    /**
     * Sets the pubSubDomain property.
     */
    public B pubSubDomain(boolean pubSubDomain) {
        getEndpoint().getEndpointConfiguration().setPubSubDomain(pubSubDomain);
        return (B) this;
    }

    @SchemaProperty(
            metadata = { @SchemaProperty.MetaData(key = "$comment", value = "group:publishSubscribe") },
            description = "When enabled the endpoint uses publish/subscribe mode.")
    @XmlAttribute(name = "pub-sub-domain")
    public void setPubSubDomain(boolean pubSubDomain) {
        pubSubDomain(pubSubDomain);
    }

    /**
     * Sets the useObjectMessages property.
     */
    public B useObjectMessages(boolean useObjectMessages) {
        getEndpoint().getEndpointConfiguration().setUseObjectMessages(useObjectMessages);
        return (B) this;
    }

    @SchemaProperty(advanced = true, description = "When enabled the endpoint uses object messages.")
    @XmlAttribute(name = "use-object-messages")
    public void setUseObjectMessages(boolean useObjectMessages) {
        useObjectMessages(useObjectMessages);
    }

    /**
     * Sets the filterInternalHeaders property.
     */
    public B filterInternalHeaders(boolean filterInternalHeaders) {
        getEndpoint().getEndpointConfiguration().setFilterInternalHeaders(filterInternalHeaders);
        return (B) this;
    }

    @SchemaProperty(advanced = true, description = "Filter internal headers.")
    @XmlAttribute(name = "filter-internal-headers")
    public void setFilterInternalHeaders(boolean filterInternalHeaders) {
        filterInternalHeaders(filterInternalHeaders);
    }

    /**
     * Sets the default timeout.
     */
    public B timeout(long timeout) {
        getEndpoint().getEndpointConfiguration().setTimeout(timeout);
        return (B) this;
    }

    @SchemaProperty(description = "Sets the receive timeout when the consumer waits for messages to arrive.", defaultValue = "5000")
    @XmlAttribute
    public void setTimeout(long timeout) {
        timeout(timeout);
    }
}
