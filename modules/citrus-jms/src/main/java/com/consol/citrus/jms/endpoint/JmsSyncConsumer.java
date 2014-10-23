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

package com.consol.citrus.jms.endpoint;

import com.consol.citrus.context.TestContext;
import com.consol.citrus.jms.message.JmsMessage;
import com.consol.citrus.message.*;
import com.consol.citrus.message.Message;
import com.consol.citrus.messaging.ReplyProducer;
import com.consol.citrus.report.MessageListeners;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.core.MessageCreator;
import org.springframework.util.Assert;

import javax.jms.*;

/**
 * @author Christoph Deppisch
 * @since 1.4
 */
public class JmsSyncConsumer extends JmsConsumer implements ReplyProducer {

    /** Map of reply destinations */
    private CorrelationManager<Destination> destinationManager = new DefaultCorrelationManager<Destination>();

    /** Endpoint configuration */
    private final JmsSyncEndpointConfiguration endpointConfiguration;

    /** Logger */
    private static Logger log = LoggerFactory.getLogger(JmsSyncConsumer.class);

    /**
     * Default constructor using endpoint configuration.
     * @param endpointConfiguration
     */
    public JmsSyncConsumer(JmsSyncEndpointConfiguration endpointConfiguration, MessageListeners messageListeners) {
        super(endpointConfiguration, messageListeners);
        this.endpointConfiguration = endpointConfiguration;
    }

    @Override
    public Message receive(String selector, TestContext context, long timeout) {
        Message receivedMessage = super.receive(selector, context, timeout);

        JmsMessage jmsMessage;
        if (receivedMessage instanceof JmsMessage) {
            jmsMessage = (JmsMessage) receivedMessage;
        } else {
            jmsMessage = new JmsMessage(receivedMessage);
        }

        saveReplyDestination(jmsMessage, context);

        return jmsMessage;
    }

    @Override
    public void send(final Message message, TestContext context) {
        Assert.notNull(message, "Message is empty - unable to send empty message");

        String correlationKey = getCorrelationKey(context);
        Destination replyDestination = findReplyDestination(correlationKey);
        Assert.notNull(replyDestination, "Failed to find JMS reply destination for message correlation key: '" + correlationKey + "'");

        log.info("Sending JMS message to destination: '" + getDestinationName(replyDestination) + "'");

        endpointConfiguration.getJmsTemplate().send(replyDestination, new MessageCreator() {
            @Override
            public javax.jms.Message createMessage(Session session) throws JMSException {
                javax.jms.Message jmsMessage = endpointConfiguration.getMessageConverter().createJmsMessage(message, session, endpointConfiguration);
                endpointConfiguration.getMessageConverter().convertOutbound(jmsMessage, message, endpointConfiguration);
                return jmsMessage;
            }
        });

        onOutboundMessage(message);

        log.info("Message was successfully sent to destination: '" + getDestinationName(replyDestination) + "'");
    }

    /**
     * Finds reply destination by correlation key in destination store.
     * @param correlationKey
     * @return
     */
    public Destination findReplyDestination(String correlationKey) {
        return destinationManager.find(correlationKey);
    }

    /**
     * Store the reply destination either straight forward or with a given
     * message correlation key.
     *
     * @param jmsMessage
     * @param context
     */
    public void saveReplyDestination(JmsMessage jmsMessage, TestContext context) {
        if (jmsMessage.getReplyTo() != null) {
            String correlationKey = endpointConfiguration.getCorrelator().getCorrelationKey(jmsMessage);
            context.setVariable(MessageHeaders.MESSAGE_CORRELATION_KEY + hashCode(), correlationKey);
            destinationManager.store(correlationKey, jmsMessage.getReplyTo());
        }  else {
            log.warn("Unable to retrieve reply to destination for message \n" +
                    jmsMessage + "\n - no reply to destination found in message headers!");
        }
    }

    /**
     * Informs message listeners if present.
     * @param message
     */
    protected void onOutboundMessage(Message message) {
        if (getMessageListener() != null) {
            getMessageListener().onOutboundMessage(message);
        } else {
            log.info("Sent message is:" + System.getProperty("line.separator") + message.toString());
        }
    }

    /**
     * Get the destination name (either a queue name or a topic name).
     * @return the destinationName
     */
    private String getDestinationName(Destination destination) {
        try {
            if (destination != null) {
                if (destination instanceof Queue) {
                    return ((Queue)destination).getQueueName();
                } else if (destination instanceof Topic) {
                    return ((Topic)destination).getTopicName();
                } else {
                    return destination.toString();
                }
            } else {
                return null;
            }
        } catch (JMSException e) {
            log.error("Error while getting destination name", e);
            return "";
        }
    }

    /**
     * Looks for default correlation id in message header and test context. If not present constructs default correlation key.
     * @param context
     * @return
     */
    private String getCorrelationKey(TestContext context) {
        if (context.getVariables().containsKey(MessageHeaders.MESSAGE_CORRELATION_KEY + hashCode())) {
            return context.getVariable(MessageHeaders.MESSAGE_CORRELATION_KEY + hashCode());
        }

        return "";
    }
}
