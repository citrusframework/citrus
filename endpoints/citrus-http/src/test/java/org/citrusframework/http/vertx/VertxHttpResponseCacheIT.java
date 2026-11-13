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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.citrusframework.http.message.HttpMessage;
import org.testng.annotations.Test;
import static org.testng.Assert.*;

public class VertxHttpResponseCacheIT {
    @Test
    public void cacheIncludesAdmittedErrorsAndHeadButNotAuthenticationRejections() throws Exception {
        var server = new VertxHttpServer(); server.setPort(0); server.setResponseCacheSize(2);
        server.setMaxBodySizeBytes(1); server.setDefaultStatusCode(202);
        server.setBasicAuthentication("u", "p", "trace");
        server.setEndpointAdapter(VertxPrototypeAdapters.delegate(message -> null));
        server.initialize(); server.start();
        try {
            var client = HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1).build();
            URI uri = URI.create("http://127.0.0.1:" + server.getActualPort() + "/trace");
            var unauthorized = client.send(HttpRequest.newBuilder(uri).timeout(Duration.ofSeconds(3)).GET().build(),
                    HttpResponse.BodyHandlers.ofString());
            assertEquals(unauthorized.statusCode(), 401); assertTrue(server.getCachedResponses().isEmpty());
            var oversized = client.send(HttpRequest.newBuilder(uri).timeout(Duration.ofSeconds(3))
                    .header("Authorization", "Basic dTpw").POST(HttpRequest.BodyPublishers.ofString("xx")).build(),
                    HttpResponse.BodyHandlers.ofString());
            assertEquals(oversized.statusCode(), 413);
            var head = client.send(HttpRequest.newBuilder(uri).timeout(Duration.ofSeconds(3))
                    .header("Authorization", "Basic dTpw").method("HEAD", HttpRequest.BodyPublishers.noBody()).build(),
                    HttpResponse.BodyHandlers.ofString());
            assertEquals(head.statusCode(), 202);
            var cached = new ArrayList<>(server.getCachedResponses().values());
            assertEquals(cached.size(), 2); assertEquals(cached.get(0).statusCode(), 413);
            assertEquals(cached.get(1).statusCode(), 202); assertTrue(cached.get(1).suppressBody());
            assertEquals(cached.get(1).body().length, 0);
        } finally { server.stop(); server.destroy(); }
    }

    @Test
    public void tracesEachExchangeEvictsOldestAndClearsOnRestart() throws Exception {
        var server = new VertxHttpServer(); server.setPort(0); server.setResponseCacheSize(2);
        List<String> ids = new ArrayList<>();
        AtomicInteger calls = new AtomicInteger();
        server.setEndpointAdapter(VertxPrototypeAdapters.delegate(message -> {
            ids.add(message.getHeader(VertxHttpMessageMapper.EXCHANGE_ID_HEADER).toString());
            return new HttpMessage("reply-" + calls.incrementAndGet()).status(org.springframework.http.HttpStatus.CREATED).header("X-Trace", "test");
        }));
        server.initialize(); server.start();
        try {
            server.setResponseCacheSize(0);
            var client = HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1).build();
            for (int i = 1; i <= 3; i++) {
                var response = client.send(HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + server.getActualPort() + "/same"))
                        .timeout(Duration.ofSeconds(3)).GET().build(), HttpResponse.BodyHandlers.ofString());
                assertEquals(response.body(), "reply-" + i);
            }
            assertEquals(calls.get(), 3, "Trace cache must never serve a response instead of invoking Citrus");
            assertEquals(server.getCachedResponses().size(), 2);
            assertNull(server.getResponseCache(ids.get(0)));
            var cached = server.getResponseCache(ids.get(2));
            assertEquals(cached.statusCode(), 201);
            assertEquals(cached.headers().get("X-Trace"), List.of("test"));
            assertEquals(new String(cached.body(), StandardCharsets.UTF_8), "reply-3");
            cached.body()[0] = 'X';
            assertEquals(new String(server.getResponseCache(ids.get(2)).body(), StandardCharsets.UTF_8), "reply-3");
            server.stop();
            assertTrue(server.getCachedResponses().isEmpty());
            server.start();
            client.send(HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + server.getActualPort() + "/same"))
                    .timeout(Duration.ofSeconds(3)).GET().build(), HttpResponse.BodyHandlers.ofString());
            assertTrue(server.getCachedResponses().isEmpty(), "Zero capacity should disable caching after restart");
        } finally { server.stop(); server.destroy(); }
    }
}
