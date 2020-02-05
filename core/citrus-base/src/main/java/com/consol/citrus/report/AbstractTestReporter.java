/*
 * Copyright 2006-2018 the original author or authors.
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

package com.consol.citrus.report;

import com.consol.citrus.TestCase;
import com.consol.citrus.TestResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;

/**
 * @author Christoph Deppisch
 * @since 2.7.4
 */
public abstract class AbstractTestReporter extends AbstractTestListener implements TestReporter, TestListener, TestSuiteListener {

    /** Logger */
    private Logger log = LoggerFactory.getLogger(getClass());

    /** Report output directory */
    @Value("${citrus.report.directory:target/citrus-reports}")
    private String reportDirectory = "target/citrus-reports";

    /** Should clear test results for each test suite */
    @Value("${citrus.report.auto.clear:true}")
    private String autoClear = Boolean.TRUE.toString();

    /** Should ignore errors when creating test report */
    @Value("${citrus.report.ignore.errors:true}")
    private String ignoreErrors = Boolean.TRUE.toString();

    /** Collect test results for overall result overview at the very end of test execution */
    private TestResults testResults = new TestResults();

    @Override
    public void clearTestResults() {
        testResults = new TestResults();
    }

    /**
     * Gets the testResults.
     * @return
     */
    public TestResults getTestResults() {
        return testResults;
    }

    /**
     * Create test report silently just logging errors.
     */
    private void createTestReport() {
        try {
            generateTestResults();
        } catch (Exception e) {
            if (isIgnoreErrors()) {
                log.error("Failed to create test report", e);
            } else {
                throw e;
            }
        }
    }

    @Override
    public void onStart() {
        if (isAutoClear()) {
            clearTestResults();
        }
    }

    @Override
    public void onFinish() {
    }

    @Override
    public void onFinishFailure(Throwable cause) {
        createTestReport();
    }

    @Override
    public void onFinishSuccess() {
        createTestReport();
    }

    @Override
    public void onStartFailure(Throwable cause) {
    }

    @Override
    public void onStartSuccess() {
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

    /**
     * Gets the reportDirectory.
     *
     * @return
     */
    public String getReportDirectory() {
        return reportDirectory;
    }

    /**
     * Sets the reportDirectory.
     *
     * @param reportDirectory
     */
    public void setReportDirectory(String reportDirectory) {
        this.reportDirectory = reportDirectory;
    }

    /**
     * Gets the autoClear.
     *
     * @return
     */
    public boolean isAutoClear() {
        return StringUtils.hasText(autoClear) && autoClear.equalsIgnoreCase(Boolean.TRUE.toString());
    }

    /**
     * Sets the autoClear.
     *
     * @param autoClear
     */
    public void setAutoClear(boolean autoClear) {
        this.autoClear = String.valueOf(autoClear);
    }

    /**
     * Gets the ignoreErrors.
     *
     * @return
     */
    public boolean isIgnoreErrors() {
        return StringUtils.hasText(ignoreErrors) && ignoreErrors.equalsIgnoreCase(Boolean.TRUE.toString());
    }

    /**
     * Sets the ignoreErrors.
     *
     * @param ignoreErrors
     */
    public void setIgnoreErrors(boolean ignoreErrors) {
        this.ignoreErrors = String.valueOf(ignoreErrors);
    }
}
