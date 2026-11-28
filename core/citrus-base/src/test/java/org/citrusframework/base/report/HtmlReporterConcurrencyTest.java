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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import org.citrusframework.TestResult;
import org.citrusframework.base.DefaultTestCase;
import org.citrusframework.report.TestResults;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies thread safety of {@link HtmlReporter} and {@link TestResults} when tests finish concurrently.
 */
class HtmlReporterConcurrencyTest {

    private static final int THREADS = 16;
    private static final int TESTS_PER_THREAD = 100;

    @Test
    void shouldRecordAllConcurrentResultsAndRenderReportWithoutRace() throws InterruptedException {
        HtmlReporter reporter = new HtmlReporter();
        TestResults testResults = new TestResults();
        AtomicInteger idSequence = new AtomicInteger();

        List<Throwable> errors = runConcurrently(() -> {
            for (int i = 0; i < TESTS_PER_THREAD; i++) {
                int id = idSequence.incrementAndGet();
                String testName = "concurrentTest_" + id;
                DefaultTestCase testCase = new DefaultTestCase();
                testCase.setName(testName);

                TestResult result;
                if (id % 5 == 0) {
                    Throwable cause = new AssertionError("expected failure " + id);
                    result = TestResult.failed(testName, "ConcurrentIT", cause).withDuration(Duration.ofMillis(5));
                    testCase.setTestResult(result);
                    reporter.onTestStart(testCase);
                    reporter.onTestFailure(testCase, cause);
                } else if (id % 7 == 0) {
                    result = TestResult.skipped(testName, "ConcurrentIT");
                    testCase.setTestResult(result);
                    reporter.onTestSkipped(testCase);
                } else {
                    result = TestResult.success(testName, "ConcurrentIT").withDuration(Duration.ofMillis(3));
                    testCase.setTestResult(result);
                    reporter.onTestStart(testCase);
                    reporter.onTestSuccess(testCase);
                }
                reporter.onTestEnd(testCase);
                testResults.addResult(result);

                // Exercise concurrent reads on TestResults while other threads add results
                assertThat(testResults.getTotalDuration()).isNotNull();
                assertThat(testResults.getSuccessPercentageFormatted()).isNotEmpty();
            }
        });

        assertThat(errors).isEmpty();
        int expectedTotal = THREADS * TESTS_PER_THREAD;
        assertThat(testResults.getSize()).isEqualTo(expectedTotal);

        String content = reporter.getReportContent(testResults);
        for (int id = 1; id <= expectedTotal; id++) {
            assertThat(content).contains("concurrentTest_" + id);
        }
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
