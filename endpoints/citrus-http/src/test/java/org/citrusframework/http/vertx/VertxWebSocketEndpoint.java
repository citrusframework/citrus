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

import io.vertx.core.Context;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.ServerWebSocket;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.*;
import org.citrusframework.context.TestContext;
import org.citrusframework.endpoint.AbstractEndpoint;
import org.citrusframework.endpoint.AbstractEndpointConfiguration;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.exceptions.MessageTimeoutException;
import org.citrusframework.message.DefaultMessage;
import org.citrusframework.message.Message;
import org.citrusframework.messaging.Consumer;
import org.citrusframework.messaging.Producer;

/** Test-only Citrus endpoint for complete native Vert.x WebSocket messages. */
public final class VertxWebSocketEndpoint extends AbstractEndpoint {
    public static final String ID = "citrus_websocket_id";
    public static final String PATH = "citrus_websocket_path";
    public static final String IS_LAST = "citrus_websocket_is_last";
    private final String path;
    private final int maxSessions;
    private final int queueCapacity;
    private final int maxMessageBytes;
    private volatile State current;

    public VertxWebSocketEndpoint(String path) { this(path, 16, 64, 65536); }
    public VertxWebSocketEndpoint(String path, int maxSessions, int queueCapacity, int maxMessageBytes) {
        super(new AbstractEndpointConfiguration() { });
        if (path == null || !path.startsWith("/") || path.contains("*"))
            throw new IllegalArgumentException("WebSocket path must be an exact absolute path");
        new VertxHttpRoute("/", path);
        if (maxSessions < 1 || queueCapacity < 1 || maxMessageBytes < 1)
            throw new IllegalArgumentException("WebSocket limits must be positive");
        this.path = path; this.maxSessions = maxSessions; this.queueCapacity = queueCapacity;
        this.maxMessageBytes = maxMessageBytes;
    }
    int maxMessageBytes() { return maxMessageBytes; }
    synchronized State open() {
        if (current != null && !current.closed) throw new IllegalStateException("WebSocket endpoint already running");
        current = new State(); return current;
    }
    private State state() {
        State state = current;
        if (state == null || state.closed) throw new CitrusRuntimeException("WebSocket endpoint is stopped");
        return state;
    }
    @Override public Consumer createConsumer() {
        return new Consumer() {
            public String getName() { return getConsumerName(); }
            public Message receive(TestContext context) { return receive(context, getEndpointConfiguration().getTimeout()); }
            public Message receive(TestContext context, long timeout) {
                if (Context.isOnEventLoopThread()) throw new IllegalStateException("Receive must run outside the event loop");
                Message message = state().receive(timeout);
                context.onInboundMessage(message); return message;
            }
        };
    }
    @Override public Producer createProducer() {
        return new Producer() {
            public String getName() { return getProducerName(); }
            public void send(Message message, TestContext context) {
                if (Context.isOnEventLoopThread()) throw new IllegalStateException("Send must run outside the event loop");
                state().send(message); context.onOutboundMessage(message);
            }
        };
    }
    final class State {
        private final ArrayDeque<Message> incoming = new ArrayDeque<>();
        private final Map<String, Session> sessions = new LinkedHashMap<>();
        private volatile boolean closed;
        private int reservations;
        boolean matches(String requestPath) { return path.equals(requestPath); }
        synchronized boolean reserve() {
            if (closed || reservations >= maxSessions) return false;
            reservations++; return true;
        }
        synchronized void releaseReservation() { reservations--; }
        synchronized void attach(ServerWebSocket socket, VertxHttpSecurity.Identity identity) {
            if (closed) { releaseReservation(); socket.close((short) 1001); return; }
            var session = new Session(socket, Vertx.currentContext());
            sessions.put(session.id, session);
            socket.setWriteQueueMaxSize(maxMessageBytes);
            socket.closeHandler(ignored -> remove(session));
            socket.exceptionHandler(error -> {
                remove(session);
                int code = error instanceof io.netty.handler.codec.http.websocketx.CorruptedWebSocketFrameException frame
                        ? frame.closeStatus().code()
                        : error instanceof IllegalStateException && error.getMessage() != null
                            && error.getMessage().contains("overflow max message size") ? 1009 : 1011;
                socket.close((short) code);
            });
            socket.textMessageHandler(text -> accept(session, text, identity));
            socket.binaryMessageHandler(bytes -> accept(session, bytes.getBytes(), identity));
        }
        private synchronized void remove(Session session) {
            if (sessions.remove(session.id) != null) reservations--;
        }
        private synchronized void accept(Session session, Object payload, VertxHttpSecurity.Identity identity) {
            if (closed || !sessions.containsKey(session.id)) return;
            if (incoming.size() >= queueCapacity) {
                remove(session); session.socket.close((short) 1013, "Inbound queue full"); return;
            }
            var message = new DefaultMessage(payload).setHeader(ID, session.id).setHeader(PATH, path).setHeader(IS_LAST, true);
            if (identity != null) {
                message.setHeader(VertxHttpSecurity.PRINCIPAL_HEADER, identity.username());
                message.setHeader(VertxHttpSecurity.ROLES_HEADER, identity.roles());
            }
            incoming.add(message); notifyAll();
        }
        synchronized Message receive(long timeout) {
            long deadline = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(Math.max(0, timeout));
            while (!closed && incoming.isEmpty()) {
                long remaining = deadline - System.nanoTime();
                if (remaining <= 0) throw new MessageTimeoutException(timeout, path);
                try { TimeUnit.NANOSECONDS.timedWait(this, remaining); }
                catch (InterruptedException e) { Thread.currentThread().interrupt(); throw new CitrusRuntimeException("WebSocket receive interrupted", e); }
            }
            if (closed) throw new CitrusRuntimeException("WebSocket endpoint stopped while receiving");
            return incoming.remove();
        }
        void send(Message message) {
            Object payload = message.getPayload();
            if (!(payload instanceof String) && !(payload instanceof byte[]))
                throw new IllegalArgumentException("WebSocket payload must be String or byte[]");
            byte[] bytes = payload instanceof byte[] binary ? binary.clone() : ((String) payload).getBytes(StandardCharsets.UTF_8);
            if (bytes.length > maxMessageBytes) throw new IllegalArgumentException("WebSocket message exceeds configured limit");
            List<Session> targets;
            synchronized (this) {
                if (closed) throw new CitrusRuntimeException("WebSocket endpoint is stopped");
                Object id = message.getHeader(ID);
                targets = id == null ? List.copyOf(sessions.values())
                        : sessions.containsKey(id.toString()) ? List.of(sessions.get(id.toString())) : List.of();
                if (targets.isEmpty()) throw new CitrusRuntimeException("No matching WebSocket sessions");
                if (targets.stream().anyMatch(session -> session.sending))
                    throw new CitrusRuntimeException("WebSocket write already pending");
                targets.forEach(session -> session.sending = true);
            }
            var writes = new ArrayList<CompletableFuture<Void>>();
            for (Session session : targets) {
                var done = new CompletableFuture<Void>(); writes.add(done);
                done.whenComplete((result, failure) -> { synchronized (this) { session.sending = false; } });
                session.context.runOnContext(ignored -> {
                    synchronized (this) {
                        if (closed || !sessions.containsKey(session.id)) {
                            done.completeExceptionally(new IllegalStateException("WebSocket session closed")); return;
                        }
                    }
                    try {
                        if (session.socket.writeQueueFull()) throw new IllegalStateException("WebSocket write queue full");
                        var write = payload instanceof String text ? session.socket.writeTextMessage(text)
                                : session.socket.writeBinaryMessage(Buffer.buffer(bytes));
                        write.onComplete(result -> {
                            if (result.succeeded()) done.complete(null); else done.completeExceptionally(result.cause());
                        });
                    } catch (Exception e) { done.completeExceptionally(e); }
                });
            }
            try { CompletableFuture.allOf(writes.toArray(CompletableFuture[]::new))
                    .get(Math.max(1, getEndpointConfiguration().getTimeout()), TimeUnit.MILLISECONDS); }
            catch (Exception e) {
                if (e instanceof InterruptedException) Thread.currentThread().interrupt();
                throw new CitrusRuntimeException("WebSocket send failed", e);
            }
        }
        synchronized void close() {
            closed = true; incoming.clear(); notifyAll();
            for (Session session : List.copyOf(sessions.values())) {
                session.context.runOnContext(ignored -> session.socket.close((short) 1001, "Server stopping"));
            }
            reservations -= sessions.size(); sessions.clear();
        }
    }
    private static final class Session {
        private final String id = UUID.randomUUID().toString();
        private final ServerWebSocket socket;
        private final Context context;
        private boolean sending;
        private Session(ServerWebSocket socket, Context context) { this.socket = socket; this.context = context; }
    }
}
