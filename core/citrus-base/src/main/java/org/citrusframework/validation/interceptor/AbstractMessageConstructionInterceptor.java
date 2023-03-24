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

package org.citrusframework.validation.interceptor;

import org.citrusframework.context.TestContext;
import org.citrusframework.message.Message;
import org.citrusframework.message.MessageDirection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract message construction interceptor reads message payload and headers for separate interceptor methods.
 * Subclasses can either do payload modifying or header modifying or both depending on which method is overwritten.
 *
 * @author Christoph Deppisch
 * @since 1.4
 * @deprecated since 3.0 in favor of using {@link org.citrusframework.message.AbstractMessageProcessor}
 */
@Deprecated
public abstract class AbstractMessageConstructionInterceptor implements MessageConstructionInterceptor {

    /** Logger */
    private Logger log = LoggerFactory.getLogger(this.getClass());

    /** Inbound/Outbound direction */
    private MessageDirection direction = MessageDirection.UNBOUND;

    @Override
    public Message interceptMessageConstruction(Message message, String messageType, TestContext context) {
        if (supportsMessageType(messageType)) {
            return interceptMessage(message, messageType, context);
        } else {
            log.debug(String.format("Message interceptor type '%s' skipped for message type: %s", getName(), messageType));
            return message;
        }
    }

    /**
     * Gets this interceptors name.
     * @return
     */
    protected String getName() {
        return getClass().getSimpleName();
    }

    /**
     * Intercept the message construction. Subclasses may overwrite this method and modify message payload.
     * @param message the payload
     * @param messageType
     * @param context the current test context
     */
    protected Message interceptMessage(Message message, String messageType, TestContext context) {
        return message;
    }

    /**
     * Gets the direction.
     *
     * @return
     */
    @Override
    public MessageDirection getDirection() {
        return direction;
    }

    /**
     * Sets the direction.
     *
     * @param direction
     */
    public void setDirection(MessageDirection direction) {
        this.direction = direction;
    }
}
