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

import org.citrusframework.annotations.CitrusEndpoint;
import org.citrusframework.annotations.CitrusTest;
import org.citrusframework.dsl.TestActionSupport;
import org.citrusframework.http.client.HttpClient;
import org.citrusframework.http.client.HttpClientBuilder;
import org.citrusframework.http.config.annotation.VertxHttpServerConfig;
import org.citrusframework.http.server.vertx.VertxHttpServer;
import org.citrusframework.message.MessageType;
import org.citrusframework.testng.spring.TestNGCitrusSpringSupport;
import org.springframework.http.HttpStatus;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

/**
 * Creates the Vert.x based Http server with the annotation configuration and runs a
 * request against it.
 */
public class VertxHttpServerAnnotationIT extends TestNGCitrusSpringSupport implements TestActionSupport {

    private static final int PORT = 19305;

    @CitrusEndpoint
    @VertxHttpServerConfig(autoStart = true,
            port = PORT,
            timeout = 5000L)
    private VertxHttpServer vertxHttpServer;

    @AfterMethod(alwaysRun = true)
    public void stopVertxHttpServer() {
        if (vertxHttpServer != null && vertxHttpServer.isRunning()) {
            vertxHttpServer.stop();
        }
    }

    @Test
    @CitrusTest
    public void shouldCreateVertxServerWithAnnotation() {
        Assert.assertTrue(vertxHttpServer.isRunning());

        HttpClient httpClient = new HttpClientBuilder()
                .requestUrl("http://localhost:" + PORT)
                .build();

        when(http().client(httpClient)
                .send()
                .get("/orders/42")
                .fork(true));

        then(http().server(vertxHttpServer)
                .receive()
                .get("/orders/42"));

        then(http().server(vertxHttpServer)
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
