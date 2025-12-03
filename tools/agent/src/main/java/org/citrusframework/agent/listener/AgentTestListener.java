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

import java.io.StringWriter;

import org.citrusframework.TestAction;
import org.citrusframework.TestCase;
import org.citrusframework.context.TestContext;
import org.citrusframework.message.Message;
import org.citrusframework.report.TestFlowReporter;
import org.citrusframework.report.OutputStreamReporter;
import org.citrusframework.report.TestResults;

public class AgentTestListener extends OutputStreamReporter {

    /** Overall collected test results */
    private final TestResults overall = new TestResults();

    /** Pending test results */
    private final TestResults pending = new TestResults();

    /** Latest test results */
    private final TestResults latest = new TestResults();

    private StringWriter logWriter;

    private final TestFlowReporter testFlowReporter = new TestFlowReporter();

    public AgentTestListener() {
        this(new StringWriter());
    }

    public AgentTestListener(StringWriter writer) {
        super(writer);
        this.logWriter = writer;
    }

    @Override
    public void generate(TestResults testResults) {
        this.pending.clear();
        this.latest.clear();

        testFlowReporter.generateReport(testResults);

        testResults.doWithResults(result ->{
            latest.addResult(result);
            overall.addResult(result);
        });
        super.generate(testResults);
    }

    /**
     * Obtains the overall collected test results.
     */
    public TestResults getResults() {
        return overall;
    }

    /**
     * Obtains the test results of the latest test run.
     */
    public TestResults getLatestResults() {
        return latest;
    }

    /**
     * Obtains the test results of the pending test run.
     */
    public TestResults getPendingResults() {
        return pending;
    }

    @Override
    public void onStart() {
        // Make sure to print the Citrus banner all the time.
        initialized(false);
        super.onStart();
    }

    @Override
    public void onTestActionFinish(TestCase testCase, TestAction testAction) {
        testFlowReporter.onTestActionFinish(testCase, testAction);
        super.onTestActionFinish(testCase, testAction);
    }

    @Override
    public void onTestActionFailed(TestCase testCase, TestAction testAction, Throwable cause) {
        testFlowReporter.onTestActionFailed(testCase, testAction, cause);
        super.onTestActionFailed(testCase, testAction, cause);
    }

    @Override
    public void onTestStart(TestCase test) {
        testFlowReporter.onTestSuccess(test);
        super.onTestStart(test);
    }

    @Override
    public void onTestSuccess(TestCase test) {
        pending.addResult(test.getTestResult());
        testFlowReporter.onTestSuccess(test);
        super.onTestSuccess(test);
    }

    @Override
    public void onTestFailure(TestCase test, Throwable cause) {
        pending.addResult(test.getTestResult());
        testFlowReporter.onTestFailure(test, cause);
        super.onTestFailure(test, cause);
    }

    @Override
    public void onTestSkipped(TestCase test) {
        pending.addResult(test.getTestResult());
        testFlowReporter.onTestSkipped(test);
        super.onTestSkipped(test);
    }

    @Override
    public void onInboundMessage(Message message, TestContext context) {
        info("INBOUND_MESSAGE");
        separator();
        info(message.print(context));
        separator();
    }

    @Override
    public void onOutboundMessage(Message message, TestContext context) {
        info("OUTBOUND_MESSAGE");
        separator();
        info(message.print(context));
        separator();
    }

    public void reset() {
        overall.clear();
        latest.clear();
        clearLogs();
        testFlowReporter.clear();
    }

    public String getLogs() {
        return logWriter.toString();
    }

    public void clearLogs() {
        logWriter = new StringWriter();
        setLogWriter(logWriter);
    }

    public String getJsonReport() {
        return testFlowReporter.getJsonReport();
    }

    public String getYamlReport() {
        return testFlowReporter.getYamlReport();
    }

    public TestFlowReporter getTestFlowReporter() {
        return testFlowReporter;
    }
}
