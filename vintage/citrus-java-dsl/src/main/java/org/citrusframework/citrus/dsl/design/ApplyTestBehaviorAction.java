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

package org.citrusframework.citrus.dsl.design;

import org.citrusframework.citrus.AbstractTestActionBuilder;
import org.citrusframework.citrus.actions.NoopTestAction;
import org.citrusframework.citrus.context.TestContext;

/**
 * @author Christoph Deppisch
 * @since 2.6
 */
public class ApplyTestBehaviorAction extends NoopTestAction {

    private final TestDesigner testDesigner;
    private final TestBehavior testBehavior;

    public ApplyTestBehaviorAction(Builder builder) {
        this.testDesigner = builder.testDesigner;
        this.testBehavior = builder.testBehavior;
    }

    @Override
    public void execute(TestContext context) {
        testBehavior.apply(testDesigner);
    }

    public static final class Builder extends AbstractTestActionBuilder<ApplyTestBehaviorAction, Builder> {
        private TestDesigner testDesigner;
        private TestBehavior testBehavior;

        public Builder designer(TestDesigner designer) {
            this.testDesigner = designer;
            return this;
        }

        public Builder behavior(TestBehavior behavior) {
            this.testBehavior = behavior;
            return this;
        }

        @Override
        public ApplyTestBehaviorAction build() {
            return new ApplyTestBehaviorAction(this);
        }
    }
}
