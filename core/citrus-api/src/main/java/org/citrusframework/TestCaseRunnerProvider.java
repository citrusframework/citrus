package org.citrusframework;

import org.citrusframework.context.TestContext;

/**
 *  Interface for providing TestCaseRunner.
 *
 *  @author Thorsten Schlathoelter
 *  @since 4.0
 */
public interface TestCaseRunnerProvider {
    /**
     * Creates a TestCaseRunner which runs the given {@link TestCase} and the given {@link TestContext}.
     * @param testCase
     * @param context
     * @return
     */
    TestCaseRunner createTestCaseRunner(TestCase testCase, TestContext context);

    /**
     * Creates a TestCaseRunner with the given {@link TestContext}.
     * @param context
     * @return
     */
    TestCaseRunner createTestCaseRunner(TestContext context);

}
