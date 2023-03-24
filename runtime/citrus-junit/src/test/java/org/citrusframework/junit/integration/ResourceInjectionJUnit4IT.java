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

package org.citrusframework.junit.integration;

import org.citrusframework.Citrus;
import org.citrusframework.GherkinTestActionRunner;
import org.citrusframework.TestActionRunner;
import org.citrusframework.annotations.CitrusEndpoint;
import org.citrusframework.annotations.CitrusFramework;
import org.citrusframework.annotations.CitrusResource;
import org.citrusframework.annotations.CitrusTest;
import org.citrusframework.context.TestContext;
import org.citrusframework.endpoint.Endpoint;
import org.citrusframework.endpoint.direct.annotation.DirectEndpointConfig;
import org.citrusframework.functions.Functions;
import org.citrusframework.junit.spring.JUnit4CitrusSpringSupport;
import org.junit.Assert;
import org.junit.Test;

import static org.citrusframework.DefaultTestActionBuilder.action;
import static org.citrusframework.actions.CreateVariablesAction.Builder.createVariable;
import static org.citrusframework.actions.EchoAction.Builder.echo;

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
