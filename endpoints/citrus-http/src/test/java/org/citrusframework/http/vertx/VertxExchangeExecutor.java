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

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Endpoint-owned worker pool for Citrus adapter handling. The Vert.x event loop never runs
 * adapter code: every exchange first acquires an admission permit (bounded by
 * maxConcurrentExchanges, saturated callers get 503) and then runs on a pool thread.
 */
public final class VertxExchangeExecutor {

    private final ThreadPoolExecutor executor;
    private final Semaphore admission;

    public VertxExchangeExecutor(String threadNamePrefix, int threads, int maxConcurrentExchanges) {
        if (threads < 1) {
            throw new IllegalArgumentException("threads must be >= 1");
        }
        if (maxConcurrentExchanges < 1) {
            throw new IllegalArgumentException("maxConcurrentExchanges must be >= 1");
        }
        this.admission = new Semaphore(maxConcurrentExchanges);
        ThreadFactory factory = new PoolThreadFactory(threadNamePrefix);
        this.executor = new ThreadPoolExecutor(threads, threads, 0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(maxConcurrentExchanges), factory);
    }

    /**
     * Try to admit a new exchange without blocking.
     *
     * @return true when the exchange may proceed, false when saturated (caller must answer 503)
     */
    public boolean tryAcquire() {
        return !executor.isShutdown() && admission.tryAcquire();
    }

    /**
     * Release a previously acquired admission permit.
     */
    public void release() {
        admission.release();
    }

    /**
     * Run adapter handling on a pool thread. Never call from a Vert.x event loop thread.
     */
    public void execute(Runnable task) {
        if (executor.isShutdown()) {
            throw new RejectedExecutionException("Exchange executor is shut down");
        }
        executor.execute(task);
    }

    public int availablePermits() {
        return admission.availablePermits();
    }

    public boolean isShutdown() {
        return executor.isShutdown();
    }

    /**
     * Bounded cancellation: discard queued tasks, interrupt workers, and await one timeout budget.
     *
     * @return true when terminated within the timeout
     */
    public boolean shutdown(long timeoutMs) throws InterruptedException {
        executor.shutdownNow();
        return executor.awaitTermination(timeoutMs, TimeUnit.MILLISECONDS);
    }

    private static final class PoolThreadFactory implements ThreadFactory {
        private final String prefix;
        private final AtomicInteger sequence = new AtomicInteger();

        private PoolThreadFactory(String prefix) {
            this.prefix = prefix;
        }

        @Override
        public Thread newThread(Runnable task) {
            Thread thread = new Thread(task, prefix + "-" + sequence.incrementAndGet());
            thread.setDaemon(false);
            return thread;
        }
    }
}
