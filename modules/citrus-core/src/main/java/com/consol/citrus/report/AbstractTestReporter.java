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
import org.springframework.beans.factory.annotation.Value;

/**
 * @author Christoph Deppisch
 * @since 2.7.4
 */
public abstract class AbstractTestReporter extends AbstractTestListener implements TestReporter, TestListener {

    /** Report output directory */
    @Value("${citrus.report.directory:target/citrus-reports}")
    private String reportDirectory = "target/citrus-reports";

    /** Collect test results for overall result overview at the very end of test execution */
    private TestResults testResults = new TestResults();

    @Override
    public void clearTestResults() {
        testResults = new TestResults();
    }

    /**
     * Gets the testResults.
     *
     * @return
     */
    public TestResults getTestResults() {
        return testResults;
    }

    @Override
    public void onTestSuccess(TestCase test) {
        testResults.addResult(TestResult.success(test.getName(), test.getParameters()));
    }

    @Override
    public void onTestFailure(TestCase test, Throwable cause) {
        testResults.addResult(TestResult.failed(test.getName(), cause, test.getParameters()));
    }

    @Override
    public void onTestSkipped(TestCase test) {
        testResults.addResult(TestResult.skipped(test.getName(), test.getParameters()));
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
}
