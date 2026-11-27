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

package org.citrusframework.base.report;

import java.time.Duration;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import org.citrusframework.actions.AbstractTestAction;
import org.citrusframework.base.DefaultTestCase;
import org.citrusframework.actions.EchoAction;
import org.citrusframework.container.Sequence;
import org.citrusframework.container.SequenceAfterTest;
import org.citrusframework.container.SequenceBeforeTest;
import org.citrusframework.context.TestContext;
import org.citrusframework.context.TestContextFactory;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.log.LogColors;
import org.citrusframework.report.LoggingReporter;
import org.citrusframework.report.TestResults;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.junit.jupiter.api.parallel.Isolated;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static org.citrusframework.TestResult.failed;
import static org.citrusframework.TestResult.skipped;
import static org.citrusframework.TestResult.success;
import static org.citrusframework.util.ReflectionHelper.findField;
import static org.citrusframework.util.ReflectionHelper.getField;
import static org.citrusframework.util.ReflectionHelper.setField;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Isolated("Replaces LoggingReporter's static logger and resets its static banner flag")
@ExtendWith(MockitoExtension.class)
public class LoggingReporterTest {

    @Mock
    private Logger logger;

    private DefaultTestCase test;

    private EchoAction echo;

    private LoggingReporter fixture;

    @BeforeEach
    public void beforeEach() {
        LogColors.setColorEnabled(false);

        test = new DefaultTestCase();
        test.setName("SampleIT");
        test.setPackageName("org.citrusframework.sample");

        echo = new EchoAction.Builder().build();
        echo.setDescription("Test echo action");
        test.addTestAction(echo);

        fixture = new LoggingReporter();

        // Comment this line if you want to see the logs in stdout
        setField(requireNonNull(findField(LoggingReporter.class, "logger")), null, logger);
    }

    @Test
    void onStartPrintsBannerExactlyOnce() {
        // Another test may have printed the banner already: it is printed once per JVM
        ((AtomicBoolean) getField(requireNonNull(findField(LoggingReporter.class, "initialized")), null)).set(false);

        // Print citrus information on startup
        fixture.onStart();

        verify(logger).info(argThat(a -> a.startsWith("C I T R U S  T E S T S")));

        // Do not print it continuously
        clearInvocations(logger);
        fixture.onStart();

        verify(logger, never()).info(argThat(a -> a.startsWith("C I T R U S  T E S T S")));
    }

    @Test
    public void testLoggingReporterContainerStart() {
        fixture.onTestActionStart(test, new Sequence.Builder().build());
        fixture.onTestActionStart(test, new Sequence.Builder().actions(echo).build());

        verify(logger).info("➤ sequential - container");
        verify(logger).info("➤ sequential - container with (1) embedded actions");
    }

    @Test
    public void testLoggingReporterSuccess() {
        fixture.onStart();
        fixture.onStartSuccess();
        fixture.onTestStart(test);
        fixture.onTestActionStart(test, echo);
        fixture.onTestActionFinish(test, echo);
        fixture.onTestExecutionEnd(test);
        fixture.onTestSuccess(test);
        fixture.onFinish();
        fixture.onFinishSuccess();

        verify(logger).info("✔ TEST SUCCESS: SampleIT");

        TestResults testResults = new TestResults();
        testResults.addResult(success("testLoggingReporterSuccess-1", getClass().getSimpleName()).withDuration(Duration.ofMillis(111)));
        testResults.addResult(success("testLoggingReporterSuccess-2", getClass().getSimpleName()).withDuration(Duration.ofMillis(222)));

        fixture.generate(testResults);

        verify(logger).info("  ✔ SUCCESS ( 111ms) testLoggingReporterSuccess-1");
        verify(logger).info("  ✔ SUCCESS ( 222ms) testLoggingReporterSuccess-2");

        verifyResultSummaryLog(2, 2, 0, 0, 333);

        verify(logger, never()).debug(anyString());
    }

    @Test
    public void testLoggingReporterFailed() {
        testLoggingReporterFailed(
                (cause) -> verify(logger).info("    CitrusRuntimeException: Failed!"));
    }

    @Test
    public void testLoggingReporterFailed_withStacktrace() {
        // Override fixture, enable stacktrace output
        fixture = new LoggingReporter(true);

        testLoggingReporterFailed(
                (cause) -> verify(logger).error("    CitrusRuntimeException: Failed!", cause));
    }

