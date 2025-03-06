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

import java.util.Optional;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.citrusframework.CitrusVersion;
import org.citrusframework.TestAction;
import org.citrusframework.TestCase;
import org.citrusframework.TestResult;
import org.citrusframework.common.Described;
import org.citrusframework.container.TestActionContainer;
import org.citrusframework.context.TestContext;
import org.citrusframework.message.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.NOPLoggerFactory;

import static java.lang.String.format;
import static java.time.Duration.ZERO;
import static java.util.Objects.nonNull;
import static org.citrusframework.util.StringUtils.hasText;

/**
 * Simple logging reporter printing test start and ending to the console/logger.
 * <p/>
 * This class provides an option for disablement, allowing you to suppress logging for specific instances
 * and delegate the logging to another facility, which could potentially be a subclass of {@link LoggingReporter}.
 * It's important to note that when an instance of this class is disabled, it will not perform any logging,
 * irrespective of the severity level.
 * <p/>
 * Implementation note: The disablement of the reporter is achieved by using a {@link org.slf4j.helpers.NOPLogger},
 * meaning that this class should primarily focus on logging operations and not extend beyond that functionality.
 */
public class LoggingReporter extends AbstractTestReporter implements MessageListener, TestSuiteListener, TestListener, TestActionListener {

    /**
     * Logger
     */
    private static Logger logger = LoggerFactory.getLogger(LoggingReporter.class);

    /**
     * Inbound message logger
     */
    private static Logger inboundMessageLogger = LoggerFactory.getLogger("Logger.Message_IN");

    /**
     * The inbound message logger used when the reporter is enabled
     */
    private static final Logger enabledInboundMessageLogger = inboundMessageLogger;

    /**
     * Outbound message logger
     */
    private static Logger outboundMessageLogger = LoggerFactory.getLogger("Logger.Message_OUT");

    /**
     * The inbound message logger used when the reporter is enabled
     */
    private static final Logger enabledOutboundMessageLogger = outboundMessageLogger;

    /**
     * The standard logger used when the reporter is enabled
     */
    private static final Logger enabledLog = logger;

    /**
     * A {@link org.slf4j.helpers.NOPLogger} used in case the reporter is not enabled.
     */
    private static final Logger noOpLogger = new NOPLoggerFactory().getLogger(LoggingReporter.class.getName());

    private static boolean isInitialized = false;

    private static void initialized() {
        LoggingReporter.isInitialized = true;
    }

    private static String formatDurationString(TestCase test) {
        return nonNull(test.getTestResult()) && nonNull(test.getTestResult().getDuration()) ? " (" + test.getTestResult().getDuration().toString() + ") " : "";
    }

    @Override
    public void generate(TestResults testResults) {
        newLine();
        info("CITRUS TEST RESULTS");
        newLine();

        testResults.doWithResults(testResult -> {
            info(toFormattedTestResult(testResult));

            if (testResult.isFailed()) {
                info(
                        Optional.ofNullable(testResult.getCause())
                                .filter(cause -> hasText(cause.getMessage()))
                                .map(ExceptionUtils::getRootCause)
                                .map(cause -> "\tCaused by: " + cause.getClass().getSimpleName() + ": " + cause.getMessage())
                                .orElse("\tCaused by: " + Optional.ofNullable(testResult.getErrorMessage()).orElse("Unknown error")));
            }
        });

        newLine();

        info(format("TOTAL:\t\t%s", testResults.getSize()));
        info(format("PASSED:\t\t%s (%s%%)", testResults.getSuccess(), testResults.getSuccessPercentageFormatted()));
        info(format("FAILED:\t\t%s (%s%%)", testResults.getFailed(), testResults.getFailedPercentageFormatted()));

        if (testResults.getSkipped() > 0) {
            info(format("SKIP:\t\t%s (%s%%)", testResults.getSkipped(), testResults.getSkippedPercentageFormatted()));
        }

        info(format("TIME:\t\t%s ms", testResults.getTotalDuration().toMillis()));

        newLine();

        separator();
    }

    private String toFormattedTestResult(TestResult testResult) {
        return format("%s (%6d ms) %s",
                testResult.getResult(),
                Optional.ofNullable(testResult.getDuration()).orElse(ZERO).toMillis(),
                testResult.getTestName());
    }

    @Override
    public void onTestFailure(TestCase testCase, Throwable cause) {
        newLine();

        var duration = formatDurationString(testCase);
        error("TEST FAILED " + testCase.getName() + " <" + testCase.getPackageName() + ">" + duration + " Nested exception is: ", cause);

        separator();
        newLine();
    }

