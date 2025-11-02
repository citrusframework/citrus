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
import org.citrusframework.message.MessageCorrelator;
import org.citrusframework.util.StringUtils;
import org.citrusframework.yaml.SchemaProperty;
import org.springframework.integration.core.MessagingTemplate;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.core.DestinationResolver;

/**
 * @since 2.7.6
 */
public class ChannelSyncEndpointBuilder extends AbstractEndpointBuilder<ChannelSyncEndpoint> {

    /** Endpoint target */
    private final ChannelSyncEndpoint endpoint = new ChannelSyncEndpoint();

    private String messagingTemplate;
    private String messageConverter;
    private String correlator;
    private String channelResolver;

    @Override
    public ChannelSyncEndpoint build() {
        if (referenceResolver != null) {
            if (StringUtils.hasText(messagingTemplate)) {
                messagingTemplate(referenceResolver.resolve(messagingTemplate, MessagingTemplate.class));
            }

            if (StringUtils.hasText(messageConverter)) {
                messageConverter(referenceResolver.resolve(messageConverter, ChannelMessageConverter.class));
            }

            if (StringUtils.hasText(correlator)) {
                correlator(referenceResolver.resolve(correlator, MessageCorrelator.class));
            }

            if (StringUtils.hasText(channelResolver)) {
                channelResolver(referenceResolver.resolve(channelResolver, DestinationResolver.class));
            }
        }

        return super.build();
    }

    @Override
    protected ChannelSyncEndpoint getEndpoint() {
        return endpoint;
    }

    /**
     * Sets the channelName property.
     */
    public ChannelSyncEndpointBuilder channel(String channelName) {
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
    public ChannelSyncEndpointBuilder channel(MessageChannel channel) {
        endpoint.getEndpointConfiguration().setChannel(channel);
        return this;
    }

    /**
     * Sets the messagingTemplate property.
     */
    public ChannelSyncEndpointBuilder messagingTemplate(MessagingTemplate messagingTemplate) {
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
    public ChannelSyncEndpointBuilder messageConverter(ChannelMessageConverter messageConverter) {
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
    public ChannelSyncEndpointBuilder channelResolver(DestinationResolver resolver) {
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
    public ChannelSyncEndpointBuilder useObjectMessages(boolean useObjectMessages) {
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
    public ChannelSyncEndpointBuilder filterInternalHeaders(boolean filterInternalHeaders) {
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
    public ChannelSyncEndpointBuilder pollingInterval(int pollingInterval) {
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
    public ChannelSyncEndpointBuilder correlator(MessageCorrelator correlator) {
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
    public ChannelSyncEndpointBuilder timeout(long timeout) {
        endpoint.getEndpointConfiguration().setTimeout(timeout);
        return this;
    }

    @SchemaProperty(description = "Sets the receive timeout when waiting for messages.", defaultValue = "5000")
    public void setTimeout(long timeout) {
        timeout(timeout);
    }
}
