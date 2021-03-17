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

package com.consol.citrus.junit.jupiter.integration;

import com.consol.citrus.Citrus;
import com.consol.citrus.GherkinTestActionRunner;
import com.consol.citrus.TestActionRunner;
import com.consol.citrus.annotations.CitrusEndpoint;
import com.consol.citrus.annotations.CitrusFramework;
import com.consol.citrus.annotations.CitrusResource;
import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.endpoint.Endpoint;
import com.consol.citrus.endpoint.direct.annotation.DirectEndpointConfig;
import com.consol.citrus.functions.Functions;
import com.consol.citrus.junit.jupiter.CitrusSupport;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static com.consol.citrus.DefaultTestActionBuilder.action;
import static com.consol.citrus.actions.CreateVariablesAction.Builder.createVariable;
import static com.consol.citrus.actions.EchoAction.Builder.echo;

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
