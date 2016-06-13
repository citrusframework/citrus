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

import com.consol.citrus.container.*;
import com.consol.citrus.dsl.design.TestDesigner;
import com.consol.citrus.dsl.runner.TestRunner;
import org.hamcrest.Matcher;

/**
 * Action builder creates a conditional container, which executes nested test actions
 * if condition expression evaluates to true.
 * 
 * @author Christoph Deppisch
 * @since 2.3
 */
public class ConditionalBuilder extends AbstractTestContainerBuilder<Conditional> {

    /**
     * Constructor using action field.
     * @param designer
     * @param action
     */
    public ConditionalBuilder(TestDesigner designer, Conditional action) {
        super(designer, action);
    }

    /**
     * Default constructor.
     * @param designer
     */
    public ConditionalBuilder(TestDesigner designer) {
        this(designer, new Conditional());
    }

    /**
     * Default constructor using runner and action container.
     * @param runner
     * @param action
     */
    public ConditionalBuilder(TestRunner runner, Conditional action) {
        super(runner, action);
    }

    /**
     * Default constructor using test runner.
     * @param runner
     */
    public ConditionalBuilder(TestRunner runner) {
        this(runner, new Conditional());
    }

    /**
     * Condition which allows execution if true.
     * @param expression
     */
    public ConditionalBuilder when(String expression) {
        action.setCondition(expression);
        return this;
    }

    /**
     * Condition which allows execution if evaluates to true.
     * @param expression
     */
    public ConditionalBuilder when(ConditionExpression expression) {
        action.setConditionExpression(expression);
        return this;
    }

    /**
     * Condition which allows execution if evaluates to true.
     * @param expression
     */
    public ConditionalBuilder when(Object value, Matcher expression) {
        action.setConditionExpression(new HamcrestConditionExpression(expression, value));
        return this;
    }
}
