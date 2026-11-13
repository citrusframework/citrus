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

package org.citrusframework.http.vertx;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.expectThrows;

public class VertxExchangeExecutorTest {

    @Test
    public void tasksRunOnPoolThreads() throws Exception {
        VertxExchangeExecutor executor = new VertxExchangeExecutor("vertx-proto-test", 2, 4);
        try {
            AtomicReference<String> threadName = new AtomicReference<>();
            CountDownLatch done = new CountDownLatch(1);
            assertTrue(executor.tryAcquire());
            executor.execute(() -> {
                threadName.set(Thread.currentThread().getName());
                done.countDown();
            });
            assertTrue(done.await(5, TimeUnit.SECONDS));
            assertTrue(threadName.get().startsWith("vertx-proto-test-"),
                    "Unexpected pool thread: " + threadName.get());
            assertFalse(threadName.get().startsWith("vert.x-eventloop"),
                    "Must never run on a Vert.x event loop thread");
        } finally {
            executor.release();
            assertTrue(executor.shutdown(5_000L));
        }
    }

    @Test
    public void admissionControlRejectsBeyondLimit() throws Exception {
        VertxExchangeExecutor executor = new VertxExchangeExecutor("vertx-proto-admit", 2, 2);
        try {
            assertTrue(executor.tryAcquire());
            assertTrue(executor.tryAcquire());
            assertFalse(executor.tryAcquire());
            assertEquals(executor.availablePermits(), 0);

            executor.release();
            assertEquals(executor.availablePermits(), 1);
            assertTrue(executor.tryAcquire());
            executor.release();
            executor.release();
        } finally {
            assertTrue(executor.shutdown(5_000L));
        }
    }

    @Test
    public void shutdownIsBoundedAndRejectsNewTasks() throws Exception {
        VertxExchangeExecutor executor = new VertxExchangeExecutor("vertx-proto-stop", 1, 1);
        assertTrue(executor.tryAcquire());
        executor.execute(() -> {
        });
        executor.release();
        assertTrue(executor.shutdown(5_000L));
        assertTrue(executor.isShutdown());
        expectThrows(RejectedExecutionException.class, () -> executor.execute(() -> {
        }));
    }

    @Test
    public void shutdownDrainsPendingTasksWithinTimeout() throws Exception {
        VertxExchangeExecutor executor = new VertxExchangeExecutor("vertx-proto-drain", 1, 4);
        try {
            CountDownLatch entered = new CountDownLatch(1);
            CountDownLatch release = new CountDownLatch(1);
            CountDownLatch done = new CountDownLatch(1);
            assertTrue(executor.tryAcquire());
            executor.execute(() -> {
                entered.countDown();
                try {
                    assertTrue(release.await(5, TimeUnit.SECONDS));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    done.countDown();
                }
            });
            assertTrue(entered.await(5, TimeUnit.SECONDS));
            release.countDown();
            assertTrue(done.await(5, TimeUnit.SECONDS));
        } finally {
            executor.release();
            assertTrue(executor.shutdown(5_000L));
        }
    }
}
