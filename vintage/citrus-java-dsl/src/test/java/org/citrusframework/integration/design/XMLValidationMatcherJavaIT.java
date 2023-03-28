/*
 * Copyright 2006-2013 the original author or authors.
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

package org.citrusframework.integration.design;

import org.citrusframework.dsl.testng.TestNGCitrusTestDesigner;
import org.citrusframework.annotations.CitrusTest;
import org.springframework.http.HttpStatus;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 */
@Test
public class XMLValidationMatcherJavaIT extends TestNGCitrusTestDesigner {

    @CitrusTest
    public void xmlValidationMatcher() {
        variable("greetingText", "Hello Citrus");

        parallel().actions(
            http().client("httpClient")
                .send()
                .post()
                .payload("<testRequestMessage>" +
                            "<text>citrus:cdataSection('<data>" +
                              "<greeting>Hello Citrus</greeting>" +
                              "<timestamp>2012-07-01T00:00:00</timestamp>" +
                            "</data>')</text>" +
                        "</testRequestMessage>")
                .contentType("application/xml")
                .accept("application/xml"),
            sequential().actions(
                http().server("httpServerRequestEndpoint")
                    .receive()
                    .post("/test")
                    .payload("<testRequestMessage>" +
                                    "<text>citrus:cdataSection('@matchesXml('<data>" +
                                  "<greeting>${greetingText}</greeting>" +
                                  "<timestamp>@ignore@</timestamp>" +
                                "</data>')@')</text>" +
                                "</testRequestMessage>")
                    .header("contentType", "application/xml")
                    .accept("application/xml")
                    .header("Authorization", "Basic c29tZVVzZXJuYW1lOnNvbWVQYXNzd29yZA==")
                    .extractFromHeader("citrus_jms_messageId", "correlation_id"),
                http().server("httpServerResponseEndpoint")
                    .send()
                    .response(HttpStatus.OK)
                    .payload("<testResponseMessage>" +
                                    "<text>Hello Citrus</text>" +
                                "</testResponseMessage>")
                    .version("HTTP/1.1")
                    .contentType("application/xml")
                    .header("citrus_jms_correlationId", "${correlation_id}")
            )
        );

        http().client("httpClient")
            .receive()
            .response(HttpStatus.OK)
            .payload("<testResponseMessage>" +
                        "<text>Hello Citrus</text>" +
                    "</testResponseMessage>")
             .version("HTTP/1.1");
    }
}
