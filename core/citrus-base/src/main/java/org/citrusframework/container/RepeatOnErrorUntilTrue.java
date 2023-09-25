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

package org.citrusframework.container;

import org.citrusframework.AbstractIteratingContainerBuilder;
import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Looping test container iterating the nested test actions in case an error occurred in one
 * of them. Iteration continues until a aborting condition evaluates to true.
 *
 * Number of iterations is kept in a index variable. The nested test actions can access this variable
 * as normal test variable.
 *
 * Between the iterations container can sleep automatically a given amount of time.
 *
 * @author Christoph Deppisch
 */
public class RepeatOnErrorUntilTrue extends AbstractIteratingActionContainer {
    /** Auto sleep in milliseconds */
    private final Long autoSleep;

    /** Logger */
    private static final Logger logger = LoggerFactory.getLogger(RepeatOnErrorUntilTrue.class);

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

        while(!checkCondition(context)) {
            try {
                exception = null;
                executeActions(context);
                break;
            } catch (CitrusRuntimeException e) {
                exception = e;

                logger.info("Caught exception of type " + e.getClass().getName() + " '" +
                        e.getMessage() + "' - performing retry #" + index);

                doAutoSleep();
                index++;
            }
        }

        if (exception != null) {
            logger.info("All retries failed - raising exception " + exception.getClass().getName());
            throw exception;
        }
    }

    /**
     * Sleep amount of time in between iterations.
     */
    private void doAutoSleep() {
        if (autoSleep > 0) {
            logger.info("Sleeping " + autoSleep + " milliseconds");

            try {
                Thread.sleep(autoSleep);
            } catch (InterruptedException e) {
                logger.error("Error during doc generation", e);
            }

            logger.info("Returning after " + autoSleep + " milliseconds");
        }
    }

    /**
     * Gets the autoSleep.
     * @return the autoSleep
     */
    public Long getAutoSleep() {
        return autoSleep;
    }

    /**
     * Action builder.
     */
    public static class Builder extends AbstractIteratingContainerBuilder<RepeatOnErrorUntilTrue, Builder> {

        private Long autoSleep = 1000L;

        /**
         * Fluent API action building entry method used in Java DSL.
         * @return
         */
        public static Builder repeatOnError() {
            return new Builder();
        }

        /**
         * Adds a condition to this iterate container.
         * @param condition
         * @return
         */
        public Builder until(String condition) {
            condition(condition);
            return this;
        }

        /**
         * Adds a condition expression to this iterate container.
         * @param condition
         * @return
         */
        public Builder until(IteratingConditionExpression condition) {
            condition(condition);
            return this;
        }

        /**
         * Sets the auto sleep time in between repeats in milliseconds.
         * @param autoSleepInMillis
         * @return
         */
        public Builder autoSleep(long autoSleepInMillis) {
            this.autoSleep = autoSleepInMillis;
            return this;
        }

        @Override
        public RepeatOnErrorUntilTrue doBuild() {
            return new RepeatOnErrorUntilTrue(this);
        }
    }
}
