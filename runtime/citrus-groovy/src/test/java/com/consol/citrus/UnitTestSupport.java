package com.consol.citrus;

import com.consol.citrus.context.TestContextFactory;
import com.consol.citrus.functions.DefaultFunctionLibrary;
import com.consol.citrus.testng.AbstractTestNGUnitTest;
import com.consol.citrus.validation.DefaultMessageHeaderValidator;
import com.consol.citrus.validation.TextEqualsMessageValidator;
import com.consol.citrus.validation.matcher.DefaultValidationMatcherLibrary;

/**
 * @author Christoph Deppisch
 */
public abstract class UnitTestSupport extends AbstractTestNGUnitTest {

    @Override
    protected TestContextFactory createTestContextFactory() {
        TestContextFactory factory = super.createTestContextFactory();
        factory.getFunctionRegistry().addFunctionLibrary(new DefaultFunctionLibrary());
        factory.getValidationMatcherRegistry().addValidationMatcherLibrary(new DefaultValidationMatcherLibrary());

        factory.getMessageValidatorRegistry().addMessageValidator("headerValidator", new DefaultMessageHeaderValidator());
        factory.getMessageValidatorRegistry().addMessageValidator("textEqualsMessageValidator", new TextEqualsMessageValidator());
        return factory;
    }
}
