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

import org.citrusframework.TestResult;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.time.Duration;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;

import static java.lang.Thread.currentThread;
import static java.util.concurrent.Executors.newFixedThreadPool;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.citrusframework.TestResult.failed;
import static org.citrusframework.TestResult.skipped;
import static org.citrusframework.TestResult.success;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

public class TestResultsTest {

    private TestResults fixture;

    @BeforeMethod
    public void beforeMethodSetup() {
        fixture = new TestResults();
    }

    @Test
    public void addResultIsThreadSafe() throws InterruptedException {
        var numberOfThreads = 10;
        var executorService = newFixedThreadPool(numberOfThreads);
        var barrier = new CyclicBarrier(numberOfThreads);
        var durationToAdd = Duration.ofMillis(100);

        try {
            sumDurations(numberOfThreads, executorService, barrier, durationToAdd);
        } catch (Exception e) {
            fail("Exception occurred while adding up durations!", e);
        } finally {
            executorService.shutdown();
            assertTrue(executorService.awaitTermination(5, SECONDS), "Tasks did not complete in time");
        }

        var expectedDuration = durationToAdd.multipliedBy(numberOfThreads);
        assertEquals(expectedDuration, fixture.getTotalDuration(), "Total duration is incorrect");
    }

    private void sumDurations(int numberOfThreads, ExecutorService executorService, CyclicBarrier barrier, Duration durationToAdd) {
        for (int i = 0; i < numberOfThreads; i++) {
            executorService.submit(() -> {
                try {
                    // Ensure all threads start at the same time
                    barrier.await();

                    var testResult = mock(TestResult.class);

                    doAnswer((invocation) -> durationToAdd).when(testResult).getDuration();

                    fixture.addResult(testResult);
                } catch (InterruptedException | BrokenBarrierException e) {
                    currentThread().interrupt();
                    fail("Thread was interrupted while executing test");
                }
            });
        }
    }

    @Test
    public void testSuccessResults() {
        fixture.addResult(success("OkTest", TestResultsTest.class.getName()));
        fixture.addResult(success("OkTest2", TestResultsTest.class.getName()));

        assertEquals(fixture.getSuccess(), 2);
        assertEquals(fixture.getSuccessPercentageFormatted(), "100.0");
        assertEquals(fixture.getFailed(), 0);
        assertEquals(fixture.getFailedPercentageFormatted(), "0.0");
        assertEquals(fixture.getSkipped(), 0);
        assertEquals(fixture.getSkippedPercentageFormatted(), "0.0");
    }

    @Test
    public void testFailedResults() {
        fixture.addResult(success("OkTest", TestResultsTest.class.getName()));
        fixture.addResult(failed("FailedTest", TestResultsTest.class.getName(), new CitrusRuntimeException("This went wrong")));
        fixture.addResult(success("OkTest2", TestResultsTest.class.getName()));

        assertEquals(fixture.getSuccess(), 2);
        assertEquals(fixture.getSuccessPercentageFormatted(), "66.7");
        assertEquals(fixture.getFailed(), 1);
        assertEquals(fixture.getFailedPercentageFormatted(), "33.3");
        assertEquals(fixture.getSkipped(), 0);
        assertEquals(fixture.getSkippedPercentageFormatted(), "0.0");
    }

    @Test
    public void testSkippedResults() {
        fixture.addResult(success("OkTest", TestResultsTest.class.getName()));
        fixture.addResult(failed("FailedTest", TestResultsTest.class.getName(), new CitrusRuntimeException("This went wrong")));
        fixture.addResult(skipped("SkippedTest", TestResultsTest.class.getName()));

        assertEquals(fixture.getSuccess(), 1);
        assertEquals(fixture.getSuccessPercentageFormatted(), "50.0");
        assertEquals(fixture.getFailed(), 1);
        assertEquals(fixture.getFailedPercentageFormatted(), "50.0");
        assertEquals(fixture.getSkipped(), 1);
        assertEquals(fixture.getSkippedPercentageFormatted(), "33.3");
    }

    @Test
    void getTotalDurationCalculatesTotal() {
        fixture.addResult(success("OkTest", TestResultsTest.class.getName()).withDuration(Duration.ofMillis(150)));
        fixture.addResult(success("OkTest2", TestResultsTest.class.getName()).withDuration(Duration.ofMillis(150)));
        fixture.addResult(failed("FailedTest", TestResultsTest.class.getName(), new CitrusRuntimeException("This went wrong")).withDuration(Duration.ofMillis(300)));
        fixture.addResult(skipped("SkippedTest", TestResultsTest.class.getName()));

        assertEquals(fixture.getTotalDuration(), Duration.ofMillis(600));
    }

    @Test
    void getTotalDurationReturnsZeroByDefault() {
        assertEquals(fixture.getTotalDuration(), Duration.ZERO);
    }
}
