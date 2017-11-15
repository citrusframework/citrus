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

package com.consol.citrus.junit.jupiter;

import com.consol.citrus.Citrus;
import com.consol.citrus.actions.AbstractTestAction;
import com.consol.citrus.annotations.*;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.dsl.design.TestDesigner;
import com.consol.citrus.dsl.functions.Functions;
import com.consol.citrus.dsl.junit.jupiter.CitrusExtension;
import com.consol.citrus.dsl.runner.TestRunner;
import com.consol.citrus.endpoint.Endpoint;
import com.consol.citrus.jms.config.annotation.JmsEndpointConfig;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * @author Christoph Deppisch
 */
@ExtendWith(CitrusExtension.class)
public class ResourceInjectionJUnit5IT {

    @CitrusFramework
    private Citrus citrus;

    @CitrusEndpoint
    @JmsEndpointConfig(destinationName = "FOO.test.queue")
    private Endpoint jmsEndpoint;

    @Test
    @CitrusTest
    public void injectResourceDesigner(@CitrusResource TestDesigner testDesigner, @CitrusResource TestContext context) {
        final String number = Functions.randomNumber(10L, context);
        context.setVariable("message", "Injection worked!");

        testDesigner.echo("${message}");
        testDesigner.createVariable("random", number);

        testDesigner.action(new AbstractTestAction() {
            @Override
            public void doExecute(TestContext context) {
                Assertions.assertEquals(context.getVariable("random"), number);
            }
        });

        Assertions.assertNotNull(citrus);
        Assertions.assertNotNull(jmsEndpoint);
    }

    @Test
    @CitrusTest
    public void injectResourceRunner(@CitrusResource TestRunner testRunner, @CitrusResource TestContext context) {
        final String number = Functions.randomNumber(10L, context);
        context.setVariable("message", "Injection worked!");

        testRunner.echo("${message}");
        testRunner.createVariable("random", number);

        testRunner.run(new AbstractTestAction() {
            @Override
            public void doExecute(TestContext context) {
                Assertions.assertEquals(context.getVariable("random"), number);
            }
        });

        Assertions.assertNotNull(citrus);
        Assertions.assertNotNull(jmsEndpoint);
    }
}
