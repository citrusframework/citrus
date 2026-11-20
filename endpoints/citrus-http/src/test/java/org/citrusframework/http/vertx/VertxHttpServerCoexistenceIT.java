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

package org.citrusframework.http.vertx;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import org.citrusframework.http.server.HttpServer;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertTrue;

/**
 * Jetty and Vert.x engines coexist in one JVM on distinct ports (AC7): both serve, stopping one
 * leaves the other intact, and neither changes the other's behavior.
 */
public class VertxHttpServerCoexistenceIT {

    private HttpServer jetty;
    private VertxHttpServer vertx;

    @AfterMethod(alwaysRun = true)
    public void stopAll() {
        if (vertx != null) {
            vertx.stop();
            vertx.destroy();
            vertx = null;
        }
        if (jetty != null) {
            jetty.stop();
            jetty.destroy();
            jetty = null;
        }
    }

    @Test
    public void jettyAndVertxServeSideBySide() throws Exception {
        jetty = new HttpServer();
        jetty.setName("coexistJetty-" + System.nanoTime());
        jetty.setPort(18093);
        jetty.setDefaultTimeout(500L);
        jetty.initialize();
        jetty.start();

        vertx = new VertxHttpServer();
        vertx.setName("coexistVertx-" + System.nanoTime());
        vertx.setPort(0);
        vertx.setReplyTimeoutMs(500L);
        vertx.initialize();
        vertx.start();

        int jettyPort = jetty.getPort();
        int vertxPort = vertx.getActualPort();
        assertEquals(jettyPort, 18093);
        assertTrue(vertxPort > 0);
        assertNotEquals(jettyPort, vertxPort);

        assertEquals(get(jettyPort, "/jetty").statusCode(), 200);
        assertEquals(get(vertxPort, "/vertx").statusCode(), 200);
    }

    @Test
    public void stoppingVertxLeavesJettyIntact() throws Exception {
        jetty = new HttpServer();
        jetty.setName("coexistJetty2-" + System.nanoTime());
        jetty.setPort(18094);
        jetty.setDefaultTimeout(500L);
        jetty.initialize();
        jetty.start();

        vertx = new VertxHttpServer();
        vertx.setName("coexistVertx2-" + System.nanoTime());
        vertx.setPort(0);
        vertx.setReplyTimeoutMs(500L);
        vertx.initialize();
        vertx.start();

        vertx.stop();
        vertx.destroy();
        vertx = null;

        assertTrue(jetty.isRunning());
        assertEquals(get(jetty.getPort(), "/jetty").statusCode(), 200);
    }

    private static HttpResponse<String> get(int port, String path) throws Exception {
        HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://127.0.0.1:" + port + path))
                .timeout(Duration.ofSeconds(15))
                .GET()
                .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }
}
