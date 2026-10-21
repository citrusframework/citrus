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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import jakarta.servlet.http.Cookie;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.http.client.HttpEndpointConfiguration;
import org.citrusframework.http.message.CookieConverter;
import org.citrusframework.http.message.DelegatingHttpEntityMessageConverter;
import org.citrusframework.http.message.HttpMessage;
import org.citrusframework.http.message.HttpMessageHeaders;
import org.citrusframework.message.Message;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

/**
 * Writes the reply of a Citrus message to the client. The writer converts the message to an Http
 * entity, resolves the content type of the response and writes the body using the delegating
 * message converter. Transport managed headers such as {@code Transfer-Encoding},
 * {@code Connection} and {@code Content-Length} are never copied from the message.
 */
class VertxResponseWriter {

    /** Transport managed headers that are computed by the server and never copied from the message */
    private static final List<String> TRANSPORT_MANAGED_HEADERS = List.of(
            HttpHeaders.CONTENT_LENGTH, HttpHeaders.TRANSFER_ENCODING, HttpHeaders.CONNECTION);

    private final HttpEndpointConfiguration configuration;
    private final DelegatingHttpEntityMessageConverter entityConverter;
    private final boolean useDefaultFilters;
    private final CookieConverter cookieConverter = new CookieConverter();

    VertxResponseWriter(HttpEndpointConfiguration configuration, DelegatingHttpEntityMessageConverter entityConverter,
                        boolean useDefaultFilters) {
        this.configuration = configuration;
        this.entityConverter = entityConverter;
        this.useDefaultFilters = useDefaultFilters;
    }

    /**
     * Creates the response for the given reply.
     *
     * @param reply the reply message, may be {@code null} when no reply was received.
     * @param request the request snapshot the reply belongs to.
     * @return the response snapshot.
     */
    VertxResponseSnapshot write(Message reply, VertxRequestSnapshot request) {
        if (reply == null) {
            return new VertxResponseSnapshot(configuration.getDefaultStatusCode(), Map.of(), new byte[0]);
        }

        HttpMessage httpResponse = reply instanceof HttpMessage ? (HttpMessage) reply : new HttpMessage(reply);

        if (httpResponse.getStatusCode() == null) {
            httpResponse.status(HttpStatusCode.valueOf(configuration.getDefaultStatusCode()));
        }

        MediaType explicitContentType = getExplicitContentType(httpResponse);

        HttpEntity<?> entity = (HttpEntity<?>) configuration.getMessageConverter()
                .convertOutbound(httpResponse, configuration, null);

        int status = getStatus(entity);
        MediaType contentType = resolveContentType(entity, explicitContentType, request);

        Map<String, List<String>> headers = copyHeaders(entity);
        addCookies(headers, httpResponse);
        headers.put(HttpHeaders.CONTENT_TYPE, List.of(contentType.toString()));

        byte[] body = new byte[0];
        boolean noContent = HttpStatus.NO_CONTENT.equals(httpResponse.getStatusCode());

        if (!noContent && entity.getBody() != null) {
            body = writeBody(entity.getBody(), contentType);

            if (useDefaultFilters && isGzipAccepted(request)) {
                body = compress(body);
                headers.put(HttpHeaders.CONTENT_ENCODING, List.of("gzip"));
            }
        }

        if (body.length > 0) {
            headers.put(HttpHeaders.CONTENT_LENGTH, List.of(String.valueOf(body.length)));
        }

        return new VertxResponseSnapshot(status, headers, body);
    }

    /**
     * Checks if the client accepts a gzip encoded response.
     */
    private boolean isGzipAccepted(VertxRequestSnapshot request) {
        if (request == null || request.headers() == null) {
            return false;
        }

        return VertxGzip.isGzipEncoding(request.headers().get(HttpHeaders.ACCEPT_ENCODING));
    }

    /**
     * Compresses the response body with gzip.
     */
    private byte[] compress(byte[] body) {
        try {
            return VertxGzip.encode(body);
        } catch (IOException e) {
            throw new CitrusRuntimeException("Failed to compress Http response body", e);
        }
    }

    /**
     * Gets the status code of the given entity.
     */
    private int getStatus(HttpEntity<?> entity) {
        if (entity instanceof ResponseEntity<?> responseEntity) {
            return responseEntity.getStatusCode().value();
        }

        return HttpStatusCode.valueOf(configuration.getDefaultStatusCode()).value();
    }

