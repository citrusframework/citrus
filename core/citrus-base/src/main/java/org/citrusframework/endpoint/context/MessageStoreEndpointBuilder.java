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

package org.citrusframework.endpoint.context;

import org.citrusframework.endpoint.AbstractEndpointBuilder;
import org.citrusframework.yaml.SchemaProperty;

public class MessageStoreEndpointBuilder extends AbstractEndpointBuilder<MessageStoreEndpoint> {

    /** Endpoint target */
    private final MessageStoreEndpoint endpoint = new MessageStoreEndpoint();

    @Override
    protected MessageStoreEndpoint getEndpoint() {
        return endpoint;
    }

    /**
     * Sets the messageName property.
     */
    public MessageStoreEndpointBuilder messageName(String messageName) {
        endpoint.getEndpointConfiguration().setMessageName(messageName);
        return this;
    }

    @SchemaProperty(description = "The message name.")
    public void setMessageName(String messageName) {
        messageName(messageName);
    }

    /**
     * Sets the default timeout.
     */
    public MessageStoreEndpointBuilder timeout(long timeout) {
        endpoint.getEndpointConfiguration().setTimeout(timeout);
        return this;
    }

    @SchemaProperty(description = "The timeout when receiving messages from the message store.")
    public void setTimeout(long timeout) {
        timeout(timeout);
    }
}
