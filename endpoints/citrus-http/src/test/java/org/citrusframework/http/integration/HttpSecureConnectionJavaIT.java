/*
 * Copyright 2006-2023 the original author or authors.
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
import org.citrusframework.http.security.HttpSecureConnection;
import org.citrusframework.http.server.HttpServer;
import org.citrusframework.http.server.HttpServerBuilder;
import org.citrusframework.spi.BindToRegistry;
import org.citrusframework.spi.Resource;
import org.citrusframework.spi.Resources;
import org.citrusframework.testng.spring.TestNGCitrusSpringSupport;
import org.citrusframework.util.SocketUtils;
import org.springframework.http.HttpStatus;
import org.testng.annotations.Test;

import static org.citrusframework.http.actions.HttpActionBuilder.http;

/**
 * @author Christoph Deppisch
 */
@Test
public class HttpSecureConnectionJavaIT extends TestNGCitrusSpringSupport {

    private final Resource keyStore = Resources.create("http-server.jks", HttpSecureConnectionJavaIT.class);

    @BindToRegistry
    private final HttpServer secureHttpServer = new HttpServerBuilder()
            .port(SocketUtils.findAvailableTcpPort(8888))
            .timeout(5000L)
            .autoStart(true)
            .secured(8443, HttpSecureConnection.ssl()
                    .keyStore(keyStore, "secret"))
            .build();

    @BindToRegistry
    private final HttpClient secureHttpClient = new HttpClientBuilder()
            .requestUrl("https://localhost:8443")
            .secured(HttpSecureConnection.ssl()
                    .trustStore(keyStore, "secret"))
            .build();

    @CitrusTest
    public void secureHttpConnection() {
        when(http().client(secureHttpClient)
                .send()
                .post("/secured")
                .fork(true)
                .message()
                .body("Hello from secure Http client")
                .contentType("text/plain"));

        then(http().server(secureHttpServer)
                .receive()
                .post("/secured")
                .message()
                .body("Hello from secure Http client")
                .contentType("text/plain"));

        then(http().server(secureHttpServer)
                .send()
                .response(HttpStatus.OK)
                .message()
                .body("Hi from secured Http server")
                .contentType("text/plain"));

        then(http().client(secureHttpClient)
                .receive()
                .response(HttpStatus.OK)
                .message()
                .body("Hi from secured Http server")
                .contentType("text/plain"));
    }
}
