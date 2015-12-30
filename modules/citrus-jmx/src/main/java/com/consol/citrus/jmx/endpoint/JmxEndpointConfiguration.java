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

package com.consol.citrus.jmx.endpoint;

import com.consol.citrus.endpoint.AbstractPollableEndpointConfiguration;
import com.consol.citrus.jmx.message.JmxMessageConverter;
import com.consol.citrus.message.DefaultMessageCorrelator;
import com.consol.citrus.message.MessageCorrelator;

import javax.management.NotificationFilter;

/**
 * @author Christoph Deppisch
 * @since 2.5
 */
public class JmxEndpointConfiguration extends AbstractPollableEndpointConfiguration {

    /** MBean server url, by default connect to platform MBean server */
    private String serverUrl = "platform";

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

    /** Message converter */
    private JmxMessageConverter messageConverter = new JmxMessageConverter();

    /** Reply message correlator */
    private MessageCorrelator correlator = new DefaultMessageCorrelator();

    public String getServerUrl() {
        return serverUrl;
    }

    public void setServerUrl(String serverUrl) {
        this.serverUrl = serverUrl;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isAutoReconnect() {
        return autoReconnect;
    }

    public void setAutoReconnect(boolean autoReconnect) {
        this.autoReconnect = autoReconnect;
    }

    public long getDelayOnReconnect() {
        return delayOnReconnect;
    }

    public void setDelayOnReconnect(long delayOnReconnect) {
        this.delayOnReconnect = delayOnReconnect;
    }

    public NotificationFilter getNotificationFilter() {
        return notificationFilter;
    }

    public void setNotificationFilter(NotificationFilter notificationFilter) {
        this.notificationFilter = notificationFilter;
    }

    public Object getNotificationHandback() {
        return notificationHandback;
    }

    public void setNotificationHandback(Object notificationHandback) {
        this.notificationHandback = notificationHandback;
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
}
