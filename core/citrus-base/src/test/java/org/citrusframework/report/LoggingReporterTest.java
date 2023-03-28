/*
 * Copyright 2006-2013 the original author or authors.
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
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 */
public class LoggingReporterTest {

    private DefaultTestCase test = new DefaultTestCase();

    private EchoAction echo = new EchoAction.Builder().build();

    @BeforeClass
    public void setupSampleIT() {
        test.setName("SampleIT");
        test.setPackageName("org.citrusframework.sample");

        echo.setDescription("Test echo action");
        test.addTestAction(echo);
    }

    @Test
    public void testLoggingReporterSuccess() {
        LoggingReporter reporter = new LoggingReporter();

        reporter.onStart();
        reporter.onStartSuccess();
        reporter.onTestStart(test);
        reporter.onTestActionStart(test, echo);
        reporter.onTestActionFinish(test, echo);
        reporter.onTestFinish(test);
        reporter.onTestSuccess(test);
        reporter.onFinish();
        reporter.onFinishSuccess();

        TestResults testResults = new TestResults();
        reporter.generate(testResults);
    }

    @Test
    public void testLoggingReporterFailed() {
        LoggingReporter reporter = new LoggingReporter();

        reporter.onStart();
        reporter.onStartSuccess();
        reporter.onTestStart(test);
        reporter.onTestActionStart(test, echo);
        reporter.onTestFinish(test);
        reporter.onTestFailure(test, new CitrusRuntimeException("Failed!"));
        reporter.onFinish();
        reporter.onFinishSuccess();

        TestResults testResults = new TestResults();
        reporter.generate(testResults);
    }

    @Test
    public void testLoggingReporterSkipped() {
        LoggingReporter reporter = new LoggingReporter();

        reporter.onStart();
        reporter.onStartSuccess();
        reporter.onTestStart(test);
        reporter.onTestFinish(test);
        reporter.onTestSuccess(test);
        reporter.onTestSkipped(new DefaultTestCase());
        reporter.onFinish();
        reporter.onFinishSuccess();

        TestResults testResults = new TestResults();
        reporter.generate(testResults);
    }

    @Test
    public void testLoggingReporterBeforeSuiteFailed() {
        LoggingReporter reporter = new LoggingReporter();

        reporter.onStart();
        reporter.onStartFailure(new CitrusRuntimeException("Failed!"));
        reporter.onFinish();
        reporter.onFinishSuccess();

        TestResults testResults = new TestResults();
        reporter.generate(testResults);
    }

    @Test
    public void testLoggingReporterAfterSuiteFailed() {
        LoggingReporter reporter = new LoggingReporter();

        reporter.onStart();
        reporter.onStartSuccess();
        reporter.onTestStart(test);
        reporter.onTestActionStart(test, echo);
        reporter.onTestActionFinish(test, echo);
        reporter.onTestFinish(test);
        reporter.onTestSuccess(test);
        reporter.onFinish();
        reporter.onFinishFailure(new CitrusRuntimeException("Failed!"));

        TestResults testResults = new TestResults();
        reporter.generate(testResults);
    }
}
