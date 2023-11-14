package org.citrusframework.ws;

import org.citrusframework.context.TestContextFactory;
import org.citrusframework.functions.DefaultFunctionLibrary;
import org.citrusframework.testng.AbstractTestNGUnitTest;
import org.citrusframework.validation.DefaultTextEqualsMessageValidator;
import org.citrusframework.validation.matcher.DefaultValidationMatcherLibrary;
import org.citrusframework.validation.xml.DomXmlMessageValidator;

/**
 * @author Christoph Deppisch
 */
public abstract class UnitTestSupport extends AbstractTestNGUnitTest {

    @Override
    protected TestContextFactory createTestContextFactory() {
        TestContextFactory factory = super.createTestContextFactory();
        factory.getFunctionRegistry().addFunctionLibrary(new DefaultFunctionLibrary());
        factory.getValidationMatcherRegistry().addValidationMatcherLibrary(new DefaultValidationMatcherLibrary());

        factory.getMessageValidatorRegistry().addMessageValidator("xml", new DomXmlMessageValidator());
        factory.getMessageValidatorRegistry().addMessageValidator("text", new DefaultTextEqualsMessageValidator());
        return factory;
    }
}
