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
import com.consol.citrus.container.TestActionContainer;
import com.consol.citrus.exceptions.CitrusRuntimeException;

/**
 * @author Christoph Deppisch
 * @since 2.3
 */
public class DefaultContainerRunner implements ContainerRunner, ExceptionContainerRunner {

    /** The test action container to run */
    protected final TestActionContainer container;

    /** Target test runner */
    protected final TestRunner testRunner;

    /**
     * Default constructor initializing test action container and test runner fields.
     * @param container
     * @param testRunner
     */
    public DefaultContainerRunner(TestActionContainer container, TestRunner testRunner) {
        this.container = container;
        this.testRunner = testRunner;
    }

    @Override
    public TestActionContainer actions(TestAction ... actions) {
        checkActionOrder(actions);
        return testRunner.run(container);
    }

    @Override
    public TestActionContainer when(TestAction ... actions) {
        checkActionOrder(actions);
        return testRunner.run(container);
    }

    /**
     * Walks through the actions and checks correct order with given action container. It is possible that
     * some test actions were added that are not recognized yet. This is the case when user adds anonymous inner classes
     * or test action instances that were not built with the Java DSL methods.
     * @param actions
     */
    private void checkActionOrder(TestAction[] actions) {
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
    }
}