    @Override
    public void onTestSkipped(TestCase test) {
        if (isDebugEnabled()) {
            newLine();
            separator();
            debug("SKIPPING TEST: " + test.getName());
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
    public void onTestExecutionEnd(TestCase test) {
        // do nothing
    }

    @Override
    public void onTestSuccess(TestCase test) {
        newLine();

        var duration = formatDurationString(test);
        info("TEST SUCCESS " + test.getName() + " (" + test.getPackageName() + ")" + duration);

        separator();
        newLine();
    }

    @Override
    public void onFinish() {
        if (isDebugEnabled()) {
            newLine();
            separator();
            debug("AFTER TEST SUITE");
            newLine();
        }
    }

    @Override
    public void onStart() {
        if (!isInitialized) {
            printBanner();
            LoggingReporter.initialized();
        }

        separator();

        if (isDebugEnabled()) {
            debug("BEFORE TEST SUITE");
            newLine();
        }
    }

    private void printBanner() {
        info("       .__  __                       ");
        info("  ____ |__|/  |________ __ __  ______");
        info("_/ ___\\|  \\   __\\_  __ \\  |  \\/  ___/");
        info("\\  \\___|  ||  |  |  | \\/  |  /\\___ \\ ");
        info(" \\___  >__||__|  |__|  |____//____  >");
        info("     \\/                           \\/");

        newLine();
        info("C I T R U S  T E S T S  " + CitrusVersion.version());
        newLine();
    }

    @Override
    public void onFinishFailure(Throwable cause) {
        newLine();
        error("AFTER TEST SUITE: FAILED");
        separator();
        newLine();
    }

    @Override
    public void onFinishSuccess() {
        if (isDebugEnabled()) {
            newLine();
            debug("AFTER TEST SUITE: SUCCESS");
            separator();
            newLine();
        }
    }

    @Override
    public void onStartFailure(Throwable cause) {
        newLine();
        error("BEFORE TEST SUITE: FAILED");
        separator();
        newLine();
    }

    @Override
    public void onStartSuccess() {
        if (isDebugEnabled()) {
            newLine();
            debug("BEFORE TEST SUITE: SUCCESS");
            separator();
            newLine();
        }
    }

    @Override
    public void onTestActionStart(TestCase testCase, TestAction testAction) {
        if (isDebugEnabled()) {
            newLine();
            if (testCase.isIncremental()) {
                debug("TEST STEP " + (testCase.getExecutedActions().size() + 1) + ": " + (testAction.getName() != null ? testAction.getName() : testAction.getClass().getName()));
            } else {
                debug("TEST STEP " + (testCase.getActionIndex(testAction) + 1) + "/" + testCase.getActionCount() + ": " + (testAction.getName() != null ? testAction.getName() : testAction.getClass().getName()));
            }

            if (testAction instanceof TestActionContainer container) {
                debug("TEST ACTION CONTAINER with " + container.getActionCount() + " embedded actions");
            }

            if (testAction instanceof Described described && hasText(described.getDescription())) {
                debug("");
                debug(described.getDescription());
                debug("");
            }
        }
    }

    @Override
    public void onTestActionFinish(TestCase testCase, TestAction testAction) {
        if (isDebugEnabled()) {
            newLine();

            var duration = formatDurationString(testCase);
            if (testCase.isIncremental()) {
                debug("TEST STEP " + (testCase.getExecutedActions().size() + 1) + " SUCCESS" + duration);
            } else {
                debug("TEST STEP " + (testCase.getActionIndex(testAction) + 1) + "/" + testCase.getActionCount() + " SUCCESS" + duration);
            }
        }
    }

    @Override
    public void onTestActionSkipped(TestCase testCase, TestAction testAction) {
        if (isDebugEnabled()) {
            newLine();

            if (testCase.isIncremental()) {
                debug("SKIPPING TEST STEP " + (testCase.getExecutedActions().size() + 1));
            } else {
                debug("SKIPPING TEST STEP " + (testCase.getActionIndex(testAction) + 1) + "/" + testCase.getActionCount());
            }

            debug("TEST ACTION " + (testAction.getName() != null ? testAction.getName() : testAction.getClass().getName()) + " SKIPPED");
        }
    }

    @Override
    public void onInboundMessage(Message message, TestContext context) {
        if (outboundMessageLogger.isDebugEnabled()) {
            inboundMessageLogger.debug(message.print(context));
        }
    }

    @Override
    public void onOutboundMessage(Message message, TestContext context) {
        if (outboundMessageLogger.isDebugEnabled()) {
            outboundMessageLogger.debug(message.print(context));
        }
    }

    /**
     * Helper method to build consistent separators
     */
    protected void separator() {
        info("------------------------------------------------------------------------");
    }

    /**
     * Adds new line to console logging output.
     */
    protected void newLine() {
        info("");
    }

    /**
     * Write info level output.
     */
    protected void info(String line) {
        logger.info(line);
    }

    /**
     * Write error level output.
     */
    protected void error(String line) {
        logger.error(line);
    }

    /**
     * Write error level output.
     */
    protected void error(String line, Throwable cause) {
        logger.error(line, cause);
    }

    /**
     * Write debug level output.
     */
    protected void debug(String line) {
        if (isDebugEnabled()) {
            logger.debug(line);
        }
    }

    /**
     * Is debug level enabled.
     */
    protected boolean isDebugEnabled() {
        return logger.isDebugEnabled();
    }

    /**
     * Sets the enablement state of the reporter.
     */
    public void setEnabled(boolean enabled) {
        if (enabled) {
            logger = enabledLog;
            inboundMessageLogger = enabledInboundMessageLogger;
            outboundMessageLogger = enabledOutboundMessageLogger;
        } else {
            logger = noOpLogger;
            inboundMessageLogger = noOpLogger;
            outboundMessageLogger = noOpLogger;
        }
    }

    protected boolean isEnabled() {
        return logger != noOpLogger;
    }
}
