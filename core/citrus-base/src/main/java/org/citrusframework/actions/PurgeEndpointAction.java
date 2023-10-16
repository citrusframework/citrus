/*
 * Copyright 2006-2015 the original author or authors.
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.citrusframework.AbstractTestActionBuilder;
import org.citrusframework.context.TestContext;
import org.citrusframework.endpoint.Endpoint;
import org.citrusframework.exceptions.ActionTimeoutException;
import org.citrusframework.message.Message;
import org.citrusframework.message.MessageSelectorBuilder;
import org.citrusframework.messaging.Consumer;
import org.citrusframework.messaging.SelectiveConsumer;
import org.citrusframework.spi.ReferenceResolver;
import org.citrusframework.spi.ReferenceResolverAware;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.citrusframework.util.StringUtils;

/**
 * Action purges all messages from a message endpoint. Action receives
 * a list of endpoint objects or a list of endpoint names that are resolved dynamically at runtime.
 *
 * @author Christoph Deppisch
 * @since 2.4
 */
public class PurgeEndpointAction extends AbstractTestAction {
    /** List of endpoint names to be purged */
    private final List<String> endpointNames;

    /** List of endpoints to be purged */
    private final List<Endpoint> endpoints;

    /** The parent bean reference resolver used for endpoint name resolving */
    private final ReferenceResolver referenceResolver;

    /** Build message selector with name value pairs */
    private final Map<String, Object> messageSelectorMap;

    /** Select messages via message selector string */
    private final String messageSelector;

    /** Time to wait until timeout in ms */
    private final long receiveTimeout;

    /** Wait some time between message consumption in ms */
    private final long sleepTime;

    /** Logger */
    private static final Logger logger = LoggerFactory.getLogger(PurgeEndpointAction.class);

    /**
     * Default constructor.
     */
    public PurgeEndpointAction(Builder builder) {
        super("purge-endpoint", builder);

        this.endpointNames = builder.endpointNames;
        this.endpoints = builder.endpoints;
        this.referenceResolver = builder.referenceResolver;
        this.messageSelector = builder.messageSelector;
        this.messageSelectorMap = builder.messageSelectorMap;
        this.receiveTimeout = builder.receiveTimeout;
        this.sleepTime = builder.sleepTime;
    }

    @Override
    public void doExecute(TestContext context) {
        if (logger.isDebugEnabled()) {
            logger.debug("Purging message endpoints ...");
        }

        for (Endpoint endpoint : endpoints) {
            purgeEndpoint(endpoint, context);
        }

        for (String endpointName : endpointNames) {
            purgeEndpoint(resolveEndpointName(endpointName), context);
        }

        logger.info("Purged message endpoints");
    }

    /**
     * Purges all messages from a message endpoint. Prerequisite is that endpoint operates on a destination
     * that queues messages.
     *
     * @param endpoint
     * @param context
     */
    private void purgeEndpoint(Endpoint endpoint, TestContext context) {
        if (logger.isDebugEnabled()) {
            logger.debug("Try to purge message endpoint " + endpoint.getName());
        }

        int messagesPurged = 0;
        Consumer messageConsumer = endpoint.createConsumer();
        Message message;
        do {
            try {
                String selector = MessageSelectorBuilder.build(messageSelector, messageSelectorMap, context);
                if (StringUtils.hasText(selector) && messageConsumer instanceof SelectiveConsumer) {
                    message = (receiveTimeout >= 0) ? ((SelectiveConsumer) messageConsumer).receive(selector, context, receiveTimeout) : ((SelectiveConsumer) messageConsumer).receive(selector, context);
                } else {
                    message = (receiveTimeout >= 0) ? messageConsumer.receive(context, receiveTimeout) : messageConsumer.receive(context);
                }
            } catch (ActionTimeoutException e) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Stop purging due to timeout - " + e.getMessage());
                }
                break;
            }

