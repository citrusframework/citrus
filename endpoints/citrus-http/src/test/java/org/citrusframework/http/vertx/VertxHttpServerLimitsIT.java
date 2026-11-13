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
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import org.citrusframework.http.message.HttpMessage;
import org.springframework.http.HttpStatus;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertTrue;

/**
 * Payload and reply semantics of the Vert.x prototype (AC4): UTF-8 and charset handling,
 * empty and binary bodies, explicit and missing reply status, null replies, reply timeouts
 * (never 504) and HEAD body suppression.
 */
public class VertxHttpServerLimitsIT {

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
    public void utf8TextRoundtripsWithCharset() throws Exception {
        server = startWith(VertxPrototypeAdapters.echo(), VertxHttpServerOptions.defaults());
        String body = "Hello Citrus äöü €";

        HttpResponse<byte[]> response = postBytes(server.getActualPort(), "/text",
                body.getBytes(StandardCharsets.UTF_8), "text/plain;charset=UTF-8");

        assertEquals(response.statusCode(), 200);
        assertEquals(new String(response.body(), StandardCharsets.UTF_8), body);
        assertTrue(contentType(response).startsWith("text/plain"));
    }

    @Test
    public void latin1CharsetIsHonored() throws Exception {
        server = startWith(VertxPrototypeAdapters.echo(), VertxHttpServerOptions.defaults());
        byte[] body = "Grüße".getBytes(StandardCharsets.ISO_8859_1);

        HttpResponse<byte[]> response = postBytes(server.getActualPort(), "/text", body,
                "text/plain;charset=ISO-8859-1");

        assertEquals(response.statusCode(), 200);
        assertTrue(Arrays.equals(response.body(), body));
    }

    @Test
    public void emptyBodyRoundtripsAsEmpty() throws Exception {
        server = startWith(VertxPrototypeAdapters.echo(), VertxHttpServerOptions.defaults());

        HttpResponse<byte[]> response = postBytes(server.getActualPort(), "/empty", new byte[0], "text/plain");

        assertEquals(response.statusCode(), 200);
        assertEquals(response.body().length, 0);
    }

    @Test
    public void binaryBodyRoundtripsByteIdentical() throws Exception {
        server = startWith(VertxPrototypeAdapters.echo(), VertxHttpServerOptions.defaults());
        byte[] body = new byte[4096];
        for (int i = 0; i < body.length; i++) {
            body[i] = (byte) (i % 251);
        }

        HttpResponse<byte[]> response = postBytes(server.getActualPort(), "/binary", body,
                "application/octet-stream");

        assertEquals(response.statusCode(), 200);
        assertTrue(Arrays.equals(response.body(), body));
        assertTrue(contentType(response).startsWith("application/octet-stream"));
    }

    @Test
    public void oversizeBodyYields413WithoutTouchingAdapter() throws Exception {
        VertxPrototypeAdapters.Recording recording =
                new VertxPrototypeAdapters.Recording(VertxPrototypeAdapters.echo()::handleMessage);
        server = startWith(recording, VertxHttpServerOptions.defaults());

        byte[] huge = new byte[(int) VertxHttpServerOptions.DEFAULT_MAX_BODY_SIZE_BYTES + 1024];
        HttpResponse<byte[]> response = postBytes(server.getActualPort(), "/huge", huge,
                "application/octet-stream");

        assertEquals(response.statusCode(), 413);
        assertEquals(recording.getInvocations(), 0);
    }

    @Test
    public void replyTimeoutYieldsDefaultStatusAndNever504() throws Exception {
        VertxHttpServerOptions options = VertxHttpServerOptions.builder()
                .replyTimeoutMs(500L)
                .defaultStatusCode(200)
                .build();
        server = startWith(null, options);

        long started = System.nanoTime();
        HttpResponse<String> response = get(server.getActualPort(), "/waiting");
        long elapsedMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - started);

