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

package org.citrusframework.validation.text;

import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.ValidationException;
import org.citrusframework.message.Message;
import org.citrusframework.message.MessageType;
import org.citrusframework.validation.context.ValidationContext;
import org.apache.commons.codec.binary.Base64;

/**
 * Message validator automatically converts received binary data message payload to base64 String. Assumes control
 * message payload is also base64 encoded String so we can compare the text data with normal plain text validation.
 * 
 * @author Christoph Deppisch
 */
public class BinaryBase64MessageValidator extends PlainTextMessageValidator {

    @Override
    public void validateMessage(Message receivedMessage, Message controlMessage,
                                TestContext context, ValidationContext validationContext) throws ValidationException {
        if (receivedMessage.getPayload() instanceof byte[]) {
            receivedMessage.setPayload(Base64.encodeBase64String(receivedMessage.getPayload(byte[].class)));
        }

        super.validateMessage(receivedMessage, controlMessage, context, validationContext);
    }
    
    @Override
    public boolean supportsMessageType(String messageType, Message message) {
        return messageType.equalsIgnoreCase(MessageType.BINARY_BASE64.toString());
    }
}
