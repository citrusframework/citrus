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
import java.util.Base64;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.citrusframework.base.server.AbstractServer;
import org.citrusframework.http.message.HttpMessage;
import org.citrusframework.http.security.BasicAuthentication;
import org.citrusframework.http.server.HttpServer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import static org.testng.Assert.*;

public class VertxHttpBasicAuthIT {
    private static final HttpClient CLIENT = HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1).build();

    static String basic(String credentials) {
        return "Basic " + Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.US_ASCII));
    }

    @DataProvider
    public Object[][] credentials() {
        List<Object[]> result = new ArrayList<>();
        for (String engine : List.of("jetty", "vertx")) {
            for (Object[] row : new Object[][] {
                    {null, 401}, {basic("alice:secret:123"), 200}, {basic("alice:wrong"), 401},
                    {basic("other:secret:123"), 401}, {"Bearer token", 401}, {"Basic %%%", engine.equals("jetty") ? 500 : 401},
                    {basic("missing-colon"), 401}, {"basic " + basic("alice:secret:123").substring(6), 200}
            }) result.add(new Object[] {engine, row[0], row[1]});
        }
        return result.toArray(Object[][]::new);
    }

    @Test(dataProvider = "credentials")
    public void authenticationBeforeCitrus(String engine, String authorization, int status) throws Exception {
        AbstractServer server;
        ServerConnector connector = null;
        if (engine.equals("jetty")) {
            HttpServer jetty = new HttpServer();
            connector = new ServerConnector(new Server());
            connector.setHost("127.0.0.1"); connector.setPort(0);
            jetty.setConnector(connector); jetty.setContextPath("/api");
            jetty.setSecurityHandler(new BasicAuthentication("alice", "secret:123").realm("test-realm").getSecurityHandler("/*"));
            server = jetty;
        } else {
            VertxHttpServer vertx = new VertxHttpServer();
            vertx.setPort(0); vertx.setContextPath("/api");
            vertx.setBasicAuthentication("alice", "secret:123", "test-realm");
            server = vertx;
        }
        AtomicInteger calls = new AtomicInteger();
        server.setEndpointAdapter(VertxPrototypeAdapters.delegate(message -> {
            calls.incrementAndGet(); return new HttpMessage("authenticated");
        }));
        server.initialize(); server.start();
        try {
            int port = connector == null ? ((VertxHttpServer) server).getActualPort() : connector.getLocalPort();
            HttpResponse<String> response = send(port, authorization);
            assertEquals(response.statusCode(), status, engine);
            assertEquals(calls.get(), status == 200 ? 1 : 0);
            if (status == 401) {
                assertTrue(response.headers().firstValue("WWW-Authenticate").orElseThrow().contains("realm=\"test-realm\""));
            } else if (status == 200) assertEquals(response.body(), "authenticated");
        } finally { server.stop(); server.destroy(); }
    }

    @Test
    public void credentialsAreCapturedUntilRestart() throws Exception {
        VertxHttpServer server = new VertxHttpServer();
        server.setPort(0); server.setContextPath("/api");
        server.setBasicAuthentication("alice", "first", "original");
        server.setEndpointAdapter(VertxPrototypeAdapters.delegate(message -> new HttpMessage("ok")));
        server.initialize(); server.start();
        try {
            server.setBasicAuthentication("alice", "second", "changed");
            assertEquals(send(server.getActualPort(), basic("alice:first")).statusCode(), 200);
            var rejected = send(server.getActualPort(), basic("alice:second"));
            assertEquals(rejected.statusCode(), 401);
            assertEquals(rejected.headers().firstValue("WWW-Authenticate").orElseThrow(), "Basic realm=\"original\"");
            server.stop(); server.start();
            assertEquals(send(server.getActualPort(), basic("alice:first")).statusCode(), 401);
            assertEquals(send(server.getActualPort(), basic("alice:second")).statusCode(), 200);
            server.clearBasicAuthentication();
            assertEquals(send(server.getActualPort(), null).statusCode(), 401);
            server.stop(); server.start();
            assertEquals(send(server.getActualPort(), null).statusCode(), 200);
        } finally { server.stop(); server.destroy(); }
    }

    @Test
    public void rejectsIncompleteUploadsAndDuplicateCredentialsBeforeAdapter() throws Exception {
        VertxHttpServer server = new VertxHttpServer();
        server.setPort(0); server.setContextPath("/api");
        server.setBasicAuthentication("alice", "secret", "test");
        AtomicInteger calls = new AtomicInteger();
        server.setEndpointAdapter(VertxPrototypeAdapters.delegate(message -> {
            calls.incrementAndGet(); return new HttpMessage("ok");
        }));
        server.initialize(); server.start();
        try {
            for (String headers : List.of("", "Authorization: " + basic("alice:secret") + "\r\nAuthorization: "
                    + basic("alice:secret") + "\r\n")) {
                try (var socket = new java.net.Socket("127.0.0.1", server.getActualPort())) {
                    socket.setSoTimeout(2000);
                    socket.getOutputStream().write(("POST /api/orders HTTP/1.1\r\nHost: localhost\r\n"
                            + headers + "Content-Length: 100000\r\n\r\nx").getBytes(StandardCharsets.US_ASCII));
                    socket.getOutputStream().flush();
                    String response = new String(socket.getInputStream().readAllBytes(), StandardCharsets.US_ASCII);
                    assertTrue(response.startsWith("HTTP/1.1 401"));
                }
            }
            assertEquals(calls.get(), 0);
            assertEquals(send(server.getActualPort(), basic("alice:secret")).statusCode(), 200);
            assertTrue(server.getFailures().isEmpty());
        } finally { server.stop(); server.destroy(); }
    }

    static HttpResponse<String> send(int port, String authorization) throws Exception {
        var request = HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + "/api/orders"))
                .timeout(Duration.ofSeconds(3)).GET();
        if (authorization != null) request.header("Authorization", authorization);
        return CLIENT.send(request.build(), HttpResponse.BodyHandlers.ofString());
    }
}
