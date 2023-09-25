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

package org.citrusframework.integration.container;

import org.citrusframework.annotations.CitrusTest;
import org.citrusframework.testng.spring.TestNGCitrusSpringSupport;
import org.testng.annotations.Test;

import static org.citrusframework.DefaultTestActionBuilder.action;
import static org.citrusframework.actions.EchoAction.Builder.echo;
import static org.citrusframework.actions.SleepAction.Builder.sleep;
import static org.citrusframework.actions.StopTimeAction.Builder.stopTime;
import static org.citrusframework.container.Sequence.Builder.sequential;

/**
 * @author Christoph Deppisch
 */
@Test
public class SequentialJavaIT extends TestNGCitrusSpringSupport {

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
            action(context -> context.setVariable("anonymous", "anonymous")),
            sleep().milliseconds(500),
            action(context -> logger.info(context.getVariable("anonymous")))
        ));

        run(sequential().actions(
            stopTime(),
            sleep().milliseconds(200),
            echo("Hello Citrus"),
            stopTime()
        ));

        run(sequential().actions(
            echo("Hello Citrus"),
            action(context -> context.setVariable("anonymous", "anonymous")),
            sleep().milliseconds(200),
            action(context -> logger.info(context.getVariable("anonymous")))
        ));
    }
}
