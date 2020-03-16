/*
 * Copyright 2006-2013 the original author or authors.
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

import com.consol.citrus.actions.AbstractTestAction;
import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.testng.TestNGCitrusSupport;
import org.testng.annotations.Test;

import static com.consol.citrus.actions.EchoAction.Builder.echo;
import static com.consol.citrus.actions.SleepAction.Builder.sleep;
import static com.consol.citrus.actions.StopTimeAction.Builder.stopTime;
import static com.consol.citrus.container.Sequence.Builder.sequential;

/**
 * @author Christoph Deppisch
 */
@Test
public class SequentialJavaIT extends TestNGCitrusSupport {

    @CitrusTest
    public void sequentialContainer() {
        run(sequential().actions(
            stopTime(),
            sleep().milliseconds(500),
            echo("Hello Citrus"),
            stopTime()
        ));

        run(sequential().actions(
            echo("Hello Citrus"),
            () -> new AbstractTestAction() {
                @Override
                public void doExecute(TestContext context) {
                    context.setVariable("anonymous", "anonymous");
                }
            },
            sleep().milliseconds(500),
            () -> new AbstractTestAction() {
                @Override
                public void doExecute(TestContext context) {
                    log.info(context.getVariable("anonymous"));
                }
            }
        ));

        run(sequential().actions(
            stopTime(),
            sleep().milliseconds(200),
            echo("Hello Citrus"),
            stopTime()
        ));

        run(sequential().actions(
            echo("Hello Citrus"),
            () -> new AbstractTestAction() {
                @Override
                public void doExecute(TestContext context) {
                    context.setVariable("anonymous", "anonymous");
                }
            },
            sleep().milliseconds(200),
            () -> new AbstractTestAction() {
                @Override
                public void doExecute(TestContext context) {
                    log.info(context.getVariable("anonymous"));
                }
            }
        ));
    }
}
