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

package org.citrusframework.citrus.dsl.runner;

import org.citrusframework.citrus.actions.AbstractTestAction;
import org.citrusframework.citrus.context.TestContext;

/**
 * @author Christoph Deppisch
 * @since 2.6
 */
public class BeforeTestRunner extends TestRunnerBeforeTestSupport {

    private CounterTestAction counterTestAction = new CounterTestAction();

    @Override
    public void beforeTest(TestRunner runner) {
        runner.echo("This action should be executed before test");
        runner.run(counterTestAction);
    }

    public int getExecutionCount() {
        return counterTestAction.getCounter();
    }

    private static class CounterTestAction extends AbstractTestAction {

        private int counter = 0;

        @Override
        public void doExecute(TestContext context) {
            counter++;
        }

        /**
         * Gets the value of the counter property.
         *
         * @return the counter
         */
        public int getCounter() {
            return counter;
        }
    }
}
