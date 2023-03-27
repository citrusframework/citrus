package org.citrusframework.citrus.report;

/**
 * @author Christoph Deppisch
 */
public interface TestSuiteListenerAware {

    /**
     * Adds a new suite listener.
     * @param suiteListener
     */
    void addTestSuiteListener(TestSuiteListener suiteListener);
}
