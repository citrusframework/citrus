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

package org.citrusframework.jmx.server;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;
import javax.management.NotificationFilter;

import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlTransient;
import jakarta.xml.bind.annotation.XmlType;
import org.citrusframework.jmx.message.JmxMessageConverter;
import org.citrusframework.jmx.model.JmxMarshaller;
import org.citrusframework.jmx.model.ManagedBeanDefinition;
import org.citrusframework.message.MessageCorrelator;
import org.citrusframework.server.AbstractServerBuilder;
import org.citrusframework.util.StringUtils;
import org.citrusframework.yaml.SchemaProperty;
import org.citrusframework.yaml.SchemaType;

/**
 * @since 2.5
 */
@SchemaType(module = "citrus-jmx")
@XmlType(name = "", propOrder = {})
public class JmxServerBuilder extends AbstractServerBuilder<JmxServer, JmxServerBuilder> {

    /** Endpoint target */
    private final JmxServer endpoint = new JmxServer();

    private String notificationFilter;
    private String messageConverter;
    private String correlator;
    private String marshaller;
    private String notificationHandback;
    private String mbeans;

    @Override
    public JmxServer build() {
        if (referenceResolver != null) {
            if (StringUtils.hasText(messageConverter)) {
                messageConverter(referenceResolver.resolve(messageConverter, JmxMessageConverter.class));
            }

            if (StringUtils.hasText(correlator)) {
                correlator(referenceResolver.resolve(correlator, MessageCorrelator.class));
            }

            if (StringUtils.hasText(marshaller)) {
                marshaller(referenceResolver.resolve(marshaller, JmxMarshaller.class));
            }

            if (StringUtils.hasText(notificationHandback)) {
                notificationHandback(referenceResolver.resolve(notificationHandback, Object.class));
            }

            if (StringUtils.hasText(notificationFilter)) {
                notificationFilter(referenceResolver.resolve(notificationFilter, NotificationFilter.class));
            }

            if (StringUtils.hasText(mbeans)) {
                Object mbeansObject = referenceResolver.resolve(mbeans, Object.class);
                if (mbeansObject instanceof List mbeansList) {
                    mbeans(mbeansList);
                } else if (mbeansObject instanceof ManagedBeanDefinition managedBeanDefinition) {
                    mbeans(Collections.singletonList(managedBeanDefinition));
                }
            }
        }

        return super.build();
    }

    @Override
    protected JmxServer getEndpoint() {
        return endpoint;
    }

    /**
     * Sets the serverUrl property.
     */
    public JmxServerBuilder serverUrl(String serverUrl) {
        endpoint.getEndpointConfiguration().setServerUrl(serverUrl);
        return this;
    }

    @SchemaProperty(description = "Sets the serverUrl property.")
    @XmlAttribute(name = "server-url")
    public void setServerUrl(String serverUrl) {
        serverUrl(serverUrl);
    }

    /**
     * Sets the host property.
     */
    public JmxServerBuilder host(String host) {
        endpoint.getEndpointConfiguration().setHost(host);
        return this;
    }

    @SchemaProperty(description = "Sets the host property.", defaultValue = "localhost")
    @XmlAttribute
    public void setHost(String host) {
        host(host);
    }

    /**
     * Sets the port property.
     */
    public JmxServerBuilder port(int port) {
        endpoint.getEndpointConfiguration().setPort(port);
        return this;
    }

    @SchemaProperty(description = "Sets the port property.")
    @XmlAttribute
    public void setPort(int port) {
        port(port);
    }

    /**
     * Sets the user property.
     */
    public JmxServerBuilder username(String user) {
        endpoint.getEndpointConfiguration().setUsername(user);
        return this;
    }

    @SchemaProperty(description = "Sets the username property.")
    @XmlAttribute
    public void setUsername(String user) {
        username(user);
    }

    /**
     * Sets the password property.
     */
    public JmxServerBuilder password(String password) {
        endpoint.getEndpointConfiguration().setPassword(password);
        return this;
    }

    @SchemaProperty(description = "Sets the user password property.")
    @XmlAttribute
    public void setPassword(String password) {
        password(password);
    }

    /**
     * Sets the autoReconnect property.
     */
    public JmxServerBuilder autoReconnect(boolean autoReconnect) {
        endpoint.getEndpointConfiguration().setAutoReconnect(autoReconnect);
        return this;
    }

    @SchemaProperty(description = "Sets the auto reconnect property.")
    @XmlAttribute(name = "auto-reconnect")
    public void setAutoReconnect(boolean autoReconnect) {
        autoReconnect(autoReconnect);
    }

    /**
     * Sets the delayOnReconnect property.
     */
    public JmxServerBuilder delayOnReconnect(long delayOnReconnect) {
        endpoint.getEndpointConfiguration().setDelayOnReconnect(delayOnReconnect);
        return this;
    }

