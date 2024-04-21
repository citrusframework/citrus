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

import org.citrusframework.DefaultTestCase;
import org.citrusframework.actions.EchoAction;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.mockito.Mock;
import org.slf4j.Logger;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.time.Duration;

import static java.lang.String.format;
import static org.citrusframework.TestResult.failed;
import static org.citrusframework.TestResult.skipped;
import static org.citrusframework.TestResult.success;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.openMocks;
import static org.springframework.test.util.ReflectionTestUtils.setField;

/**
 * @author Christoph Deppisch
 */
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

        test = new DefaultTestCase();
        test.setName("SampleIT");
        test.setPackageName("org.citrusframework.sample");

        echo = new EchoAction.Builder().build();
        echo.setDescription("Test echo action");
        test.addTestAction(echo);

        fixture = new LoggingReporter();

        // Comment this line if you want to see the logs in stdout
        setField(fixture, "logger", logger, Logger.class);
    }

    @Test
    public void testLoggingReporterSuccess() {
        fixture.onStart();
        fixture.onStartSuccess();
        fixture.onTestStart(test);
        fixture.onTestActionStart(test, echo);
        fixture.onTestActionFinish(test, echo);
        fixture.onTestFinish(test);
        fixture.onTestSuccess(test);
        fixture.onFinish();
        fixture.onFinishSuccess();

        verify(logger).info("TEST SUCCESS SampleIT (org.citrusframework.sample)");

        TestResults testResults = new TestResults();
        testResults.addResult(success("testLoggingReporterSuccess-1", getClass().getSimpleName()).withDuration(Duration.ofMillis(111)));
        testResults.addResult(success("testLoggingReporterSuccess-2", getClass().getSimpleName()).withDuration(Duration.ofMillis(222)));

        fixture.generate(testResults);

        verify(logger).info("TestResult[testName=testLoggingReporterSuccess-1, result=SUCCESS, durationMs=111]");
        verify(logger).info("TestResult[testName=testLoggingReporterSuccess-2, result=SUCCESS, durationMs=222]");

        verifyResultSummaryLog(2, 2, 0, 333);

        verify(logger, never()).debug(anyString());
    }

    @Test
    public void testLoggingReporterFailed() {
        var cause = new CitrusRuntimeException("Failed!");

        fixture.onStart();
        fixture.onStartSuccess();
        fixture.onTestStart(test);
        fixture.onTestActionStart(test, echo);
        fixture.onTestFinish(test);
        fixture.onTestFailure(test, cause);
        fixture.onFinish();
        fixture.onFinishSuccess();

        verify(logger).error("TEST FAILED SampleIT <org.citrusframework.sample> Nested exception is: ", cause);

        TestResults testResults = new TestResults();
        testResults.addResult(failed("testLoggingReporterFailed-1", getClass().getSimpleName(), cause).withDuration(Duration.ofMillis(1234)));
        testResults.addResult(failed("testLoggingReporterFailed-2", getClass().getSimpleName(), cause).withDuration(Duration.ofMillis(2345)));

        fixture.generate(testResults);

        verify(logger).info("TestResult[testName=testLoggingReporterFailed-1, result=FAILURE, durationMs=1234]");
        verify(logger).info("TestResult[testName=testLoggingReporterFailed-2, result=FAILURE, durationMs=2345]");

        verifyResultSummaryLog(2, 0, 2, 3579);

        verify(logger, never()).debug(anyString());
    }

    @Test
    public void testLoggingReporterMiscellaneous() {
        var cause = new CitrusRuntimeException("Failed!");

        TestResults testResults = new TestResults();
        testResults.addResult(success("testLoggingReporterMiscellaneous-1", getClass().getSimpleName()).withDuration(Duration.ofSeconds(1)));
        testResults.addResult(failed("testLoggingReporterMiscellaneous-2", getClass().getSimpleName(), cause).withDuration(Duration.ofNanos(22848329)));

        fixture.generate(testResults);

        verify(logger).info("TestResult[testName=testLoggingReporterMiscellaneous-1, result=SUCCESS, durationMs=1000]");
        verify(logger).info("TestResult[testName=testLoggingReporterMiscellaneous-2, result=FAILURE, durationMs=22]");

        verifyResultSummaryLog(2, 1, 1, 1022);

        verify(logger, never()).debug(anyString());
    }

    @Test
    public void testLoggingReporterSkipped() {
        fixture.onStart();
        fixture.onStartSuccess();
        fixture.onTestStart(test);
        fixture.onTestFinish(test);
        fixture.onTestSuccess(test);
        fixture.onTestSkipped(new DefaultTestCase());
        fixture.onFinish();
        fixture.onFinishSuccess();

        TestResults testResults = new TestResults();
        testResults.addResult(skipped("testLoggingReporterSkipped", getClass().getSimpleName()));

        fixture.generate(testResults);

        verify(logger).info("TestResult[testName=testLoggingReporterSkipped, result=SKIP]");

        verifyResultSummaryLog(0, 0, 0, 0);

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
        fixture.onTestFinish(test);
        fixture.onTestSuccess(test);
        fixture.onFinish();
        fixture.onFinishFailure(new CitrusRuntimeException("Failed!"));

        TestResults testResults = new TestResults();
        fixture.generate(testResults);
    }

    @AfterMethod
    void afterMethodTeardown() throws Exception {
        mocks.close();
    }

    private void verifyResultSummaryLog(int total, int success, int failed, long performance) {
        verify(logger).info("TOTAL:\t\t\t" + total);
        verify(logger).info("SUCCESS:\t\t" + success + " (" + calculatePercentage(total, success) + "%)");
        verify(logger).info("FAILED:\t\t" + failed + " (" + calculatePercentage(total, failed) + "%)");
        verify(logger).info("PERFORMANCE:\t" + performance + " ms");
    }

    private String calculatePercentage(int total, int success) {
        if (total == 0) {
            return "0.0";
        }

        double percentage = (double) success / total * 100;
        return format("%3.1f", percentage);
    }
}
