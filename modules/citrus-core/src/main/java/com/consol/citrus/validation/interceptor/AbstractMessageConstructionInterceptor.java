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

package com.consol.citrus.validation.interceptor;

import com.consol.citrus.context.TestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.integration.support.MessageBuilder;

/**
 * Abstract message construction interceptor reads messsage payload and headers for separate interceptor methods.
 * Subclasses can either do payload modifying or header modifying or both depending on which method is overwritten.
 *
 * @author Christoph Deppisch
 * @since 1.4
 */
public abstract class AbstractMessageConstructionInterceptor implements MessageConstructionInterceptor {

    /** Logger */
    private Logger log = LoggerFactory.getLogger(this.getClass());

    @Override
    public Message<?> interceptMessageConstruction(Message<?> message, String messageType, TestContext context) {
        if (supportsMessageType(messageType)) {
            return MessageBuilder.withPayload(interceptMessagePayload(message.getPayload().toString(), messageType, context))
                    .copyHeaders(interceptMessageHeaders(message.getHeaders(), messageType, context)).build();
        } else {
            log.info(String.format("Message interceptor (%s) does not support message type: %s", getClass().getSimpleName(), messageType));
            return message;
        }
    }

    /**
     * Intercept message headers. Subclasses may overwrite this method and modify headers.
     * @param headers the message header entries
     * @param messageType
     * @param context the current test context
     * @return
     */
    protected MessageHeaders interceptMessageHeaders(MessageHeaders headers, String messageType, TestContext context) {
        return headers;
    }

    /**
     * Intercept the message payload construction. Subclasses may overwrite this method and modify message payload.
     * @param messagePayload the payload
     * @param messageType
     * @param context the current test context
     */
    protected String interceptMessagePayload(String messagePayload, String messageType, TestContext context) {
        return messagePayload;
    }
}
