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
import org.citrusframework.message.MessageCorrelator;
import org.springframework.integration.core.MessagingTemplate;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.core.DestinationResolver;

/**
 * @author Christoph Deppisch
 * @since 2.7.6
 */
public class ChannelSyncEndpointBuilder extends AbstractEndpointBuilder<ChannelSyncEndpoint> {

    /** Endpoint target */
    private ChannelSyncEndpoint endpoint = new ChannelSyncEndpoint();

    @Override
    protected ChannelSyncEndpoint getEndpoint() {
        return endpoint;
    }

    /**
     * Sets the channelName property.
     * @param channelName
     * @return
     */
    public ChannelSyncEndpointBuilder channel(String channelName) {
        endpoint.getEndpointConfiguration().setChannelName(channelName);
        return this;
    }

    /**
     * Sets the channel property.
     * @param channel
     * @return
     */
    public ChannelSyncEndpointBuilder channel(MessageChannel channel) {
        endpoint.getEndpointConfiguration().setChannel(channel);
        return this;
    }

    /**
     * Sets the messagingTemplate property.
     * @param messagingTemplate
     * @return
     */
    public ChannelSyncEndpointBuilder messagingTemplate(MessagingTemplate messagingTemplate) {
        endpoint.getEndpointConfiguration().setMessagingTemplate(messagingTemplate);
        return this;
    }

    /**
     * Sets the messageConverter property.
     * @param messageConverter
     * @return
     */
    public ChannelSyncEndpointBuilder messageConverter(ChannelMessageConverter messageConverter) {
        endpoint.getEndpointConfiguration().setMessageConverter(messageConverter);
        return this;
    }

    /**
     * Sets the channel resolver.
     * @param resolver
     * @return
     */
    public ChannelSyncEndpointBuilder channelResolver(DestinationResolver resolver) {
        endpoint.getEndpointConfiguration().setChannelResolver(resolver);
        return this;
    }

    /**
     * Sets the useObjectMessages property.
     * @param useObjectMessages
     * @return
     */
    public ChannelSyncEndpointBuilder useObjectMessages(boolean useObjectMessages) {
        endpoint.getEndpointConfiguration().setUseObjectMessages(useObjectMessages);
        return this;
    }

    /**
     * Sets the filterInternalHeaders property.
     * @param filterInternalHeaders
     * @return
     */
    public ChannelSyncEndpointBuilder filterInternalHeaders(boolean filterInternalHeaders) {
        endpoint.getEndpointConfiguration().setFilterInternalHeaders(filterInternalHeaders);
        return this;
    }

    /**
     * Sets the polling interval.
     * @param pollingInterval
     * @return
     */
    public ChannelSyncEndpointBuilder pollingInterval(int pollingInterval) {
        endpoint.getEndpointConfiguration().setPollingInterval(pollingInterval);
        return this;
    }

    /**
     * Sets the message correlator.
     * @param correlator
     * @return
     */
    public ChannelSyncEndpointBuilder correlator(MessageCorrelator correlator) {
        endpoint.getEndpointConfiguration().setCorrelator(correlator);
        return this;
    }

    /**
     * Sets the default timeout.
     * @param timeout
     * @return
     */
    public ChannelSyncEndpointBuilder timeout(long timeout) {
        endpoint.getEndpointConfiguration().setTimeout(timeout);
        return this;
    }
}
