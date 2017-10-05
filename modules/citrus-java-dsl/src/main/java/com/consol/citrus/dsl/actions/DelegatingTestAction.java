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

package com.consol.citrus.dsl.actions;

import com.consol.citrus.TestAction;
import com.consol.citrus.TestActor;
import com.consol.citrus.actions.AbstractTestAction;
import com.consol.citrus.context.TestContext;

/**
 * Test action delegates execution and other operations to nested test action that can be set once. Delegate is used in Java DSL
 * when delegating test action execution to a set of actions. The action to execute can be set to some later time then.
 *
 * @author Christoph Deppisch
 * @since 2.4
 */
public class DelegatingTestAction<T extends TestAction> extends AbstractTestAction {

    /** Delegate */
    private T delegate;

    /**
     * Default constructor.
     */
    public DelegatingTestAction() {
        super();
    }

    /**
     * Constructor using the delegate test action.
     * @param delegate
     */
    public DelegatingTestAction(T delegate) {
        this.delegate = delegate;
    }

    @Override
    public void doExecute(TestContext context) {
        if (delegate != null) {
            delegate.execute(context);
        }
    }

    @Override
    public boolean isDisabled(TestContext context) {
        return delegate.isDisabled(context);
    }

    @Override
    public TestActor getActor() {
        return delegate.getActor();
    }

    @Override
    public AbstractTestAction setActor(TestActor actor) {
        delegate.setActor(actor);
        return this;
    }

    @Override
    public String getDescription() {
        return delegate.getDescription();
    }

    @Override
    public AbstractTestAction setDescription(String description) {
        delegate.setDescription(description);
        return this;
    }

    @Override
    public String getName() {
        return delegate.getName();
    }

    @Override
    public AbstractTestAction setName(String name) {
        delegate.setName(name);
        return this;
    }

    /**
     * Sets the delegate test action. Should be set only once.
     * @param delegate
     */
    public void setDelegate(T delegate) {
        this.delegate = delegate;
    }

    /**
     * Gets the delegate test action.
     * @return
     */
    public T getDelegate() {
        return delegate;
    }
}
