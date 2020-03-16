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

package com.consol.citrus.integration.inject;

import com.consol.citrus.Citrus;
import com.consol.citrus.TestCaseRunner;
import com.consol.citrus.actions.AbstractTestAction;
import com.consol.citrus.annotations.CitrusEndpoint;
import com.consol.citrus.annotations.CitrusFramework;
import com.consol.citrus.annotations.CitrusResource;
import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.endpoint.Endpoint;
import com.consol.citrus.endpoint.direct.annotation.DirectEndpointConfig;
import com.consol.citrus.functions.Functions;
import com.consol.citrus.testng.CitrusParameters;
import com.consol.citrus.testng.TestNGCitrusSupport;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import static com.consol.citrus.actions.CreateVariablesAction.Builder.createVariable;
import static com.consol.citrus.actions.EchoAction.Builder.echo;

/**
 * @author Christoph Deppisch
 * @since 2.5
 */
public class ResourceInjectionJavaIT extends TestNGCitrusSupport {

    @CitrusFramework
    private Citrus citrus;

    @CitrusEndpoint
    @DirectEndpointConfig(queueName = "FOO.test.queue")
    private Endpoint directEndpoint;

    @Test
    @Parameters( { "designer", "context" })
    @CitrusTest
    public void injectResources(@Optional @CitrusResource TestCaseRunner runner,
                                       @Optional @CitrusResource TestContext context) {
        final String number = Functions.randomNumber(10L, context);
        context.setVariable("message", "Injection worked with test designer!");

        runner.run(echo("${message}"));
        runner.run(createVariable("random", number));

        runner.run(new AbstractTestAction() {
            @Override
            public void doExecute(TestContext context) {
                Assert.assertEquals(context.getVariable("random"), number);
            }
        });

        Assert.assertNotNull(citrus);
        Assert.assertNotNull(directEndpoint);
    }

    @Test(dataProvider = "testData")
    @Parameters( { "data", "designer", "context" })
    @CitrusTest
    public void injectResourcesCombinedWithParameter(String data,
                                                            @CitrusResource TestCaseRunner runner,
                                                            @CitrusResource TestContext context) {
        final String number = Functions.randomNumber(10L, context);
        context.setVariable("message", "Injection worked!");

        runner.run(echo("${message}"));
        runner.run(echo("${data}"));
        runner.run(createVariable("random", number));

        runner.run(new AbstractTestAction() {
            @Override
            public void doExecute(TestContext context) {
                Assert.assertEquals(context.getVariable("random"), number);
            }
        });

        Assert.assertNotNull(citrus);
        Assert.assertNotNull(directEndpoint);
    }

    @Test(dataProvider = "testDataObjects")
    @CitrusParameters( { "dataContainer", "designer", "context" })
    @CitrusTest
    public void injectResourcesCombinedWithObjectParameter(DataContainer dataContainer,
                                                                  @CitrusResource TestCaseRunner runner,
                                                                  @CitrusResource TestContext context) {
        final String number = Functions.randomNumber(10L, context);
        context.setVariable("message", "Injection worked!");

        runner.run(echo("${message}"));
        runner.run(echo("${dataContainer.text}"));
        runner.run(createVariable("random", number));

        runner.run(new AbstractTestAction() {
            @Override
            public void doExecute(TestContext context) {
                Assert.assertEquals(context.getVariable("random"), number);
            }
        });

        Assert.assertNotNull(citrus);
        Assert.assertNotNull(directEndpoint);
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
