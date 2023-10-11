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

package org.citrusframework.jmx.client;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.Collections;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import javax.management.Attribute;
import javax.management.InstanceNotFoundException;
import javax.management.JMException;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.Notification;
import javax.management.NotificationListener;
import javax.management.ObjectName;
import javax.management.openmbean.CompositeData;
import javax.management.remote.JMXConnectionNotification;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import org.citrusframework.context.TestContext;
import org.citrusframework.endpoint.AbstractEndpoint;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.exceptions.MessageTimeoutException;
import org.citrusframework.jmx.endpoint.JmxEndpointConfiguration;
import org.citrusframework.jmx.message.JmxMessage;
import org.citrusframework.jmx.model.ManagedBeanInvocation;
import org.citrusframework.message.DefaultMessage;
import org.citrusframework.message.Message;
import org.citrusframework.message.correlation.CorrelationManager;
import org.citrusframework.message.correlation.PollingCorrelationManager;
import org.citrusframework.messaging.Producer;
import org.citrusframework.messaging.ReplyConsumer;
import org.citrusframework.messaging.SelectiveConsumer;
import org.citrusframework.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Christoph Deppisch
 * @since 2.5
 */
public class JmxClient extends AbstractEndpoint implements Producer, ReplyConsumer, NotificationListener {

    /** Logger */
    private static final Logger logger = LoggerFactory.getLogger(JmxClient.class);

    /** Store of reply messages */
    private final CorrelationManager<Message> correlationManager;

    /** Saves the network connection id */
    private String connectionId;

    /** MBean object name */
    private ObjectName objectName;

    /** Optional notification listener */
    private NotificationListener notificationListener;

    /** Scheduler */
    private final ScheduledExecutorService scheduledExecutor = new ScheduledThreadPoolExecutor(1);

    /**
     * Default constructor initializing endpoint configuration.
     */
    public JmxClient() {
        this(new JmxEndpointConfiguration());
    }

    /**
     * Default constructor using endpoint configuration.
     * @param endpointConfiguration
     */
    public JmxClient(JmxEndpointConfiguration endpointConfiguration) {
        super(endpointConfiguration);

        this.correlationManager = new PollingCorrelationManager<>(endpointConfiguration, "Reply message did not arrive yet");
    }

    @Override
    public JmxEndpointConfiguration getEndpointConfiguration() {
        return (JmxEndpointConfiguration) super.getEndpointConfiguration();
    }

    @Override
    public void send(Message message, TestContext context) {
        String correlationKeyName = getEndpointConfiguration().getCorrelator().getCorrelationKeyName(getName());
        String correlationKey = getEndpointConfiguration().getCorrelator().getCorrelationKey(message);
        correlationManager.saveCorrelationKey(correlationKeyName, correlationKey, context);

        MBeanServerConnection serverConnection;
        if (getEndpointConfiguration().getServerUrl().equals("platform")) {
            serverConnection = ManagementFactory.getPlatformMBeanServer();
        } else {
            serverConnection = getNetworkConnection();
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Sending message to JMX MBeanServer server: '" + getEndpointConfiguration().getServerUrl() + "'");
            logger.debug("Message to send:\n" + message.getPayload(String.class));
        }
        context.onOutboundMessage(message);

        ManagedBeanInvocation invocation = getEndpointConfiguration().getMessageConverter().convertOutbound(message, getEndpointConfiguration(), context);
        try {
            if (StringUtils.hasText(invocation.getMbean())) {
                objectName = new ObjectName(invocation.getMbean().toString());
            } else if (StringUtils.hasText(invocation.getObjectKey())) {
                objectName = new ObjectName(invocation.getObjectDomain(), invocation.getObjectKey(), invocation.getObjectValue());
            } else {
                objectName = new ObjectName(invocation.getObjectDomain(), "name", invocation.getObjectName());
            }
        } catch (MalformedObjectNameException e) {
            throw new CitrusRuntimeException("Failed to create object name", e);
        }

        try {
            if (invocation.getOperation() != null) {
                Object result = serverConnection.invoke(objectName, invocation.getOperation().getName(), invocation.getOperation().getParamValues(context.getReferenceResolver()), invocation.getOperation().getParamTypes());
                if (result != null) {
                    correlationManager.store(correlationKey, JmxMessage.result(result));
                } else {
                    correlationManager.store(correlationKey, JmxMessage.result());
                }
            } else if (invocation.getAttribute() != null) {
                ManagedBeanInvocation.Attribute attribute = invocation.getAttribute();

                if (StringUtils.hasText(attribute.getValue())) {
                    serverConnection.setAttribute(objectName, new Attribute(attribute.getName(), invocation.getAttributeValue(context.getReferenceResolver())));
                } else {
                    Object attributeValue = serverConnection.getAttribute(objectName, attribute.getName());

                    if (StringUtils.hasText(attribute.getInnerPath())) {
                        if (attributeValue instanceof CompositeData) {
                            if (!((CompositeData) attributeValue).containsKey(attribute.getInnerPath())) {
                                throw new CitrusRuntimeException("Failed to find inner path attribute value: " + attribute.getInnerPath());
                            }

                            attributeValue = ((CompositeData) attributeValue).get(attribute.getInnerPath());
                        } else {
                            throw new CitrusRuntimeException("Failed to get inner path on attribute value: " + attributeValue);
                        }
                    }

                    if (attributeValue != null) {
                        correlationManager.store(correlationKey, JmxMessage.result(attributeValue));
                    } else {
                        correlationManager.store(correlationKey, JmxMessage.result());
                    }
                }
            } else {
                addNotificationListener(objectName, correlationKey, serverConnection);
            }
        } catch (JMException e) {
            throw new CitrusRuntimeException("Failed to execute MBean operation", e);
        } catch (IOException e) {
            throw new CitrusRuntimeException("Failed to execute MBean operation", e);
        }
    }