            if (message != null) {
                logger.debug("Removed message from endpoint " + endpoint.getName());
                messagesPurged++;

                try {
                    Thread.sleep(sleepTime);
                } catch (InterruptedException e) {
                    logger.warn("Interrupted during wait", e);
                }
            }
        } while (message != null);

        if (logger.isDebugEnabled()) {
            logger.debug("Purged " + messagesPurged + " messages from endpoint");
        }
    }

    /**
     * Resolve the endpoint by name.
     * @param endpointName the name to resolve
     * @return the Endpoint object
     */
    protected Endpoint resolveEndpointName(String endpointName) {
        return referenceResolver.resolve(endpointName, Endpoint.class);
    }

    /**
     * Gets the bean reference resolver for endpoint name resolving.
     * @return
     */
    public ReferenceResolver getReferenceResolver() {
        return referenceResolver;
    }

    /**
     * Gets the endpointNames.
     * @return the endpointNames the endpointNames to get.
     */
    public List<String> getEndpointNames() {
        return endpointNames;
    }

    /**
     * Gets the endpoints.
     * @return the endpoints the endpoints to get.
     */
    public List<Endpoint> getEndpoints() {
        return endpoints;
    }

    /**
     * Gets the messageSelector as map.
     * @return the messageSelectorMap
     */
    public Map<String, Object> getMessageSelectorMap() {
        return messageSelectorMap;
    }

    /**
     * Gets the messageSelector.
     * @return the messageSelector
     */
    public String getMessageSelector() {
        return messageSelector;
    }

    /**
     * Gets the receiveTimeout.
     * @return the receiveTimeout
     */
    public long getReceiveTimeout() {
        return receiveTimeout;
    }

    /**
     * Gets the sleepTime.
     * @return the sleepTime the sleepTime to get.
     */
    public long getSleepTime() {
        return sleepTime;
    }

    /**
     * Action builder.
     */
    public static final class Builder extends AbstractTestActionBuilder<PurgeEndpointAction, Builder> implements ReferenceResolverAware {

        private final List<String> endpointNames = new ArrayList<>();
        private final List<Endpoint> endpoints = new ArrayList<>();
        private ReferenceResolver referenceResolver;
        private Map<String, Object> messageSelectorMap = new HashMap<>();
        private String messageSelector;
        private long receiveTimeout = 100;
        private long sleepTime = 350;

        /**
         * Fluent API action building entry method used in Java DSL.
         * @return
         */
        public static Builder purge() {
            return new Builder();
        }

        /**
         * Fluent API action building entry method used in Java DSL.
         * @return
         */
        public static Builder purgeEndpoints() {
            return new Builder();
        }

        /**
         * Sets the messageSelector.
         * @param messageSelector the messageSelector to set
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

        /**
         * Adds list of endpoint names to purge in this action.
         * @param endpointNames the endpointNames to set
         */
        public Builder endpointNames(List<String> endpointNames) {
            this.endpointNames.addAll(endpointNames);
            return this;
        }

        /**
         * Adds several endpoint names to the list of endpoints to purge in this action.
         * @param endpointNames
         * @return
         */
        public Builder endpointNames(String... endpointNames) {
            return endpointNames(Arrays.asList(endpointNames));
        }

        /**
         * Adds a endpoint name to the list of endpoints to purge in this action.
         * @param name
         * @return
         */
        public Builder endpoint(String name) {
            this.endpointNames.add(name);
            return this;
        }

        /**
         * Adds list of endpoints to purge in this action.
         * @param endpoints the endpoints to set
         */
        public Builder endpoints(List<Endpoint> endpoints) {
            this.endpoints.addAll(endpoints);
            return this;
        }

        /**
         * Sets several endpoints to purge in this action.
         * @param endpoints
         * @return
         */
        public Builder endpoints(Endpoint... endpoints) {
            return endpoints(Arrays.asList(endpoints));
        }

        /**
         * Adds a endpoint to the list of endpoints to purge in this action.
         * @param endpoint
         * @return
         */
        public Builder endpoint(Endpoint endpoint) {
            this.endpoints.add(endpoint);
            return this;
        }

        /**
         * Receive timeout for reading message from a destination.
         * @param receiveTimeout the receiveTimeout to set
         */
        public Builder timeout(long receiveTimeout) {
            this.receiveTimeout = receiveTimeout;
            return this;
        }

        /**
         * Sets the sleepTime.
         * @param millis the sleepTime to set
         */
        public Builder sleep(long millis) {
            this.sleepTime = millis;
            return this;
        }

        /**
         * Sets the bean reference resolver for using endpoint names.
         * @param referenceResolver
         */
        public Builder withReferenceResolver(ReferenceResolver referenceResolver) {
            this.referenceResolver = referenceResolver;
            return this;
        }

        public Builder referenceResolver(ReferenceResolver referenceResolver) {
            this.referenceResolver = referenceResolver;
            return this;
        }

        @Override
        public void setReferenceResolver(ReferenceResolver referenceResolver) {
            this.referenceResolver = referenceResolver;
        }

        @Override
        public PurgeEndpointAction build() {
            return new PurgeEndpointAction(this);
        }
    }
}
