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
import com.consol.citrus.message.MessageType;
import org.springframework.http.HttpStatus;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 */
@Test
public class JsonTextValidationTestRunnerIT extends TestNGCitrusTestRunner {
    
    @CitrusTest
    public void jsonTextValidation() {
        parallel().actions(
            http(new BuilderSupport<HttpActionBuilder>() {
                @Override
                public void configure(HttpActionBuilder builder) {
                    builder.client("httpClient")
                            .post()
                            .payload("{" +
                                    "\"type\" : \"read\"," +
                                    "\"mbean\" : \"java.lang:type=Memory\"," +
                                    "\"attribute\" : \"HeapMemoryUsage\"," +
                                    "\"path\" : \"used\"" +
                                    "}");
                }
            }),
            sequential().actions(
                http(new BuilderSupport<HttpActionBuilder>() {
                    @Override
                    public void configure(HttpActionBuilder builder) {
                        builder.server("httpServerRequestEndpoint")
                                .post()
                                .messageType(MessageType.JSON)
                                .payload("{" +
                                        "\"type\" : \"read\"," +
                                        "\"mbean\" : \"java.lang:type=Memory\"," +
                                        "\"attribute\" : \"HeapMemoryUsage\"," +
                                        "\"path\" : \"@equalsIgnoreCase('USED')@\"" +
                                        "}")
                                .extractFromHeader("citrus_jms_messageId", "correlation_id");
                    }
                }),
                http(new BuilderSupport<HttpActionBuilder>() {
                    @Override
                    public void configure(HttpActionBuilder builder) {
                        builder.server("httpServerResponseEndpoint")
                                .respond(HttpStatus.OK)
                                .payload("{" +
                                        "\"timestamp\" : \"2011-01-01\"," +
                                        "\"status\" : 200," +
                                        "\"request\" : " +
                                        "{" +
                                        "\"mbean\" : \"java.lang:type=Memory\"," +
                                        "\"path\" : \"used\"," +
                                        "\"attribute\" : \"HeapMemoryUsage\"," +
                                        "\"type\" : \"read\"" +
                                        "}," +
                                        "\"value\" : 512" +
                                        "}")
                                .version("HTTP/1.1")
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
                        .messageType(MessageType.JSON)
                        .payload("{" +
                                "\"timestamp\" : \"@matchesDatePattern('yyyy-MM-dd')@\"," +
                                "\"status\" : 200," +
                                "\"request\" : " +
                                "{" +
                                "\"mbean\" : \"java.lang:type=Memory\"," +
                                "\"path\" : \"@matches('u*s*e*d*')@\"," +
                                "\"attribute\" : \"HeapMemoryUsage\"," +
                                "\"type\" : \"read\"" +
                                "}," +
                                "\"value\" : \"@isNumber()@\"" +
                                "}")
                        .version("HTTP/1.1");
            }
        });

        http(new BuilderSupport<HttpActionBuilder>() {
            @Override
            public void configure(HttpActionBuilder builder) {
                builder.client("httpClient")
                        .post()
                        .payload("{" +
                                "\"type\" : \"read\"," +
                                "\"mbean\" : \"java.lang:type=Memory\"," +
                                "\"attribute\" : \"HeapMemoryUsage\"," +
                                "\"path\" : \"used\"" +
                                "}");
            }
        });
        
        sleep(2000);
        
        http(new BuilderSupport<HttpActionBuilder>() {
            @Override
            public void configure(HttpActionBuilder builder) {
                builder.client("httpClient")
                        .response(HttpStatus.OK)
                        .messageType(MessageType.JSON)
                        .version("HTTP/1.1");
            }
        });
    }
}