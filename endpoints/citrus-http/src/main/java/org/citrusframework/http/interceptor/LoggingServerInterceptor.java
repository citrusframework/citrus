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

package org.citrusframework.http.interceptor;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.citrusframework.context.TestContextFactory;
import org.citrusframework.http.message.HttpMessage;
import org.citrusframework.http.message.HttpMessageHeaders;
import org.citrusframework.message.Message;
import org.citrusframework.message.RawMessage;
import org.citrusframework.report.MessageListeners;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Transport neutral logging interceptor for the Citrus HTTP server. Logs the received request and
 * the sent reply in raw HTTP form. When the server runs in a Citrus context with message listeners
 * the messages are reported to those listeners instead of being written to the debug log.
 *
 * @since 5.1.0
 */
public class LoggingServerInterceptor implements HttpServerInterceptor {

    /** Logger */
    private final Logger logger = LoggerFactory.getLogger(getClass());

    /** New line used to separate the single lines of the logged message */
    private static final String NEWLINE = System.lineSeparator();

    /** Prefix of all internal Citrus headers, those are not logged */
    private static final String CITRUS_HEADER_PREFIX = "citrus_";

    /** Test context factory used to create a context for the reported messages */
    private final TestContextFactory contextFactory = TestContextFactory.newInstance();

    /** Optional message listeners, the messages are reported to */
    private MessageListeners messageListener;

    @Override
    public Message intercept(HttpMessage request, Chain chain) {
        handleRequest(request);

        Message response = chain.proceed(request);

        handleResponse(response, getVersion(request));

        return response;
    }

    /**
     * Reports the received request to the message listeners or logs it.
     *
     * @param request the received request.
     */
    public void handleRequest(HttpMessage request) {
        String content = getRequestContent(request);

        if (hasMessageListeners()) {
            logger.debug("Received Http request");
            messageListener.onInboundMessage(new RawMessage(content), contextFactory.getObject());
        } else {
            logger.debug("Received Http request:{}{}", NEWLINE, content);
        }
    }

    /**
     * Reports the sent reply to the message listeners or logs it.
     *
     * @param response the reply, may be {@code null} when the request was not answered.
     * @param requestVersion the protocol version of the request, used when the reply does not carry one.
     */
    public void handleResponse(Message response, String requestVersion) {
        String content = getResponseContent(response, requestVersion);

        if (hasMessageListeners()) {
            logger.debug("Sending Http response");
            messageListener.onOutboundMessage(new RawMessage(content), contextFactory.getObject());
        } else {
            logger.debug("Sending Http response:{}{}", NEWLINE, content);
        }
    }

    /**
     * Sets the message listeners the messages are reported to.
     *
     * @param messageListener the message listeners.
     */
    public void setMessageListener(MessageListeners messageListener) {
        this.messageListener = messageListener;
    }

    /**
     * Checks if the interceptor reports the messages to message listeners.
     *
     * @return {@code true} when message listeners are set and not empty.
     */
    public boolean hasMessageListeners() {
        return messageListener != null && !messageListener.isEmpty();
    }

    /**
     * Builds the raw HTTP representation of the received request.
     *
     * @param request the received request.
     * @return the request content.
     */
    protected String getRequestContent(HttpMessage request) {
        StringBuilder builder = new StringBuilder();

        builder.append(getVersion(request)).append(" ")
                .append(request.getRequestMethod()).append(" ")
                .append(getRequestUri(request)).append(NEWLINE);

        appendHeaders(builder, request);
        builder.append(NEWLINE);
        builder.append(getBodyContent(request));

        return builder.toString();
    }

    /**
     * Builds the raw HTTP representation of the sent reply.
     *
     * @param response the reply, may be {@code null} when the request was not answered.
     * @param requestVersion the protocol version of the request, used when the reply does not carry one.
     * @return the response content.
     */
    protected String getResponseContent(Message response, String requestVersion) {
        StringBuilder builder = new StringBuilder();

        builder.append(getResponseStartLine(response, requestVersion)).append(NEWLINE);

        if (response != null) {
            appendHeaders(builder, response);
        }
        builder.append(NEWLINE);

        if (response != null) {
            builder.append(getBodyContent(response));
        }

        return builder.toString();
    }

    private String getRequestUri(HttpMessage request) {
        String uri = request.getUri() != null ? request.getUri() : request.getPath();

        String queryParams = request.getQueryParamString();
        if (queryParams != null && !queryParams.isEmpty()) {
            uri = uri + "?" + queryParams.replace(",", "&");
        }

        return uri;
    }

    private String getVersion(HttpMessage request) {
        String version = request.getVersion();
        return version != null ? version : "HTTP/1.1";
    }

    private String getResponseStartLine(Message response, String requestVersion) {
        if (response == null) {
            return "<no reply — default status>";
        }

        Object version = response.getHeader(HttpMessageHeaders.HTTP_VERSION);
        Object status = response.getHeader(HttpMessageHeaders.HTTP_STATUS_CODE);

        if (response instanceof HttpMessage httpMessage && httpMessage.getStatusCode() != null) {
            status = httpMessage.getStatusCode().value();
        }

        if (status == null) {
            return "<no reply — default status>";
        }

        return (version != null ? version.toString() : requestVersion) + " " + status;
    }

    private void appendHeaders(StringBuilder builder, Message message) {
        for (Map.Entry<String, Object> header : message.getHeaders().entrySet()) {
            if (header.getKey() == null || header.getKey().startsWith(CITRUS_HEADER_PREFIX)) {
                continue;
            }

            builder.append(header.getKey()).append(":").append(header.getValue()).append(NEWLINE);
        }
    }

    private String getBodyContent(Message message) {
        Object payload = message.getPayload();
        byte[] body = payload instanceof byte[] bytes ? bytes
                : payload != null ? String.valueOf(payload).getBytes(StandardCharsets.UTF_8) : new byte[0];

        return LoggingInterceptorUtils.getBodyContent(body, getContentType(message), getContentEncoding(message));
    }

    private String getContentType(Message message) {
        Object contentType = message.getHeader("Content-Type");
        if (contentType == null) {
            contentType = message.getHeader("content-type");
        }
        return contentType != null ? contentType.toString() : null;
    }

    private String getContentEncoding(Message message) {
        Object contentEncoding = message.getHeader("Content-Encoding");
        if (contentEncoding == null) {
            contentEncoding = message.getHeader("content-encoding");
        }
        return contentEncoding != null ? contentEncoding.toString() : null;
    }
}
