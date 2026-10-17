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

package org.citrusframework.http.integration;

import org.citrusframework.TestAction;
import org.citrusframework.annotations.CitrusTest;
import org.citrusframework.dsl.TestActionSupport;
import org.citrusframework.http.client.HttpClient;
import org.citrusframework.http.client.HttpClientBuilder;
import org.citrusframework.http.vertx.VertxHttpServer;
import org.citrusframework.testng.spring.TestNGCitrusSpringSupport;
import org.springframework.http.HttpStatus;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Citrus-controlled wire matrix for the Vert.x HTTP server prototype (AC3): every request is
 * sent by the Citrus HTTP client, received and answered through Citrus server actions, and the
 * reply is validated by the Citrus HTTP client.
 */
@Test
public class VertxHttpServerRoundtripIT extends TestNGCitrusSpringSupport implements TestActionSupport {

    private static final String PATH = "/vertx-echo";

    private VertxHttpServer prototype;
    private HttpClient httpClient;

    @BeforeClass
    public void startPrototype() {
        prototype = new VertxHttpServer();
        prototype.setName("vertxRoundtrip");
        prototype.setPort(0);
        prototype.initialize();
        prototype.start();

        httpClient = new HttpClientBuilder()
                .requestUrl("http://127.0.0.1:" + prototype.getActualPort() + PATH)
                .initialize()
                .build();
    }

    @AfterClass(alwaysRun = true)
    public void stopPrototype() {
        if (prototype != null) {
            prototype.stop();
            prototype.destroy();
        }
    }

    @Test
    public void secureWebSocketUsesCitrusActions() throws Exception {
        var endpoint = new org.citrusframework.http.vertx.VertxWebSocketEndpoint("/ws");
        var server = new VertxHttpServer(); server.setPort(0); server.setWebSocketEndpoint(endpoint);
        server.setSecurity(new org.citrusframework.http.vertx.VertxHttpSecurity("ws", (name, password) ->
                password.equals("secret") ? new org.citrusframework.http.vertx.VertxHttpSecurity.Identity(name,
                        java.util.Set.of(name.equals("admin") ? "admin" : "reader")) : null,
                java.util.List.of(new org.citrusframework.http.vertx.VertxHttpSecurity.Rule("/ws", java.util.Set.of("GET"),
                        org.citrusframework.http.vertx.VertxHttpSecurity.Access.ANY_ROLE, java.util.Set.of("admin")))));
        try (var keys = new org.citrusframework.http.vertx.VertxTlsTestSupport()) {
            server.setTls(new org.citrusframework.http.vertx.VertxHttpTls(keys.server,
                    org.citrusframework.http.vertx.VertxTlsTestSupport.PASSWORD));
            server.initialize(); server.start();
            var client = java.net.http.HttpClient.newBuilder().sslContext(keys.context(keys.server, null)).build();
            try {
                var uri = java.net.URI.create("wss://localhost:" + server.getActualPort() + "/ws");
                for (String credentials : new String[]{"", "admin:wrong", "reader:secret"}) {
                    var builder = client.newWebSocketBuilder();
                    if (!credentials.isEmpty()) builder.header("Authorization", "Basic " + java.util.Base64.getEncoder()
                            .encodeToString(credentials.getBytes(java.nio.charset.StandardCharsets.US_ASCII)));
                    var failure = org.testng.Assert.expectThrows(java.util.concurrent.ExecutionException.class,
                            () -> builder.buildAsync(uri, new org.citrusframework.http.vertx.VertxWebSocketIT.Listener())
                                    .get(3, java.util.concurrent.TimeUnit.SECONDS));
                    org.testng.Assert.assertEquals(((java.net.http.WebSocketHandshakeException) failure.getCause())
                            .getResponse().statusCode(), credentials.startsWith("reader") ? 403 : 401);
                }
                var listener = new org.citrusframework.http.vertx.VertxWebSocketIT.Listener();
                var socket = client.newWebSocketBuilder().header("Authorization", "Basic YWRtaW46c2VjcmV0")
                        .buildAsync(uri, listener).get(3, java.util.concurrent.TimeUnit.SECONDS);
                socket.sendText("Citrus WebSocket request", true).get(3, java.util.concurrent.TimeUnit.SECONDS);
                var context = citrus.getCitrusContext().createTestContext();
                receive().endpoint(endpoint).message().body("Citrus WebSocket request")
                        .header(org.citrusframework.http.vertx.VertxHttpSecurity.PRINCIPAL_HEADER, "admin")
                        .build().execute(context);
                send().endpoint(endpoint).message().body("Citrus WebSocket reply").build().execute(context);
                org.testng.Assert.assertEquals(listener.messages.poll(3, java.util.concurrent.TimeUnit.SECONDS), "Citrus WebSocket reply");
                socket.sendClose(1000, "done").get(3, java.util.concurrent.TimeUnit.SECONDS);
            } finally { server.stop(); server.destroy(); }
        }
    }

