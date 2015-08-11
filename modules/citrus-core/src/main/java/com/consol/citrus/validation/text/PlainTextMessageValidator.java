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

package com.consol.citrus.validation.text;

import com.consol.citrus.message.Message;
import com.consol.citrus.validation.ControlMessageValidationContext;
import org.springframework.util.Assert;

import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.ValidationException;
import com.consol.citrus.message.MessageType;
import com.consol.citrus.validation.ControlMessageValidator;

/**
 * Plain text validator using simple String comparison.
 * 
 * @author Christoph Deppisch
 */
public class PlainTextMessageValidator extends ControlMessageValidator {

    @Override
    public void validateMessagePayload(Message receivedMessage, Message controlMessage,
                                       ControlMessageValidationContext validationContext, TestContext context) throws ValidationException {
        log.info("Start plain text message validation");
        
        if (log.isDebugEnabled()) {
            log.debug("Received message:\n" + receivedMessage);
            log.debug("Control message:\n" + controlMessage);
        }
        
        try {
            validateText(receivedMessage.getPayload(String.class).trim(),
                    context.replaceDynamicContentInString(controlMessage.getPayload(String.class).trim()));
        } catch (IllegalArgumentException e) {
            throw new ValidationException("Failed to validate plain text", e);
        }
        
        log.info("Plain text validation finished successfully: All values OK");
    }
    
    /**
     * Compares two string with each other in order to validate plain text.
     * 
     * @param receivedMessagePayload
     * @param controlMessagePayload
     */
    private void validateText(String receivedMessagePayload, String controlMessagePayload) {
        Assert.isTrue(receivedMessagePayload.equals(controlMessagePayload),
                "Plain text values not equal, expected '" + controlMessagePayload + "' " +
                		"but was '" + receivedMessagePayload + "'");
    }

    @Override
    public boolean supportsMessageType(String messageType, Message message) {
        return messageType.equalsIgnoreCase(MessageType.PLAINTEXT.toString());
    }
}
