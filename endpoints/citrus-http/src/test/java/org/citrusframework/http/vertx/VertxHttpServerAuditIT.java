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

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.citrusframework.endpoint.direct.DirectEndpointAdapter;
import org.citrusframework.endpoint.direct.DirectMessageHeaders;
import org.citrusframework.http.message.HttpMessage;
import org.citrusframework.http.message.HttpMessageHeaders;
import org.citrusframework.message.Message;
import org.citrusframework.message.MessageQueue;
import org.springframework.http.HttpStatus;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

/** Regression cases reproduced by the evaluation audit. */
public class VertxHttpServerAuditIT {
    private static final HttpClient CLIENT = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1).connectTimeout(Duration.ofSeconds(2)).build();

    @Test
    public void repeatedStartKeepsTheSameListener() {
        VertxHttpServer server = server();
        server.initialize(); server.start();
        int port = server.getActualPort();
        try {
            // A second bind would fail; an idempotent start keeps the existing listener.
            server.setPort(port);
            server.start();
            assertEquals(server.getActualPort(), port);
            assertEquals(server.getGeneration(), 1L);
        } finally { server.stop(); server.destroy(); }
    }

    @Test
    public void stopTerminatesOwnedWorkers() throws Exception {
        VertxHttpServer server = server();
        server.setEndpointAdapter(VertxPrototypeAdapters.fixedReply(new HttpMessage("ok")));
        server.initialize(); server.start();
        try {
            assertEquals(send(server, "GET", "").statusCode(), 200);
            server.stop();
            assertFalse(hasWorker(server), "stop left an owned exchange worker alive");
        } finally { server.destroy(); }
    }

    @Test
    public void destroyClearsRunningStateAndAllowsAutoStart() {
        VertxHttpServer server = server(); server.initialize(); server.start();
        try {
            server.destroy();
            assertFalse(server.isRunning());
            server.setAutoStart(true); server.initialize();
            assertTrue(server.isRunning());
            assertTrue(server.getActualPort() > 0);
        } finally { server.stop(); server.destroy(); }
    }

    @Test
    public void oldQueuedRequestCannotReachRestartedDefaultAdapter() throws Exception {
        VertxHttpServer server = server();
        server.setExecutorThreads(1); server.setMaxConcurrentExchanges(2);
        server.setReplyTimeoutMs(5000); server.initialize(); server.start();
        Message first = null;
        try {
            CLIENT.sendAsync(request(server, "POST", "first"), HttpResponse.BodyHandlers.ofString());
            DirectEndpointAdapter old = (DirectEndpointAdapter) server.getEndpointAdapter();
            first = old.getEndpointConfiguration().getQueue().receive(1000);
            assertNotNull(first);
            CLIENT.sendAsync(request(server, "POST", "old-queued"), HttpResponse.BodyHandlers.ofString());
            var field = VertxHttpServer.class.getDeclaredField("exchangeExecutor");
            field.setAccessible(true);
            var executor = (VertxExchangeExecutor) field.get(server);
            long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(2);
            while (executor.availablePermits() != 0 && System.nanoTime() < deadline) Thread.sleep(10);
            assertEquals(executor.availablePermits(), 0, "second request did not reach the worker queue");
            server.stop();
            server.start();
            reply(first);
            DirectEndpointAdapter fresh = (DirectEndpointAdapter) server.getEndpointAdapter();
            Message leaked = fresh.getEndpointConfiguration().getQueue().receive(500);
            if (leaked != null) reply(leaked);
            assertNull(leaked, "old work entered the fresh Citrus queue");
        } finally {
            if (first != null) reply(first);
            server.stop(); server.destroy();
        }
    }

    @Test
    public void changedAdmissionLimitAppliesAtStart() throws Exception {
        VertxHttpServer server = server();
        CountDownLatch entered = new CountDownLatch(1), release = new CountDownLatch(1);
        server.setEndpointAdapter(VertxPrototypeAdapters.delegate(message -> {
            if ("first".equals(message.getPayload())) {
                entered.countDown();
                try { release.await(5, TimeUnit.SECONDS); }
                catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            }
            return new HttpMessage("ok");
        }));
        server.initialize(); server.setMaxConcurrentExchanges(1); server.start();
        try {
            CLIENT.sendAsync(request(server, "POST", "first"), HttpResponse.BodyHandlers.ofString());
            assertTrue(entered.await(2, TimeUnit.SECONDS));
            assertEquals(send(server, "POST", "second").statusCode(), 503);
        } finally { release.countDown(); server.stop(); server.destroy(); }
    }

    @Test
    public void oversizedChunkIsRejectedBeforeUploadFinishes() throws Exception {
        VertxHttpServer server = server(); server.setMaxBodySizeBytes(4);
        server.initialize(); server.start();
        try (Socket socket = new Socket("127.0.0.1", server.getActualPort())) {
            socket.setSoTimeout(1500);
            socket.getOutputStream().write(("POST / HTTP/1.1\r\nHost: localhost\r\n"
                    + "Transfer-Encoding: chunked\r\n\r\n8\r\n12345678\r\n")
                    .getBytes(StandardCharsets.US_ASCII));
            String status = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.US_ASCII)).readLine();
            assertTrue(status.contains("413"), status);
        } finally { server.stop(); server.destroy(); }
    }

    @Test
    public void bodyDeadlineClosesIncompleteUpload() throws Exception {
        VertxHttpServer server = server(); server.setBodyReadTimeoutMs(100);
        server.initialize(); server.start();
        try (Socket socket = new Socket("127.0.0.1", server.getActualPort())) {
            socket.setSoTimeout(1500);
            socket.getOutputStream().write(("POST / HTTP/1.1\r\nHost: localhost\r\n"
                    + "Transfer-Encoding: chunked\r\n\r\n1\r\nx\r\n").getBytes(StandardCharsets.US_ASCII));
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.US_ASCII));
            assertTrue(reader.readLine().contains("408"));
            while (!reader.readLine().isEmpty()) { }
            assertEquals(reader.read(), -1, "timed-out upload connection should close");
        } finally { server.stop(); server.destroy(); }
    }

    @Test
    public void invalidOutboundStatusGetsTerminal500() throws Exception {
        VertxHttpServer server = server();
        server.setEndpointAdapter(VertxPrototypeAdapters.delegate(message -> {
            HttpMessage response = new HttpMessage("private response");
            response.setHeader(HttpMessageHeaders.HTTP_STATUS_CODE, "bad");
            return response;
        }));
        server.initialize(); server.start();
        try {
            HttpResponse<String> response = send(server, "GET", "");
            assertEquals(response.statusCode(), 500);
            assertEquals(response.body(), "");
            assertEquals(server.getFailures().size(), 1, "conversion failure must be visible to the harness");
        } finally { server.stop(); server.destroy(); }
    }

    @Test
    public void customCharsetSurvivesStartup() throws Exception {
        VertxHttpServer server = server();
        server.getHttpEndpointConfiguration().setCharset("ISO-8859-1");
        server.setEndpointAdapter(VertxPrototypeAdapters.fixedReply(new HttpMessage("Grüße")));
        server.initialize(); server.start();
        try {
            HttpResponse<byte[]> response = CLIENT.send(request(server, "GET", ""), HttpResponse.BodyHandlers.ofByteArray());
            assertEquals(response.body(), "Grüße".getBytes(StandardCharsets.ISO_8859_1));
        } finally { server.stop(); server.destroy(); }
    }

    @Test
    public void incompleteUploadConsumesAdmissionBeforeBuffering() throws Exception {
        VertxHttpServer server = server(); server.setMaxConcurrentExchanges(1);
        server.setBodyReadTimeoutMs(5000); server.initialize(); server.start();
        try (Socket socket = new Socket("127.0.0.1", server.getActualPort())) {
            socket.getOutputStream().write(("POST / HTTP/1.1\r\nHost: localhost\r\n"
                    + "Transfer-Encoding: chunked\r\n\r\n1\r\nx\r\n").getBytes(StandardCharsets.US_ASCII));
            socket.getOutputStream().flush();
            var field = VertxHttpServer.class.getDeclaredField("exchangeExecutor"); field.setAccessible(true);
            var executor = (VertxExchangeExecutor) field.get(server);
            long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(2);
            while (executor.availablePermits() != 0 && System.nanoTime() < deadline) Thread.sleep(10);
            assertEquals(executor.availablePermits(), 0);
            assertEquals(send(server, "POST", "second").statusCode(), 503);
        } finally { server.stop(); server.destroy(); }
    }

    @Test
    public void uncooperativeAdapterMakesShutdownFailureVisibleAndPreventsRestart() throws Exception {
        VertxHttpServer server = server(); server.setShutdownTimeoutMs(100);
        CountDownLatch entered = new CountDownLatch(1), release = new CountDownLatch(1);
        server.setEndpointAdapter(VertxPrototypeAdapters.delegate(message -> {
            entered.countDown();
            while (release.getCount() != 0) {
                try { release.await(); } catch (InterruptedException ignored) { /* deliberately uncooperative */ }
            }
            return new HttpMessage("late");
        }));
        server.initialize(); server.start();
        try {
            CLIENT.sendAsync(request(server, "POST", "first"), HttpResponse.BodyHandlers.ofString());
            assertTrue(entered.await(2, TimeUnit.SECONDS));
            long started = System.nanoTime();
            expectThrows(org.citrusframework.exceptions.CitrusRuntimeException.class, server::stop);
            assertTrue(TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - started) < 1500);
            assertFalse(server.isRunning()); assertFalse(server.getFailures().isEmpty());
            expectThrows(org.citrusframework.exceptions.CitrusRuntimeException.class, server::start);
            assertEquals(server.getGeneration(), 1L, "restart must not create a second generation while old workers survive");
        } finally {
            release.countDown();
            long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(2);
            while (hasWorker(server) && System.nanoTime() < deadline) Thread.sleep(10);
            server.stop(); server.destroy();
        }
    }

    private static VertxHttpServer server() {
        VertxHttpServer server = new VertxHttpServer();
        server.setName("vertxAudit-" + System.nanoTime()); server.setPort(0);
        return server;
    }
    private static HttpRequest request(VertxHttpServer server, String method, String body) {
        return HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + server.getActualPort() + "/audit"))
                .timeout(Duration.ofSeconds(3)).header("Content-Type", "text/plain")
                .method(method, HttpRequest.BodyPublishers.ofString(body)).build();
    }
    private static HttpResponse<String> send(VertxHttpServer server, String method, String body) throws Exception {
        return CLIENT.send(request(server, method, body), HttpResponse.BodyHandlers.ofString());
    }
    private static void reply(Message message) {
        ((MessageQueue) message.getHeader(DirectMessageHeaders.REPLY_QUEUE)).send(new HttpMessage("ok").status(HttpStatus.OK));
    }
    private static boolean hasWorker(VertxHttpServer server) {
        return Thread.getAllStackTraces().keySet().stream()
                .anyMatch(thread -> thread.isAlive() && thread.getName().startsWith(server.getName() + "-exchange"));
    }
}
