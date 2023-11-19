/*
 * Copyright 2006-2014 the original author or authors.
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

package org.citrusframework.http.message;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import jakarta.servlet.http.Cookie;
import org.citrusframework.context.TestContext;
import org.citrusframework.http.client.HttpEndpointConfiguration;
import org.citrusframework.message.Message;
import org.citrusframework.message.MessageConverter;
import org.citrusframework.message.MessageHeaderUtils;
import org.citrusframework.message.MessageHeaders;
import org.citrusframework.util.StringUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Message converter implementation able to convert HTTP request and response entities to internal message
 * representation and other way round.
 *
 * @since 2.0
 */
public class HttpMessageConverter implements MessageConverter<HttpEntity<?>, HttpEntity<?>, HttpEndpointConfiguration> {

    private final CookieConverter cookieConverter;

    public HttpMessageConverter() {
        cookieConverter = new CookieConverter();
    }

    public HttpMessageConverter(CookieConverter cookieConverter) {
        this.cookieConverter = cookieConverter;
    }

    private static String resolveCookieValue(TestContext context, Cookie cookie) {
        return Objects.isNull(context) ? cookie.getValue() : context.replaceDynamicContentInString(cookie.getValue());
    }

    @Override
    public HttpEntity<?> convertOutbound(Message message,
                                         HttpEndpointConfiguration endpointConfiguration,
                                         TestContext context) {

        HttpMessage httpMessage = convertOutboundMessage(message);

        HttpHeaders httpHeaders = createHttpHeaders(httpMessage, endpointConfiguration);

        for (Cookie cookie : httpMessage.getCookies()) {
            httpHeaders.add(
                    HttpHeaders.COOKIE,
                    cookie.getName() + "=" + resolveCookieValue(context, cookie));
        }

        Object payload = httpMessage.getPayload();
        if (httpMessage.getStatusCode() != null) {
            return new ResponseEntity<>(payload, httpHeaders, httpMessage.getStatusCode());
        }

        RequestMethod method = determineRequestMethod(endpointConfiguration, httpMessage);

        return createHttpEntity(httpHeaders, payload, method);
    }

    @Override
    public HttpMessage convertInbound(HttpEntity<?> message,
                                      HttpEndpointConfiguration endpointConfiguration,
                                      TestContext context) {
        Map<String, Object> mappedHeaders = endpointConfiguration.getHeaderMapper().toHeaders(message.getHeaders());
        HttpMessage httpMessage = new HttpMessage(extractMessageBody(message), convertHeaderTypes(mappedHeaders));

        for (Map.Entry<String, String> customHeader : getCustomHeaders(message.getHeaders(), mappedHeaders).entrySet()) {
            httpMessage.setHeader(customHeader.getKey(), customHeader.getValue());
        }

        if (message instanceof ResponseEntity<?>) {
            httpMessage.status(((ResponseEntity<?>) message).getStatusCode());

            // We've no information here about the HTTP Version in this context.
            // Because HTTP/2 is not supported anyway currently, this should be acceptable.
            httpMessage.version("HTTP/1.1");

            if (endpointConfiguration.isHandleCookies()) {
                httpMessage.setCookies(cookieConverter.convertCookies(message));
            }
        }

        return httpMessage;
    }

    @Override
    public void convertOutbound(HttpEntity externalMessage,
                                Message internalMessage,
                                HttpEndpointConfiguration endpointConfiguration,
                                TestContext context) {
        throw new UnsupportedOperationException("HttpMessageConverter does not support predefined HttpEntity objects");
    }



    /**
     * Message headers consist of standard HTTP message headers and custom headers.
     * This method assumes that all header entries that were not initially mapped
     * by header mapper implementations are custom headers.
     *
     * @param httpHeaders all message headers in their pre nature.
     * @param mappedHeaders the previously mapped header entries (all standard headers).
     * @return The map of custom headers
     */
    private Map<String, String> getCustomHeaders(HttpHeaders httpHeaders, Map<String, Object> mappedHeaders) {
        Map<String, String> customHeaders = new HashMap<>();

        for (Map.Entry<String, List<String>> header : httpHeaders.entrySet()) {
            if (!mappedHeaders.containsKey(header.getKey())) {
                customHeaders.put(header.getKey(), String.join(",", header.getValue()));
            }
        }

        return customHeaders;
    }

