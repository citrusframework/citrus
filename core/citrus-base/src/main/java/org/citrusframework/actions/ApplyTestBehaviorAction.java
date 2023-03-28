/*
 * Copyright 2006-2016 the original author or authors.
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

import org.citrusframework.AbstractTestActionBuilder;
import org.citrusframework.TestActionRunner;
import org.citrusframework.TestBehavior;
import org.citrusframework.context.TestContext;

/**
 * @author Christoph Deppisch
 * @since 2.6
 */
public class ApplyTestBehaviorAction extends AbstractTestAction {

    private final TestActionRunner runner;
    private final TestBehavior behavior;

    public ApplyTestBehaviorAction(Builder builder) {
        super("apply-behavior", builder);

        this.runner = builder.runner;
        this.behavior = builder.behavior;
    }

    @Override
    public void doExecute(TestContext context) {
        behavior.apply(runner);
    }

    public static final class Builder extends AbstractTestActionBuilder<ApplyTestBehaviorAction, Builder> {
        private TestActionRunner runner;
        private TestBehavior behavior;

        public static Builder apply() {
            return new Builder();
        }

        public static Builder apply(TestBehavior behavior) {
            Builder builder = new Builder();
            builder.behavior = behavior;
            return builder;
        }

        public Builder behavior(TestBehavior behavior) {
            this.behavior = behavior;
            return this;
        }

        public Builder on(TestActionRunner runner) {
            this.runner = runner;
            return this;
        }

        @Override
        public ApplyTestBehaviorAction build() {
            return new ApplyTestBehaviorAction(this);
        }
    }
}