    @SchemaProperty(description = "Sets the delay on reconnect property.")
    @XmlAttribute(name = "delay-on-reconnect")
    public void setDelayOnReconnect(long delayOnReconnect) {
        delayOnReconnect(delayOnReconnect);
    }

    /**
     * Sets the notificationFilter property.
     */
    public JmxServerBuilder notificationFilter(NotificationFilter notificationFilter) {
        endpoint.getEndpointConfiguration().setNotificationFilter(notificationFilter);
        return this;
    }

    @SchemaProperty(description = "Sets the notification filter as a bean reference.")
    @XmlAttribute(name = "notification-filter")
    public void setNotificationFilter(String notificationFilter) {
        this.notificationFilter = notificationFilter;
    }

    /**
     * Sets the notificationHandback property.
     */
    public JmxServerBuilder notificationHandback(Object notificationHandback) {
        endpoint.getEndpointConfiguration().setNotificationHandback(notificationHandback);
        return this;
    }

    @SchemaProperty(description = "Sets the notification handback.")
    @XmlAttribute(name = "notification-handback")
    public void setNotificationHandback(String notificationHandback) {
        this.notificationHandback = notificationHandback;
    }

    /**
     * Sets the marshaller property.
     */
    public JmxServerBuilder marshaller(JmxMarshaller marshaller) {
        endpoint.getEndpointConfiguration().setMarshaller(marshaller);
        return this;
    }

    @SchemaProperty
    @XmlAttribute
    public void setMarshaller(String marshaller) {
        this.marshaller = marshaller;
    }

    /**
     * Sets the correlator property.
     */
    public JmxServerBuilder correlator(MessageCorrelator correlator) {
        endpoint.getEndpointConfiguration().setCorrelator(correlator);
        return this;
    }

    @SchemaProperty(description = "Sets the message correlator.")
    @XmlAttribute(name = "message-correlator")
    public void setCorrelator(String correlator) {
        this.correlator = correlator;
    }

    /**
     * Sets the binding property.
     */
    public JmxServerBuilder binding(String binding) {
        endpoint.getEndpointConfiguration().setBinding(binding);
        return this;
    }

    @SchemaProperty(description = "Sets the binding property.")
    @XmlAttribute
    public void setBinding(String binding) {
        binding(binding);
    }

    /**
     * Sets the protocol property.
     */
    public JmxServerBuilder protocol(String protocol) {
        endpoint.getEndpointConfiguration().setProtocol(protocol);
        return this;
    }

    @SchemaProperty(description = "Sets the protocol.", defaultValue = "rmi")
    @XmlAttribute
    public void setProtocol(String protocol) {
        protocol(protocol);
    }

    /**
     * Sets the createRegistry property.
     */
    public JmxServerBuilder createRegistry(boolean createRegistry) {
        endpoint.setCreateRegistry(createRegistry);
        return this;
    }

    @SchemaProperty(description = "Sets the create registry property.")
    @XmlAttribute(name = "create-registry")
    public void setCreateRegistry(boolean createRegistry) {
        createRegistry(createRegistry);
    }

    /**
     * Sets the environment properties.
     */
    public JmxServerBuilder environmentProperties(Map<String, Object> environmentProperties) {
        endpoint.getEndpointConfiguration().setEnvironmentProperties(environmentProperties);
        return this;
    }

    @SchemaProperty(description = "Sets the environment properties.")
    @XmlTransient
    public void setEnvironmentProperties(Map<String, Object> environmentProperties) {
        environmentProperties(environmentProperties);
    }

    @XmlAttribute(name = "environment-properties")
    public void setEnvironmentProperties(String environmentProperties) {
        environmentProperties(Arrays.stream(environmentProperties.split(","))
                .map(String::trim)
                .filter(expression -> expression.contains("="))
                .map(expression -> expression.split("=", 2))
                .collect(Collectors.toMap(tokens -> tokens[0].trim(), tokens -> tokens[1].trim())));
    }

    /**
     * Sets the environment properties.
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
     */
    public JmxServerBuilder mbeans(List<ManagedBeanDefinition> mbeans) {
        endpoint.setMbeans(mbeans);
        return this;
    }

    @SchemaProperty(description = "Sets the Mbean definitions as bean references")
    @XmlAttribute
    public void setMbeans(String mbeans) {
        this.mbeans = mbeans;
    }

    /**
     * Sets the message converter.
     */
    public JmxServerBuilder messageConverter(JmxMessageConverter messageConverter) {
        endpoint.getEndpointConfiguration().setMessageConverter(messageConverter);
        return this;
    }

    @SchemaProperty(description = "Sets the message converter.")
    @XmlAttribute(name = "message-converter")
    public void setMessageConverter(String messageConverter) {
        this.messageConverter = messageConverter;
    }
}
