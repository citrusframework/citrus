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

package org.citrusframework.citrus.integration.runner;

import org.citrusframework.citrus.actions.AbstractTestAction;
import org.citrusframework.citrus.annotations.CitrusTest;
import org.citrusframework.citrus.context.TestContext;
import org.citrusframework.citrus.dsl.testng.TestNGCitrusTestRunner;
import org.citrusframework.citrus.exceptions.TestCaseFailedException;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 * @since 2.7.4
 */
public class AsyncTestRunnerIT extends TestNGCitrusTestRunner {

    @Test
    @CitrusTest
    public void asyncContainer() {
        async().actions(
            stopTime(),
            sleep(1000),
            echo("Hello Citrus"),
            stopTime()
        );

        async().actions(
            echo("Hello Citrus"),
            () -> new AbstractTestAction() {
                @Override
                public void doExecute(TestContext context) {
                    context.setVariable("anonymous", "anonymous");
                }
            },
            sleep(1000),
            () -> new AbstractTestAction() {
                @Override
                public void doExecute(TestContext context) {
                    log.info(context.getVariable("anonymous"));
                }
            }
        );

        sleep(1000L);

        traceVariables("anonymous");
    }

    @Test(groups = "org.citrusframework.citrus.ShouldFailGroup", expectedExceptions = TestCaseFailedException.class)
    @CitrusTest
    public void asyncContainerError() {
        async().actions(
            fail("Should fail async container")
        );
    }
}
