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

package org.citrusframework.agent.listener;

import org.citrusframework.TestCase;
import org.citrusframework.report.AbstractTestListener;
import org.citrusframework.report.TestReporter;
import org.citrusframework.report.TestResults;

public class AgentTestListener extends AbstractTestListener implements TestReporter {

    /** Overall collected test results */
    private TestResults overall = new TestResults();

    /** Pending test results */
    private TestResults pending = new TestResults();

    /** Latest test results */
    private TestResults latest = new TestResults();

    @Override
    public void generateReport(TestResults testResults) {
        this.pending = new TestResults();
        this.latest = testResults;
        testResults.doWithResults(overall::addResult);
    }

    /**
     * Obtains the overall collected test results.
     * @return
     */
    public TestResults getResults() {
        return overall;
    }

    /**
     * Obtains the test results of the latest test run.
     * @return
     */
    public TestResults getLatestResults() {
        return latest;
    }

    /**
     * Obtains the test results of the pending test run.
     * @return
     */
    public TestResults getPendingResults() {
        return pending;
    }

    @Override
    public void onTestSuccess(TestCase test) {
        pending.addResult(test.getTestResult());
    }

    @Override
    public void onTestFailure(TestCase test, Throwable cause) {
        pending.addResult(test.getTestResult());
    }

    @Override
    public void onTestSkipped(TestCase test) {
        pending.addResult(test.getTestResult());
    }

    public void reset() {
        overall = new TestResults();
        latest = new TestResults();
    }

}
