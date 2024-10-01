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

package org.citrusframework.actions;

import org.citrusframework.Completable;
import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

import static java.util.concurrent.Executors.newSingleThreadExecutor;

/**
 * Test action that performs in a separate thread. Action execution is not blocking the test execution chain. After
 * action has performed optional validation step is called.
 *
 * @since 2.7.4
 */
public abstract class AbstractAsyncTestAction extends AbstractTestAction implements Completable {

    /** Logger */
    private static final Logger logger = LoggerFactory.getLogger(AbstractAsyncTestAction.class);

    /** Future finished indicator */
    private Future<?> finished;

    @Override
    public final void doExecute(TestContext context) {
        CompletableFuture<TestContext> result = new CompletableFuture<>();

        result.whenComplete((ctx, throwable) -> {
            if (throwable != null) {
                onError(ctx, throwable);
            } else if (ctx.hasExceptions()) {
                onError(ctx, ctx.getExceptions().get(0));
            } else {
                onSuccess(ctx);
            }
        });

        var executor = newSingleThreadExecutor();
        finished = executor.submit(() -> {
            try {
                doExecuteAsync(context);
            } catch (Exception | Error e) {
                logger.warn("Async test action execution raised error", e);

                if (e instanceof CitrusRuntimeException citrusEx) {
                    context.addException(citrusEx);
                } else {
                    context.addException(new CitrusRuntimeException(e));
                }
            } finally {
                executor.shutdownNow();
                result.complete(context);
            }
        });
    }

    @Override
    public boolean isDone(TestContext context) {
        return Optional.ofNullable(finished)
                .map(future -> future.isDone() || isDisabled(context))
                .orElseGet(() -> isDisabled(context));
    }

    public abstract void doExecuteAsync(TestContext context);

    /**
     * Optional validation step after async test action performed with success.
     * @param context
     */
    public void onSuccess(TestContext context) {
    }

    /**
     * Optional validation step after async test action performed with success.
     * @param context
     */
    public void onError(TestContext context, Throwable error) {
    }
}
