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

package org.citrusframework.message;

import org.citrusframework.context.TestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract message processor is message direction aware and automatically applies message type selector.
 * Subclasses can modify payload and/or headers of the processed message.
 *
 * @author Christoph Deppisch
 */
public abstract class AbstractMessageProcessor implements MessageProcessor, MessageDirectionAware, MessageTypeSelector {

    /** Logger */
    private final Logger logger = LoggerFactory.getLogger(getClass());

    /** Inbound/Outbound direction */
    private MessageDirection direction = MessageDirection.UNBOUND;

    @Override
    public void process(Message message, TestContext context) {
        if (supportsMessageType(message.getType())) {
            processMessage(message, context);
        } else {
            logger.debug(String.format("Message processor '%s' skipped for message type: %s", getName(), message.getType()));
        }
    }

    /**
     * Subclasses may overwrite this method in order to modify payload and/or headers of the processed message.
     * @param message the message to process.
     * @param context the current test context.
     * @return the processed message.
     */
    protected void processMessage(Message message, TestContext context) {
        // subclasses may implement processing logic
    }

    @Override
    public boolean supportsMessageType(String messageType) {
        return true;
    }

    /**
     * Gets this processors name.
     * @return
     */
    protected String getName() {
        return getClass().getSimpleName();
    }

    @Override
    public MessageDirection getDirection() {
        return direction;
    }

    /**
     * Sets the processor direction (inbound, outbound, unbound).
     * @param direction
     */
    public void setDirection(MessageDirection direction) {
        this.direction = direction;
    }
}
