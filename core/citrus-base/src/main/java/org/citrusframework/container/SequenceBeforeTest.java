/*
 * Copyright 2006-2011 the original author or authors.
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

import org.citrusframework.AbstractTestBoundaryContainerBuilder;
import org.citrusframework.TestAction;
import org.citrusframework.TestActionBuilder;
import org.citrusframework.context.TestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Sequence of test actions executed before a test case. Container execution can be restricted according to test name ,
 * package and test groups.
 *
 * @author Christoph Deppisch
 */
public class SequenceBeforeTest extends AbstractTestBoundaryActionContainer implements BeforeTest {

    /** Logger */
    private static final Logger logger = LoggerFactory.getLogger(SequenceBeforeTest.class);

    @Override
    public void doExecute(TestContext context) {
        if (actions == null || actions.isEmpty()) {
            return;
        }

        logger.info("Entering before test block");

        if (logger.isDebugEnabled()) {
            logger.debug("Executing " + actions.size() + " actions before test");
            logger.debug("");
        }

        for (TestActionBuilder<?> actionBuilder : actions)  {
            TestAction action = actionBuilder.build();
            action.execute(context);
        }
    }

    /**
     * Container builder.
     */
    public static class Builder extends AbstractTestBoundaryContainerBuilder<SequenceBeforeTest, Builder> {

        /**
         * Fluent API action building entry method used in Java DSL.
         * @return
         */
        public static Builder beforeTest() {
            return new Builder();
        }

        @Override
        public SequenceBeforeTest doBuild() {
            return new SequenceBeforeTest();
        }
    }
}
