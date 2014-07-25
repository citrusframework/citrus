/*
 * Copyright 2006-2013 the original author or authors.
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

package com.consol.citrus.http.client;

import com.consol.citrus.endpoint.AbstractEndpoint;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.http.message.CitrusHttpMessageHeaders;
import com.consol.citrus.message.*;
import com.consol.citrus.messaging.*;
import com.consol.citrus.util.MessageUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.integration.Message;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.util.StringUtils;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.ResponseErrorHandler;

import java.io.IOException;
import java.util.*;

/**
 * Http client sends messages via Http protocol to some Http server instance, defined by a request endpoint url. Synchronous response
 * messages are cached in local memory and receive operations are able to fetch responses from this cache later on.
 *
 * @author Christoph Deppisch
 * @since 1.4
 */
public class HttpClient extends AbstractEndpoint implements Producer, ReplyConsumer {
    /** Logger */
    private static Logger log = LoggerFactory.getLogger(HttpClient.class);

    /** Store of reply messages */
    private Map<String, Message<?>> replyMessages = new HashMap<String, Message<?>>();

    /** Retry logger */
    private static final Logger RETRY_LOG = LoggerFactory.getLogger("com.consol.citrus.MessageRetryLogger");

    /**
     * Default constructor initializing endpoint configuration.
     */
    public HttpClient() {
        super(new HttpEndpointConfiguration());
    }

    /**
     * Default constructor using endpoint configuration.
     * @param endpointConfiguration
     */
    protected HttpClient(HttpEndpointConfiguration endpointConfiguration) {
        super(endpointConfiguration);
    }

    @Override
    public HttpEndpointConfiguration getEndpointConfiguration() {
        return (HttpEndpointConfiguration) super.getEndpointConfiguration();
    }

    @Override
    public void send(Message<?> message) {
        String endpointUri;
        if (getEndpointConfiguration().getEndpointUriResolver() != null) {
            endpointUri = getEndpointConfiguration().getEndpointUriResolver().resolveEndpointUri(message, getEndpointConfiguration().getRequestUrl());
        } else {
            endpointUri = getEndpointConfiguration().getRequestUrl();
        }

        log.info("Sending HTTP message to: '" + endpointUri + "'");

        if (log.isDebugEnabled()) {
            log.debug("Message to be sent:\n" + message.getPayload().toString());
        }

        HttpMethod method = getEndpointConfiguration().getRequestMethod();
        if (message.getHeaders().containsKey(CitrusHttpMessageHeaders.HTTP_REQUEST_METHOD)) {
            method = HttpMethod.valueOf((String)message.getHeaders().get(CitrusHttpMessageHeaders.HTTP_REQUEST_METHOD));
        }

        HttpEntity<?> requestEntity = generateRequest(message, method);

        getEndpointConfiguration().getRestTemplate().setErrorHandler(new InternalResponseErrorHandler(message));
        ResponseEntity<?> response = getEndpointConfiguration().getRestTemplate().exchange(endpointUri, method, requestEntity, String.class);

        log.info("HTTP message was successfully sent to endpoint: '" + endpointUri + "'");

        onReplyMessage(message, buildResponseMessage(response.getHeaders(),
                response.getBody() != null ? response.getBody() : "",
                response.getStatusCode()));
    }

    @Override
    public Message<?> receive() {
        return receive("", getEndpointConfiguration().getTimeout());
    }

    @Override
    public Message<?> receive(String selector) {
        return receive(selector, getEndpointConfiguration().getTimeout());
    }

    @Override
    public Message<?> receive(long timeout) {
        return receive("", timeout);
    }

    @Override
    public Message<?> receive(String selector, long timeout) {
        long timeLeft = timeout;
        Message<?> message = findReplyMessage(selector);

        while (message == null && timeLeft > 0) {
            timeLeft -= getEndpointConfiguration().getPollingInterval();

            if (RETRY_LOG.isDebugEnabled()) {
                RETRY_LOG.debug("Reply message did not arrive yet - retrying in " + (timeLeft > 0 ? getEndpointConfiguration().getPollingInterval() : getEndpointConfiguration().getPollingInterval() + timeLeft) + "ms");
            }

            try {
                Thread.sleep(timeLeft > 0 ? getEndpointConfiguration().getPollingInterval() : getEndpointConfiguration().getPollingInterval() + timeLeft);
            } catch (InterruptedException e) {
                RETRY_LOG.warn("Thread interrupted while waiting for retry", e);
            }

            message = findReplyMessage(selector);
        }

        return message;
    }

    /**
     * Handles error response messages constructing a proper response message
     * which will be propagated to the respective reply message handler for
     * further processing.
     */
    private class InternalResponseErrorHandler implements ResponseErrorHandler {

        /** Request message associated with this response error handler */
        private Message<?> requestMessage;

        /**
         * Default constructor provided with request message
         * associated with this error handler.
         */
        public InternalResponseErrorHandler(Message<?> requestMessage) {
            this.requestMessage = requestMessage;
        }

        /**
         * Check for error HTTP status code in response message.
         * Delegates to default Spring implementation.
         */
        public boolean hasError(ClientHttpResponse response) throws IOException {
            return new DefaultResponseErrorHandler().hasError(response);
        }

