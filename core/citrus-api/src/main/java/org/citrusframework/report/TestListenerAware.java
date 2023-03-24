package org.citrusframework.report;

/**
 * @author Christoph Deppisch
 */
public interface TestListenerAware {

    /**
     * Adds a new test listener.
     * @param testListener
     */
    void addTestListener(TestListener testListener);
}
