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

package org.citrusframework.citrus.container;

import org.citrusframework.citrus.AbstractSuiteContainerBuilder;
import org.citrusframework.citrus.TestAction;
import org.citrusframework.citrus.TestActionBuilder;
import org.citrusframework.citrus.context.TestContext;
import org.citrusframework.citrus.exceptions.CitrusRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Sequence of Citrus test actions that get executed after a test suite run. Sequence should
 * decide weather to execute according to given suite name and included test groups if any.
 *
 * @author Christoph Deppisch
 */
public class SequenceAfterSuite extends AbstractSuiteActionContainer implements AfterSuite {

    /** Logger */
    private static Logger log = LoggerFactory.getLogger(SequenceAfterSuite.class);

    @Override
    public void doExecute(TestContext context) {
        boolean success = true;

        log.info("Entering after suite block");

        if (log.isDebugEnabled()) {
            log.debug("Executing " + actions.size() + " actions after suite");
            log.debug("");
        }

        for (TestActionBuilder<?> actionBuilder : actions)  {
            TestAction action = actionBuilder.build();
            try {
                /* Executing test action and validate its success */
                action.execute(context);
            } catch (Exception e) {
                log.error("After suite action failed " + action.getName() + "Nested exception is: ", e);
                log.error("Continue after suite actions");
                success = false;
            }
        }

        if (!success) {
            throw new CitrusRuntimeException("Error in after suite");
        }
    }

    /**
     * Container builder.
     */
    public static class Builder extends AbstractSuiteContainerBuilder<SequenceAfterSuite, Builder> {

        /**
         * Fluent API action building entry method used in Java DSL.
         * @return
         */
        public static Builder afterSuite() {
            return new Builder();
        }

        @Override
        public SequenceAfterSuite doBuild() {
            return new SequenceAfterSuite();
        }
    }
}
