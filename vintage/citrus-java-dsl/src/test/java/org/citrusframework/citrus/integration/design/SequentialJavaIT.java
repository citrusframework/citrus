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

package org.citrusframework.citrus.integration.design;

import org.citrusframework.citrus.actions.AbstractTestAction;
import org.citrusframework.citrus.context.TestContext;
import org.citrusframework.citrus.dsl.testng.TestNGCitrusTestDesigner;
import org.citrusframework.citrus.annotations.CitrusTest;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 */
@Test
public class SequentialJavaIT extends TestNGCitrusTestDesigner {

    @CitrusTest
    public void sequentialContainer() {
        sequential().actions(
            stopTime(),
            sleep(500),
            echo("Hello Citrus"),
            stopTime()
        );

        sequential().actions(
            echo("Hello Citrus"),
            () -> new AbstractTestAction() {
                @Override
                public void doExecute(TestContext context) {
                    context.setVariable("anonymous", "anonymous");
                }
            },
            sleep(500),
            () -> new AbstractTestAction() {
                @Override
                public void doExecute(TestContext context) {
                    log.info(context.getVariable("anonymous"));
                }
            }
        );

        sequential().actions(
            stopTime(),
            sleep(200),
            echo("Hello Citrus"),
            stopTime()
        );

        sequential().actions(
            echo("Hello Citrus"),
            () -> new AbstractTestAction() {
                @Override
                public void doExecute(TestContext context) {
                    context.setVariable("anonymous", "anonymous");
                }
            },
            sleep(200),
            () -> new AbstractTestAction() {
                @Override
                public void doExecute(TestContext context) {
                    log.info(context.getVariable("anonymous"));
                }
            }
        );
    }
}
