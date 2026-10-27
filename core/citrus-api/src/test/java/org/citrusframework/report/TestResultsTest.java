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
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;

import org.citrusframework.TestResult;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static java.lang.Thread.currentThread;
import static java.util.concurrent.Executors.newFixedThreadPool;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.citrusframework.TestResult.failed;
import static org.citrusframework.TestResult.skipped;
import static org.citrusframework.TestResult.success;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

public class TestResultsTest {

    private TestResults fixture;

    @BeforeEach
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
        assertEquals(fixture.getTotalDuration(), expectedDuration, "Total duration is incorrect");
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

        assertEquals(2, fixture.getSuccess());
        assertEquals("100.0", fixture.getSuccessPercentageFormatted());
        assertEquals(0, fixture.getFailed());
        assertEquals("0.0", fixture.getFailedPercentageFormatted());
        assertEquals(0, fixture.getSkipped());
        assertEquals("0.0", fixture.getSkippedPercentageFormatted());
    }

    @Test
    public void testFailedResults() {
        fixture.addResult(success("OkTest", TestResultsTest.class.getName()));
        fixture.addResult(failed("FailedTest", TestResultsTest.class.getName(), new CitrusRuntimeException("This went wrong")));
        fixture.addResult(success("OkTest2", TestResultsTest.class.getName()));

        assertEquals(2, fixture.getSuccess());
        assertEquals("66.7", fixture.getSuccessPercentageFormatted());
        assertEquals(1, fixture.getFailed());
        assertEquals("33.3", fixture.getFailedPercentageFormatted());
        assertEquals(0, fixture.getSkipped());
        assertEquals("0.0", fixture.getSkippedPercentageFormatted());
    }

    @Test
    public void testAllResults() {
        fixture.addResult(success("OkTest1", TestResultsTest.class.getName()));
        fixture.addResult(success("OkTest2", TestResultsTest.class.getName()));
        fixture.addResult(failed("FailedTest1", TestResultsTest.class.getName(), new CitrusRuntimeException("This went wrong")));
        fixture.addResult(failed("FailedTest2", TestResultsTest.class.getName(), new CitrusRuntimeException("This went wrong")));
        fixture.addResult(skipped("SkippedTest1", TestResultsTest.class.getName()));

        assertEquals(2, fixture.getSuccess());
        assertEquals("40.0", fixture.getSuccessPercentageFormatted());
        assertEquals(2, fixture.getFailed());
        assertEquals("40.0", fixture.getFailedPercentageFormatted());
        assertEquals(1, fixture.getSkipped());
        assertEquals("20.0", fixture.getSkippedPercentageFormatted());
    }

    @Test
    public void testResultsForNoResult() {
        assertEquals(0, fixture.getSuccess());
        assertEquals("0.0", fixture.getSuccessPercentageFormatted());
        assertEquals(0, fixture.getFailed());
        assertEquals("0.0", fixture.getFailedPercentageFormatted());
        assertEquals(0, fixture.getSkipped());
        assertEquals("0.0", fixture.getSkippedPercentageFormatted());
    }

    @Test
    void getTotalDurationCalculatesTotal() {
        fixture.addResult(success("OkTest", TestResultsTest.class.getName()).withDuration(Duration.ofMillis(150)));
        fixture.addResult(success("OkTest2", TestResultsTest.class.getName()).withDuration(Duration.ofMillis(150)));
        fixture.addResult(failed("FailedTest", TestResultsTest.class.getName(), new CitrusRuntimeException("This went wrong")).withDuration(Duration.ofMillis(300)));
        fixture.addResult(skipped("SkippedTest", TestResultsTest.class.getName()));

        assertEquals(Duration.ofMillis(600), fixture.getTotalDuration());
    }

    @Test
    void getTotalDurationReturnsZeroByDefault() {
        assertEquals(Duration.ZERO, fixture.getTotalDuration());
    }
}
