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

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import jakarta.servlet.http.Cookie;
import org.citrusframework.http.client.HttpEndpointConfiguration;
import org.citrusframework.http.message.HttpMessage;
import org.citrusframework.http.message.HttpMessageConverter;
import org.citrusframework.http.message.HttpMessageHeaders;
import org.citrusframework.message.Message;
import org.citrusframework.message.MessageHeaderUtils;
import org.citrusframework.util.StringUtils;
import org.springframework.util.MultiValueMap;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.ResponseCookie;
import org.springframework.web.util.UrlPathHelper;
import org.springframework.web.util.UriUtils;

/**
 * Maps between raw HTTP wire data and Citrus {@link HttpMessage}s, mirroring the semantics of
 * {@code HttpMessageController}: inbound conversion reuses the configured
 * {@link HttpMessageConverter} plus controller-compatible enrichment (path, uri, query params,
 * version, method, cookies); outbound conversion reuses the same converter and applies the
 * configured default status code when the reply carries none.
 */
public final class VertxHttpMessageMapper {

    public static final String EXCHANGE_ID_HEADER = "citrus_vertx_exchange_id";
    public static final String GENERATION_HEADER = "citrus_vertx_generation";

    private VertxHttpMessageMapper() {
    }

    /**
     * Snapshot of a single inbound HTTP request. Header values keep wire order; body may be null.
     * Attributes are staged on the event loop before dispatch (the Vert.x analog of servlet
     * request attributes set by container filters) and are exposed as message headers only when
     * the endpoint configuration enables {@code handleAttributeHeaders}.
     */
    public record RequestSnapshot(String method, String uri, String path, String query, String version,
                                  Map<String, List<String>> headers, byte[] body,
                                  String exchangeId, long generation, Map<String, Object> attributes) {
        public RequestSnapshot {
            if (method == null || method.isBlank()) {
                throw new IllegalArgumentException("Request method must not be blank");
            }
            if (uri == null || uri.isBlank()) {
                throw new IllegalArgumentException("Request uri must not be blank");
            }
            if (path == null || path.isBlank()) {
                throw new IllegalArgumentException("Request path must not be blank");
            }
            if (headers == null) {
                throw new IllegalArgumentException("Request headers must not be null");
            }
            attributes = attributes == null ? Map.of()
                    : java.util.Collections.unmodifiableMap(new LinkedHashMap<>(attributes));
        }

        public RequestSnapshot(String method, String uri, String path, String query, String version,
                               Map<String, List<String>> headers, byte[] body,
                               String exchangeId, long generation) {
            this(method, uri, path, query, version, headers, body, exchangeId, generation, Map.of());
        }
    }

    /**
     * Outbound write plan for a single HTTP response.
     */
    public record ResponsePlan(int statusCode, Map<String, List<String>> headers, byte[] body,
                               boolean suppressBody) {
    }

    /**
     * Convert an inbound request snapshot to a Citrus {@link HttpMessage}.
     */
    public static HttpMessage toHttpMessage(RequestSnapshot snapshot, HttpEndpointConfiguration endpointConfiguration) {
        return toHttpMessage(snapshot, endpointConfiguration, List.of());
    }

    public static HttpMessage toHttpMessage(RequestSnapshot snapshot, HttpEndpointConfiguration endpointConfiguration,
                                            List<VertxMessageBodyConverter<?>> converters) {
        HttpHeaders httpHeaders = new HttpHeaders();
        snapshot.headers().forEach((name, values) -> {
            if (values != null) {
                values.forEach(value -> httpHeaders.add(name, value));
            }
        });

        String contentType = firstHeader(snapshot.headers(), HttpMessageHeaders.HTTP_CONTENT_TYPE);
        VertxMessageBodyConverter<?> reader = converters.stream()
                .filter(registration -> registration.converter().canRead(registration.payloadType(), httpHeaders.getContentType()))
                .findFirst().orElse(null);
        Object body = reader == null ? toInboundPayload(snapshot.body(), contentType, endpointConfiguration)
                : reader.read(httpHeaders, snapshot.body());

        HttpMessage message = endpointConfiguration.getMessageConverter()
                .convertInbound(new HttpEntity<>(body, httpHeaders), endpointConfiguration, null);

        UrlPathHelper pathHelper = new UrlPathHelper();
        pathHelper.setRemoveSemicolonContent(endpointConfiguration.isRemoveSemicolonPathContent());
        String path = pathHelper.removeSemicolonContent(snapshot.path());
        String requestCharset = charsetParameter(contentType);
        path = UriUtils.decode(path, StringUtils.hasText(requestCharset) ? requestCharset : "ISO-8859-1")
                .replaceAll("/{2,}", "/");

        message.path(path)
                .uri(path)
                .contextPath("")
                .queryParams(snapshot.query() == null || snapshot.query().isBlank() ? ""
                        : MessageHeaderUtils.encodeQueryParam(snapshot.query()).replace("&", ","))
                .version(snapshot.version())
                .method(HttpMethod.valueOf(snapshot.method()));

        if (endpointConfiguration.isHandleCookies()) {
            parseRequestCookies(snapshot.headers()).forEach(message::cookie);
        }

        message.setHeader(EXCHANGE_ID_HEADER, snapshot.exchangeId());
        message.setHeader(GENERATION_HEADER, snapshot.generation());

        if (endpointConfiguration.isHandleAttributeHeaders()) {
            // Mirrors HttpMessageController: staged request attributes become message headers
            // verbatim, overwriting any header of the same name. Null names and values carry no
            // servlet-attribute meaning (setAttribute(name, null) removes) and are skipped.
            snapshot.attributes().forEach((name, value) -> {
                if (name != null && value != null) {
                    message.setHeader(name, value);
                }
            });
        }

        return message;
    }

