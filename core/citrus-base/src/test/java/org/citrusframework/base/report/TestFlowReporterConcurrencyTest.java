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

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

import org.citrusframework.TestAction;
import org.citrusframework.TestActionResult;
import org.citrusframework.TestCase;
import org.citrusframework.TestResult;
import org.citrusframework.actions.EchoAction;
import org.citrusframework.base.DefaultTestCase;
import org.citrusframework.container.SequenceAfterTest;
import org.citrusframework.container.SequenceBeforeTest;
import org.citrusframework.context.TestContext;
import org.citrusframework.context.TestContextFactory;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Concurrent runs of test cases with the same name share one reporter instance.
 */
class TestFlowReporterConcurrencyTest {

    private static final String TEST_NAME = "sameTest";

    private static final int THREADS = 16;

    private final TestAction echo = new EchoAction.Builder().build();

    @Test
    void shouldRecordConcurrentRunsWithSameName() throws InterruptedException {
        int runsPerThread = 200;
        int actionsPerRun = 12;
        TestFlowReporter reporter = new TestFlowReporter();

        List<Throwable> errors = runConcurrently(() -> {
            DefaultTestCase testCase = new DefaultTestCase();
            testCase.setName(TEST_NAME);

            for (int run = 0; run < runsPerThread; run++) {
                reporter.onTestStart(testCase);
                for (int i = 0; i < actionsPerRun; i++) {
                    reporter.onTestActionFinish(testCase, echo);
                }
                reporter.onTestEnd(testCase);
            }
        });

        assertThat(errors).isEmpty();
        assertThat(reporter.getJsonReport()).contains("\"name\": \"" + TEST_NAME + "\"");
        assertThat(publishedEntry(reporter).getActions()).hasSize(actionsPerRun);
    }

    @Test
    void shouldCreateOneEntryPerNameConcurrently() throws InterruptedException {
        for (int round = 0; round < 200; round++) {
            TestFlowReporter.TestFlowReport report = new TestFlowReporter.TestFlowReport();
            Set<TestFlowReporter.ReportEntry> entries = Collections.newSetFromMap(Collections.synchronizedMap(new IdentityHashMap<>()));

            List<Throwable> errors = runConcurrently(() -> {
                TestFlowReporter.ReportEntry entry = report.getOrCreateNew(TEST_NAME);
                entries.add(entry);
                entry.addAction(new TestActionResult("echo", "actions.0.echo"));
            });

            assertThat(errors).isEmpty();
            assertThat(entries).hasSize(1);
            assertThat(report.get(TEST_NAME).orElseThrow().getActions()).hasSize(THREADS);
        }
    }

    @Test
    void shouldKeepActionsOfRunningTestWhenSameNamedTestStarts() {
        TestFlowReporter reporter = new TestFlowReporter();
        TestCase first = mockTestCase();
        TestCase second = mockTestCase();

        reporter.onTestStart(first);
        reporter.onTestActionFinish(first, echo);
        reporter.onTestActionFinish(first, echo);

        reporter.onTestStart(second);

        reporter.onTestActionFinish(first, echo);
        reporter.onTestSuccess(first);
        reporter.onTestEnd(first);

        assertThat(publishedEntry(reporter).getActions()).hasSize(3);
    }

    @Test
    void shouldNotReportActionsAsFinalWhenSameNamedTestRunsFinalActions() {
        TestFlowReporter reporter = new TestFlowReporter();
        TestCase first = mockTestCase();
        TestCase second = mockTestCase();

        reporter.onTestStart(first);
        reporter.onTestStart(second);

        reporter.onFinalActionsStart(first);
        reporter.onTestActionFinish(second, echo);
        reporter.onTestSuccess(second);

        TestFlowReporter.ReportEntry entry = publishedEntry(reporter);
        assertThat(entry.getActions()).hasSize(1);
        assertThat(entry.getFinalActions()).isEmpty();
    }

    @Test
    void shouldResetReportWhenTestRunsAgain() {
        TestFlowReporter reporter = new TestFlowReporter();
        TestCase test = mockTestCase();

        reporter.onTestStart(test);
        reporter.onTestActionFinish(test, echo);
        reporter.onTestActionFinish(test, echo);
        reporter.onTestSuccess(test);
        reporter.onTestEnd(test);

        reporter.onTestStart(test);
        reporter.onTestActionFinish(test, echo);

        assertThat(publishedEntry(reporter).getActions()).hasSize(1);
    }

