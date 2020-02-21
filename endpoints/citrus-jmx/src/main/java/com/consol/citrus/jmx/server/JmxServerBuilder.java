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

package com.consol.citrus.jmx.server;

import com.consol.citrus.endpoint.AbstractEndpointBuilder;
import com.consol.citrus.jmx.message.JmxMessageConverter;
import com.consol.citrus.jmx.model.ManagedBeanDefinition;

import java.util.*;

/**
 * @author Christoph Deppisch
 * @since 2.5
 */
public class JmxServerBuilder extends AbstractEndpointBuilder<JmxServer> {

    /** Endpoint target */
    private JmxServer endpoint = new JmxServer();

    @Override
    protected JmxServer getEndpoint() {
        return endpoint;
    }

    /**
     * Sets the serverUrl property.
     * @param serverUrl
     * @return
     */
    public JmxServerBuilder serverUrl(String serverUrl) {
        endpoint.getEndpointConfiguration().setServerUrl(serverUrl);
        return this;
    }

    /**
     * Sets the host property.
     * @param host
     * @return
     */
    public JmxServerBuilder host(String host) {
        endpoint.getEndpointConfiguration().setHost(host);
        return this;
    }

    /**
     * Sets the port property.
     * @param port
     * @return
     */
    public JmxServerBuilder port(int port) {
        endpoint.getEndpointConfiguration().setPort(port);
        return this;
    }

    /**
     * Sets the binding property.
     * @param binding
     * @return
     */
    public JmxServerBuilder binding(String binding) {
        endpoint.getEndpointConfiguration().setBinding(binding);
        return this;
    }

    /**
     * Sets the protocol property.
     * @param protocol
     * @return
     */
    public JmxServerBuilder protocol(String protocol) {
        endpoint.getEndpointConfiguration().setProtocol(protocol);
        return this;
    }

    /**
     * Sets the createRegistry property.
     * @param createRegistry
     * @return
     */
    public JmxServerBuilder createRegistry(boolean createRegistry) {
        endpoint.setCreateRegistry(createRegistry);
        return this;
    }

    /**
     * Sets the environment properties.
     * @param environmentProperties
     * @return
     */
    public JmxServerBuilder environmentProperties(Properties environmentProperties) {
        HashMap<String, Object> properties = new HashMap<>(environmentProperties.size());
        for (Map.Entry<Object, Object> entry : environmentProperties.entrySet()) {
            properties.put(entry.getKey().toString(), entry.getValue());
        }

        endpoint.getEndpointConfiguration().setEnvironmentProperties(properties);
        return this;
    }

    /**
     * Sets the Mbean definitions property.
     * @param mbeans
     * @return
     */
    public JmxServerBuilder mbeans(List<ManagedBeanDefinition> mbeans) {
        endpoint.setMbeans(mbeans);
        return this;
    }

    /**
     * Sets the autoStart property.
     * @param autoStart
     * @return
     */
    public JmxServerBuilder autoStart(boolean autoStart) {
        endpoint.setAutoStart(autoStart);
        return this;
    }

    /**
     * Sets the message converter.
     * @param messageConverter
     * @return
     */
    public JmxServerBuilder messageConverter(JmxMessageConverter messageConverter) {
        endpoint.getEndpointConfiguration().setMessageConverter(messageConverter);
        return this;
    }

    /**
     * Sets the default timeout.
     * @param timeout
     * @return
     */
    public JmxServerBuilder timeout(long timeout) {
        endpoint.setDefaultTimeout(timeout);
        endpoint.getEndpointConfiguration().setTimeout(timeout);
        return this;
    }
}
