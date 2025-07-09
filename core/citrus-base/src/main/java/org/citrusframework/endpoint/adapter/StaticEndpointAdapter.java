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

package org.citrusframework.endpoint.adapter;

import org.citrusframework.endpoint.AbstractEndpointAdapter;
import org.citrusframework.endpoint.Endpoint;
import org.citrusframework.endpoint.EndpointConfiguration;
import org.citrusframework.endpoint.StaticEndpoint;
import org.citrusframework.message.Message;

/**
 * Static endpoint adapter always responds with static response message. No endpoint is provided as this is a
 * static endpoint adapter. Clients trying to get endpoint for interaction will receive runtime exception.
 *
 * @since 1.4
 */
public class StaticEndpointAdapter extends AbstractEndpointAdapter {

    private final StaticEndpoint endpoint;

    public StaticEndpointAdapter() {
        this.endpoint = new StaticEndpoint();
    }

    public StaticEndpointAdapter(Message message) {
        this.endpoint = new StaticEndpoint(message);
    }

    @Override
    protected Message handleMessageInternal(Message message) {
        return endpoint.getMessage();
    }

    @Override
    public Endpoint getEndpoint() {
        return endpoint;
    }

    @Override
    public EndpointConfiguration getEndpointConfiguration() {
        return endpoint.getEndpointConfiguration();
    }

    public StaticEndpointAdapter withReuseMessage(boolean reuseMessage) {
        this.endpoint.getEndpointConfiguration().setReuseMessage(reuseMessage);
        return this;
    }
}
