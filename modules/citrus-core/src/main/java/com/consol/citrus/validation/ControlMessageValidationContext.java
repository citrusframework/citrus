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

package com.consol.citrus.validation;

import com.consol.citrus.context.TestContext;
import com.consol.citrus.validation.builder.*;
import com.consol.citrus.validation.context.ValidationContext;
import com.consol.citrus.message.Message;


/**
 * Validation context providing a control message for message validation. 
 * The control message holds expected message content and header information. Message 
 * validators compare the actual message to the control message marking differences.
 * 
 * @author Christoph Deppisch
 */
public class ControlMessageValidationContext implements ValidationContext {
    /** Builder constructing a control message */
    private MessageContentBuilder messageBuilder = new PayloadTemplateMessageBuilder();

    /** The message type this context was built for */
    private final String messageType;

    /**
     * Default constructor using message type field.
     * @param messageType
     */
    public ControlMessageValidationContext(String messageType) {
        this.messageType = messageType;
    }

    /**
     * Gets the control message in particular builds the control message with 
     * defined message builder implementation.
     * 
     * @param context the current test context.
     * @return the controlMessage
     */
    public Message getControlMessage(TestContext context) {
        return messageBuilder.buildMessageContent(context, messageType);
    }
    
    /**
     * Sets a static control message for this validation context.
     * @param controlMessage the static control message.
     */
    public void setControlMessage(Message controlMessage) {
        messageBuilder = StaticMessageContentBuilder.withMessage(controlMessage);
    }

    /**
     * Sets the control message builder.
     * @param messageBuilder the messageBuilder to set
     */
    public void setMessageBuilder(MessageContentBuilder messageBuilder) {
        this.messageBuilder = messageBuilder;
    }

    /**
     * Gets the messageBuilder.
     * @return the messageBuilder
     */
    public MessageContentBuilder getMessageBuilder() {
        return messageBuilder;
    }

    @Override
    public String getMessageType() {
        return messageType;
    }
}
