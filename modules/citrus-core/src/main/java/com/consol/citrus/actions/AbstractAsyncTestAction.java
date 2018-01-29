/*
 * Copyright 2006-2018 the original author or authors.
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

package com.consol.citrus.actions;

import com.consol.citrus.context.TestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;

/**
 * Test action that performs in a separate thread. Action execution is not blocking the test execution chain. After
 * action has performed optional validation step is called.
 *
 * @author Christoph Deppisch
 * @since 2.7.4
 */
public abstract class AbstractAsyncTestAction extends AbstractTestAction {

    /** Logger */
    private static Logger log = LoggerFactory.getLogger(AbstractAsyncTestAction.class);

    private Executor executor = Executors.newSingleThreadExecutor();

    @Override
    public final void doExecute(TestContext context) {
        CompletableFuture<Void> result = new CompletableFuture<>();

        executor.execute(() -> {
            try {
                doExecuteAsync(context);
                result.complete(null);
            } catch (Exception e) {
                log.warn("Async test action execution raised error", e);
                result.completeExceptionally(e);
            }
        });

        result.whenComplete((nothing, throwable) -> {
            if (throwable != null) {
                onError(context, throwable);
            } else {
                onSuccess(context);
            }
        });
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

    /**
     * Sets the executor.
     *
     * @param executor
     */
    public void setExecutor(Executor executor) {
        this.executor = executor;
    }
}
