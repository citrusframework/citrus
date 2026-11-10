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
import org.citrusframework.http.client.HttpClient;
import org.citrusframework.http.client.HttpClientBuilder;
import org.citrusframework.http.server.HttpServer;
import org.citrusframework.http.server.HttpServerBuilder;
import org.citrusframework.junit.jupiter.spring.CitrusSpringSupport;
import org.citrusframework.spi.BindToRegistry;
import org.citrusframework.spring.config.CitrusSpringConfig;
import org.springframework.http.HttpStatus;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import org.springframework.test.context.ContextConfiguration;
import static org.citrusframework.http.security.HttpAuthentication.basic;
import static org.citrusframework.util.SocketUtils.findAvailableTcpPort;

@CitrusSpringSupport
@ContextConfiguration(classes = CitrusSpringConfig.class)
@Isolated("HTTP ITs share singleton endpoints and Citrus Spring context state under parallel classes")
public class HttpBasicAuthJavaIT implements TestActionSupport {

    private static final int port = findAvailableTcpPort(8888);

    @BindToRegistry
    private static final HttpServer basicAuthServer = new HttpServerBuilder()
            .port(port)
            .autoStart(true)
            .defaultStatus(HttpStatus.NO_CONTENT)
            .authentication("/secured/*", basic("citrus", "secr3t"))
            .build();

    @BindToRegistry
    private static final HttpClient basicAuthClient = new HttpClientBuilder()
            .requestUrl("http://localhost:%d".formatted(port))
            .authentication(basic("citrus", "secr3t"))
            .build();

    @AfterAll
    static void stopBasicAuthServer() {
        basicAuthServer.stop();
    }

    @Test
    @CitrusTest
    public void basicAuth(@CitrusResource GherkinTestActionRunner runner) {
        runner.when(http().client(basicAuthClient)
                .send()
                .post("/secured")
                .fork(true)
                .message()
                .body("Hello from auth user")
                .contentType("text/plain"));

        runner.then(http().server(basicAuthServer)
                .receive()
                .post("/secured")
                .message()
                .body("Hello from auth user")
                .contentType("text/plain"));

        runner.then(http().server(basicAuthServer)
                .send()
                .response(HttpStatus.OK)
                .message()
                .body("Hi from secured Http server")
                .contentType("text/plain"));

        runner.then(http().client(basicAuthClient)
                .receive()
                .response(HttpStatus.OK)
                .message()
                .body("Hi from secured Http server")
                .contentType("@startsWith(text/plain)@"));
    }

    @Test
    @CitrusTest
    public void basicAuthPathUnprotected(@CitrusResource GherkinTestActionRunner runner) {
        runner.when(http().client("http://localhost:%d".formatted(port))
                .send()
                .get("/other"));

        runner.then(http().client("http://localhost:%d".formatted(port))
                .receive()
                .response(HttpStatus.NO_CONTENT)
                .message());
    }

    @Test
    @CitrusTest
    public void basicAuthHeader(@CitrusResource GherkinTestActionRunner runner) {
        runner.when(http().client("http://localhost:%d".formatted(port))
                .send()
                .get("/secured")
                .message()
                .header("Authorization", "Basic citrus:encodeBase64(citrus:secr3t)"));

        runner.then(http().client("http://localhost:%d".formatted(port))
                .receive()
                .response(HttpStatus.NO_CONTENT)
                .message());
    }

    @Test
    @CitrusTest
    public void basicAuthError(@CitrusResource GherkinTestActionRunner runner) {
        runner.when(http().client("http://localhost:%d".formatted(port))
                .send()
                .get("/secured"));

        runner.then(http().client("http://localhost:%d".formatted(port))
                .receive()
                .response(HttpStatus.UNAUTHORIZED)
                .message());
    }
}
