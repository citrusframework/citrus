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

import com.consol.citrus.context.TestContext;
import com.consol.citrus.endpoint.AbstractEndpoint;
import com.consol.citrus.exceptions.ActionTimeoutException;
import com.consol.citrus.http.interceptor.LoggingClientInterceptor;
import com.consol.citrus.http.message.HttpMessage;
import com.consol.citrus.message.Message;
import com.consol.citrus.message.correlation.CorrelationManager;
import com.consol.citrus.message.correlation.PollingCorrelationManager;
import com.consol.citrus.messaging.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.util.CollectionUtils;

import java.net.URI;
import java.util.Collections;
import java.util.Optional;

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
    private CorrelationManager<Message> correlationManager;

    /**
     * Default constructor initializing endpoint configuration.
     */
    public HttpClient() {
        this(new HttpEndpointConfiguration());
    }

    /**
     * Default constructor using endpoint configuration.
     * @param endpointConfiguration
     */
    public HttpClient(HttpEndpointConfiguration endpointConfiguration) {
        super(endpointConfiguration);

        this.correlationManager = new PollingCorrelationManager<>(endpointConfiguration, "Reply message did not arrive yet");
    }

    @Override
    public HttpEndpointConfiguration getEndpointConfiguration() {
        return (HttpEndpointConfiguration) super.getEndpointConfiguration();
    }

    @Override
    public void send(Message message, TestContext context) {
        if (CollectionUtils.isEmpty(getEndpointConfiguration().getClientInterceptors())) {
            LoggingClientInterceptor loggingClientInterceptor = new LoggingClientInterceptor();
            loggingClientInterceptor.setMessageListener(context.getMessageListeners());

            getEndpointConfiguration().setClientInterceptors(Collections.singletonList(loggingClientInterceptor));
        }

        HttpMessage httpMessage;
        if (message instanceof HttpMessage) {
            httpMessage = (HttpMessage) message;
        } else {
            httpMessage = new HttpMessage(message);
        }

        String correlationKeyName = getEndpointConfiguration().getCorrelator().getCorrelationKeyName(getName());
        String correlationKey = getEndpointConfiguration().getCorrelator().getCorrelationKey(httpMessage);
        correlationManager.saveCorrelationKey(correlationKeyName, correlationKey, context);

        String endpointUri;
        if (getEndpointConfiguration().getEndpointUriResolver() != null) {
            endpointUri = getEndpointConfiguration().getEndpointUriResolver().resolveEndpointUri(httpMessage, getEndpointConfiguration().getRequestUrl());
        } else {
            endpointUri = getEndpointConfiguration().getRequestUrl();
        }

        if (log.isDebugEnabled()) {
            log.debug("Sending HTTP message to: '" + endpointUri + "'");
            log.debug("Message to send:\n" + httpMessage.getPayload(String.class));
        }

        HttpMethod method = getEndpointConfiguration().getRequestMethod();
        if (httpMessage.getRequestMethod() != null) {
            method = httpMessage.getRequestMethod();
        }

        HttpEntity<?> requestEntity = getEndpointConfiguration().getMessageConverter().convertOutbound(httpMessage, getEndpointConfiguration(), context);

        try {
            ResponseEntity<?> response;
            MediaType accept = Optional.ofNullable(httpMessage.getAccept())
                                .map(mediaType -> mediaType.split(","))
                                .map(mediaType -> {
                                    try {
                                        return MediaType.valueOf(mediaType[0]);
                                    } catch (InvalidMediaTypeException e) {
                                        log.warn(String.format("Failed to parse accept media type '%s' - using default media type '%s'",
                                                mediaType[0], MediaType.ALL_VALUE), e);
                                        return MediaType.ALL;
                                    }
                                })
                                .orElse(MediaType.ALL);

            if (getEndpointConfiguration().getBinaryMediaTypes().stream().anyMatch(mediaType -> mediaType.includes(accept))) {
                response = getEndpointConfiguration().getRestTemplate().exchange(URI.create(endpointUri), method, requestEntity, byte[].class);
            } else {
                response = getEndpointConfiguration().getRestTemplate().exchange(URI.create(endpointUri), method, requestEntity, String.class);
            }

            log.info("HTTP message was sent to endpoint: '" + endpointUri + "'");
            correlationManager.store(correlationKey, getEndpointConfiguration().getMessageConverter().convertInbound(response, getEndpointConfiguration(), context));
        } catch (HttpErrorPropagatingException e) {
            log.info("Caught HTTP rest client exception: " + e.getMessage());
            log.info("Propagating HTTP rest client exception according to error handling strategy");
            Message responseMessage = getEndpointConfiguration().getMessageConverter().convertInbound(
                    new ResponseEntity<>(e.getResponseBodyAsString(), e.getResponseHeaders(), e.getStatusCode()), getEndpointConfiguration(), context);
            correlationManager.store(correlationKey, responseMessage);
        }
    }

    @Override
    public Message receive(TestContext context) {
        return receive(correlationManager.getCorrelationKey(
                getEndpointConfiguration().getCorrelator().getCorrelationKeyName(getName()), context), context);
    }

    @Override
    public Message receive(String selector, TestContext context) {
        return receive(selector, context, getEndpointConfiguration().getTimeout());
    }

    @Override
    public Message receive(TestContext context, long timeout) {
        return receive(correlationManager.getCorrelationKey(
                getEndpointConfiguration().getCorrelator().getCorrelationKeyName(getName()), context), context, timeout);
    }

    @Override
    public Message receive(String selector, TestContext context, long timeout) {
        Message message = correlationManager.find(selector, timeout);

        if (message == null) {
            throw new ActionTimeoutException("Action timeout while receiving synchronous reply message from http server");
        }

        return message;
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

    /**
     * Sets the correlation manager.
     * @param correlationManager
     */
    public void setCorrelationManager(CorrelationManager<Message> correlationManager) {
        this.correlationManager = correlationManager;
    }

}
