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

package org.citrusframework.container;

import jakarta.annotation.Nullable;
import org.apache.commons.lang3.time.StopWatch;
import org.citrusframework.AbstractIteratingContainerBuilder;
import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.ActionTimeoutException;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

import static java.util.Objects.nonNull;

/**
 * Looping test container iterating the nested test actions in case an error occurred in one
 * of them. Iteration continues until a aborting condition evaluates to true.
 * <p>
 * Number of iterations is kept in a index variable. The nested test actions can access this variable
 * as normal test variable.
 * <p>
 * Between the iterations container can sleep automatically a given amount of time.
 */
public class RepeatOnErrorUntilTrue extends AbstractIteratingActionContainer {

    private static final Logger logger = LoggerFactory.getLogger(RepeatOnErrorUntilTrue.class);

    private final Duration autoSleep;

    /**
     * Default constructor.
     */
    public RepeatOnErrorUntilTrue(Builder builder) {
        super("repeat-on-error", builder);

        this.autoSleep = builder.autoSleep;
    }

    @Override
    public void executeIteration(TestContext context) {
        CitrusRuntimeException exception = null;

        while (!checkCondition(context)) {
            try {
                exception = null;
                executeActions(context);
                break;
            } catch (CitrusRuntimeException e) {
                exception = e;

                logger.info("Caught exception of type {} '{}' - performing retry #{}", e.getClass().getName(), e.getMessage(), index);

                doAutoSleep();
                index++;
            }
        }

        if (exception != null) {
            logger.info("All retries failed - raising exception {}", exception.getClass().getName());
            throw exception;
        }
    }

    /**
     * Sleep amount of time in between iterations.
     */
    private void doAutoSleep() {
        if (autoSleep.toMillis() > 0) {
            logger.info("Sleeping {}", autoSleep);

            try {
                Thread.sleep(autoSleep.toMillis());
            } catch (InterruptedException e) {
                logger.error("Error during doc generation", e);
                Thread.currentThread().interrupt();
            }

            logger.info("Returning after {}", autoSleep);
        }
    }

    /**
     * Gets the duration this action sleeps in milliseconds.
     *
     * @return the autoSleep
     * @deprecated use {@link RepeatOnErrorUntilTrue#getAutoSleepDuration()} instead
     */
    @Deprecated(forRemoval = true)
    public Long getAutoSleep() {
        return autoSleep.toMillis();
    }

    /**
     * @return the duration this action sleeps in between retries
     */
    public Duration getAutoSleepDuration() {
        return autoSleep;
    }

    /**
     * Action builder.
     */
    public static class Builder extends AbstractIteratingContainerBuilder<RepeatOnErrorUntilTrue, Builder> {

        private Duration autoSleep = Duration.ofMillis(1_000L);

        /**
         * Fluent API action building entry method used in Java DSL.
         */
        public static Builder repeatOnError() {
            return new Builder();
        }

        /**
         * Adds a condition to this iterate container.
         */
        public Builder until(String condition) {
            condition(condition);
            return this;
        }

        /**
         * Adds a condition expression to this iterate container.
         */
        public Builder until(IteratingConditionExpression condition) {
            condition(condition);
            return this;
        }

        /**
         * Sets the auto sleep time in between repeats in milliseconds.
         *
         * @deprecated use {@link Builder#autoSleep(Duration)} instead
         */
        public Builder autoSleep(long autoSleepInMillis) {
            this.autoSleep = Duration.ofMillis(autoSleepInMillis);
            return this;
        }

        /**
         * Sets the sleep interval between retries of this action.
         */
        public Builder autoSleep(Duration autoSleep) {
            this.autoSleep = autoSleep;
            return this;
        }

        @Override
        public RepeatOnErrorUntilTrue doBuild() {
            return new RepeatOnErrorUntilTrue(this);
        }
    }
}
