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

package org.citrusframework.poc.mcp;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Correlates framework dispatches with Citrus simulation replies.
 *
 * <p>Token identifies endpoint + generation + framework session + request + local nonce.
 * One pending future per exchange; terminal states (reply/error/timeout/cancel/stop)
 * are atomic. Expired entries are removed; duplicate or late replies are rejected;
 * request-ID collisions across sessions cannot satisfy the wrong exchange.
 */
public final class ExchangeRegistry {

    public enum Terminal {
        REPLY,
        ERROR,
        TIMEOUT,
        CANCEL,
        STOP
    }

    public record PendingExchange(String token, String endpointId, int generation,
            String sessionId, String requestId, CompletableFuture<SimulationReply> future) {
    }

    private final ConcurrentHashMap<String, PendingExchange> pending = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, PendingExchange> all = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Terminal> terminal = new ConcurrentHashMap<>();

    /** Registers a new pending exchange and returns its opaque token. */
    public String register(String endpointId, int generation, String sessionId, String requestId) {
        Objects.requireNonNull(endpointId, "endpointId");
        Objects.requireNonNull(sessionId, "sessionId");
        Objects.requireNonNull(requestId, "requestId");
        String token = endpointId + "/g" + generation + "/" + sessionId + "/" + requestId
                + "#" + UUID.randomUUID().toString().substring(0, 8);
        PendingExchange exchange = new PendingExchange(token, endpointId, generation,
                sessionId, requestId, new CompletableFuture<>());
        if (pending.putIfAbsent(token, exchange) != null) {
            throw new IllegalStateException("token collision: " + token);
        }
        all.put(token, exchange);
        return token;
    }

    public PendingExchange pending(String token) {
        PendingExchange exchange = pending.get(token);
        if (exchange == null) {
            throw new IllegalArgumentException("unknown or terminal exchange: " + token);
        }
        return exchange;
    }

    /** Completes the exchange with a reply. Returns false if already terminal (late/duplicate). */
    public boolean reply(String token, SimulationReply reply) {
        PendingExchange exchange = pending.remove(token);
        if (exchange == null) {
            return false;
        }
        terminal.put(token, Terminal.REPLY);
        return exchange.future().complete(reply);
    }

    /** Fails the exchange with an error. Returns false if already terminal. */
    public boolean fail(String token, Throwable cause) {
        PendingExchange exchange = pending.remove(token);
        if (exchange == null) {
            return false;
        }
        terminal.put(token, Terminal.ERROR);
        return exchange.future().completeExceptionally(cause);
    }

    /** Cancels the exchange. Returns false if already terminal. */
    public boolean cancel(String token) {
        PendingExchange exchange = pending.remove(token);
        if (exchange == null) {
            return false;
        }
        terminal.put(token, Terminal.CANCEL);
        return exchange.future().cancel(true);
    }

    /** Marks all pending exchanges of a stopped generation terminal; they cannot satisfy later receives. */
    public int stopGeneration(String endpointId, int generation) {
        int stopped = 0;
        for (Map.Entry<String, PendingExchange> entry : pending.entrySet()) {
            PendingExchange exchange = entry.getValue();
            if (exchange.endpointId().equals(endpointId) && exchange.generation() == generation) {
                if (pending.remove(entry.getKey(), exchange)) {
                    terminal.put(entry.getKey(), Terminal.STOP);
                    exchange.future().cancel(true);
                    stopped++;
                }
            }
        }
        return stopped;
    }

    /** Awaits the reply within the simulation budget. Expired entries are removed from pending. */
    public SimulationReply await(String token, long timeout, TimeUnit unit) throws Exception {
        PendingExchange exchange = all.get(token);
        if (exchange == null) {
            throw new IllegalArgumentException("unknown exchange: " + token);
        }
        try {
            return exchange.future().get(timeout, unit);
        } catch (TimeoutException e) {
            if (pending.remove(token, exchange)) {
                terminal.put(token, Terminal.TIMEOUT);
            }
            throw e;
        }
    }

    public boolean isPending(String token) {
        return pending.containsKey(token);
    }

    /** Diagnostic snapshot of pending tokens (tests and probe reports; never a dispatch path). */
    public java.util.Set<String> pendingTokens() {
        return java.util.Set.copyOf(pending.keySet());
    }

    public Terminal terminalState(String token) {
        return terminal.get(token);
    }

    public int pendingCount() {
        return pending.size();
    }
}
