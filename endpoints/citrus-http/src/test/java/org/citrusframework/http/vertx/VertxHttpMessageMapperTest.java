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

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import jakarta.servlet.http.Cookie;
import org.citrusframework.http.client.HttpEndpointConfiguration;
import org.citrusframework.http.message.HttpMessage;
import org.citrusframework.http.message.HttpMessageHeaders;
import org.citrusframework.message.DefaultMessage;
import org.springframework.http.HttpStatus;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

public class VertxHttpMessageMapperTest {

    private HttpEndpointConfiguration endpointConfiguration;

    @BeforeMethod
    public void setUp() {
        endpointConfiguration = new HttpEndpointConfiguration();
    }

    @Test
    public void getRequestWithoutBodyMapsToEmptyPayload() {
        HttpMessage message = VertxHttpMessageMapper.toHttpMessage(
                snapshot("GET", "/api/items", "a=1&b=2", null, Map.of("X-Custom", List.of("custom-value"))),
                endpointConfiguration);

        assertEquals(message.getRequestMethod().name(), "GET");
        assertEquals(message.getUri(), "/api/items");
        assertEquals(message.getPath(), "/api/items");
        assertEquals(message.getVersion(), "HTTP/1.1");
        assertEquals(message.getPayload(String.class), "");
        assertEquals(message.getHeader("X-Custom"), "custom-value");
        assertEquals(message.getQueryParams().get("a").iterator().next(), "1");
        assertEquals(message.getQueryParams().get("b").iterator().next(), "2");
    }

    @Test(dataProvider = "httpMethods")
    public void allWireMethodsMapToRequestMethod(String method) {
        HttpMessage message = VertxHttpMessageMapper.toHttpMessage(
                snapshot(method, "/api/items", null, "ping".getBytes(StandardCharsets.UTF_8), Map.of()),
                endpointConfiguration);

        assertEquals(message.getRequestMethod().name(), method);
    }

    @Test
    public void postTextBodyDecodesWithCharset() {
        byte[] body = "Hello Citrus äöü".getBytes(StandardCharsets.UTF_8);
        HttpMessage message = VertxHttpMessageMapper.toHttpMessage(
                snapshot("POST", "/api/echo", null, body,
                        Map.of("Content-Type", List.of("text/plain;charset=UTF-8"))),
                endpointConfiguration);

        assertEquals(message.getPayload(String.class), "Hello Citrus äöü");
    }

    @Test
    public void binaryContentTypeMapsToByteArrayPayload() {
        byte[] body = new byte[] {0x00, 0x01, 0x02, (byte) 0xFF};
        HttpMessage message = VertxHttpMessageMapper.toHttpMessage(
                snapshot("POST", "/api/binary", null, body,
                        Map.of("Content-Type", List.of("application/octet-stream"))),
                endpointConfiguration);

        assertTrue(message.getPayload() instanceof byte[]);
        assertEquals(message.getPayload(byte[].class), body);
    }

    @Test
    public void missingContentTypeFallsBackToTextPayload() {
        HttpMessage message = VertxHttpMessageMapper.toHttpMessage(
                snapshot("POST", "/api/echo", null, "plain".getBytes(StandardCharsets.UTF_8), Map.of()),
                endpointConfiguration);

        assertEquals(message.getPayload(String.class), "plain");
    }

    @Test
    public void multiValueHeadersAreCommaJoined() {
        Map<String, List<String>> headers = new LinkedHashMap<>();
        headers.put("X-Multi", List.of("one", "two"));
        HttpMessage message = VertxHttpMessageMapper.toHttpMessage(
                snapshot("GET", "/api/items", null, null, headers), endpointConfiguration);

        assertEquals(message.getHeader("X-Multi"), "one,two");
    }

    @Test
    public void semicolonPathContentIsRemovedByDefault() {
        HttpMessage message = VertxHttpMessageMapper.toHttpMessage(
                snapshot("GET", "/api/items;jsessionid=abc123", null, null, Map.of()),
                endpointConfiguration);

        assertEquals(message.getUri(), "/api/items");
        assertEquals(message.getPath(), "/api/items");
    }

    @Test
    public void cookiesAreParsedWhenEnabled() {
        endpointConfiguration.setHandleCookies(true);
        HttpMessage message = VertxHttpMessageMapper.toHttpMessage(
                snapshot("GET", "/api/items", null, null,
                        Map.of("Cookie", List.of("session=abc; theme=dark"))),
                endpointConfiguration);

        assertEquals(message.getCookies().size(), 2);
    }

    @Test
    public void cookiesAreIgnoredByDefault() {
        HttpMessage message = VertxHttpMessageMapper.toHttpMessage(
                snapshot("GET", "/api/items", null, null,
                        Map.of("Cookie", List.of("session=abc"))),
                endpointConfiguration);

        assertTrue(message.getCookies().isEmpty());
    }

