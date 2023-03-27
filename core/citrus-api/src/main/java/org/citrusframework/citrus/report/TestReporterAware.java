package org.citrusframework.citrus.report;

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
