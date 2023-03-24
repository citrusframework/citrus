package org.citrusframework;

import org.citrusframework.context.TestContextFactory;
import org.citrusframework.functions.DefaultFunctionLibrary;
import org.citrusframework.testng.AbstractTestNGUnitTest;
import org.citrusframework.validation.matcher.DefaultValidationMatcherLibrary;

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
