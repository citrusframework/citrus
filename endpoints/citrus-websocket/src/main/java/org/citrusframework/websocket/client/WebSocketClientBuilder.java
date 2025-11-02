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

package org.citrusframework.websocket.client;

import org.citrusframework.endpoint.AbstractEndpointBuilder;
import org.citrusframework.endpoint.resolver.EndpointUriResolver;
import org.citrusframework.util.StringUtils;
import org.citrusframework.websocket.message.WebSocketMessageConverter;
import org.citrusframework.yaml.SchemaProperty;

/**
 * @since 2.5
 */
public class WebSocketClientBuilder extends AbstractEndpointBuilder<WebSocketClient> {

    /** Endpoint target */
    private final WebSocketClient endpoint = new WebSocketClient();

    private String messageConverter;
    private String endpointResolver;

    @Override
    public WebSocketClient build() {
        if (referenceResolver != null) {
            if (StringUtils.hasText(messageConverter)) {
                messageConverter(referenceResolver.resolve(messageConverter, WebSocketMessageConverter.class));
            }

            if (StringUtils.hasText(endpointResolver)) {
                endpointResolver(referenceResolver.resolve(endpointResolver, EndpointUriResolver.class));
            }
        }

        return super.build();
    }

    @Override
    protected WebSocketClient getEndpoint() {
        return endpoint;
    }

    /**
     * Sets the requestUrl property.
     */
    public WebSocketClientBuilder requestUrl(String requestUrl) {
        endpoint.getEndpointConfiguration().setEndpointUri(requestUrl);
        return this;
    }

    @SchemaProperty(description = "Sets the client request URL.")
    public void setRequestUrl(String requestUrl) {
        requestUrl(requestUrl);
    }

    /**
     * Sets the message converter.
     */
    public WebSocketClientBuilder messageConverter(WebSocketMessageConverter messageConverter) {
        endpoint.getEndpointConfiguration().setMessageConverter(messageConverter);
        return this;
    }

    @SchemaProperty(advanced = true, description = "Bean reference to a message converter.")
    public void setMessageConverter(String messageConverter) {
        this.messageConverter = messageConverter;
    }

    /**
     * Sets the endpoint uri resolver.
     */
    public WebSocketClientBuilder endpointResolver(EndpointUriResolver resolver) {
        endpoint.getEndpointConfiguration().setEndpointUriResolver(resolver);
        return this;
    }

    @SchemaProperty(advanced = true, description = "Sets the endpoint URI resolver.")
    public void setEndpointResolver(String resolver) {
        this.endpointResolver = resolver;
    }

    /**
     * Sets the polling interval.
     */
    public WebSocketClientBuilder pollingInterval(int pollingInterval) {
        endpoint.getEndpointConfiguration().setPollingInterval(pollingInterval);
        return this;
    }

    @SchemaProperty(advanced = true, description = "Sets the polling interval when consuming messages.")
    public void setPollingInterval(int pollingInterval) {
        pollingInterval(pollingInterval);
    }

    /**
     * Sets the default timeout.
     */
    public WebSocketClientBuilder timeout(long timeout) {
        endpoint.getEndpointConfiguration().setTimeout(timeout);
        return this;
    }

    @SchemaProperty(description = "The Http request timeout while waiting for a response", defaultValue = "5000")
    public void setTimeout(long timeout) {
        timeout(timeout);
    }
}
