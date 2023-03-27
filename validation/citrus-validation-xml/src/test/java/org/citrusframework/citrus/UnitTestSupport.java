package org.citrusframework.citrus;

import java.util.List;

import org.citrusframework.citrus.context.TestContext;
import org.citrusframework.citrus.context.TestContextFactory;
import org.citrusframework.citrus.exceptions.ValidationException;
import org.citrusframework.citrus.functions.DefaultFunctionLibrary;
import org.citrusframework.citrus.message.Message;
import org.citrusframework.citrus.message.MessageType;
import org.citrusframework.citrus.testng.AbstractTestNGUnitTest;
import org.citrusframework.citrus.validation.DefaultMessageHeaderValidator;
import org.citrusframework.citrus.validation.MessageValidator;
import org.citrusframework.citrus.validation.context.ValidationContext;
import org.citrusframework.citrus.validation.matcher.DefaultValidationMatcherLibrary;
import org.citrusframework.citrus.validation.xhtml.XhtmlMessageValidator;
import org.citrusframework.citrus.validation.xhtml.XhtmlXpathMessageValidator;
import org.citrusframework.citrus.validation.xml.DomXmlMessageValidator;
import org.citrusframework.citrus.validation.xml.XpathMessageValidator;
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
        factory.getMessageValidatorRegistry().addMessageValidator("xml", new DomXmlMessageValidator());
        factory.getMessageValidatorRegistry().addMessageValidator("xpath", new XpathMessageValidator());
        factory.getMessageValidatorRegistry().addMessageValidator("xhtml", new XhtmlMessageValidator());
        factory.getMessageValidatorRegistry().addMessageValidator("xhtmlXpath", new XhtmlXpathMessageValidator());
        factory.getMessageValidatorRegistry().addMessageValidator("plaintext", new MessageValidator<ValidationContext>() {
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
