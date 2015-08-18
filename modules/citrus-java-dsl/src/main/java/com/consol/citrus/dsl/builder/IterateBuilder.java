/*
 * Copyright 2006-2015 the original author or authors.
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

package com.consol.citrus.dsl.builder;

import com.consol.citrus.container.Iterate;
import com.consol.citrus.container.IteratingConditionExpression;
import com.consol.citrus.dsl.design.TestDesigner;
import com.consol.citrus.dsl.runner.TestRunner;

/**
 * @author Christoph Deppisch
 * @since 2.3
 */
public class IterateBuilder extends AbstractTestContainerBuilder<Iterate> {

    /**
     * Default constructor using designer and action container.
     * @param designer
     * @param action
     */
    public IterateBuilder(TestDesigner designer, Iterate action) {
        super(designer, action);
    }

    /**
     * Default constructor using test designer.
     * @param designer
     */
    public IterateBuilder(TestDesigner designer) {
        this(designer, new Iterate());
    }

    /**
     * Default constructor using runner and action container.
     * @param runner
     * @param action
     */
    public IterateBuilder(TestRunner runner, Iterate action) {
        super(runner, action);
    }

    /**
     * Default constructor using test runner.
     * @param runner
     */
    public IterateBuilder(TestRunner runner) {
        this(runner, new Iterate());
    }

    /**
     * Adds a condition to this iterate container.
     * @param condition
     * @return
     */
    public IterateBuilder condition(String condition) {
        action.setCondition(condition);
        return this;
    }

    /**
     * Adds a condition expression to this iterate container.
     * @param condition
     * @return
     */
    public IterateBuilder condition(IteratingConditionExpression condition) {
        action.setConditionExpression(condition);
        return this;
    }
    
    /**
     * Sets the index variable name.
     * @param name
     * @return
     */
    public IterateBuilder index(String name) {
        action.setIndexName(name);
        return this;
    }
    
    /**
     * Sets the index start value.
     * @param index
     * @return
     */
    public IterateBuilder startsWith(int index) {
        action.setStart(index);
        return this;
    }
    
    /**
     * Sets the step for each iteration.
     * @param step
     * @return
     */
    public IterateBuilder step(int step) {
        action.setStep(step);
        return this;
    }

}
