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

package org.citrusframework.base;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import org.citrusframework.TestAction;
import org.citrusframework.TestCase;
import org.citrusframework.actions.EchoAction;
import org.citrusframework.context.TestContext;
import org.citrusframework.report.AbstractTestListener;
import org.citrusframework.report.TestActionListener;
import org.citrusframework.spi.BindToRegistry;
import org.citrusframework.variable.GlobalVariables;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies thread safety of {@link DefaultCitrusContext} configuration parsing, listeners, and global variables
 * under concurrent method execution.
 */
class DefaultCitrusContextConcurrencyTest {

    private static final int THREADS = 12;

    @Test
    void shouldParseConfigurationOnceAndWaitForCompletionAcrossConcurrentThreads() throws InterruptedException {
        DefaultCitrusContext citrusContext = new DefaultCitrusContext();
        SlowBindConfiguration.INVOCATIONS.set(0);

        List<Throwable> errors = runConcurrently(THREADS, () -> {
            SlowBindConfiguration target = new SlowBindConfiguration();
            citrusContext.parseConfiguration(target);
            // Every thread returning from parseConfiguration MUST immediately observe the bound bean
            String resolved = citrusContext.getReferenceResolver().resolve("slowBoundBean", String.class);
            assertThat(resolved).isEqualTo("ready-value");
        });

        assertThat(errors).isEmpty();
        assertThat(SlowBindConfiguration.INVOCATIONS.get()).isEqualTo(1);
    }

    @Test
    void shouldSafelyRegisterListenersAndGlobalVariablesWhileTestsExecuteConcurrently() throws InterruptedException {
        DefaultCitrusContext citrusContext = new DefaultCitrusContext();
        EchoAction echo = new EchoAction.Builder().message("hello").build();

        List<Throwable> errors = runConcurrently(THREADS, index -> {
            for (int i = 0; i < 50; i++) {
                if (index % 3 == 0) {
                    citrusContext.addTestListener(new AbstractTestListener() {
                        @Override
                        public void onTestStart(TestCase test) {
                        }
                    });
                    citrusContext.addTestActionListener(new TestActionListener() {
                        @Override
                        public void onTestActionStart(TestCase testCase, TestAction action) {
                        }

                        @Override
                        public void onTestActionFinish(TestCase testCase, TestAction action) {
                        }

                        @Override
                        public void onTestActionSkipped(TestCase testCase, TestAction action) {
                        }

                        @Override
                        public void onTestActionFailed(TestCase testCase, TestAction action, Throwable throwable) {
                        }
                    });
                    GlobalVariables vars = new GlobalVariables.Builder()
                            .variable("global_" + index + "_" + i, "val_" + i)
                            .build();
                    citrusContext.addComponent("vars_" + index + "_" + i, vars);
                } else {
                    TestContext testContext = citrusContext.createTestContext();
                    DefaultTestCase testCase = new DefaultTestCase();
                    testCase.setName("concurrent_" + index + "_" + i);
                    testCase.start(testContext);
                    testCase.executeAction(echo, testContext);
                    testCase.finish(testContext);
                }
            }
        });

        assertThat(errors).isEmpty();
    }

    public static class SlowBindConfiguration {
        static final AtomicInteger INVOCATIONS = new AtomicInteger();

        @BindToRegistry(name = "slowBoundBean")
        public String slowBoundBean() throws InterruptedException {
            INVOCATIONS.incrementAndGet();
            Thread.sleep(60L);
            return "ready-value";
        }
    }

    private interface IndexedTask {
        void run(int index) throws Exception;
    }

    private static List<Throwable> runConcurrently(int threadCount, Runnable task) throws InterruptedException {
        return runConcurrently(threadCount, idx -> task.run());
    }

    private static List<Throwable> runConcurrently(int threadCount, IndexedTask task) throws InterruptedException {
        List<Throwable> errors = Collections.synchronizedList(new ArrayList<>());
        CountDownLatch start = new CountDownLatch(1);
        List<Thread> threads = new ArrayList<>();

        for (int t = 0; t < threadCount; t++) {
            final int index = t;
            Thread thread = new Thread(() -> {
                try {
                    start.await();
                    task.run(index);
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
