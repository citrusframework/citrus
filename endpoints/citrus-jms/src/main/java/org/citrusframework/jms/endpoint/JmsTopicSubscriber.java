/*
 * Copyright 2006-2018 the original author or authors.
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

package org.citrusframework.jms.endpoint;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import jakarta.jms.ConnectionFactory;
import jakarta.jms.JMSException;
import jakarta.jms.Topic;
import jakarta.jms.TopicConnection;
import jakarta.jms.TopicConnectionFactory;
import jakarta.jms.TopicSession;
import jakarta.jms.TopicSubscriber;
import org.citrusframework.context.TestContext;
import org.citrusframework.context.TestContextFactory;
import org.citrusframework.endpoint.direct.DirectEndpoint;
import org.citrusframework.endpoint.direct.DirectEndpointConfiguration;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.message.DefaultMessageQueue;
import org.citrusframework.message.Message;
import org.citrusframework.message.MessageQueue;
import org.citrusframework.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Christoph Deppisch
 * @since 2.7.6
 */
public class JmsTopicSubscriber extends JmsConsumer implements Runnable {

    /** Logger */
    private static final Logger logger = LoggerFactory.getLogger(JmsConsumer.class);

    /** Boolean flag for continued message consumption, if false stop */
    private boolean running = true;

    /** Test context factory for send operation on message queue */
    private final TestContextFactory testContextFactory;

    /** Delegate in-memory message queue caching all inbound messages */
    private DirectEndpoint messageQueue;

    private Executor subscription = Executors.newSingleThreadExecutor();
    private CompletableFuture<Boolean> stopped = new CompletableFuture<>();
    private CompletableFuture<Boolean> started = new CompletableFuture<>();

    /**
     * Default constructor using endpoint.
     *
     * @param name
     * @param endpointConfiguration
     */
    public JmsTopicSubscriber(String name, JmsEndpointConfiguration endpointConfiguration, TestContextFactory testContextFactory) {
        super(name, endpointConfiguration);

        this.testContextFactory = testContextFactory;

        DirectEndpointConfiguration directEndpointConfiguration = new DirectEndpointConfiguration();

        MessageQueue inboundQueue = new DefaultMessageQueue(name + ".inbound");
        directEndpointConfiguration.setQueue(inboundQueue);

        this.messageQueue = new DirectEndpoint(directEndpointConfiguration);
    }

    /**
     * Starts consuming topic events.
     */
    public void run() {
        ConnectionFactory connectionFactory = Optional.ofNullable(endpointConfiguration.getConnectionFactory())
                                                      .orElseGet(() -> endpointConfiguration.getJmsTemplate().getConnectionFactory());

        TopicConnection connection = null;
        try {
            if (!(connectionFactory instanceof TopicConnectionFactory)) {
                throw new CitrusRuntimeException("Failed to create JMS topic subscriber for unsupported connection factory type: " + Optional.ofNullable(connectionFactory)
                        .map(Object::getClass)
                        .map(Class::getName)
                        .orElse("connection factory not set"));
            }

            connection = ((TopicConnectionFactory)connectionFactory).createTopicConnection();

            TopicSession session = connection.createTopicSession(false, jakarta.jms.Session.AUTO_ACKNOWLEDGE);
            Topic topic;
            if (endpointConfiguration.getDestination() != null && endpointConfiguration.getDestination() instanceof Topic) {
                topic = (Topic) endpointConfiguration.getDestination();
            } else if (StringUtils.hasText(endpointConfiguration.getDestinationName())) {
                topic = session.createTopic(endpointConfiguration.getDestinationName());
            } else if (endpointConfiguration.getJmsTemplate().getDefaultDestination() != null && endpointConfiguration.getJmsTemplate().getDefaultDestination() instanceof Topic) {
                topic = (Topic) endpointConfiguration.getJmsTemplate().getDefaultDestination();
            } else if (StringUtils.hasText(endpointConfiguration.getJmsTemplate().getDefaultDestinationName())) {
                topic = session.createTopic(endpointConfiguration.getJmsTemplate().getDefaultDestinationName());
            } else {
                throw new CitrusRuntimeException("Unable to receive message - JMS destination not set");
            }

            TopicSubscriber subscriber;
            if (endpointConfiguration.isDurableSubscription()) {
                logger.debug(String.format("Create JMS topic durable subscription '%s'", Optional.ofNullable(endpointConfiguration.getDurableSubscriberName()).orElseGet(this::getName)));
                subscriber = session.createDurableSubscriber(topic, Optional.ofNullable(endpointConfiguration.getDurableSubscriberName()).orElseGet(this::getName));
            } else {
                logger.debug("Create JMS topic subscription");
                subscriber = session.createSubscriber(topic);
            }

            connection.start();

            started.complete(true);

            while (running) {
                jakarta.jms.Message event = subscriber.receive();

                if (event != null) {
                    TestContext context = testContextFactory.getObject();
                    Message message = endpointConfiguration.getMessageConverter().convertInbound(event, endpointConfiguration, context);

                    if (logger.isDebugEnabled()) {
                        logger.debug(String.format("Received topic event '%s'", message.getId()));
                    }
                    messageQueue.createProducer().send(message, context);
                } else {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Topic subscriber received null message - continue after " + endpointConfiguration.getPollingInterval() + " milliseconds");
                    }

                    try {
                        Thread.sleep(endpointConfiguration.getPollingInterval());
                    } catch (InterruptedException e) {
                        logger.warn("Interrupted while waiting after null message", e);
                    }
                }
            }
        } catch (RuntimeException e) {
            started.completeExceptionally(e);
            throw e;
        } catch (JMSException e) {
            started.completeExceptionally(e);
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (JMSException e) {
                    logger.warn("Failed to close JMS topic connection", e);
                }
            }

            stopped.complete(true);
        }
    }

    public void start() {
        subscription.execute(this);

        try {
            if (started.get()) {
                logger.info("Started JMS topic subscription");
            }
        } catch (InterruptedException | ExecutionException e) {
            logger.warn("Failed to wait for topic subscriber to start subscription", e);
        }
    }

    public void stop() {
        running = false;

        try {
            stopped.get(endpointConfiguration.getTimeout(), TimeUnit.MILLISECONDS);
        } catch (InterruptedException | ExecutionException e) {
            logger.warn("Failed to wait for topic subscriber to stop gracefully", e);
        } catch (TimeoutException e) {
            logger.warn("Timeout while waiting for topic subscriber to stop gracefully", e);
        }
    }

    @Override
    public Message receive(TestContext context, long timeout) {
        return messageQueue.createConsumer().receive(context, timeout);
    }

    @Override
    public Message receive(String selector, TestContext context, long timeout) {
        return messageQueue.createConsumer().receive(selector, context, timeout);
    }

    /**
     * Gets the running state.
     * @return
     */
    public boolean isRunning() {
        return running;
    }
}
