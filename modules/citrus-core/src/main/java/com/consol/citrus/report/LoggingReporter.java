/*
 * Copyright 2006-2010 the original author or authors.
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

import com.consol.citrus.*;
import com.consol.citrus.container.TestActionContainer;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.message.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

/**
 * Simple logging reporter printing test start and ending to the console/logger.
 * 
 * @author Christoph Deppisch
 */
public class LoggingReporter implements MessageListener, TestSuiteListener, TestListener, TestActionListener, TestReporter {
    
    /** Collect test results for overall result overview at the very end of test execution */
    private TestResults testResults = new TestResults();

    /** Inbound message logger */
    private static Logger inboundMsgLogger = LoggerFactory.getLogger("Logger.Message_IN");

    /** Outbound message logger */
    private static Logger outboundMsgLogger = LoggerFactory.getLogger("Logger.Message_OUT");
    
    /** Logger */
    private static Logger log = LoggerFactory.getLogger(Citrus.class);

    @Override
    public void clearTestResults() {
        testResults = new TestResults();
    }

    @Override
    public void generateTestResults() {
        separator();
        newLine();
        log.info("CITRUS TEST RESULTS");
        newLine();

        testResults.doWithResults(new TestResults.ResultCallback() {
            @Override
            public void doWithResult(TestResult testResult) {
                log.info(testResult.toString());

                if (testResult.isFailed()) {
                    log.info(testResult.getFailureCause());
                }
            }
        });

        newLine();
        log.info("Number of skipped tests: " + testResults.getSkipped() + " (" + testResults.getSkippedPercentage() + "%)");
        newLine();

        log.info("TOTAL:\t" + (testResults.getFailed() + testResults.getSuccess()));
        log.info("FAILED:\t" + testResults.getFailed() + " (" + testResults.getFailedPercentage() + "%)");
        log.info("SUCCESS:\t" + testResults.getSuccess() + " (" + testResults.getSuccessPercentage() + "%)");
        newLine();

        separator();
    }

    @Override
    public void onTestFailure(TestCase test, Throwable cause) {
        testResults.addResult(TestResult.failed(test.getName(), cause, test.getParameters()));

        newLine();
        log.error("TEST FAILED " + test.getName() + " <" + test.getPackageName() + "> Nested exception is: ", cause);
        separator();
        newLine();
    }

    @Override
    public void onTestSkipped(TestCase test) {
        newLine();
        separator();
        log.info("SKIPPING TEST: " + test.getName());
        separator();
        newLine();

        testResults.addResult(TestResult.skipped(test.getName(), test.getParameters()));
    }

    @Override
    public void onTestStart(TestCase test) {
        newLine();
        separator();
        log.info("STARTING TEST " + test.getName() + " <" + test.getPackageName() + ">");
        newLine();
    }

    @Override
    public void onTestFinish(TestCase test) {
    }

    @Override
    public void onTestSuccess(TestCase test) {
        testResults.addResult(TestResult.success(test.getName(), test.getParameters()));

        newLine();
        log.info("TEST SUCCESS " + test.getName() + " (" + test.getPackageName() + ")");
        separator();
        newLine();
    }

    @Override
    public void onFinish() {
        newLine();
        separator();
        log.info("AFTER TEST SUITE");
        newLine();
    }

    @Override
    public void onStart() {
        newLine();
        separator();
        newLine();
        log.info("C I T R U S  T E S T S  " + Citrus.getVersion());
        newLine();

        separator();
        log.info("BEFORE TEST SUITE");
        newLine();
    }

    @Override
    public void onFinishFailure(Throwable cause) {
        newLine();
        log.info("AFTER TEST SUITE: FAILED");
        separator();
        newLine();
    }

    @Override
    public void onFinishSuccess() {
        newLine();
        log.info("AFTER TEST SUITE: SUCCESS");
        separator();
        newLine();
    }

    @Override
    public void onStartFailure(Throwable cause) {
        newLine();
        log.info("BEFORE TEST SUITE: FAILED");
        separator();
        newLine();
    }

    @Override
    public void onStartSuccess() {
        newLine();
        log.info("BEFORE TEST SUITE: SUCCESS");
        separator();
        newLine();
    }

    @Override
    public void onTestActionStart(TestCase testCase, TestAction testAction) {
        newLine();
        if (testCase.isTestRunner()) {
            log.info("TEST STEP " + (testCase.getActionIndex(testAction) + 1) + ": " + (testAction.getName() != null ? testAction.getName() : testAction.getClass().getName()));
        } else {
            log.info("TEST STEP " + (testCase.getActionIndex(testAction) + 1) + "/" + testCase.getActionCount() + ": " + (testAction.getName() != null ? testAction.getName() : testAction.getClass().getName()));
        }

        if (testAction instanceof TestActionContainer) {
            log.info("TEST ACTION CONTAINER with " + ((TestActionContainer)testAction).getActionCount() + " embedded actions");
        }

        if (log.isDebugEnabled() && StringUtils.hasText(testAction.getDescription())) {
            log.debug("");
            log.debug(testAction.getDescription());
            log.debug("");
        }
    }

    @Override
    public void onTestActionFinish(TestCase testCase, TestAction testAction) {
        newLine();
        if (testCase.isTestRunner()) {
            log.info("TEST STEP " + (testCase.getActionIndex(testAction) + 1) + " SUCCESS");
        } else {
            log.info("TEST STEP " + (testCase.getActionIndex(testAction) + 1) + "/" + testCase.getActionCount() + " SUCCESS");
        }
    }

    @Override
    public void onTestActionSkipped(TestCase testCase, TestAction testAction) {
        newLine();
        if (testCase.isTestRunner()) {
            log.info("SKIPPING TEST STEP " + (testCase.getActionIndex(testAction) + 1));
        } else {
            log.info("SKIPPING TEST STEP " + (testCase.getActionIndex(testAction) + 1) + "/" + testCase.getActionCount());
        }
        log.info("TEST ACTION " + (testAction.getName() != null ? testAction.getName() : testAction.getClass().getName()) + " SKIPPED");
    }

    @Override
    public void onInboundMessage(Message message, TestContext context) {
        inboundMsgLogger.info(message.toString());
    }

    @Override
    public void onOutboundMessage(Message message, TestContext context) {
        outboundMsgLogger.info(message.toString());
    }

    /**
     * Helper method to build consistent separators
     */
    private void separator() {
        log.info("------------------------------------------------------------------------");
    }

    /**
     * Adds new line to console logging output.
     */
    private void newLine() {
        log.info("");
    }
}
