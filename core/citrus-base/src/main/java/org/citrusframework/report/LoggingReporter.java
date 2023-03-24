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

package org.citrusframework.report;

import java.util.Optional;

import org.citrusframework.CitrusVersion;
import org.citrusframework.TestAction;
import org.citrusframework.TestCase;
import org.citrusframework.common.Described;
import org.citrusframework.container.TestActionContainer;
import org.citrusframework.context.TestContext;
import org.citrusframework.message.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

/**
 * Simple logging reporter printing test start and ending to the console/logger.
 *
 * @author Christoph Deppisch
 */
public class LoggingReporter extends AbstractTestReporter implements MessageListener, TestSuiteListener, TestListener, TestActionListener {

    /** Inbound message logger */
    private static final Logger INBOUND_MSG_LOGGER = LoggerFactory.getLogger("Logger.Message_IN");

    /** Outbound message logger */
    private static final Logger OUTBOUND_MSG_LOGGER = LoggerFactory.getLogger("Logger.Message_OUT");

    /** Logger */
    private static Logger log = LoggerFactory.getLogger(LoggingReporter.class);

    @Override
    public void generate(TestResults testResults) {
        separator();
        newLine();
        info("CITRUS TEST RESULTS");
        newLine();

        testResults.doWithResults(testResult -> {
            info(testResult.toString());

            if (testResult.isFailed()) {
                info(Optional.ofNullable(testResult.getCause())
                        .filter(cause -> StringUtils.hasText(cause.getMessage()))
                        .map(cause -> " FAILURE: Caused by: " + cause.getClass().getSimpleName() + ": " +  cause.getMessage())
                        .orElse(" FAILURE: Caused by: " + Optional.ofNullable(testResult.getErrorMessage()).orElse("Unknown error")));
            }
        });

        newLine();

        info("TOTAL:\t" + (testResults.getFailed() + testResults.getSuccess()));

        debug("SKIPPED:\t" + testResults.getSkipped() + " (" + testResults.getSkippedPercentage() + "%)");

        info("FAILED:\t" + testResults.getFailed() + " (" + testResults.getFailedPercentage() + "%)");
        info("SUCCESS:\t" + testResults.getSuccess() + " (" + testResults.getSuccessPercentage() + "%)");
        newLine();

        separator();
    }

    @Override
    public void onTestFailure(TestCase test, Throwable cause) {
        newLine();
        error("TEST FAILED " + test.getName() + " <" + test.getPackageName() + "> Nested exception is: ", cause);
        separator();
        newLine();
    }

    @Override
    public void onTestSkipped(TestCase test) {
        if (isDebugEnabled()) {
            newLine();
            separator();
            debug("SKIPPING TEST: " + test.getName());
            separator();
            newLine();
        }
    }

    @Override
    public void onTestStart(TestCase test) {
        if (isDebugEnabled()) {
            newLine();
            separator();
            debug("STARTING TEST " + test.getName() + " <" + test.getPackageName() + ">");
            newLine();
        }
    }

    @Override
    public void onTestFinish(TestCase test) {
        // do nothing
    }

    @Override
    public void onTestSuccess(TestCase test) {
        newLine();
        info("TEST SUCCESS " + test.getName() + " (" + test.getPackageName() + ")");
        separator();
        newLine();
    }

    @Override
    public void onFinish() {
        newLine();
        separator();
        debug("AFTER TEST SUITE");
        newLine();
    }

    @Override
    public void onStart() {
        newLine();
        separator();
        info("       .__  __                       ");
        info("  ____ |__|/  |________ __ __  ______");
        info("_/ ___\\|  \\   __\\_  __ \\  |  \\/  ___/");
        info("\\  \\___|  ||  |  |  | \\/  |  /\\___ \\ ");
        info(" \\___  >__||__|  |__|  |____//____  >");
        info("     \\/                           \\/");

        newLine();
        info("C I T R U S  T E S T S  " + CitrusVersion.version());
        newLine();

        separator();
        debug("BEFORE TEST SUITE");
        newLine();
    }

