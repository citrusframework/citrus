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

import jakarta.annotation.Nullable;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlTransient;
import jakarta.xml.bind.annotation.XmlType;
import org.citrusframework.endpoint.Endpoint;
import org.citrusframework.endpoint.EndpointBuilder;
import org.citrusframework.spi.ReferenceResolver;
import org.citrusframework.spi.ReferenceResolverAware;
import org.citrusframework.api.yaml.SchemaProperty;
import org.citrusframework.api.yaml.SchemaType;

@SchemaType(module = "citrus-jms")
@XmlType(name = "", propOrder = {
        "asynchronous",
        "synchronous"
})
@XmlRootElement(name = "jms")
public class JmsEndpointsBuilder implements EndpointBuilder<Endpoint>, ReferenceResolverAware {

    private AbstractJmsEndpointBuilder<?, ?> delegate;
    private ReferenceResolver referenceResolver;

    private String name;
    private String destination;
    private String connectionFactory;
    private String jmsTemplate;
    private String messageConverter;
    private String destinationResolver;
    private String destinationNameResolver;
    private Boolean pubSubDomain;
    private Boolean autoStart;
    private Boolean durableSubscription;
    private String durableSubscriberName;
    private Boolean useObjectMessages;
    private Boolean filterInternalHeaders;
    private Long timeout;

    @SchemaProperty(description = "Sets the synchronous Jms endpoint.")
    @XmlElement
    public void setSynchronous(JmsSyncEndpointBuilder builder) {
        this.delegate = builder;
    }

    @SchemaProperty(description = "Sets the Jms endpoint.")
    @XmlElement
    public void setAsynchronous(JmsEndpointBuilder builder) {
        this.delegate = builder;
    }

    @SchemaProperty(description = "The name of the endpoint.")
    @XmlAttribute
    public void setName(String name) {
        this.name = name;
    }

    @SchemaProperty(description = "The JMS destination name.")
    @XmlAttribute
    public void setDestination(String destinationName) {
        this.destination = destinationName;
    }

    @SchemaProperty(description = "The JMS connection factory.")
    @XmlAttribute(name = "connection-factory")
    public void setConnectionFactory(String connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    @SchemaProperty(advanced = true, description = "Sets the JMS template.")
    @XmlAttribute(name = "jms-template")
    public void setJmsTemplate(String jmsTemplate) {
        this.jmsTemplate = jmsTemplate;
    }

    @SchemaProperty(advanced = true, description = "Sets the message converter bean reference.")
    @XmlAttribute(name = "message-converter")
    public void setMessageConverter(String messageConverter) {
        this.messageConverter = messageConverter;
    }

    @SchemaProperty(advanced = true, description = "Sets the destination resolver.")
    @XmlAttribute(name = "destination-resolver")
    public void setDestinationResolver(String resolver) {
        this.destinationResolver = resolver;
    }

    @SchemaProperty(advanced = true, description = "Sets the destination name resolver.")
    @XmlAttribute(name = "destination-name-resolver")
    public void setDestinationNameResolver(String resolver) {
        this.destinationNameResolver = resolver;
    }

    @SchemaProperty(
            metadata = { @SchemaProperty.MetaData(key = "$comment", value = "group:publishSubscribe") },
            description = "When enabled the endpoint uses publish/subscribe mode.")
    @XmlAttribute(name = "pub-sub-domain")
    public void setPubSubDomain(boolean pubSubDomain) {
        this.pubSubDomain = pubSubDomain;
    }

    @SchemaProperty(
            metadata = { @SchemaProperty.MetaData(key = "$comment", value = "group:publishSubscribe") },
            description = "When enabled the JMS consumer is started right after the endpoint is created.")
    @XmlAttribute(name = "auto-start")
    public void setAutoStart(boolean autoStart) {
        this.autoStart = autoStart;
    }

    @SchemaProperty(
            metadata = { @SchemaProperty.MetaData(key = "$comment", value = "group:publishSubscribe") },
            description = "Enables/disables durable subscription mode.")
    @XmlAttribute(name = "durable-subscription")
    public void setDurableSubscription(boolean durableSubscription) {
        this.durableSubscription = durableSubscription;
    }

    @SchemaProperty(
            metadata = { @SchemaProperty.MetaData(key = "$comment", value = "group:publishSubscribe") },
            description = "Sets the durable subscriber name.")
    @XmlAttribute(name = "durable-subscriber-name")
    public void setDurableSubscriberName(String durableSubscriberName) {
        this.durableSubscriberName = durableSubscriberName;
    }

    @SchemaProperty(advanced = true, description = "When enabled the endpoint uses object messages.")
    @XmlAttribute(name = "use-object-messages")
    public void setUseObjectMessages(boolean useObjectMessages) {
        this.useObjectMessages = useObjectMessages;
    }

    @SchemaProperty(advanced = true, description = "Filter internal headers.")
    @XmlAttribute(name = "filter-internal-headers")
    public void setFilterInternalHeaders(boolean filterInternalHeaders) {
        this.filterInternalHeaders = filterInternalHeaders;
    }

    @SchemaProperty(description = "Sets the receive timeout when the consumer waits for messages to arrive.", defaultValue = "5000")
    @XmlAttribute
    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    @Override
    public Endpoint build() {
        if (delegate == null) {
            delegate = new JmsEndpointBuilder();
        }

        if (name != null) {
            delegate.setName(name);
        }
        if (destination != null) {
            delegate.setDestination(destination);
        }
        if (connectionFactory != null) {
            delegate.setConnectionFactory(connectionFactory);
        }
        if (jmsTemplate != null) {
            delegate.setJmsTemplate(jmsTemplate);
        }
        if (messageConverter != null) {
            delegate.setMessageConverter(messageConverter);
        }
        if (destinationResolver != null) {
            delegate.setDestinationResolver(destinationResolver);
        }
        if (destinationNameResolver != null) {
            delegate.setDestinationNameResolver(destinationNameResolver);
        }
        if (pubSubDomain != null) {
            delegate.setPubSubDomain(pubSubDomain);
        }
        if (delegate instanceof JmsEndpointBuilder d) {
            if (autoStart != null) {
                d.setAutoStart(autoStart);
            }
            if (durableSubscription != null) {
                d.setDurableSubscription(durableSubscription);
            }
            if (durableSubscriberName != null) {
                d.setDurableSubscriberName(durableSubscriberName);
            }
        }
        if (useObjectMessages != null) {
            delegate.setUseObjectMessages(useObjectMessages);
        }
        if (filterInternalHeaders != null) {
            delegate.setFilterInternalHeaders(filterInternalHeaders);
        }
        if (timeout != null) {
            delegate.setTimeout(timeout);
        }

        if (referenceResolver != null && delegate instanceof ReferenceResolverAware resolverAware) {
            resolverAware.setReferenceResolver(referenceResolver);
        }

        return delegate.build();
    }

    @Override
    public boolean supports(Class<?> endpointType) {
        return delegate != null && delegate.supports(endpointType);
    }

    @Override
    @XmlTransient
    public void setReferenceResolver(@Nullable ReferenceResolver referenceResolver) {
        this.referenceResolver = referenceResolver;
    }
}
