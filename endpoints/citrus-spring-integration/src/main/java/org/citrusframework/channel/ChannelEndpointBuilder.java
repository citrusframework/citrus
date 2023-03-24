/*
 * Copyright 2006-2018 the original author or authors.
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
import org.springframework.integration.core.MessagingTemplate;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.core.DestinationResolver;

/**
 * @author Christoph Deppisch
 * @since 2.7.6
 */
public class ChannelEndpointBuilder extends AbstractEndpointBuilder<ChannelEndpoint> {

    /** Endpoint target */
    private ChannelEndpoint endpoint = new ChannelEndpoint();

    @Override
    protected ChannelEndpoint getEndpoint() {
        return endpoint;
    }

    /**
     * Sets the channelName property.
     * @param channelName
     * @return
     */
    public ChannelEndpointBuilder channel(String channelName) {
        endpoint.getEndpointConfiguration().setChannelName(channelName);
        return this;
    }

    /**
     * Sets the channel property.
     * @param channel
     * @return
     */
    public ChannelEndpointBuilder channel(MessageChannel channel) {
        endpoint.getEndpointConfiguration().setChannel(channel);
        return this;
    }

    /**
     * Sets the messagingTemplate property.
     * @param messagingTemplate
     * @return
     */
    public ChannelEndpointBuilder messagingTemplate(MessagingTemplate messagingTemplate) {
        endpoint.getEndpointConfiguration().setMessagingTemplate(messagingTemplate);
        return this;
    }

    /**
     * Sets the messageConverter property.
     * @param messageConverter
     * @return
     */
    public ChannelEndpointBuilder messageConverter(ChannelMessageConverter messageConverter) {
        endpoint.getEndpointConfiguration().setMessageConverter(messageConverter);
        return this;
    }

    /**
     * Sets the channel resolver.
     * @param resolver
     * @return
     */
    public ChannelEndpointBuilder channelResolver(DestinationResolver resolver) {
        endpoint.getEndpointConfiguration().setChannelResolver(resolver);
        return this;
    }

    /**
     * Sets the useObjectMessages property.
     * @param useObjectMessages
     * @return
     */
    public ChannelEndpointBuilder useObjectMessages(boolean useObjectMessages) {
        endpoint.getEndpointConfiguration().setUseObjectMessages(useObjectMessages);
        return this;
    }

    /**
     * Sets the filterInternalHeaders property.
     * @param filterInternalHeaders
     * @return
     */
    public ChannelEndpointBuilder filterInternalHeaders(boolean filterInternalHeaders) {
        endpoint.getEndpointConfiguration().setFilterInternalHeaders(filterInternalHeaders);
        return this;
    }

    /**
     * Sets the default timeout.
     * @param timeout
     * @return
     */
    public ChannelEndpointBuilder timeout(long timeout) {
        endpoint.getEndpointConfiguration().setTimeout(timeout);
        return this;
    }
}
