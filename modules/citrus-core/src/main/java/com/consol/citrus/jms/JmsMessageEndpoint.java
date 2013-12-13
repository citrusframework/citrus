/*
 * Copyright 2006-2013 the original author or authors.
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

package com.consol.citrus.jms;

import com.consol.citrus.exceptions.ActionTimeoutException;
import com.consol.citrus.report.MessageListeners;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.Message;
import org.springframework.integration.jms.DefaultJmsHeaderMapper;
import org.springframework.integration.jms.JmsHeaderMapper;
import org.springframework.integration.message.GenericMessage;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.jms.support.converter.SimpleMessageConverter;
import org.springframework.jms.support.destination.DestinationResolver;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import javax.jms.*;

/**
 * Jms message endpoint capable of sending/receiving messages from Jms message destination. Either uses a Jms connection factory or
 * a Spring Jms template to connect with Jms destinations.
 *
 * @author Christoph Deppisch
 * @since 1.4
 */
public class JmsMessageEndpoint extends AbstractJmsMessageEndpoint {
    /** The connection factory */
    private ConnectionFactory connectionFactory;

    /** The destination object */
    private Destination destination;

    /** The destination name */
    private String destinationName;

    /** The destination resolver */
    private DestinationResolver destinationResolver;

    /** The JMS template */
    private JmsTemplate jmsTemplate;

    /** The message converter */
    private MessageConverter messageConverter = new SimpleMessageConverter();

    /** The JMS header mapper */
    private JmsHeaderMapper headerMapper = new DefaultJmsHeaderMapper();

    /** Use topics instead of queues */
    private boolean pubSubDomain = false;

    @Autowired(required = false)
    private MessageListeners messageListener;

    /** Logger */
    private static Logger log = LoggerFactory.getLogger(JmsMessageEndpoint.class);

    @Override
    public void send(Message<?> message) {
        Assert.notNull(message, "Message is empty - unable to send empty message");

        String defaultDestinationName = getDefaultDestinationName();

        log.info("Sending JMS message to destination: '" + defaultDestinationName + "'");

        getJmsTemplate().convertAndSend(message);

        onOutboundMessage(message);

        log.info("Message was successfully sent to destination: '" + defaultDestinationName + "'");
    }

    @Override
    public Message<?> receive(String selector, long timeout) {
        String destinationName;

        if (StringUtils.hasText(selector)) {
            destinationName = getDefaultDestinationName() + "(" + selector + ")'";
        } else {
            destinationName = getDefaultDestinationName();
        }

        log.info("Waiting for JMS message on destination: '" + destinationName);

        getJmsTemplate().setReceiveTimeout(timeout);
        Object receivedObject = null;

        if (StringUtils.hasText(selector)) {
            receivedObject = getJmsTemplate().receiveSelectedAndConvert(selector);
        } else {
            receivedObject = getJmsTemplate().receiveAndConvert();
        }

        if (receivedObject == null) {
            throw new ActionTimeoutException("Action timed out while receiving JMS message on '" + destinationName);
        }

        Message<?> receivedMessage;
        if (receivedObject instanceof Message<?>) {
            receivedMessage = (Message<?>)receivedObject;
        } else {
            receivedMessage = new GenericMessage<Object>(receivedObject);
        }

        log.info("Received JMS message on destination: '" + destinationName);

        onInboundMessage(receivedMessage);

        return receivedMessage;
    }

    /**
     * Informs message listeners if present.
     * @param receivedMessage
     */
    protected void onInboundMessage(Message<?> receivedMessage) {
        if (messageListener != null) {
            messageListener.onInboundMessage((receivedMessage != null ? receivedMessage.toString() : ""));
        } else {
            log.debug("Received message is:" + System.getProperty("line.separator") + (receivedMessage != null ? receivedMessage.toString() : ""));
        }
    }

    /**
     * Informs message listeners if present.
     * @param message
     */
    protected void onOutboundMessage(Message<?> message) {
        if (messageListener != null) {
            messageListener.onOutboundMessage(message.toString());
        } else {
            log.info("Sent message is:" + System.getProperty("line.separator") + message.toString());
        }
    }

