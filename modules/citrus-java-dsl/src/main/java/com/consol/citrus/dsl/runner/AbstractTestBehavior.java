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

import com.consol.citrus.TestAction;
import com.consol.citrus.dsl.builder.FinallySequenceBuilder;

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
        apply();
    }

    @Override
    public <T> T variable(String name, T value) {
        return target.variable(name, value);
    }

    @Override
    public <T extends TestAction> T run(T testAction) {
        return target.run(testAction);
    }

    @Override
    public FinallySequenceBuilder doFinally() {
        return target.doFinally();
    }
}
