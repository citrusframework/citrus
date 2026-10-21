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

package org.citrusframework.http.server.vertx;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPInputStream;

import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.KeyStoreOptions;
import io.vertx.ext.web.Router;
import org.citrusframework.annotations.CitrusTest;
import org.citrusframework.dsl.TestActionSupport;
import org.citrusframework.http.client.HttpClient;
import org.citrusframework.http.client.HttpClientBuilder;
import org.citrusframework.http.security.HttpSecureConnection;
import org.citrusframework.testng.spring.TestNGCitrusSpringSupport;
import org.citrusframework.util.SocketUtils;
import org.citrusframework.validation.interceptor.GzipMessageProcessor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Throw-away smoke test verifying that the Vert.x stack (Vert.x 4.5.35 on Netty 4.2.19.Final) can
 * be embedded into citrus-http and talks to the Citrus HTTP client. This test is replaced by the
 * real Vert.x server tests in T10.
 */
@Test
public class NettySmokeIT extends TestNGCitrusSpringSupport implements TestActionSupport {

    private static final int plainPort = SocketUtils.findAvailableTcpPort(9000);
    private static final int securePort = SocketUtils.findAvailableTcpPort(9100);

    private Vertx vertx;

    private HttpClient plainClient;
    private HttpClient secureClient;

    @BeforeClass
    public void setup() throws Exception {
        vertx = Vertx.vertx();

        startServer(plainPort, false);
        startServer(securePort, true);

        plainClient = new HttpClientBuilder()
                .requestUrl("http://localhost:" + plainPort)
                .build();

        secureClient = new HttpClientBuilder()
                .requestUrl("https://localhost:" + securePort)
                .secured(HttpSecureConnection.ssl())
                .build();
    }

    @AfterClass
    public void teardown() throws Exception {
        if (vertx != null) {
            vertx.close().toCompletionStage().toCompletableFuture().get(30, TimeUnit.SECONDS);
        }
    }

    private void startServer(int port, boolean secure) throws Exception {
        Router router = Router.router(vertx);
        router.route("/echo").handler(context -> context.request().body()
                .onSuccess(body -> context.response()
                        .putHeader(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN_VALUE)
                        .end(decodeIfGzipped(context.request().getHeader(HttpHeaders.CONTENT_ENCODING), body))));

        var options = new io.vertx.core.http.HttpServerOptions()
                .setHost("localhost")
                .setPort(port);

        if (secure) {
            options.setSsl(true);
            options.setKeyCertOptions(keyStoreOptions());
        }

        vertx.createHttpServer(options)
                .requestHandler(router)
                .listen()
                .toCompletionStage()
                .toCompletableFuture()
                .get(30, TimeUnit.SECONDS);
    }

    private Buffer decodeIfGzipped(String contentEncoding, Buffer body) {
        if (contentEncoding == null || !contentEncoding.contains("gzip")) {
            return body;
        }

        try (InputStream is = new GZIPInputStream(new ByteArrayInputStream(body.getBytes()))) {
            return Buffer.buffer(is.readAllBytes());
        } catch (Exception e) {
            throw new IllegalStateException("Failed to decode gzipped request body", e);
        }
    }

    private KeyStoreOptions keyStoreOptions() throws Exception {
        try (InputStream is = NettySmokeIT.class.getClassLoader()
                .getResourceAsStream("org/citrusframework/http/integration/http-server.jks")) {
            Buffer buffer = Buffer.buffer(is.readAllBytes());
            return new KeyStoreOptions()
                    .setType("JKS")
                    .setPassword("secret")
                    .setValue(buffer);
        }
    }

    @CitrusTest
    public void shouldSendGetRequest() {
        given(http().client(plainClient)
                .send()
                .get("/echo"));

        then(http().client(plainClient)
                .receive()
                .response(HttpStatus.OK));
    }

    @CitrusTest
    public void shouldSendPostRequestWithGzipBody() {
        given(http().client(plainClient)
                .send()
                .post("/echo")
                .message()
                .contentType(MediaType.TEXT_PLAIN_VALUE)
                .header(HttpHeaders.CONTENT_ENCODING, "gzip")
                .process(new GzipMessageProcessor())
                .body("Hello Vert.x"));

        then(http().client(plainClient)
                .receive()
                .response(HttpStatus.OK)
                .message()
                .body("Hello Vert.x"));
    }

    @CitrusTest
    public void shouldSendGetRequestOverTls() {
        given(http().client(secureClient)
                .send()
                .get("/echo"));

        then(http().client(secureClient)
                .receive()
                .response(HttpStatus.OK));
    }
}
