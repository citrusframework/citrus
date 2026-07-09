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
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.citrusframework.CitrusSettings;
import org.citrusframework.CitrusVersion;
import org.citrusframework.TestAction;
import org.citrusframework.TestCase;
import org.citrusframework.TestResult;
import org.citrusframework.common.Described;
import org.citrusframework.common.ShutdownPhase;
import org.citrusframework.container.TestActionContainer;
import org.citrusframework.context.TestContext;
import org.citrusframework.log.CitrusLogSettings;
import org.citrusframework.log.LogColors;
import org.citrusframework.message.Message;
import org.citrusframework.message.MessageDirection;
import org.citrusframework.message.MessagePayloadUtils;
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
public class LoggingReporter extends AbstractTestReporter implements MessageListener, TestSuiteListener,
        TestListener, TestActionListener, ShutdownPhase {

    private static Logger logger = LoggerFactory.getLogger(LoggingReporter.class);

    private static Logger inboundMessageLogger = LoggerFactory.getLogger(LoggingReporter.class.getName() + ".Message_IN");
    private static Logger outboundMessageLogger = LoggerFactory.getLogger(LoggingReporter.class.getName() + ".Message_OUT");

    private static final Logger noOpLogger = new NOPLoggerFactory().getLogger(LoggingReporter.class.getName());

    private static final AtomicBoolean initialized = new AtomicBoolean(false);

    private static final String DOUBLE_SEPARATOR = "═".repeat(68);
    private static final String SINGLE_SEPARATOR = "─".repeat(68);

    private static final String INDICATOR_START = "➤";
    private static final String INDICATOR_SUCCESS = "✔";
    private static final String INDICATOR_FAILED = "✘";
    private static final String INDICATOR_SKIPPED = "⊘";
    private static final String ARROW_OUTBOUND = "→";
    private static final String ARROW_INBOUND = "←";

    private final boolean stackTraceOutputEnabled;

    protected static void initialized(boolean value) {
        LoggingReporter.initialized.set(value);
    }

    private static String resolveActionName(TestAction testAction) {
        return hasText(testAction.getName()) ? testAction.getName() : testAction.getClass().getSimpleName();
    }

    private static String formatDuration(TestCase test) {
        if (nonNull(test.getTestResult()) && nonNull(test.getTestResult().getDuration())) {
            return test.getTestResult().getDuration().toMillis() + "ms";
        }
        return "";
    }

    public LoggingReporter() {
        this(CitrusSettings.isStackTraceOutputEnabled());
    }

    public LoggingReporter(boolean stackTraceOutputEnabled) {
        this.stackTraceOutputEnabled = stackTraceOutputEnabled;
    }

    public boolean isStackTraceOutputEnabled() {
        return stackTraceOutputEnabled;
    }

    @Override
    public void generate(TestResults testResults) {
        newLine();
        info(LogColors.bold((testResults.getFailed() > 0 ? LogColors.failed(INDICATOR_FAILED) : LogColors.success(INDICATOR_SUCCESS)) + " CITRUS TEST RESULTS"));
        newLine();

        testResults.doWithResults(testResult -> {
            info(toFormattedTestResult(testResult));

            if (testResult.isFailed()) {
                info(LogColors.failed(
                        Optional.ofNullable(testResult.getCause())
                                .filter(cause -> hasText(cause.getMessage()))
                                .map(ExceptionUtils::getRootCause)
                                .map(cause -> "    Caused by: " + cause.getClass().getSimpleName() + ": " + cause.getMessage())
                                .orElse("    Caused by: " + Optional.ofNullable(testResult.getErrorMessage()).orElse("Unknown error")))
                );
            }
        });

        newLine();

        info(format("  TOTAL:    %s", testResults.getSize()));
        info(format("  PASSED:   %s (%s%%)", testResults.getSuccess(), testResults.getSuccessPercentageFormatted()));
        if (testResults.getFailed() > 0) {
            info(LogColors.failed(format("  FAILED:   %s (%s%%)", testResults.getFailed(), testResults.getFailedPercentageFormatted())));
        } else {
            info(format("  FAILED:   %s (%s%%)", testResults.getFailed(), testResults.getFailedPercentageFormatted()));
        }
        if (testResults.getSkipped() > 0) {
            info(LogColors.skipped(format("  SKIPPED:  %s (%s%%)", testResults.getSkipped(), testResults.getSkippedPercentageFormatted())));
        }

        info(format("  TIME:     %s ms", testResults.getTotalDuration().toMillis()));

        newLine();
        info(LogColors.bold(DOUBLE_SEPARATOR));
    }

    private String toFormattedTestResult(TestResult testResult) {
        String resultText;
        if (testResult.isSuccess()) {
            resultText = LogColors.success(INDICATOR_SUCCESS) + " SUCCESS";
        } else if (testResult.isFailed()) {
            resultText = LogColors.failed(INDICATOR_FAILED + " FAILED ");
        } else {
            resultText = LogColors.skipped(INDICATOR_SKIPPED + " SKIPPED");
        }
        return format("  %s (%4dms) %s",
                resultText,
                Optional.ofNullable(testResult.getDuration()).orElse(ZERO).toMillis(),
                testResult.getTestName());
    }

    @Override
    public void onTestFailure(TestCase testCase, Throwable cause) {
        newLine();

        info(SINGLE_SEPARATOR);
        var duration = formatDuration(testCase);
        String line = LogColors.failed(INDICATOR_FAILED) + " TEST FAILED: " + testCase.getName();
        if (hasText(duration)) {
            line += LogColors.dim("  " + duration);
        }
        info(line);

        if (stackTraceOutputEnabled) {
            error("    " + LogColors.failed(cause.getClass().getSimpleName() + ": " + cause.getMessage()), cause);
        } else {
            info(LogColors.failed("    " + cause.getClass().getSimpleName() + ": " + cause.getMessage()));
        }

        info(SINGLE_SEPARATOR);
        newLine();
    }

    @Override
    public void onTestSkipped(TestCase test) {
        newLine();
        info(SINGLE_SEPARATOR);
        info(LogColors.skipped(INDICATOR_SKIPPED) + " TEST SKIPPED: " + test.getName());
        info(SINGLE_SEPARATOR);
        newLine();
    }

    @Override
    public void onTestStart(TestCase test) {
        newLine();
        info(SINGLE_SEPARATOR);
        info(LogColors.start(INDICATOR_START) + " TEST START: " + test.getName() + " (" + test.getPackageName() + ")");
        info(SINGLE_SEPARATOR);
        newLine();
    }

    @Override
    public void onTestExecutionEnd(TestCase test) {
        // do nothing
    }

    @Override
    public void onTestSuccess(TestCase test) {
        newLine();
        info(SINGLE_SEPARATOR);

        var duration = formatDuration(test);
        String line = LogColors.success(INDICATOR_SUCCESS) + " TEST SUCCESS: " + test.getName();
        if (hasText(duration)) {
            line += LogColors.dim("  " + duration);
        }
        info(line);

        info(SINGLE_SEPARATOR);
        newLine();
    }

    @Override
    public void onFinish() {
        if (isDebugEnabled()) {
            debug(SINGLE_SEPARATOR);
            debug(LogColors.start(INDICATOR_START) + " TEST SUITE FINISH");
            debug(SINGLE_SEPARATOR);
            newLine();
        }
    }

    @Override
    public void onStart() {
        if (!initialized.getAndSet(true)) {
            printBanner();
        }

        separator();

        if (isDebugEnabled()) {
            newLine();
            debug(SINGLE_SEPARATOR);
            debug(LogColors.start(INDICATOR_START) + " TEST SUITE START");
            debug(SINGLE_SEPARATOR);
        }
    }

    private void printBanner() {
        if (!CitrusSettings.isPrintBanner()) {
            return;
        }

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
        info(SINGLE_SEPARATOR);
        info(LogColors.failed(INDICATOR_FAILED) + " TEST SUITE FINISH: FAILED");

        if (stackTraceOutputEnabled) {
            error("    " + LogColors.failed(cause.getClass().getSimpleName() + ": " + cause.getMessage()), cause);
        } else {
            info(LogColors.failed("    " + cause.getClass().getSimpleName() + ": " + cause.getMessage()));
        }

        info(SINGLE_SEPARATOR);
        newLine();
    }

    @Override
    public void onFinishSuccess() {
        debug(SINGLE_SEPARATOR);
        debug(LogColors.success(INDICATOR_SUCCESS) + " TEST SUITE FINISHED");
        debug(SINGLE_SEPARATOR);
        newLine();
    }

    @Override
    public void onStartFailure(Throwable cause) {
        newLine();
        info(SINGLE_SEPARATOR);
        info(LogColors.failed(INDICATOR_FAILED) + " TEST SUITE START: FAILED");

        if (stackTraceOutputEnabled) {
            error("    " + LogColors.failed(cause.getClass().getSimpleName() + ": " + cause.getMessage()), cause);
        } else {
            info(LogColors.failed("    " + cause.getClass().getSimpleName() + ": " + cause.getMessage()));
        }

        info(SINGLE_SEPARATOR);
    }

    @Override
    public void onStartSuccess() {
        newLine();
        debug(SINGLE_SEPARATOR);
        debug(LogColors.success(INDICATOR_SUCCESS) + " TEST SUITE STARTED");
        debug(SINGLE_SEPARATOR);
    }

    @Override
    public void onTestActionStart(TestCase testCase, TestAction testAction) {
        String actionName = resolveActionName(testAction);

        if (testAction instanceof TestActionContainer container) {
            info(LogColors.start(INDICATOR_START) + " " + actionName + " - container with (" + container.getActionCount() + ") embedded actions");
        } else {
            debug(LogColors.start(INDICATOR_START) + " " + actionName);
        }

        if (testAction instanceof Described described && hasText(described.getDescription())) {
            debug("    " + LogColors.dim(described.getDescription()));
        }
    }

    @Override
    public void onTestActionFinish(TestCase testCase, TestAction testAction) {
        String actionName = resolveActionName(testAction);

        var duration = formatDuration(testCase);
        String line = LogColors.success(INDICATOR_SUCCESS) + " " + actionName;
        if (hasText(duration)) {
            line += LogColors.dim("  " + duration);
        }
        info(line);
    }

    @Override
    public void onTestActionFailed(TestCase testCase, TestAction testAction, Throwable cause) {
        String actionName = resolveActionName(testAction);

        var duration = formatDuration(testCase);
        String line = LogColors.failed(INDICATOR_FAILED) + " " + actionName;
        if (hasText(duration)) {
            line += LogColors.dim("  " + duration);
        }
        info(line);
        info("    " + LogColors.failed(cause.getClass().getSimpleName() + ": " + cause.getMessage()));
    }

    @Override
    public void onTestActionSkipped(TestCase testCase, TestAction testAction) {
        String actionName = resolveActionName(testAction);
        debug(LogColors.skipped(INDICATOR_SKIPPED) + " " + actionName);
    }

    @Override
    public void onInboundMessage(Message message, TestContext context) {
        if (inboundMessageLogger.isDebugEnabled() || CitrusLogSettings.isPrintInboundMessageContentEnabled()) {
            logMessage(message, context, MessageDirection.INBOUND);
        } else {
            logMessageSummary(message, MessageDirection.INBOUND);
        }
    }

    @Override
    public void onOutboundMessage(Message message, TestContext context) {
        if (outboundMessageLogger.isDebugEnabled() || CitrusLogSettings.isPrintOutboundMessageContentEnabled()) {
            logMessage(message, context, MessageDirection.OUTBOUND);
        } else {
            logMessageSummary(message, MessageDirection.OUTBOUND);
        }
    }

    private void logMessage(Message message, TestContext context, MessageDirection direction) {
        String messageContent = message.print(context);
        String directionArrow = direction == MessageDirection.OUTBOUND
                ? LogColors.arrow(ARROW_OUTBOUND)
                : LogColors.arrow(ARROW_INBOUND);

        messageLogger(direction).info("{} {} MESSAGE {}", directionArrow, direction.name(), messageContent);
    }

    private void logMessageSummary(Message message, MessageDirection direction) {
        String directionArrow = direction == MessageDirection.OUTBOUND
                ? LogColors.arrow(ARROW_OUTBOUND)
                : LogColors.arrow(ARROW_INBOUND);

        messageLogger(direction).info("{} {} MESSAGE {}", directionArrow, direction.name(), LogColors.dim("(" + MessagePayloadUtils.sizeInfo(message) + ")"));
    }

    protected Logger messageLogger(MessageDirection direction) {
        return switch (direction) {
            case INBOUND -> inboundMessageLogger;
            case OUTBOUND -> outboundMessageLogger;
            case UNBOUND -> logger;
        };
    }

    protected void separator() {
        info(DOUBLE_SEPARATOR);
    }

    protected void newLine() {
        info("");
    }

    protected void info(String line) {
        logger.info(line);
    }

    protected void error(String line) {
        logger.error(line);
    }

    protected void error(String line, Throwable cause) {
        logger.error(line, cause);
    }

    protected void debug(String line) {
        if (isDebugEnabled()) {
            logger.debug(line);
        }
    }

    protected boolean isDebugEnabled() {
        return logger.isDebugEnabled();
    }

    public void setEnabled(boolean enabled) {
        if (enabled) {
            logger = LoggerFactory.getLogger(LoggingReporter.class);
            inboundMessageLogger = LoggerFactory.getLogger(LoggingReporter.class.getName() + ".Message_IN");
            outboundMessageLogger = LoggerFactory.getLogger(LoggingReporter.class.getName() + ".Message_OUT");
        } else {
            logger = noOpLogger;
            inboundMessageLogger = noOpLogger;
            outboundMessageLogger = noOpLogger;
        }
    }

    protected boolean isEnabled() {
        return logger != noOpLogger;
    }

    @Override
    public void destroy() {
        initialized(false);
    }

}
