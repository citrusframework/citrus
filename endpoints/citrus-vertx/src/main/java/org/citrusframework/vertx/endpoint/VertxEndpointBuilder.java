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

package org.citrusframework.vertx.endpoint;

import org.citrusframework.endpoint.AbstractEndpointBuilder;
import org.citrusframework.util.StringUtils;
import org.citrusframework.vertx.factory.VertxInstanceFactory;
import org.citrusframework.vertx.message.VertxMessageConverter;
import org.citrusframework.yaml.SchemaProperty;

/**
 * @since 2.5
 */
public class VertxEndpointBuilder extends AbstractEndpointBuilder<VertxEndpoint> {

    /** Endpoint target */
    private final VertxEndpoint endpoint = new VertxEndpoint();

    private String vertxFactory;
    private String messageConverter;

    @Override
    public VertxEndpoint build() {
        if (referenceResolver != null) {
            if (StringUtils.hasText(vertxFactory)) {
                vertxFactory(referenceResolver.resolve(vertxFactory, VertxInstanceFactory.class));
            }

            if (StringUtils.hasText(messageConverter)) {
                messageConverter(referenceResolver.resolve(messageConverter, VertxMessageConverter.class));
            }
        }

        return super.build();
    }

    @Override
    protected VertxEndpoint getEndpoint() {
        return endpoint;
    }

    /**
     * Sets the host property.
     */
    public VertxEndpointBuilder host(String host) {
        endpoint.getEndpointConfiguration().setHost(host);
        return this;
    }

    @SchemaProperty(description = "The Vert.x event bus host.")
    public void setHost(String host) {
        host(host);
    }

    /**
     * Sets the port property.
     */
    public VertxEndpointBuilder port(int port) {
        endpoint.getEndpointConfiguration().setPort(port);
        return this;
    }

    @SchemaProperty(description = "The Vert.x event bus port.")
    public void setPort(int port) {
        port(port);
    }

    /**
     * Sets the address property.
     */
    public VertxEndpointBuilder address(String address) {
        endpoint.getEndpointConfiguration().setAddress(address);
        return this;
    }

    @SchemaProperty(description = "The event bus address.")
    public void setAddress(String address) {
        address(address);
    }

    /**
     * Sets the vertxFactory property.
     */
    public VertxEndpointBuilder vertxFactory(VertxInstanceFactory vertxFactory) {
        endpoint.setVertxInstanceFactory(vertxFactory);
        return this;
    }

    @SchemaProperty(advanced = true, description = "Sets a custom Vert.x factory.")
    public void setVertxFactory(String vertxFactory) {
        this.vertxFactory = vertxFactory;
    }

    /**
     * Sets the messageConverter property.
     */
    public VertxEndpointBuilder messageConverter(VertxMessageConverter messageConverter) {
        endpoint.getEndpointConfiguration().setMessageConverter(messageConverter);
        return this;
    }

    @SchemaProperty(advanced = true, description = "Sets the message converter as a bean reference.")
    public void setMessageConverter(String messageConverter) {
        this.messageConverter = messageConverter;
    }

    /**
     * Sets the pubSubDomain property.
     */
    public VertxEndpointBuilder pubSubDomain(boolean pubSubDomain) {
        endpoint.getEndpointConfiguration().setPubSubDomain(pubSubDomain);
        return this;
    }

    @SchemaProperty(advanced = true, description = "When enabled the endpoint uses publish/subscribe mode.")
    public void setPubSubDomain(boolean pubSubDomain) {
        pubSubDomain(pubSubDomain);
    }

    /**
     * Sets the polling interval.
     */
    public VertxEndpointBuilder pollingInterval(int pollingInterval) {
        endpoint.getEndpointConfiguration().setPollingInterval(pollingInterval);
        return this;
    }

    @SchemaProperty(description = "Sets the polling interval when consuming messages.")
    public void setPollingInterval(int pollingInterval) {
        pollingInterval(pollingInterval);
    }

    /**
     * Sets the default timeout.
     */
    public VertxEndpointBuilder timeout(long timeout) {
        endpoint.getEndpointConfiguration().setTimeout(timeout);
        return this;
    }

    @SchemaProperty(description = "The endpoint timeout when waiting for messages.")
    public void setTimeout(long timeout) {
        timeout(timeout);
    }
}