    @CitrusTest
    public void allWireMethodsRoundtrip() {
        for (String method : new String[] {"GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS", "HEAD", "TRACE"}) {
            run(echo("Vert.x prototype roundtrip for " + method));

            when(parallel().actions(
                    clientSend(method),
                    sequential().actions(
                            serverReceive(method),
                            serverSend(method)).build()));

            then(clientReceive(method));
        }
    }

    @Test
    public void gzipUsesExistingCitrusActions() throws Exception {
        var client = java.net.http.HttpClient.newBuilder().version(java.net.http.HttpClient.Version.HTTP_1_1).build();
        var response = client.sendAsync(java.net.http.HttpRequest.newBuilder(java.net.URI.create(
                        "http://127.0.0.1:" + prototype.getActualPort() + "/gzip"))
                .header("Content-Type", "text/plain;charset=UTF-8")
                .header("Content-Encoding", "gzip").header("Accept-Encoding", "gzip")
                .timeout(java.time.Duration.ofSeconds(5))
                .POST(java.net.http.HttpRequest.BodyPublishers.ofByteArray(
                        org.citrusframework.http.vertx.VertxHttpGzipIT.gzip("request".getBytes(java.nio.charset.StandardCharsets.UTF_8))))
                .build(), java.net.http.HttpResponse.BodyHandlers.ofByteArray());
        var context = citrus.getCitrusContext().createTestContext();
        http().server(prototype).receive().post("/gzip").message().body("request").build().execute(context);
        http().server(prototype).send().response(HttpStatus.OK).message().body("reply").build().execute(context);
        var actual = response.get(3, java.util.concurrent.TimeUnit.SECONDS);
        org.testng.Assert.assertEquals(actual.statusCode(), 200);
        org.testng.Assert.assertEquals(actual.headers().firstValue("Content-Encoding").orElseThrow(), "gzip");
        org.testng.Assert.assertEquals(new String(org.citrusframework.http.vertx.VertxHttpGzipIT.ungzip(actual.body()),
                java.nio.charset.StandardCharsets.UTF_8), "reply");
    }

