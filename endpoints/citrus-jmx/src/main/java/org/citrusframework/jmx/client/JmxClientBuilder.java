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

package org.citrusframework.jmx.client;

import org.citrusframework.endpoint.AbstractEndpointBuilder;
import org.citrusframework.jmx.message.JmxMessageConverter;
import org.citrusframework.message.MessageCorrelator;

import javax.management.NotificationFilter;

/**
 * @author Christoph Deppisch
 * @since 2.5
 */
public class JmxClientBuilder extends AbstractEndpointBuilder<JmxClient> {

    /** Endpoint target */
    private JmxClient endpoint = new JmxClient();

    @Override
    protected JmxClient getEndpoint() {
        return endpoint;
    }

    /**
     * Sets the serverUrl property.
     * @param serverUrl
     * @return
     */
    public JmxClientBuilder serverUrl(String serverUrl) {
        endpoint.getEndpointConfiguration().setServerUrl(serverUrl);
        return this;
    }

    /**
     * Sets the username property.
     * @param username
     * @return
     */
    public JmxClientBuilder username(String username) {
        endpoint.getEndpointConfiguration().setUsername(username);
        return this;
    }

    /**
     * Sets the password property.
     * @param password
     * @return
     */
    public JmxClientBuilder password(String password) {
        endpoint.getEndpointConfiguration().setPassword(password);
        return this;
    }

    /**
     * Sets the reconnectDelay property.
     * @param reconnectDelay
     * @return
     */
    public JmxClientBuilder reconnectDelay(long reconnectDelay) {
        endpoint.getEndpointConfiguration().setDelayOnReconnect(reconnectDelay);
        return this;
    }

    /**
     * Sets the autoReconnect property.
     * @param autoReconnect
     * @return
     */
    public JmxClientBuilder autoReconnect(boolean autoReconnect) {
        endpoint.getEndpointConfiguration().setAutoReconnect(autoReconnect);
        return this;
    }

    /**
     * Sets the notification filter.
     * @param notificationFilter
     * @return
     */
    public JmxClientBuilder notificationFilter(NotificationFilter notificationFilter) {
        endpoint.getEndpointConfiguration().setNotificationFilter(notificationFilter);
        return this;
    }

    /**
     * Sets the message converter.
     * @param messageConverter
     * @return
     */
    public JmxClientBuilder messageConverter(JmxMessageConverter messageConverter) {
        endpoint.getEndpointConfiguration().setMessageConverter(messageConverter);
        return this;
    }

    /**
     * Sets the message correlator.
     * @param correlator
     * @return
     */
    public JmxClientBuilder correlator(MessageCorrelator correlator) {
        endpoint.getEndpointConfiguration().setCorrelator(correlator);
        return this;
    }

    /**
     * Sets the polling interval.
     * @param pollingInterval
     * @return
     */
    public JmxClientBuilder pollingInterval(int pollingInterval) {
        endpoint.getEndpointConfiguration().setPollingInterval(pollingInterval);
        return this;
    }

    /**
     * Sets the default timeout.
     * @param timeout
     * @return
     */
    public JmxClientBuilder timeout(long timeout) {
        endpoint.getEndpointConfiguration().setTimeout(timeout);
        return this;
    }
}
