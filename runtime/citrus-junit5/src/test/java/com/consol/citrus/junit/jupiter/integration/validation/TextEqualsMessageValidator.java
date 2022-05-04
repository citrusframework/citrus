package com.consol.citrus.junit.jupiter.integration.validation;

import com.consol.citrus.context.TestContext;
import com.consol.citrus.message.Message;
import com.consol.citrus.validation.DefaultMessageValidator;
import com.consol.citrus.validation.context.ValidationContext;
import org.testng.Assert;

/**
 * Basic message validator performs String equals on received message payloads. We add this validator in order to have a
 * matching message validation strategy for integration tests in this module.
 * @author Christoph Deppisch
 */
public class TextEqualsMessageValidator extends DefaultMessageValidator {

    @Override
    public void validateMessage(Message receivedMessage, Message controlMessage, TestContext context, ValidationContext validationContext) {
        Assert.assertEquals(receivedMessage.getPayload(String.class), controlMessage.getPayload(String.class), "Validation failed - " +
                "expected message contents not equal!");
    }

    @Override
    public boolean supportsMessageType(String messageType, Message message) {
        return true;
    }
}