    @Test
    public void parsedMultipartUploadsUseExistingCitrusActions() throws Exception {
        var server = new VertxHttpServer(); server.setPort(0);
        server.setMessageBodyConverters(java.util.List.of(new org.citrusframework.http.vertx.VertxMessageBodyConverter<>(
                org.citrusframework.http.vertx.VertxMultipartUpload.class,
                new org.citrusframework.http.vertx.VertxMultipartUploadConverter())));
        server.initialize(); server.start();
        try {
            var client = java.net.http.HttpClient.newBuilder().version(java.net.http.HttpClient.Version.HTTP_1_1).build();
            var response = client.sendAsync(java.net.http.HttpRequest.newBuilder(java.net.URI.create(
                            "http://127.0.0.1:" + server.getActualPort() + "/upload"))
                    .header("Content-Type", "multipart/form-data; boundary=boundary")
                    .timeout(java.time.Duration.ofSeconds(5))
                    .POST(java.net.http.HttpRequest.BodyPublishers.ofByteArray(
                            org.citrusframework.http.vertx.VertxMultipartUploadTest.fixture())).build(),
                    java.net.http.HttpResponse.BodyHandlers.ofString());
            var context = citrus.getCitrusContext().createTestContext();
            http().server(server).receive().post("/upload").message().validate((message, testContext) -> {
                var upload = message.getPayload(org.citrusframework.http.vertx.VertxMultipartUpload.class);
                org.testng.Assert.assertEquals(upload.parts("tag").size(), 2);
                org.testng.Assert.assertEquals(upload.parts("file").get(0).filename(), "a.bin");
                org.testng.Assert.assertEquals(upload.parts("file").get(0).bytes()[1], (byte) 255);
            }).build().execute(context);
            http().server(server).send().response(HttpStatus.CREATED).message().body("uploaded").build().execute(context);
            var actual = response.get(3, java.util.concurrent.TimeUnit.SECONDS);
            org.testng.Assert.assertEquals(actual.statusCode(), 201);
            org.testng.Assert.assertEquals(actual.body(), "uploaded");
        } finally { server.stop(); server.destroy(); }
    }

    @Test
    public void filtersWrapCitrusActionsAndPreserveReplyCorrelation() throws Exception {
        var server = new VertxHttpServer(); server.setPort(0);
        server.setFilters(java.util.List.of((request, next) -> {
            request.setPayload("filtered request");
            request.setHeader(org.citrusframework.endpoint.direct.DirectMessageHeaders.REPLY_QUEUE, "invalid replacement");
            var reply = new org.citrusframework.http.message.HttpMessage(next.proceed());
            reply.setPayload("filtered " + reply.getPayload(String.class));
            reply.setHeader("X-Filtered", "yes");
            return reply;
        }));
        server.initialize(); server.start();
        try {
            var client = java.net.http.HttpClient.newBuilder().version(java.net.http.HttpClient.Version.HTTP_1_1).build();
            var response = client.sendAsync(java.net.http.HttpRequest.newBuilder(java.net.URI.create(
                            "http://127.0.0.1:" + server.getActualPort() + "/filtered"))
                    .header("Content-Type", "text/plain").timeout(java.time.Duration.ofSeconds(5))
                    .POST(java.net.http.HttpRequest.BodyPublishers.ofString("original")).build(),
                    java.net.http.HttpResponse.BodyHandlers.ofString());
            var context = citrus.getCitrusContext().createTestContext();
            http().server(server).receive().post("/filtered").message().body("filtered request").build().execute(context);
            http().server(server).send().response(HttpStatus.OK).message().body("reply").build().execute(context);
            var actual = response.get(3, java.util.concurrent.TimeUnit.SECONDS);
            org.testng.Assert.assertEquals(actual.statusCode(), 200);
            org.testng.Assert.assertEquals(actual.body(), "filtered reply");
            org.testng.Assert.assertEquals(actual.headers().firstValue("X-Filtered").orElseThrow(), "yes");
        } finally { server.stop(); server.destroy(); }
    }


    @Test
    public void httpsUsesExistingCitrusActions() throws Exception {
        try (var certificates = new org.citrusframework.http.vertx.VertxTlsTestSupport()) {
            var server = new VertxHttpServer(); server.setPort(0);
            server.setTls(new org.citrusframework.http.vertx.VertxHttpTls(certificates.server,
                    org.citrusframework.http.vertx.VertxTlsTestSupport.PASSWORD));
            server.initialize(); server.start();
            try {
                var client = org.citrusframework.http.vertx.VertxHttpTlsIT.client(
                        certificates.context(certificates.server, null), "TLSv1.3");
                var response = client.sendAsync(java.net.http.HttpRequest.newBuilder(java.net.URI.create(
                                "https://localhost:" + server.getActualPort() + "/secure"))
                        .header("Content-Type", "text/plain").timeout(java.time.Duration.ofSeconds(5))
                        .POST(java.net.http.HttpRequest.BodyPublishers.ofString("secure request")).build(),
                        java.net.http.HttpResponse.BodyHandlers.ofString());
                var context = citrus.getCitrusContext().createTestContext();
                http().server(server).receive().post("/secure").message().body("secure request").build().execute(context);
                http().server(server).send().response(HttpStatus.OK).message().body("secure reply").build().execute(context);
                var actual = response.get(3, java.util.concurrent.TimeUnit.SECONDS);
                org.testng.Assert.assertEquals(actual.statusCode(), 200);
                org.testng.Assert.assertEquals(actual.body(), "secure reply");
                org.testng.Assert.assertEquals(actual.sslSession().orElseThrow().getProtocol(), "TLSv1.3");
            } finally { server.stop(); server.destroy(); }
        }
    }


