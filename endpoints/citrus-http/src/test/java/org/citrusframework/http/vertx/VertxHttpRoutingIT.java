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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.citrusframework.base.server.AbstractServer;
import org.citrusframework.http.message.HttpMessage;
import org.citrusframework.http.server.HttpServer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

/** Socket-level comparison of context paths and Citrus dispatcher URL mappings. */
public class VertxHttpRoutingIT {
    private static final HttpClient CLIENT = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1).followRedirects(HttpClient.Redirect.NEVER).build();

    @DataProvider
    public Object[][] routes() {
        List<Object[]> cases = new ArrayList<>();
        for (String engine : List.of("jetty", "vertx")) {
            for (Object[] route : new Object[][] {
                    {"/", "/*", "/orders", 200},
                    {"/api", "/*", "/api/orders?key=a%20b&key=c&flag", 200},
                    {"/api", "/*", "/other/orders", 404},
                    {"/api", "/*", "/apix/orders", 404},
                    {"/api", "/*", "/api?x=1", 301},
                    {"/api", "/orders/*", "/api/orders", 200},
                    {"/api", "/orders/*", "/api/orders/42", 200},
                    {"/api", "/orders/*", "/api/ordersXYZ", 404},
                    {"/api", "/orders/*", "/api/other", 404},
                    {"/api", "/orders", "/api/orders", 200},
                    {"/api", "/orders", "/api/orders/42", 404},
                    {"/api", "*.json", "/api/nested/order.json", 200},
                    {"/api", "*.json", "/api/order.json/extra", 404},
                    {"/api", "/", "/api/anything", 200},
                    {"/api", "", "/api/", 200},
                    {"/api", "", "/api/other", 404},
                    {"/api/v1", "/*", "/api/v1/orders", 200},
                    {"/api", "/orders/*", "/api/orders;key=a,b/42", "jetty".equals(engine) ? 500 : 200},
                    {"/api", "/hello world", "/api/hello%20world", 200}
            }) cases.add(new Object[] {engine, route[0], route[1], route[2], route[3]});
        }
        return cases.toArray(Object[][]::new);
    }

    @Test
    public void settingsAreCapturedUntilRestart() throws Exception {
        VertxHttpServer server = new VertxHttpServer();
        server.setPort(0);
        server.setContextPath("/api");
        server.setServletMappingPath("/orders/*");
        server.setEndpointAdapter(VertxPrototypeAdapters.delegate(message ->
                new HttpMessage(((HttpMessage) message).getContextPath())));
        server.initialize(); server.start();
        try {
            server.setContextPath("/next");
            server.setServletMappingPath("/items/*");
            assertEquals(get(server, "/api/orders/42").body(), "/api");
            assertEquals(get(server, "/next/items/42").statusCode(), 404);
            assertEquals(get(server, "/api/%2e%2e/secret").statusCode(), 400);
            server.stop(); server.start();
            assertEquals(get(server, "/api/orders/42").statusCode(), 404);
            assertEquals(get(server, "/next/items/42").body(), "/next");
            assertTrue(server.getFailures().isEmpty());
        } finally { server.stop(); server.destroy(); }
    }

    private static HttpResponse<String> get(VertxHttpServer server, String path) throws Exception {
        return CLIENT.send(HttpRequest.newBuilder(URI.create("http://127.0.0.1:"
                + server.getActualPort() + path)).timeout(Duration.ofSeconds(3)).GET().build(),
                HttpResponse.BodyHandlers.ofString());
    }

    @Test(dataProvider = "routes")
    public void routeSelectionAndCitrusHeaders(String engine, String context, String mapping,
                                              String path, int expectedStatus) throws Exception {
        AtomicReference<HttpMessage> received = new AtomicReference<>();
        AbstractServer server;
        ServerConnector connector = null;
        if ("jetty".equals(engine)) {
            HttpServer jetty = new HttpServer();
            connector = new ServerConnector(new Server());
            connector.setHost("127.0.0.1"); connector.setPort(0);
            jetty.setConnector(connector); jetty.setContextPath(context); jetty.setServletMappingPath(mapping);
            server = jetty;
        } else {
            VertxHttpServer vertx = new VertxHttpServer();
            vertx.setPort(0); vertx.setContextPath(context); vertx.setServletMappingPath(mapping);
            server = vertx;
        }
        server.setEndpointAdapter(VertxPrototypeAdapters.delegate(message -> {
            received.set((HttpMessage) message);
            return new HttpMessage("matched");
        }));
        server.initialize(); server.start();
        int port = connector == null ? ((VertxHttpServer) server).getActualPort() : connector.getLocalPort();
        try {
            var response = CLIENT.send(HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + path))
                    .timeout(Duration.ofSeconds(3)).GET().build(), HttpResponse.BodyHandlers.ofString());
            assertEquals(response.statusCode(), expectedStatus, engine + " " + context + " " + mapping + " " + path);
            if (expectedStatus == 200) {
                assertEquals(response.body(), "matched");
                assertNotNull(received.get());
                assertEquals(received.get().getContextPath(), "/".equals(context) ? "" : context);
                String expectedPath = URI.create(path).getPath().replace(";key=a,b", "");
                assertEquals(received.get().getPath(), expectedPath);
                assertEquals(received.get().getUri(), expectedPath);
                if (path.contains("?key=")) assertEquals(received.get().getQueryParamString(), "key=a%20b,key=c,flag");
            } else {
                assertNull(received.get(), "Unmatched or redirected request reached Citrus");
                if (expectedStatus == 301) {
                    URI location = URI.create(response.headers().firstValue("Location").orElseThrow());
                    assertEquals(location.getPath(), context + "/");
                    assertEquals(location.getRawQuery(), "x=1");
                }
            }
        } finally { server.stop(); server.destroy(); }
    }
}
