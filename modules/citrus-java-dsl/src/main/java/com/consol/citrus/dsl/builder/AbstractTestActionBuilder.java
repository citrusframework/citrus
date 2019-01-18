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
import com.consol.citrus.TestActor;
import com.consol.citrus.context.TestContext;

/**
 * @author Christoph Deppisch
 * @since 2.3
 */
public abstract class AbstractTestActionBuilder<T extends TestAction> implements TestAction, TestActionBuilder<T> {

    /** The test action observed by this builder */
    protected T action;

    /**
     * Default constructor with test action.
     * @param action
     */
    public AbstractTestActionBuilder(T action) {
        this.action = action;
    }

    /**
     * Sets the description of the test action.
     * @param description
     * @return
     */
    public AbstractTestActionBuilder<T> description(String description) {
        action.setDescription(description);
        return this;
    }

    /**
     * Sets the actor this action is related with.
     * @param actor
     * @return
     */
    public AbstractTestActionBuilder<T> actor(TestActor actor) {
        action.setActor(actor);
        return this;
    }

    /**
     * Gets the action.
     * @return the action to get.
     */
    public T build() {
        return action;
    }

    @Override
    public final void execute(TestContext context) {
        throw new IllegalStateException("Test action builder must not be executed");
    }

    @Override
    public final boolean isDisabled(TestContext context) {
        return false;
    }

    @Override
    public final String getName() {
        return action.getName();
    }

    @Override
    public final AbstractTestActionBuilder setName(String name) {
        action.setName(name);
        return this;
    }

    @Override
    public TestActor getActor() {
        return action.getActor();
    }

    @Override
    public TestAction setActor(TestActor actor) {
        action.setActor(actor);
        return this;
    }

    @Override
    public final String getDescription() {
        return action.getDescription();
    }

    @Override
    public final AbstractTestActionBuilder setDescription(String description) {
        action.setDescription(description);
        return this;
    }
}
