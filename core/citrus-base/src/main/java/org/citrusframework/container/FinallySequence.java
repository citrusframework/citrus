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

package org.citrusframework.container;

import java.util.Optional;

import org.citrusframework.AbstractTestContainerBuilder;
import org.citrusframework.TestAction;
import org.citrusframework.TestActionBuilder;
import org.citrusframework.context.TestContext;

/**
 * Helper sequence to mark actions as finally actions that should be
 * executed in finally block of test case.
 *
 * @since 2.3
 */
public class FinallySequence extends Sequence {

    /**
     * Default constructor.
     */
    public FinallySequence(Builder builder) {
        super(new Sequence.Builder()
            .name(Optional.ofNullable(builder.getName()).orElse("finally"))
            .description(builder.getDescription())
            .actor(builder.getActor())
            .actions(builder.getActions().toArray(new TestActionBuilder<?>[0]))
        );
    }

    @Override
    protected void executeAction(TestAction action, TestContext context) {
        context.doFinally(() -> action);
    }

    /**
     * Action builder.
     */
    public static class Builder extends AbstractTestContainerBuilder<FinallySequence, Builder>
            implements FinallyContainerBuilder<FinallySequence, Builder> {

        public static Builder doFinally() {
            return new Builder();
        }

        @Override
        public FinallySequence doBuild() {
            return new FinallySequence(this);
        }
    }
}
