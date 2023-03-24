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

package org.citrusframework.actions;

import java.util.Optional;

import org.citrusframework.AbstractTestActionBuilder;
import org.citrusframework.TestAction;
import org.citrusframework.TestActor;
import org.citrusframework.TestActorAware;
import org.citrusframework.common.Described;
import org.citrusframework.common.Named;
import org.citrusframework.context.TestContext;

/**
 * Abstract base class for test actions. Class provides a default name and description.
 * @author Christoph Deppisch
 */
public abstract class AbstractTestAction implements TestAction, Named, Described, TestActorAware {

    /** Describing the test action */
    protected String description;

    /** TestAction name injected as spring bean name */
    private String name = this.getClass().getSimpleName();

    /** This actions explicit test actor */
    private TestActor actor;

    protected AbstractTestAction() {
        super();
    }

    public AbstractTestAction(String name, AbstractTestActionBuilder<?, ?> builder) {
        this.name = Optional.ofNullable(builder.getName()).orElse(name);
        this.description = builder.getDescription();
        this.actor = builder.getActor();
    }

    /**
     * Do basic logging and delegate execution to subclass.
     */
    public void execute(TestContext context) {
        if (!isDisabled(context)) {
            doExecute(context);
        }
    }

    /**
     * Checks if this test action is disabled. Delegates to test actor defined
     * for this test action by default. Subclasses may add additional disabled logic here.
     *
     * @param context the current test context.
     * @return
     */
    public boolean isDisabled(TestContext context) {
        if (actor != null) {
            return actor.isDisabled();
        } else {
            return false;
        }
    }

    /**
     * Subclasses may add custom execution logic here.
     */
    public abstract void doExecute(TestContext context);

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public AbstractTestAction setDescription(String description) {
        this.description = description;
        return this;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the actor.
     * @return the actor the actor to get.
     */
    public TestActor getActor() {
        return actor;
    }

    /**
     * Sets the actor.
     * @param actor the actor to set
     */
    public AbstractTestAction setActor(TestActor actor) {
        this.actor = actor;
        return this;
    }
}
