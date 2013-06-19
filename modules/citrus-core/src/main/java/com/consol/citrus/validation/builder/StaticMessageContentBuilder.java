/*
 * Copyright 2006-2011 the original author or authors.
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

package com.consol.citrus.validation.builder;

import com.consol.citrus.validation.interceptor.MessageConstructionInterceptor;
import org.springframework.integration.Message;

import com.consol.citrus.context.TestContext;
import org.springframework.integration.support.MessageBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * Message builder returning a static message every time the build mechanism is called. This
 * class is primary used in unit tests and Soap message validators as we have other mechanisms there to
 * construct the control message.
 *  
 * @author Christoph Deppisch
 */
public class StaticMessageContentBuilder<T> implements MessageContentBuilder<T> {

    /** List of manipulators for static message payload */
    private List<MessageConstructionInterceptor<T>> messageInterceptors = new ArrayList<MessageConstructionInterceptor<T>>();

    /** The static message to build here */
    private Message<T> message;

    /**
     * Default constructor with static message to be built by this message builder.
     */
    public StaticMessageContentBuilder(Message<T> message) {
        this.message = message;
    }
    
    /**
     * Default constructor with static message to be built by this message builder. 
     */
    public static <T> StaticMessageContentBuilder<T> withMessage(Message<T> message) {
        return new StaticMessageContentBuilder<T>(message);
    }
    
    /**
     * Returns the static message every time.
     */
    public Message<T> buildMessageContent(TestContext context) {
        if (message != null && messageInterceptors.size() > 0) {
            T payload = message.getPayload();

            for (MessageConstructionInterceptor<T> modifyer : messageInterceptors) {
                payload = modifyer.interceptMessageConstruction(payload, context);
            }

            return MessageBuilder.withPayload(payload).copyHeaders(message.getHeaders()).build();
        }

        return message;
    }

    /**
     * Gets the message.
     * @return the message the message to get.
     */
    public Message<T> getMessage() {
        return message;
    }

    /**
     * Adds a new interceptor to the message construction process.
     * @param interceptor
     */
    public void add(MessageConstructionInterceptor<T> interceptor) {
        messageInterceptors.add(interceptor);
    }

    /**
     * Gets the messageInterceptors.
     * @return the messageInterceptors
     */
    public List<MessageConstructionInterceptor<T>> getMessageInterceptors() {
        return messageInterceptors;
    }

    /**
     * Sets the messageInterceptors.
     * @param messageInterceptors the messageInterceptors to set
     */
    public void setMessageInterceptors(
            List<MessageConstructionInterceptor<T>> messageInterceptors) {
        this.messageInterceptors = messageInterceptors;
    }
}
