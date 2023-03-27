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

package org.citrusframework.citrus.junit.integration;

import org.citrusframework.citrus.Citrus;
import org.citrusframework.citrus.GherkinTestActionRunner;
import org.citrusframework.citrus.TestActionRunner;
import org.citrusframework.citrus.annotations.CitrusEndpoint;
import org.citrusframework.citrus.annotations.CitrusFramework;
import org.citrusframework.citrus.annotations.CitrusResource;
import org.citrusframework.citrus.annotations.CitrusTest;
import org.citrusframework.citrus.context.TestContext;
import org.citrusframework.citrus.endpoint.Endpoint;
import org.citrusframework.citrus.endpoint.direct.annotation.DirectEndpointConfig;
import org.citrusframework.citrus.functions.Functions;
import org.citrusframework.citrus.junit.spring.JUnit4CitrusSpringSupport;
import org.junit.Assert;
import org.junit.Test;

import static org.citrusframework.citrus.DefaultTestActionBuilder.action;
import static org.citrusframework.citrus.actions.CreateVariablesAction.Builder.createVariable;
import static org.citrusframework.citrus.actions.EchoAction.Builder.echo;

/**
 * @author Christoph Deppisch
 * @since 2.5
 */
public class ResourceInjectionJUnit4IT extends JUnit4CitrusSpringSupport {

    @CitrusFramework
    private Citrus citrus;

    @CitrusEndpoint
    @DirectEndpointConfig(queueName = "FOO.test.queue")
    private Endpoint directEndpoint;

    @CitrusResource
    private TestContext globalContext;

    @Test
    @CitrusTest
    public void injectActionRunner(@CitrusResource TestActionRunner runner, @CitrusResource TestContext context) {
        final String number = Functions.randomNumber(10L, context);
        context.setVariable("message", "Injection worked!");
        runner.run(echo("${message}"));
        runner.run(createVariable("random", number));

        runner.run(action(tc -> {
            Assert.assertEquals(globalContext, tc);
            Assert.assertEquals(tc.getVariable("random"), number);
        }));

        Assert.assertNotNull(citrus);
        Assert.assertNotNull(directEndpoint);
        Assert.assertNotNull(globalContext);
        Assert.assertEquals(globalContext, context);
    }

    @Test
    @CitrusTest
    public void injectGherkinActionRunner(@CitrusResource GherkinTestActionRunner runner, @CitrusResource TestContext context) {
        final String number = Functions.randomNumber(10L, context);
        context.setVariable("message", "Injection worked!");

        runner.run(echo("${message}"));
        runner.run(createVariable("random", number));

        runner.run(action(tc -> {
            Assert.assertEquals(globalContext, tc);
            Assert.assertEquals(tc.getVariable("random"), number);
        }));

        Assert.assertNotNull(citrus);
        Assert.assertNotNull(directEndpoint);
        Assert.assertNotNull(globalContext);
        Assert.assertEquals(globalContext, context);
    }
}
