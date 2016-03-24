/*
 *  Copyright 2006-2016 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.consol.citrus.validation.text;

import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.ValidationException;
import com.consol.citrus.message.Message;
import com.consol.citrus.message.MessageType;
import com.consol.citrus.validation.context.ValidationContext;
import org.apache.commons.codec.binary.Base64;

/**
 * Message validator automatically converts received binary data message payload to base64 String. Assumes control
 * message payload is also base64 encoded String so we can compare the text data with normal plain text validation.
 * 
 * @author Christoph Deppisch
 */
public class BinaryBase64MessageValidator extends PlainTextMessageValidator {

    @Override
    public void validateMessagePayload(Message receivedMessage, Message controlMessage,
                                       ValidationContext validationContext, TestContext context) throws ValidationException {

        if (receivedMessage.getPayload() instanceof byte[]) {
            receivedMessage.setPayload(Base64.encodeBase64String(receivedMessage.getPayload(byte[].class)));
        }

        super.validateMessagePayload(receivedMessage, controlMessage, validationContext, context);
    }
    
    @Override
    public boolean supportsMessageType(String messageType, Message message) {
        return messageType.equalsIgnoreCase(MessageType.BINARY_BASE64.toString());
    }
}
