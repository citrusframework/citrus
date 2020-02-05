package com.consol.citrus.channel.endpoint.builder;

import com.consol.citrus.channel.ChannelEndpointBuilder;
import com.consol.citrus.channel.ChannelSyncEndpointBuilder;
import com.consol.citrus.endpoint.builder.AsyncSyncEndpointBuilder;

/**
 * @author Christoph Deppisch
 */
public final class MessageChannelEndpoints extends AsyncSyncEndpointBuilder<ChannelEndpointBuilder, ChannelSyncEndpointBuilder> {

    /**
     * Private constructor setting the sync and async builder implementation.
     */
    private MessageChannelEndpoints() {
        super(new ChannelEndpointBuilder(), new ChannelSyncEndpointBuilder());
    }

    /**
     * Static entry method for channel endpoint builders.
     * @return
     */
    public static MessageChannelEndpoints channel() {
        return new MessageChannelEndpoints();
    }
}
