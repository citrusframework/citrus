package org.citrusframework;

/**
 * @author Christoph Deppisch
 */
public interface TestCaseRunner extends TestCaseBuilder, GherkinTestActionRunner {

    /**
     * Starts the test case execution.
     */
    void start();

    /**
     * Stops test case execution.
     */
    void stop();
}
