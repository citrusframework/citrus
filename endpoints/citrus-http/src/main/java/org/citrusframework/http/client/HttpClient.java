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

package org.citrusframework.http.client;

import java.net.URI;
import java.util.Optional;

import org.citrusframework.context.TestContext;
import org.citrusframework.endpoint.AbstractEndpoint;
import org.citrusframework.exceptions.MessageTimeoutException;
import org.citrusframework.http.interceptor.LoggingClientInterceptor;
import org.citrusframework.http.message.HttpMessage;
import org.citrusframework.message.Message;
import org.citrusframework.message.MessageHeaders;
import org.citrusframework.message.correlation.CorrelationManager;
import org.citrusframework.message.correlation.PollingCorrelationManager;
import org.citrusframework.messaging.Producer;
import org.citrusframework.messaging.ReplyConsumer;
import org.citrusframework.messaging.SelectiveConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.InvalidMediaTypeException;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Http client sends messages via Http protocol to some Http server instance, defined by a request endpoint url. Synchronous response
 * messages are cached in local memory and receive operations are able to fetch responses from this cache later on.
 *
 * @author Christoph Deppisch
 * @since 1.4
 */
public class HttpClient extends AbstractEndpoint implements Producer, ReplyConsumer {
    /** Logger */
    private static final Logger logger = LoggerFactory.getLogger(HttpClient.class);

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
        getEndpointConfiguration().getClientInterceptors()
                .stream()
                .filter(LoggingClientInterceptor.class::isInstance)
                .map(LoggingClientInterceptor.class::cast)
                .filter(interceptor -> !interceptor.hasMessageListeners())
                .forEach(interceptor -> interceptor.setMessageListener(context.getMessageListeners()));

        HttpMessage httpMessage;
        if (message instanceof HttpMessage) {
            httpMessage = (HttpMessage) message;
        } else {
            httpMessage = new HttpMessage(message);
        }

        String correlationKeyName = getEndpointConfiguration().getCorrelator().getCorrelationKeyName(getName());
        String correlationKey = getEndpointConfiguration().getCorrelator().getCorrelationKey(httpMessage);
        correlationManager.saveCorrelationKey(correlationKeyName, correlationKey, context);

        final String endpointUri = getEndpointUri(httpMessage);
        context.setVariable(MessageHeaders.MESSAGE_REPLY_TO + "_" + correlationKeyName, endpointUri);

        logger.info("Sending HTTP message to: '" + endpointUri + "'");
        if (logger.isDebugEnabled()) {
            logger.debug("Message to send:\n" + httpMessage.getPayload(String.class));
        }

        RequestMethod method = getEndpointConfiguration().getRequestMethod();
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
                                        logger.warn(String.format("Failed to parse accept media type '%s' - using default media type '%s'",
                                                mediaType[0], MediaType.ALL_VALUE), e);
                                        return MediaType.ALL;
                                    }
                                })
                                .orElse(MediaType.ALL);

            if (getEndpointConfiguration().getBinaryMediaTypes().stream().anyMatch(mediaType -> mediaType.includes(accept))) {
                response = getEndpointConfiguration().getRestTemplate().exchange(URI.create(endpointUri), HttpMethod.valueOf(method.toString()), requestEntity, byte[].class);
            } else {
                response = getEndpointConfiguration().getRestTemplate().exchange(URI.create(endpointUri), HttpMethod.valueOf(method.toString()), requestEntity, String.class);
            }

            logger.info("HTTP message was sent to endpoint: '" + endpointUri + "'");
            correlationManager.store(correlationKey, getEndpointConfiguration().getMessageConverter().convertInbound(response, getEndpointConfiguration(), context));
        } catch (HttpErrorPropagatingException e) {
            logger.info("Caught HTTP rest client exception: " + e.getMessage());
            logger.info("Propagating HTTP rest client exception according to error handling strategy");
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

        String endpointUri;
        if (context.getVariables().containsKey(MessageHeaders.MESSAGE_REPLY_TO + "_" + selector)) {
            endpointUri = context.getVariable(MessageHeaders.MESSAGE_REPLY_TO + "_" + selector);
        } else {
            endpointUri = getName();
        }

        if (message == null) {
            throw new MessageTimeoutException(timeout, endpointUri);
        }

        return message;
    }

    private String getEndpointUri(HttpMessage httpMessage) {
        if (getEndpointConfiguration().getEndpointUriResolver() != null) {
            return getEndpointConfiguration().getEndpointUriResolver().resolveEndpointUri(httpMessage, getEndpointConfiguration().getRequestUrl());
        } else {
            return getEndpointConfiguration().getRequestUrl();
        }
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
