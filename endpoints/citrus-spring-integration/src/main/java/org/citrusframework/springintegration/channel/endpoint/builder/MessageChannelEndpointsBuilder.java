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

package org.citrusframework.springintegration.channel.endpoint.builder;

import jakarta.annotation.Nullable;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlTransient;
import jakarta.xml.bind.annotation.XmlType;
import org.citrusframework.springintegration.channel.AbstractChannelEndpointBuilder;
import org.citrusframework.springintegration.channel.ChannelEndpointBuilder;
import org.citrusframework.springintegration.channel.ChannelSyncEndpointBuilder;
import org.citrusframework.endpoint.Endpoint;
import org.citrusframework.endpoint.EndpointBuilder;
import org.citrusframework.spi.ReferenceResolver;
import org.citrusframework.spi.ReferenceResolverAware;
import org.citrusframework.api.yaml.SchemaProperty;
import org.citrusframework.api.yaml.SchemaType;

@SchemaType(module = "citrus-spring-integration")
@XmlType(name = "", propOrder = {
        "asynchronous",
        "synchronous"
})
@XmlRootElement(name = "channel")
public class MessageChannelEndpointsBuilder implements EndpointBuilder<Endpoint>, ReferenceResolverAware {

    private AbstractChannelEndpointBuilder<?, ?> delegate;
    private ReferenceResolver referenceResolver;

    private String name;
    private String channel;
    private String channelName;
    private String messagingTemplate;
    private String messageConverter;
    private String channelResolver;
    private Boolean useObjectMessages;
    private Boolean filterInternalHeaders;
    private Long timeout;

    @SchemaProperty(description = "Sets the synchronous Spring message channel endpoint.")
    @XmlElement
    public void setSynchronous(ChannelSyncEndpointBuilder builder) {
        this.delegate = builder;
    }

    @SchemaProperty(description = "Sets the Spring message channel endpoint.")
    @XmlElement
    public void setAsynchronous(ChannelEndpointBuilder builder) {
        this.delegate = builder;
    }

    @SchemaProperty(description = "The name of the endpoint.")
    @XmlAttribute
    public void setName(String name) {
        this.name = name;
    }

    @SchemaProperty(description = "The Spring message channel name.")
    @XmlAttribute
    public void setChannel(String channelName) {
        this.channel = channelName;
    }

    @XmlAttribute(name = "channel-name")
    public void setChannelName(String channelName) {
        this.channelName = channelName;
    }

    @SchemaProperty(advanced = true, description = "Sets a custom messaging template.")
    @XmlAttribute(name = "messaging-template")
    public void setMessagingTemplate(String messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @SchemaProperty(advanced = true, description = "Sets the message converter as a bean reference.")
    @XmlAttribute(name = "message-converter")
    public void setMessageConverter(String messageConverter) {
        this.messageConverter = messageConverter;
    }

    @SchemaProperty(advanced = true, description = "The channel destination resolver.")
    @XmlAttribute(name = "channel-resolver")
    public void setChannelResolver(String resolver) {
        this.channelResolver = resolver;
    }

    @SchemaProperty(advanced = true, description = "When enabled the endpoint uses object messages.")
    @XmlAttribute(name = "use-object-messages")
    public void setUseObjectMessages(boolean useObjectMessages) {
        this.useObjectMessages = useObjectMessages;
    }

    @SchemaProperty(
            advanced = true,
            description = "When enabled the endpoint removes all internal headers before sending a message.")
    @XmlAttribute(name = "filter-internal-headers")
    public void setFilterInternalHeaders(boolean filterInternalHeaders) {
        this.filterInternalHeaders = filterInternalHeaders;
    }

    @SchemaProperty(description = "Sets the receive timeout when waiting for messages.", defaultValue = "5000")
    @XmlAttribute
    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    @Override
    public Endpoint build() {
        if (delegate == null) {
            delegate = new ChannelEndpointBuilder();
        }

        if (name != null) {
            delegate.setName(name);
        }
        if (channel != null) {
            delegate.setChannel(channel);
        }
        if (channelName != null) {
            delegate.setChannelName(channelName);
        }
        if (messagingTemplate != null) {
            delegate.setMessagingTemplate(messagingTemplate);
        }
        if (messageConverter != null) {
            delegate.setMessageConverter(messageConverter);
        }
        if (channelResolver != null) {
            delegate.setChannelResolver(channelResolver);
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
