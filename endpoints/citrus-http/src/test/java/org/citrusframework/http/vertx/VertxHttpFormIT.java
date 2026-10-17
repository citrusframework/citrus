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
import java.util.concurrent.atomic.AtomicReference;

import org.citrusframework.base.server.AbstractServer;
import org.citrusframework.http.message.HttpMessage;
import org.citrusframework.http.server.HttpServer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import static org.testng.Assert.*;

public class VertxHttpFormIT {
    @DataProvider
    public Object[][] cases() {
        return new Object[][] {{"jetty", "form-read"}, {"vertx", "form-read"},
                {"jetty", "form-read-charset"}, {"vertx", "form-read-charset"},
                {"jetty", "form-write"}, {"vertx", "form-write"},
                {"jetty", "multipart-read"}, {"vertx", "multipart-read"},
                {"jetty", "multipart-write"}, {"vertx", "multipart-write"}};
    }

    @Test
    public void conversionFailuresAndUploadLimitKeepTheirHttpStatus() throws Exception {
        VertxHttpServer server = new VertxHttpServer(); server.setPort(0); server.setMaxBodySizeBytes(128);
        var parts = new LinkedMultiValueMap<String, Object>(); parts.add("unsupported", new Object());
        var calls = new java.util.concurrent.atomic.AtomicInteger();
        server.setEndpointAdapter(VertxPrototypeAdapters.delegate(message -> {
            calls.incrementAndGet(); return new HttpMessage(parts).contentType("multipart/form-data");
        }));
        server.initialize(); server.start();
        try {
            var client = HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1).build();
            String[] types = {"application/x-www-form-urlencoded", "multipart/form-data;boundary=fixture", "text/plain"};
            String[] bodies = {"name=%GG", "x".repeat(129), "valid"};
            int[] statuses = {400, 413, 500};
            for (int i = 0; i < types.length; i++) {
                var response = client.send(HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + server.getActualPort() + "/forms"))
                        .timeout(Duration.ofSeconds(3)).header("Content-Type", types[i])
                        .POST(HttpRequest.BodyPublishers.ofString(bodies[i])).build(), HttpResponse.BodyHandlers.ofString());
                assertEquals(response.statusCode(), statuses[i]);
                assertEquals(response.body(), "");
            }
            assertEquals(calls.get(), 1);
            assertEquals(server.getFailures().size(), 2);
        } finally { server.stop(); server.destroy(); }
    }

    @Test(dataProvider = "cases")
    public void formAndMultipartConversion(String engine, String scenario) throws Exception {
        AtomicReference<HttpMessage> received = new AtomicReference<>();
        var fields = new LinkedMultiValueMap<String, Object>();
        fields.add("name", "a b"); fields.add("name", "c+d"); fields.add("empty", "");
        HttpMessage reply = new HttpMessage("ok");
        if (scenario.equals("form-write")) reply = new HttpMessage(fields).contentType("application/x-www-form-urlencoded");
        if (scenario.equals("multipart-write")) {
            fields.add("file", new ByteArrayResource(new byte[] {0, (byte) 255, 13, 10, 42}) {
                @Override public String getFilename() { return "sample.bin"; }
            });
            var partHeaders = new org.springframework.http.HttpHeaders();
            partHeaders.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            partHeaders.add("X-Part", "retained");
            fields.add("bytes", new org.springframework.http.HttpEntity<>(new byte[] {1, 2, 3}, partHeaders));
            reply = new HttpMessage(fields).contentType("multipart/form-data");
        }
        final HttpMessage responseMessage = reply;
        AbstractServer server;
        ServerConnector connector = null;
        if (engine.equals("jetty")) {
            HttpServer jetty = new HttpServer();
            connector = new ServerConnector(new Server()); connector.setHost("127.0.0.1"); connector.setPort(0);
            jetty.setConnector(connector); server = jetty;
        } else { VertxHttpServer vertx = new VertxHttpServer(); vertx.setPort(0); server = vertx; }
        server.setEndpointAdapter(VertxPrototypeAdapters.delegate(message -> {
            received.set((HttpMessage) message); return responseMessage;
        }));
        server.initialize(); server.start();
        try {
            int port = connector == null ? ((VertxHttpServer) server).getActualPort() : connector.getLocalPort();
            String multipart = "--fixture\r\nContent-Disposition: form-data; name=\"file\"; filename=\"sample.txt\"\r\n"
                    + "Content-Type: text/plain\r\n\r\nattachment-content\r\n--fixture--\r\n";
            String body = scenario.equals("multipart-read") ? multipart : "name=a+b&name=c%2Bd&empty=&flag";
            String type = scenario.equals("multipart-read") ? "multipart/form-data;boundary=fixture" : "application/x-www-form-urlencoded";
            if (scenario.equals("form-read-charset")) type += ";charset=UTF-8";
            var client = HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1).build();
            var response = client.send(HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + "/forms"))
                    .timeout(Duration.ofSeconds(4)).header("Content-Type", type)
                    .POST(HttpRequest.BodyPublishers.ofString(body)).build(), HttpResponse.BodyHandlers.ofByteArray());
            assertEquals(response.statusCode(), 200, engine + " " + scenario);
            if (scenario.equals("form-read-charset") && engine.equals("jetty")) {
                assertTrue(received.get().getPayload() instanceof String);
            } else if (scenario.startsWith("form-read")) {
                assertTrue(received.get().getPayload() instanceof MultiValueMap, engine);
                var form = (MultiValueMap<?, ?>) received.get().getPayload();
                assertEquals(form.get("name"), engine.equals("jetty") ? List.of("a b") : List.of("a b", "c+d"), engine + " " + form);
                assertEquals(form.get("empty").get(0), "");
                assertTrue(form.containsKey("flag"));
                assertEquals(form.get("flag").get(0), engine.equals("jetty") ? "" : null);
            } else if (scenario.equals("form-write")) {
                assertEquals(new String(response.body(), StandardCharsets.UTF_8), "name=a+b&name=c%2Bd&empty=");
            } else if (scenario.equals("multipart-read")) {
                assertEquals(received.get().getPayload(), multipart);
            } else {
                String responseType = response.headers().firstValue("Content-Type").orElseThrow();
                String boundary = MediaType.parseMediaType(responseType).getParameter("boundary");
                assertNotNull(boundary, engine);
                String wire = new String(response.body(), StandardCharsets.ISO_8859_1);
                assertTrue(wire.startsWith("--" + boundary + "\r\n"));
                assertTrue(wire.endsWith("--" + boundary + "--\r\n"));
                assertTrue(wire.contains("X-Part: retained"));
                assertTrue(wire.contains("filename=\"sample.bin\""));
                assertTrue(wire.contains(new String(new byte[] {0, (byte) 255, 13, 10, 42}, StandardCharsets.ISO_8859_1)));
                assertEquals(wire.split("name=\"name\"", -1).length - 1, 2);
            }
        } finally { server.stop(); server.destroy(); }
    }
}
