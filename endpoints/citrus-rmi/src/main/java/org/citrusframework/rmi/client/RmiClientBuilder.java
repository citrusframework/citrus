/*
 *  Copyright 2006-2016 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.citrusframework.rmi.client;

import org.citrusframework.endpoint.AbstractEndpointBuilder;
import org.citrusframework.message.MessageCorrelator;
import org.citrusframework.rmi.message.RmiMessageConverter;

/**
 * @author Christoph Deppisch
 * @since 2.5
 */
public class RmiClientBuilder extends AbstractEndpointBuilder<RmiClient> {

    /** Endpoint target */
    private RmiClient endpoint = new RmiClient();

    @Override
    protected RmiClient getEndpoint() {
        return endpoint;
    }

    /**
     * Sets the serverUrl property.
     * @param serverUrl
     * @return
     */
    public RmiClientBuilder serverUrl(String serverUrl) {
        endpoint.getEndpointConfiguration().setServerUrl(serverUrl);
        return this;
    }

    /**
     * Sets the host property.
     * @param host
     * @return
     */
    public RmiClientBuilder host(String host) {
        endpoint.getEndpointConfiguration().setHost(host);
        return this;
    }

    /**
     * Sets the port property.
     * @param port
     * @return
     */
    public RmiClientBuilder port(int port) {
        endpoint.getEndpointConfiguration().setPort(port);
        return this;
    }

    /**
     * Sets the binding property.
     * @param binding
     * @return
     */
    public RmiClientBuilder binding(String binding) {
        endpoint.getEndpointConfiguration().setBinding(binding);
        return this;
    }

    /**
     * Sets the method property.
     * @param method
     * @return
     */
    public RmiClientBuilder method(String method) {
        endpoint.getEndpointConfiguration().setMethod(method);
        return this;
    }

    /**
     * Sets the message converter.
     * @param messageConverter
     * @return
     */
    public RmiClientBuilder messageConverter(RmiMessageConverter messageConverter) {
        endpoint.getEndpointConfiguration().setMessageConverter(messageConverter);
        return this;
    }

    /**
     * Sets the message correlator.
     * @param correlator
     * @return
     */
    public RmiClientBuilder correlator(MessageCorrelator correlator) {
        endpoint.getEndpointConfiguration().setCorrelator(correlator);
        return this;
    }

    /**
     * Sets the polling interval.
     * @param pollingInterval
     * @return
     */
    public RmiClientBuilder pollingInterval(int pollingInterval) {
        endpoint.getEndpointConfiguration().setPollingInterval(pollingInterval);
        return this;
    }

    /**
     * Sets the default timeout.
     * @param timeout
     * @return
     */
    public RmiClientBuilder timeout(long timeout) {
        endpoint.getEndpointConfiguration().setTimeout(timeout);
        return this;
    }
}
