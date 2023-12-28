/*
 * Copyright 2006-2024 the original author or authors.
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

import org.citrusframework.annotations.CitrusTest;
import org.citrusframework.http.client.HttpClient;
import org.citrusframework.http.client.HttpClientBuilder;
import org.citrusframework.http.server.HttpServer;
import org.citrusframework.http.server.HttpServerBuilder;
import org.citrusframework.spi.BindToRegistry;
import org.citrusframework.testng.spring.TestNGCitrusSpringSupport;
import org.springframework.http.HttpStatus;
import org.testng.annotations.Test;

import static org.citrusframework.http.actions.HttpActionBuilder.http;
import static org.citrusframework.http.security.HttpAuthentication.basic;
import static org.citrusframework.util.SocketUtils.findAvailableTcpPort;

/**
 * @author Christoph Deppisch
 */
@Test
public class HttpBasicAuthJavaIT extends TestNGCitrusSpringSupport {

    private final int port = findAvailableTcpPort(8888);

    @BindToRegistry
    private final HttpServer basicAuthServer = new HttpServerBuilder()
            .port(port)
            .autoStart(true)
            .defaultStatus(HttpStatus.NO_CONTENT)
            .authentication("/secured/*", basic("citrus", "secr3t"))
            .build();

    @BindToRegistry
    private final HttpClient basicAuthClient = new HttpClientBuilder()
            .requestUrl("http://localhost:%d".formatted(port))
            .authentication(basic("citrus", "secr3t"))
            .build();

    @CitrusTest
    public void basicAuth() {
        when(http().client(basicAuthClient)
                .send()
                .post("/secured")
                .fork(true)
                .message()
                .body("Hello from auth user")
                .contentType("text/plain"));

        then(http().server(basicAuthServer)
                .receive()
                .post("/secured")
                .message()
                .body("Hello from auth user")
                .contentType("text/plain"));

        then(http().server(basicAuthServer)
                .send()
                .response(HttpStatus.OK)
                .message()
                .body("Hi from secured Http server")
                .contentType("text/plain"));

        then(http().client(basicAuthClient)
                .receive()
                .response(HttpStatus.OK)
                .message()
                .body("Hi from secured Http server")
                .contentType("text/plain"));
    }

    @CitrusTest
    public void basicAuthPathUnprotected() {
        when(http().client("http://localhost:%d".formatted(port))
                .send()
                .get("/other"));

        then(http().client("http://localhost:%d".formatted(port))
                .receive()
                .response(HttpStatus.NO_CONTENT)
                .message());
    }

    @CitrusTest
    public void basicAuthHeader() {
        when(http().client("http://localhost:%d".formatted(port))
                .send()
                .get("/secured")
                .message()
                .header("Authorization", "Basic citrus:encodeBase64(citrus:secr3t)"));

        then(http().client("http://localhost:%d".formatted(port))
                .receive()
                .response(HttpStatus.NO_CONTENT)
                .message());
    }

    @CitrusTest
    public void basicAuthError() {
        when(http().client("http://localhost:%d".formatted(port))
                .send()
                .get("/secured"));

        then(http().client("http://localhost:%d".formatted(port))
                .receive()
                .response(HttpStatus.UNAUTHORIZED)
                .message());
    }
}
