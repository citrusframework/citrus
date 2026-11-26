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

package org.citrusframework.testng.integration.container;

import org.citrusframework.Completable;
import org.citrusframework.TestAction;
import org.citrusframework.TestCaseRunner;
import org.citrusframework.annotations.CitrusResource;
import org.citrusframework.context.TestContext;
import org.citrusframework.dsl.TestActionSupport;
import org.citrusframework.annotations.CitrusTest;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.testng.spring.TestNGCitrusSpringSupport;
import org.testng.annotations.Optional;
import org.testng.annotations.Test;

import static org.citrusframework.util.TestUtils.waitForCompletion;

/**
 * @since 2.7.4
 */
public class AsyncJavaIT extends TestNGCitrusSpringSupport implements TestActionSupport {

    @Test
    @CitrusTest
    public void asyncContainer() {
        run(async().actions(
            stopTime(),
            sleep().milliseconds(500),
            echo("Hello Citrus"),
            stopTime()
        ));

        run(async().actions(
            echo("Hello Citrus"),
            action(context -> context.setVariable("anonymous", "anonymous")),
            sleep().milliseconds(500),
            action(context -> logger.info(context.getVariable("anonymous")))
        ));

        run(async().actions(
            stopTime(),
            sleep().milliseconds(200),
            echo("Hello Citrus"),
            stopTime()
        ));

        run(async().actions(
            echo("Hello Citrus"),
            action(context -> context.setVariable("anonymous", "anonymous")),
            sleep().milliseconds(200),
            action(context -> logger.info(context.getVariable("anonymous")))
        ));

        run(sleep().milliseconds(500L));

        run(traceVariables("anonymous"));
    }

    @Test(groups = "org.citrusframework.testng.ShouldFailGroup")
    @CitrusTest
    public void asyncContainerError(@Optional @CitrusResource TestCaseRunner runner,
                                    @Optional @CitrusResource TestContext context) {
        run(async().actions(
            fail("Should fail async container")
        ));

        waitForCompletion((Completable) runner.getTestCase(), context);

        assertException().exception(CitrusRuntimeException.class)
                .action((TestAction) ctx -> {
                    if (!ctx.hasExceptions()) {
                        throw new AssertionError("Expected async failure in test context");
                    }
                    throw ctx.getExceptions().remove(0);
                })
                .build()
                .execute(context);
    }
}
