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

import org.citrusframework.AbstractTestContainerBuilder;
import org.citrusframework.TestActionBuilder;
import org.citrusframework.context.TestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Sequence container executing a set of nested test actions in simple sequence.
 *
 * @author Christoph Deppisch
 * @since 2007
 */
public class Sequence extends AbstractActionContainer {

    /** Logger */
    private static final Logger logger = LoggerFactory.getLogger(Sequence.class);

    /**
     * Default constructor.
     */
    public Sequence(Builder builder) {
        super("sequential", builder);
    }

    @Override
    public void doExecute(TestContext context) {
        for (TestActionBuilder<?> actionBuilder: actions) {
            executeAction(actionBuilder.build(), context);
        }

        logger.debug("Action sequence finished successfully");
    }

    /**
     * Action builder.
     */
    public static class Builder extends AbstractTestContainerBuilder<Sequence, Builder> {

        /**
         * Fluent API action building entry method used in Java DSL.
         * @return
         */
        public static Builder sequential() {
            return new Builder();
        }

        @Override
        public Sequence doBuild() {
            return new Sequence(this);
        }
    }
}
