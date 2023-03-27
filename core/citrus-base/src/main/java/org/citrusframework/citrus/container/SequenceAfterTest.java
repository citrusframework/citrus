/*
 * Copyright 2006-2014 the original author or authors.
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

package org.citrusframework.citrus.container;

import org.citrusframework.citrus.AbstractTestBoundaryContainerBuilder;
import org.citrusframework.citrus.TestAction;
import org.citrusframework.citrus.TestActionBuilder;
import org.citrusframework.citrus.context.TestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

/**
 * Sequence of test actions executed after a test case. Container execution can be restricted according to test name ,
 * package and test groups.
 *
 * @author Christoph Deppisch
 */
public class SequenceAfterTest extends AbstractTestBoundaryActionContainer implements AfterTest {

    /** Logger */
    private static final Logger LOG = LoggerFactory.getLogger(SequenceAfterTest.class);

    @Override
    public void doExecute(TestContext context) {
        if (CollectionUtils.isEmpty(actions)) {
            return;
        }

        LOG.info("Entering after test block");

        if (LOG.isDebugEnabled()) {
            LOG.debug("Executing " + actions.size() + " actions after test");
            LOG.debug("");
        }

        for (TestActionBuilder<?> actionBuilder : actions)  {
            TestAction action = actionBuilder.build();
            action.execute(context);
        }
    }

    /**
     * Container builder.
     */
    public static class Builder extends AbstractTestBoundaryContainerBuilder<SequenceAfterTest, Builder> {

        /**
         * Fluent API action building entry method used in Java DSL.
         * @return
         */
        public static Builder afterTest() {
            return new Builder();
        }

        @Override
        public SequenceAfterTest doBuild() {
            return new SequenceAfterTest();
        }
    }
}
