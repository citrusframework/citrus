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

package com.consol.citrus.actions;

import com.consol.citrus.context.TestContext;
import com.consol.citrus.endpoint.Endpoint;
import com.consol.citrus.exceptions.ActionTimeoutException;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.message.Message;
import com.consol.citrus.message.MessageSelectorBuilder;
import com.consol.citrus.messaging.Consumer;
import com.consol.citrus.messaging.SelectiveConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.*;

/**
 * Action purges all messages from a message endpoint. Action receives
 * a list of endpoint objects or a list of endpoint names that are resolved dynamically at runtime.
 * 
 * @author Christoph Deppisch
 * @since 2.4
 */
public class PurgeEndpointAction extends AbstractTestAction implements BeanFactoryAware {
    /** List of endpoint names to be purged */
    private List<String> endpointNames = new ArrayList<>();

    /** List of endpoints to be purged */
    private List<Endpoint> endpoints = new ArrayList<>();

    /** The parent bean factory used for endpoint name resolving */
    private BeanFactory beanFactory;

    /** Build message selector with name value pairs */
    private Map<String, Object> messageSelector = new HashMap<>();

    /** Select messages via message selector string */
    private String messageSelectorString;

    /** Time to wait until timeout in ms */
    private long receiveTimeout = 100;

    /** Wait some time between message consumption in ms */
    private long sleepTime = 350;

    /** Logger */
    private static Logger log = LoggerFactory.getLogger(PurgeEndpointAction.class);

    /**
     * Default constructor.
     */
    public PurgeEndpointAction() {
        setName("purge-endpoint");
    }

    @Override
    public void doExecute(TestContext context) {
        if (log.isDebugEnabled()) {
            log.debug("Purging message endpoints ...");
        }

        for (Endpoint endpoint : endpoints) {
            purgeEndpoint(endpoint, context);
        }

        for (String endpointName : endpointNames) {
            purgeEndpoint(resolveEndpointName(endpointName), context);
        }

        log.info("Purged message endpoints");
    }

    /**
     * Purges all messages from a message endpoint. Prerequisite is that endpoint operates on a destination
     * that queues messages.
     * 
     * @param endpoint
     * @param context
     */
    private void purgeEndpoint(Endpoint endpoint, TestContext context) {
        if (log.isDebugEnabled()) {
            log.debug("Try to purge message endpoint " + endpoint.getName());
        }

        int messagesPurged = 0;
        Consumer messageConsumer = endpoint.createConsumer();
        Message message;
        do {
            String selector = buildMessageSelector(context);
            try {
                if (StringUtils.hasText(selector) && messageConsumer instanceof SelectiveConsumer) {
                    message = (receiveTimeout >= 0) ? ((SelectiveConsumer) messageConsumer).receive(selector, context, receiveTimeout) : ((SelectiveConsumer) messageConsumer).receive(selector, context);
                } else {
                    message = (receiveTimeout >= 0) ? messageConsumer.receive(context, receiveTimeout) : messageConsumer.receive(context);
                }
            } catch (ActionTimeoutException e) {
                if (log.isDebugEnabled()) {
                    log.debug("Stop purging due to timeout - " + e.getMessage());
                }
                break;
            }

            if (message != null) {
                log.debug("Removed message from endpoint " + endpoint.getName());
                messagesPurged++;

                try {
                    Thread.sleep(sleepTime);
                } catch (InterruptedException e) {
                    log.warn("Interrupted during wait", e);
                }
            }
        } while (message != null);

        if (log.isDebugEnabled()) {
            log.debug("Purged " + messagesPurged + " messages from endpoint");
        }
    }

    /**
     * Build message selector string from either message key value pairs or selector string.
     * @return
     */
    private String buildMessageSelector(TestContext context) {
        //build message selector string if present
        if (StringUtils.hasText(messageSelectorString)) {
            return context.replaceDynamicContentInString(messageSelectorString);
        } else if (!CollectionUtils.isEmpty(messageSelector)) {
            return MessageSelectorBuilder.fromKeyValueMap(
                    context.resolveDynamicValuesInMap(messageSelector)).build();
        }

        return "";
    }

    /**
     * Resolve the endpoint by name.
     * @param endpointName the name to resolve
     * @return the Endpoint object
     */
    protected Endpoint resolveEndpointName(String endpointName) {
        try {
            return beanFactory.getBean(endpointName, Endpoint.class);
        } catch (BeansException e) {
            throw new CitrusRuntimeException(String.format("Unable to resolve endpoint for name '%s'", endpointName), e);
        }
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }

    /**
     * Gets the bean factory for endpoint name resolving.
     * @return
     */
    public BeanFactory getBeanFactory() {
        return beanFactory;
    }

    /**
     * Gets the endpointNames.
     * @return the endpointNames the endpointNames to get.
     */
    public List<String> getEndpointNames() {
        return endpointNames;
    }

    /**
     * Sets the endpointNames.
     * @param endpointNames the endpointNames to set
     */
    public PurgeEndpointAction setEndpointNames(List<String> endpointNames) {
        this.endpointNames = endpointNames;
        return this;
    }

    /**
     * Gets the endpoints.
     * @return the endpoints the endpoints to get.
     */
    public List<Endpoint> getEndpoints() {
        return endpoints;
    }

    /**
     * Sets the endpoints.
     * @param endpoints the endpoints to set
     */
    public PurgeEndpointAction setEndpoints(List<Endpoint> endpoints) {
        this.endpoints = endpoints;
        return this;
    }

    /**
     * Setter for messageSelector.
     * @param messageSelector
     */
    public PurgeEndpointAction setMessageSelector(Map<String, Object> messageSelector) {
        this.messageSelector = messageSelector;
        return this;
    }

    /**
     * Set message selector string.
     * @param messageSelectorString
     */
    public PurgeEndpointAction setMessageSelectorString(String messageSelectorString) {
        this.messageSelectorString = messageSelectorString;
        return this;
    }

    /**
     * Gets the messageSelector.
     * @return the messageSelector
     */
    public Map<String, Object> getMessageSelector() {
        return messageSelector;
    }

    /**
     * Gets the messageSelectorString.
     * @return the messageSelectorString
     */
    public String getMessageSelectorString() {
        return messageSelectorString;
    }

    /**
     * Gets the receiveTimeout.
     * @return the receiveTimeout
     */
    public long getReceiveTimeout() {
        return receiveTimeout;
    }

    /**
     * Set the receive timeout.
     * @param receiveTimeout the receiveTimeout to set
     */
    public PurgeEndpointAction setReceiveTimeout(long receiveTimeout) {
        this.receiveTimeout = receiveTimeout;
        return this;
    }

    /**
     * Sets the sleepTime.
     * @param sleepTime the sleepTime to set
     */
    public void setSleepTime(long sleepTime) {
        this.sleepTime = sleepTime;
    }

    /**
     * Gets the sleepTime.
     * @return the sleepTime the sleepTime to get.
     */
    public long getSleepTime() {
        return sleepTime;
    }
}
