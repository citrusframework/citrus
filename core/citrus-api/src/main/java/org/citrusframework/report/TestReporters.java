/*
 * Copyright the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.citrusframework.report;

import org.citrusframework.TestCase;

import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.unmodifiableList;
import static java.util.Objects.nonNull;

public class TestReporters implements TestListener, TestSuiteListener, TestReporterAware {

    /**
     * Should clear test results for each test suite
     */
    private boolean autoClear = TestReporterSettings.isAutoClear();

    /**
     * List of test listeners
     **/
    private final List<TestReporter> testReporters = new ArrayList<>();

    /**
     * Collect test results for overall result overview at the very end of test execution
     */
    private TestResults testResults = new TestResults();

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
    public void onTestExecutionEnd(TestCase test) {
        if (nonNull(test.getTestResult())) {
            testResults.addResult(test.getTestResult());
        }
    }

    @Override
    public void onTestSuccess(TestCase test) {
        // do nothing
    }

    @Override
    public void onTestFailure(TestCase test, Throwable cause) {
        // do nothing
    }

    @Override
    public void onTestSkipped(TestCase test) {
        // do nothing
    }

    @Override
    public void addTestReporter(TestReporter testReporter) {
        this.testReporters.add(testReporter);
    }

    /**
     * Obtains the testReporters.
     */
    public List<TestReporter> getTestReporters() {
        return unmodifiableList(testReporters);
    }

    /**
     * Obtains the autoClear.
     */
    public boolean isAutoClear() {
        return autoClear;
    }

    /**
     * Specifies the autoClear.
     */
    public void setAutoClear(boolean autoClear) {
        this.autoClear = autoClear;
    }

    /**
     * Gets the testResults.
     */
    public TestResults getTestResults() {
        return testResults;
    }

    /**
     * Call each reporter to generate its reports. Ignore errors according to global setting.
     */
    private void generateReports() {
        for (TestReporter reporter : testReporters) {
            reporter.generateReport(testResults);
        }
    }
}
