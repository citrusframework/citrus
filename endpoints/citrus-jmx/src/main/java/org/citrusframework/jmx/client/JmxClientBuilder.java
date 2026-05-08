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

package org.citrusframework.jmx.client;

import javax.management.NotificationFilter;

import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlType;
import org.citrusframework.endpoint.AbstractEndpointBuilder;
import org.citrusframework.jmx.message.JmxMessageConverter;
import org.citrusframework.message.MessageCorrelator;
import org.citrusframework.util.StringUtils;
import org.citrusframework.yaml.SchemaProperty;
import org.citrusframework.yaml.SchemaType;

/**
 * @since 2.5
 */
@SchemaType(module = "citrus-jmx")
@XmlType(name = "", propOrder = {})
public class JmxClientBuilder extends AbstractEndpointBuilder<JmxClient> {

    /** Endpoint target */
    private final JmxClient endpoint = new JmxClient();

    private String notificationFilter;
    private String messageConverter;
    private String correlator;

    @Override
    public JmxClient build() {
        if (referenceResolver != null) {
            if (StringUtils.hasText(messageConverter)) {
                messageConverter(referenceResolver.resolve(messageConverter, JmxMessageConverter.class));
            }

            if (StringUtils.hasText(correlator)) {
                correlator(referenceResolver.resolve(correlator, MessageCorrelator.class));
            }

            if (StringUtils.hasText(notificationFilter)) {
                notificationFilter(referenceResolver.resolve(notificationFilter, NotificationFilter.class));
            }
        }

        return super.build();
    }

    @Override
    protected JmxClient getEndpoint() {
        return endpoint;
    }

    /**
     * Sets the serverUrl property.
     */
    public JmxClientBuilder serverUrl(String serverUrl) {
        endpoint.getEndpointConfiguration().setServerUrl(serverUrl);
        return this;
    }

    @SchemaProperty(description = "Sets the serverUrl property.")
    @XmlAttribute(name = "server-url")
    public void setServerUrl(String serverUrl) {
        serverUrl(serverUrl);
    }

    /**
     * Sets the username property.
     */
    public JmxClientBuilder username(String username) {
        endpoint.getEndpointConfiguration().setUsername(username);
        return this;
    }

    @SchemaProperty(description = "Sets the username property.")
    @XmlAttribute
    public void setUsername(String username) {
        username(username);
    }

    /**
     * Sets the password property.
     */
    public JmxClientBuilder password(String password) {
        endpoint.getEndpointConfiguration().setPassword(password);
        return this;
    }

    @SchemaProperty(description = "Sets the password property.")
    @XmlAttribute
    public void setPassword(String password) {
        password(password);
    }

    /**
     * Sets the reconnectDelay property.
     */
    public JmxClientBuilder reconnectDelay(long reconnectDelay) {
        endpoint.getEndpointConfiguration().setDelayOnReconnect(reconnectDelay);
        return this;
    }

    @SchemaProperty(description = "Sets the reconnectDelay property.")
    @XmlAttribute(name = "reconnect-delay")
    public void setReconnectDelay(long reconnectDelay) {
        reconnectDelay(reconnectDelay);
    }

    /**
     * Sets the autoReconnect property.
     */
    public JmxClientBuilder autoReconnect(boolean autoReconnect) {
        endpoint.getEndpointConfiguration().setAutoReconnect(autoReconnect);
        return this;
    }

    @SchemaProperty(description = "Sets the autoReconnect property.")
    @XmlAttribute(name = "auto-connect")
    public void setAutoReconnect(boolean autoReconnect) {
        autoReconnect(autoReconnect);
    }

    /**
     * Sets the notification filter.
     */
    public JmxClientBuilder notificationFilter(NotificationFilter notificationFilter) {
        endpoint.getEndpointConfiguration().setNotificationFilter(notificationFilter);
        return this;
    }

    @SchemaProperty(description = "Sets the notification filter.")
    @XmlAttribute(name = "notification-filter")
    public void setNotificationFilter(String notificationFilter) {
        this.notificationFilter = notificationFilter;
    }

    /**
     * Sets the message converter.
     */
    public JmxClientBuilder messageConverter(JmxMessageConverter messageConverter) {
        endpoint.getEndpointConfiguration().setMessageConverter(messageConverter);
        return this;
    }

    @SchemaProperty(description = "Sets the message converter.")
    @XmlAttribute(name = "message-converter")
    public void setMessageConverter(String messageConverter) {
        this.messageConverter = messageConverter;
    }

    /**
     * Sets the message correlator.
     */
    public JmxClientBuilder correlator(MessageCorrelator correlator) {
        endpoint.getEndpointConfiguration().setCorrelator(correlator);
        return this;
    }

    @SchemaProperty(description = "Sets the message correlator.")
    @XmlAttribute(name = "message-correlator")
    public void setCorrelator(String correlator) {
        this.correlator = correlator;
    }

    /**
     * Sets the polling interval.
     */
    public JmxClientBuilder pollingInterval(int pollingInterval) {
        endpoint.getEndpointConfiguration().setPollingInterval(pollingInterval);
        return this;
    }

    @SchemaProperty(description = "Sets the polling interval.")
    @XmlAttribute(name = "polling-interval")
    public void setPollingInterval(int pollingInterval) {
        pollingInterval(pollingInterval);
    }

    /**
     * Sets the default timeout.
     */
    public JmxClientBuilder timeout(long timeout) {
        endpoint.getEndpointConfiguration().setTimeout(timeout);
        return this;
    }

    @SchemaProperty(description = "Sets the default timeout.")
    @XmlAttribute
    public void setTimeout(long timeout) {
        timeout(timeout);
    }
}
