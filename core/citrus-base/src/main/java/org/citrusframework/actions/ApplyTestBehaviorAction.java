/*
 * Copyright the original author or authors.
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.citrusframework.AbstractTestContainerBuilder;
import org.citrusframework.TestActionRunner;
import org.citrusframework.TestActionRunnerAware;
import org.citrusframework.TestBehavior;
import org.citrusframework.api.actions.ApplyTestBehaviorActionBuilder;
import org.citrusframework.base.NestedTestActionRunner;
import org.citrusframework.container.AbstractActionContainer;
import org.citrusframework.context.TestContext;
import org.citrusframework.context.TestContextFactory;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.util.StringUtils;
import org.citrusframework.variable.VariableUtils;

/**
 * Applies a test behavior. The actions the behavior runs are nested in this container, so test reports
 * show them grouped under the behavior's name.
 * <p>
 * The behavior shares the variables of the calling test by default. An isolated behavior works on a copy
 * of the test context instead and only hands back the variables it publishes.
 *
 * @since 2.6
 */
public class ApplyTestBehaviorAction extends AbstractActionContainer {

    private static final String DEFAULT_NAME = "apply-behavior";

    private final TestActionRunner runner;
    private final TestBehavior behavior;
    private final boolean globalContext;
    private final List<String> publish;
    private final List<String> requires;

    /** Isolated test context of the current execution, handed back to the caller once async actions are done */
    private volatile TestContext isolatedScope;

    public ApplyTestBehaviorAction(Builder builder) {
        super(Optional.ofNullable(builder.behavior)
                .map(TestBehavior::getName)
                .filter(StringUtils::hasText)
                .orElse(DEFAULT_NAME), builder);

        // Nested actions belong to this execution only, never to the builder that may build again (e.g. in iterations)
        this.actions = new ArrayList<>();

        this.runner = Optional.ofNullable(builder.runner).orElse(builder.injectedRunner);
        this.behavior = builder.behavior;
        this.globalContext = builder.globalContext;
        this.publish = List.copyOf(builder.publish);
        this.requires = List.copyOf(builder.requires);
    }

    @Override
    public void doExecute(TestContext context) {
        if (behavior == null) {
            throw new CitrusRuntimeException("Missing test behavior to apply");
        }

        verifyRequiredVariables(context);

        if (globalContext) {
            behavior.apply(new NestedTestActionRunner(this, context, runner));
        } else {
            applyIsolated(context);
        }

        publishVariables(globalContext ? context : isolatedScope, context);
    }

    private void applyIsolated(TestContext context) {
        if (context.hasExceptions()) {
            throw context.getExceptions().remove(0);
        }

        isolatedScope = TestContextFactory.copyOf(context);
        try {
            behavior.apply(new NestedTestActionRunner(this, isolatedScope, runner));
        } finally {
            handBack(isolatedScope, context);
        }
    }

    /**
     * Moves exceptions raised by forked actions and finally actions registered on the isolated test context
     * to the calling test context, where the test case picks them up.
     */
    private static void handBack(TestContext scope, TestContext context) {
        context.getExceptions().addAll(scope.getExceptions());
        scope.getExceptions().clear();
        context.getFinalActions().addAll(scope.getFinalActions());
        scope.getFinalActions().clear();
    }

    @Override
    public boolean isDone(TestContext context) {
        boolean done = super.isDone(context);

        TestContext scope = isolatedScope;
        if (done && scope != null) {
            handBack(scope, context);
        }

        return done;
    }

    private void verifyRequiredVariables(TestContext context) {
        for (String variable : requires) {
            if (!context.getVariables().containsKey(VariableUtils.cutOffVariablesPrefix(variable))) {
                throw new CitrusRuntimeException(String.format(
                        "Missing required variable '%s' for test behavior '%s'", variable, getName()));
            }
        }
    }

    private void publishVariables(TestContext scope, TestContext context) {
        for (String variable : publish) {
            String name = VariableUtils.cutOffVariablesPrefix(variable);
            if (!scope.getVariables().containsKey(name)) {
                throw new CitrusRuntimeException(String.format(
                        "Test behavior '%s' did not set published variable '%s'", getName(), variable));
            }

            context.setVariable(name, scope.getVariables().get(name));
        }
    }

    public boolean isGlobalContext() {
        return globalContext;
    }

    public static final class Builder extends AbstractTestContainerBuilder<ApplyTestBehaviorAction, Builder>
            implements ApplyTestBehaviorActionBuilder<ApplyTestBehaviorAction>, TestActionRunnerAware {

        private TestActionRunner runner;
        private TestActionRunner injectedRunner;
        private TestBehavior behavior;
        private boolean globalContext = true;
        private final List<String> publish = new ArrayList<>();
        private final List<String> requires = new ArrayList<>();

        public static Builder apply() {
            return new Builder();
        }

        public static Builder apply(TestBehavior behavior) {
            Builder builder = new Builder();
            builder.behavior = behavior;
            return builder;
        }

        @Override
        public Builder behavior(TestBehavior behavior) {
            this.behavior = behavior;
            return this;
        }

        @Override
        public Builder on(TestActionRunner runner) {
            this.runner = runner;
            return this;
        }

        @Override
        public Builder isolated() {
            return globalContext(false);
        }

        @Override
        public Builder globalContext(boolean enabled) {
            this.globalContext = enabled;
            return this;
        }

        @Override
        public Builder publish(String... variableNames) {
            this.publish.addAll(Arrays.asList(variableNames));
            return this;
        }

        @Override
        public Builder requires(String... variableNames) {
            this.requires.addAll(Arrays.asList(variableNames));
            return this;
        }

        /**
         * Receives the runner that runs this action. A runner set explicitly with {@link #on(TestActionRunner)} wins.
         */
        @Override
        public void setTestActionRunner(TestActionRunner runner) {
            this.injectedRunner = runner;
        }

        @Override
        protected ApplyTestBehaviorAction doBuild() {
            if (!actions.isEmpty()) {
                throw new CitrusRuntimeException("Test behavior action does not accept nested actions - " +
                        "the actions to run are defined by the test behavior");
            }

            return new ApplyTestBehaviorAction(this);
        }
    }
}
