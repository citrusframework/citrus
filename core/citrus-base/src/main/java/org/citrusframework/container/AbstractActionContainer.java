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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.citrusframework.AbstractTestContainerBuilder;
import org.citrusframework.Completable;
import org.citrusframework.TestAction;
import org.citrusframework.TestActionBuilder;
import org.citrusframework.actions.AbstractTestAction;
import org.citrusframework.context.TestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract base class for all containers holding several embedded test actions.
 *
 * @author Christoph Deppisch
 */
public abstract class AbstractActionContainer extends AbstractTestAction implements TestActionContainer, Completable {

    /** Logger */
    private final Logger logger = LoggerFactory.getLogger(getClass());

    /** List of nested actions */
    protected List<TestActionBuilder<?>> actions = new ArrayList<>();

    /** List of all executed actions during container run  */
    private final List<TestAction> executedActions = new ArrayList<>();

    /** Last executed action for error reporting reasons */
    private TestAction activeAction;

    public AbstractActionContainer() {
        super();
    }

    public AbstractActionContainer(String name, AbstractTestContainerBuilder<?, ?> builder) {
        super(name, builder);
        actions = builder.getActions();
    }

    /**
     * Runs the give action and makes sure to properly set active and executed action state for this container.
     * @param action
     * @param context
     */
    protected void executeAction(TestAction action, TestContext context) {
        try {
            setActiveAction(action);
            action.execute(context);
        } finally {
            setExecutedAction(action);
        }
    }

    @Override
    public AbstractActionContainer setActions(List<TestAction> actions) {
        this.actions = actions.stream().map(action -> (TestActionBuilder<?>) () -> action).collect(Collectors.toList());
        return this;
    }

    @Override
    public AbstractActionContainer addTestActions(TestAction ... toAdd) {
        actions.addAll((Stream.of(toAdd).map(action -> (TestActionBuilder<?>) () -> action).collect(Collectors.toList())));
        return this;
    }

    public AbstractActionContainer addTestActions(TestActionBuilder<?> ... toAdd) {
        actions.addAll(Arrays.asList(toAdd));
        return this;
    }

    @Override
    public boolean isDone(TestContext context) {
        if (actions.isEmpty() || isDisabled(context)) {
            return true;
        }

        if (activeAction == null && executedActions.isEmpty()) {
            return true;
        }

        if (!executedActions.contains(activeAction)) {
            return false;
        }

        for (TestAction action : new ArrayList<>(executedActions)) {
            if (action instanceof Completable && !((Completable) action).isDone(context)) {
                if (logger.isDebugEnabled()) {
                    logger.debug(Optional.ofNullable(action.getName()).filter(name -> name.trim().length() > 0)
                            .orElseGet(() -> action.getClass().getName()) + " not completed yet");
                }
                return false;
            }
        }

        return true;
    }

    @Override
    public List<TestAction> getActions() {
        return actions.stream().map(TestActionBuilder::build).collect(Collectors.toList());
    }

    @Override
    public long getActionCount() {
        return actions.size();
    }

    @Override
    public AbstractActionContainer addTestAction(TestAction action) {
        actions.add(() -> action);
        return this;
    }

    public AbstractActionContainer addTestAction(TestActionBuilder<?> action) {
        actions.add(action);
        return this;
    }

    @Override
    public int getActionIndex(TestAction action) {
        return executedActions.indexOf(action);
    }

    @Override
    public TestAction getActiveAction() {
        return activeAction;
    }

    @Override
    public void setActiveAction(TestAction action) {
        this.activeAction = action;
    }

    @Override
    public void setExecutedAction(TestAction action) {
        this.executedActions.add(action);
    }

    @Override
    public List<TestAction> getExecutedActions() {
        return executedActions;
    }

    @Override
    public TestAction getTestAction(int index) {
        if (index < this.executedActions.size()) {
            return this.executedActions.get(index);
        }

        return actions.get(index).build();
    }
}
