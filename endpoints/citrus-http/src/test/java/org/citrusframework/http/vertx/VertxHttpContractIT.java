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
import java.util.concurrent.atomic.AtomicReference;

import jakarta.servlet.http.Cookie;
import org.citrusframework.base.server.AbstractServer;
import org.citrusframework.http.message.HttpMessage;
import org.citrusframework.http.server.HttpServer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.springframework.http.HttpStatus;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

/** Identical HTTP/1.1 fixtures against both engines; no Spring support or global Citrus instance. */
public class VertxHttpContractIT {
    @DataProvider
    public Object[][] engines() { return new Object[][] {{"jetty"}, {"vertx"}}; }

    @Test(dataProvider = "engines")
    public void sharedPayloadStatusHeadCookieAndMetadataContract(String engine) throws Exception {
        AtomicReference<HttpMessage> received = new AtomicReference<>();
        AtomicReference<HttpMessage> reply = new AtomicReference<>();
        var adapter = VertxPrototypeAdapters.delegate(message -> {
            received.set((HttpMessage) message);
            return reply.get();
        });
        AbstractServer server;
        ServerConnector connector = null;
        if ("jetty".equals(engine)) {
            HttpServer jetty = new HttpServer();
            connector = new ServerConnector(new Server());
            connector.setHost("127.0.0.1"); connector.setPort(0);
            jetty.setConnector(connector); jetty.setHandleCookies(true);
            jetty.setDefaultStatusCode(202); server = jetty;
        } else {
            VertxHttpServer vertx = new VertxHttpServer();
            vertx.setPort(0); vertx.setHandleCookies(true); vertx.setDefaultStatusCode(202);
            server = vertx;
        }
        server.setEndpointAdapter(adapter); server.initialize(); server.start();
        int port = connector == null ? ((VertxHttpServer) server).getActualPort() : connector.getLocalPort();
        HttpClient client = HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1).build();
        try {
            byte[][] bodies = { "Grüße".getBytes(StandardCharsets.ISO_8859_1),
                    new byte[] {0, (byte) 255, 13, 10}, new byte[0], "Grüße 世界".getBytes(StandardCharsets.UTF_8) };
            String[] types = {"text/plain;charset=ISO-8859-1", "application/octet-stream", "text/plain", "text/plain;charset=UTF-8"};
            for (int i = 0; i < bodies.length; i++) {
                Object payload = i == 1 ? bodies[i] : new String(bodies[i], i == 3 ? StandardCharsets.UTF_8 : StandardCharsets.ISO_8859_1);
                reply.set(new HttpMessage(payload).status(HttpStatus.CREATED).contentType(types[i]));
                var response = client.send(HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port
                                + "/hello%20world;key=a,b?key=a%20b&key=c&flag"))
                        .timeout(Duration.ofSeconds(5)).header("Content-Type", types[i])
                        .POST(HttpRequest.BodyPublishers.ofByteArray(bodies[i])).build(), HttpResponse.BodyHandlers.ofByteArray());
                assertEquals(response.statusCode(), 201, engine);
                assertEquals(response.version(), HttpClient.Version.HTTP_1_1, engine);
                assertEquals(response.body(), bodies[i], engine);
                HttpMessage request = received.get();
                assertEquals(request.getPath(), "/hello world", engine);
                assertEquals(request.getUri(), "/hello world", engine);
                assertEquals(request.getContextPath(), "", engine);
                assertEquals(request.getVersion(), "HTTP/1.1", engine);
                assertEquals(request.getQueryParamString(), "key=a%20b,key=c,flag", engine);
                if (i == 0) assertEquals(request.getPayload(), "Grüße", engine);
                if (i == 3) assertEquals(request.getPayload(), "Grüße 世界", engine);
                if (i == 1) assertEquals((byte[]) request.getPayload(), bodies[i], engine);
            }
            for (String method : new String[] {"GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS", "HEAD", "TRACE"}) {
                received.set(null);
                reply.set(new HttpMessage("reply-" + method).status(HttpStatus.OK));
                var response = client.send(HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + "/methods"))
                        .timeout(Duration.ofSeconds(5)).method(method, HttpRequest.BodyPublishers.noBody()).build(),
                        HttpResponse.BodyHandlers.ofString());
                assertEquals(response.statusCode(), 200, engine + ": " + method);
                if ("jetty".equals(engine) && "TRACE".equals(method)) {
                    // The existing Servlet layer handles TRACE before the Citrus controller.
                    assertNull(received.get(), "Jetty TRACE must not be represented as a Citrus-controlled round trip");
                    assertTrue(response.body().startsWith("TRACE /methods HTTP/1.1"), response.body());
                    continue;
                }
                assertEquals(received.get().getRequestMethod().name(), method, engine);
                assertEquals(response.body(), "HEAD".equals(method) ? "" : "reply-" + method, engine);
            }
            reply.set(new HttpMessage("head-body"));
            var head = client.send(HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + "/head"))
                    .timeout(Duration.ofSeconds(5)).method("HEAD", HttpRequest.BodyPublishers.noBody()).build(),
                    HttpResponse.BodyHandlers.ofByteArray());
            assertEquals(head.statusCode(), 202, engine);
            assertEquals(head.body().length, 0, engine);
            assertEquals(head.headers().firstValue("Content-Length").orElseThrow(), "9", engine);
            reply.set(null);
            var empty = client.send(HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + "/empty"))
                    .timeout(Duration.ofSeconds(5)).GET().build(), HttpResponse.BodyHandlers.ofString());
            assertEquals(empty.statusCode(), 202, engine); assertEquals(empty.body(), "", engine);
            Cookie cookie = new Cookie("token", "value"); cookie.setMaxAge(0); cookie.setSecure(true);
            cookie.setHttpOnly(true); cookie.setAttribute("SameSite", "Strict");
            reply.set(new HttpMessage("cookie").cookie(cookie));
            var cookies = client.send(HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + "/cookie"))
                    .timeout(Duration.ofSeconds(5)).GET().build(), HttpResponse.BodyHandlers.ofString());
            String serialized = cookies.headers().firstValue("Set-Cookie").orElseThrow().toLowerCase();
            assertTrue(serialized.contains("max-age=0") || serialized.contains("expires=thu, 01 jan 1970"),
                    engine + ": deletion cookie must expire immediately: " + serialized);
            for (String attribute : new String[] {"secure", "httponly", "samesite=strict"})
                assertTrue(serialized.contains(attribute), engine + ": " + serialized);
        } finally { server.stop(); server.destroy(); }
    }
}
