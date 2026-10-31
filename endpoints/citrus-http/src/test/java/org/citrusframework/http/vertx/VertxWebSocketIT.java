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
import java.net.http.WebSocket;
import java.nio.ByteBuffer;
import java.util.concurrent.*;
import org.citrusframework.context.TestContext;
import org.citrusframework.message.DefaultMessage;
import org.testng.annotations.Test;
import static org.testng.Assert.*;

public class VertxWebSocketIT {
    public static class Listener implements WebSocket.Listener {
        public final BlockingQueue<Object> messages = new LinkedBlockingQueue<>();
        public final CompletableFuture<Integer> closed = new CompletableFuture<>();
        private final StringBuilder text = new StringBuilder();
        private final java.io.ByteArrayOutputStream binary = new java.io.ByteArrayOutputStream();
        public void onOpen(WebSocket socket) { socket.request(1); }
        public CompletionStage<?> onText(WebSocket socket, CharSequence data, boolean last) {
            text.append(data); if (last) { messages.add(text.toString()); text.setLength(0); }
            socket.request(1); return null;
        }
        public CompletionStage<?> onBinary(WebSocket socket, ByteBuffer data, boolean last) {
            byte[] bytes = new byte[data.remaining()]; data.get(bytes); binary.writeBytes(bytes);
            if (last) { messages.add(binary.toByteArray()); binary.reset(); }
            socket.request(1); return null;
        }
        public CompletionStage<?> onPong(WebSocket socket, ByteBuffer data) {
            messages.add("pong"); socket.request(1); return null;
        }
        public CompletionStage<?> onClose(WebSocket socket, int status, String reason) {
            closed.complete(status); return null;
        }
        public void onError(WebSocket socket, Throwable error) { closed.completeExceptionally(error); }
    }
    static WebSocket connect(VertxHttpServer server, Listener listener) throws Exception {
        return HttpClient.newHttpClient().newWebSocketBuilder().buildAsync(
                URI.create("ws://127.0.0.1:" + server.getActualPort() + "/ws"), listener).get(5, TimeUnit.SECONDS);
    }
    @Test public void exchangesFragmentedTextBinaryTargetedAndBroadcastMessages() throws Exception {
        var endpoint = new VertxWebSocketEndpoint("/ws");
        var server = new VertxHttpServer(); server.setPort(0); server.setWebSocketEndpoint(endpoint);
        server.initialize(); server.start();
        var context = new TestContext();
        var first = new Listener(); var second = new Listener();
        try {
            var a = connect(server, first); var b = connect(server, second);
            a.sendText("hel", false).get(3, TimeUnit.SECONDS); a.sendText("lo", true).get(3, TimeUnit.SECONDS);
            var received = endpoint.createConsumer().receive(context, 3000);
            assertEquals(received.getPayload(), "hello");
            assertEquals(received.getHeader(VertxWebSocketEndpoint.PATH), "/ws");
            endpoint.createProducer().send(new DefaultMessage("one").setHeader(VertxWebSocketEndpoint.ID,
                    received.getHeader(VertxWebSocketEndpoint.ID)), context);
            assertEquals(first.messages.poll(3, TimeUnit.SECONDS), "one"); assertNull(second.messages.poll(100, TimeUnit.MILLISECONDS));
            byte[] bytes = {0, -1, 42};
            b.sendBinary(ByteBuffer.wrap(bytes), true).get(3, TimeUnit.SECONDS);
            assertEquals(endpoint.createConsumer().receive(context, 3000).getPayload(), bytes);
            endpoint.createProducer().send(new DefaultMessage(bytes), context);
            assertEquals(first.messages.poll(3, TimeUnit.SECONDS), bytes); assertEquals(second.messages.poll(3, TimeUnit.SECONDS), bytes);
            a.sendPing(ByteBuffer.wrap(new byte[]{1})).get(3, TimeUnit.SECONDS);
            assertEquals(first.messages.poll(3, TimeUnit.SECONDS), "pong");
            a.sendClose(1000, "done").get(3, TimeUnit.SECONDS);
            assertEquals(first.closed.get(3, TimeUnit.SECONDS).intValue(), 1000);
        } finally { server.stop(); server.destroy(); }
    }
    @Test public void boundsSessionsQueueAndMessageSizeAndResetsOnRestart() throws Exception {
        var endpoint = new VertxWebSocketEndpoint("/ws", 1, 1, 32);
        var server = new VertxHttpServer(); server.setPort(0); server.setWebSocketEndpoint(endpoint);
        server.initialize(); server.start(); var context = new TestContext();
        try {
            var listener = new Listener(); var socket = connect(server, listener);
            var failure = expectThrows(ExecutionException.class, () -> connect(server, new Listener()));
            assertEquals(((java.net.http.WebSocketHandshakeException) failure.getCause()).getResponse().statusCode(), 503);
            socket.sendText("one", true).get(3, TimeUnit.SECONDS);
            socket.sendText("two", true).get(3, TimeUnit.SECONDS);
            assertEquals(listener.closed.get(3, TimeUnit.SECONDS).intValue(), 1013);
            var old = endpoint.createConsumer().receive(context, 3000);
            server.stop(); server.start();
            var nextListener = new Listener(); var next = connect(server, nextListener);
            expectThrows(RuntimeException.class, () -> endpoint.createProducer().send(
                    new DefaultMessage("late").setHeader(VertxWebSocketEndpoint.ID, old.getHeader(VertxWebSocketEndpoint.ID)), context));
            next.sendText("x".repeat(33), true).get(3, TimeUnit.SECONDS);
            assertEquals(nextListener.closed.get(3, TimeUnit.SECONDS).intValue(), 1009);
            server.stop(); server.start();
            var fragments = new Listener(); var fragmented = connect(server, fragments);
            fragmented.sendText("x".repeat(20), false).get(3, TimeUnit.SECONDS);
            fragmented.sendText("x".repeat(20), true).get(3, TimeUnit.SECONDS);
            assertEquals(fragments.closed.get(3, TimeUnit.SECONDS).intValue(), 1009);
            expectThrows(org.citrusframework.exceptions.MessageTimeoutException.class,
                    () -> endpoint.createConsumer().receive(context, 50));
        } finally { server.stop(); server.destroy(); }
    }
    @Test public void stopsWaitingConsumersAndRejectsEndpointReuse() throws Exception {
        var endpoint = new VertxWebSocketEndpoint("/ws");
        var server = new VertxHttpServer(); server.setPort(0); server.setWebSocketEndpoint(endpoint);
        var other = new VertxHttpServer(); other.setPort(0); other.setWebSocketEndpoint(endpoint);
        server.initialize(); other.initialize(); server.start();
        var pool = Executors.newSingleThreadExecutor();
        try {
            expectThrows(IllegalStateException.class, other::start);
            var ready = new CountDownLatch(1);
            var waiting = pool.submit(() -> { ready.countDown(); return endpoint.createConsumer().receive(new TestContext(), 30000); });
            assertTrue(ready.await(1, TimeUnit.SECONDS));
            server.stop();
            assertTrue(expectThrows(ExecutionException.class, () -> waiting.get(2, TimeUnit.SECONDS))
                    .getCause() instanceof org.citrusframework.exceptions.CitrusRuntimeException);
            other.start(); var listener = new Listener(); connect(other, listener);
            other.stop(); assertEquals(listener.closed.get(3, TimeUnit.SECONDS).intValue(), 1001);
        } finally { pool.shutdownNow(); server.stop(); server.destroy(); other.stop(); other.destroy(); }
    }
    @Test public void rejectsUnmappedUpgradesAndPreservesHttpTraffic() throws Exception {
        var server = new VertxHttpServer(); server.setPort(0); server.setContextPath("/api");
        server.setWebSocketEndpoint(new VertxWebSocketEndpoint("/api/ws"));
        server.setEndpointAdapter(VertxPrototypeAdapters.fixedReply(new org.citrusframework.http.message.HttpMessage("http")));
        server.initialize(); server.start();
        var client = HttpClient.newHttpClient();
        try {
            for (String path : new String[]{"/ws", "/api/other"}) {
                var failure = expectThrows(ExecutionException.class, () -> client.newWebSocketBuilder().buildAsync(
                        URI.create("ws://127.0.0.1:" + server.getActualPort() + path), new Listener()).get(3, TimeUnit.SECONDS));
                assertEquals(((java.net.http.WebSocketHandshakeException) failure.getCause()).getResponse().statusCode(), 404);
            }
            var socket = client.newWebSocketBuilder().buildAsync(URI.create("ws://127.0.0.1:" + server.getActualPort() + "/api/ws"), new Listener()).get(3, TimeUnit.SECONDS);
            var response = client.send(java.net.http.HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + server.getActualPort() + "/api/hello")).build(),
                    java.net.http.HttpResponse.BodyHandlers.ofString());
            assertEquals(response.body(), "http"); socket.abort();
        } finally { server.stop(); server.destroy(); }
    }
}