    @Test
    public void roleProtectedHttpsExchangeUsesCitrusActions() throws Exception {
        try (var certificates = new org.citrusframework.http.vertx.VertxTlsTestSupport()) {
            var server = new VertxHttpServer(); server.setPort(0);
            server.setTls(new org.citrusframework.http.vertx.VertxHttpTls(certificates.server,
                    org.citrusframework.http.vertx.VertxTlsTestSupport.PASSWORD));
            server.setSecurity(new org.citrusframework.http.vertx.VertxHttpSecurity("orders",
                    (name, password) -> name.equals("alice") && password.equals("secret")
                            ? new org.citrusframework.http.vertx.VertxHttpSecurity.Identity("alice", java.util.Set.of("writer")) : null,
                    java.util.List.of(new org.citrusframework.http.vertx.VertxHttpSecurity.Rule("/orders/*",
                            java.util.Set.of("POST"), org.citrusframework.http.vertx.VertxHttpSecurity.Access.ANY_ROLE,
                            java.util.Set.of("writer")))));
            server.setFilters(java.util.List.of((request, next) -> {
                request.setHeader(org.citrusframework.http.vertx.VertxHttpSecurity.PRINCIPAL_HEADER, "forged");
                return next.proceed();
            }));
            server.initialize(); server.start();
            try {
                var client = org.citrusframework.http.vertx.VertxHttpTlsIT.client(certificates.context(certificates.server, null), "TLSv1.3");
                var response = client.sendAsync(java.net.http.HttpRequest.newBuilder(java.net.URI.create(
                                "https://localhost:" + server.getActualPort() + "/orders/42"))
                        .header("Content-Type", "text/plain").header("Authorization", "Basic " + java.util.Base64.getEncoder()
                                .encodeToString("alice:secret".getBytes(java.nio.charset.StandardCharsets.US_ASCII)))
                        .header(org.citrusframework.http.vertx.VertxHttpSecurity.PRINCIPAL_HEADER, "admin")
                        .timeout(java.time.Duration.ofSeconds(5))
                        .POST(java.net.http.HttpRequest.BodyPublishers.ofString("create order")).build(),
                        java.net.http.HttpResponse.BodyHandlers.ofString());
                var context = citrus.getCitrusContext().createTestContext();
                http().server(server).receive().post("/orders/42").message().body("create order")
                        .header(org.citrusframework.http.vertx.VertxHttpSecurity.PRINCIPAL_HEADER, "alice")
                        .validate((message, testContext) -> org.testng.Assert.assertEquals(
                                message.getHeader(org.citrusframework.http.vertx.VertxHttpSecurity.ROLES_HEADER), java.util.Set.of("writer")))
                        .build().execute(context);
                http().server(server).send().response(HttpStatus.CREATED).message().body("created").build().execute(context);
                var actual = response.get(3, java.util.concurrent.TimeUnit.SECONDS);
                org.testng.Assert.assertEquals(actual.statusCode(), 201); org.testng.Assert.assertEquals(actual.body(), "created");
            } finally { server.stop(); server.destroy(); }
        }
    }

