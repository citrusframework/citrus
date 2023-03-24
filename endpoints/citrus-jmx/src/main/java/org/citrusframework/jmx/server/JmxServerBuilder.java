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

package org.citrusframework.jmx.server;

import javax.management.NotificationFilter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.citrusframework.jmx.message.JmxMessageConverter;
import org.citrusframework.jmx.model.JmxMarshaller;
import org.citrusframework.jmx.model.ManagedBeanDefinition;
import org.citrusframework.message.MessageCorrelator;
import org.citrusframework.server.AbstractServerBuilder;

/**
 * @author Christoph Deppisch
 * @since 2.5
 */
public class JmxServerBuilder extends AbstractServerBuilder<JmxServer, JmxServerBuilder> {

    /** Endpoint target */
    private final JmxServer endpoint = new JmxServer();

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
     * Sets the user property.
     * @param user
     * @return
     */
    public JmxServerBuilder username(String user) {
        endpoint.getEndpointConfiguration().setUsername(user);
        return this;
    }

    /**
     * Sets the password property.
     * @param password
     * @return
     */
    public JmxServerBuilder password(String password) {
        endpoint.getEndpointConfiguration().setPassword(password);
        return this;
    }

    /**
     * Sets the autoReconnect property.
     * @param autoReconnect
     * @return
     */
    public JmxServerBuilder autoReconnect(boolean autoReconnect) {
        endpoint.getEndpointConfiguration().setAutoReconnect(autoReconnect);
        return this;
    }

    /**
     * Sets the delayOnReconnect property.
     * @param delayOnReconnect
     * @return
     */
    public JmxServerBuilder delayOnReconnect(long delayOnReconnect) {
        endpoint.getEndpointConfiguration().setDelayOnReconnect(delayOnReconnect);
        return this;
    }

    /**
     * Sets the notificationFilter property.
     * @param notificationFilter
     * @return
     */
    public JmxServerBuilder notificationFilter(NotificationFilter notificationFilter) {
        endpoint.getEndpointConfiguration().setNotificationFilter(notificationFilter);
        return this;
    }

    /**
     * Sets the notificationHandback property.
     * @param notificationHandback
     * @return
     */
    public JmxServerBuilder notificationHandback(Object notificationHandback) {
        endpoint.getEndpointConfiguration().setNotificationHandback(notificationHandback);
        return this;
    }

    /**
     * Sets the marshaller property.
     * @param marshaller
     * @return
     */
    public JmxServerBuilder marshaller(JmxMarshaller marshaller) {
        endpoint.getEndpointConfiguration().setMarshaller(marshaller);
        return this;
    }

    /**
     * Sets the correlator property.
     * @param correlator
     * @return
     */
    public JmxServerBuilder correlator(MessageCorrelator correlator) {
        endpoint.getEndpointConfiguration().setCorrelator(correlator);
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
    public JmxServerBuilder environmentProperties(Map<String, Object> environmentProperties) {
        endpoint.getEndpointConfiguration().setEnvironmentProperties(environmentProperties);
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
     * Sets the message converter.
     * @param messageConverter
     * @return
     */
    public JmxServerBuilder messageConverter(JmxMessageConverter messageConverter) {
        endpoint.getEndpointConfiguration().setMessageConverter(messageConverter);
        return this;
    }
}
