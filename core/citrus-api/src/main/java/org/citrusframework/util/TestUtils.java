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

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;

import org.citrusframework.CitrusSettings;
import org.citrusframework.Completable;
import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for test cases providing several utility
 * methods regarding Citrus test cases.
 *
 */
public abstract class TestUtils {

    public static final String HTTPS_CITRUSFRAMEWORK_ORG = "https://citrusframework.org";

    /** Used to identify waiting task threads pool */
    public static final String WAIT_THREAD_PREFIX = "citrus-waiting-";

    /** Logger */
    private static final Logger logger = LoggerFactory.getLogger(TestUtils.class);

    /**
     * Prevent instantiation.
     */
    private TestUtils() {
        super();
    }

    /**
     * Wait for container completion with default timeout.
     */
    public static void waitForCompletion(final Completable container,
                                         final TestContext context) {
        waitForCompletion(container, context, 10000L);
    }

    /**
     * Wait for container completion using default thread executor.
     */
    public static void waitForCompletion(final Completable container,
                                         final TestContext context, long timeout) {
        if (container.isDone(context)) {
            return;
        }

        final AtomicReference<Thread> waitThread = new AtomicReference<>();
        final ScheduledExecutorService scheduledExecutor = Executors.newSingleThreadScheduledExecutor(runnable -> {
            Thread thread = TestUtils.createWaitingThread(runnable, context);
            waitThread.set(thread);
            return thread;
        });
        try {
            waitForCompletion(scheduledExecutor, container, context, timeout);
        } finally {
            Thread thread = waitThread.get();
            if (thread != null && thread != Thread.currentThread()) {
                boolean wasInterrupted = Thread.interrupted();
                try {
                    thread.join(Math.max(100L, (timeout / 10) / 2));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    logger.warn("Failed to join waiting thread '{}', caused by {}", thread.getName(), e.getMessage());
                } finally {
                    if (wasInterrupted) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }
    }

    /**
     * Uses given scheduler to wait for container to finish properly. Method polls for done state on container for given
     * amount of time.
     */
    public static void waitForCompletion(final ScheduledExecutorService scheduledExecutor,
                                         final Completable container,
                                         final TestContext context, long timeout) {
        if (container.isDone(context)) {
            return;
        }

        ScheduledFuture<?> scheduler = null;
        try {
            final CompletableFuture<Boolean> finished = new CompletableFuture<>();
            scheduler = scheduledExecutor.scheduleAtFixedRate(() -> {
                try {
                    if (container.isDone(context)) {
                        finished.complete(true);
                    } else if (logger.isDebugEnabled()) {
                        logger.debug("Wait for test container in test '%s' to finish properly ...".formatted(getTestName(context)));
                    }
                } catch (Exception e) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Failed to wait for completion of nested test actions", e);
                    } else {
                        logger.warn("Failed to wait for completion of nested test actions because of {}", e.getMessage());
                    }
                }
            }, 100L, Math.max(1L, timeout / 10), TimeUnit.MILLISECONDS);

            finished.get(timeout, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new CitrusRuntimeException("Failed to wait for test container in test '%s' to finish properly".formatted(getTestName(context)), e);
        } catch (ExecutionException | TimeoutException e) {
            throw new CitrusRuntimeException("Failed to wait for test container in test '%s' to finish properly".formatted(getTestName(context)), e);
        } finally {
            if (scheduler != null) {
                scheduler.cancel(true);
            }

            boolean wasInterrupted = Thread.interrupted();
            long terminationTimeout = Math.max(100L, (timeout / 10) / 2);
            try {
                scheduledExecutor.shutdown();
                if (!scheduledExecutor.awaitTermination(terminationTimeout, TimeUnit.MILLISECONDS)) {
                    scheduledExecutor.shutdownNow();
                    scheduledExecutor.awaitTermination(terminationTimeout, TimeUnit.MILLISECONDS);
                }
            } catch (InterruptedException e) {
                scheduledExecutor.shutdownNow();
                Thread.currentThread().interrupt();
                logger.warn("Failed to await orderly termination of waiting tasks to complete, caused by {}", e.getMessage());
            } finally {
                if (wasInterrupted) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    public static String getTestName(TestContext context) {
        if (context.getVariables().containsKey(CitrusSettings.getTestNameVariable())) {
            return context.getVariable(CitrusSettings.getTestNameVariable());
        } else {
            return "";
        }
    }

    /**
     * Normalize the text by trimming whitespace and replacing line endings by a linux representation.
     */
    public static String normalizeLineEndings(String text) {
        return text != null ? text.replace("\r\n", "\n").replace("&#13;", ""): null;
    }

    private static Thread createWaitingThread(final Runnable runnable, TestContext context) {
        final Thread waitThread = Executors.defaultThreadFactory().newThread(runnable);

        if (context.getVariables().containsKey(CitrusSettings.getTestNameVariable())) {
            waitThread.setName(WAIT_THREAD_PREFIX.concat(context.getVariable(CitrusSettings.getTestNameVariable()))
                    .concat("-").concat(waitThread.getName()));
        } else {
            waitThread.setName(WAIT_THREAD_PREFIX.concat(waitThread.getName()));
        }
        return waitThread;
    }

    public static boolean isNetworkReachable() {
        try {
            URL url = URI.create(HTTPS_CITRUSFRAMEWORK_ORG).toURL();
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            int responseCode = connection.getResponseCode();
            return responseCode == HttpURLConnection.HTTP_OK;
        } catch (IOException | IllegalArgumentException e) {
            return false;
        }
    }
}
