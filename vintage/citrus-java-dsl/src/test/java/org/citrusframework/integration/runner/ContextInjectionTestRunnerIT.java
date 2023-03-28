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

package org.citrusframework.integration.runner;

import org.citrusframework.annotations.CitrusResource;
import org.citrusframework.annotations.CitrusTest;
import org.citrusframework.context.TestContext;
import org.citrusframework.dsl.testng.TestNGCitrusTestRunner;
import org.testng.annotations.*;

/**
 * @author Christoph Deppisch
 * @since 2.5
 */
public class ContextInjectionTestRunnerIT extends TestNGCitrusTestRunner {

    @Test
    @Parameters( { "context" })
    @CitrusTest
    public void contextInjection(@Optional @CitrusResource TestContext context) {
        context.setVariable("message", "Injection worked!");

        echo("${message}");
    }

    @Test(dataProvider = "testData")
    @Parameters({ "data", "context" })
    @CitrusTest
    public void contextInjectionCombinedWithParameters(String data, @CitrusResource TestContext context) {
        context.setVariable("message", "Injection worked!");

        echo("${message}");
        echo("${data}");
    }

    @DataProvider
    public Object[][] testData() {
        return new Object[][] { { "hello", null }, { "bye", null } };
    }
}
