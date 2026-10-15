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

import org.citrusframework.exceptions.CitrusRuntimeException;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.expectThrows;

/**
 * Lifecycle contract of the Vert.x HTTP server prototype (AC6): no socket before start,
 * bind on start, failed bind, idempotent stop, destroy after stop, generations with fresh
 * adapter state and port reuse, bounded termination.
 */
public class VertxHttpServerLifecycleIT {

    private static final int FIXED_PORT = 18091;
    private static final int OTHER_FIXED_PORT = 18092;

    @Test
    public void constructAndInitializeBindNoSocket() throws Exception {
        VertxHttpServer server = newServer(FIXED_PORT, 500L);
        server.initialize();

        assertFalse(server.isRunning());
        assertEquals(server.getActualPort(), -1);
        assertConnectionRefused(FIXED_PORT);

        server.destroy();
    }

    @Test
    public void startBindsPortAndServesWithDefaultStatus() throws Exception {
        VertxHttpServer server = newServer(0, 500L);
        server.initialize();
        server.start();
        try {
            assertTrue(server.isRunning());
            assertTrue(server.getActualPort() > 0);

            HttpResponse<String> response = get(server.getActualPort(), "/ping");
            assertEquals(response.statusCode(), 200);
            assertEquals(response.body(), "");
        } finally {
            server.stop();
            server.destroy();
        }
    }

    @Test
    public void failedBindLeavesServerStopped() {
        VertxHttpServer first = newServer(OTHER_FIXED_PORT, 500L);
        first.initialize();
        first.start();

        VertxHttpServer second = newServer(OTHER_FIXED_PORT, 500L);
        second.initialize();
        try {
            expectThrows(CitrusRuntimeException.class, second::start);
            assertFalse(second.isRunning());
            assertEquals(second.getActualPort(), -1);
        } finally {
            second.destroy();
            first.stop();
            first.destroy();
        }
    }

    @Test
    public void repeatedStopIsSafe() {
        VertxHttpServer server = newServer(0, 500L);
        server.initialize();
        server.start();
        assertTrue(server.isRunning());

        server.stop();
        assertFalse(server.isRunning());
        server.stop();
        assertFalse(server.isRunning());

        server.destroy();
        server.destroy();
    }

    @Test
    public void restartReusesPortWithFreshGenerationState() throws Exception {
        VertxHttpServer server = newServer(0, 500L);
        server.initialize();

        server.start();
        var firstAdapter = server.getEndpointAdapter();
        assertNotNull(firstAdapter);
        assertEquals(server.getGeneration(), 1L);
        int firstPort = server.getActualPort();
        server.stop();

        server.start();
        try {
            assertEquals(server.getGeneration(), 2L);
            assertTrue(server.getActualPort() > 0);
            assertNotEquals(server.getEndpointAdapter(), firstAdapter);

            HttpResponse<String> response = get(server.getActualPort(), "/ping");
            assertEquals(response.statusCode(), 200);
        } finally {
            server.stop();
            server.destroy();
        }
        assertTrue(firstPort > 0);
    }

    @Test
    public void fixedPortIsReusableAcrossGenerations() throws Exception {
        VertxHttpServer server = newServer(FIXED_PORT, 500L);
        server.initialize();

        for (int generation = 1; generation <= 3; generation++) {
            server.start();
            try {
                assertEquals(server.getGeneration(), (long) generation);
                assertEquals(server.getActualPort(), FIXED_PORT);
                assertEquals(get(FIXED_PORT, "/ping").statusCode(), 200);
            } finally {
                server.stop();
            }
        }
        server.destroy();
        assertConnectionRefused(FIXED_PORT);
    }

    private static VertxHttpServer newServer(int port, long replyTimeoutMs) {
        VertxHttpServer server = new VertxHttpServer();
        server.setName("vertxLifecycle-" + port + "-" + System.nanoTime());
        server.setPort(port);
        server.setReplyTimeoutMs(replyTimeoutMs);
        return server;
    }

    private static HttpResponse<String> get(int port, String path) throws Exception {
        HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://127.0.0.1:" + port + path))
                .timeout(Duration.ofSeconds(10))
                .GET()
                .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private static void assertConnectionRefused(int port) {
        HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(2)).build();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://127.0.0.1:" + port + "/ping"))
                .timeout(Duration.ofSeconds(2))
                .GET()
                .build();
        try {
            client.send(request, HttpResponse.BodyHandlers.discarding());
            throw new AssertionError("Expected connection refused on port " + port);
        } catch (Exception e) {
            assertTrue(e instanceof java.net.ConnectException || e.getCause() instanceof java.net.ConnectException,
                    "Expected ConnectException but was: " + e);
        }
    }
}
