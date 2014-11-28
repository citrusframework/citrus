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
import com.consol.citrus.message.Message;
import com.consol.citrus.messaging.Producer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.core.MessageCreator;
import org.springframework.util.Assert;

import javax.jms.JMSException;
import javax.jms.Session;

/**
 * @author Christoph Deppisch
 * @since 1.4
 */
public class JmsProducer implements Producer {

    /** Logger */
    private static Logger log = LoggerFactory.getLogger(JmsProducer.class);

    /** The producer name. */
    private final String name;

    /** Endpoint configuration */
    private final JmsEndpointConfiguration endpointConfiguration;

    /**
     * Default constructor using endpoint configuration.
     * @param name
     * @param endpointConfiguration
     */
    public JmsProducer(String name, JmsEndpointConfiguration endpointConfiguration) {
        this.name = name;
        this.endpointConfiguration = endpointConfiguration;
    }

    @Override
    public void send(final Message message, TestContext context) {
        Assert.notNull(message, "Message is empty - unable to send empty message");

        String defaultDestinationName = endpointConfiguration.getDefaultDestinationName();

        log.info("Sending JMS message to destination: '" + defaultDestinationName + "'");

        endpointConfiguration.getJmsTemplate().send(new MessageCreator() {
            @Override
            public javax.jms.Message createMessage(Session session) throws JMSException {
                javax.jms.Message jmsMessage = endpointConfiguration.getMessageConverter().createJmsMessage(message, session, endpointConfiguration);
                endpointConfiguration.getMessageConverter().convertOutbound(jmsMessage, message, endpointConfiguration);
                return jmsMessage;
            }
        });

        context.onOutboundMessage(message);

        log.info("Message was successfully sent to destination: '" + defaultDestinationName + "'");
    }

    @Override
    public String getName() {
        return name;
    }

}
