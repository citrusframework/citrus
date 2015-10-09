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

package com.consol.citrus.javadsl.runner;

import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.dsl.builder.*;
import com.consol.citrus.dsl.testng.TestNGCitrusTestRunner;
import org.springframework.http.HttpStatus;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 */
@Test
public class XMLValidationMatcherTestRunnerIT extends TestNGCitrusTestRunner {
    
    @CitrusTest
    public void xmlValidationMatcher() {
        variable("greetingText", "Hello Citrus");      
        
        parallel().actions(
            http(new BuilderSupport<HttpActionBuilder>() {
                @Override
                public void configure(HttpActionBuilder builder) {
                    builder.client("httpClient")
                            .post()
                            .payload("<testRequestMessage>" +
                                    "<text>citrus:cdataSection('<data>" +
                                    "<greeting>Hello Citrus</greeting>" +
                                    "<timestamp>2012-07-01T00:00:00</timestamp>" +
                                    "</data>')</text>" +
                                    "</testRequestMessage>")
                            .contentType("text/xml")
                            .accept("text/xml, */*");
                }
            }),
            sequential().actions(
                http(new BuilderSupport<HttpActionBuilder>() {
                    @Override
                    public void configure(HttpActionBuilder builder) {
                        builder.server("httpServerRequestEndpoint")
                                .post("/test")
                                .payload("<testRequestMessage>" +
                                        "<text>citrus:cdataSection('@matchesXml('<data>" +
                                        "<greeting>${greetingText}</greeting>" +
                                        "<timestamp>@ignore@</timestamp>" +
                                        "</data>')@')</text>" +
                                        "</testRequestMessage>")
                                .contentType("text/xml")
                                .accept("text/xml, */*")
                                .header("Authorization", "Basic c29tZVVzZXJuYW1lOnNvbWVQYXNzd29yZA==")
                                .extractFromHeader("citrus_jms_messageId", "correlation_id");
                    }
                }),
                http(new BuilderSupport<HttpActionBuilder>() {
                    @Override
                    public void configure(HttpActionBuilder builder) {
                        builder.server("httpServerResponseEndpoint")
                                .respond(HttpStatus.OK)
                                .payload("<testResponseMessage>" +
                                        "<text>Hello Citrus</text>" +
                                        "</testResponseMessage>")
                                .version("HTTP/1.1")
                                .contentType("text/xml")
                                .header("citrus_jms_correlationId", "${correlation_id}");
                    }
                })
            )
        );
        
        http(new BuilderSupport<HttpActionBuilder>() {
            @Override
            public void configure(HttpActionBuilder builder) {
                builder.client("httpClient")
                        .response(HttpStatus.OK)
                        .payload("<testResponseMessage>" +
                                "<text>Hello Citrus</text>" +
                                "</testResponseMessage>")
                        .version("HTTP/1.1");
            }
        });
    }
}