package org.citrusframework.channel.endpoint.builder;

import org.citrusframework.channel.ChannelEndpointBuilder;
import org.citrusframework.channel.ChannelSyncEndpointBuilder;
import org.citrusframework.endpoint.builder.AsyncSyncEndpointBuilder;

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
