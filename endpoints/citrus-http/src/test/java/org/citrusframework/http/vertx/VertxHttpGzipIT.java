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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicReference;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import org.citrusframework.base.server.AbstractServer;
import org.citrusframework.http.message.HttpMessage;
import org.citrusframework.http.server.HttpServer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import static org.testng.Assert.*;

public class VertxHttpGzipIT {
    public static byte[] gzip(byte[] body) throws Exception {
        var bytes = new ByteArrayOutputStream();
        try (var gzip = new GZIPOutputStream(bytes)) { gzip.write(body); }
        return bytes.toByteArray();
    }
    public static byte[] ungzip(byte[] body) throws Exception {
        try (var gzip = new GZIPInputStream(new ByteArrayInputStream(body))) { return gzip.readAllBytes(); }
    }
    @DataProvider public Object[][] engines() { return new Object[][] {{"jetty"}, {"vertx"}}; }

    @Test(dataProvider = "engines")
    public void compressedRequestsAndReplies(String engine) throws Exception {
        AbstractServer server; ServerConnector connector = null;
        if (engine.equals("jetty")) {
            var jetty = new HttpServer(); connector = new ServerConnector(new Server());
            connector.setHost("127.0.0.1"); connector.setPort(0); jetty.setConnector(connector); server = jetty;
        } else { var vertx = new VertxHttpServer(); vertx.setPort(0); server = vertx; }
        AtomicReference<Object> received = new AtomicReference<>();
        server.setEndpointAdapter(VertxPrototypeAdapters.delegate(message -> {
            received.set(message.getPayload());
            return new HttpMessage(message.getPayload()).contentType(message.getHeader("Content-Type").toString());
        }));
        server.initialize(); server.start();
        try {
            int port = connector == null ? ((VertxHttpServer) server).getActualPort() : connector.getLocalPort();
            byte[][] bodies = {"Grüße 世界".getBytes(StandardCharsets.UTF_8), {0, (byte) 255, 1, 13, 10}};
            String[] types = {"text/plain;charset=UTF-8", "application/octet-stream"};
            for (int i = 0; i < bodies.length; i++) {
                var response = send(port, gzip(bodies[i]), types[i], "gzip", "gzip");
                assertEquals(response.statusCode(), 200, engine);
                assertEquals(response.headers().firstValue("Content-Encoding").orElseThrow(), "gzip");
                assertEquals(ungzip(response.body()), bodies[i]);
                if (i == 0) assertEquals(received.get(), new String(bodies[i], StandardCharsets.UTF_8));
                else assertEquals((byte[]) received.get(), bodies[i]);
            }
        } finally { server.stop(); server.destroy(); }
    }
    @Test
    public void rejectsInvalidAndExpandedUploadsBeforeCallingCitrus() throws Exception {
        var server = new VertxHttpServer(); server.setPort(0); server.setMaxBodySizeBytes(128);
        var calls = new java.util.concurrent.atomic.AtomicInteger();
        server.setEndpointAdapter(VertxPrototypeAdapters.delegate(message -> {
            calls.incrementAndGet(); return new HttpMessage("ok");
        }));
        server.initialize(); server.start();
        try {
            assertEquals(send(server.getActualPort(), new byte[] {1, 2}, "text/plain", "gzip", null).statusCode(), 400);
            assertEquals(send(server.getActualPort(), gzip(new byte[4096]), "text/plain", "gzip", null).statusCode(), 413);
            assertEquals(send(server.getActualPort(), new byte[0], "text/plain", "gzip, br", null).statusCode(), 415);
            byte[] corrupt = gzip("hello".getBytes(StandardCharsets.UTF_8)); corrupt[corrupt.length - 8] ^= 1;
            assertEquals(send(server.getActualPort(), corrupt, "text/plain", "gzip", null).statusCode(), 400);
            assertEquals(calls.get(), 0);
            var reply = send(server.getActualPort(), gzip("hello".getBytes(StandardCharsets.UTF_8)),
                    "text/plain", "GZip", "gzip;q=0, *;q=1");
            assertEquals(reply.statusCode(), 200);
            assertFalse(reply.headers().firstValue("Content-Encoding").isPresent());
            assertEquals(new String(reply.body(), StandardCharsets.UTF_8), "ok");
            assertEquals(calls.get(), 1);
        } finally { server.stop(); server.destroy(); }
    }

    static HttpResponse<byte[]> send(int port, byte[] body, String type, String encoding, String accept) throws Exception {
        var request = HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + "/gzip"))
                .timeout(Duration.ofSeconds(3)).header("Content-Type", type);
        if (encoding != null) request.header("Content-Encoding", encoding);
        if (accept != null) request.header("Accept-Encoding", accept);
        return HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1).build().send(
                request.POST(HttpRequest.BodyPublishers.ofByteArray(body)).build(), HttpResponse.BodyHandlers.ofByteArray());
    }
}
