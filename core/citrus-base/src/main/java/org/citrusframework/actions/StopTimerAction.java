/*
 * Copyright 2006-2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.citrusframework.actions;

import org.citrusframework.AbstractTestActionBuilder;
import org.citrusframework.context.TestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Martin Maher
 * @since 2.5
 */
public class StopTimerAction extends AbstractTestAction {
    /** Logger */
    private static final Logger logger = LoggerFactory.getLogger(StopTimerAction.class);

    private final String timerId;

    /**
     * Default constructor.
     */
    public StopTimerAction(Builder builder) {
        super("stop-timer", builder);

        this.timerId = builder.timerId;
    }

    public String getTimerId() {
        return timerId;
    }

    @Override
    public void doExecute(TestContext context) {
        if (timerId != null) {
            boolean success = context.stopTimer(timerId);
            logger.info(String.format("Stopping timer %s - stop successful: %s", timerId, success));
        } else {
            context.stopTimers();
            logger.info("Stopping all timers");
        }
    }

    /**
     * Action builder.
     */
    public static final class Builder extends AbstractTestActionBuilder<StopTimerAction, Builder> {

        private String timerId;

        /**
         * Fluent API action building entry method used in Java DSL.
         * @return
         */
        public static Builder stopTimer() {
            return new Builder();
        }

        /**
         * Fluent API action building entry method used in Java DSL.
         * @param timerId
         * @return
         */
        public static Builder stopTimer(String timerId) {
            Builder builder = new Builder();
            builder.id(timerId);
            return builder;
        }

        public Builder id(String timerId) {
            this.timerId = timerId;
            return this;
        }

        @Override
        public StopTimerAction build() {
            return new StopTimerAction(this);
        }
    }
}