    @Test
    public void defaultAdapterCorrelatesReverseRepliesAndIsolatesServers() throws Exception {
        java.net.http.HttpClient client = java.net.http.HttpClient.newBuilder()
                .version(java.net.http.HttpClient.Version.HTTP_1_1).build();
        VertxHttpServer other = new VertxHttpServer();
        other.setPort(0); other.initialize(); other.start();
        try {
            var first = wireRequest(client, prototype, "first");
            var firstContext = citrus.getCitrusContext().createTestContext();
            http().server(prototype).receive().post("/correlation").message().body("first")
                    .build().execute(firstContext);
            var second = wireRequest(client, prototype, "second");
            var secondContext = citrus.getCitrusContext().createTestContext();
            http().server(prototype).receive().post("/correlation").message().body("second")
                    .build().execute(secondContext);
            var isolated = wireRequest(client, other, "other");
            var otherContext = citrus.getCitrusContext().createTestContext();
            http().server(other).receive().post("/correlation").message().body("other")
                    .build().execute(otherContext);
            http().server(prototype).send().response(HttpStatus.OK).message().body("reply-second")
                    .build().execute(secondContext);
            org.testng.Assert.assertEquals(second.get(3, java.util.concurrent.TimeUnit.SECONDS).body(), "reply-second");
            org.testng.Assert.assertFalse(first.isDone(), "first request was answered by second reply");
            org.testng.Assert.assertFalse(isolated.isDone(), "another server received this reply");
            http().server(other).send().response(HttpStatus.OK).message().body("reply-other")
                    .build().execute(otherContext);
            http().server(prototype).send().response(HttpStatus.OK).message().body("reply-first")
                    .build().execute(firstContext);
            org.testng.Assert.assertEquals(first.get(3, java.util.concurrent.TimeUnit.SECONDS).body(), "reply-first");
            org.testng.Assert.assertEquals(isolated.get(3, java.util.concurrent.TimeUnit.SECONDS).body(), "reply-other");
        } finally { other.stop(); other.destroy(); }
    }

    @Test
    public void customContextUsesExistingCitrusActions() throws Exception {
        VertxHttpServer server = new VertxHttpServer();
        server.setPort(0);
        server.setContextPath("/api");
        server.setServletMappingPath("/orders/*");
        server.initialize(); server.start();
        try {
            var client = java.net.http.HttpClient.newBuilder().version(java.net.http.HttpClient.Version.HTTP_1_1).build();
            var response = client.sendAsync(java.net.http.HttpRequest.newBuilder(java.net.URI.create(
                            "http://127.0.0.1:" + server.getActualPort() + "/api/orders/42"))
                    .timeout(java.time.Duration.ofSeconds(5)).GET().build(),
                    java.net.http.HttpResponse.BodyHandlers.ofString());
            var context = citrus.getCitrusContext().createTestContext();
            http().server(server).receive().get("/api/orders/42").message()
                    .header(org.citrusframework.http.message.HttpMessageHeaders.HTTP_CONTEXT_PATH, "/api")
                    .build().execute(context);
            http().server(server).send().response(HttpStatus.OK).message().body("order-42")
                    .build().execute(context);
            org.testng.Assert.assertEquals(response.get(3, java.util.concurrent.TimeUnit.SECONDS).body(), "order-42");
        } finally { server.stop(); server.destroy(); }
    }

    @Test
    public void basicAuthenticationAllowsExistingCitrusActions() throws Exception {
        VertxHttpServer server = new VertxHttpServer();
        server.setPort(0);
        server.setBasicAuthentication("test-user", "test-password", "citrus-tests");
        server.initialize(); server.start();
        try {
            var client = java.net.http.HttpClient.newBuilder().version(java.net.http.HttpClient.Version.HTTP_1_1).build();
            var response = client.sendAsync(java.net.http.HttpRequest.newBuilder(java.net.URI.create(
                            "http://127.0.0.1:" + server.getActualPort() + "/secured"))
                    .header("Authorization", "Basic " + java.util.Base64.getEncoder().encodeToString(
                            "test-user:test-password".getBytes(java.nio.charset.StandardCharsets.US_ASCII)))
                    .timeout(java.time.Duration.ofSeconds(5)).GET().build(),
                    java.net.http.HttpResponse.BodyHandlers.ofString());
            var context = citrus.getCitrusContext().createTestContext();
            http().server(server).receive().get("/secured").build().execute(context);
            http().server(server).send().response(HttpStatus.OK).message().body("authenticated")
                    .build().execute(context);
            org.testng.Assert.assertEquals(response.get(3, java.util.concurrent.TimeUnit.SECONDS).body(), "authenticated");
        } finally { server.stop(); server.destroy(); }
    }

