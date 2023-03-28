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

package org.citrusframework.integration.design;

import org.citrusframework.Citrus;
import org.citrusframework.actions.AbstractTestAction;
import org.citrusframework.annotations.*;
import org.citrusframework.context.TestContext;
import org.citrusframework.dsl.design.TestDesigner;
import org.citrusframework.functions.Functions;
import org.citrusframework.dsl.runner.TestRunner;
import org.citrusframework.dsl.testng.TestNGCitrusTest;
import org.citrusframework.endpoint.Endpoint;
import org.citrusframework.jms.config.annotation.JmsEndpointConfig;
import org.citrusframework.testng.CitrusParameters;
import org.testng.Assert;
import org.testng.annotations.*;

/**
 * @author Christoph Deppisch
 * @since 2.5
 */
public class ResourceInjectionJavaIT extends TestNGCitrusTest {

    @CitrusFramework
    private Citrus citrus;

    @CitrusEndpoint
    @JmsEndpointConfig(destinationName = "FOO.test.queue")
    private Endpoint jmsEndpoint;

    @CitrusResource
    private TestContext globalContext;

    @Test
    @Parameters( { "designer", "context" })
    @CitrusTest
    public void injectResourceDesigner(@Optional @CitrusResource TestDesigner testDesigner, @Optional @CitrusResource TestContext context) {
        final String number = Functions.randomNumber(10L, context);
        context.setVariable("message", "Injection worked with test designer!");

        testDesigner.echo("${message}");
        testDesigner.createVariable("random", number);

        testDesigner.action(new AbstractTestAction() {
            @Override
            public void doExecute(TestContext context) {
                Assert.assertEquals(context, globalContext);
                Assert.assertEquals(context.getVariable("random"), number);
            }
        });

        Assert.assertNotNull(citrus);
        Assert.assertNotNull(jmsEndpoint);
        Assert.assertNotNull(globalContext);
        Assert.assertEquals(context, globalContext);
    }

    @Test
    @Parameters( { "runner", "context" })
    @CitrusTest
    public void injectResourceRunner(@Optional @CitrusResource TestRunner testRunner, @Optional @CitrusResource TestContext context) {
        final String number = Functions.randomNumber(10L, context);
        context.setVariable("message", "Injection worked with test runner!");

        testRunner.echo("${message}");
        testRunner.createVariable("random", number);

        testRunner.run(new AbstractTestAction() {
            @Override
            public void doExecute(TestContext context) {
                Assert.assertEquals(context.getVariable("random"), number);
            }
        });

        Assert.assertNotNull(citrus);
        Assert.assertNotNull(jmsEndpoint);
        Assert.assertNotNull(globalContext);
        Assert.assertEquals(context, globalContext);
    }

    @Test(dataProvider = "testData")
    @Parameters( { "data", "designer", "context" })
    @CitrusTest
    public void injectResourceDesignerCombinedWithParameter(String data, @CitrusResource TestDesigner testDesigner, @CitrusResource TestContext context) {
        final String number = Functions.randomNumber(10L, context);
        context.setVariable("message", "Injection worked!");

        testDesigner.echo("${message}");
        testDesigner.echo("${data}");
        testDesigner.createVariable("random", number);

        testDesigner.action(new AbstractTestAction() {
            @Override
            public void doExecute(TestContext context) {
                Assert.assertEquals(context.getVariable("random"), number);
            }
        });

        Assert.assertNotNull(citrus);
        Assert.assertNotNull(jmsEndpoint);
        Assert.assertNotNull(globalContext);
        Assert.assertEquals(context, globalContext);
    }

    @Test(dataProvider = "testDataObjects")
    @CitrusParameters( { "dataContainer", "designer", "context" })
    @CitrusTest
    public void injectResourceDesignerCombinedWithObjectParameter(DataContainer dataContainer, @CitrusResource TestDesigner testDesigner, @CitrusResource TestContext context) {
        final String number = Functions.randomNumber(10L, context);
        context.setVariable("message", "Injection worked!");

        testDesigner.echo("${message}");
        testDesigner.echo("${dataContainer.text}");
        testDesigner.createVariable("random", number);

        testDesigner.action(new AbstractTestAction() {
            @Override
            public void doExecute(TestContext context) {
                Assert.assertEquals(context.getVariable("random"), number);
            }
        });

        Assert.assertNotNull(citrus);
        Assert.assertNotNull(jmsEndpoint);
        Assert.assertNotNull(globalContext);
        Assert.assertEquals(context, globalContext);
    }

    @Test(dataProvider = "testData")
    @Parameters( { "data", "runner", "context" })
    @CitrusTest
    public void injectResourceRunnerCombinedWithParameter(String data, @CitrusResource TestRunner testRunner, @CitrusResource TestContext context) {
        final String number = Functions.randomNumber(10L, context);
        context.setVariable("message", "Injection worked!");

        testRunner.echo("${message}");
        testRunner.echo("${data}");
        testRunner.createVariable("random", number);

        testRunner.run(new AbstractTestAction() {
            @Override
            public void doExecute(TestContext context) {
                Assert.assertEquals(context.getVariable("random"), number);
            }
        });

        Assert.assertNotNull(citrus);
        Assert.assertNotNull(jmsEndpoint);
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
