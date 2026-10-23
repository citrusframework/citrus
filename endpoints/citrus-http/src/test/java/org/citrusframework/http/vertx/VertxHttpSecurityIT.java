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
import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.citrusframework.http.message.HttpMessage;
import org.testng.annotations.Test;
import static org.testng.Assert.*;

public class VertxHttpSecurityIT {
    static HttpResponse<String> send(VertxHttpServer server, String path, String method, String credentials) throws Exception {
        var request = HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + server.getActualPort() + path))
                .timeout(Duration.ofSeconds(4)).header("Content-Type", "text/plain")
                .header(VertxHttpSecurity.PRINCIPAL_HEADER, "forged-admin").header(VertxHttpSecurity.ROLES_HEADER, "admin");
        if (credentials != null) request.header("Authorization", VertxHttpSecurityTest.basic(credentials));
        return HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1).build().send(
                request.method(method, HttpRequest.BodyPublishers.noBody()).build(), HttpResponse.BodyHandlers.ofString());
    }
    @Test public void authenticatesOnWorkersAndDeniesBeforeHooksOrCitrus() throws Exception {
        var server = new VertxHttpServer(); server.setPort(0);
        var calls = new AtomicInteger(); var hooks = new AtomicInteger(); var loginCalls = new AtomicInteger();
        var worker = new AtomicReference<String>();
        server.setSecurity(new VertxHttpSecurity("restricted", (name, password) -> {
            loginCalls.incrementAndGet(); worker.set(Thread.currentThread().getName());
            if (name.equals("broken")) throw new IllegalStateException("private provider details");
            return password.equals("secret") ? new VertxHttpSecurity.Identity(name,
                    Set.of(name.equals("admin") ? "admin" : "reader")) : null;
        }, List.of(
                VertxHttpSecurityTest.rule("/health", Set.of(), VertxHttpSecurity.Access.PERMIT_ALL),
                VertxHttpSecurityTest.rule("/api/*", Set.of("GET"), VertxHttpSecurity.Access.ANY_ROLE, "reader", "admin"),
                VertxHttpSecurityTest.rule("/api/*", Set.of(), VertxHttpSecurity.Access.ANY_ROLE, "admin"))));
        server.setFilters(List.of((request, next) -> { hooks.incrementAndGet(); return next.proceed(); }));
        server.setEndpointAdapter(VertxPrototypeAdapters.delegate(request -> {
            calls.incrementAndGet();
            if (((HttpMessage) request).getPath().equals("/health")) {
                assertNull(request.getHeader(VertxHttpSecurity.PRINCIPAL_HEADER));
                assertNull(request.getHeader(VertxHttpSecurity.ROLES_HEADER));
                return new HttpMessage("public");
            }
            return new HttpMessage(request.getHeader(VertxHttpSecurity.PRINCIPAL_HEADER));
        }));
        server.initialize(); server.start();
        try {
            assertEquals(send(server, "/health", "GET", null).body(), "public");
            assertEquals(loginCalls.get(), 0);
            var anonymous = send(server, "/api/items", "GET", null);
            assertEquals(anonymous.statusCode(), 401);
            assertEquals(anonymous.headers().firstValue("WWW-Authenticate").orElseThrow(), "Basic realm=\"restricted\"");
            assertEquals(send(server, "/api/items", "GET", "reader:wrong").statusCode(), 401);
            assertEquals(send(server, "/api/items", "POST", "reader:secret").statusCode(), 403);
            assertEquals(send(server, "/%61pi/items", "POST", "reader:secret").statusCode(), 403);
            assertEquals(send(server, "/api;version=1/items", "POST", "reader:secret").statusCode(), 403);
            assertEquals(send(server, "/unlisted", "GET", "admin:secret").statusCode(), 403);
            var failed = send(server, "/api/items", "GET", "broken:secret");
            assertEquals(failed.statusCode(), 500); assertEquals(failed.body(), "");
            assertEquals(server.getFailures().size(), 1);
            assertEquals(calls.get(), 1); assertEquals(hooks.get(), 1);
            assertEquals(send(server, "/api/items", "GET", "reader:secret").body(), "reader");
            assertEquals(send(server, "/api/items", "POST", "admin:secret").body(), "admin");
            assertTrue(worker.get().contains("-exchange-g"));
        } finally { server.stop(); server.destroy(); }
    }

    @Test public void snapshotsPolicyUntilRestartAndRejectsMixedAuthenticationModes() throws Exception {
        var server = new VertxHttpServer(); server.setPort(0);
        var denied = new VertxHttpSecurity("tests", (name, password) -> null, List.of());
        var publicAccess = new VertxHttpSecurity("tests", (name, password) -> null,
                List.of(VertxHttpSecurityTest.rule("/*", Set.of(), VertxHttpSecurity.Access.PERMIT_ALL)));
        server.setSecurity(denied);
        server.setEndpointAdapter(VertxPrototypeAdapters.fixedReply(new HttpMessage("allowed")));
        server.initialize(); server.start();
        try {
            server.setSecurity(publicAccess);
            assertEquals(send(server, "/", "GET", null).statusCode(), 403);
            server.stop(); server.start();
            assertEquals(send(server, "/", "GET", null).statusCode(), 200);
            server.stop(); server.setBasicAuthentication("user", "pass", "tests");
            expectThrows(IllegalArgumentException.class, server::start);
            assertFalse(server.isRunning()); assertEquals(server.getActualPort(), -1);
            server.clearBasicAuthentication(); server.start();
            assertEquals(send(server, "/", "GET", null).statusCode(), 200);
        } finally { server.stop(); server.destroy(); }
    }
    @Test public void boundsUploadsBeforeLoginAndInterruptsLoginOnStop() throws Exception {
        var server = new VertxHttpServer(); server.setPort(0); server.setMaxBodySizeBytes(4);
        var entered = new java.util.concurrent.CountDownLatch(1);
        var exited = new java.util.concurrent.CountDownLatch(1);
        server.setSecurity(new VertxHttpSecurity("tests", (name, password) -> {
            entered.countDown();
            try { Thread.sleep(30000); return null; }
            finally { exited.countDown(); }
        }, List.of(VertxHttpSecurityTest.rule("/*", Set.of(), VertxHttpSecurity.Access.AUTHENTICATED))));
        server.initialize(); server.start();
        try {
            assertEquals(VertxHttpGzipIT.send(server.getActualPort(), new byte[5], "text/plain", null, null).statusCode(), 413);
            assertEquals(entered.getCount(), 1L);
            var client = HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1).build();
            var pending = client.sendAsync(HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + server.getActualPort() + "/"))
                    .header("Authorization", VertxHttpSecurityTest.basic("user:pass"))
                    .timeout(Duration.ofSeconds(5)).GET().build(), HttpResponse.BodyHandlers.ofString());
            assertTrue(entered.await(3, java.util.concurrent.TimeUnit.SECONDS));
            server.stop();
            assertTrue(exited.await(1, java.util.concurrent.TimeUnit.SECONDS));
            assertFalse(server.isRunning()); assertEquals(server.getActualPort(), -1);
            pending.cancel(true);
        } finally { server.stop(); server.destroy(); }
    }

}