    /**
     * Gets the destination name.
     * @return the destinationName
     */
    public String getDefaultDestinationName() {
        try {
            if (getJmsTemplate().getDefaultDestination() != null) {
                if (getJmsTemplate().getDefaultDestination() instanceof Queue) {
                    return ((Queue)getJmsTemplate().getDefaultDestination()).getQueueName();
                } else if (getJmsTemplate().getDefaultDestination() instanceof Topic) {
                    return ((Topic)getJmsTemplate().getDefaultDestination()).getTopicName();
                } else {
                    return getJmsTemplate().getDefaultDestination().toString();
                }
            } else {
                return getJmsTemplate().getDefaultDestinationName();
            }
        } catch (JMSException e) {
            log.error("Unable to resolve destination name", e);
            return "";
        }
    }

    /**
     * Initialize default JMS template if not already set.
     */
    public void afterPropertiesSet() {
        if (jmsTemplate == null) {
            Assert.isTrue(this.connectionFactory != null,
                    "Either a 'jmsTemplate' or 'connectionFactory' is required - none of those was set correctly.");

            jmsTemplate = new JmsTemplate();

            jmsTemplate.setConnectionFactory(this.connectionFactory);

            if (this.destination != null) {
                jmsTemplate.setDefaultDestination(this.destination);
            } else if (this.destinationName != null) {
                jmsTemplate.setDefaultDestinationName(this.destinationName);
            }

            if (this.destinationResolver != null) {
                jmsTemplate.setDestinationResolver(this.destinationResolver);
            }
        }

        jmsTemplate.setMessageConverter(new JmsMessageConverter(messageConverter, headerMapper));
        jmsTemplate.setPubSubDomain(pubSubDomain);
    }

    /**
     * Does domain use topics instead of queues.
     * @return the pubSubDomain
     */
    public boolean isPubSubDomain() {
        return pubSubDomain;
    }

    /**
     * Sets if domain uses topics instead of queues.
     * @param pubSubDomain the pubSubDomain to set
     */
    public void setPubSubDomain(boolean pubSubDomain) {
        this.pubSubDomain = pubSubDomain;
    }

    /**
     * Gets the connection factory.
     * @return the connectionFactory
     */
    public ConnectionFactory getConnectionFactory() {
        return connectionFactory;
    }

    /**
     * Sets the connection factory.
     * @param connectionFactory the connectionFactory to set
     */
    public void setConnectionFactory(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    /**
     * Gets the destination.
     * @return the destination
     */
    public Destination getDestination() {
        return destination;
    }

    /**
     * Sets the destination.
     * @param destination the destination to set
     */
    public void setDestination(Destination destination) {
        this.destination = destination;
    }

    /**
     * Gets the destination name.
     * @return the destinationName
     */
    public String getDestinationName() {
        return destinationName;
    }

    /**
     * Sets the destination name.
     * @param destinationName the destinationName to set
     */
    public void setDestinationName(String destinationName) {
        this.destinationName = destinationName;
    }

    /**
     * Sets the destination resolver.
     * @return the destinationResolver
     */
    public DestinationResolver getDestinationResolver() {
        return destinationResolver;
    }

    /**
     * Gets the destination resolver.
     * @param destinationResolver the destinationResolver to set
     */
    public void setDestinationResolver(DestinationResolver destinationResolver) {
        this.destinationResolver = destinationResolver;
    }

    /**
     * Gets the JMS message converter.
     * @return the messageConverter
     */
    public MessageConverter getMessageConverter() {
        return messageConverter;
    }

    /**
     * Sets the JMS message converter.
     * @param messageConverter the messageConverter to set
     */
    public void setMessageConverter(MessageConverter messageConverter) {
        this.messageConverter = messageConverter;
    }

    /**
     * Gets the JMS header mapper.
     * @return the headerMapper
     */
    public JmsHeaderMapper getHeaderMapper() {
        return headerMapper;
    }

    /**
     * Sets the JMS header mapper.
     * @param headerMapper the headerMapper to set
     */
    public void setHeaderMapper(JmsHeaderMapper headerMapper) {
        this.headerMapper = headerMapper;
    }

    /**
     * Sets the JMS template.
     * @param jmsTemplate the jmsTemplate to set
     */
    public void setJmsTemplate(JmsTemplate jmsTemplate) {
        this.jmsTemplate = jmsTemplate;
    }

    /**
     * Gets the JMS template.
     * @return the jmsTemplate
     */
    public JmsTemplate getJmsTemplate() {
        if (jmsTemplate == null) {
            afterPropertiesSet();
        }

        return jmsTemplate;
    }
}
