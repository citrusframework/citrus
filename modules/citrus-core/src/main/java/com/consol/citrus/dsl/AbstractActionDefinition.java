/*
 * Copyright 2006-2012 the original author or authors.
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

package com.consol.citrus.dsl;

import com.consol.citrus.TestAction;
import com.consol.citrus.context.TestContext;

/**
 * Abstract action definition implementation offers basic action delegation methods and 
 * generics.
 * 
 * @author Christoph Deppisch
 */
public class AbstractActionDefinition<T extends TestAction> implements TestAction {

    /** The test action observed by this definition */
    protected T action;
    
    /**
     * Default constructor with test action.
     * @param action
     */
    public AbstractActionDefinition(T action) {
        this.action = action;
    }
    
    public AbstractActionDefinition<T> description(String description) {
        action.setDescription(description);
        return this;
    }

    /**
     * Gets the action.
     * @return the action the action to get.
     */
    public T getAction() {
        return action;
    }

    /**
     * {@inheritDoc}
     */
    public void execute(TestContext context) {
        throw new IllegalStateException("Test action definition must not be executed");
    }

    /**
     * {@inheritDoc}
     */
    public String getName() {
        return action.getName();
    }

    /**
     * {@inheritDoc}
     */
    public void setName(String name) {
        action.setName(name);
    }

    /**
     * {@inheritDoc}
     */
    public String getDescription() {
        return action.getDescription();
    }

    /**
     * {@inheritDoc}
     */
    public void setDescription(String description) {
        action.setDescription(description);
    }
    
}
