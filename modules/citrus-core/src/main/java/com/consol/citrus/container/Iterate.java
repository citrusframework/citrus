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

package com.consol.citrus.container;

import com.consol.citrus.context.TestContext;

/**
 * Class executes nested test actions in loops. Iteration continues as long
 * as looping condition evaluates to true.
 * 
 * See {@link com.consol.citrus.util.BooleanExpressionParser} for supported boolean expressions that define
 * the conditioning.
 * 
 * Each loop an index variable is incremented. The index variable is accessible inside the nested
 * test actions as normal test variable. Iteration starts with index=1 and increments with a 
 * default step=1.
 * 
 * @author Christoph Deppisch
 */
public class Iterate extends AbstractIteratingActionContainer {
    /** Index increment step */
    private int step = 1;

    /**
     * Default constructor.
     */
    public Iterate() {
        setName("iterate");
    }

    /**
     * @see AbstractIteratingActionContainer#executeIteration(com.consol.citrus.context.TestContext)
     * @throws com.consol.citrus.exceptions.CitrusRuntimeException
     */
    @Override
    public void executeIteration(TestContext context) {
        while (checkCondition(context)) {
            executeActions(context);

            index = index + step ;
        }
    }

    /**
     * Step o increment.
     * @param step the step to set
     */
    public void setStep(int step) {
        this.step = step;
    }

    /**
     * Gets the step.
     * @return the step
     */
    public int getStep() {
        return step;
    }
}
