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

package org.citrusframework.util;

import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.citrusframework.CitrusSettings;
import org.citrusframework.Completable;
import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TestUtilsTest {

    @Test
    void waitForCompletionDoesNotSpawnWaitingThreadWhenAlreadyDone() {
        TestContext context = new TestContext();
        String testName = "WaitAlreadyDoneTest";
        context.setVariable(CitrusSettings.getTestNameVariable(), testName);

        AtomicBoolean ranOnWaitingThread = new AtomicBoolean(false);
        Completable completed = ctx -> {
            if (Thread.currentThread().getName().startsWith(TestUtils.WAIT_THREAD_PREFIX)) {
                ranOnWaitingThread.set(true);
            }
            return true;
        };

        TestUtils.waitForCompletion(completed, context, 500L);

        assertThat(ranOnWaitingThread).isFalse();
        assertNoAliveWaitingThread(testName);
    }

    @Test
    void waitForCompletionTerminatesWaitingThreadBeforeReturningOnAsyncCompletion() {
        for (int round = 0; round < 5; round++) {
            TestContext context = new TestContext();
            String testName = "WaitAsyncCompleteTest-" + round;
            context.setVariable(CitrusSettings.getTestNameVariable(), testName);

            AtomicInteger checks = new AtomicInteger();
            Completable asyncCompletable = ctx -> checks.getAndIncrement() >= 1;

            TestUtils.waitForCompletion(asyncCompletable, context, 300L);

            assertNoAliveWaitingThread(testName);
        }
    }

    @Test
    void waitForCompletionTerminatesWaitingThreadBeforeReturningOnTimeout() {
        TestContext context = new TestContext();
        String testName = "WaitTimeoutTerminationTest";
        context.setVariable(CitrusSettings.getTestNameVariable(), testName);

        AtomicInteger checks = new AtomicInteger();
        Completable slowCompletable = ctx -> {
            if (checks.getAndIncrement() == 0) {
                return false;
            }
            try {
                Thread.sleep(5_000L);
            } catch (InterruptedException e) {
                // Simulate brief post-interrupt cleanup inside isDone before worker exits
                long deadline = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(25L);
                while (System.nanoTime() < deadline) {
                    Thread.onSpinWait();
                }
                Thread.currentThread().interrupt();
            }
            return false;
        };

        assertThrows(CitrusRuntimeException.class,
                () -> TestUtils.waitForCompletion(slowCompletable, context, 150L));

        assertNoAliveWaitingThread(testName);
    }

    @Test
    void waitForCompletionWithCustomSchedulerAwaitsTerminationInMilliseconds() {
        TestContext context = new TestContext();
        String testName = "WaitCustomSchedulerTest";
        context.setVariable(CitrusSettings.getTestNameVariable(), testName);

        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        AtomicInteger checks = new AtomicInteger();
        Completable slowCompletable = ctx -> {
            if (checks.getAndIncrement() == 0) {
                return false;
            }
            try {
                Thread.sleep(5_000L);
            } catch (InterruptedException e) {
                long deadline = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(25L);
                while (System.nanoTime() < deadline) {
                    Thread.onSpinWait();
                }
                Thread.currentThread().interrupt();
            }
            return false;
        };

        assertThrows(CitrusRuntimeException.class,
                () -> TestUtils.waitForCompletion(scheduler, slowCompletable, context, 150L));

        assertThat(scheduler.isTerminated()).isTrue();
    }

    @Test
    void waitForCompletionRestoresInterruptFlagWhenCallerInterrupted() throws Exception {
        TestContext context = new TestContext();
        String testName = "WaitCallerInterruptTest";
        context.setVariable(CitrusSettings.getTestNameVariable(), testName);

        CountDownLatch pollingStarted = new CountDownLatch(1);
        AtomicInteger checks = new AtomicInteger();
        Completable neverDone = ctx -> {
            if (checks.getAndIncrement() >= 1) {
                pollingStarted.countDown();
            }
            return false;
        };

        AtomicBoolean interruptRestored = new AtomicBoolean(false);
        AtomicBoolean threwCitrusException = new AtomicBoolean(false);
        Thread caller = new Thread(() -> {
            try {
                TestUtils.waitForCompletion(neverDone, context, 5_000L);
            } catch (CitrusRuntimeException e) {
                threwCitrusException.set(true);
                interruptRestored.set(Thread.currentThread().isInterrupted());
            } finally {
                Thread.interrupted();
            }
        }, "wait-caller-test-thread");

        caller.start();
        assertThat(pollingStarted.await(2, TimeUnit.SECONDS)).isTrue();
        caller.interrupt();
        caller.join(2_000L);

        assertThat(caller.isAlive()).isFalse();
        assertThat(threwCitrusException).isTrue();
        assertThat(interruptRestored).isTrue();
        assertNoAliveWaitingThread(testName);
    }

    private static void assertNoAliveWaitingThread(String testName) {
        String expectedPrefix = TestUtils.WAIT_THREAD_PREFIX.concat(testName);
        Optional<Thread> aliveWaitingThread = Thread.getAllStackTraces().keySet().stream()
                .filter(thread -> thread.getName().startsWith(expectedPrefix))
                .filter(Thread::isAlive)
                .findAny();

        assertThat(aliveWaitingThread)
                .withFailMessage(() -> "Waiting thread still alive: " + aliveWaitingThread.orElse(null))
                .isEmpty();
    }
}
