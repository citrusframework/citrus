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

package org.citrusframework.channel;

import org.citrusframework.endpoint.AbstractEndpointBuilder;
import org.citrusframework.util.StringUtils;
import org.citrusframework.yaml.SchemaProperty;
import org.springframework.integration.core.MessagingTemplate;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.core.DestinationResolver;

/**
 * @since 2.7.6
 */
public class ChannelEndpointBuilder extends AbstractEndpointBuilder<ChannelEndpoint> {

    /** Endpoint target */
    private final ChannelEndpoint endpoint = new ChannelEndpoint();

    private String messagingTemplate;
    private String messageConverter;
    private String channelResolver;

    @Override
    public ChannelEndpoint build() {
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

    @Override
    protected ChannelEndpoint getEndpoint() {
        return endpoint;
    }

    /**
     * Sets the channelName property.
     */
    public ChannelEndpointBuilder channel(String channelName) {
        endpoint.getEndpointConfiguration().setChannelName(channelName);
        return this;
    }

    @SchemaProperty(description = "The Spring message channel name.")
    public void setChannel(String channelName) {
        channel(channelName);
    }

    /**
     * Sets the channel property.
     */
    public ChannelEndpointBuilder channel(MessageChannel channel) {
        endpoint.getEndpointConfiguration().setChannel(channel);
        return this;
    }

    /**
     * Sets the messagingTemplate property.
     */
    public ChannelEndpointBuilder messagingTemplate(MessagingTemplate messagingTemplate) {
        endpoint.getEndpointConfiguration().setMessagingTemplate(messagingTemplate);
        return this;
    }

    @SchemaProperty(advanced = true, description = "Sets a custom messaging template.")
    public void setMessagingTemplate(String messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * Sets the messageConverter property.
     */
    public ChannelEndpointBuilder messageConverter(ChannelMessageConverter messageConverter) {
        endpoint.getEndpointConfiguration().setMessageConverter(messageConverter);
        return this;
    }

    @SchemaProperty(advanced = true, description = "Sets the message converter as a bean reference.")
    public void setMessageConverter(String messageConverter) {
        this.messageConverter = messageConverter;
    }

    /**
     * Sets the channel resolver.
     */
    public ChannelEndpointBuilder channelResolver(DestinationResolver resolver) {
        endpoint.getEndpointConfiguration().setChannelResolver(resolver);
        return this;
    }

    @SchemaProperty(advanced = true, description = "The channel destination resolver.")
    public void setChannelResolver(String resolver) {
        this.channelResolver = resolver;
    }

    /**
     * Sets the useObjectMessages property.
     */
    public ChannelEndpointBuilder useObjectMessages(boolean useObjectMessages) {
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
    public ChannelEndpointBuilder filterInternalHeaders(boolean filterInternalHeaders) {
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
     * Sets the default timeout.
     */
    public ChannelEndpointBuilder timeout(long timeout) {
        endpoint.getEndpointConfiguration().setTimeout(timeout);
        return this;
    }

    @SchemaProperty(description = "Sets the receive timeout when waiting for messages.", defaultValue = "5000")
    public void setTimeout(long timeout) {
        timeout(timeout);
    }
}
