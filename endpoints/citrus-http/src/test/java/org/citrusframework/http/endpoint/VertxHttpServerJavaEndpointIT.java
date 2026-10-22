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
package org.citrusframework.http.endpoint;

import org.citrusframework.annotations.CitrusTest;
import org.citrusframework.dsl.TestActionSupport;
import org.citrusframework.http.client.HttpClient;
import org.citrusframework.http.client.HttpClientBuilder;
import org.citrusframework.http.endpoint.builder.HttpEndpoints;
import org.citrusframework.http.server.vertx.VertxHttpServer;
import org.citrusframework.http.server.vertx.VertxHttpServerBuilder;
import org.citrusframework.message.MessageType;
import org.citrusframework.testng.spring.TestNGCitrusSpringSupport;
import org.citrusframework.util.SocketUtils;
import org.springframework.http.HttpStatus;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Creates the Vert.x based Http server with the Java endpoint builder and runs a
 * request against it.
 */
public class VertxHttpServerJavaEndpointIT extends TestNGCitrusSpringSupport implements TestActionSupport {

    @Test
    @CitrusTest
    public void shouldCreateVertxServerWithJavaBuilder() {
        int port = SocketUtils.findAvailableTcpPort();

        VertxHttpServerBuilder builder = HttpEndpoints.http().vertxServer();
        builder.port(port);
        builder.autoStart(true);
        builder.name("vertxHttpServer");

        VertxHttpServer vertxServer = builder.initialize().build();
        Assert.assertTrue(vertxServer.isRunning());

        HttpClient httpClient = new HttpClientBuilder()
                .requestUrl("http://localhost:" + port)
                .build();

        when(http().client(httpClient)
                .send()
                .get("/orders/42")
                .fork(true));

        then(http().server(vertxServer)
                .receive()
                .get("/orders/42"));

        then(http().server(vertxServer)
                .send()
                .response(HttpStatus.OK)
                .message()
                .contentType("text/plain")
                .type(MessageType.PLAINTEXT)
                .body("Vert.x server response"));

        then(http().client(httpClient)
                .receive()
                .response(HttpStatus.OK));
    }
}