    public void testLoggingReporterFailed(Consumer<Throwable> verifyErrorLogInvocation) {
        var nestedException = new CitrusRuntimeException("I am the final boss.");
        var cause = new CitrusRuntimeException("Failed!", nestedException);

        // Re-inject mock logger in case fixture was replaced
        setField(requireNonNull(findField(LoggingReporter.class, "logger")), null, logger);

        fixture.onStart();
        fixture.onStartSuccess();
        fixture.onTestStart(test);
        fixture.onTestActionStart(test, echo);
        fixture.onTestExecutionEnd(test);
        fixture.onTestFailure(test, cause);
        fixture.onFinish();
        fixture.onFinishSuccess();

        verify(logger).info("✘ TEST FAILED: SampleIT");
        verifyErrorLogInvocation.accept(cause);

        TestResults testResults = new TestResults();
        testResults.addResult(failed("testLoggingReporterFailed-1", getClass().getSimpleName(), cause).withDuration(Duration.ofMillis(1234)));
        testResults.addResult(failed("testLoggingReporterFailed-2", getClass().getSimpleName(), nestedException).withDuration(Duration.ofMillis(2345)));
        var customErrorMessage = "custom error message";
        testResults.addResult(failed("testLoggingReporterFailed-3", getClass().getSimpleName(), customErrorMessage).withDuration(Duration.ofMillis(3456)));

        fixture.generate(testResults);

        verify(logger).info("  ✘ FAILED  (1234ms) testLoggingReporterFailed-1");
        verify(logger).info("  ✘ FAILED  (2345ms) testLoggingReporterFailed-2");
        verify(logger, times(2)).info("    Caused by: CitrusRuntimeException: I am the final boss.");
        verify(logger).info("  ✘ FAILED  (3456ms) testLoggingReporterFailed-3");
        verify(logger).info("    Caused by: custom error message");

        verifyResultSummaryLog(3, 0, 3, 0, 7035);

        verify(logger, never()).debug(anyString());
    }

    @Test
    public void testLoggingReporterMiscellaneous() {
        TestResults testResults = new TestResults();
        testResults.addResult(success("testLoggingReporterMiscellaneous-1", getClass().getSimpleName()).withDuration(Duration.ofSeconds(1)));
        testResults.addResult(failed("testLoggingReporterMiscellaneous-2", getClass().getSimpleName(), (String) null).withDuration(Duration.ofNanos(22848329)));

        fixture.generate(testResults);

        verify(logger).info("  ✔ SUCCESS (1000ms) testLoggingReporterMiscellaneous-1");
        verify(logger).info("  ✘ FAILED  (  22ms) testLoggingReporterMiscellaneous-2");
        verify(logger).info("    Caused by: Unknown error");

        verifyResultSummaryLog(2, 1, 1, 0, 1022);

        verify(logger, never()).debug(anyString());
    }

    @Test
    public void testLoggingReporterSkipped() {
        fixture.onStart();
        fixture.onStartSuccess();
        fixture.onTestStart(test);
        fixture.onTestExecutionEnd(test);
        fixture.onTestSuccess(test);
        fixture.onTestSkipped(new DefaultTestCase());
        fixture.onFinish();
        fixture.onFinishSuccess();

        TestResults testResults = new TestResults();
        testResults.addResult(skipped("testLoggingReporterSkipped", getClass().getSimpleName()));

        fixture.generate(testResults);

        verify(logger).info("  ⊘ SKIPPED (   0ms) testLoggingReporterSkipped");

        verifyResultSummaryLog(1, 0, 0, 1, 0);

        verify(logger, never()).debug(anyString());
    }

    @Test
    public void testLoggingReporterBeforeSuiteFailed() {
        fixture.onStart();
        fixture.onStartFailure(new CitrusRuntimeException("Failed!"));
        fixture.onFinish();
        fixture.onFinishSuccess();

        TestResults testResults = new TestResults();
        fixture.generate(testResults);
    }

    @Test
    public void testLoggingReporterAfterSuiteFailed() {
        fixture.onStart();
        fixture.onStartSuccess();
        fixture.onTestStart(test);
        fixture.onTestActionStart(test, echo);
        fixture.onTestActionFinish(test, echo);
        fixture.onTestExecutionEnd(test);
        fixture.onTestSuccess(test);
        fixture.onFinish();
        fixture.onFinishFailure(new CitrusRuntimeException("Failed!"));

        TestResults testResults = new TestResults();
        fixture.generate(testResults);
    }

    @Test
    public void testLoggingReporterBeforeAndAfterTestSequences() {
        TestContext context = TestContextFactory.newInstance().getObject();
        context.getTestListeners().addTestListener(fixture);
        context.getTestActionListeners().addTestActionListener(fixture);

        context.getBeforeTest().add(SequenceBeforeTest.Builder.beforeTest()
                .name("beforeCommon")
                .actions(namedAction("create-variables"), new EchoAction.Builder().build())
                .build());
        context.getBeforeTest().add(SequenceBeforeTest.Builder.beforeTest()
                .name("skippedBefore")
                .onTests("Unmatched*")
                .actions(new EchoAction.Builder().build())
                .build());
        context.getAfterTest().add(SequenceAfterTest.Builder.afterTest()
                .name("afterCommon")
                .actions(namedAction("purge-endpoint"))
                .build());

        test.execute(context);
        test.finish(context);

        InOrder ordered = inOrder(logger);
        ordered.verify(logger).info("➤ TEST START: SampleIT (org.citrusframework.sample)");
        ordered.verify(logger).info("➤ before test (beforeCommon)");
        ordered.verify(logger).info("  ✔ create-variables");
        ordered.verify(logger).info("  ✔ echo");
        ordered.verify(logger).info("");
        ordered.verify(logger).info("✔ echo");
        ordered.verify(logger).info(argThat(line -> line.startsWith("✔ TEST SUCCESS: SampleIT")));
        ordered.verify(logger).info("➤ after test (afterCommon)");
        ordered.verify(logger).info("  ✔ purge-endpoint");

        verify(logger, never()).info("➤ before test (skippedBefore)");
        verify(logger, never()).debug(anyString());
    }

