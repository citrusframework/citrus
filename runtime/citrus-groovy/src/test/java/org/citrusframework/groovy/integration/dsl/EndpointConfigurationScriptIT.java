/*
 * Copyright the original author or authors.
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

package org.citrusframework.groovy.integration.dsl;

import java.io.IOException;

import org.citrusframework.Citrus;
import org.citrusframework.TestActionRunner;
import org.citrusframework.annotations.CitrusFramework;
import org.citrusframework.annotations.CitrusResource;
import org.citrusframework.annotations.CitrusTest;
import org.citrusframework.context.TestContext;
import org.citrusframework.endpoint.direct.DirectEndpoint;
import org.citrusframework.groovy.dsl.configuration.endpoints.EndpointConfigurationScript;
import org.citrusframework.message.DefaultMessageQueue;
import org.citrusframework.message.MessageQueue;
import org.citrusframework.spi.Resources;
import org.citrusframework.junit.jupiter.spring.CitrusSpringSupport;
import org.citrusframework.spring.config.CitrusSpringConfig;
import org.citrusframework.util.FileUtils;
import org.junit.jupiter.api.Assertions;

import org.junit.jupiter.api.Test;
import org.springframework.test.context.ContextConfiguration;

@CitrusSpringSupport
@ContextConfiguration(classes = CitrusSpringConfig.class)
public class EndpointConfigurationScriptIT {

    @CitrusFramework
    private Citrus citrus;

    @Test
    @CitrusTest
    public void shouldLoadEndpointsFromScript(@CitrusResource TestActionRunner runner,
                                       @CitrusResource TestContext context) throws IOException {
        context.getReferenceResolver().bind("say-hello", new DefaultMessageQueue("say-hello"));
        context.getReferenceResolver().bind("say-goodbye", new DefaultMessageQueue("say-goodbye"));

        EndpointConfigurationScript script = new EndpointConfigurationScript(FileUtils.readToString(Resources.fromClasspath("org/citrusframework/groovy/dsl/endpoints.groovy")), citrus);
        script.execute(context);

        Assertions.assertTrue(context.getReferenceResolver().isResolvable("say-hello", MessageQueue.class));
        Assertions.assertTrue(context.getReferenceResolver().isResolvable("say-goodbye", MessageQueue.class));
        Assertions.assertTrue(context.getReferenceResolver().isResolvable("hello", DirectEndpoint.class));
        Assertions.assertTrue(context.getReferenceResolver().isResolvable("goodbye", DirectEndpoint.class));

        DirectEndpoint hello = context.getReferenceResolver().resolve("hello", DirectEndpoint.class);
        Assertions.assertEquals("hello", hello.getName());
        Assertions.assertEquals("say-hello", hello.getEndpointConfiguration().getQueueName());

        DirectEndpoint goodbye = context.getReferenceResolver().resolve("goodbye", DirectEndpoint.class);
        Assertions.assertEquals("goodbye", goodbye.getName());
        Assertions.assertEquals("say-goodbye", goodbye.getEndpointConfiguration().getQueueName());
    }

    @Test
    @CitrusTest
    public void shouldLoadEndpointFromScript(@CitrusResource TestActionRunner runner,
                                              @CitrusResource TestContext context) throws IOException {
        context.getReferenceResolver().bind("say-hello", new DefaultMessageQueue("say-hello"));

        EndpointConfigurationScript script = new EndpointConfigurationScript(FileUtils.readToString(Resources.fromClasspath("org/citrusframework/groovy/dsl/endpoint.groovy")), citrus);
        script.execute(context);

        Assertions.assertTrue(context.getReferenceResolver().isResolvable("say-hello", MessageQueue.class));
        Assertions.assertTrue(context.getReferenceResolver().isResolvable("hello", DirectEndpoint.class));

        DirectEndpoint hello = context.getReferenceResolver().resolve("hello", DirectEndpoint.class);
        Assertions.assertEquals("hello", hello.getName());
        Assertions.assertEquals("say-hello", hello.getEndpointConfiguration().getQueueName());
    }
}
