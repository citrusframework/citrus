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

package org.citrusframework.grpc.server;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import com.google.protobuf.Descriptors;
import com.google.protobuf.DynamicMessage;
import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.Status;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Keeps the {@link ServerCall} instances of all calls currently in flight on a simulator endpoint.
 * Writes to a call are serialized per call because grpc-java requires it, and an idle timeout is
 * restarted on every simulator send.
 */
public class ServerCallRegistry {

    private static final Logger LOG = LoggerFactory.getLogger(ServerCallRegistry.class);

    private final Map<String, ActiveCall> calls = new ConcurrentHashMap<>();

    public ActiveCall register(String callId,
                               ServerCall<DynamicMessage, DynamicMessage> call,
                               Descriptors.MethodDescriptor method,
                               Metadata requestMetadata,
                               long idleTimeout,
                               ScheduledExecutorService scheduler) {
        ActiveCall activeCall = new ActiveCall(this, callId, call, method, requestMetadata, idleTimeout, scheduler);
        calls.put(callId, activeCall);
        return activeCall;
    }

    public ActiveCall get(String callId) {
        return calls.get(callId);
    }

    public void remove(String callId) {
        calls.remove(callId);
    }

    public List<ActiveCall> getCalls() {
        return new ArrayList<>(calls.values());
    }

    /**
     * One live server call. All writes take the call's lock; closing or cancelling ends the idle
     * timer.
     */
    public static final class ActiveCall {

        private final ServerCallRegistry registry;
        private final String callId;
        private final ServerCall<DynamicMessage, DynamicMessage> call;
        private final Descriptors.MethodDescriptor method;
        private final Metadata requestMetadata;
        private final Object lock = new Object();
        private final AtomicBoolean headersSent = new AtomicBoolean();
        private final AtomicBoolean closed = new AtomicBoolean();
        private final Runnable idleTask;
        private final long idleTimeout;
        private final ScheduledExecutorService scheduler;

        private volatile boolean cancelled;
        private volatile ScheduledFuture<?> timeoutTask;

        private ActiveCall(ServerCallRegistry registry,
                           String callId,
                           ServerCall<DynamicMessage, DynamicMessage> call,
                           Descriptors.MethodDescriptor method,
                           Metadata requestMetadata,
                           long idleTimeout,
                           ScheduledExecutorService scheduler) {
            this.registry = registry;
            this.callId = callId;
            this.call = call;
            this.method = method;
            this.requestMetadata = requestMetadata;
            this.idleTimeout = idleTimeout;
            this.scheduler = scheduler;
            this.idleTask = () -> {
                if (closed.get() || cancelled) {
                    return;
                }
                LOG.warn("No response for gRPC call '{}' within {} ms", callId, idleTimeout);
                close(Status.INTERNAL.withDescription("Citrus simulator: no response within " + idleTimeout + " ms"), new Metadata());
                registry.remove(callId);
            };
        }

        public String getCallId() {
            return callId;
        }

        public Descriptors.MethodDescriptor getMethod() {
            return method;
        }

        public Metadata getRequestMetadata() {
            return requestMetadata;
        }

        public boolean isCancelled() {
            return cancelled;
        }

        public boolean isClosed() {
            return closed.get();
        }

        public void markCancelled() {
            cancelled = true;
            cancelTimeout();
        }

        public void sendHeaders(Metadata headers) {
            synchronized (lock) {
                if (headersSent.compareAndSet(false, true)) {
                    call.sendHeaders(headers);
                }
            }
        }

        public void sendMessage(DynamicMessage message) {
            synchronized (lock) {
                if (closed.get()) {
                    throw new CitrusRuntimeException(String.format("gRPC call '%s' is already closed", callId));
                }
                call.sendMessage(message);
            }
        }

        public void close(Status status, Metadata trailers) {
            synchronized (lock) {
                if (closed.compareAndSet(false, true)) {
                    call.close(status, trailers);
                }
            }
            cancelTimeout();
        }

        public void startTimeout() {
            cancelTimeout();
            if (idleTimeout > 0 && !closed.get() && !cancelled) {
                timeoutTask = scheduler.schedule(idleTask, idleTimeout, TimeUnit.MILLISECONDS);
            }
        }

        public void restartTimeout() {
            startTimeout();
        }

        private void cancelTimeout() {
            if (timeoutTask != null) {
                timeoutTask.cancel(false);
                timeoutTask = null;
            }
        }
    }
}
