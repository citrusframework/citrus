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

package com.consol.citrus.rmi.server;

import com.consol.citrus.endpoint.AbstractEndpointBuilder;
import com.consol.citrus.message.MessageCorrelator;
import com.consol.citrus.rmi.message.RmiMessageConverter;

import java.rmi.Remote;
import java.util.Arrays;

/**
 * @author Christoph Deppisch
 * @since 2.5
 */
public class RmiServerBuilder  extends AbstractEndpointBuilder<RmiServer> {

    /** Endpoint target */
    private RmiServer endpoint = new RmiServer();

    @Override
    protected RmiServer getEndpoint() {
        return endpoint;
    }

    /**
     * Sets the serverUrl property.
     * @param serverUrl
     * @return
     */
    public RmiServerBuilder serverUrl(String serverUrl) {
        endpoint.getEndpointConfiguration().setServerUrl(serverUrl);
        return this;
    }

    /**
     * Sets the host property.
     * @param host
     * @return
     */
    public RmiServerBuilder host(String host) {
        endpoint.getEndpointConfiguration().setHost(host);
        return this;
    }

    /**
     * Sets the port property.
     * @param port
     * @return
     */
    public RmiServerBuilder port(int port) {
        endpoint.getEndpointConfiguration().setPort(port);
        return this;
    }

    /**
     * Sets the binding property.
     * @param binding
     * @return
     */
    public RmiServerBuilder binding(String binding) {
        endpoint.getEndpointConfiguration().setBinding(binding);
        return this;
    }

    /**
     * Sets the createRegistry property.
     * @param createRegistry
     * @return
     */
    public RmiServerBuilder createRegistry(boolean createRegistry) {
        endpoint.setCreateRegistry(createRegistry);
        return this;
    }

    /**
     * Sets the remote interfaces property.
     * @param remoteInterfaces
     * @return
     */
    public RmiServerBuilder remoteInterfaces(Class<? extends Remote> ... remoteInterfaces) {
        endpoint.setRemoteInterfaces(Arrays.asList(remoteInterfaces));
        return this;
    }

    /**
     * Sets the message converter.
     * @param messageConverter
     * @return
     */
    public RmiServerBuilder messageConverter(RmiMessageConverter messageConverter) {
        endpoint.getEndpointConfiguration().setMessageConverter(messageConverter);
        return this;
    }

    /**
     * Sets the message correlator.
     * @param correlator
     * @return
     */
    public RmiServerBuilder correlator(MessageCorrelator correlator) {
        endpoint.getEndpointConfiguration().setCorrelator(correlator);
        return this;
    }

    /**
     * Sets the polling interval.
     * @param pollingInterval
     * @return
     */
    public RmiServerBuilder pollingInterval(int pollingInterval) {
        endpoint.getEndpointConfiguration().setPollingInterval(pollingInterval);
        return this;
    }

    /**
     * Sets the default timeout.
     * @param timeout
     * @return
     */
    public RmiServerBuilder timeout(long timeout) {
        endpoint.getEndpointConfiguration().setTimeout(timeout);
        return this;
    }

}
