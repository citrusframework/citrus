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
import org.citrusframework.http.message.HttpMessage;
import org.citrusframework.junit.jupiter.spring.CitrusSpringSupport;
import org.citrusframework.spring.config.CitrusSpringConfig;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ContextConfiguration;
import org.junit.jupiter.api.parallel.Isolated;

@CitrusSpringSupport
@ContextConfiguration(classes = CitrusSpringConfig.class)
@Isolated("HTTP ITs share singleton endpoints and Citrus Spring context state under parallel classes")
public class HttpMessageControllerJavaIT implements TestActionSupport {

    @Test
    @CitrusTest(name = "HttpMessageControllerJavaIT")
    public void httpMessageControllerIT(@CitrusResource GherkinTestActionRunner runner) {
        runner.given(createVariable("id", "123456789"));

        runner.run(echo("First request without query parameter and context path variables."));

        runner.when(parallel().actions(
            http().client("httpClient")
                .send()
                .get()
                .uri("http://localhost:11082")
                .message(new HttpMessage()
                    .method(HttpMethod.GET)
                    .contentType("text/html")
                    .accept("application/xml;charset=UTF-8")),

            sequential().actions(
                http().server("httpServerRequestEndpoint")
                    .receive()
                    .get()
                    .message(new HttpMessage()
                        .method(HttpMethod.GET)
                        .header("contentType", "text/html")
                        .header("Host", "localhost:11082")
                        .accept("application/xml;charset=UTF-8"))
            )
        ));

        runner.then(http().client("httpClient")
            .receive()
            .response(HttpStatus.OK)
            .timeout(2000L)
            .message()
            .version("HTTP/1.1"));

        runner.run(echo("Use context path variables."));

        runner.when(parallel().actions(
            http().client("httpClient")
                .send()
                .get()
                .uri("http://localhost:11082/test/user/${id}")
                .message(new HttpMessage()
                    .method(HttpMethod.GET)
                    .contentType("text/html")
                    .accept("application/xml;charset=UTF-8")),

            sequential().actions(
                http().server("httpServerRequestEndpoint")
                    .receive()
                    .get("/test/user/${id}")
                    .message(new HttpMessage()
                        .header("contentType","text/html")
                        .method(HttpMethod.GET)
                        .header("Host", "localhost:11082")
                        .accept("application/xml;charset=UTF-8"))
            )
        ));

        runner.then(http().client("httpClient")
            .receive()
            .response(HttpStatus.OK)
            .timeout(2000L)
            .message()
            .version("HTTP/1.1"));

        runner.run(echo("Use query parameter and context path variables."));

        runner.when(parallel().actions(
            http().client("httpClient")
                .send()
                .get()
                .uri("http://localhost:11082/test")
                .message(new HttpMessage()
                    .method(HttpMethod.GET)
                    .contentType("text/html")
                    .queryParam("id", "${id}")
                    .queryParam("name", "TestUser")
                    .queryParam("alive")
                    .accept("application/xml;charset=UTF-8")
                    .path("user")),

            sequential().actions(
                http().server("httpServerRequestEndpoint")
                    .receive()
                    .get("/test/user")
                    .message(new HttpMessage()
                        .method(HttpMethod.GET)
                        .header("contentType","text/html")
                        .header("Host", "localhost:11082")
                        .accept("application/xml;charset=UTF-8")
                        .queryParam("name", "@ignore@")
                        .queryParam("id", "${id}")
                        .queryParam("alive"))
            )
        ));

        runner.then(http().client("httpClient")
            .receive()
            .response(HttpStatus.OK)
            .timeout(2000L)
            .message()
            .version("HTTP/1.1"));

        runner.run(echo("Query WSDL with special query param"));

        runner.when(parallel().actions(
                http().client("httpClient")
                        .send()
                        .get()
                        .queryParam("wsdl")
                        .message()
                        .contentType("text/html")
                        .accept("application/xml;charset=UTF-8"),

                sequential().actions(
                        http().server("httpServerRequestEndpoint")
                                .receive()
                                .get()
                                .message()
                                .header("contentType","text/html")
                                .header("Host", "localhost:11082")
                                .accept("application/xml;charset=UTF-8")
                                .queryParam("wsdl"))
        ));

        runner.then(http().client("httpClient")
                .receive()
                .response(HttpStatus.OK)
                .timeout(2000L)
                .message()
                .version("HTTP/1.1"));
    }
}
