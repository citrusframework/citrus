/*
 * Copyright 2023 the original author or authors.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.citrusframework.integration.dsl;

import java.io.IOException;

import org.citrusframework.TestActionRunner;
import org.citrusframework.annotations.CitrusResource;
import org.citrusframework.annotations.CitrusTest;
import org.citrusframework.context.TestContext;
import org.citrusframework.endpoint.direct.DirectEndpoint;
import org.citrusframework.groovy.dsl.configuration.ConfigurationScript;
import org.citrusframework.message.MessageQueue;
import org.citrusframework.spi.Resources;
import org.citrusframework.testng.spring.TestNGCitrusSpringSupport;
import org.citrusframework.util.FileUtils;
import org.testng.Assert;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

public class ConfigurationScriptIT extends TestNGCitrusSpringSupport {

    @Test
    @Parameters({"runner", "context"})
    @CitrusTest
    public void shouldRunConfigurationScript(@Optional @CitrusResource TestActionRunner runner,
                                       @Optional @CitrusResource TestContext context) throws IOException {
        ConfigurationScript script = new ConfigurationScript(FileUtils.readToString(Resources.fromClasspath("org/citrusframework/groovy/dsl/configuration.groovy")), citrus);
        script.execute(context);

        Assert.assertTrue(context.getReferenceResolver().isResolvable("say-hello", MessageQueue.class));
        Assert.assertTrue(context.getReferenceResolver().isResolvable("say-goodbye", MessageQueue.class));
        Assert.assertTrue(context.getReferenceResolver().isResolvable("hello", DirectEndpoint.class));
        Assert.assertTrue(context.getReferenceResolver().isResolvable("goodbye", DirectEndpoint.class));

        DirectEndpoint hello = context.getReferenceResolver().resolve("hello", DirectEndpoint.class);
        Assert.assertEquals(hello.getName(), "hello");
        Assert.assertEquals(hello.getEndpointConfiguration().getQueueName(), "say-hello");

        DirectEndpoint goodbye = context.getReferenceResolver().resolve("goodbye", DirectEndpoint.class);
        Assert.assertEquals(goodbye.getName(), "goodbye");
        Assert.assertEquals(goodbye.getEndpointConfiguration().getQueueName(), "say-goodbye");
    }
}
