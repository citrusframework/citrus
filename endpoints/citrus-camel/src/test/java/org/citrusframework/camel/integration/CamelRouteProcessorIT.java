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
import org.citrusframework.camel.message.CamelRouteProcessor;
import org.citrusframework.message.MessageType;
import org.citrusframework.testng.spring.TestNGCitrusSpringSupport;
import org.apache.camel.CamelContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.Test;

import static org.citrusframework.actions.CreateVariablesAction.Builder.createVariable;
import static org.citrusframework.actions.ReceiveMessageAction.Builder.receive;
import static org.citrusframework.actions.SendMessageAction.Builder.send;
import static org.citrusframework.camel.dsl.CamelSupport.camel;
import static org.apache.camel.builder.Builder.constant;
import static org.apache.camel.builder.Builder.jsonpath;
import static org.apache.camel.builder.Builder.simple;
import static org.apache.camel.builder.endpoint.StaticEndpointBuilders.seda;

/**
 * @author Christoph Deppisch
 */
public class CamelRouteProcessorIT extends TestNGCitrusSpringSupport {

    @Autowired
    private CamelContext camelContext;

    @Test
    @CitrusTest
    public void shouldProcessRouteOnSend() {
        CamelRouteProcessor.Builder beforeSend = camel(camelContext).route()
                .processor()
                .setBody(simple("{" +
                        "\"greeting\": {" +
                            "\"language\": \"${body}\"" +
                        "}" +
                    "}"));

        when(send(camel().endpoint(seda("greetings")::getUri))
                .process(beforeSend)
                .message()
                .body("EN")
        );

        then(receive("camel:" + camel().endpoints().seda("greetings").getUri())
                .message()
                .type(MessageType.PLAINTEXT)
                .body("{" +
                        "\"greeting\": {" +
                            "\"language\": \"EN\"" +
                        "}" +
                    "}"));
    }

    @Test
    @CitrusTest
    public void shouldProcessRouteOnReceive() {
        CamelRouteProcessor.Builder beforeReceive = camel(camelContext).route(route ->
                route.choice()
                    .when(jsonpath("$.greeting[?(@.language == 'EN')]"))
                        .setBody(constant("Hello!"))
                    .when(jsonpath("$.greeting[?(@.language == 'DE')]"))
                        .setBody(constant("Hallo!"))
                    .otherwise()
                        .setBody(constant("Hi!")));

        given(createVariable("lang", "EN"));

        when(send(camel().endpoint(seda("greetings")::getUri))
                .message()
                .body("{" +
                        "\"greeting\": {" +
                            "\"language\": \"${lang}\"" +
                        "}" +
                      "}")
        );

        then(receive("camel:" + camel().endpoints().seda("greetings").getUri())
                .process(beforeReceive)
                .message()
                .type(MessageType.PLAINTEXT)
                .body("Hello!"));

        given(createVariable("lang", "DE"));

        when(send(camel().endpoint(seda("greetings")::getUri))
                .message()
                .body("{" +
                        "\"greeting\": {" +
                            "\"language\": \"${lang}\"" +
                        "}" +
                    "}")
        );

        then(receive("camel:" + camel().endpoints().seda("greetings").getUri())
                .process(beforeReceive)
                .message()
                .type(MessageType.PLAINTEXT)
                .body("Hallo!"));
    }
}
