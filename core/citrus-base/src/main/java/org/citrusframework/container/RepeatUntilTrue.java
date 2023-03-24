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

/**
 * Typical implementation of repeat iteration loop. Nested test actions are executed until
 * aborting condition evaluates to true.
 *
 * Index is incremented each iteration and stored as test variable accessible in the nested test actions
 * as normal variable. Index starts with 1 by default.
 *
 * @author Christoph Deppisch
 */
public class RepeatUntilTrue extends AbstractIteratingActionContainer {

    /**
     * Default constructor.
     */
    public RepeatUntilTrue(Builder builder) {
        super("repeat", builder);
    }

    @Override
    public void executeIteration(TestContext context) {
        do {
            executeActions(context);
            index++;
        } while (!checkCondition(context));
    }

    /**
     * Action builder.
     */
    public static class Builder extends AbstractIteratingContainerBuilder<RepeatUntilTrue, Builder> {

        /**
         * Fluent API action building entry method used in Java DSL.
         * @return
         */
        public static RepeatUntilTrue.Builder repeat() {
            return new RepeatUntilTrue.Builder();
        }

        /**
         * Adds a condition to this iterate container.
         * @param condition
         * @return
         */
        public RepeatUntilTrue.Builder until(String condition) {
            condition(condition);
            return this;
        }

        /**
         * Adds a condition expression to this iterate container.
         * @param condition
         * @return
         */
        public RepeatUntilTrue.Builder until(IteratingConditionExpression condition) {
            condition(condition);
            return this;
        }

        @Override
        public RepeatUntilTrue doBuild() {
            return new RepeatUntilTrue(this);
        }
    }
}
