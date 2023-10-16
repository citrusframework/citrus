/*
 * Copyright 2006-2018 the original author or authors.
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

package org.citrusframework.condition;

import java.util.Optional;

import org.citrusframework.TestAction;
import org.citrusframework.context.TestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Christoph Deppisch
 * @since 2.7.6
 */
public class ActionCondition extends AbstractCondition {

    /** Logger */
    private static final Logger logger = LoggerFactory.getLogger(ActionCondition.class);

    /** Action to execute */
    private TestAction action;

    /** Optional exception caught during action */
    private Exception caughtException;

    /**
     * Default constructor.
     */
    public ActionCondition() {
        super();
    }

    /**
     * Default constructor using test action to execute.
     * @param action The test action to execute
     */
    public ActionCondition(TestAction action) {
        this.action = action;
    }

    @Override
    public boolean isSatisfied(TestContext context) {
        if (action == null) {
            return false;
        }

        try {
            action.execute(context);
        } catch (Exception e) {
            this.caughtException = e;
            logger.warn(String.format("Nested action did not perform as expected - %s", Optional.ofNullable(e.getMessage())
                                                                                            .map(msg -> e.getClass().getName() + ": " + msg)
                                                                                            .orElseGet(() -> e.getClass().getName())));
            return false;
        }

        return true;
    }

    @Override
    public String getSuccessMessage(TestContext context) {
        return String.format("Test action condition success - action '%s' did perform as expected", getActionName());
    }

    @Override
    public String getErrorMessage(TestContext context) {
        if (caughtException != null) {
            return String.format("Failed to check test action condition - action '%s' did not perform as expected: %s", getActionName(), Optional.ofNullable(caughtException.getMessage())
                    .map(msg -> caughtException.getClass().getName() + ": " + msg)
                    .orElseGet(() -> caughtException.getClass().getName()));
        } else {
            return String.format("Failed to check test action condition - action '%s' did not perform as expected", getActionName());
        }
    }

    private String getActionName() {
        return Optional.ofNullable(action).map(TestAction::getName).orElse("unknown");
    }

    /**
     * Gets the test action of this condition
     *
     * @return The test action
     */
    public TestAction getAction() {
        return action;
    }

    /**
     * Sets the Action to set for this condition
     *
     * @param action The test action to set
     */
    public void setAction(TestAction action) {
        this.action = action;
    }

    /**
     * Gets the caughtException.
     *
     * @return The exception
     */
    public Exception getCaughtException() {
        return caughtException;
    }

    /**
     * Sets the caughtException.
     *
     * @param caughtException the exception to set
     */
    public void setCaughtException(Exception caughtException) {
        this.caughtException = caughtException;
    }

    @Override
    public String toString() {
        return "ActionCondition{" +
                "action=" + action +
                ", caughtException=" + caughtException +
                ", name=" + getName() +'}';
    }
}
