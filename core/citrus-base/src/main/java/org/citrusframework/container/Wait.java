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

package org.citrusframework.container;

import java.time.Duration;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.citrusframework.AbstractTestActionBuilder;
import org.citrusframework.TestActionBuilder;
import org.citrusframework.actions.AbstractTestAction;
import org.citrusframework.condition.ActionCondition;
import org.citrusframework.condition.Condition;
import org.citrusframework.condition.FileCondition;
import org.citrusframework.condition.HttpCondition;
import org.citrusframework.condition.MessageCondition;
import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Pause the test execution until the condition is met or the wait time has been exceeded.
 *
 * @author Martin Maher
 * @since 2.4
 */
public class Wait extends AbstractTestAction {

    /** Logger */
    private static final Logger logger = LoggerFactory.getLogger(Wait.class);

    /** Condition to be met */
    private final Condition condition;

    /** The total time to wait in milliseconds, for the condition to be met before failing */
    private final String time;

    /** The time interval in milliseconds between each test of the condition */
    private final String interval;

    /**
     * Default constructor.
     */
    public Wait(Builder builder) {
        super("wait", builder);

        this.condition = builder.condition;
        this.time = builder.time;
        this.interval = builder.interval;
    }

    @Override
    public void doExecute(final TestContext context) {
        Boolean conditionSatisfied = null;
        long timeLeft = getWaitTimeMs(context);
        long intervalMs = getIntervalMs(context);

        if (intervalMs > timeLeft) {
            intervalMs = timeLeft;
        }

        Callable<Boolean> callable = () -> condition.isSatisfied(context);

        while (timeLeft > 0) {
            timeLeft -= intervalMs;

            if (logger.isDebugEnabled()) {
                logger.debug(String.format("Waiting for condition %s", condition.getName()));
            }

            ExecutorService executor = Executors.newSingleThreadExecutor();
            Future<Boolean> future = executor.submit(callable);
            long checkStartTime = System.currentTimeMillis();
            try {
                conditionSatisfied = future.get(intervalMs, TimeUnit.MILLISECONDS);
            } catch (InterruptedException | TimeoutException | ExecutionException e) {
                logger.warn(String.format("Condition check interrupted with '%s'", e.getClass().getSimpleName()));
            }
            executor.shutdown();

            if (Boolean.TRUE.equals(conditionSatisfied)) {
                logger.info(condition.getSuccessMessage(context));
                return;
            }

            long sleepTime = intervalMs - (System.currentTimeMillis() - checkStartTime);
            if (sleepTime > 0) {
                try {
                    Thread.sleep(sleepTime);
                } catch (InterruptedException e) {
                    logger.warn("Interrupted during wait!", e);
                }
            }
        }

        throw new CitrusRuntimeException(condition.getErrorMessage(context));
    }

    /**
     * Gets total wait time in milliseconds. Either uses second time value or default milliseconds.
     * @param context
     * @return
     */
    private long getWaitTimeMs(TestContext context) {
        return Long.parseLong(context.replaceDynamicContentInString(time));
    }

    /**
     * Gets the time interval for the condition check in milliseconds.
     * @param context
     * @return
     */
    private long getIntervalMs(TestContext context) {
        return Long.parseLong(context.replaceDynamicContentInString(interval));
    }

    public String getTime() {
        return time;
    }

    public Condition getCondition() {
        return condition;
    }

    public String getInterval() {
        return interval;
    }

    /**
     * Action builder.
     */
    public static class Builder<C extends Condition> extends AbstractTestActionBuilder<Wait, Builder<C>> implements TestActionBuilder.DelegatingTestActionBuilder<Wait> {

        protected C condition;
        protected String time = "5000";
        protected String interval = "1000";

        protected TestActionBuilder<?> delegate;

        /**
         * Fluent API action building entry method used in Java DSL.
         * @return
         */
        public static Builder<Condition> waitFor() {
            return new Builder<>();
        }

        /**
         * Condition to wait for during execution.
         * @param condition The condition to add to the wait action
         * @return The wait action
         */
        public Builder<C> condition(C condition) {
            this.condition = condition;
            this.delegate = this;
            return this;
        }

        /**
         * Sets custom condition builder.
         * @param conditionBuilder
         * @param <T>
         * @return
         */
        public <T extends WaitConditionBuilder<C, T>> T condition(T conditionBuilder) {
            this.condition = conditionBuilder.getCondition();
            this.delegate = conditionBuilder;
            return conditionBuilder;
        }

        /**
         * The message condition to wait for during execution.
         * @return A WaitMessageConditionBuilder for further configuration
         */
        public WaitMessageConditionBuilder message() {
            this.condition = (C) new MessageCondition();
            WaitMessageConditionBuilder builder = new WaitMessageConditionBuilder((Builder<MessageCondition>) this);
            this.delegate = builder;
            return builder;
        }

        /**
         * The test action condition to wait for during execution.
         * @return A WaitActionConditionBuilder for further configuration
         */
        public WaitActionConditionBuilder execution() {
            this.condition = (C) new ActionCondition();
            WaitActionConditionBuilder builder = new WaitActionConditionBuilder((Builder<ActionCondition>) this);
            this.delegate = builder;
            return builder;
        }

        /**
         * The HTTP condition to wait for during execution.
         * @return A WaitHttpConditionBuilder for further configuration
         */
        public WaitHttpConditionBuilder http() {
            this.condition = (C) new HttpCondition();
            WaitHttpConditionBuilder builder = new WaitHttpConditionBuilder((Builder<HttpCondition>) this);
            this.delegate = builder;
            return builder;
        }

        /**
         * The file condition to wait for during execution.
         * @return A WaitFileConditionBuilder for further configuration
         */
        public WaitFileConditionBuilder file() {
            this.condition = (C) new FileCondition();
            WaitFileConditionBuilder builder = new WaitFileConditionBuilder((Builder<FileCondition>) this);
            this.delegate = builder;
            return builder;
        }

        /**
         * The interval in milliseconds to use between each test of the condition
         * @param interval The interval to use
         * @return The altered WaitBuilder
         */
        public Builder<C> interval(Long interval) {
            return interval(String.valueOf(interval));
        }

        /**
         * The interval in milliseconds to use between each test of the condition
         * @param interval The interval to use
         * @return The altered WaitBuilder
         */
        public Builder<C> interval(String interval) {
            this.interval = interval;
            return this;
        }

        public Builder<C> milliseconds(long milliseconds) {
            return milliseconds(String.valueOf(milliseconds));
        }

        public Builder<C> milliseconds(String milliseconds) {
            this.time = milliseconds;
            return this;
        }

        public Builder<C> seconds(double seconds) {
            milliseconds(Math.round(seconds * 1000));
            return this;
        }

        public Builder<C> time(Duration duration) {
            milliseconds(duration.toMillis());
            return this;
        }

        @Override
        public Wait build() {
            return new Wait(this);
        }

        @Override
        public TestActionBuilder<?> getDelegate() {
            return delegate;
        }

        public C getCondition() {
            return (C) condition;
        }
    }
}
