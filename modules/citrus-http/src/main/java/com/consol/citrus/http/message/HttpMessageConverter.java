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

package com.consol.citrus.http.message;

import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.http.client.HttpEndpointConfiguration;
import com.consol.citrus.message.*;
import org.springframework.http.*;
import org.springframework.util.StringUtils;

import javax.servlet.http.Cookie;
import java.util.*;

/**
 * Message converter implementation able to convert HTTP request and response entities to internal message
 * representation and other way round.
 *
 * @author Christoph Deppisch
 * @since 2.0
 */
public class HttpMessageConverter implements MessageConverter<HttpEntity<?>, HttpEndpointConfiguration> {

    @Override
    public HttpEntity<?> convertOutbound(Message message, HttpEndpointConfiguration endpointConfiguration, TestContext context) {
        HttpMessage httpMessage;
        if (message instanceof HttpMessage) {
            httpMessage = (HttpMessage) message;
        } else {
            httpMessage = new HttpMessage(message);
        }

        HttpHeaders httpHeaders = new HttpHeaders();
        endpointConfiguration.getHeaderMapper().fromHeaders(new org.springframework.messaging.MessageHeaders(httpMessage.getHeaders()), httpHeaders);

        Map<String, Object> messageHeaders = httpMessage.getHeaders();
        for (Map.Entry<String, Object> header : messageHeaders.entrySet()) {
            if (!header.getKey().startsWith(MessageHeaders.PREFIX) &&
                    !MessageHeaderUtils.isSpringInternalHeader(header.getKey()) &&
                    !httpHeaders.containsKey(header.getKey())) {
                httpHeaders.add(header.getKey(), header.getValue().toString());
            }
        }

        if (httpHeaders.getFirst(HttpMessageHeaders.HTTP_CONTENT_TYPE) == null) {
            httpHeaders.add(HttpMessageHeaders.HTTP_CONTENT_TYPE, (endpointConfiguration.getContentType().contains("charset") || !StringUtils.hasText(endpointConfiguration.getCharset())) ?
                    endpointConfiguration.getContentType() : endpointConfiguration.getContentType() + ";charset=" + endpointConfiguration.getCharset());
        }

        Object payload = httpMessage.getPayload();
        if (httpMessage.getStatusCode() != null) {
            return new ResponseEntity<>(payload, httpHeaders, httpMessage.getStatusCode());
        } else {
            for (Cookie cookie : httpMessage.getCookies()) {
                httpHeaders.set("Cookie", cookie.getName() + "=" + context.replaceDynamicContentInString(cookie.getValue()));
            }
        }

        HttpMethod method = endpointConfiguration.getRequestMethod();
        if (httpMessage.getRequestMethod() != null) {
            method = httpMessage.getRequestMethod();
        }

        if (httpMethodSupportsBody(method)) {
            return new HttpEntity<>(payload, httpHeaders);
        } else {
            return new HttpEntity<>(httpHeaders);
        }
    }

    private boolean httpMethodSupportsBody(HttpMethod method) {
        return HttpMethod.POST.equals(method) || HttpMethod.PUT.equals(method)
                || HttpMethod.DELETE.equals(method) || HttpMethod.PATCH.equals(method);
    }

