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
 * Class executes nested test actions in loops. Iteration continues as long
 * as looping condition evaluates to true.
 *
 * Each loop an index variable is incremented. The index variable is accessible inside the nested
 * test actions as normal test variable. Iteration starts with index=1 and increments with a
 * default step=1.
 *
 * @author Christoph Deppisch
 */
public class Iterate extends AbstractIteratingActionContainer {
    /** Index increment step */
    private final int step;

    /**
     * Default constructor.
     */
    public Iterate(Builder builder) {
        super("iterate", builder);

        this.step = builder.step;
    }

    @Override
    public void executeIteration(TestContext context) {
        while (checkCondition(context)) {
            executeActions(context);

            index = index + step ;
        }
    }

    /**
     * Gets the step.
     * @return the step
     */
    public int getStep() {
        return step;
    }

    /**
     * Action builder.
     */
    public static class Builder extends AbstractIteratingContainerBuilder<Iterate, Builder> {

        private int step = 1;

        /**
         * Fluent API action building entry method used in Java DSL.
         * @return
         */
        public static Builder iterate() {
            return new Builder();
        }

        /**
         * Sets the step for each iteration.
         * @param step
         * @return
         */
        public Builder step(int step) {
            this.step = step;
            return this;
        }

        @Override
        public Iterate doBuild() {
            return new Iterate(this);
        }
    }
}