    /**
     * Gets the content type that was explicitly set on the reply message.
     */
    private MediaType getExplicitContentType(HttpMessage httpResponse) {
        String contentType = getHeaderValue(httpResponse, HttpMessageHeaders.HTTP_CONTENT_TYPE);

        if (contentType == null) {
            contentType = getHeaderValue(httpResponse, HttpHeaders.CONTENT_TYPE);
        }

        if (contentType == null || contentType.isBlank()) {
            return null;
        }

        try {
            return MediaType.parseMediaType(contentType);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * Resolves the content type of the response. An explicitly set content type always wins.
     * Otherwise the first writable type that is compatible with the accepted content types of the
     * request is used. The default content type of the converter is the last resort.
     */
    private MediaType resolveContentType(HttpEntity<?> entity, MediaType explicitContentType,
                                         VertxRequestSnapshot request) {
        if (explicitContentType != null) {
            return explicitContentType;
        }

        MediaType defaultContentType = entity.getHeaders().getContentType();
        if (defaultContentType == null) {
            defaultContentType = MediaType.TEXT_PLAIN;
        }

        List<MediaType> accepted = getAcceptedMediaTypes(request);
        if (accepted.isEmpty()) {
            return defaultContentType;
        }

        for (MediaType candidate : getWritableMediaTypes(defaultContentType)) {
            for (MediaType accept : accepted) {
                if (accept.isCompatibleWith(candidate)) {
                    return candidate;
                }
            }
        }

        return defaultContentType;
    }

    /**
     * Gets all media types that the server is able to write.
     */
    private List<MediaType> getWritableMediaTypes(MediaType defaultContentType) {
        List<MediaType> mediaTypes = new ArrayList<>();
        mediaTypes.add(defaultContentType);

        if (configuration.getBinaryMediaTypes() != null) {
            mediaTypes.addAll(configuration.getBinaryMediaTypes());
        }

        mediaTypes.add(MediaType.TEXT_PLAIN);
        mediaTypes.add(MediaType.APPLICATION_FORM_URLENCODED);

        return mediaTypes;
    }

    /**
     * Gets the accepted media types of the request.
     */
    private List<MediaType> getAcceptedMediaTypes(VertxRequestSnapshot request) {
        if (request == null || request.headers() == null) {
            return List.of();
        }

        String acceptHeader = request.headers().get(HttpHeaders.ACCEPT);
        if (acceptHeader == null || acceptHeader.isBlank()) {
            return List.of();
        }

        try {
            return MediaType.parseMediaTypes(acceptHeader);
        } catch (IllegalArgumentException e) {
            return List.of();
        }
    }

    /**
     * Copies all headers of the given entity excluding the transport managed headers.
     */
    private Map<String, List<String>> copyHeaders(HttpEntity<?> entity) {
        Map<String, List<String>> headers = new LinkedHashMap<>();

        for (Map.Entry<String, List<String>> header : entity.getHeaders().headerSet()) {
            if (TRANSPORT_MANAGED_HEADERS.stream().anyMatch(name -> name.equalsIgnoreCase(header.getKey()))) {
                continue;
            }

            headers.put(header.getKey(), List.copyOf(header.getValue()));
        }

        return headers;
    }

    /**
     * Gets a header value of the given message as string.
     */
    private String getHeaderValue(HttpMessage message, String name) {
        Object value = message.getHeader(name);
        return value != null ? value.toString() : null;
    }

    /**
     * Adds the cookies of the reply as {@code Set-Cookie} headers.
     */
    private void addCookies(Map<String, List<String>> headers, HttpMessage httpResponse) {
        if (!configuration.isHandleCookies() || httpResponse.getCookies() == null) {
            return;
        }

        for (Cookie cookie : httpResponse.getCookies()) {
            headers.computeIfAbsent(HttpHeaders.SET_COOKIE, name -> new ArrayList<>())
                    .add(cookieConverter.getCookieString(cookie));
        }
    }

    /**
     * Writes the given payload using the delegating message converter.
     */
    private byte[] writeBody(Object payload, MediaType contentType) {
        if (payload == null) {
            return new byte[0];
        }

        ByteArrayHttpOutputMessage outputMessage = new ByteArrayHttpOutputMessage();
        outputMessage.getHeaders().setContentType(contentType);

        try {
            entityConverter.write(payload, contentType, outputMessage);
        } catch (IOException e) {
            throw new CitrusRuntimeException("Failed to write Http response body", e);
        }

        return outputMessage.getBody().toByteArray();
    }

    /**
     * Simple Http output message backed by a byte array.
     */
    private static final class ByteArrayHttpOutputMessage implements HttpOutputMessage {

        private final HttpHeaders headers = new HttpHeaders();
        private final ByteArrayOutputStream body = new ByteArrayOutputStream();

        @Override
        public ByteArrayOutputStream getBody() {
            return body;
        }

        @Override
        public HttpHeaders getHeaders() {
            return headers;
        }
    }
}
