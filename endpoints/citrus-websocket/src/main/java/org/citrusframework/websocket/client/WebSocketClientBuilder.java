/*
 * Copyright 2006-2016 the original author or authors.
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

package org.citrusframework.websocket.client;

import org.citrusframework.endpoint.AbstractEndpointBuilder;
import org.citrusframework.endpoint.resolver.EndpointUriResolver;
import org.citrusframework.websocket.message.WebSocketMessageConverter;

/**
 * @author Christoph Deppisch
 * @since 2.5
 */
public class WebSocketClientBuilder extends AbstractEndpointBuilder<WebSocketClient> {

    /** Endpoint target */
    private WebSocketClient endpoint = new WebSocketClient();

    @Override
    protected WebSocketClient getEndpoint() {
        return endpoint;
    }

    /**
     * Sets the requestUrl property.
     * @param requestUrl
     * @return
     */
    public WebSocketClientBuilder requestUrl(String requestUrl) {
        endpoint.getEndpointConfiguration().setEndpointUri(requestUrl);
        return this;
    }

    /**
     * Sets the message converter.
     * @param messageConverter
     * @return
     */
    public WebSocketClientBuilder messageConverter(WebSocketMessageConverter messageConverter) {
        endpoint.getEndpointConfiguration().setMessageConverter(messageConverter);
        return this;
    }

    /**
     * Sets the endpoint uri resolver.
     * @param resolver
     * @return
     */
    public WebSocketClientBuilder endpointResolver(EndpointUriResolver resolver) {
        endpoint.getEndpointConfiguration().setEndpointUriResolver(resolver);
        return this;
    }

    /**
     * Sets the polling interval.
     * @param pollingInterval
     * @return
     */
    public WebSocketClientBuilder pollingInterval(int pollingInterval) {
        endpoint.getEndpointConfiguration().setPollingInterval(pollingInterval);
        return this;
    }

    /**
     * Sets the default timeout.
     * @param timeout
     * @return
     */
    public WebSocketClientBuilder timeout(long timeout) {
        endpoint.getEndpointConfiguration().setTimeout(timeout);
        return this;
    }

}