    /**
     * Checks for collection typed header values and convert them to comma delimited String.
     * We need this for further header processing e.g when forwarding headers to JMS queues.
     *
     * @param headers the http request headers.
     */
    private Map<String, Object> convertHeaderTypes(Map<String, Object> headers) {
        Map<String, Object> convertedHeaders = new HashMap<>();

        for (Map.Entry<String, Object> header : headers.entrySet()) {
            if (header.getValue() instanceof Collection<?>) {
                Collection<?> value = (Collection<?>)header.getValue();
                convertedHeaders.put(header.getKey(), value.stream().map(String::valueOf).collect(Collectors.joining(",")));
            } else if (header.getValue() instanceof MediaType) {
                convertedHeaders.put(header.getKey(), header.getValue().toString());
            } else {
                convertedHeaders.put(header.getKey(), header.getValue());
            }
        }

        return convertedHeaders;
    }

    /**
     * Creates HttpHeaders based on the outbound message and the endpoint configurations header mapper.
     * @param httpMessage The HttpMessage to copy the headers from
     * @param endpointConfiguration The endpoint configuration to get th header mapper from
     */
    private HttpHeaders createHttpHeaders(HttpMessage httpMessage,
                                          HttpEndpointConfiguration endpointConfiguration) {

        HttpHeaders httpHeaders = new HttpHeaders();

        endpointConfiguration
                .getHeaderMapper()
                .fromHeaders(
                        new org.springframework.messaging.MessageHeaders(httpMessage.getHeaders()),
                        httpHeaders);

        Map<String, Object> messageHeaders = httpMessage.getHeaders();
        for (Map.Entry<String, Object> header : messageHeaders.entrySet()) {
            if (!header.getKey().startsWith(MessageHeaders.PREFIX) &&
                    !MessageHeaderUtils.isSpringInternalHeader(header.getKey()) &&
                    !httpHeaders.containsKey(header.getKey())) {
                httpHeaders.add(header.getKey(), header.getValue().toString());
            }
        }

        if (httpHeaders.getFirst(HttpMessageHeaders.HTTP_CONTENT_TYPE) == null) {
            httpHeaders.add(HttpMessageHeaders.HTTP_CONTENT_TYPE, composeContentTypeHeaderValue(endpointConfiguration));
        }

        return httpHeaders;
    }

    /**
     *
     * @param endpointConfiguration The HttpEndpointConfiguration to get the default request method from
     * @param httpMessage The HttpMessage to override the default with if necessary
     * @return The HttpMethod of the message to send
     */
    private RequestMethod determineRequestMethod(HttpEndpointConfiguration endpointConfiguration,
                                                 HttpMessage httpMessage) {
        RequestMethod method = endpointConfiguration.getRequestMethod();
        if (httpMessage.getRequestMethod() != null) {
            method = httpMessage.getRequestMethod();
        }
        return method;
    }

    /**
     * Composes a HttpEntity based on the given parameters
     * @param httpHeaders The headers to set
     * @param payload The payload to set
     * @param method The HttpMethod to use
     * @return The composed HttpEntitiy
     */
    private HttpEntity<?> createHttpEntity(HttpHeaders httpHeaders, Object payload, RequestMethod method) {
        if (httpMethodSupportsBody(method)) {
            return new HttpEntity<>(payload, httpHeaders);
        } else {
            return new HttpEntity<>(httpHeaders);
        }
    }

    /**
     * Converts the outbound Message object into a HttpMessage
     * @param message The message to convert
     * @return The converted message as HttpMessage
     */
    private HttpMessage convertOutboundMessage(Message message) {
        HttpMessage httpMessage;
        if (message instanceof HttpMessage) {
            httpMessage = (HttpMessage) message;
        } else {
            httpMessage = new HttpMessage(message);
        }
        return httpMessage;
    }

    /**
     * Determines whether the given message type supports a message body
     * @param method The HttpMethod to evaluate
     * @return Whether a message body is supported
     */
    private boolean httpMethodSupportsBody(RequestMethod method) {
        return RequestMethod.POST.equals(method) || RequestMethod.PUT.equals(method)
                || RequestMethod.DELETE.equals(method) || RequestMethod.PATCH.equals(method);
    }

    /**
     * Creates the content type header value enriched with charset information if possible
     * @param endpointConfiguration The endpoint configuration to get the content type from
     * @return The content type header including charset information
     */
    private String composeContentTypeHeaderValue(HttpEndpointConfiguration endpointConfiguration) {
        return (endpointConfiguration.getContentType().contains("charset") || !StringUtils.hasText(endpointConfiguration.getCharset())) ?
                endpointConfiguration.getContentType() :
                endpointConfiguration.getContentType() + ";charset=" + endpointConfiguration.getCharset();
    }

    /**
     * Extracts the message body from the given HttpEntity or returns a default
     * @param message The message to extract the body from
     * @return The body of the HttpEntity or a default value, if no payload is available
     */
    private Object extractMessageBody(HttpEntity<?> message) {
        return message.getBody() != null ? message.getBody() : "";
    }
}
