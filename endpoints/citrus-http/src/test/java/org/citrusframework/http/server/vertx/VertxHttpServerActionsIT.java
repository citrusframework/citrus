/*
 * Copyright the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.citrusframework.http.server.vertx;

import static org.springframework.http.HttpStatus.OK;

import org.citrusframework.annotations.CitrusTest;
import org.citrusframework.dsl.TestActionSupport;
import org.citrusframework.http.client.HttpClient;
import org.citrusframework.http.client.HttpClientBuilder;
import org.citrusframework.message.MessageType;
import org.citrusframework.testng.spring.TestNGCitrusSpringSupport;
import org.citrusframework.util.SocketUtils;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Verifies that the regular Http server actions work with the Vert.x based Http server without any
 * change to the action builders.
 */
public class VertxHttpServerActionsIT extends TestNGCitrusSpringSupport implements TestActionSupport {

    private VertxHttpServer vertxServer;
    private HttpClient httpClient;
    private int port;

    @BeforeMethod(alwaysRun = true)
    public void setup() {
        port = SocketUtils.findAvailableTcpPort();

        vertxServer = new VertxHttpServerBuilder()
                .port(port)
                .timeout(5000L)
                .build();

        vertxServer.initialize();
        vertxServer.start();

        httpClient = new HttpClientBuilder()
                .requestUrl("http://localhost:" + port)
                .build();
    }

    @AfterMethod(alwaysRun = true)
    public void teardown() {
        if (vertxServer != null) {
            vertxServer.stop();
            vertxServer.destroy();
            vertxServer = null;
        }
    }

    @Test
    @CitrusTest
    public void shouldReceiveRequestAndSendResponse() {
        when(http().client(httpClient)
                .send()
                .post("/orders/42")
                .message()
                .body("order-42")
                .type(MessageType.PLAINTEXT)
                .fork(true));

        then(http().server(vertxServer)
                .receive()
                .post("/orders/42")
                .message()
                .body("order-42")
                .type(MessageType.PLAINTEXT));

        then(http().server(vertxServer)
                .send()
                .response(OK)
                .message()
                .body("order-accepted")
                .type(MessageType.PLAINTEXT));

        then(http().client(httpClient)
                .receive()
                .response(OK)
                .message()
                .body("order-accepted")
                .type(MessageType.PLAINTEXT));
    }
}
