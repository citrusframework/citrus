package com.consol.citrus.report;

import com.consol.citrus.TestCase;

public interface TestListener {
    /**
     * Invoked when test gets started
     * @param test
     */
    public void onTestStart(TestCase test);

    /**
     * Invoked when test gets finished
     * @param test
     */
    public void onTestFinish(TestCase test);

    /**
     * Invoked when test finished with success
     * @param test
     */
    public void onTestSuccess(TestCase test);

    /**
     * Invoked when test finished with failure
     * @param test
     */
    public void onTestFailure(TestCase test, Throwable cause);

    /**
     * Invoked when test is skipped
     * @param test
     */
    public void onTestSkipped(TestCase test);
}
