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

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import org.citrusframework.base.endpoint.AbstractEndpointAdapter;
import org.citrusframework.endpoint.EndpointAdapter;
import org.citrusframework.http.message.HttpMessage;
import org.citrusframework.message.Message;
import org.springframework.http.HttpStatus;

/**
 * Test-only endpoint adapters used by the Vert.x prototype ITs.
 */
public final class VertxPrototypeAdapters {

    private VertxPrototypeAdapters() {
    }

    public static EndpointAdapter echo() {
        return delegate(request -> {
            HttpMessage reply = new HttpMessage(request);
            reply.status(HttpStatus.OK);
            return reply;
        });
    }

    public static EndpointAdapter fixedReply(Message reply) {
        return delegate(request -> reply);
    }

    public static EndpointAdapter throwing(RuntimeException failure) {
        return delegate(request -> {
            throw failure;
        });
    }

    public static EndpointAdapter delegate(Function<Message, Message> handler) {
        return new AbstractEndpointAdapter() {
            @Override
            protected Message handleMessageInternal(Message message) {
                return handler.apply(message);
            }

            @Override
            public org.citrusframework.endpoint.Endpoint getEndpoint() {
                return null;
            }

            @Override
            public org.citrusframework.endpoint.EndpointConfiguration getEndpointConfiguration() {
                return null;
            }
        };
    }

    /**
     * Adapter that records invocation threads and counts invocations.
     */
    public static final class Recording extends AbstractEndpointAdapter {
        private final Function<Message, Message> handler;
        private final List<String> threads = new CopyOnWriteArrayList<>();
        private final AtomicInteger invocations = new AtomicInteger();

        public Recording(Function<Message, Message> handler) {
            this.handler = handler;
        }

        @Override
        protected Message handleMessageInternal(Message message) {
            threads.add(Thread.currentThread().getName());
            invocations.incrementAndGet();
            return handler.apply(message);
        }

        @Override
        public org.citrusframework.endpoint.Endpoint getEndpoint() {
            return null;
        }

        @Override
        public org.citrusframework.endpoint.EndpointConfiguration getEndpointConfiguration() {
            return null;
        }

        public List<String> getThreads() {
            return threads;
        }

        public int getInvocations() {
            return invocations.get();
        }
    }

    /**
     * Adapter that blocks each request on a per-body latch until the test releases it.
     */
    public static final class Gated extends AbstractEndpointAdapter {
        private final ConcurrentMap<String, CountDownLatch> arrived = new ConcurrentHashMap<>();
        private final ConcurrentMap<String, CountDownLatch> releases = new ConcurrentHashMap<>();
        private final Function<Message, Message> replies;

        public Gated(Function<Message, Message> replies) {
            this.replies = replies;
        }

        public void expect(String body) {
            arrived.put(body, new CountDownLatch(1));
            releases.put(body, new CountDownLatch(1));
        }

        public boolean awaitArrival(String body, long timeoutMs) throws InterruptedException {
            return arrived.get(body).await(timeoutMs, TimeUnit.MILLISECONDS);
        }

        public void release(String body) {
            releases.get(body).countDown();
        }

        @Override
        protected Message handleMessageInternal(Message message) {
            String body = message.getPayload(String.class);
            CountDownLatch arrival = arrived.get(body);
            if (arrival != null) {
                arrival.countDown();
            }
            CountDownLatch release = releases.get(body);
            if (release != null) {
                try {
                    if (!release.await(30, TimeUnit.SECONDS)) {
                        throw new IllegalStateException("Timed out waiting for release of: " + body);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            return replies.apply(message);
        }

        @Override
        public org.citrusframework.endpoint.Endpoint getEndpoint() {
            return null;
        }

        @Override
        public org.citrusframework.endpoint.EndpointConfiguration getEndpointConfiguration() {
            return null;
        }
    }
}