    @Override
    public void onFinishFailure(Throwable cause) {
        newLine();
        info("AFTER TEST SUITE: FAILED");
        separator();
        newLine();
    }

    @Override
    public void onFinishSuccess() {
        newLine();
        info("AFTER TEST SUITE: SUCCESS");
        separator();
        newLine();
    }

    @Override
    public void onStartFailure(Throwable cause) {
        newLine();
        info("BEFORE TEST SUITE: FAILED");
        separator();
        newLine();
    }

    @Override
    public void onStartSuccess() {
        newLine();
        info("BEFORE TEST SUITE: SUCCESS");
        separator();
        newLine();
    }

    @Override
    public void onTestActionStart(TestCase testCase, TestAction testAction) {
        if (isDebugEnabled()) {
            newLine();
            if (testCase.isIncremental()) {
                debug("TEST STEP " + (testCase.getActionIndex(testAction) + 1) + ": " + (testAction.getName() != null ? testAction.getName() : testAction.getClass().getName()));
            } else {
                debug("TEST STEP " + (testCase.getActionIndex(testAction) + 1) + "/" + testCase.getActionCount() + ": " + (testAction.getName() != null ? testAction.getName() : testAction.getClass().getName()));
            }

            if (testAction instanceof TestActionContainer) {
                debug("TEST ACTION CONTAINER with " + ((TestActionContainer)testAction).getActionCount() + " embedded actions");
            }

            if (testAction instanceof Described &&
                    StringUtils.hasText(((Described) testAction).getDescription())) {
                debug("");
                debug(((Described) testAction).getDescription());
                debug("");
            }
        }
    }

    @Override
    public void onTestActionFinish(TestCase testCase, TestAction testAction) {
        if (isDebugEnabled()) {
            newLine();
            if (testCase.isIncremental()) {
                debug("TEST STEP " + (testCase.getActionIndex(testAction) + 1) + " SUCCESS");
            } else {
                debug("TEST STEP " + (testCase.getActionIndex(testAction) + 1) + "/" + testCase.getActionCount() + " SUCCESS");
            }
        }
    }

    @Override
    public void onTestActionSkipped(TestCase testCase, TestAction testAction) {
        if (isDebugEnabled()) {
            newLine();
            if (testCase.isIncremental()) {
                debug("SKIPPING TEST STEP " + (testCase.getActionIndex(testAction) + 1));
            } else {
                debug("SKIPPING TEST STEP " + (testCase.getActionIndex(testAction) + 1) + "/" + testCase.getActionCount());
            }
            debug("TEST ACTION " + (testAction.getName() != null ? testAction.getName() : testAction.getClass().getName()) + " SKIPPED");
        }
    }

    @Override
    public void onInboundMessage(Message message, TestContext context) {
        INBOUND_MSG_LOGGER.debug(message.print(context));
    }

    @Override
    public void onOutboundMessage(Message message, TestContext context) {
        OUTBOUND_MSG_LOGGER.debug(message.print(context));
    }

    /**
     * Helper method to build consistent separators
     */
    private void separator() {
        info("------------------------------------------------------------------------");
    }

    /**
     * Adds new line to console logging output.
     */
    private void newLine() {
        info("");
    }

    /**
     * Write info level output.
     * @param line
     */
    protected void info(String line) {
        log.info(line);
    }

    /**
     * Write error level output.
     * @param line
     * @param cause
     */
    protected void error(String line, Throwable cause) {
        log.error(line, cause);
    }

    /**
     * Write debug level output.
     * @param line
     */
    protected void debug(String line) {
        if (isDebugEnabled()) {
            log.debug(line);
        }
    }

    /**
     * Is debug level enabled.
     * @return
     */
    protected boolean isDebugEnabled() {
        return log.isDebugEnabled();
    }
}
