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

package org.citrusframework.camel.integration;

import org.apache.camel.CamelContext;
import org.apache.camel.ServiceStatus;
import org.apache.camel.builder.RouteBuilder;
import org.citrusframework.TestCaseRunner;
import org.citrusframework.annotations.CitrusResource;
import org.citrusframework.annotations.CitrusTest;
import org.citrusframework.camel.dsl.CamelSupport;
import org.citrusframework.dsl.TestActionSupport;
import org.citrusframework.junit.jupiter.spring.CitrusSpringSupport;
import org.citrusframework.message.MessageType;
import org.citrusframework.spring.config.CitrusSpringConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import static org.apache.camel.builder.endpoint.StaticEndpointBuilders.direct;
import static org.apache.camel.builder.endpoint.StaticEndpointBuilders.seda;

@CitrusSpringSupport
@ContextConfiguration(classes = CitrusSpringConfig.class)
public class CamelControlbusIT implements TestActionSupport {

    @Autowired
    private CamelContext camelContext;

    private final CamelSupport camel = new CamelSupport();

    @Test
    @CitrusTest
    public void shouldManageRoutes(@CitrusResource TestCaseRunner runner) throws Exception {
        camelContext.addRoutes(new RouteBuilder() {
            @Override
            public void configure() {
                from(direct("message"))
                    .routeId("message-to-words")
                    .autoStartup(false)
                    .split().tokenize(" ")
                    .to(seda("words"));
            }
        });

        runner.given(camel().camelContext(camelContext)
                .controlBus()
                .route("message-to-words")
                .status()
                .result(ServiceStatus.Stopped));

        runner.when(camel().camelContext(camelContext)
                .controlBus()
                .route("message-to-words")
                .start());

        runner.then(camel().camelContext(camelContext)
                .controlBus()
                .route("message-to-words")
                .status()
                .result(ServiceStatus.Started));

        runner.when(send(camel.endpoint(direct("message")::getRawUri))
                .message()
                .type(MessageType.PLAINTEXT)
                .body("Citrus rocks!"));

        runner.then(receive(camel.endpoint(seda("words")::getRawUri))
                .message()
                .type(MessageType.PLAINTEXT)
                .body("Citrus"));

        runner.then(receive(camel.endpoint(seda("words")::getRawUri))
                .message()
                .type(MessageType.PLAINTEXT)
                .body("rocks!"));
    }
}
