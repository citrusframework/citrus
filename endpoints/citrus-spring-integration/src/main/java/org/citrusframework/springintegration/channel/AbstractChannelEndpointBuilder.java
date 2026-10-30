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

package org.citrusframework.springintegration.channel;

import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlType;
import org.citrusframework.base.endpoint.AbstractEndpointBuilder;
import org.citrusframework.util.StringUtils;
import org.citrusframework.api.yaml.SchemaProperty;
import org.citrusframework.api.yaml.SchemaType;
import org.springframework.integration.core.MessagingTemplate;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.core.DestinationResolver;

@SchemaType(module = "citrus-spring-integration")
@XmlType(name = "", propOrder = {})
@SuppressWarnings("unchecked")
public abstract class AbstractChannelEndpointBuilder<T extends ChannelEndpoint, B extends AbstractChannelEndpointBuilder<T, B>> extends AbstractEndpointBuilder<T> {

    private String messagingTemplate;
    private String messageConverter;
    private String channelResolver;

    @Override
    public T build() {
        if (referenceResolver != null) {
            if (StringUtils.hasText(messagingTemplate)) {
                messagingTemplate(referenceResolver.resolve(messagingTemplate, MessagingTemplate.class));
            }

            if (StringUtils.hasText(messageConverter)) {
                messageConverter(referenceResolver.resolve(messageConverter, ChannelMessageConverter.class));
            }

            if (StringUtils.hasText(channelResolver)) {
                channelResolver(referenceResolver.resolve(channelResolver, DestinationResolver.class));
            }
        }

        return super.build();
    }

    /**
     * Sets the channelName property.
     */
    public B channel(String channelName) {
        getEndpoint().getEndpointConfiguration().setChannelName(channelName);
        return (B) this;
    }

    @SchemaProperty(description = "The Spring message channel name.")
    @XmlAttribute
    public void setChannel(String channelName) {
        channel(channelName);
    }

    @XmlAttribute(name = "channel-name")
    public void setChannelName(String channelName) {
        channel(channelName);
    }

    /**
     * Sets the channel property.
     */
    public B channel(MessageChannel channel) {
        getEndpoint().getEndpointConfiguration().setChannel(channel);
        return (B) this;
    }

    /**
     * Sets the messagingTemplate property.
     */
    public B messagingTemplate(MessagingTemplate messagingTemplate) {
        getEndpoint().getEndpointConfiguration().setMessagingTemplate(messagingTemplate);
        return (B) this;
    }

    @SchemaProperty(advanced = true, description = "Sets a custom messaging template.")
    @XmlAttribute(name = "messaging-template")
    public void setMessagingTemplate(String messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * Sets the messageConverter property.
     */
    public B messageConverter(ChannelMessageConverter messageConverter) {
        getEndpoint().getEndpointConfiguration().setMessageConverter(messageConverter);
        return (B) this;
    }

    @SchemaProperty(advanced = true, description = "Sets the message converter as a bean reference.")
    @XmlAttribute(name = "message-converter")
    public void setMessageConverter(String messageConverter) {
        this.messageConverter = messageConverter;
    }

    /**
     * Sets the channel resolver.
     */
    public B channelResolver(DestinationResolver resolver) {
        getEndpoint().getEndpointConfiguration().setChannelResolver(resolver);
        return (B) this;
    }

    @SchemaProperty(advanced = true, description = "The channel destination resolver.")
    @XmlAttribute(name = "channel-resolver")
    public void setChannelResolver(String resolver) {
        this.channelResolver = resolver;
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

    @SchemaProperty(
            advanced = true,
            description = "When enabled the endpoint removes all internal headers before sending a message.")
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

    @SchemaProperty(description = "Sets the receive timeout when waiting for messages.", defaultValue = "5000")
    @XmlAttribute
    public void setTimeout(long timeout) {
        timeout(timeout);
    }
}
