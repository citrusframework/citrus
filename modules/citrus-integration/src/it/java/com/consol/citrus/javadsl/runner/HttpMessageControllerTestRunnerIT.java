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
import com.consol.citrus.http.message.HttpMessage;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 */
@Test
public class HttpMessageControllerTestRunnerIT extends TestNGCitrusTestRunner {
    
    @CitrusTest(name = "HttpMessageControllerTestRunnerIT")
    public void httpMessageControllerIT() {
        variable("id", "123456789");
        
        echo("First request without query parameter and context path variables.");
        
        parallel().actions(
                http(new BuilderSupport<HttpActionBuilder>() {
                    @Override
                    public void configure(HttpActionBuilder builder) {
                        builder.client("httpClient")
                                .get()
                                .uri("http://localhost:8072")
                                .message(new HttpMessage()
                                        .method(HttpMethod.GET)
                                        .contentType("text/html")
                                        .accept("application/xml;charset=UTF-8"));
                    }
                }),

                sequential().actions(
                        http(new BuilderSupport<HttpActionBuilder>() {
                            @Override
                            public void configure(HttpActionBuilder builder) {
                                builder.server("httpServerRequestEndpoint")
                                        .get()
                                        .message(new HttpMessage()
                                                .method(HttpMethod.GET)
                                                .contentType("text/html")
                                                .header("Host", "localhost:8072")
                                                .accept("application/xml;charset=UTF-8"));
                            }
                        }))
        );
        
        http(new BuilderSupport<HttpActionBuilder>() {
            @Override
            public void configure(HttpActionBuilder builder) {
                builder.client("httpClient")
                        .response(HttpStatus.OK)
                        .timeout(2000L)
                        .version("HTTP/1.1");
            }
        });

        echo("Use context path variables.");
        
        parallel().actions(
            http(new BuilderSupport<HttpActionBuilder>() {
                @Override
                public void configure(HttpActionBuilder builder) {
                    builder.client("httpClient")
                            .get()
                            .uri("http://localhost:8072/test/user/${id}")
                            .message(new HttpMessage()
                                    .method(HttpMethod.GET)
                                    .contentType("text/html")
                                    .accept("application/xml;charset=UTF-8"));
                }
            }),

            sequential().actions(
                http(new BuilderSupport<HttpActionBuilder>() {
                    @Override
                    public void configure(HttpActionBuilder builder) {
                        builder.server("httpServerRequestEndpoint")
                                .get("/test/user/${id}")
                                .message(new HttpMessage()
                                        .contentType("text/html")
                                        .method(HttpMethod.GET)
                                        .header("Host", "localhost:8072")
                                        .accept("application/xml;charset=UTF-8"));
                    }
                }))
        );
        
        http(new BuilderSupport<HttpActionBuilder>() {
            @Override
            public void configure(HttpActionBuilder builder) {
                builder.client("httpClient")
                        .response(HttpStatus.OK)
                        .timeout(2000L)
                        .version("HTTP/1.1");
            }
        });
        
        echo("Use query parameter and context path variables.");
        
        parallel().actions(
            http(new BuilderSupport<HttpActionBuilder>() {
                @Override
                public void configure(HttpActionBuilder builder) {
                    builder.client("httpClient")
                            .get()
                            .uri("http://localhost:8072/test")
                            .message(new HttpMessage()
                                    .method(HttpMethod.GET)
                                    .contentType("text/html")
                                    .queryParam("id", "${id}")
                                    .queryParam("name", "TestUser")
                                    .accept("application/xml;charset=UTF-8"))
                            .path("user");
                }
            }),

            sequential().actions(
                http(new BuilderSupport<HttpActionBuilder>() {
                    @Override
                    public void configure(HttpActionBuilder builder) {
                        builder.server("httpServerRequestEndpoint")
                                .get("/test/user")
                                .message(new HttpMessage()
                                        .method(HttpMethod.GET)
                                        .contentType("text/html")
                                        .header("Host", "localhost:8072")
                                        .accept("application/xml;charset=UTF-8"))
                                .queryParam("id", "${id}")
                                .queryParam("name", "TestUser");
                    }
                })
            )
        );

        http(new BuilderSupport<HttpActionBuilder>() {
            @Override
            public void configure(HttpActionBuilder builder) {
                builder.client("httpClient")
                        .response(HttpStatus.OK)
                        .timeout(2000L)
                        .version("HTTP/1.1");
            }
        });
    }