    /**
     * Establish network connection to remote mBean server.
     * @return
     */
    private MBeanServerConnection getNetworkConnection() {
        try {
            JMXServiceURL url = new JMXServiceURL(getEndpointConfiguration().getServerUrl());
            String[] creds = {getEndpointConfiguration().getUsername(), getEndpointConfiguration().getPassword()};
            JMXConnector networkConnector = JMXConnectorFactory.connect(url, Collections.singletonMap(JMXConnector.CREDENTIALS, creds));
            connectionId = networkConnector.getConnectionId();

            networkConnector.addConnectionNotificationListener(this, null, null);
            return networkConnector.getMBeanServerConnection();
        } catch (IOException e) {
            throw new CitrusRuntimeException("Failed to connect to network MBean server '" + getEndpointConfiguration().getServerUrl() + "'", e);
        }
    }

    @Override
    public Message receive(TestContext context) {
        return receive(correlationManager.getCorrelationKey(
                getEndpointConfiguration().getCorrelator().getCorrelationKeyName(getName()), context), context);
    }

    @Override
    public Message receive(String selector, TestContext context) {
        return receive(selector, context, getEndpointConfiguration().getTimeout());
    }

    @Override
    public Message receive(TestContext context, long timeout) {
        return receive(correlationManager.getCorrelationKey(
                getEndpointConfiguration().getCorrelator().getCorrelationKeyName(getName()), context), context, timeout);
    }

    @Override
    public Message receive(String selector, TestContext context, long timeout) {
        Message message = correlationManager.find(selector, timeout);

        if (message == null) {
            throw new MessageTimeoutException(timeout, getEndpointConfiguration().getServerUrl());
        }

        return message;
    }

    @Override
    public void handleNotification(Notification notification, Object handback) {
        JMXConnectionNotification connectionNotification = (JMXConnectionNotification) notification;
        if (connectionNotification.getConnectionId().equals(getConnectionId()) && connectionLost(connectionNotification)) {
            logger.warn("JmxClient lost JMX connection for : {}", getEndpointConfiguration().getServerUrl());
            if (getEndpointConfiguration().isAutoReconnect()) {
                scheduleReconnect();
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

    /**
     * Schedules an attempt to re-initialize a lost connection after the reconnect delay
     */
    public void scheduleReconnect() {
        Runnable startRunnable = new Runnable() {
            @Override
            public void run() {
                try {
                    MBeanServerConnection serverConnection = getNetworkConnection();
                    if (notificationListener != null) {
                        serverConnection.addNotificationListener(objectName, notificationListener, getEndpointConfiguration().getNotificationFilter(), getEndpointConfiguration().getNotificationHandback());
                    }
                } catch (Exception e) {
                    logger.warn("Failed to reconnect to JMX MBean server. {}", e.getMessage());
                    scheduleReconnect();
                }
            }
        };
        logger.info("Reconnecting to MBean server {} in {} milliseconds.", getEndpointConfiguration().getServerUrl(), getEndpointConfiguration().getDelayOnReconnect());
        scheduledExecutor.schedule(startRunnable, getEndpointConfiguration().getDelayOnReconnect(), TimeUnit.MILLISECONDS);
    }

    /**
     * Add notification listener for response messages.
     * @param objectName
     * @param correlationKey
     * @param serverConnection
     */
    private void addNotificationListener(ObjectName objectName, final String correlationKey, MBeanServerConnection serverConnection) {
        try {
            notificationListener = new NotificationListener() {
                @Override
                public void handleNotification(Notification notification, Object handback) {
                    correlationManager.store(correlationKey, new DefaultMessage(notification.getMessage()));
                }
            };

            serverConnection.addNotificationListener(objectName, notificationListener, getEndpointConfiguration().getNotificationFilter(), getEndpointConfiguration().getNotificationHandback());
        } catch (InstanceNotFoundException e) {
            throw new CitrusRuntimeException("Failed to find object name instance", e);
        } catch (IOException e) {
            throw new CitrusRuntimeException("Failed to add notification listener", e);
        }
    }

    /**
     * Creates a message producer for this endpoint for sending messages
     * to this endpoint.
     */
    @Override
    public Producer createProducer() {
        return this;
    }

    /**
     * Creates a message consumer for this endpoint. Consumer receives
     * messages on this endpoint.
     *
     * @return
     */
    @Override
    public SelectiveConsumer createConsumer() {
        return this;
    }

    public String getConnectionId() {
        return connectionId;
    }

    public void setConnectionId(String connectionId) {
        this.connectionId = connectionId;
    }
}
