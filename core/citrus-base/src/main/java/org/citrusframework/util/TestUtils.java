/*
 * Copyright 2006-2010 the original author or authors.
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

import org.citrusframework.CitrusSettings;
import org.citrusframework.Completable;
import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Utility class for test cases providing several utility
 * methods regarding Citrus test cases.
 *
 * @author Christoph Deppisch
 */
public abstract class TestUtils {

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
        waitForCompletion(Executors.newSingleThreadScheduledExecutor(runnable -> TestUtils.createWaitingThread(runnable, context)),
                container, context, timeout);
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
                    } else {
                        logger.debug("Wait for test container to finish properly ...");
                    }
                } catch (Exception e) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Failed to wait for completion of nested test actions", e);
                    } else {
                        logger.warn(String.format("Failed to wait for completion of nested test actions because of %s",  e.getMessage()));
                    }
                }
            }, 100L, timeout / 10, TimeUnit.MILLISECONDS);

            finished.get(timeout, TimeUnit.MILLISECONDS);
        } catch (ExecutionException | TimeoutException | InterruptedException e) {
            throw new CitrusRuntimeException("Failed to wait for test container to finish properly", e);
        } finally {
            if (scheduler != null) {
                scheduler.cancel(true);
            }

            try {
                scheduledExecutor.shutdown();
                scheduledExecutor.awaitTermination((timeout / 10) / 2, TimeUnit.MICROSECONDS);
            } catch (InterruptedException e) {
                logger.warn(String.format("Failed to await orderly termination of waiting tasks to complete, caused by %s", e.getMessage()));
            }

            if (!scheduledExecutor.isTerminated()) {
                scheduledExecutor.shutdownNow();
            }
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

        if (context.getVariables().containsKey(CitrusSettings.TEST_NAME_VARIABLE)) {
            waitThread.setName(WAIT_THREAD_PREFIX.concat(context.getVariable(CitrusSettings.TEST_NAME_VARIABLE))
                    .concat("-").concat(waitThread.getName()));
        } else {
            waitThread.setName(WAIT_THREAD_PREFIX.concat(waitThread.getName()));
        }
        return waitThread;
    }
}