    @CitrusTest(name = "HttpMessageControllerTestRunnerDeprecatedIT")
    public void httpMessageControllerDeprecatedIT() {
        variable("id", "123456789");

        echo("First request without query parameter and context path variables.");

        parallel().actions(
                send(new BuilderSupport<SendMessageBuilder>() {
                    @Override
                    public void configure(SendMessageBuilder builder) {
                        builder.endpoint("httpClient")
                                .http()
                                .uri("http://localhost:8072")
                                .message(new HttpMessage()
                                        .method(HttpMethod.GET)
                                        .contentType("text/html")
                                        .accept("application/xml;charset=UTF-8"));
                    }
                }),

                sequential().actions(
                        receive(new BuilderSupport<ReceiveMessageBuilder>() {
                            @Override
                            public void configure(ReceiveMessageBuilder builder) {
                                builder.endpoint("httpServerRequestEndpoint")
                                        .message(new HttpMessage()
                                                .method(HttpMethod.GET)
                                                .contentType("text/html")
                                                .header("Host", "localhost:8072")
                                                .accept("application/xml;charset=UTF-8"))
                                        .http().uri("/").contextPath("");
                            }
                        }))
        );

        receive(new BuilderSupport<ReceiveMessageBuilder>() {
            @Override
            public void configure(ReceiveMessageBuilder builder) {
                builder.endpoint("httpClient")
                        .timeout(2000L)
                        .http()
                        .status(HttpStatus.OK)
                        .version("HTTP/1.1");
            }
        });

        echo("Use context path variables.");

        parallel().actions(
                send(new BuilderSupport<SendMessageBuilder>() {
                    @Override
                    public void configure(SendMessageBuilder builder) {
                        builder.endpoint("httpClient")
                                .http()
                                .uri("http://localhost:8072/test/user/${id}")
                                .message(new HttpMessage()
                                        .method(HttpMethod.GET)
                                        .contentType("text/html")
                                        .accept("application/xml;charset=UTF-8"));
                    }
                }),

                sequential().actions(
                        receive(new BuilderSupport<ReceiveMessageBuilder>() {
                            @Override
                            public void configure(ReceiveMessageBuilder builder) {
                                builder.endpoint("httpServerRequestEndpoint")
                                        .http()
                                        .message(new HttpMessage()
                                                .contentType("text/html")
                                                .method(HttpMethod.GET)
                                                .header("Host", "localhost:8072")
                                                .accept("application/xml;charset=UTF-8"))
                                        .uri("/test/user/${id}")
                                        .contextPath("");
                            }
                        }))
        );

        receive(new BuilderSupport<ReceiveMessageBuilder>() {
            @Override
            public void configure(ReceiveMessageBuilder builder) {
                builder.endpoint("httpClient")
                        .timeout(2000L)
                        .http()
                        .status(HttpStatus.OK)
                        .version("HTTP/1.1");
            }
        });

        echo("Use query parameter and context path variables.");

        parallel().actions(
                send(new BuilderSupport<SendMessageBuilder>() {
                    @Override
                    public void configure(SendMessageBuilder builder) {
                        builder.endpoint("httpClient")
                                .http()
                                .uri("http://localhost:8072/test")
                                .message(new HttpMessage()
                                        .method(HttpMethod.GET)
                                        .contentType("text/html")
                                        .queryParam("id", "${id}")
                                        .queryParam("name", "TestUser")
                                        .accept("application/xml;charset=UTF-8"))
                                .path("user");
                    }
                }),

                sequential().actions(
                        receive(new BuilderSupport<ReceiveMessageBuilder>() {
                            @Override
                            public void configure(ReceiveMessageBuilder builder) {
                                builder.endpoint("httpServerRequestEndpoint")
                                        .http()
                                        .message(new HttpMessage()
                                                .method(HttpMethod.GET)
                                                .contentType("text/html")
                                                .header("Host", "localhost:8072")
                                                .accept("application/xml;charset=UTF-8"))
                                        .uri("/test/user")
                                        .contextPath("")
                                        .queryParam("id", "${id}")
                                        .queryParam("name", "TestUser");
                            }
                        })
                )
        );

        receive(new BuilderSupport<ReceiveMessageBuilder>() {
            @Override
            public void configure(ReceiveMessageBuilder builder) {
                builder.endpoint("httpClient")
                        .timeout(2000L)
                        .http()
                        .status(HttpStatus.OK)
                        .version("HTTP/1.1");
            }
        });
    }
}