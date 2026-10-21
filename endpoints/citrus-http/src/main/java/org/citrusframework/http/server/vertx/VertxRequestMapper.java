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
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;

import io.vertx.core.MultiMap;
import jakarta.servlet.http.Cookie;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.http.client.HttpEndpointConfiguration;
import org.citrusframework.http.message.DelegatingHttpEntityMessageConverter;
import org.citrusframework.http.message.HttpMessage;
import org.citrusframework.message.MessageHeaderUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.util.UriUtils;
import org.springframework.web.util.UrlPathHelper;

/**
 * Maps an incoming Http request to a Citrus message. The mapping follows the very same rules as
 * the Jetty based server does: the request path is decoded and cleaned up like Spring's
 * {@link UrlPathHelper} does, the query string is encoded with {@link MessageHeaderUtils} and all
 * headers are copied to the message.
 */
class VertxRequestMapper {

    /** Default character encoding used to decode the request path when the request does not specify one */
    static final Charset DEFAULT_PATH_CHARSET = StandardCharsets.ISO_8859_1;

    private final HttpEndpointConfiguration configuration;
    private final DelegatingHttpEntityMessageConverter entityConverter;
    private final UrlPathHelper pathHelper = new UrlPathHelper();
    private final String contextPath;

    VertxRequestMapper(HttpEndpointConfiguration configuration, DelegatingHttpEntityMessageConverter entityConverter,
                        String contextPath) {
        this.configuration = configuration;
        this.entityConverter = entityConverter;
        this.contextPath = contextPath;
        this.pathHelper.setRemoveSemicolonContent(configuration.isRemoveSemicolonPathContent());
    }

    /**
     * Maps the given request snapshot to a Citrus message.
     *
     * @param snapshot the request snapshot.
     * @return the Citrus message representing the request.
     */
    HttpMessage map(VertxRequestSnapshot snapshot) {
        HttpHeaders headers = createHeaders(snapshot.headers());
        Object payload = readPayload(headers, snapshot.body());

        HttpEntity<?> entity = new HttpEntity<>(payload, headers);
        HttpMessage request = configuration.getMessageConverter().convertInbound(entity, configuration, null);

        copyMissingHeaders(request, snapshot.headers());

        if (configuration.isHandleCookies() && snapshot.cookies() != null && !snapshot.cookies().isEmpty()) {
            request.setCookies(snapshot.cookies().toArray(new Cookie[0]));
        }

        String requestUri = getRequestUri(snapshot.path(), getPathCharset(headers));

        request.path(requestUri)
                .uri(requestUri)
                .contextPath(getContextPath())
                .queryParams(getQueryParams(snapshot.query()))
                .version(snapshot.version())
                .method(getRequestMethod(snapshot.method()));

        return request;
    }

    /**
     * Creates the Spring Http headers from the Vert.x request headers.
     */
    private HttpHeaders createHeaders(MultiMap headers) {
        HttpHeaders httpHeaders = new HttpHeaders();

        if (headers != null) {
            for (String name : headers.names()) {
                List<String> values = headers.getAll(name);
                for (String value : values) {
                    httpHeaders.add(name, value);
                }
            }
        }

        return httpHeaders;
    }

    /**
     * Reads the request body using the delegating message converter. An empty body results in a
     * {@code null} payload. A missing content type is handled as {@code application/octet-stream}.
     */
    private Object readPayload(HttpHeaders headers, byte[] body) {
        if (body == null || body.length == 0) {
            return null;
        }

        HttpHeaders readHeaders = new HttpHeaders();
        readHeaders.putAll(headers);

        if (readHeaders.getContentType() == null) {
            readHeaders.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        }

        try {
            return entityConverter.read(Object.class, new ByteArrayHttpInputMessage(body, readHeaders));
        } catch (IOException e) {
            throw new CitrusRuntimeException("Failed to read Http request body", e);
        }
    }

    /**
     * Copies all request headers that are not already present on the message. A header without a
     * value is mapped to an empty string.
     */
    private void copyMissingHeaders(HttpMessage request, MultiMap headers) {
        if (headers == null) {
            return;
        }

        for (String name : headers.names()) {
            if (request.getHeader(name) == null) {
                String value = headers.get(name);
                request.header(name, value != null ? value : "");
            }
        }
    }

    /**
     * Gets the request path relative to the context path, decoded and cleaned up like Spring does.
     */
    private String getRequestUri(String path, Charset charset) {
        String uri = path != null ? path : "";

        if (!"/".equals(contextPath) && uri.startsWith(contextPath)) {
            uri = uri.substring(contextPath.length());
        }

        if (uri.isEmpty()) {
            uri = "/";
        }

        if (configuration.isRemoveSemicolonPathContent()) {
            uri = pathHelper.removeSemicolonContent(uri);
        }

        uri = decode(uri, charset);

        return sanitizePath(uri);
    }

    /**
     * Decodes the given path using the given character encoding.
     */
    private String decode(String uri, Charset charset) {
        try {
            return UriUtils.decode(uri, charset);
        } catch (IllegalArgumentException e) {
            return uri;
        }
    }

    /**
     * Gets the character encoding used to decode the request path. The charset of the request
     * content type is used when it defines one. Otherwise the default encoding of the servlet
     * container applies.
     */
    private Charset getPathCharset(HttpHeaders headers) {
        MediaType contentType = headers.getContentType();

        if (contentType != null && contentType.getCharset() != null) {
            return contentType.getCharset();
        }

        return DEFAULT_PATH_CHARSET;
    }

    /**
     * Removes duplicate slashes from the given path.
     */
    private static String sanitizePath(String path) {
        int start = path.indexOf("//");
        if (start == -1) {
            return path;
        }

        char[] content = path.toCharArray();
        int slowIndex = start;

        for (int fastIndex = start + 1; fastIndex < content.length; fastIndex++) {
            if (content[fastIndex] != '/' || content[slowIndex] != '/') {
                content[++slowIndex] = content[fastIndex];
            }
        }

        return new String(content, 0, slowIndex + 1);
    }

    /**
     * Gets the context path of the request. A context path of {@code /} is adapted to an empty
     * context path.
     */
    private String getContextPath() {
        if (contextPath == null || "/".equals(contextPath)) {
            return "";
        }

        return contextPath;
    }

    /**
     * Gets the encoded query parameters of the request.
     */
    private String getQueryParams(String query) {
        if (query == null || query.isEmpty()) {
            return "";
        }

        return MessageHeaderUtils.encodeQueryParam(query).replace("&", ",");
    }

    /**
     * Gets the request method.
     */
    private HttpMethod getRequestMethod(String method) {
        try {
            return HttpMethod.valueOf(method);
        } catch (IllegalArgumentException e) {
            throw new CitrusRuntimeException("Unsupported Http request method: %s".formatted(method), e);
        }
    }

    /**
     * Simple Http input message backed by a byte array.
     */
    private static final class ByteArrayHttpInputMessage implements HttpInputMessage {

        private final byte[] body;
        private final HttpHeaders headers;

        private ByteArrayHttpInputMessage(byte[] body, HttpHeaders headers) {
            this.body = body;
            this.headers = headers;
        }

        @Override
        public InputStream getBody() {
            return new ByteArrayInputStream(body);
        }

        @Override
        public HttpHeaders getHeaders() {
            return headers;
        }
    }
}
