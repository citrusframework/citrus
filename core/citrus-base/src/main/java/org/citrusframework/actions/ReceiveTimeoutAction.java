/*
 * Copyright 2006-2010 the original author or authors.
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

package org.citrusframework.actions;

import java.util.HashMap;
import java.util.Map;

import org.citrusframework.AbstractTestActionBuilder;
import org.citrusframework.context.TestContext;
import org.citrusframework.endpoint.Endpoint;
import org.citrusframework.exceptions.ActionTimeoutException;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.message.Message;
import org.citrusframework.message.MessageSelectorBuilder;
import org.citrusframework.messaging.Consumer;
import org.citrusframework.messaging.SelectiveConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.citrusframework.util.StringUtils;

/**
 * Action expecting a timeout on a message destination, this means that no message
 * should arrive on the destination.
 *
 * @author Christoph Deppisch
 * @since 2006
 */
public class ReceiveTimeoutAction extends AbstractTestAction {
    /** Time to wait until timeout */
    private final long timeout;

    /** Message endpoint */
    private final Endpoint endpoint;

    /** Message endpoint uri */
    private final String endpointUri;

    /** Build message selector with name value pairs */
    private final Map<String, Object> messageSelectorMap;

    /** Message selector string */
    private final String messageSelector;

    /** Logger */
    private static final Logger logger = LoggerFactory.getLogger(ReceiveTimeoutAction.class);

    /**
     * Default constructor.
     */
    public ReceiveTimeoutAction(Builder builder) {
        super("receive-timeout", builder);

        this.endpoint = builder.endpoint;
        this.endpointUri = builder.endpointUri;
        this.timeout = builder.timeout;
        this.messageSelector = builder.messageSelector;
        this.messageSelectorMap = builder.messageSelectorMap;
    }

    @Override
    public void doExecute(TestContext context) {
        try {
            Message receivedMessage;
            Consumer consumer = getOrCreateEndpoint(context).createConsumer();

            String selector = MessageSelectorBuilder.build(messageSelector, messageSelectorMap, context);

            if (StringUtils.hasText(selector) && consumer instanceof SelectiveConsumer) {
                receivedMessage = ((SelectiveConsumer)consumer).receive(selector, context, timeout);
            } else {
                receivedMessage = consumer.receive(context, timeout);
            }

            if (receivedMessage != null) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Received message:\n" + receivedMessage.print(context));
                }

                throw new CitrusRuntimeException("Message timeout validation failed! " +
                		"Received message while waiting for timeout on destination");
            }
        } catch (ActionTimeoutException e) {
            logger.info("No messages received on destination. Message timeout validation OK!");
            logger.info(e.getMessage());
        }
    }

    /**
     * Creates or gets the endpoint instance.
     * @param context
     * @return
     */
    public Endpoint getOrCreateEndpoint(TestContext context) {
        if (endpoint != null) {
            return endpoint;
        } else if (StringUtils.hasText(endpointUri)) {
            return context.getEndpointFactory().create(endpointUri, context);
        } else {
            throw new CitrusRuntimeException("Neither endpoint nor endpoint uri is set properly!");
        }
    }

    /**
     * Gets the messageSelector.
     *
     * @return
     */
    public String getMessageSelector() {
        return messageSelector;
    }

    /**
     * Gets the messageSelectorMap.
     *
     * @return
     */
    public Map<String, Object> getMessageSelectorMap() {
        return messageSelectorMap;
    }

    /**
     * Get the message endpoint.
     * @return the message endpoint
     */
    public Endpoint getEndpoint() {
        return endpoint;
    }

    /**
     * Gets the timeout.
     * @return the timeout
     */
    public long getTimeout() {
        return timeout;
    }

    /**
     * Gets the endpoint uri.
     * @return
     */
    public String getEndpointUri() {
        return endpointUri;
    }

    /**
     * Action builder.
     */
    public static final class Builder extends AbstractTestActionBuilder<ReceiveTimeoutAction, Builder> {

        private long timeout = 1000L;
        private Endpoint endpoint;
        private String endpointUri;
        private Map<String, Object> messageSelectorMap = new HashMap<>();
        private String messageSelector;

        public static Builder expectTimeout() {
            return new Builder();
        }

        /**
         * Fluent API action building entry method used in Java DSL.
         * @param endpointUri
         * @return
         */
        public static Builder expectTimeout(String endpointUri) {
            return receiveTimeout(endpointUri);
        }

        /**
         * Fluent API action building entry method used in Java DSL.
         * @param endpoint
         * @return
         */
        public static Builder expectTimeout(Endpoint endpoint) {
            return receiveTimeout(endpoint);
        }

        /**
         * Fluent API action building entry method used in Java DSL.
         * @param endpointUri
         * @return
         */
        public static Builder receiveTimeout(String endpointUri) {
            Builder builder = new Builder();
            builder.endpoint(endpointUri);
            return builder;
        }

        /**
         * Fluent API action building entry method used in Java DSL.
         * @param endpoint
         * @return
         */
        public static Builder receiveTimeout(Endpoint endpoint) {
            Builder builder = new Builder();
            builder.endpoint(endpoint);
            return builder;
        }

        /**
         * Sets the message endpoint to receive a timeout with.
         * @param messageEndpoint
         * @return
         */
        public Builder endpoint(Endpoint messageEndpoint) {
            this.endpoint = messageEndpoint;
            return this;
        }

        /**
         * Sets the message endpoint uri to receive a timeout with.
         * @param messageEndpointUri
         * @return
         */
        public Builder endpoint(String messageEndpointUri) {
            this.endpointUri = messageEndpointUri;
            return this;
        }

        /**
         * Sets time to wait for messages on destination.
         * @param timeout
         */
        public Builder timeout(long timeout) {
            this.timeout = timeout;
            return this;
        }

        /**
         * Adds message selector string for selective consumer.
         * @param messageSelector
         */
        public Builder selector(String messageSelector) {
            this.messageSelector = messageSelector;
            return this;
        }

        /**
         * Sets the messageSelector.
         * @param messageSelector the messageSelector to set
         */
        public Builder selector(Map<String, Object> messageSelector) {
            this.messageSelectorMap = messageSelector;
            return this;
        }

        @Override
        public ReceiveTimeoutAction build() {
            return new ReceiveTimeoutAction(this);
        }
    }
}
