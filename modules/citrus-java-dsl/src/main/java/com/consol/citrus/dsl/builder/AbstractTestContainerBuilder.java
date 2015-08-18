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
import com.consol.citrus.exceptions.CitrusRuntimeException;

import java.util.List;

/**
 * Abstract container builder takes care on calling the container runner when actions are placed in the container.
 * @author Christoph Deppisch
 * @since 2.3
 */
public abstract class AbstractTestContainerBuilder<T extends TestActionContainer> extends AbstractTestActionBuilder<T> implements TestActionContainerBuilder<T> {

    /** The test runner */
    protected TestRunner runner;

    /** The test designer */
    protected TestDesigner designer;

    /** The action container */
    protected final TestActionContainer container;

    /**
     * Default constructor with test runner and test action.
     * @param runner
     * @param container
     */
    public AbstractTestContainerBuilder(TestRunner runner, T container) {
        super(container);
        this.runner = runner;
        this.container = container;
    }

    /**
     * Default constructor.
     * @param designer
     * @param container
     */
    public AbstractTestContainerBuilder(TestDesigner designer, T container) {
        super(container);
        this.designer = designer;
        this.container = container;
    }

    /**
     * Delegates container execution to container runner or fills container with actions.
     * @param actions
     * @return
     */
    public TestActionContainer actions(TestAction ... actions) {
        if (runner != null) {
            for (int i = 0; i < actions.length; i++) {
                if (container.getActions().size() == i) {
                    container.addTestAction(actions[i]);
                } else if (!container.getActions().get(i).equals(actions[i])) {
                    container.getActions().add(i, actions[i]);
                }
            }

            if (container.getActions().size() != actions.length) {
                throw new CitrusRuntimeException("Invalid number of nested test actions for container execution - found unexpected actions");
            }

            return runner.run(container);
        } else {
            for (TestAction action : actions) {
                if (action instanceof TestActionBuilder<?>) {
                    container.addTestAction(((TestActionBuilder<?>) action).build());
                } else {
                    container.addTestAction(action);
                }
            }

            designer.action(container);
            return container;
        }
    }

    @Override
    public List<TestAction> getActions() {
        return super.build().getActions();
    }
}
