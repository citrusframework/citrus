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

package org.citrusframework.container;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.citrusframework.AbstractTestContainerBuilder;
import org.citrusframework.TestAction;
import org.citrusframework.TestActionBuilder;
import org.citrusframework.actions.AbstractAsyncTestAction;
import org.citrusframework.context.TestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Christoph Deppisch
 * @since 2.7.4
 */
public class Async extends AbstractActionContainer {

    /** Logger */
    private static final Logger logger = LoggerFactory.getLogger(Async.class);

    private final List<TestActionBuilder<?>> errorActions;
    private final List<TestActionBuilder<?>> successActions;

    public Async(Builder builder) {
        super("async", builder);

        this.successActions = builder.successActions;
        this.errorActions = builder.errorActions;
    }

    @Override
    public void doExecute(TestContext context) {
        logger.debug("Async container forking action execution ...");

        AbstractAsyncTestAction asyncTestAction = new AbstractAsyncTestAction() {
            @Override
            public void doExecuteAsync(TestContext context) {
                for (TestActionBuilder<?> actionBuilder : actions) {
                    executeAction(actionBuilder.build(), context);
                }
            }

            @Override
            public void onError(TestContext context, Throwable error) {
                logger.info("Apply error actions after async container ...");
                for (TestActionBuilder<?> actionBuilder : errorActions) {
                    TestAction action = actionBuilder.build();
                    action.execute(context);
                }
            }

            @Override
            public void onSuccess(TestContext context) {
                logger.info("Apply success actions after async container ...");
                for (TestActionBuilder<?> actionBuilder : successActions) {
                    TestAction action = actionBuilder.build();
                    action.execute(context);
                }
            }
        };

        executeAction(asyncTestAction, context);
    }

    /**
     * Gets the successActions.
     *
     * @return
     */
    public List<TestAction> getSuccessActions() {
        return successActions.stream().map(TestActionBuilder::build).collect(Collectors.toList());
    }

    /**
     * Gets the errorActions.
     *
     * @return
     */
    public List<TestAction> getErrorActions() {
        return errorActions.stream().map(TestActionBuilder::build).collect(Collectors.toList());
    }

    /**
     * Action builder.
     */
    public static class Builder extends AbstractTestContainerBuilder<Async, Builder> {

        private final List<TestActionBuilder<?>> errorActions = new ArrayList<>();
        private final List<TestActionBuilder<?>> successActions = new ArrayList<>();

        /**
         * Fluent API action building entry method used in Java DSL.
         * @return
         */
        public static Builder async() {
            return new Builder();
        }

        /**
         * Adds a error action.
         * @param action
         * @return
         */
        public Builder errorAction(TestAction action) {
            this.errorActions.add(() -> action);
            return this;
        }

        /**
         * Adds a success action.
         * @param action
         * @return
         */
        public Builder successAction(TestAction action) {
            this.successActions.add(() -> action);
            return this;
        }

        /**
         * Adds a error action.
         * @param action
         * @return
         */
        public Builder errorAction(TestActionBuilder<?> action) {
            this.errorActions.add(action);
            return this;
        }

        /**
         * Adds a success action.
         * @param action
         * @return
         */
        public Builder successAction(TestActionBuilder<?> action) {
            this.successActions.add(action);
            return this;
        }

        /**
         * Adds one to many error actions.
         * @param actions
         * @return
         */
        public Builder errorActions(TestActionBuilder<?> ... actions) {
            this.errorActions.addAll(Arrays.asList(actions));
            return this;
        }

        /**
         * Adds one to many success actions.
         * @param actions
         * @return
         */
        public Builder successActions(TestActionBuilder<?> ... actions) {
            this.successActions.addAll(Arrays.asList(actions));
            return this;
        }

        /**
         * Adds one to many error actions.
         * @param actions
         * @return
         */
        public Builder errorActions(TestAction ... actions) {
            Stream.of(actions).map(action -> (TestActionBuilder<?>) () -> action).forEach(this::errorAction);
            return this;
        }

        /**
         * Adds one to many success actions.
         * @param actions
         * @return
         */
        public Builder successActions(TestAction ... actions) {
            Stream.of(actions).map(action -> (TestActionBuilder<?>) () -> action).forEach(this::successAction);
            return this;
        }

        @Override
        public Async build() {
            return doBuild();
        }

        @Override
        protected Async doBuild() {
            return new Async(this);
        }
    }
}
