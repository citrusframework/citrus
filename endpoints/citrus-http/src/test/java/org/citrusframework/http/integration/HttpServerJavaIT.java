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

package org.citrusframework.http.integration;

import org.citrusframework.GherkinTestActionRunner;
import org.citrusframework.annotations.CitrusResource;
import org.citrusframework.dsl.TestActionSupport;
import org.citrusframework.annotations.CitrusTest;
import org.citrusframework.junit.jupiter.spring.CitrusSpringSupport;
import org.citrusframework.spring.config.CitrusSpringConfig;
import org.springframework.http.HttpStatus;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ContextConfiguration;
import org.junit.jupiter.api.parallel.Isolated;

@CitrusSpringSupport
@ContextConfiguration(classes = CitrusSpringConfig.class)
@Isolated("HTTP ITs share singleton endpoints and Citrus Spring context state under parallel classes")
public class HttpServerJavaIT implements TestActionSupport {

    @Test
    @CitrusTest
    public void httpServer(@CitrusResource GherkinTestActionRunner runner) {
        runner.given(createVariable("custom_header_id", "123456789"));

        runner.run(echo("Send Http message and respond with 200 OK"));

        runner.when(parallel().actions(
            http().client("httpClient")
                .send()
                .post()
                .message()
                .body("<testRequestMessage>" +
                        "<text>Hello HttpServer</text>" +
                        "</testRequestMessage>")
                .header("CustomHeaderId", "${custom_header_id}")
                .contentType("application/xml")
                .accept("application/xml"),

            sequential().actions(
                http().server("httpServerRequestEndpoint")
                    .receive()
                    .post("/test")
                    .message()
                    .body("<testRequestMessage>" +
                                "<text>Hello HttpServer</text>" +
                            "</testRequestMessage>")
                    .header("CustomHeaderId", "${custom_header_id}")
                    .header("contentType", "application/xml")
                    .accept("application/xml")
                    .header("Authorization", "Basic c29tZVVzZXJuYW1lOnNvbWVQYXNzd29yZA==")
                    .extract(extractor().fromHeaders()
                                .header("citrus_jms_messageId", "correlation_id")),

               http().server("httpServerResponseEndpoint")
                   .send()
                   .response(HttpStatus.OK.value())
                   .message()
                   .body("<testResponseMessage>" +
                                "<text>Hello Citrus</text>" +
                            "</testResponseMessage>")
                    .header("CustomHeaderId", "${custom_header_id}")
                    .version("HTTP/1.1")
                    .contentType("application/xml")
                    .header("citrus_jms_correlationId", "${correlation_id}")
            )
        ));

        runner.then(http().client("httpClient")
            .receive()
            .response(HttpStatus.OK)
            .message()
            .body("<testResponseMessage>" +
                    "<text>Hello Citrus</text>" +
                    "</testResponseMessage>")
            .header("CustomHeaderId", "${custom_header_id}")
            .version("HTTP/1.1"));

        runner.run(echo("Send Http request and respond with 404 status code"));

        runner.when(parallel().actions(
            http().client("httpClient")
                .send()
                .post()
                .message()
                .body("<testRequestMessage>" +
                                "<text>Hello HttpServer</text>" +
                            "</testRequestMessage>")
                .header("CustomHeaderId", "${custom_header_id}")
                .contentType("application/xml")
                .accept("application/xml"),

            sequential().actions(
                http().server("httpServerRequestEndpoint")
                    .receive()
                    .post()
                    .path("/test")
                    .message()
                    .body("<testRequestMessage>" +
                                "<text>Hello HttpServer</text>" +
                            "</testRequestMessage>")
                    .header("CustomHeaderId", "${custom_header_id}")
                    .header("contentType","application/xml")
                    .accept("application/xml")
                    .header("Authorization", "Basic c29tZVVzZXJuYW1lOnNvbWVQYXNzd29yZA==")
                    .extract(extractor().fromHeaders()
                                .header("citrus_jms_messageId", "correlation_id")),

               http().server("httpServerResponseEndpoint")
                   .send()
                   .response()
                   .message()
                   .status(HttpStatus.NOT_FOUND)
                   .body("<testResponseMessage>" +
                                "<text>Hello Citrus</text>" +
                            "</testResponseMessage>")
                    .header("CustomHeaderId", "${custom_header_id}")
                    .version("HTTP/1.1")
                    .contentType("application/xml")
                    .header("citrus_jms_correlationId", "${correlation_id}")
            )
        ));

        runner.then(http().client("httpClient")
            .receive()
            .response()
            .message()
            .status(HttpStatus.NOT_FOUND)
            .body("<testResponseMessage>" +
                        "<text>Hello Citrus</text>" +
                    "</testResponseMessage>")
            .header("CustomHeaderId", "${custom_header_id}")
            .version("HTTP/1.1"));

        runner.run(echo("Skip response and use fallback endpoint adapter"));

        runner.when(http().client("httpClient")
            .send()
            .post()
            .message()
            .body("<testRequestMessage>" +
                            "<text>Hello HttpServer</text>" +
                        "</testRequestMessage>")
            .header("CustomHeaderId", "${custom_header_id}")
            .contentType("application/xml")
            .accept("application/xml"));

        runner.then(http().client("httpClient")
            .receive()
            .response(HttpStatus.OK)
            .message()
            .version("HTTP/1.1")
            .timeout(2000L));

    }
}
