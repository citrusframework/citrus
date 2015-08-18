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

import com.consol.citrus.TestAction;
import com.consol.citrus.container.TestActionContainer;
import com.consol.citrus.dsl.design.TestDesigner;
import com.consol.citrus.dsl.runner.TestRunner;

/**
 * Special exception container builder adds when clause that receives a sequence of actions to be watched at for possible exceptions.
 * @author Christoph Deppisch
 * @since 2.3
 */
public abstract class AbstractExceptionContainerBuilder<T extends TestActionContainer> extends AbstractTestContainerBuilder<T> {

    /**
     * Default constructor with test runner and test action.
     * @param runner
     * @param container
     */
    public AbstractExceptionContainerBuilder(TestRunner runner, T container) {
        super(runner, container);
    }

    /**
     * Default constructor.
     * @param container
     */
    public AbstractExceptionContainerBuilder(TestDesigner designer, T container) {
        super(designer, container);
    }

    /**
     * Delegates container execution to container runner or fills container with actions.
     * @param actions
     * @return
     */
    public TestActionContainer when(TestAction... actions) {
        return actions(actions);
    }
}
