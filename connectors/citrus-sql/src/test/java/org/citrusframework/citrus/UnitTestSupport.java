package org.citrusframework.citrus;

import org.citrusframework.citrus.context.TestContextFactory;
import org.citrusframework.citrus.functions.DefaultFunctionLibrary;
import org.citrusframework.citrus.testng.AbstractTestNGUnitTest;
import org.citrusframework.citrus.validation.matcher.DefaultValidationMatcherLibrary;

/**
 * @author Christoph Deppisch
 */
public abstract class UnitTestSupport extends AbstractTestNGUnitTest {

    @Override
    protected TestContextFactory createTestContextFactory() {
        TestContextFactory factory = super.createTestContextFactory();
        factory.getFunctionRegistry().addFunctionLibrary(new DefaultFunctionLibrary());
        factory.getValidationMatcherRegistry().addValidationMatcherLibrary(new DefaultValidationMatcherLibrary());
        return factory;
    }
}
