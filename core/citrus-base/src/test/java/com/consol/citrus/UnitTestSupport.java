package com.consol.citrus;

import com.consol.citrus.context.TestContext;
import com.consol.citrus.context.TestContextFactory;
import com.consol.citrus.functions.DefaultFunctionLibrary;
import com.consol.citrus.validation.matcher.DefaultValidationMatcherLibrary;
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