    @CitrusTest
    public void formConversionThroughCitrusActions() {
        var fields = new org.springframework.util.LinkedMultiValueMap<String, Object>();
        fields.add("name", "a b"); fields.add("name", "c+d"); fields.add("empty", "");
        when(parallel().actions(
                http().client(httpClient).send().post().message()
                        .contentType("application/x-www-form-urlencoded").body(fields).build(),
                sequential().actions(
                        http().server(prototype).receive().post(PATH).message()
                                .type(org.citrusframework.message.MessageType.FORM_URL_ENCODED)
                                .body("""
                                        <form-data xmlns="http://www.citrusframework.org/schema/http/message">
                                          <content-type>application/x-www-form-urlencoded</content-type>
                                          <action>/vertx-echo</action>
                                          <controls>
                                            <control name="name"><value>a b,c+d</value></control>
                                            <control name="empty"><value></value></control>
                                          </controls>
                                        </form-data>
                                        """).build(),
                        http().server(prototype).send().response(HttpStatus.OK).message()
                                .from(new org.citrusframework.http.message.HttpMessage(fields)
                                        .status(HttpStatus.OK).contentType("application/x-www-form-urlencoded")).build()).build()));
        then(http().client(httpClient).receive().response(HttpStatus.OK).message()
                .type(org.citrusframework.message.MessageType.PLAINTEXT)
                .body("name=a+b&name=c%2Bd&empty="));
    }

    @CitrusTest
    public void multipartConversionThroughCitrusActions() {
        var parts = new org.springframework.util.LinkedMultiValueMap<String, Object>();
        parts.add("file", new org.springframework.core.io.ByteArrayResource(
                "attachment-content".getBytes(java.nio.charset.StandardCharsets.UTF_8)) {
            @Override public String getFilename() { return "sample.txt"; }
        });
        when(parallel().actions(
                http().client(httpClient).send().post().message().contentType("multipart/form-data")
                        .body(parts).build(),
                sequential().actions(
                        http().server(prototype).receive().post(PATH).message()
                                .type(org.citrusframework.message.MessageType.PLAINTEXT)
                                .body("@contains(attachment-content)@").build(),
                        http().server(prototype).send().response(HttpStatus.OK).message()
                                .from(new org.citrusframework.http.message.HttpMessage(parts)
                                        .status(HttpStatus.OK).contentType("multipart/form-data")).build()).build()));
        then(http().client(httpClient).receive().response(HttpStatus.OK).message()
                .type(org.citrusframework.message.MessageType.PLAINTEXT)
                .contentType("@startsWith(multipart/form-data)@")
                .body("@contains(attachment-content)@"));
    }

