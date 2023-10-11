package org.citrusframework.validation;

import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.ValidationException;
import org.citrusframework.message.Message;
import org.citrusframework.message.MessageType;
import org.citrusframework.validation.context.ValidationContext;

/**
 * Basic message validator performs String equals on received message payloads. We add this validator in order to have a
 * matching message validation strategy for integration tests in this module.
 * @author Christoph Deppisch
 */
public class TextEqualsMessageValidator extends DefaultMessageValidator {

    @Override
    public void validateMessage(Message receivedMessage, Message controlMessage, TestContext context, ValidationContext validationContext) {
        if (!receivedMessage.getPayload(String.class).equals(controlMessage.getPayload(String.class))) {
            throw new ValidationException("Validation failed - expected message contents not equal!");
        }
    }

    @Override
    public boolean supportsMessageType(String messageType, Message message) {
        return messageType.equalsIgnoreCase(MessageType.XML.toString())
                || messageType.equalsIgnoreCase(MessageType.PLAINTEXT.toString());
    }
}
