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
import org.citrusframework.TestCaseRunner;
import org.citrusframework.annotations.CitrusResource;
import org.citrusframework.annotations.CitrusTest;
import org.citrusframework.camel.dsl.CamelSupport;
import org.citrusframework.camel.message.CamelRouteProcessor;
import org.citrusframework.dsl.TestActionSupport;
import org.citrusframework.junit.jupiter.spring.CitrusSpringSupport;
import org.citrusframework.message.MessageType;
import org.citrusframework.spring.config.CitrusSpringConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import static org.apache.camel.builder.Builder.constant;
import static org.apache.camel.builder.Builder.jsonpath;
import static org.apache.camel.builder.Builder.simple;
import static org.apache.camel.builder.endpoint.StaticEndpointBuilders.seda;

@CitrusSpringSupport
@ContextConfiguration(classes = CitrusSpringConfig.class)
public class CamelRouteProcessorIT implements TestActionSupport {

    @Autowired
    private CamelContext camelContext;

    private final CamelSupport camel = new CamelSupport();

    @Test
    @CitrusTest
    public void shouldProcessRouteOnSend(@CitrusResource TestCaseRunner runner) {
        CamelRouteProcessor.Builder beforeSend = camel.camelContext(camelContext).route()
                .processor()
                .setBody(simple("{" +
                        "\"greeting\": {" +
                            "\"language\": \"${body}\"" +
                        "}" +
                    "}"));

        runner.when(send(camel.endpoint(seda("greetings")::getRawUri))
                .process(beforeSend)
                .message()
                .body("EN")
        );

        runner.then(receive("camel:" + camel.endpoints().seda("greetings").getRawUri())
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
    public void shouldProcessRouteOnReceive(@CitrusResource TestCaseRunner runner) {
        CamelRouteProcessor.Builder beforeReceive = camel.camelContext(camelContext).route(route ->
                route.choice()
                    .when(jsonpath("$.greeting[?(@.language == 'EN')]"))
                        .setBody(constant("Hello!"))
                    .when(jsonpath("$.greeting[?(@.language == 'DE')]"))
                        .setBody(constant("Hallo!"))
                    .otherwise()
                        .setBody(constant("Hi!")));

        runner.given(createVariable("lang", "EN"));

        runner.when(send(camel.endpoint(seda("greetings")::getRawUri))
                .message()
                .body("{" +
                        "\"greeting\": {" +
                            "\"language\": \"${lang}\"" +
                        "}" +
                      "}")
        );

        runner.then(receive("camel:" + camel.endpoints().seda("greetings").getRawUri())
                .process(beforeReceive)
                .message()
                .type(MessageType.PLAINTEXT)
                .body("Hello!"));

        runner.given(createVariable("lang", "DE"));

        runner.when(send(camel.endpoint(seda("greetings")::getRawUri))
                .message()
                .body("{" +
                        "\"greeting\": {" +
                            "\"language\": \"${lang}\"" +
                        "}" +
                    "}")
        );

        runner.then(receive("camel:" + camel.endpoints().seda("greetings").getRawUri())
                .process(beforeReceive)
                .message()
                .type(MessageType.PLAINTEXT)
                .body("Hallo!"));
    }
}
