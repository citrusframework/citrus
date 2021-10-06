package com.consol.citrus;

import java.util.List;

import com.consol.citrus.context.TestContext;
import com.consol.citrus.context.TestContextFactory;
import com.consol.citrus.exceptions.ValidationException;
import com.consol.citrus.functions.DefaultFunctionLibrary;
import com.consol.citrus.message.Message;
import com.consol.citrus.message.MessageType;
import com.consol.citrus.testng.AbstractTestNGUnitTest;
import com.consol.citrus.validation.DefaultMessageHeaderValidator;
import com.consol.citrus.validation.MessageValidator;
import com.consol.citrus.validation.context.ValidationContext;
import com.consol.citrus.validation.json.JsonPathMessageValidator;
import com.consol.citrus.validation.json.JsonTextMessageValidator;
import com.consol.citrus.validation.matcher.DefaultValidationMatcherLibrary;
import org.testng.Assert;

/**
 * @author Christoph Deppisch
 */
public abstract class UnitTestSupport extends AbstractTestNGUnitTest {

    @Override
    protected TestContextFactory createTestContextFactory() {
        TestContextFactory factory = super.createTestContextFactory();
        factory.getFunctionRegistry().addFunctionLibrary(new DefaultFunctionLibrary());
        factory.getValidationMatcherRegistry().addValidationMatcherLibrary(new DefaultValidationMatcherLibrary());

        factory.getMessageValidatorRegistry().addMessageValidator("header", new DefaultMessageHeaderValidator());
        factory.getMessageValidatorRegistry().addMessageValidator("json", new JsonTextMessageValidator());
        factory.getMessageValidatorRegistry().addMessageValidator("jsonPath", new JsonPathMessageValidator());
        factory.getMessageValidatorRegistry().addMessageValidator("plaintext", new MessageValidator<>() {
            @Override
            public void validateMessage(Message receivedMessage, Message controlMessage, TestContext context, List<ValidationContext> validationContexts) throws ValidationException {
                Assert.assertEquals(receivedMessage.getPayload(String.class), controlMessage.getPayload());
            }

            @Override
            public boolean supportsMessageType(String messageType, Message message) {
                return messageType.equalsIgnoreCase(MessageType.PLAINTEXT.name());
            }
        });

        return factory;
    }
}
