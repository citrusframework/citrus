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

import org.citrusframework.Citrus;
import org.citrusframework.TestCaseRunner;
import org.citrusframework.annotations.CitrusEndpoint;
import org.citrusframework.annotations.CitrusFramework;
import org.citrusframework.annotations.CitrusResource;
import org.citrusframework.annotations.CitrusTest;
import org.citrusframework.context.TestContext;
import org.citrusframework.endpoint.Endpoint;
import org.citrusframework.endpoint.direct.annotation.DirectEndpointConfig;
import org.citrusframework.functions.Functions;
import org.citrusframework.testng.CitrusParameters;
import org.citrusframework.testng.spring.TestNGCitrusSpringSupport;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Optional;
import org.testng.annotations.Test;

import static org.citrusframework.DefaultTestActionBuilder.action;
import static org.citrusframework.actions.CreateVariablesAction.Builder.createVariable;
import static org.citrusframework.actions.EchoAction.Builder.echo;

/**
 * @author Christoph Deppisch
 * @since 2.5
 */
public class ResourceInjectionJavaIT extends TestNGCitrusSpringSupport {

    @CitrusFramework
    private Citrus citrus;

    @CitrusEndpoint
    @DirectEndpointConfig(queueName = "FOO.test.queue")
    private Endpoint directEndpoint;

    @CitrusResource
    private TestContext globalContext;

    @Test
    @CitrusTest
    public void injectResources(@Optional @CitrusResource TestCaseRunner runner,
                                @Optional @CitrusResource TestContext context) {
        final String number = Functions.randomNumber(10L, context);
        context.setVariable("message", "Injection worked with test designer!");

        runner.run(echo("${message}"));
        runner.run(createVariable("random", number));

        runner.run(action(tc -> {
            Assert.assertEquals(tc, globalContext);
            Assert.assertEquals(tc.getVariable("random"), number);
        }));

        Assert.assertNotNull(citrus);
        Assert.assertNotNull(directEndpoint);
        Assert.assertNotNull(globalContext);
        Assert.assertEquals(context, globalContext);
    }

    @Test(dataProvider = "testData")
    @CitrusParameters( { "data", "runner", "context" })
    @CitrusTest
    public void injectResourcesCombinedWithParameter(String data,
                                                    @CitrusResource TestCaseRunner runner,
                                                    @CitrusResource TestContext context) {
        final String number = Functions.randomNumber(10L, context);
        context.setVariable("message", "Injection worked!");
        context.setVariable("parameter", data);

        runner.run(echo("${message}"));
        runner.run(echo("${data}"));
        runner.run(echo("${parameter}"));
        runner.run(createVariable("random", number));

        runner.run(action(tc -> {
            Assert.assertEquals(tc, globalContext);
            Assert.assertEquals(tc.getVariable("random"), number);
        }));

        Assert.assertNotNull(citrus);
        Assert.assertNotNull(directEndpoint);
        Assert.assertNotNull(globalContext);
        Assert.assertEquals(context, globalContext);
    }

    @Test(dataProvider = "testDataObjects")
    @CitrusParameters( { "dataContainer", "runner", "context" })
    @CitrusTest
    public void injectResourcesCombinedWithObjectParameter(DataContainer dataContainer,
                                                          @CitrusResource TestCaseRunner runner,
                                                          @CitrusResource TestContext context) {
        final String number = Functions.randomNumber(10L, context);
        context.setVariable("message", "Injection worked!");

        runner.run(echo("${message}"));
        runner.run(echo("${dataContainer.text}"));
        runner.run(createVariable("random", number));

        runner.run(action(tc -> {
            Assert.assertEquals(tc, globalContext);
            Assert.assertEquals(tc.getVariable("random"), number);
        }));

        Assert.assertNotNull(citrus);
        Assert.assertNotNull(directEndpoint);
        Assert.assertNotNull(globalContext);
        Assert.assertEquals(context, globalContext);
    }

    @DataProvider
    public Object[][] testData() {
        return new Object[][] { { "hello", null, null }, { "bye", null, null } };
    }

    @DataProvider
    public Object[][] testDataObjects() {
        return new Object[][] { { new DataContainer("hello"), null, null }, { new DataContainer("bye"), null, null } };
    }

    /**
     * Sample data object holding some fields.
     */
    private static class DataContainer {
        private String text;

        public DataContainer(String text) {
            this.text = text;
        }

        /**
         * Gets the value of the text property.
         *
         * @return the text
         */
        public String getText() {
            return text;
        }
    }
}
