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

import java.util.ArrayList;
import java.util.List;

import org.citrusframework.http.message.HttpMessage;
import org.citrusframework.message.Message;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

/**
 * Interceptor chain ordering follows Spring's {@code HandlerExecutionChain}: {@code preHandle}
 * in registration order, {@code postHandle} and {@code afterCompletion} in reverse, with
 * short-circuit and failure paths always completing previously applied interceptors.
 */
public class VertxHttpInterceptorTest {

    @Test
    public void chainRunsPrePostAndCompletionInSpringOrder() throws Exception {
        List<String> events = new ArrayList<>();
        VertxHttpInterceptor first = recording("first", events);
        VertxHttpInterceptor second = recording("second", events);
        HttpMessage request = request("/api/orders");
        Message handlerReply = new HttpMessage("handled");

        Message reply = VertxHttpInterceptor.invoke(List.of(first, second), request,
                actual -> {
                    events.add("handler");
                    return handlerReply;
                });

        assertSame(reply, handlerReply);
        assertEquals(events, List.of("pre:first", "pre:second", "handler",
                "post:second", "post:first", "completion:second:handled", "completion:first:handled"));
    }

    @Test
    public void shortCircuitSkipsHandlerAndPostButCompletesApplied() throws Exception {
        List<String> events = new ArrayList<>();
        Message shortCircuit = new HttpMessage("denied");
        VertxHttpInterceptor first = recording("first", events);
        VertxHttpInterceptor blocker = new VertxHttpInterceptor() {
            @Override
            public Message preHandle(HttpMessage request) {
                events.add("pre:blocker");
                return shortCircuit;
            }

            @Override
            public void afterCompletion(HttpMessage request, Message reply, Exception error) {
                events.add("completion:blocker:" + ((HttpMessage) reply).getPayload(String.class));
            }
        };
        VertxHttpInterceptor second = recording("second", events);

        Message reply = VertxHttpInterceptor.invoke(List.of(first, blocker, second),
                request("/api/orders"), actual -> {
                    fail("handler must not run on short-circuit");
                    return null;
                });

        assertSame(reply, shortCircuit);
        assertEquals(events, List.of("pre:first", "pre:blocker",
                "completion:blocker:denied", "completion:first:denied"));
    }

    @Test
    public void handlerFailureCompletesWithErrorAndPropagates() {
        List<String> events = new ArrayList<>();
        IllegalStateException failure = new IllegalStateException("boom");
        VertxHttpInterceptor first = recording("first", events);

        try {
            VertxHttpInterceptor.invoke(List.of(first), request("/api/orders"), actual -> {
                throw failure;
            });
            fail("handler failure must propagate");
        } catch (Exception e) {
            assertSame(e, failure);
        }

        assertEquals(events, List.of("pre:first", "completion-error:first:boom"));
    }

    @Test
    public void throwingPreHandleCompletesOnlyPreviouslyApplied() {
        List<String> events = new ArrayList<>();
        IllegalStateException failure = new IllegalStateException("pre-boom");
        VertxHttpInterceptor first = recording("first", events);
        VertxHttpInterceptor broken = new VertxHttpInterceptor() {
            @Override
            public Message preHandle(HttpMessage request) {
                throw failure;
            }
        };
        VertxHttpInterceptor second = recording("second", events);

        try {
            VertxHttpInterceptor.invoke(List.of(first, broken, second), request("/api/orders"),
                    actual -> {
                        fail("handler must not run when preHandle throws");
                        return null;
                    });
            fail("preHandle failure must propagate");
        } catch (Exception e) {
            assertSame(e, failure);
        }

        assertEquals(events, List.of("pre:first", "completion-error:first:pre-boom"));
    }

    @Test
    public void mappedInterceptorAppliesOnlyToMatchingPaths() throws Exception {
        List<String> events = new ArrayList<>();
        VertxHttpInterceptor scoped = VertxMappedInterceptor.include("/api/**", recording("api", events));

        VertxHttpInterceptor.invoke(List.of(scoped), request("/api/orders"), actual -> new HttpMessage("ok"));
        assertTrue(events.contains("pre:api"));

        events.clear();
        VertxHttpInterceptor.invoke(List.of(scoped), request("/other"), actual -> new HttpMessage("ok"));
        assertTrue(events.isEmpty());
    }

    @Test
    public void mappedInterceptorExcludeWinsOverInclude() throws Exception {
        List<String> events = new ArrayList<>();
        VertxHttpInterceptor scoped = VertxMappedInterceptor.include(
                List.of("/api/**"), List.of("/api/public/**"), recording("api", events));

        VertxHttpInterceptor.invoke(List.of(scoped), request("/api/public/status"),
                actual -> new HttpMessage("ok"));
        assertTrue(events.isEmpty());

        VertxHttpInterceptor.invoke(List.of(scoped), request("/api/orders"),
                actual -> new HttpMessage("ok"));
        assertTrue(events.contains("pre:api"));
    }

    @Test
    public void emptyIncludeListMatchesEveryPath() {
        assertTrue(VertxMappedInterceptor.include(List.of(), List.of(), new VertxHttpInterceptor() { })
                .matches(request("/anything")));
    }

    @Test
    public void postHandleMayReplaceTheReply() throws Exception {
        VertxHttpInterceptor replacer = new VertxHttpInterceptor() {
            @Override
            public Message postHandle(HttpMessage request, Message reply) {
                return new HttpMessage("replaced");
            }
        };

        Message reply = VertxHttpInterceptor.invoke(List.of(replacer), request("/api/orders"),
                actual -> new HttpMessage("original"));

        assertEquals(((HttpMessage) reply).getPayload(String.class), "replaced");
    }

    @Test
    public void emptyChainInvokesHandlerDirectly() throws Exception {
        Message handlerReply = new HttpMessage("direct");
        Message reply = VertxHttpInterceptor.invoke(List.of(), request("/api/orders"),
                actual -> handlerReply);
        assertSame(reply, handlerReply);
    }

    private static VertxHttpInterceptor recording(String name, List<String> events) {
        return new VertxHttpInterceptor() {
            @Override
            public Message preHandle(HttpMessage request) {
                events.add("pre:" + name);
                return null;
            }

            @Override
            public Message postHandle(HttpMessage request, Message reply) {
                events.add("post:" + name);
                return reply;
            }

            @Override
            public void afterCompletion(HttpMessage request, Message reply, Exception error) {
                if (error == null) {
                    events.add("completion:" + name + ":"
                            + ((HttpMessage) reply).getPayload(String.class));
                } else {
                    events.add("completion-error:" + name + ":" + error.getMessage());
                }
            }
        };
    }

    private static HttpMessage request(String path) {
        HttpMessage request = new HttpMessage();
        request.path(path);
        return request;
    }
}
