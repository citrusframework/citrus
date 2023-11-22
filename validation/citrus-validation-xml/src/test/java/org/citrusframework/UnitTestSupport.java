package org.citrusframework;

import java.util.List;

import org.citrusframework.context.TestContext;
import org.citrusframework.context.TestContextFactory;
import org.citrusframework.exceptions.ValidationException;
import org.citrusframework.message.Message;
import org.citrusframework.message.MessageType;
import org.citrusframework.testng.AbstractTestNGUnitTest;
import org.citrusframework.validation.DefaultMessageHeaderValidator;
import org.citrusframework.validation.MessageValidator;
import org.citrusframework.validation.context.ValidationContext;
import org.citrusframework.validation.xhtml.XhtmlMessageValidator;
import org.citrusframework.validation.xhtml.XhtmlXpathMessageValidator;
import org.citrusframework.validation.xml.DomXmlMessageValidator;
import org.citrusframework.validation.xml.XpathMessageValidator;
import org.testng.Assert;

/**
 * @author Christoph Deppisch
 */
public abstract class UnitTestSupport extends AbstractTestNGUnitTest {

    @Override
    protected TestContextFactory createTestContextFactory() {
        TestContextFactory factory = super.createTestContextFactory();
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
