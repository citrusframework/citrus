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

package org.citrusframework.channel.endpoint.builder;

import org.citrusframework.channel.ChannelEndpointBuilder;
import org.citrusframework.channel.ChannelSyncEndpointBuilder;
import org.citrusframework.endpoint.builder.AsyncSyncEndpointBuilder;

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
