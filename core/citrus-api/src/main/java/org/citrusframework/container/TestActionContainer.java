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

import java.util.List;

import org.citrusframework.TestAction;

/**
 * Container interface describing all test action containers that hold several embedded test actions
 * to execute.
 *
 * @author Christoph Deppisch
 */
public interface TestActionContainer extends TestAction {

    /**
     * Sets the embedded test actions to execute within this container.
     * @param actions
     */
    TestActionContainer setActions(List<TestAction> actions);

    /**
     * Get the embedded test actions within this container.
     */
    List<TestAction> getActions();

    /**
     * Get the number of embedded actions in this container.
     * @return
     */
    long getActionCount();

    /**
     * Adds one to many test actions to the nested action list.
     * @param action
     */
    TestActionContainer addTestActions(TestAction ... action);

    /**
     * Adds a test action to the nested action list.
     * @param action
     */
    TestActionContainer addTestAction(TestAction action);

    /**
     * Returns the index in the action chain for provided action instance.
     * @return the action index in the action list
     */
    int getActionIndex(TestAction action);

    /**
     * Sets the current active action executed.
     * @param action
     */
    void setActiveAction(TestAction action);

    /**
     * Sets the last action that has been executed.
     * @param action
     */
    void setExecutedAction(TestAction action);

    /**
     * Get the action that was executed most recently.
     * @return
     */
    TestAction getActiveAction();

    /**
     * Gets all nested actions that have been executed in the container.
     * @return
     */
    List<TestAction> getExecutedActions();

    /**
     * Get the test action with given index in list.
     * @param index
     * @return
     */
    TestAction getTestAction(int index);
}
