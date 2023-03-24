package org.citrusframework.report;

/**
 * @author Christoph Deppisch
 */
public interface TestReporterAware {

    /**
     * Adds a new test reporter.
     * @param testReporter
     */
    void addTestReporter(TestReporter testReporter);
}