    @Test
    public void testLoggingReporterBeforeAndAfterTestFailures() {
        TestContext context = TestContextFactory.newInstance().getObject();
        context.getTestListeners().addTestListener(fixture);
        context.getTestActionListeners().addTestActionListener(fixture);

        context.getBeforeTest().add(SequenceBeforeTest.Builder.beforeTest()
                .name("beforeFail")
                .actions(namedAction("create-variables"), failingAction("failingSetup", "Setup failed"))
                .build());
        context.getAfterTest().add(SequenceAfterTest.Builder.afterTest()
                .name("afterCleanup")
                .actions(failingAction("failingCleanup", "Cleanup failed"))
                .build());

        try {
            test.execute(context);
        } catch (CitrusRuntimeException expected) {
            // Expected failure in before-test sequence
        } finally {
            test.finish(context);
        }

        verify(logger).info("➤ before test (beforeFail)");
        verify(logger).info("  ✔ create-variables");
        verify(logger).info("  ✘ failingSetup");
        verify(logger).info("      CitrusRuntimeException: Setup failed");
        verify(logger).info("➤ after test (afterCleanup)");
        verify(logger).info("  ✘ failingCleanup");
        verify(logger).info("      CitrusRuntimeException: Cleanup failed");
    }

    @Test
    public void testLoggingReporterNestedContainerDebugLogging() {
        TestContext context = TestContextFactory.newInstance().getObject();
        context.getTestListeners().addTestListener(fixture);
        context.getTestActionListeners().addTestActionListener(fixture);

        DefaultTestCase containerTest = new DefaultTestCase();
        containerTest.setName("ContainerIT");
        containerTest.setPackageName("org.citrusframework.sample");
        containerTest.addTestAction(Sequence.Builder.sequential().actions(new EchoAction.Builder().build()).build());

        when(logger.isDebugEnabled()).thenReturn(false);
        containerTest.execute(context);
        containerTest.finish(context);

        verify(logger).info("➤ sequential - container with (1) embedded actions");
        verify(logger).info("✔ sequential");
        verify(logger, never()).info("  ✔ echo");
        verify(logger, never()).debug(anyString());

        clearInvocations(logger);
        when(logger.isDebugEnabled()).thenReturn(true);

        DefaultTestCase debugTest = new DefaultTestCase();
        debugTest.setName("ContainerDebugIT");
        debugTest.setPackageName("org.citrusframework.sample");
        debugTest.addTestAction(Sequence.Builder.sequential().actions(new EchoAction.Builder().build()).build());

        debugTest.execute(context);
        debugTest.finish(context);

        verify(logger).debug("  ➤ echo");
        verify(logger).debug("  ✔ echo");
        verify(logger, never()).info("  ✔ echo");
    }

    private static AbstractTestAction namedAction(String name) {
        return new AbstractTestAction() {
            {
                setName(name);
            }

            @Override
            public void doExecute(TestContext context) {
            }
        };
    }

    private static AbstractTestAction failingAction(String name, String message) {
        return new AbstractTestAction() {
            {
                setName(name);
            }

            @Override
            public void doExecute(TestContext context) {
                throw new CitrusRuntimeException(message);
            }
        };
    }

    @AfterEach
    void afterEach() throws Exception {
        LogColors.resetColorsEnabled();
    }

    private void verifyResultSummaryLog(int total, int success, int failed, int skipped, long performance) {
        verify(logger).info("  TOTAL:    " + total);
        verify(logger).info("  PASSED:   " + success + " (" + calculatePercentage(total, success) + "%)");
        verify(logger).info("  FAILED:   " + failed + " (" + calculatePercentage(total, failed) + "%)");
        if (skipped > 0) {
            verify(logger).info("  SKIPPED:  " + skipped + " (" + calculatePercentage(total, skipped) + "%)");
        }
        verify(logger).info("  TIME:     " + performance + " ms");
    }

    private String calculatePercentage(int total, int count) {
        if (total == 0 || count == 0) {
            return "0.0";
        }

        double percentage = (double) count / total * 100;
        return format(Locale.US, "%.1f", percentage);
    }
}
