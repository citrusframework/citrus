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

package org.citrusframework.vertx.endpoint;

import org.citrusframework.endpoint.AbstractEndpointBuilder;
import org.citrusframework.vertx.factory.VertxInstanceFactory;
import org.citrusframework.vertx.message.VertxMessageConverter;

/**
 * @author Christoph Deppisch
 * @since 2.5
 */
public class VertxEndpointBuilder extends AbstractEndpointBuilder<VertxEndpoint> {

    /** Endpoint target */
    private VertxEndpoint endpoint = new VertxEndpoint();

    @Override
    protected VertxEndpoint getEndpoint() {
        return endpoint;
    }

    /**
     * Sets the host property.
     * @param host
     * @return
     */
    public VertxEndpointBuilder host(String host) {
        endpoint.getEndpointConfiguration().setHost(host);
        return this;
    }

    /**
     * Sets the port property.
     * @param port
     * @return
     */
    public VertxEndpointBuilder port(int port) {
        endpoint.getEndpointConfiguration().setPort(port);
        return this;
    }

    /**
     * Sets the destinationName property.
     * @param address
     * @return
     */
    public VertxEndpointBuilder address(String address) {
        endpoint.getEndpointConfiguration().setAddress(address);
        return this;
    }

    /**
     * Sets the vertxFactory property.
     * @param vertxFactory
     * @return
     */
    public VertxEndpointBuilder vertxFactory(VertxInstanceFactory vertxFactory) {
        endpoint.setVertxInstanceFactory(vertxFactory);
        return this;
    }

    /**
     * Sets the messageConverter property.
     * @param messageConverter
     * @return
     */
    public VertxEndpointBuilder messageConverter(VertxMessageConverter messageConverter) {
        endpoint.getEndpointConfiguration().setMessageConverter(messageConverter);
        return this;
    }

    /**
     * Sets the pubSubDomain property.
     * @param pubSubDomain
     * @return
     */
    public VertxEndpointBuilder pubSubDomain(boolean pubSubDomain) {
        endpoint.getEndpointConfiguration().setPubSubDomain(pubSubDomain);
        return this;
    }

    /**
     * Sets the polling interval.
     * @param pollingInterval
     * @return
     */
    public VertxEndpointBuilder pollingInterval(int pollingInterval) {
        endpoint.getEndpointConfiguration().setPollingInterval(pollingInterval);
        return this;
    }

    /**
     * Sets the default timeout.
     * @param timeout
     * @return
     */
    public VertxEndpointBuilder timeout(long timeout) {
        endpoint.getEndpointConfiguration().setTimeout(timeout);
        return this;
    }
}