        /**
         * Handle error response message according to error strategy.
         */
        public void handleError(ClientHttpResponse response) throws IOException {
            if (getEndpointConfiguration().getErrorHandlingStrategy().equals(ErrorHandlingStrategy.PROPAGATE)) {
                onReplyMessage(buildResponseMessage(response.getHeaders(),
                        response.getBody() != null ? response.getBody() : "",
                        response.getStatusCode()), requestMessage);
            } else if (getEndpointConfiguration().getErrorHandlingStrategy().equals(ErrorHandlingStrategy.THROWS_EXCEPTION)) {
                new DefaultResponseErrorHandler().handleError(response);
            } else {
                throw new CitrusRuntimeException("Unsupported error strategy: " + getEndpointConfiguration().getErrorHandlingStrategy());
            }
        }

    }

    /**
     * Builds the actual integration message from HTTP response entity.
     * @param headers HTTP headers which will be transformed into Message headers
     * @param responseBody the HTTP body of the response
     * @param statusCode HTTP status code received
     * @return the response message
     */
    private Message<?> buildResponseMessage(HttpHeaders headers, Object responseBody, HttpStatus statusCode) {
        Map<String, ?> mappedHeaders = getEndpointConfiguration().getHeaderMapper().toHeaders(headers);

        Message<?> responseMessage = MessageBuilder.withPayload(responseBody)
                .copyHeaders(mappedHeaders)
                .copyHeaders(getCustomHeaders(headers, mappedHeaders))
                .setHeader(CitrusHttpMessageHeaders.HTTP_STATUS_CODE, statusCode)
                .setHeader(CitrusHttpMessageHeaders.HTTP_VERSION, "HTTP/1.1") //TODO check if we have access to version information
                .setHeader(CitrusHttpMessageHeaders.HTTP_REASON_PHRASE, statusCode.name())
                .build();

        return responseMessage;
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
    private Map<String, String> getCustomHeaders(HttpHeaders httpHeaders, Map<String, ?> mappedHeaders) {
        Map<String, String> customHeaders = new HashMap<String, String>();

        for (Map.Entry<String, List<String>> header : httpHeaders.entrySet()) {
            if (!mappedHeaders.containsKey(header.getKey())) {
                customHeaders.put(header.getKey(), StringUtils.collectionToCommaDelimitedString(header.getValue()));
            }
        }

        return customHeaders;
    }

    /**
     * Generate http request entity from Spring Integration message.
     * @param requestMessage
     * @param method
     * @return
     */
    private HttpEntity<?> generateRequest(Message<?> requestMessage, HttpMethod method) {
        HttpHeaders httpHeaders = new HttpHeaders();
        getEndpointConfiguration().getHeaderMapper().fromHeaders(requestMessage.getHeaders(), httpHeaders);

        Map<String, ?> messageHeaders = requestMessage.getHeaders();
        for (Map.Entry<String, ?> header : messageHeaders.entrySet()) {
            if (!header.getKey().startsWith(CitrusMessageHeaders.PREFIX) &&
                    !MessageUtils.isSpringInternalHeader(header.getKey()) &&
                    !httpHeaders.containsKey(header.getKey())) {
                httpHeaders.add(header.getKey(), header.getValue().toString());
            }
        }

        Object payload = requestMessage.getPayload();
        if (httpHeaders.getContentType() == null) {
            httpHeaders.setContentType(MediaType.parseMediaType(getEndpointConfiguration().getContentType().contains("charset") ?
                    getEndpointConfiguration().getContentType() : getEndpointConfiguration().getContentType() + ";charset=" + getEndpointConfiguration().getCharset()));
        }

        if (HttpMethod.POST.equals(method) || HttpMethod.PUT.equals(method)) {
            return new HttpEntity<Object>(payload, httpHeaders);
        }

        return new HttpEntity<Object>(httpHeaders);
    }

    /**
     * Saves reply message with correlation key to local store for later processing.
     * @param correlationKey
     * @param replyMessage the reply message.
     */
    public void onReplyMessage(String correlationKey, Message<?> replyMessage) {
        replyMessages.put(correlationKey, replyMessage);
    }

    /**
     * Saves reply message to local store for later processing. Constructs correlation key from initial request.
     * @param requestMessage
     * @param replyMessage
     */
    public void onReplyMessage(Message<?> requestMessage, Message<?> replyMessage) {
        if (getEndpointConfiguration().getCorrelator() != null) {
            onReplyMessage(getEndpointConfiguration().getCorrelator().getCorrelationKey(requestMessage), replyMessage);
        } else {
            onReplyMessage("", replyMessage);
        }
    }

    /**
     * Tries to find reply message for correlation key from local store.
     * @param correlationKey
     * @return
     */
    public Message<?> findReplyMessage(String correlationKey) {
        return replyMessages.remove(correlationKey);
    }

    /**
     * Creates a message producer for this endpoint for sending messages
     * to this endpoint.
     */
    @Override
    public Producer createProducer() {
        return this;
    }

    /**
     * Creates a message consumer for this endpoint. Consumer receives
     * messages on this endpoint.
     *
     * @return
     */
    @Override
    public SelectiveConsumer createConsumer() {
        return this;
    }

}
