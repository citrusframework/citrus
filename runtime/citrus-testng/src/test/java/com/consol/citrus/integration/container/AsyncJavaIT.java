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

package com.consol.citrus.integration.container;

import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.exceptions.TestCaseFailedException;
import com.consol.citrus.testng.TestNGCitrusSupport;
import org.testng.annotations.Test;

import static com.consol.citrus.DefaultTestActionBuilder.action;
import static com.consol.citrus.actions.EchoAction.Builder.echo;
import static com.consol.citrus.actions.FailAction.Builder.fail;
import static com.consol.citrus.actions.SleepAction.Builder.sleep;
import static com.consol.citrus.actions.StopTimeAction.Builder.stopTime;
import static com.consol.citrus.actions.TraceVariablesAction.Builder.traceVariables;
import static com.consol.citrus.container.Async.Builder.async;

/**
 * @author Christoph Deppisch
 * @since 2.7.4
 */
public class AsyncJavaIT extends TestNGCitrusSupport {

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
            action(context -> log.info(context.getVariable("anonymous")))
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
            action(context -> log.info(context.getVariable("anonymous")))
        ));

        run(sleep().milliseconds(500L));

        run(traceVariables("anonymous"));
    }

    @Test(groups = "com.consol.citrus.ShouldFailGroup", expectedExceptions = TestCaseFailedException.class)
    @CitrusTest
    public void asyncContainerError() {
        run(async().actions(
            fail("Should fail async container")
        ));
    }
}