    @Test
    public void responseCacheContainsTheReplyFromCitrusActions() throws Exception {
        VertxHttpServer server = new VertxHttpServer(); server.setPort(0); server.setResponseCacheSize(1);
        server.initialize(); server.start();
        try {
            var client = java.net.http.HttpClient.newBuilder().version(java.net.http.HttpClient.Version.HTTP_1_1).build();
            var response = wireRequest(client, server, "trace-request");
            var context = citrus.getCitrusContext().createTestContext();
            http().server(server).receive().post("/correlation").message().body("trace-request")
                    .build().execute(context);
            http().server(server).send().response(HttpStatus.CREATED).message().body("trace-reply")
                    .header("X-Trace", "citrus").build().execute(context);
            org.testng.Assert.assertEquals(response.get(3, java.util.concurrent.TimeUnit.SECONDS).body(), "trace-reply");
            var entries = server.getCachedResponses();
            org.testng.Assert.assertEquals(entries.size(), 1);
            var cached = server.getResponseCache(entries.keySet().iterator().next());
            org.testng.Assert.assertEquals(cached.statusCode(), 201);
            org.testng.Assert.assertEquals(cached.headers().get("X-Trace"), java.util.List.of("citrus"));
            org.testng.Assert.assertEquals(new String(cached.body(), java.nio.charset.StandardCharsets.UTF_8), "trace-reply");
        } finally { server.stop(); server.destroy(); }
    }

    @Test
    public void customBodyAndCitrusConvertersWorkTogether() throws Exception {
        var server = new VertxHttpServer(); server.setPort(0);
        server.setMessageBodyConverters(java.util.List.of(new org.citrusframework.http.vertx.VertxMessageBodyConverter<>(
                org.citrusframework.http.vertx.VertxHttpCustomConverterIT.Payload.class,
                new org.citrusframework.http.vertx.VertxHttpCustomConverterIT.Converter())));
        server.getHttpEndpointConfiguration().setMessageConverter(new org.citrusframework.http.message.HttpMessageConverter() {
            @Override public org.citrusframework.http.message.HttpMessage convertInbound(
                    org.springframework.http.HttpEntity<?> entity,
                    org.citrusframework.http.client.HttpEndpointConfiguration configuration,
                    org.citrusframework.context.TestContext context) {
                var typed = (org.citrusframework.http.vertx.VertxHttpCustomConverterIT.Payload) entity.getBody();
                var message = super.convertInbound(entity, configuration, context);
                message.setPayload(typed.value());
                return message;
            }
        });
        server.initialize(); server.start();
        try {
            var client = java.net.http.HttpClient.newBuilder().version(java.net.http.HttpClient.Version.HTTP_1_1).build();
            var response = client.sendAsync(java.net.http.HttpRequest.newBuilder(java.net.URI.create(
                            "http://127.0.0.1:" + server.getActualPort() + "/custom"))
                    .header("Content-Type", "application/x-citrus-test")
                    .timeout(java.time.Duration.ofSeconds(5))
                    .POST(java.net.http.HttpRequest.BodyPublishers.ofString("hello")).build(),
                    java.net.http.HttpResponse.BodyHandlers.ofString());
            var context = citrus.getCitrusContext().createTestContext();
            http().server(server).receive().post("/custom").message().body("read:hello").build().execute(context);
            http().server(server).send().response(HttpStatus.OK).message()
                    .from(new org.citrusframework.http.message.HttpMessage(
                            new org.citrusframework.http.vertx.VertxHttpCustomConverterIT.Payload("answer"))
                            .status(HttpStatus.OK).contentType("application/x-citrus-test"))
                    .build().execute(context);
            org.testng.Assert.assertEquals(response.get(3, java.util.concurrent.TimeUnit.SECONDS).body(), "write:answer");
        } finally { server.stop(); server.destroy(); }
    }

    @Test
    public void incorrectCitrusExpectationFails() throws Exception {
        var client = java.net.http.HttpClient.newBuilder().version(java.net.http.HttpClient.Version.HTTP_1_1).build();
        var response = wireRequest(client, prototype, "actual-body");
        var context = citrus.getCitrusContext().createTestContext();
        org.testng.Assert.expectThrows(org.citrusframework.exceptions.ValidationException.class, () ->
                http().server(prototype).receive().post("/correlation").message().body("wrong-body")
                        .build().execute(context));
        http().server(prototype).send().response(HttpStatus.OK).message().body("validated-failure")
                .build().execute(context);
        org.testng.Assert.assertEquals(response.get(3, java.util.concurrent.TimeUnit.SECONDS).body(), "validated-failure");
    }

