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

package org.citrusframework.container;

import org.citrusframework.AbstractTestContainerBuilder;
import org.citrusframework.TestActionBuilder;

/**
 * Helper sequence to mark actions as finally actions that should be
 * executed in finally block of test case.
 *
 * @author Christoph Deppisch
 * @since 2.3
 */
public class FinallySequence extends Sequence {

    /**
     * Default constructor.
     *
     * @param builder
     */
    public FinallySequence(Builder builder) {
        super(new Sequence.Builder()
            .name(builder.getName())
            .description(builder.getDescription())
            .actor(builder.getActor())
            .actions(builder.getActions().toArray(new TestActionBuilder<?>[0]))
        );
    }

    /**
     * Action builder.
     */
    public static class Builder extends AbstractTestContainerBuilder<FinallySequence, Builder> {

        /**
         * Fluent API action building entry method used in Java DSL.
         * @return
         */
        public static Builder doFinally() {
            return new Builder();
        }

        @Override
        public FinallySequence doBuild() {
            return new FinallySequence(this);
        }
    }
}
