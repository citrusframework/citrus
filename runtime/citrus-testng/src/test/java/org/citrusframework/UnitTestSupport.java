package org.citrusframework;

import org.citrusframework.context.TestContext;
import org.citrusframework.context.TestContextFactory;
import org.citrusframework.validation.DefaultTextEqualsMessageValidator;
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
        factory.getMessageValidatorRegistry().addMessageValidator("all", new DefaultTextEqualsMessageValidator());
        return factory;
    }
}
