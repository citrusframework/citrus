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

import com.consol.citrus.CitrusConstants;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.endpoint.Endpoint;
import com.consol.citrus.validation.builder.MessageContentBuilder;
import com.consol.citrus.validation.builder.PayloadTemplateMessageBuilder;
import com.consol.citrus.variable.VariableExtractor;
import com.consol.citrus.variable.dictionary.DataDictionary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.integration.Message;

import java.util.ArrayList;
import java.util.List;


/**
 * This action sends a messages to a specified message endpoint. The action holds a reference to
 * a {@link com.consol.citrus.endpoint.Endpoint}, which is capable of the message transport implementation. So action is
 * independent of the message transport configuration.
 *
 * @author Christoph Deppisch 
 * @since 2008
 */
public class SendMessageAction extends AbstractTestAction {
    /** Message endpoint instance */
    private Endpoint endpoint;

    /** Message endpoint uri - either bean name or dynamic uri */
    private String endpointUri;

    /** List of variable extractors responsible for creating variables from received message content */
    private List<VariableExtractor> variableExtractors = new ArrayList<VariableExtractor>();
    
    /** Builder constructing a control message */
    private MessageContentBuilder<?> messageBuilder = new PayloadTemplateMessageBuilder();
    
    /** Forks the message sending action so other actions can take place while this
     * message sender is waiting for the synchronous response */
    private boolean forkMode = false;

    /** The message type to send in this action - this information is needed to find proper
     * message construction interceptors for this message */
    private String messageType = CitrusConstants.DEFAULT_MESSAGE_TYPE;

    /** Optional data dictionary that explicitly modifies message content before sending */
    private DataDictionary dataDictionary;

    /** Logger */
    private static Logger log = LoggerFactory.getLogger(SendMessageAction.class);

    /**
     * Default constructor.
     */
    public SendMessageAction() {
        setName("send");
    }

    /**
     * Message is constructed with payload and header entries and sent via
     * {@link com.consol.citrus.endpoint.Endpoint} instance.
     */
    @Override
    public void doExecute(TestContext context) {
        final Message<?> message = createMessage(context, messageType);
        
        // extract variables from before sending message so we can save dynamic message ids
        for (VariableExtractor variableExtractor : variableExtractors) {
            variableExtractor.extractVariables(message, context);
        }
        
        final Endpoint messageEndpoint = createOrGetEndpoint(context);
        if (forkMode) {
            log.info("Forking send message action ...");

            SimpleAsyncTaskExecutor taskExecutor = new SimpleAsyncTaskExecutor();
            taskExecutor.execute(new Runnable() {
                public void run() {
                    messageEndpoint.createProducer().send(message);
                }
            });
        } else {
            messageEndpoint.createProducer().send(message);
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isDisabled(TestContext context) {
        Endpoint messageEndpoint = createOrGetEndpoint(context);
        if (getActor() == null && messageEndpoint.getActor() != null) {
            return messageEndpoint.getActor().isDisabled();
        }
        
        return super.isDisabled(context);
    }

    /**
     * Create message to be sent.
     * @param context
     * @param messageType
     * @return
     */
    protected Message<?> createMessage(TestContext context, String messageType) {
        if (dataDictionary != null) {
            messageBuilder.setDataDictionary(dataDictionary);
        }

        return messageBuilder.buildMessageContent(context, messageType);
    }

    /**
     * Creates or gets the message endpoint instance.
     * @return the message endpoint
     */
    public Endpoint createOrGetEndpoint(TestContext context) {
        if (endpoint == null) {
            endpoint = context.getEndpointFactory().create(context.replaceDynamicContentInString(endpointUri), context.getApplicationContext());
        }

        return endpoint;
    }

    /**
     * Gets the message endpoint.
     * @return
     */
    public Endpoint getEndpoint() {
        return endpoint;
    }

    /**
     * Sets the message endpoint.
     * @param endpoint
     */
    public void setEndpoint(Endpoint endpoint) {
        this.endpoint = endpoint;
    }

    /**
     * Sets the message builder implementation.
     * @param messageBuilder the messageBuilder to set
     */
    public void setMessageBuilder(MessageContentBuilder<?> messageBuilder) {
        this.messageBuilder = messageBuilder;
    }

    /**
     * The variable extractors for this message sending action.
     * @param variableExtractors the variableExtractors to set
     */
    public void setVariableExtractors(List<VariableExtractor> variableExtractors) {
        this.variableExtractors = variableExtractors;
    }

    /**
     * Get the variable extractors.
     * @return the variableExtractors
     */
    public List<VariableExtractor> getVariableExtractors() {
        return variableExtractors;
    }

    /**
     * Gets the messageBuilder.
     * @return the messageBuilder
     */
    public MessageContentBuilder<?> getMessageBuilder() {
        return messageBuilder;
    }

    /**
     * Enables fork mode for this message sender.
     * @param fork the fork to set.
     */
    public void setForkMode(boolean fork) {
        this.forkMode = fork;
    }

    /**
     * Gets the forkMode.
     * @return the forkMode the forkMode to get.
     */
    public boolean isForkMode() {
        return forkMode;
    }

    /**
     * Sets the expected message type for this receive action.
     * @param messageType the messageType to set
     */
    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    /**
     * Gets the message type for this receive action.
     * @return the messageType
     */
    public String getMessageType() {
        return messageType;
    }

    /**
     * Gets the data dictionary.
     * @return
     */
    public DataDictionary getDataDictionary() {
        return dataDictionary;
    }

    /**
     * Sets the data dictionary.
     * @param dataDictionary
     */
    public void setDataDictionary(DataDictionary dataDictionary) {
        this.dataDictionary = dataDictionary;
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
    public void setEndpointUri(String endpointUri) {
        this.endpointUri = endpointUri;
    }
}