        assertEquals(response.statusCode(), 200);
        assertEquals(response.body(), "");
        assertNotEquals(response.statusCode(), 504);
        assertTrue(elapsedMs < 10_000L, "Reply timeout took too long: " + elapsedMs + " ms");
    }

    @Test
    public void nullReplyYieldsDefaultStatus() throws Exception {
        server = startWith(VertxPrototypeAdapters.fixedReply(null), VertxHttpServerOptions.defaults());

        HttpResponse<String> response = get(server.getActualPort(), "/null");
        assertEquals(response.statusCode(), 200);
        assertEquals(response.body(), "");
    }

    @Test
    public void explicitReplyStatusPropagates() throws Exception {
        server = startWith(VertxPrototypeAdapters.delegate(request -> {
            HttpMessage reply = new HttpMessage("created");
            reply.status(HttpStatus.CREATED);
            return reply;
        }), VertxHttpServerOptions.defaults());

        HttpResponse<String> response = get(server.getActualPort(), "/create");
        assertEquals(response.statusCode(), 201);
        assertEquals(response.body(), "created");
    }

    @Test
    public void missingReplyStatusFallsBackToConfiguredDefault() throws Exception {
        VertxHttpServerOptions options = VertxHttpServerOptions.builder()
                .defaultStatusCode(202)
                .build();
        server = startWith(VertxPrototypeAdapters.delegate(request -> new HttpMessage("accepted")),
                options);

        HttpResponse<String> response = get(server.getActualPort(), "/accept");
        assertEquals(response.statusCode(), 202);
        assertEquals(response.body(), "accepted");
    }

    @Test
    public void headSuppressesBodyButKeepsContentLength() throws Exception {
        String body = "would-be-body";
        server = startWith(VertxPrototypeAdapters.delegate(request -> {
            HttpMessage reply = new HttpMessage(body);
            reply.status(HttpStatus.OK);
            return reply;
        }), VertxHttpServerOptions.defaults());

        HttpResponse<byte[]> response = head(server.getActualPort(), "/head");

        assertEquals(response.statusCode(), 200);
        assertEquals(response.body().length, 0);
        assertEquals(response.headers().firstValue("Content-Length").orElse("-1"),
                String.valueOf(body.getBytes(StandardCharsets.UTF_8).length));
    }

    private VertxHttpServer startWith(org.citrusframework.endpoint.EndpointAdapter adapter,
                                      VertxHttpServerOptions options) {
        VertxHttpServer prototype = new VertxHttpServer();
        prototype.setName("vertxLimits-" + System.nanoTime());
        prototype.setPort(0);
        prototype.setReplyTimeoutMs(options.getReplyTimeoutMs());
        prototype.setDefaultStatusCode(options.getDefaultStatusCode());
        prototype.setMaxBodySizeBytes(options.getMaxBodySizeBytes());
        prototype.setBodyReadTimeoutMs(options.getBodyReadTimeoutMs());
        prototype.setExecutorThreads(options.getExecutorThreads());
        prototype.setMaxConcurrentExchanges(options.getMaxConcurrentExchanges());
        if (adapter != null) {
            prototype.setEndpointAdapter(adapter);
        }
        prototype.initialize();
        prototype.start();
        server = prototype;
        return prototype;
    }

    private static HttpResponse<byte[]> postBytes(int port, String path, byte[] body, String contentType)
            throws Exception {
        HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://127.0.0.1:" + port + path))
                .timeout(Duration.ofSeconds(30))
                .header("Content-Type", contentType)
                .POST(HttpRequest.BodyPublishers.ofByteArray(body))
                .build();
        return client.send(request, HttpResponse.BodyHandlers.ofByteArray());
    }

    private static HttpResponse<String> get(int port, String path) throws Exception {
        HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://127.0.0.1:" + port + path))
                .timeout(Duration.ofSeconds(30))
                .GET()
                .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private static HttpResponse<byte[]> head(int port, String path) throws Exception {
        HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://127.0.0.1:" + port + path))
                .timeout(Duration.ofSeconds(30))
                .method("HEAD", HttpRequest.BodyPublishers.noBody())
                .build();
        return client.send(request, HttpResponse.BodyHandlers.ofByteArray());
    }

    private static String contentType(HttpResponse<?> response) {
        return response.headers().firstValue("Content-Type").orElse("");
    }
}
