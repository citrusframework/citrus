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

package com.consol.citrus.actions;

import com.consol.citrus.context.TestContext;
import com.consol.citrus.endpoint.Endpoint;
import com.consol.citrus.exceptions.ActionTimeoutException;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.message.Message;
import com.consol.citrus.messaging.Consumer;
import com.consol.citrus.messaging.SelectiveConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

/**
 * Action expecting a timeout on a message destination, this means that no message 
 * should arrive on the destination.
 * 
 * @author Christoph Deppisch
 * @since 2006
 */
public class ReceiveTimeoutAction extends AbstractTestAction {
    /** Time to wait until timeout */
    private long timeout = 1000L;

    /** Message endpoint */
    private Endpoint endpoint;

    /** Message endpoint uri */
    private String endpointUri;

    /** Message selector string */
    private String messageSelector;

    /** Logger */
    private static Logger log = LoggerFactory.getLogger(ReceiveTimeoutAction.class);

    /**
     * Default constructor.
     */
    public ReceiveTimeoutAction() {
        setName("receive-timeout");
    }

    @Override
    public void doExecute(TestContext context) {
        try {
            Message receivedMessage;
            Consumer consumer = getOrCreateEndpoint(context).createConsumer();

            if (StringUtils.hasText(messageSelector) && consumer instanceof SelectiveConsumer) {
                receivedMessage = ((SelectiveConsumer)consumer).receive(messageSelector, context, timeout);
            } else {
                receivedMessage = consumer.receive(context, timeout);
            }

            if (receivedMessage != null) {
                if (log.isDebugEnabled()) {
                    log.debug("Received message: " + receivedMessage.getPayload());
                }
                
                throw new CitrusRuntimeException("Message timeout validation failed! " +
                		"Received message while waiting for timeout on destination");
            }
        } catch (ActionTimeoutException e) {
            log.info("No messages received on destination. Message timeout validation OK!");
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
            endpoint = context.getEndpointFactory().create(endpointUri, context);
            return endpoint;
        } else {
            throw new CitrusRuntimeException("Neither endpoint nor endpoint uri is set properly!");
        }
    }

    /**
     * Setter for receive timeout.
     * @param timeout
     */
    public ReceiveTimeoutAction setTimeout(long timeout) {
        this.timeout = timeout;
        return this;
    }

    /**
     * Set message selector string.
     * @param messageSelector
     */
    public ReceiveTimeoutAction setMessageSelector(String messageSelector) {
        this.messageSelector = messageSelector;
        return this;
    }

    /**
     * Set message endpoint instance.
     * @param endpoint the message endpoint
     */
    public ReceiveTimeoutAction setEndpoint(Endpoint endpoint) {
        this.endpoint = endpoint;
        return this;
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
     * Gets the messageSelector.
     * @return the messageSelector
     */
    public String getMessageSelector() {
        return messageSelector;
    }

    /**
     * Gets the endpoint uri.
     * @return
     */
    public String getEndpointUri() {
        return endpointUri;
    }

    /**
     * Sets the endpoint uri.
     * @param endpointUri
     */
    public ReceiveTimeoutAction setEndpointUri(String endpointUri) {
        this.endpointUri = endpointUri;
        return this;
    }
}
