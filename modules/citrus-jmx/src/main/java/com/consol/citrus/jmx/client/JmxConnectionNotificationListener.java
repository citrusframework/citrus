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

package com.consol.citrus.jmx.client;

import com.consol.citrus.jmx.message.JmxMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.Notification;
import javax.management.NotificationListener;
import javax.management.remote.JMXConnectionNotification;

/**
 * @author Christoph Deppisch
 * @since 2.5
 */
public class JmxConnectionNotificationListener implements NotificationListener {

    /** Logger */
    private static Logger log = LoggerFactory.getLogger(JmxConnectionNotificationListener.class);

    /** Jms client instance */
    private final JmxClient jmxClient;

    private final JmxMessage jmxMessage;

    /**
     * Default constructor with jmx client.
     * @param jmxClient
     */
    public JmxConnectionNotificationListener(JmxClient jmxClient, JmxMessage jmxMessage) {
        this.jmxClient = jmxClient;
        this.jmxMessage = jmxMessage;
    }

    @Override
    public void handleNotification(Notification notification, Object handback) {
        JMXConnectionNotification connectionNotification = (JMXConnectionNotification) notification;
        if (connectionNotification.getConnectionId().equals(jmxClient.getConnectionId()) && connectionLost(connectionNotification)) {
            log.warn("JmxClient lost JMX connection for : {}", jmxClient.getEndpointConfiguration().getServerUrl());
            if (jmxClient.getEndpointConfiguration().isAutoReconnect()) {
                jmxClient.scheduleReconnect(jmxMessage);
            }
        }
    }

    /**
     * Finds connection lost type notifications.
     * @param connectionNotification
     * @return
     */
    private boolean connectionLost(JMXConnectionNotification connectionNotification) {
        return connectionNotification.getType().equals(JMXConnectionNotification.NOTIFS_LOST)
                || connectionNotification.getType().equals(JMXConnectionNotification.CLOSED)
                || connectionNotification.getType().equals(JMXConnectionNotification.FAILED);
    }
}
