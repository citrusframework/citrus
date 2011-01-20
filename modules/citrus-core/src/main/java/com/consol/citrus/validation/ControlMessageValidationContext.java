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

import org.springframework.integration.core.Message;

import com.consol.citrus.context.TestContext;
import com.consol.citrus.validation.builder.MessageContentBuilder;
import com.consol.citrus.validation.builder.PayloadTemplateMessageBuilder;
import com.consol.citrus.validation.context.ValidationContext;


/**
 * Validation context providing a control message for message validation. 
 * The control message holds expected message content and header information. Message 
 * validators compare the actual message to the control message marking differences.
 * 
 * @author Christoph Deppisch
 */
public class ControlMessageValidationContext implements ValidationContext {
    /** Builder constructing a control message */
    private MessageContentBuilder<?> messageBuilder = new PayloadTemplateMessageBuilder();
    
    /** Control message */
    private Message<?> controlMessage;
    
    /**
     * @param controlMessage the controlMessage to set
     */
    public void setControlMessage(Message<?> controlMessage) {
        this.controlMessage = controlMessage;
    }

    /**
     * Get the control message.
     * @param context the current test context.
     * @return the controlMessage
     */
    public Message<?> getControlMessage(TestContext context) {
        if (controlMessage == null) {
            controlMessage = messageBuilder.buildMessageContent(context);
        }
        
        return controlMessage;
    }
    
    /**
     * Sets the control message builder.
     * @param messageBuilder the messageBuilder to set
     */
    public void setMessageBuilder(MessageContentBuilder<?> messageBuilder) {
        this.messageBuilder = messageBuilder;
    }
   
}
