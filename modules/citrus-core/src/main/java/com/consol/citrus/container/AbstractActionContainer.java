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

package com.consol.citrus.container;

import com.consol.citrus.Completable;
import com.consol.citrus.TestAction;
import com.consol.citrus.actions.AbstractTestAction;
import com.consol.citrus.context.TestContext;

import java.util.*;

/**
 * Abstract base class for all containers holding several embedded test actions.
 * 
 * @author Christoph Deppisch
 */
public abstract class AbstractActionContainer extends AbstractTestAction implements TestActionContainer, Completable {

    /** List of nested actions */
    protected List<TestAction> actions = new ArrayList<>();

    /** List of all executed actions during container run  */
    private List<TestAction> executedActions = new ArrayList<>();

    /** Last executed action for error reporting reasons */
    private TestAction activeAction;
    
    @Override
    public AbstractActionContainer setActions(List<TestAction> actions) {
        this. actions = actions;
        return this;
    }

    @Override
    public AbstractActionContainer addTestActions(TestAction ... toAdd) {
        actions.addAll(Arrays.asList(toAdd));
        return this;
    }

    @Override
    public boolean isDone(TestContext context) {
        return isDisabled(context) || executedActions.stream().filter(action -> action instanceof Completable)
                                .map(Completable.class::cast)
                                .allMatch(action -> action.isDone(context));
    }

    @Override
    public List<TestAction> getActions() {
        return actions;
    }

    @Override
    public long getActionCount() {
        return actions.size();
    }

    @Override
    public AbstractActionContainer addTestAction(TestAction action) {
        actions.add(action);
        return this;
    }

    @Override
    public int getActionIndex(TestAction action) {
        return actions.indexOf(action);
    }

    @Override
    public TestAction getActiveAction() {
        return activeAction;
    }

    @Override
    public void setActiveAction(TestAction action) {
        this.activeAction = action;
        this.executedActions.add(action);
    }

    @Override
    public List<TestAction> getExecutedActions() {
        return executedActions;
    }

    @Override
    public TestAction getTestAction(int index) {
        return actions.get(index);
    }
}
