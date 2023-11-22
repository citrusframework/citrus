package org.citrusframework;

import org.citrusframework.context.TestContextFactory;
import org.citrusframework.testng.AbstractTestNGUnitTest;

/**
 * @author Christoph Deppisch
 */
public abstract class UnitTestSupport extends AbstractTestNGUnitTest {

    @Override
    protected TestContextFactory createTestContextFactory() {
        return TestContextFactory.newInstance();
    }
}
