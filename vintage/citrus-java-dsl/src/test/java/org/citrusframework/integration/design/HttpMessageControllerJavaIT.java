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
import org.citrusframework.http.message.HttpMessage;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 */
@Test
public class HttpMessageControllerJavaIT extends TestNGCitrusTestDesigner {

    @CitrusTest(name = "HttpMessageControllerJavaIT")
    public void httpMessageControllerIT() {
        variable("id", "123456789");

        echo("First request without query parameter and context path variables.");

        parallel().actions(
            http().client("httpClient")
                .send()
                .get()
                .uri("http://localhost:8072")
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
                        .header("Host", "localhost:8072")
                        .accept("application/xml;charset=UTF-8"))
            )
        );

        http().client("httpClient")
            .receive()
            .response(HttpStatus.OK)
            .timeout(2000L)
            .version("HTTP/1.1");

        echo("Use context path variables.");

        parallel().actions(
            http().client("httpClient")
                .send()
                .get()
                .uri("http://localhost:8072/test/user/${id}")
                .message(new HttpMessage()
                    .method(HttpMethod.GET)
                    .contentType("text/html")
                    .accept("application/xml;charset=UTF-8")),

            sequential().actions(
                http().server("httpServerRequestEndpoint")
                    .receive()
                    .get("/test/user/${id}")
                    .message(new HttpMessage()
                        .header("contentType", "text/html")
                        .method(HttpMethod.GET)
                        .header("Host", "localhost:8072")
                        .accept("application/xml;charset=UTF-8"))
            )
        );

        http().client("httpClient")
            .receive()
            .response(HttpStatus.OK)
            .timeout(2000L)
            .version("HTTP/1.1");

        echo("Use query parameter and context path variables.");

        parallel().actions(
            http().client("httpClient")
                .send()
                .get()
                .uri("http://localhost:8072/test")
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
                        .header("contentType", "text/html")
                        .header("Host", "localhost:8072")
                        .accept("application/xml;charset=UTF-8")
                        .queryParam("name", "@ignore@")
                        .queryParam("id", "${id}")
                        .queryParam("alive"))
            )
        );

        http().client("httpClient")
            .receive()
            .response(HttpStatus.OK)
            .timeout(2000L)
            .version("HTTP/1.1");

        echo("Query WSDL with special query param");

        parallel().actions(
                http().client("httpClient")
                        .send()
                        .get()
                        .queryParam("wsdl")
                        .contentType("text/html")
                        .accept("application/xml;charset=UTF-8"),

                sequential().actions(
                        http().server("httpServerRequestEndpoint")
                                .receive()
                                .get()
                                .header("contentType", "text/html")
                                .header("Host", "localhost:8072")
                                .accept("application/xml;charset=UTF-8")
                                .queryParam("wsdl"))
        );

        http().client("httpClient")
                .receive()
                .response(HttpStatus.OK)
                .timeout(2000L)
                .version("HTTP/1.1");
    }
}