    /**
     * Convert a Citrus reply message to a response write plan. A null reply (adapter timeout or
     * no reply) maps to an empty body with the configured default status code - never 504.
     */
    public static ResponsePlan toResponsePlan(Message replyMessage,
                                              HttpEndpointConfiguration endpointConfiguration,
                                              boolean headRequest) {
        return toResponsePlan(replyMessage, endpointConfiguration, headRequest, List.of());
    }

    public static ResponsePlan toResponsePlan(Message replyMessage, HttpEndpointConfiguration endpointConfiguration,
                                              boolean headRequest, List<VertxMessageBodyConverter<?>> converters) {
        if (replyMessage == null) {
            return emptyPlan(endpointConfiguration.getDefaultStatusCode(), endpointConfiguration, headRequest);
        }

        HttpMessage httpResponse = replyMessage instanceof HttpMessage httpMessage
                ? httpMessage : new HttpMessage(replyMessage);

        if (httpResponse.getStatusCode() == null) {
            httpResponse.status(org.springframework.http.HttpStatusCode.valueOf(endpointConfiguration.getDefaultStatusCode()));
        }

        ResponseEntity<?> responseEntity = (ResponseEntity<?>) endpointConfiguration.getMessageConverter()
                .convertOutbound(httpResponse, endpointConfiguration, null);

        Map<String, List<String>> headers = new LinkedHashMap<>();
        responseEntity.getHeaders().headerSet().forEach(entry -> {
            if (!HttpHeaders.CONTENT_LENGTH.equalsIgnoreCase(entry.getKey())
                    && !HttpHeaders.TRANSFER_ENCODING.equalsIgnoreCase(entry.getKey())) {
                headers.put(entry.getKey(), new ArrayList<>(entry.getValue()));
            }
        });

        if (endpointConfiguration.isHandleCookies() && httpResponse.getCookies() != null) {
            httpResponse.getCookies().forEach(cookie -> headers
                    .computeIfAbsent(HttpHeaders.SET_COOKIE, key -> new ArrayList<>())
                    .add(renderSetCookie(cookie)));
        }

        String contentType = singleContentType(headers);
        Object payload = responseEntity.getBody();
        VertxMessageBodyConverter<?> writer = converters.stream()
                .filter(registration -> payload != null && registration.payloadType().isInstance(payload)
                        && registration.converter().canWrite(payload.getClass(), responseEntity.getHeaders().getContentType()))
                .findFirst().orElse(null);
        byte[] body;
        if (writer != null) {
            body = writer.write(payload, headers);
        } else if (responseEntity.getBody() instanceof MultiValueMap<?, ?> fields
                && VertxHttpFormConverter.isWritable(contentType)) {
            @SuppressWarnings("unchecked")
            MultiValueMap<String, ?> formFields = (MultiValueMap<String, ?>) fields;
            body = VertxHttpFormConverter.write(formFields, headers, contentType,
                    inboundCharset(contentType, endpointConfiguration));
        } else {
            body = toOutboundBytes(responseEntity.getBody(), contentType, endpointConfiguration);
        }
        headers.keySet().removeIf(name -> HttpHeaders.CONTENT_LENGTH.equalsIgnoreCase(name)
                || HttpHeaders.TRANSFER_ENCODING.equalsIgnoreCase(name));
        headers.put(HttpHeaders.CONTENT_LENGTH, List.of(String.valueOf(body.length)));

        if (headRequest) {
            return new ResponsePlan(responseEntity.getStatusCode().value(), headers, new byte[0], true);
        }
        return new ResponsePlan(responseEntity.getStatusCode().value(), headers, body, false);
    }

