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

import java.net.Socket;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.citrusframework.http.message.HttpMessage;
import org.springframework.http.HttpStatus;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * Concurrency and failure isolation of the Vert.x prototype (AC5): concurrent exchanges with
 * reverse-order replies, adapter code never on the event loop, exactly one response per request,
 * adapter exceptions, client disconnects and admission saturation.
 */
public class VertxHttpServerConcurrencyIT {

    private VertxHttpServer server;

    @AfterMethod(alwaysRun = true)
    public void stopServer() {
        if (server != null) {
            server.stop();
            server.destroy();
            server = null;
        }
    }

    @Test
    public void concurrentExchangesReplyInReverseOrder() throws Exception {
        VertxPrototypeAdapters.Gated gated = new VertxPrototypeAdapters.Gated(request -> {
            HttpMessage reply = new HttpMessage(request.getPayload(String.class));
            reply.status(HttpStatus.OK);
            return reply;
        });
        gated.expect("first");
        gated.expect("second");

        server = startWith(gated, VertxHttpServerOptions.defaults());
        int port = server.getActualPort();

        CompletableFuture<HttpResponse<String>> first = postAsync(port, "first");
        assertTrue(gated.awaitArrival("first", 10_000L));
        CompletableFuture<HttpResponse<String>> second = postAsync(port, "second");
        assertTrue(gated.awaitArrival("second", 10_000L));

        gated.release("second");
        assertEquals(second.get(10, TimeUnit.SECONDS).statusCode(), 200);
        assertEquals(second.get(10, TimeUnit.SECONDS).body(), "second");

        gated.release("first");
        assertEquals(first.get(10, TimeUnit.SECONDS).statusCode(), 200);
        assertEquals(first.get(10, TimeUnit.SECONDS).body(), "first");
    }

    @Test
    public void adapterNeverRunsOnEventLoop() throws Exception {
        VertxPrototypeAdapters.Recording recording = new VertxPrototypeAdapters.Recording(request -> {
            HttpMessage reply = new HttpMessage("ok");
            reply.status(HttpStatus.OK);
            return reply;
        });
        server = startWith(recording, VertxHttpServerOptions.defaults());

        HttpResponse<String> response = post(server.getActualPort(), "ping");
        assertEquals(response.statusCode(), 200);
        assertEquals(recording.getInvocations(), 1);

        List<String> threads = recording.getThreads();
        assertEquals(threads.size(), 1);
        assertTrue(threads.get(0).contains("exchange"),
                "Adapter ran on unexpected thread: " + threads.get(0));
        assertTrue(!threads.get(0).startsWith("vert.x-eventloop"),
                "Adapter must never run on a Vert.x event loop thread: " + threads.get(0));
    }

    @Test
    public void adapterExceptionYields500WithoutStacktrace() throws Exception {
        server = startWith(VertxPrototypeAdapters.throwing(new IllegalStateException("boom-secret-marker")),
                VertxHttpServerOptions.defaults());

        HttpResponse<String> response = post(server.getActualPort(), "ping");
        assertEquals(response.statusCode(), 500);
        assertEquals(response.body(), "");
        assertTrue(!response.body().contains("boom-secret-marker"));
    }

    @Test
    public void clientDisconnectDuringHandlingIsTolerated() throws Exception {
        VertxPrototypeAdapters.Gated gated = new VertxPrototypeAdapters.Gated(request -> {
            HttpMessage reply = new HttpMessage("late");
            reply.status(HttpStatus.OK);
            return reply;
        });
        gated.expect("slow");
        server = startWith(gated, VertxHttpServerOptions.defaults());
        int port = server.getActualPort();

        try (Socket socket = new Socket("127.0.0.1", port)) {
            String raw = "POST /disc HTTP/1.1\r\nHost: 127.0.0.1\r\nContent-Length: 4\r\nConnection: close\r\n\r\nslow";
            socket.getOutputStream().write(raw.getBytes(StandardCharsets.UTF_8));
            socket.getOutputStream().flush();
            assertTrue(gated.awaitArrival("slow", 10_000L));
        }
        gated.release("slow");

        HttpResponse<String> response = post(port, "after");
        assertEquals(response.statusCode(), 200);
    }

    @Test
    public void saturatedAdmissionYields503WithoutTouchingAdapter() throws Exception {
        java.util.concurrent.CountDownLatch arrived = new java.util.concurrent.CountDownLatch(1);
        java.util.concurrent.CountDownLatch release = new java.util.concurrent.CountDownLatch(1);
        VertxPrototypeAdapters.Recording recording = new VertxPrototypeAdapters.Recording(request -> {
            arrived.countDown();
            try {
                if (!release.await(30, TimeUnit.SECONDS)) {
                    throw new IllegalStateException("Timed out waiting for release");
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            HttpMessage reply = new HttpMessage("blocked");
            reply.status(HttpStatus.OK);
            return reply;
        });

        VertxHttpServerOptions options = VertxHttpServerOptions.builder()
                .maxConcurrentExchanges(1)
                .executorThreads(2)
                .build();
        server = startWith(recording, options);
        int port = server.getActualPort();

        CompletableFuture<HttpResponse<String>> blocked = postAsync(port, "blocked");
        assertTrue(arrived.await(10, TimeUnit.SECONDS));

        HttpResponse<String> saturated = post(port, "other");
        assertEquals(saturated.statusCode(), 503);
        assertEquals(recording.getInvocations(), 1);

        release.countDown();
        HttpResponse<String> done = blocked.get(10, TimeUnit.SECONDS);
        assertEquals(done.statusCode(), 200);
        assertEquals(done.body(), "blocked");
    }

    private VertxHttpServer startWith(org.citrusframework.endpoint.EndpointAdapter adapter,
                                      VertxHttpServerOptions options) {
        VertxHttpServer prototype = new VertxHttpServer();
        prototype.setName("vertxConcurrency-" + System.nanoTime());
        prototype.setPort(0);
        prototype.setReplyTimeoutMs(options.getReplyTimeoutMs());
        prototype.setMaxBodySizeBytes(options.getMaxBodySizeBytes());
        prototype.setBodyReadTimeoutMs(options.getBodyReadTimeoutMs());
        prototype.setExecutorThreads(options.getExecutorThreads());
        prototype.setMaxConcurrentExchanges(options.getMaxConcurrentExchanges());
        prototype.setEndpointAdapter(adapter);
        prototype.initialize();
        prototype.start();
        server = prototype;
        return prototype;
    }

    private static CompletableFuture<HttpResponse<String>> postAsync(int port, String body) {
        HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://127.0.0.1:" + port + "/conc"))
                .timeout(Duration.ofSeconds(30))
                .header("Content-Type", "text/plain;charset=UTF-8")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString());
    }

    private static HttpResponse<String> post(int port, String body) throws Exception {
        return postAsync(port, body).get(15, TimeUnit.SECONDS);
    }
}