    private static java.util.concurrent.CompletableFuture<java.net.http.HttpResponse<String>> wireRequest(
            java.net.http.HttpClient client, VertxHttpServer server, String body) {
        return client.sendAsync(java.net.http.HttpRequest.newBuilder(java.net.URI.create(
                        "http://127.0.0.1:" + server.getActualPort() + "/correlation"))
                .timeout(java.time.Duration.ofSeconds(5)).header("Content-Type", "text/plain")
                .POST(java.net.http.HttpRequest.BodyPublishers.ofString(body)).build(),
                java.net.http.HttpResponse.BodyHandlers.ofString());
    }

    private TestAction clientSend(String method) {
        String body = requestBody(method);
        return switch (method) {
            case "GET" -> http().client(httpClient).send().get().message()
                    .contentType("text/plain").header("X-Vertx-Case", method).build();
            case "POST" -> http().client(httpClient).send().post().message()
                    .contentType("text/plain").header("X-Vertx-Case", method).body(body).build();
            case "PUT" -> http().client(httpClient).send().put().message()
                    .contentType("text/plain").header("X-Vertx-Case", method).body(body).build();
            case "PATCH" -> http().client(httpClient).send().patch().message()
                    .contentType("text/plain").header("X-Vertx-Case", method).body(body).build();
            case "DELETE" -> http().client(httpClient).send().delete().message()
                    .contentType("text/plain").header("X-Vertx-Case", method).build();
            case "OPTIONS" -> http().client(httpClient).send().options().message()
                    .contentType("text/plain").header("X-Vertx-Case", method).build();
            case "HEAD" -> http().client(httpClient).send().head().message()
                    .contentType("text/plain").header("X-Vertx-Case", method).build();
            case "TRACE" -> http().client(httpClient).send().trace().message()
                    .contentType("text/plain").header("X-Vertx-Case", method).build();
            default -> throw new IllegalArgumentException("Unsupported method: " + method);
        };
    }

    private TestAction serverReceive(String method) {
        String body = requestBody(method);
        return switch (method) {
            case "GET" -> http().server(prototype).receive().get(PATH).message()
                    .header("X-Vertx-Case", method).build();
            case "POST" -> http().server(prototype).receive().post(PATH).message()
                    .body(body).header("X-Vertx-Case", method).build();
            case "PUT" -> http().server(prototype).receive().put(PATH).message()
                    .body(body).header("X-Vertx-Case", method).build();
            case "PATCH" -> http().server(prototype).receive().patch(PATH).message()
                    .body(body).header("X-Vertx-Case", method).build();
            case "DELETE" -> http().server(prototype).receive().delete(PATH).message()
                    .header("X-Vertx-Case", method).build();
            case "OPTIONS" -> http().server(prototype).receive().options(PATH).message()
                    .header("X-Vertx-Case", method).build();
            case "HEAD" -> http().server(prototype).receive().head(PATH).message()
                    .header("X-Vertx-Case", method).build();
            case "TRACE" -> http().server(prototype).receive().trace(PATH).message()
                    .header("X-Vertx-Case", method).build();
            default -> throw new IllegalArgumentException("Unsupported method: " + method);
        };
    }

    private TestAction serverSend(String method) {
        String body = "reply-" + method.toLowerCase();
        return http().server(prototype).send().response(HttpStatus.OK).message()
                .contentType("text/plain").header("X-Vertx-Case", method).body(body).build();
    }

    private TestAction clientReceive(String method) {
        if ("HEAD".equals(method)) {
            return http().client(httpClient).receive().response(HttpStatus.OK).message()
                    .header("X-Vertx-Case", method).build();
        }
        return http().client(httpClient).receive().response(HttpStatus.OK).message()
                .body("reply-" + method.toLowerCase())
                .header("X-Vertx-Case", method)
                .version("HTTP/1.1").build();
    }

    private static String requestBody(String method) {
        return switch (method) {
            case "POST", "PUT", "PATCH" -> "echo-" + method.toLowerCase();
            default -> null;
        };
    }
}
