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

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import org.citrusframework.http.message.HttpMessage;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.testng.annotations.Test;
import static org.testng.Assert.*;

public class VertxHttpCustomConverterIT {
    public record Payload(String value) { }

    public static class Converter extends AbstractHttpMessageConverter<Payload> {
        public Converter() { super(MediaType.parseMediaType("application/x-citrus-test")); }
        @Override protected boolean supports(Class<?> type) { return type == Payload.class; }
        @Override protected Payload readInternal(Class<? extends Payload> type, HttpInputMessage message) throws IOException {
            if (io.vertx.core.Context.isOnEventLoopThread()) throw new AssertionError("Converter ran on event loop");
            String value = new String(message.getBody().readAllBytes(), StandardCharsets.UTF_8);
            if (value.equals("bad-input")) throw new IOException("Invalid custom input");
            return new Payload("read:" + value);
        }
        @Override protected void writeInternal(Payload body, HttpOutputMessage message) throws IOException {
            if (io.vertx.core.Context.isOnEventLoopThread()) throw new AssertionError("Converter ran on event loop");
            if (body.value().equals("bad-output")) throw new IOException("Invalid custom output");
            message.getHeaders().set("X-Converter", "custom");
            message.getHeaders().setContentLength(9999);
            message.getHeaders().set("Transfer-Encoding", "chunked");
            message.getBody().write(("write:" + body.value()).getBytes(StandardCharsets.UTF_8));
        }
    }

    @Test
    public void customReadWriteErrorsAndRestart() throws Exception {
        VertxHttpServer server = new VertxHttpServer(); server.setPort(0);
        var registrations = new java.util.ArrayList<VertxMessageBodyConverter<?>>();
        registrations.add(new VertxMessageBodyConverter<>(Payload.class, new Converter()));
        server.setMessageBodyConverters(registrations);
        registrations.clear();
        AtomicReference<Object> received = new AtomicReference<>();
        AtomicReference<Object> reply = new AtomicReference<>(new Payload("answer"));
        server.setEndpointAdapter(VertxPrototypeAdapters.delegate(message -> {
            received.set(message.getPayload()); return new HttpMessage(reply.get()).contentType("application/x-citrus-test");
        }));
        server.initialize(); server.start();
        try {
            server.setMessageBodyConverters(List.of());
            var response = send(server, "hello", "POST", "application/x-citrus-test;charset=UTF-8");
            assertEquals(received.get(), new Payload("read:hello"));
            assertEquals(response.body(), "write:answer");
            assertEquals(response.headers().firstValue("X-Converter").orElseThrow(), "custom");
            assertEquals(response.headers().firstValue("Content-Length").orElseThrow(), "12");
            assertTrue(response.headers().firstValue("Transfer-Encoding").isEmpty());
            var head = send(server, "", "HEAD", "application/x-citrus-test");
            assertEquals(head.body(), "");
            assertEquals(head.headers().firstValue("Content-Length").orElseThrow(), "12");
            assertEquals(send(server, "bad-input", "POST", "application/x-citrus-test").statusCode(), 400);
            reply.set(new Payload("bad-output"));
            assertEquals(send(server, "ok", "POST", "application/x-citrus-test").statusCode(), 500);
            assertEquals(server.getFailures().size(), 2);
            reply.set("plain"); server.stop(); server.start();
            assertEquals(send(server, "hello", "POST", "text/plain").body(), "plain");
            assertEquals(received.get(), "hello");
        } finally { server.stop(); server.destroy(); }
    }

    static HttpResponse<String> send(VertxHttpServer server, String body, String method, String type) throws Exception {
        return HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1).build().send(
                HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + server.getActualPort() + "/custom"))
                        .timeout(Duration.ofSeconds(3)).header("Content-Type", type)
                        .method(method, HttpRequest.BodyPublishers.ofString(body)).build(), HttpResponse.BodyHandlers.ofString());
    }
}