    @Test
    void shouldRecordBeforeAndAfterTestActionsConcurrently() throws InterruptedException {
        int runsPerThread = 50;
        TestFlowReporter reporter = new TestFlowReporter();
        SequenceBeforeTest beforeTest = SequenceBeforeTest.Builder.beforeTest().name("beforeCommon").build();
        SequenceAfterTest afterTest = SequenceAfterTest.Builder.afterTest().name("afterCommon").build();
        TestContext context = TestContextFactory.newInstance().getObject();

        List<Throwable> errors = runConcurrently(() -> {
            DefaultTestCase testCase = new DefaultTestCase();
            testCase.setName(TEST_NAME);

            for (int run = 0; run < runsPerThread; run++) {
                reporter.onTestStart(testCase);
                reporter.onBeforeTestStart(testCase);
                reporter.onNestedActionSuccess(beforeTest, echo, context);
                reporter.onNestedActionSuccess(beforeTest, echo, context);
                reporter.onBeforeTestEnd(testCase);
                reporter.onTestActionFinish(testCase, echo);
                reporter.onTestSuccess(testCase);
                reporter.onAfterTestStart(testCase);
                reporter.onNestedActionSuccess(afterTest, echo, context);
                reporter.onNestedActionSuccess(afterTest, echo, context);
                reporter.onAfterTestEnd(testCase);
                reporter.onTestEnd(testCase);
            }
        });

        assertThat(errors).isEmpty();
        TestFlowReporter.ReportEntry entry = publishedEntry(reporter);
        assertThat(entry.getBeforeTestActions()).hasSize(2);
        assertThat(entry.getActions()).hasSize(1);
        assertThat(entry.getAfterTestActions()).hasSize(2);
        assertThat(reporter.getJsonReport()).contains("\"beforeTest\"").contains("\"afterTest\"");
    }

    @Test
    void shouldIsolateBoundaryActionsAcrossTests() {
        TestFlowReporter reporter = new TestFlowReporter();
        TestCase first = mockTestCase();
        TestCase second = mockTestCase();
        SequenceBeforeTest beforeTest = SequenceBeforeTest.Builder.beforeTest().name("beforeCommon").build();
        TestContext context = TestContextFactory.newInstance().getObject();

        reporter.onTestStart(first);
        reporter.onBeforeTestStart(first);
        reporter.onNestedActionSuccess(beforeTest, echo, context);
        reporter.onBeforeTestEnd(first);

        reporter.onTestStart(second);
        reporter.onTestActionFinish(second, echo);
        reporter.onTestSuccess(second);

        TestFlowReporter.ReportEntry entry = publishedEntry(reporter);
        assertThat(entry.getBeforeTestActions()).isEmpty();
        assertThat(entry.getActions()).hasSize(1);
    }

    private TestCase mockTestCase() {
        TestCase test = Mockito.mock(TestCase.class);
        when(test.getName()).thenReturn(TEST_NAME);
        when(test.getActionIndex(echo)).thenReturn(0);
        when(test.getTestResult()).thenReturn(TestResult.success(TEST_NAME, "SameTest"));
        return test;
    }

    private static TestFlowReporter.ReportEntry publishedEntry(TestFlowReporter reporter) {
        return reporter.getFlowReport().get(TEST_NAME).orElseThrow();
    }

    private static List<Throwable> runConcurrently(Runnable task) throws InterruptedException {
        List<Throwable> errors = Collections.synchronizedList(new ArrayList<>());
        CountDownLatch start = new CountDownLatch(1);
        List<Thread> threads = new ArrayList<>();

        for (int t = 0; t < THREADS; t++) {
            Thread thread = new Thread(() -> {
                try {
                    start.await();
                    task.run();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch (Throwable e) {
                    errors.add(e);
                }
            });
            threads.add(thread);
            thread.start();
        }

        start.countDown();
        for (Thread thread : threads) {
            thread.join();
        }

        return errors;
    }
}
