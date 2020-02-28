package com.consol.citrus.ws;

import com.consol.citrus.context.TestContextFactory;
import com.consol.citrus.functions.DefaultFunctionLibrary;
import com.consol.citrus.testng.AbstractTestNGUnitTest;
import com.consol.citrus.validation.matcher.DefaultValidationMatcherLibrary;

/**
 * @author Christoph Deppisch
 */
public abstract class UnitTestSupport extends AbstractTestNGUnitTest {

    @Override
    protected TestContextFactory createTestContextFactory() {
        TestContextFactory factory = super.createTestContextFactory();
        factory.getFunctionRegistry().addFunctionLibrary(new DefaultFunctionLibrary());
        factory.getValidationMatcherRegistry().getValidationMatcherLibraries().add(new DefaultValidationMatcherLibrary());
        return factory;
    }
}
