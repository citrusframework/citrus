package com.consol.citrus.validation;

import com.consol.citrus.context.TestContext;
import com.consol.citrus.message.Message;
import com.consol.citrus.validation.context.ValidationContext;
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

        if (log.isDebugEnabled()) {
            log.debug("Start text equals validation ...");
            log.debug("Received message:\n" + receivedMessage.getPayload(String.class));
            log.debug("Control message:\n" + controlMessage.getPayload(String.class));
        }

        Assert.assertEquals(receivedMessage.getPayload(String.class), controlMessage.getPayload(String.class), "Validation failed - " +
                "expected message contents not equal!");

        log.info("Text validation successful: All values OK");
    }

    @Override
    public boolean supportsMessageType(String messageType, Message message) {
        return true;
    }
}
