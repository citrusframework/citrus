package org.citrusframework.report;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.citrusframework.TestCase;
import org.citrusframework.TestResult;

/**
 * @author Christoph Deppisch
 */
public class TestReporters implements TestListener, TestSuiteListener, TestReporterAware {

    /** Should clear test results for each test suite */
    private boolean autoClear = TestReporterSettings.isAutoClear();

    /** List of test listeners **/
    private final List<TestReporter> testReporters = new ArrayList<>();

    /** Collect test results for overall result overview at the very end of test execution */
    private TestResults testResults = new TestResults();

    /**
     * Call each reporter to generate its reports. Ignore errors according to global setting.
     */
    private void generateReports() {
        for (TestReporter reporter : testReporters) {
            reporter.generateReport(testResults);
        }
    }

    @Override
    public void onStart() {
        if (autoClear) {
            // Dismiss previous test results for next test run
            testResults = new TestResults();
        }
    }

    @Override
    public void onFinishFailure(Throwable cause) {
        generateReports();
    }

    @Override
    public void onFinishSuccess() {
        generateReports();
    }

    /**
     * Gets the testResults.
     * @return
     */
    public TestResults getTestResults() {
        return testResults;
    }

    @Override
    public void onFinish() {
        // do nothing
    }

    @Override
    public void onStartFailure(Throwable cause) {
        // do nothing
    }

    @Override
    public void onStartSuccess() {
        // do nothing
    }

    @Override
    public void onTestStart(TestCase test) {
        // do nothing
    }

    @Override
    public void onTestFinish(TestCase test) {
        // do nothing
    }

    @Override
    public void onTestSuccess(TestCase test) {
        testResults.addResult(TestResult.success(test.getName(), test.getTestClass().getName()));
    }

    @Override
    public void onTestFailure(TestCase test, Throwable cause) {
        testResults.addResult(TestResult.failed(test.getName(), test.getTestClass().getName(), cause));
    }

    @Override
    public void onTestSkipped(TestCase test) {
        testResults.addResult(TestResult.skipped(test.getName(), test.getTestClass().getName()));
    }

    @Override
    public void addTestReporter(TestReporter testReporter) {
        this.testReporters.add(testReporter);
    }

    /**
     * Obtains the testReporters.
     * @return
     */
    public List<TestReporter> getTestReporters() {
        return Collections.unmodifiableList(testReporters);
    }

    /**
     * Obtains the autoClear.
     * @return
     */
    public boolean isAutoClear() {
        return autoClear;
    }

    /**
     * Specifies the autoClear.
     * @param autoClear
     */
    public void setAutoClear(boolean autoClear) {
        this.autoClear = autoClear;
    }
}
