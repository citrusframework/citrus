/*
 * Copyright 2006-2015 the original author or authors.
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

import org.citrusframework.annotations.CitrusTest;
import org.citrusframework.message.MessageType;
import org.citrusframework.testng.spring.TestNGCitrusSpringSupport;
import org.apache.camel.CamelContext;
import org.apache.camel.ServiceStatus;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.Test;

import static org.citrusframework.actions.ReceiveMessageAction.Builder.receive;
import static org.citrusframework.actions.SendMessageAction.Builder.send;
import static org.citrusframework.camel.dsl.CamelSupport.camel;
import static org.apache.camel.builder.endpoint.StaticEndpointBuilders.direct;
import static org.apache.camel.builder.endpoint.StaticEndpointBuilders.seda;

/**
 * @author Christoph Deppisch
 */
public class CamelControlbusIT extends TestNGCitrusSpringSupport {

    @Autowired
    private CamelContext camelContext;

    @Test
    @CitrusTest
    public void shouldManageRoutes() throws Exception {
        camelContext.addRoutes(new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from(direct("message"))
                    .routeId("message-to-words")
                    .autoStartup(false)
                    .split().tokenize(" ")
                    .to(seda("words"));
            }
        });

        given(camel().camelContext(camelContext)
                .controlBus()
                .route("message-to-words")
                .status()
                .result(ServiceStatus.Stopped));

        when(camel().camelContext(camelContext)
                .controlBus()
                .route("message-to-words")
                .start());

        then(camel().camelContext(camelContext)
                .controlBus()
                .route("message-to-words")
                .status()
                .result(ServiceStatus.Started));

        when(send(camel().endpoint(direct("message")::getUri))
                .message()
                .type(MessageType.PLAINTEXT)
                .body("Citrus rocks!"));

        then(receive(camel().endpoint(seda("words")::getUri))
                .message()
                .type(MessageType.PLAINTEXT)
                .body("Citrus"));

        then(receive(camel().endpoint(seda("words")::getUri))
                .message()
                .type(MessageType.PLAINTEXT)
                .body("rocks!"));
    }
}
