package org.citrusframework.citrus.integration;

import org.citrusframework.citrus.context.TestContext;
import org.citrusframework.citrus.message.Message;
import org.citrusframework.citrus.message.MessageType;
import org.citrusframework.citrus.validation.DefaultMessageValidator;
import org.citrusframework.citrus.validation.context.ValidationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;

/**
 * Basic message validator performs String equals on received message payloads. We add this validator in order to have a
 * matching message validation strategy for integration tests in this module.
 * @author Christoph Deppisch
 */
public class TextEqualsMessageValidator extends DefaultMessageValidator {

    @Override
    public void validateMessage(Message receivedMessage, Message controlMessage, TestContext context, ValidationContext validationContext) {
        Logger log = LoggerFactory.getLogger("TextEqualsMessageValidator");

        Assert.assertEquals(receivedMessage.getPayload(String.class), controlMessage.getPayload(String.class), "Validation failed - " +
                "expected message contents not equal!");

        log.info("Text validation successful: All values OK");
    }

    @Override
    public boolean supportsMessageType(String messageType, Message message) {
        return messageType.equalsIgnoreCase(MessageType.PLAINTEXT.toString());
    }
}
