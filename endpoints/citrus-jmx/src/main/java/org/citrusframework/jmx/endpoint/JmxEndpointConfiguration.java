/*
 * Copyright 2006-2015 the original author or authors.
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

package org.citrusframework.jmx.endpoint;

import java.rmi.registry.Registry;
import java.util.HashMap;
import java.util.Map;
import javax.management.NotificationFilter;

import org.citrusframework.endpoint.AbstractPollableEndpointConfiguration;
import org.citrusframework.jmx.message.JmxMessageConverter;
import org.citrusframework.jmx.model.JmxMarshaller;
import org.citrusframework.message.DefaultMessageCorrelator;
import org.citrusframework.message.MessageCorrelator;
import org.citrusframework.spi.ReferenceResolver;
import org.citrusframework.spi.ReferenceResolverAware;
import org.citrusframework.util.StringUtils;

/**
 * @author Christoph Deppisch
 * @since 2.5
 */
public class JmxEndpointConfiguration extends AbstractPollableEndpointConfiguration implements ReferenceResolverAware {

    /** MBean server url, by default connect to platform MBean server */
    private String serverUrl;

    /** Host, port and protocol information constructing proper server url */
    private String protocol = "rmi";
    private String host = "localhost";
    private int port = Registry.REGISTRY_PORT;
    private String binding;

    /** User credentials */
    private String username;
    private String password;

    /** Should reconnect on connection lost */
    private boolean autoReconnect = false;

    /** Wait when reconnecting */
    private long delayOnReconnect = 1000L;

    /** Optional notification filter */
    private NotificationFilter notificationFilter;

    /** Optional notification handback */
    private Object notificationHandback;

    /** Marshaller converts from XML to JMX mbean invocation objects */
    private JmxMarshaller marshaller = new JmxMarshaller();

    /** Message converter */
    private JmxMessageConverter messageConverter = new JmxMessageConverter();

    /** Reply message correlator */
    private MessageCorrelator correlator = new DefaultMessageCorrelator();

    /** JMX server environment properties */
    private Map<String, Object> environmentProperties = new HashMap<>();

    /** Reference resolver used for method arg object reference evaluation */
    private ReferenceResolver referenceResolver;

    /**
     * Gets the value of the protocol property.
     *
     * @return the protocol
     */
    public String getProtocol() {
        return protocol;
    }

    /**
     * Sets the protocol property.
     *
     * @param protocol
     */
    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    /**
     * Gets the value of the host property.
     *
     * @return the host
     */
    public String getHost() {
        return host;
    }

    /**
     * Sets the host property.
     *
     * @param host
     */
    public void setHost(String host) {
        this.host = host;
    }

    /**
     * Gets the value of the port property.
     *
     * @return the port
     */
    public int getPort() {
        return port;
    }

    /**
     * Sets the port property.
     *
     * @param port
     */
    public void setPort(int port) {
        this.port = port;
    }

    /**
     * Gets the value of the binding property.
     *
     * @return the binding
     */
    public String getBinding() {
        return binding;
    }

    /**
     * Sets the binding property.
     *
     * @param binding
     */
    public void setBinding(String binding) {
        this.binding = binding;
    }

    /**
     * Gets the value of the serverUrl property.
     *
     * @return the serverUrl
     */
    public String getServerUrl() {
        if (StringUtils.hasText(this.serverUrl)) {
            return serverUrl;
        } else {
            return "service:jmx:" + protocol + ":///jndi/" + protocol + "://" + host + ":" + port + (binding != null ? "/" + binding : "");
        }
    }

    /**
     * Sets the serverUrl property.
     *
     * @param serverUrl
     */
    public void setServerUrl(String serverUrl) {
        this.serverUrl = serverUrl;
    }

    /**
     * Gets the value of the username property.
     *
     * @return the username
     */
    public String getUsername() {
        return username;
    }

    /**
     * Sets the username property.
     *
     * @param username
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Gets the value of the password property.
     *
     * @return the password
     */
    public String getPassword() {
        return password;
    }

    /**
     * Sets the password property.
     *
     * @param password
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Gets the value of the autoReconnect property.
     *
     * @return the autoReconnect
     */
    public boolean isAutoReconnect() {
        return autoReconnect;
    }

    /**
     * Sets the autoReconnect property.
     *
     * @param autoReconnect
     */
    public void setAutoReconnect(boolean autoReconnect) {
        this.autoReconnect = autoReconnect;
    }

    /**
     * Gets the value of the delayOnReconnect property.
     *
     * @return the delayOnReconnect
     */
    public long getDelayOnReconnect() {
        return delayOnReconnect;
    }

    /**
     * Sets the delayOnReconnect property.
     *
     * @param delayOnReconnect
     */
    public void setDelayOnReconnect(long delayOnReconnect) {
        this.delayOnReconnect = delayOnReconnect;
    }

    /**
     * Gets the value of the notificationFilter property.
     *
     * @return the notificationFilter
     */
    public NotificationFilter getNotificationFilter() {
        return notificationFilter;
    }

    /**
     * Sets the notificationFilter property.
     *
     * @param notificationFilter
     */
    public void setNotificationFilter(NotificationFilter notificationFilter) {
        this.notificationFilter = notificationFilter;
    }

    /**
     * Gets the value of the notificationHandback property.
     *
     * @return the notificationHandback
     */
    public Object getNotificationHandback() {
        return notificationHandback;
    }

    /**
     * Sets the notificationHandback property.
     *
     * @param notificationHandback
     */
    public void setNotificationHandback(Object notificationHandback) {
        this.notificationHandback = notificationHandback;
    }

    /**
     * Gets the value of the marshaller property.
     *
     * @return the marshaller
     */
    public JmxMarshaller getMarshaller() {
        return marshaller;
    }

    /**
     * Sets the marshaller property.
     *
     * @param marshaller
     */
    public void setMarshaller(JmxMarshaller marshaller) {
        this.marshaller = marshaller;
    }

    /**
     * Set the reply message correlator.
     * @param correlator the correlator to set
     */
    public void setCorrelator(MessageCorrelator correlator) {
        this.correlator = correlator;
    }

    /**
     * Gets the correlator.
     * @return the correlator
     */
    public MessageCorrelator getCorrelator() {
        return correlator;
    }

    /**
     * Gets the message converter.
     * @return
     */
    public JmxMessageConverter getMessageConverter() {
        return messageConverter;
    }

    /**
     * Sets the message converter.
     * @param messageConverter
     */
    public void setMessageConverter(JmxMessageConverter messageConverter) {
        this.messageConverter = messageConverter;
    }

    /**
     * Gets the value of the environmentProperties property.
     *
     * @return the environmentProperties
     */
    public Map<String, Object> getEnvironmentProperties() {
        return environmentProperties;
    }

    /**
     * Sets the environmentProperties property.
     *
     * @param environmentProperties
     */
    public void setEnvironmentProperties(Map<String, Object> environmentProperties) {
        this.environmentProperties = environmentProperties;
    }

    @Override
    public void setReferenceResolver(ReferenceResolver referenceResolver) {
        this.referenceResolver = referenceResolver;
    }

    public ReferenceResolver getReferenceResolver() {
        return referenceResolver;
    }
}
