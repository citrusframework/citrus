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

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPOutputStream;

import org.citrusframework.http.client.HttpEndpointConfiguration;
import org.citrusframework.http.message.HttpMessage;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

/**
 * Staged request attributes (the Vert.x analog of servlet request attributes) become Citrus
 * message headers verbatim when {@code handleAttributeHeaders} is enabled, mirroring
 * {@code HttpMessageController}.
 */
public class VertxHttpAttributeHeadersTest {

    private HttpEndpointConfiguration endpointConfiguration;

    @BeforeMethod
    public void setUp() {
        endpointConfiguration = new HttpEndpointConfiguration();
    }

    @Test
    public void attributesAreIgnoredWhenFlagIsDisabled() {
        HttpMessage message = VertxHttpMessageMapper.toHttpMessage(
                snapshot(Map.of("app.user", "alice")), endpointConfiguration);

        assertNull(message.getHeader("app.user"));
    }

    @Test
    public void attributesAreExposedVerbatimWithRawValuesWhenEnabled() {
        endpointConfiguration.setHandleAttributeHeaders(true);
        Map<String, Object> attributes = new LinkedHashMap<>();
        attributes.put("app.user", "alice");
        attributes.put("app.attempt", 7);

        HttpMessage message = VertxHttpMessageMapper.toHttpMessage(
                snapshot(attributes), endpointConfiguration);

        assertEquals(message.getHeader("app.user"), "alice");
        assertEquals(message.getHeader("app.attempt"), 7);
    }

    @Test
    public void attributeOverwritesWireHeaderOfTheSameName() {
        endpointConfiguration.setHandleAttributeHeaders(true);

        HttpMessage message = VertxHttpMessageMapper.toHttpMessage(
                new VertxHttpMessageMapper.RequestSnapshot("POST", "/orders", "/orders", null,
                        "HTTP/1.1", Map.of("X-Source", List.of("wire")), new byte[0],
                        "g1-x1", 1L, Map.of("X-Source", "attribute")),
                endpointConfiguration);

        assertEquals(message.getHeader("X-Source"), "attribute");
    }

    @Test
    public void nullNamesAndValuesAreSkipped() {
        endpointConfiguration.setHandleAttributeHeaders(true);
        Map<String, Object> attributes = new LinkedHashMap<>();
        attributes.put(null, "no-name");
        attributes.put("no-value", null);
        attributes.put("kept", "yes");

        HttpMessage message = VertxHttpMessageMapper.toHttpMessage(
                snapshot(attributes), endpointConfiguration);

        assertNull(message.getHeader("no-value"));
        assertEquals(message.getHeader("kept"), "yes");
    }

    @Test
    public void gzipDecodingPreservesAttributes() throws Exception {
        endpointConfiguration.setHandleAttributeHeaders(true);
        byte[] gzipped = gzip("hello".getBytes(StandardCharsets.UTF_8));

        VertxHttpMessageMapper.RequestSnapshot decoded = VertxHttpGzip.decode(
                new VertxHttpMessageMapper.RequestSnapshot("POST", "/gzip", "/gzip", null,
                        "HTTP/1.1", Map.of("Content-Encoding", List.of("gzip"),
                                "Content-Type", List.of("text/plain")),
                        gzipped, "g1-x1", 1L, Map.of("app.user", "alice")),
                1024);

        HttpMessage message = VertxHttpMessageMapper.toHttpMessage(decoded, endpointConfiguration);

        assertEquals(message.getPayload(String.class), "hello");
        assertEquals(message.getHeader("app.user"), "alice");
    }

    @Test
    public void throwingHookProducesEmpty500AndRecordsFailure() throws Exception {
        VertxHttpServer server = new VertxHttpServer();
        server.setName("vertxAttributesFailure");
        server.setPort(0);
        server.setRequestAttributes((request, attributes) -> {
            throw new IllegalStateException("boom");
        });
        server.initialize();
        server.start();
        try {
            java.net.http.HttpResponse<String> response = java.net.http.HttpClient.newHttpClient().send(
                    java.net.http.HttpRequest.newBuilder(java.net.URI.create(
                                    "http://127.0.0.1:" + server.getActualPort() + "/orders"))
                            .POST(java.net.http.HttpRequest.BodyPublishers.ofString("order"))
                            .build(),
                    java.net.http.HttpResponse.BodyHandlers.ofString());

            assertEquals(response.statusCode(), 500);
            assertEquals(response.body(), "");
            assertTrue(server.getFailures().stream()
                    .anyMatch(failure -> failure instanceof IllegalStateException
                            && "boom".equals(failure.getMessage())));
        } finally {
            server.stop();
            server.destroy();
        }
    }

    private static VertxHttpMessageMapper.RequestSnapshot snapshot(Map<String, Object> attributes) {
        return new VertxHttpMessageMapper.RequestSnapshot("POST", "/orders", "/orders", null,
                "HTTP/1.1", Map.of("Content-Type", List.of("text/plain")),
                "order".getBytes(StandardCharsets.UTF_8), "g1-x1", 1L, attributes);
    }

    private static byte[] gzip(byte[] body) throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try (GZIPOutputStream gzip = new GZIPOutputStream(output)) {
            gzip.write(body);
        }
        return output.toByteArray();
    }
}
