/*
 * Copyright 2006-2017 the original author or authors.
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

package org.citrusframework.citrus.junit.jupiter.integration;

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
import org.citrusframework.citrus.junit.jupiter.CitrusSupport;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.citrusframework.citrus.DefaultTestActionBuilder.action;
import static org.citrusframework.citrus.actions.CreateVariablesAction.Builder.createVariable;
import static org.citrusframework.citrus.actions.EchoAction.Builder.echo;

/**
 * @author Christoph Deppisch
 */
@CitrusSupport
public class ResourceInjectionJUnit5IT {

    @CitrusFramework
    private Citrus citrus;

    @CitrusEndpoint
    @DirectEndpointConfig(queueName = "FOO.test.queue")
    private Endpoint directEndpoint;

    @CitrusResource
    private TestContext globalContext;

    @Test
    @CitrusTest
    public void injectResourceActionRunner(@CitrusResource TestActionRunner runner, @CitrusResource TestContext context) {
        final String number = Functions.randomNumber(10L, context);
        context.setVariable("message", "Injection worked!");

        runner.run(echo("${message}"));
        runner.run(createVariable("random", number));

        runner.run(action(tc -> {
            Assertions.assertEquals(tc, globalContext);
            Assertions.assertEquals(tc.getVariable("random"), number);
        }));

        Assertions.assertNotNull(citrus);
        Assertions.assertNotNull(directEndpoint);
        Assertions.assertNotNull(globalContext);
        Assertions.assertEquals(context, globalContext);
    }

    @Test
    @CitrusTest
    public void injectResourceGherkinRunner(@CitrusResource GherkinTestActionRunner runner, @CitrusResource TestContext context) {
        final String number = Functions.randomNumber(10L, context);
        context.setVariable("message", "Injection worked!");

        runner.given(echo("${message}"));
        runner.when(createVariable("random", number));

        runner.then(action(tc -> {
            Assertions.assertEquals(tc, globalContext);
            Assertions.assertEquals(tc.getVariable("random"), number);
        }));

        Assertions.assertNotNull(citrus);
        Assertions.assertNotNull(directEndpoint);
        Assertions.assertNotNull(globalContext);
        Assertions.assertEquals(context, globalContext);
    }
}