    @Test
    public void exchangeIdentityHeadersAreSet() {
        HttpMessage message = VertxHttpMessageMapper.toHttpMessage(
                new VertxHttpMessageMapper.RequestSnapshot("GET", "/api/items", "/api/items", null,
                        "HTTP/1.1", Map.of(), null, "g3-x42", 3L),
                endpointConfiguration);

        assertEquals(message.getHeader(VertxHttpMessageMapper.EXCHANGE_ID_HEADER), "g3-x42");
        assertEquals(message.getHeader(VertxHttpMessageMapper.GENERATION_HEADER), 3L);
    }

    @Test
    public void nullReplyMapsToDefaultStatusAndEmptyBody() {
        VertxHttpMessageMapper.ResponsePlan plan =
                VertxHttpMessageMapper.toResponsePlan(null, endpointConfiguration, false);

        assertEquals(plan.statusCode(), HttpStatus.OK.value());
        assertEquals(plan.body().length, 0);
        assertEquals(singleHeader(plan.headers(), "Content-Length"), "0");
        assertNotNull(singleHeader(plan.headers(), "Content-Type"));
    }

    @Test
    public void replyWithoutStatusFallsBackToDefaultStatus() {
        HttpMessage reply = new HttpMessage("response-body");
        VertxHttpMessageMapper.ResponsePlan plan =
                VertxHttpMessageMapper.toResponsePlan(reply, endpointConfiguration, false);

        assertEquals(plan.statusCode(), HttpStatus.OK.value());
        assertEquals(new String(plan.body(), StandardCharsets.UTF_8), "response-body");
    }

    @Test
    public void explicitReplyStatusAndHeadersAreKept() {
        HttpMessage reply = new HttpMessage("created");
        reply.status(HttpStatus.CREATED);
        reply.setHeader("X-Reply", "yes");

        VertxHttpMessageMapper.ResponsePlan plan =
                VertxHttpMessageMapper.toResponsePlan(reply, endpointConfiguration, false);

        assertEquals(plan.statusCode(), HttpStatus.CREATED.value());
        assertEquals(singleHeader(plan.headers(), "X-Reply"), "yes");
        assertEquals(new String(plan.body(), StandardCharsets.UTF_8), "created");
    }

    @Test
    public void binaryReplyBodyIsPassedThrough() {
        byte[] body = new byte[] {0x10, 0x20};
        HttpMessage reply = new HttpMessage(body);
        reply.status(HttpStatus.OK);
        reply.setHeader(HttpMessageHeaders.HTTP_CONTENT_TYPE, "application/octet-stream");

        VertxHttpMessageMapper.ResponsePlan plan =
                VertxHttpMessageMapper.toResponsePlan(reply, endpointConfiguration, false);

        assertEquals(plan.body(), body);
    }

    @Test
    public void headRequestSuppressesBodyButKeepsLength() {
        HttpMessage reply = new HttpMessage("would-be-body");
        reply.status(HttpStatus.OK);

        VertxHttpMessageMapper.ResponsePlan plan =
                VertxHttpMessageMapper.toResponsePlan(reply, endpointConfiguration, true);

        assertTrue(plan.suppressBody());
        assertEquals(plan.body().length, 0);
        assertEquals(singleHeader(plan.headers(), "Content-Length"), String.valueOf("would-be-body".length()));
    }

    @Test
    public void replyCookiesBecomeSetCookieHeaders() {
        endpointConfiguration.setHandleCookies(true);
        HttpMessage reply = new HttpMessage("ok");
        reply.status(HttpStatus.OK);
        reply.cookie(new Cookie("session", "abc"));

        VertxHttpMessageMapper.ResponsePlan plan =
                VertxHttpMessageMapper.toResponsePlan(reply, endpointConfiguration, false);

        assertTrue(plan.headers().containsKey("Set-Cookie"));
        assertTrue(plan.headers().get("Set-Cookie").get(0).startsWith("session=abc"));
    }

    @Test
    public void plainMessageReplyIsWrapped() {
        VertxHttpMessageMapper.ResponsePlan plan = VertxHttpMessageMapper.toResponsePlan(
                new DefaultMessage("wrapped"), endpointConfiguration, false);

        assertEquals(plan.statusCode(), HttpStatus.OK.value());
        assertEquals(new String(plan.body(), StandardCharsets.UTF_8), "wrapped");
    }

    @Test
    public void snapshotRejectsBlankMethod() {
        try {
            snapshot(" ", "/api", null, null, Map.of());
            throw new AssertionError("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("method"));
        }
    }

    private static VertxHttpMessageMapper.RequestSnapshot snapshot(String method, String path,
            String query, byte[] body, Map<String, List<String>> headers) {
        return new VertxHttpMessageMapper.RequestSnapshot(method, path, path, query,
                "HTTP/1.1", headers, body, "g1-x1", 1L);
    }

    private static String singleHeader(Map<String, List<String>> headers, String name) {
        List<String> values = headers.get(name);
        assertNotNull(values, "Missing header: " + name);
        assertEquals(values.size(), 1, "Expected single value for header: " + name);
        return values.get(0);
    }

    @org.testng.annotations.DataProvider(name = "httpMethods")
    public static Object[][] httpMethods() {
        return new Object[][] {
                {"GET"}, {"POST"}, {"PUT"}, {"PATCH"}, {"DELETE"}, {"OPTIONS"}, {"HEAD"}, {"TRACE"}
        };
    }
}
