/*
 * Copyright 2006-2010 the original author or authors.
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

package com.consol.citrus.message;

import com.consol.citrus.TestActor;
import com.consol.citrus.endpoint.Endpoint;
import com.consol.citrus.endpoint.EndpointConfiguration;
import com.consol.citrus.messaging.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.integration.Message;

/**
 * Generic implementation for reply message receiver implementations. In addition to the usual
 * {@link MessageReceiver} functionality this class implements the {@link ReplyMessageHandler} interface.
 * 
 *  This means that synchronous message senders may invoke this receiver as soon as synchronous reply
 *  has arrived. Once invoked with a reply message the class saves the reply message to a local storage.
 *  When invoked by a receiving action inside a test the store reply message is returned.
 *  
 * @author Christoph Deppisch
 * @deprecated
 */
public class ReplyMessageReceiver implements MessageReceiver, ReplyMessageHandler, BeanNameAware {

    /** Logger */
    private static Logger log = LoggerFactory.getLogger(ReplyMessageReceiver.class);

    /** Endpoint holding reply messages */
    private Endpoint endpoint;

    /**
     * Default constructor using endpoint field.
     * @param endpoint
     */
    public ReplyMessageReceiver(Endpoint endpoint) {
        this.endpoint = endpoint;
    }

    @Override
    public Consumer createConsumer() {
        return endpoint.createConsumer();
    }

    @Override
    public Producer createProducer() {
        return endpoint.createProducer();
    }

    @Override
    public EndpointConfiguration getEndpointConfiguration() {
        return endpoint.getEndpointConfiguration();
    }

    /**
     * @see com.consol.citrus.message.MessageReceiver#receive()
     */
    public Message<?> receive() {
        return receiveSelected("");
    }

    /**
     * @see com.consol.citrus.message.MessageReceiver#receive(long)
     */
    public Message<?> receive(long timeout) {
        return receiveSelected("", timeout);
    }

    /**
     * @see com.consol.citrus.message.MessageReceiver#receiveSelected(java.lang.String)
     */
    public Message<?> receiveSelected(String selector) {
        return receiveSelected(selector, endpoint.getEndpointConfiguration().getTimeout());
    }

    /**
     * @see com.consol.citrus.message.MessageReceiver#receiveSelected(java.lang.String, long)
     */
    public Message<?> receiveSelected(String selector, long timeout) {
        Consumer consumer = endpoint.createConsumer();

        if (consumer instanceof SelectiveConsumer) {
            return ((SelectiveConsumer)endpoint.createConsumer()).receive(selector, timeout);
        } else {
            log.warn(String.format("Unable to receive selected with consumer implementation: '%s'", consumer.getClass()));
            return consumer.receive(endpoint.getEndpointConfiguration().getTimeout());
        }
    }

    @Override
    public void onReplyMessage(Message<?> replyMessage) {
    }

    @Override
    public void onReplyMessage(Message<?> replyMessage, String correlationKey) {
    }

    /**
     * Gets the actor.
     * @return the actor the actor to get.
     */
    public TestActor getActor() {
        return endpoint.getActor();
    }

    /**
     * Sets the actor.
     * @param actor the actor to set
     */
    public void setActor(TestActor actor) {
        endpoint.setActor(actor);
    }

    @Override
    public void setBeanName(String name) {
        endpoint.setName(name);
    }

    @Override
    public String getName() {
        return endpoint.getName();
    }

    @Override
    public void setName(String name) {
        endpoint.setName(name);
    }

    /**
     * Gets the message endpoint.
     * @return
     */
    public Endpoint getEndpoint() {
        return endpoint;
    }

    /**
     * Sets the message endpoint.
     * @param endpoint
     */
    public void setEndpoint(Endpoint endpoint) {
        this.endpoint = endpoint;
    }
}
