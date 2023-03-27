package org.citrusframework.citrus;

import org.citrusframework.citrus.context.TestContext;
import org.citrusframework.citrus.context.TestContextFactory;
import org.citrusframework.citrus.functions.DefaultFunctionLibrary;
import org.citrusframework.citrus.validation.matcher.DefaultValidationMatcherLibrary;
import org.testng.annotations.BeforeMethod;

/**
 * @author Christoph Deppisch
 */
public abstract class UnitTestSupport {

    protected TestContextFactory testContextFactory;
    protected TestContext context;

    /**
     * Setup test execution.
     */
    @BeforeMethod
    public void prepareTest() {
        testContextFactory = createTestContextFactory();
        context = testContextFactory.getObject();
    }

    protected TestContextFactory createTestContextFactory() {
        TestContextFactory factory = TestContextFactory.newInstance();
        factory.getFunctionRegistry().addFunctionLibrary(new DefaultFunctionLibrary());
        factory.getValidationMatcherRegistry().addValidationMatcherLibrary(new DefaultValidationMatcherLibrary());
        return factory;
    }
}