    @Override
    public HttpMessage convertInbound(HttpEntity<?> message, HttpEndpointConfiguration endpointConfiguration, TestContext context) {
        Map<String, Object> mappedHeaders = endpointConfiguration.getHeaderMapper().toHeaders(message.getHeaders());
        HttpMessage httpMessage = new HttpMessage(message.getBody() != null ? message.getBody() : "", convertHeaderTypes(mappedHeaders));

        for (Map.Entry<String, String> customHeader : getCustomHeaders(message.getHeaders(), mappedHeaders).entrySet()) {
            httpMessage.setHeader(customHeader.getKey(), customHeader.getValue());
        }

        if (message instanceof ResponseEntity<?>) {
            httpMessage.status(((ResponseEntity<?>) message).getStatusCode());
            httpMessage.version("HTTP/1.1"); //TODO check if we have access to version information

            if (endpointConfiguration.isHandleCookies()) {
                List<String> cookies = message.getHeaders().get("Set-Cookie");
                if (cookies != null) {
                    for (String cookieString : cookies) {
                        Cookie cookie = new Cookie(getCookieParam("Name", cookieString), getCookieParam("Value", cookieString));

                        if (cookieString.contains("Comment")) {
                            cookie.setComment(getCookieParam("Comment", cookieString));
                        }

                        if (cookieString.contains("Path")) {
                            cookie.setPath(getCookieParam("Path", cookieString));
                        }

                        if (cookieString.contains("Domain")) {
                            cookie.setDomain(getCookieParam("Domain", cookieString));
                        }

                        if (cookieString.contains("Max-Age")) {
                            cookie.setMaxAge(Integer.valueOf(getCookieParam("Max-Age", cookieString)));
                        }

                        if (cookieString.contains("Secure")) {
                            cookie.setSecure(Boolean.valueOf(getCookieParam("Secure", cookieString)));
                        }

                        if (cookieString.contains("Version")) {
                            cookie.setVersion(Integer.valueOf(getCookieParam("Version", cookieString)));
                        }

                        httpMessage.cookie(cookie);
                    }
                }
            }
        }

        return httpMessage;
    }

    /**
     * Extract cookie param from cookie string as it was provided by "Set-Cookie" header.
     * @param param
     * @param cookieString
     * @return
     */
    private String getCookieParam(String param, String cookieString) {
        if (param.equals("Name")) {
            return cookieString.substring(0, cookieString.indexOf("="));
        }

        if (param.equals("Value")) {
            if (cookieString.contains(";")) {
                return cookieString.substring(cookieString.indexOf("=") + 1, cookieString.indexOf(";"));
            } else {
                return cookieString.substring(cookieString.indexOf("=") + 1);
            }
        }

        if (cookieString.contains(param + "=")) {
            int endParam = cookieString.indexOf(";", cookieString.indexOf(param + "="));
            if (endParam > 0) {
                return cookieString.substring(cookieString.indexOf(param + "=") + param.length() + 1, endParam);
            } else {
                return cookieString.substring(cookieString.indexOf(param + "=") + param.length() + 1);
            }
        }

        throw new CitrusRuntimeException(String.format("Unable to get cookie argument '%s' from cookie String: %s", param, cookieString));
    }

    /**
     * Message headers consist of standard HTTP message headers and custom headers.
     * This method assumes that all header entries that were not initially mapped
     * by header mapper implementations are custom headers.
     *
     * @param httpHeaders all message headers in their pre nature.
     * @param mappedHeaders the previously mapped header entries (all standard headers).
     * @return
     */
    private Map<String, String> getCustomHeaders(HttpHeaders httpHeaders, Map<String, Object> mappedHeaders) {
        Map<String, String> customHeaders = new HashMap<>();

        for (Map.Entry<String, List<String>> header : httpHeaders.entrySet()) {
            if (!mappedHeaders.containsKey(header.getKey())) {
                customHeaders.put(header.getKey(), StringUtils.collectionToCommaDelimitedString(header.getValue()));
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
                convertedHeaders.put(header.getKey(), StringUtils.collectionToCommaDelimitedString(value));
            } else if (header.getValue() instanceof MediaType) {
                convertedHeaders.put(header.getKey(), header.getValue().toString());
            } else {
                convertedHeaders.put(header.getKey(), header.getValue());
            }
        }

        return convertedHeaders;
    }

    @Override
    public void convertOutbound(HttpEntity externalMessage, Message internalMessage, HttpEndpointConfiguration endpointConfiguration, TestContext context) {
        throw new UnsupportedOperationException("HttpMessageConverter does not support predefined HttpEntity objects");
    }
}
