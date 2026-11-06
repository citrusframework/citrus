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
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.citrusframework.http.message.HttpMessage;
import org.springframework.http.HttpStatus;
import org.testng.annotations.Test;
import static org.testng.Assert.*;

public class VertxHttpPipelineIT {
    static HttpResponse<byte[]> send(VertxHttpServer server, String path, String method, String accept) throws Exception {
        var request = HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + server.getActualPort() + path))
                .timeout(Duration.ofSeconds(4)).header("Content-Type", "text/plain");
        if (accept != null) request.header("Accept-Encoding", accept);
        return HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1).build().send(
                request.method(method, HttpRequest.BodyPublishers.noBody()).build(), HttpResponse.BodyHandlers.ofByteArray());
    }

    @Test public void wrapsCustomHandlersAndAdapterRepliesWithNormalFraming() throws Exception {
        var server = new VertxHttpServer(); server.setPort(0);
        var fallbackCalls = new AtomicInteger();
        var thread = new AtomicReference<String>();
        server.setEndpointAdapter(VertxPrototypeAdapters.delegate(request -> {
            fallbackCalls.incrementAndGet(); return new HttpMessage("fallback");
        }));
        server.setFilters(List.of((request, next) -> {
            thread.set(Thread.currentThread().getName());
            if (request.getPath().equals("/denied")) return new HttpMessage("denied").status(HttpStatus.FORBIDDEN);
            var reply = new HttpMessage(next.proceed()); reply.setHeader("X-Filtered", "yes"); return reply;
        }));
        server.setHandlers(List.of(new VertxHttpHandlerMapping(request -> request.getPath().equals("/custom"),
                request -> new HttpMessage("custom").status(HttpStatus.CREATED))));
        server.initialize(); server.start();
        try {
            var custom = send(server, "/custom", "GET", "gzip");
            assertEquals(custom.statusCode(), 201);
            assertEquals(new String(VertxHttpGzipIT.ungzip(custom.body()), StandardCharsets.UTF_8), "custom");
            assertEquals(custom.headers().firstValue("X-Filtered").orElseThrow(), "yes");
            assertTrue(thread.get().contains("-exchange-g"));
            assertEquals(send(server, "/denied", "GET", null).statusCode(), 403);
            assertEquals(fallbackCalls.get(), 0);
            assertEquals(new String(send(server, "/other", "GET", null).body(), StandardCharsets.UTF_8), "fallback");
            assertEquals(fallbackCalls.get(), 1);
            var head = send(server, "/custom", "HEAD", null);
            assertEquals(head.statusCode(), 201); assertEquals(head.body().length, 0);
            assertEquals(head.headers().firstValue("Content-Length").orElseThrow(), "6");
            assertTrue(server.getCachedResponses().values().stream().anyMatch(plan -> plan.statusCode() == 403));
        } finally { server.stop(); server.destroy(); }
    }

    @Test public void reportsExceptionsAndPreventsRepeatedAdapterCalls() throws Exception {
        var server = new VertxHttpServer(); server.setPort(0);
        var calls = new AtomicInteger();
        server.setEndpointAdapter(VertxPrototypeAdapters.delegate(request -> { calls.incrementAndGet(); return new HttpMessage("ok"); }));
        server.setFilters(List.of((request, next) -> {
            if (request.getPath().equals("/filter-error")) throw new IllegalStateException("private filter detail");
            var reply = next.proceed();
            if (request.getPath().equals("/twice")) next.proceed();
            return reply;
        }));
        server.setHandlers(List.of(new VertxHttpHandlerMapping(request -> request.getPath().equals("/handler-error"),
                request -> { throw new IllegalStateException("private handler detail"); })));
        server.initialize(); server.start();
        try {
            for (String path : List.of("/filter-error", "/handler-error", "/twice")) {
                var reply = send(server, path, "GET", null);
                assertEquals(reply.statusCode(), 500); assertEquals(reply.body().length, 0);
            }
            assertEquals(calls.get(), 1);
            assertEquals(server.getFailures().size(), 3);
            assertEquals(send(server, "/healthy", "GET", null).statusCode(), 200);
        } finally { server.stop(); server.destroy(); }
    }

    @Test public void snapshotsConfigurationUntilRestart() throws Exception {
        var server = new VertxHttpServer(); server.setPort(0);
        server.setFilters(List.of((request, next) -> new HttpMessage("first")));
        server.initialize(); server.start();
        try {
            server.setFilters(List.of((request, next) -> next.proceed()));
            server.setHandlers(List.of(new VertxHttpHandlerMapping(request -> true, request -> new HttpMessage("second"))));
            assertEquals(new String(send(server, "/", "GET", null).body(), StandardCharsets.UTF_8), "first");
            server.stop(); server.start();
            assertEquals(new String(send(server, "/", "GET", null).body(), StandardCharsets.UTF_8), "second");
        } finally { server.stop(); server.destroy(); }
    }

    @Test public void authenticatesAndRoutesBeforeInvokingFilters() throws Exception {
        var server = new VertxHttpServer(); server.setPort(0); server.setContextPath("/api");
        server.setBasicAuthentication("user", "pass", "tests");
        var calls = new AtomicInteger();
        server.setFilters(List.of((request, next) -> { calls.incrementAndGet(); return new HttpMessage("allowed"); }));
        server.initialize(); server.start();
        try {
            assertEquals(send(server, "/outside", "GET", null).statusCode(), 404);
            assertEquals(send(server, "/api/private", "GET", null).statusCode(), 401);
            assertEquals(calls.get(), 0);
        } finally { server.stop(); server.destroy(); }
    }
    @Test public void enforcesBodyLimitsBeforeFiltersAndInterruptsBlockedFiltersOnStop() throws Exception {
        var server = new VertxHttpServer(); server.setPort(0); server.setMaxBodySizeBytes(4);
        var entered = new java.util.concurrent.CountDownLatch(1);
        var exited = new java.util.concurrent.CountDownLatch(1);
        server.setFilters(List.of((request, next) -> {
            entered.countDown();
            try { Thread.sleep(30000); return new HttpMessage("late"); }
            finally { exited.countDown(); }
        }));
        server.initialize(); server.start();
        try {
            assertEquals(VertxHttpGzipIT.send(server.getActualPort(), new byte[5], "text/plain", null, null).statusCode(), 413);
            assertEquals(entered.getCount(), 1L);
            var client = HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1).build();
            var pending = client.sendAsync(HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + server.getActualPort() + "/"))
                    .timeout(Duration.ofSeconds(5)).GET().build(), HttpResponse.BodyHandlers.ofByteArray());
            assertTrue(entered.await(3, java.util.concurrent.TimeUnit.SECONDS));
            server.stop();
            assertTrue(exited.await(1, java.util.concurrent.TimeUnit.SECONDS));
            assertFalse(server.isRunning()); assertEquals(server.getActualPort(), -1);
            pending.cancel(true);
        } finally { server.stop(); server.destroy(); }
    }

}