    private static ResponsePlan emptyPlan(int statusCode, HttpEndpointConfiguration endpointConfiguration, boolean headRequest) {
        Map<String, List<String>> headers = new LinkedHashMap<>();
        headers.put(HttpMessageHeaders.HTTP_CONTENT_TYPE,
                List.of(composeContentType(endpointConfiguration)));
        headers.put(HttpHeaders.CONTENT_LENGTH, List.of("0"));
        return new ResponsePlan(statusCode, headers, new byte[0], headRequest);
    }

    private static Object toInboundPayload(byte[] rawBody, String contentType, HttpEndpointConfiguration endpointConfiguration) {
        if (rawBody == null || rawBody.length == 0) {
            return "";
        }
        if (isBinary(contentType, endpointConfiguration)) {
            return rawBody.clone();
        }
        if (VertxHttpFormConverter.isForm(contentType)) {
            return VertxHttpFormConverter.read(rawBody, contentType, inboundCharset(contentType, endpointConfiguration));
        }
        return new String(rawBody, inboundCharset(contentType, endpointConfiguration));
    }

    private static boolean isBinary(String contentType, HttpEndpointConfiguration endpointConfiguration) {
        if (!StringUtils.hasText(contentType)) {
            return false;
        }
        MediaType parsed;
        try {
            parsed = MediaType.parseMediaType(contentType);
        } catch (IllegalArgumentException e) {
            return false;
        }
        return endpointConfiguration.getBinaryMediaTypes().stream().anyMatch(parsed::isCompatibleWith);
    }

    private static Charset inboundCharset(String contentType, HttpEndpointConfiguration endpointConfiguration) {
        String charsetName = charsetParameter(contentType);
        if (!StringUtils.hasText(charsetName)) {
            charsetName = endpointConfiguration.getCharset();
        }
        try {
            return StringUtils.hasText(charsetName) ? Charset.forName(charsetName) : StandardCharsets.UTF_8;
        } catch (IllegalArgumentException e) {
            return StandardCharsets.UTF_8;
        }
    }

    private static byte[] toOutboundBytes(Object body, String contentType, HttpEndpointConfiguration endpointConfiguration) {
        if (body == null) {
            return new byte[0];
        }
        if (body instanceof byte[] bytes) {
            return bytes;
        }
        Charset charset = inboundCharset(contentType, endpointConfiguration);
        if (body instanceof String text) {
            return text.getBytes(charset);
        }
        return String.valueOf(body).getBytes(charset);
    }

    private static String charsetParameter(String contentType) {
        if (!StringUtils.hasText(contentType)) {
            return null;
        }
        for (String parameter : contentType.split(";")) {
            String candidate = parameter.trim();
            if (candidate.regionMatches(true, 0, "charset=", 0, 8)) {
                String value = candidate.substring(8).trim();
                if (value.length() > 1 && value.startsWith("\"") && value.endsWith("\"")) {
                    value = value.substring(1, value.length() - 1);
                }
                return value;
            }
        }
        return null;
    }

    private static String singleContentType(Map<String, List<String>> headers) {
        return firstHeader(headers, HttpMessageHeaders.HTTP_CONTENT_TYPE);
    }

    private static String firstHeader(Map<String, List<String>> headers, String name) {
        for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
            if (entry.getKey() != null && entry.getKey().equalsIgnoreCase(name)
                    && entry.getValue() != null && !entry.getValue().isEmpty()) {
                return entry.getValue().get(0);
            }
        }
        return null;
    }

    private static String composeContentType(HttpEndpointConfiguration endpointConfiguration) {
        if (endpointConfiguration.getContentType().contains("charset")
                || !StringUtils.hasText(endpointConfiguration.getCharset())) {
            return endpointConfiguration.getContentType();
        }
        return endpointConfiguration.getContentType() + ";charset=" + endpointConfiguration.getCharset();
    }

    private static List<Cookie> parseRequestCookies(Map<String, List<String>> headers) {
        List<Cookie> cookies = new ArrayList<>();
        headers.forEach((name, values) -> {
            if (name != null && name.equalsIgnoreCase(HttpHeaders.COOKIE) && values != null) {
                values.forEach(headerValue -> {
                    if (headerValue != null) {
                        for (String pair : headerValue.split(";")) {
                            int separator = pair.indexOf('=');
                            if (separator > 0) {
                                cookies.add(new Cookie(pair.substring(0, separator).trim(),
                                        pair.substring(separator + 1).trim()));
                            }
                        }
                    }
                });
            }
        });
        return cookies;
    }

    private static String renderSetCookie(Cookie cookie) {
        return ResponseCookie.from(cookie.getName(), cookie.getValue() == null ? "" : cookie.getValue())
                .path(cookie.getPath()).domain(cookie.getDomain()).maxAge(cookie.getMaxAge())
                .secure(cookie.getSecure()).httpOnly(cookie.isHttpOnly())
                .sameSite(cookie.getAttribute("SameSite")).build().toString();
    }
}
