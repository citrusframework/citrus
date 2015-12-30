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

import com.consol.citrus.context.TestContext;
import com.consol.citrus.endpoint.AbstractEndpoint;
import com.consol.citrus.exceptions.ActionTimeoutException;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.jmx.endpoint.JmxEndpointConfiguration;
import com.consol.citrus.jmx.message.JmxMessage;
import com.consol.citrus.jmx.message.JmxMessageHeaders;
import com.consol.citrus.message.Message;
import com.consol.citrus.message.MessageHeaders;
import com.consol.citrus.message.correlation.CorrelationManager;
import com.consol.citrus.message.correlation.PollingCorrelationManager;
import com.consol.citrus.messaging.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.*;
import javax.management.remote.*;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.*;

/**
 * @author Christoph Deppisch
 * @since 2.5
 */
public class JmxClient extends AbstractEndpoint implements Producer, ReplyConsumer {

    /** Logger */
    private static Logger log = LoggerFactory.getLogger(JmxClient.class);

    /** Store of reply messages */
    private CorrelationManager<Message> correlationManager;

    /** Connection to the MBean server (local or remote) */
    private MBeanServerConnection serverConnection;

    /** Network jmx connector */
    private JMXConnector networkConnector;

    /** Saves the network connection id */
    private String connectionId;

    /** Scheduler */
    private ScheduledExecutorService scheduledExecutor = new ScheduledThreadPoolExecutor(1);

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

        this.correlationManager = new PollingCorrelationManager(endpointConfiguration, "Reply message did not arrive yet");
    }

    @Override
    public JmxEndpointConfiguration getEndpointConfiguration() {
        return (JmxEndpointConfiguration) super.getEndpointConfiguration();
    }

    @Override
    public void send(Message message, TestContext context) {
        JmxMessage jmxMessage;
        if (message instanceof JmxMessage) {
            jmxMessage = (JmxMessage) message;
        } else {
            jmxMessage = new JmxMessage(message);
        }

        String correlationKeyName = getEndpointConfiguration().getCorrelator().getCorrelationKeyName(getName());
        String correlationKey = getEndpointConfiguration().getCorrelator().getCorrelationKey(message);
        correlationManager.saveCorrelationKey(correlationKeyName, correlationKey, context);

        if (serverConnection == null) {
            if (getEndpointConfiguration().getServerUrl().equals("platform")) {
                serverConnection = ManagementFactory.getPlatformMBeanServer();
            } else {
                serverConnection = getNetworkConnection(jmxMessage);
            }
        }

        jmxMessage.setHeader(MessageHeaders.MESSAGE_CORRELATION_KEY, correlationKey);
        execute(jmxMessage);
    }

    private void execute(final JmxMessage jmxMessage) {
        ObjectName objectName;
        try {
            if (jmxMessage.getHeader(JmxMessageHeaders.JMX_MBEAN) != null) {
                objectName = new ObjectName(jmxMessage.getHeader(JmxMessageHeaders.JMX_MBEAN).toString());
            } else {
                objectName = new ObjectName(jmxMessage.getHeader(JmxMessageHeaders.JMX_OBJECT_DOMAIN).toString(), "name", jmxMessage.getHeader(JmxMessageHeaders.JMX_OBJECT_NAME).toString());
            }
        } catch (MalformedObjectNameException e) {
            throw new CitrusRuntimeException("Failed to create object name", e);
        }

        try {
            if (jmxMessage.hasOperation()) {
                serverConnection.invoke(objectName, jmxMessage.getHeader(JmxMessageHeaders.JMX_OPERATION).toString(), new Object[]{}, null);
            } else if (jmxMessage.hasValue()) {
                serverConnection.setAttribute(objectName, new Attribute(jmxMessage.getHeader(JmxMessageHeaders.JMX_ATTRIBUTE).toString(), jmxMessage.getHeader(JmxMessageHeaders.JMX_VALUE)));
            } else if (jmxMessage.hasAttribute()) {
                Object attributeValue = serverConnection.getAttribute(objectName, jmxMessage.getHeader(JmxMessageHeaders.JMX_ATTRIBUTE).toString());
                JmxMessage response = new JmxMessage(jmxMessage);
                response.setPayload(attributeValue);
                correlationManager.store(jmxMessage.getHeader(MessageHeaders.MESSAGE_CORRELATION_KEY).toString(), response);
            } else {
                addNotificationListener(objectName, jmxMessage.getHeader(MessageHeaders.MESSAGE_CORRELATION_KEY).toString());
            }
        } catch (JMException e) {
            throw new CitrusRuntimeException("Failed to execute MBean operation", e);
        } catch (IOException e) {
            throw new CitrusRuntimeException("Failed to execute MBean operation", e);
        }
    }

    /**
     * Establish network connection to remote mBean server.
     * @param jmxMessage
     * @return
     */
    private MBeanServerConnection getNetworkConnection(JmxMessage jmxMessage) {
        try {
            JMXServiceURL url = new JMXServiceURL(getEndpointConfiguration().getServerUrl());
            String[] creds = {getEndpointConfiguration().getUsername(), getEndpointConfiguration().getPassword()};
            Map<String, String[]> map = Collections.singletonMap(JMXConnector.CREDENTIALS, creds);
            networkConnector = JMXConnectorFactory.connect(url, map);
            connectionId = networkConnector.getConnectionId();

            networkConnector.addConnectionNotificationListener(new JmxConnectionNotificationListener(this, jmxMessage), null, null);
            return networkConnector.getMBeanServerConnection();
        } catch (IOException e) {
            throw new CitrusRuntimeException("Failed to connect to network MBean server", e);
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
            throw new ActionTimeoutException("Action timeout while receiving synchronous reply message from MBean server");
        }

        return message;
    }

    /**
     * Schedules an attempt to re-initialize a lost connection after the reconnect delay
     * @param jmxMessage
     */
    public void scheduleReconnect(final JmxMessage jmxMessage) {
        Runnable startRunnable = new Runnable() {
            @Override
            public void run() {
                try {
                    serverConnection = getNetworkConnection(jmxMessage);
                    execute(jmxMessage);
                } catch (Exception e) {
                    log.warn("Failed to reconnect to JMX MBean server. {}", e.getMessage());
                    scheduleReconnect(jmxMessage);
                }
            }
        };
        log.info("Reconnecting to MBean server {} in {} milliseconds.", getEndpointConfiguration().getServerUrl(), getEndpointConfiguration().getDelayOnReconnect());
        scheduledExecutor.schedule(startRunnable, getEndpointConfiguration().getDelayOnReconnect(), TimeUnit.MILLISECONDS);
    }

    /**
     * Add notification listener for response messages.
     * @param objectName
     * @param correlationKey
     */
    private void addNotificationListener(ObjectName objectName, final String correlationKey) {
        try {
            serverConnection.addNotificationListener(objectName, new NotificationListener() {
                @Override
                public void handleNotification(Notification notification, Object handback) {
                    correlationManager.store(correlationKey, getEndpointConfiguration().getMessageConverter().convertInbound(notification, getEndpointConfiguration()));
                }
            }, getEndpointConfiguration().getNotificationFilter(), getEndpointConfiguration().getNotificationHandback());
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
