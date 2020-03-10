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

package com.consol.citrus.dsl.runner;

import java.util.Stack;

import com.consol.citrus.TestAction;
import com.consol.citrus.TestActionBuilder;
import com.consol.citrus.TestActionContainerBuilder;
import com.consol.citrus.container.FinallySequence;
import com.consol.citrus.container.TestActionContainer;

/**
 * @author Christoph Deppisch
 * @since 2.3
 */
public abstract class AbstractTestBehavior extends DefaultTestRunner implements TestBehavior {

    /** Target test builder to add actions and variables on */
    private TestRunner target;

    /**
     * Subclasses must overwrite this apply building method in order
     * to add test action logic.
     */
    public abstract void apply();

    @Override
    public void apply(TestRunner target) {
        this.target = target;
        containers = new Stack<TestActionContainerBuilder<? extends TestActionContainer, ?>>() {
            @Override
            public TestActionContainerBuilder<? extends TestActionContainer, ?> push(TestActionContainerBuilder<? extends TestActionContainer, ?> item) {
                target.container(item);
                return item;
            }
        };
        apply();
    }

    @Override
    public <T> T variable(String name, T value) {
        return target.variable(name, value);
    }

    @Override
    public <A extends TestAction> TestActionBuilder<A> run(A testAction) {
        return target.run(testAction);
    }

    @Override
    public <T extends TestActionBuilder<?>> T run(T builder) {
        return target.run(builder);
    }

    @Override
    public FinallySequence.Builder doFinally() {
        return target.doFinally();
    }
}
