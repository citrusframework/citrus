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

package org.citrusframework.integration.inject;

import org.citrusframework.annotations.CitrusResource;
import org.citrusframework.annotations.CitrusTest;
import org.citrusframework.context.TestContext;
import org.citrusframework.testng.spring.TestNGCitrusSpringSupport;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import static org.citrusframework.actions.EchoAction.Builder.echo;

/**
 * @author Christoph Deppisch
 * @since 2.5
 */
public class ContextInjectionJavaIT extends TestNGCitrusSpringSupport {

    @CitrusResource
    private TestContext globalContext;

    @Test
    @Parameters("context")
    @CitrusTest
    public void contextInjection(@Optional @CitrusResource TestContext context) {
        context.setVariable("message", "Injection worked!");

        Assert.assertEquals(context, globalContext);
        run(echo("${message}"));
    }

    @Test(dataProvider = "testData")
    @Parameters({ "data", "context" })
    @CitrusTest
    public void contextInjectionCombinedWithParameters(String data, @CitrusResource TestContext context) {
        context.setVariable("message", "Injection worked!");

        Assert.assertEquals(context, globalContext);
        run(echo("${message}"));
        run(echo("${data}"));
    }

    @DataProvider
    public Object[][] testData() {
        return new Object[][] { { "hello", globalContext }, { "bye", globalContext } };
    }

}
