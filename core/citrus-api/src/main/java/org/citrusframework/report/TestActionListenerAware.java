package org.citrusframework.report;

/**
 * @author Christoph Deppisch
 */
public interface TestActionListenerAware {

    /**
     * Adds a new test action listener.
     * @param listener
     */
    void addTestActionListener(TestActionListener listener);
}
