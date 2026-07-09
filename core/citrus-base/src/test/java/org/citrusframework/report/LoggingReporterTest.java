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

import java.time.Duration;
import java.util.Locale;
import java.util.function.Consumer;

import org.citrusframework.DefaultTestCase;
import org.citrusframework.actions.EchoAction;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.log.LogColors;
import org.mockito.Mock;
import org.slf4j.Logger;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static org.citrusframework.TestResult.failed;
import static org.citrusframework.TestResult.skipped;
import static org.citrusframework.TestResult.success;
import static org.citrusframework.util.ReflectionHelper.findField;
import static org.citrusframework.util.ReflectionHelper.setField;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.openMocks;

public class LoggingReporterTest {

    @Mock
    private Logger logger;

    private DefaultTestCase test;

    private EchoAction echo;

    private LoggingReporter fixture;

    private AutoCloseable mocks;

    @BeforeMethod
    public void beforeMethod() {
        mocks = openMocks(this);

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
        // Print citrus information on startup
        fixture.onStart();

        verify(logger).info(argThat(a -> a.startsWith("C I T R U S  T E S T S")));

        // Do not print it continuously
        clearInvocations(logger);
        fixture.onStart();

        verify(logger, never()).info(argThat(a -> a.startsWith("C I T R U S  T E S T S")));
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

    @AfterMethod
    void afterMethodTeardown() throws Exception {
        LogColors.resetColorsEnabled();
        mocks.close();
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
