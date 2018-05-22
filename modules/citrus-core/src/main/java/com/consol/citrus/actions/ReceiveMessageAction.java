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

import com.consol.citrus.Citrus;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.endpoint.Endpoint;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.message.*;
import com.consol.citrus.messaging.Consumer;
import com.consol.citrus.messaging.SelectiveConsumer;
import com.consol.citrus.validation.DefaultMessageHeaderValidator;
import com.consol.citrus.validation.MessageValidator;
import com.consol.citrus.validation.builder.MessageContentBuilder;
import com.consol.citrus.validation.builder.PayloadTemplateMessageBuilder;
import com.consol.citrus.validation.callback.ValidationCallback;
import com.consol.citrus.validation.context.ValidationContext;
import com.consol.citrus.validation.json.JsonPathMessageValidationContext;
import com.consol.citrus.validation.script.ScriptValidationContext;
import com.consol.citrus.validation.xml.XpathMessageValidationContext;
import com.consol.citrus.variable.VariableExtractor;
import com.consol.citrus.variable.dictionary.DataDictionary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.*;

/**
 * This action receives messages from a service destination. Action uses a {@link com.consol.citrus.endpoint.Endpoint}
 * to receive the message, this means that action is independent from any message transport.
 * 
 * The received message is validated using a {@link MessageValidator} supporting expected 
 * control message payload and header templates.
 *
 * @author Christoph Deppisch
 * @since 2008
 */
public class ReceiveMessageAction extends AbstractTestAction {
    /** Build message selector with name value pairs */
    private Map<String, Object> messageSelectorMap = new HashMap<>();

    /** Select messages via message selector string */
    private String messageSelector;

    /** Message endpoint */
    private Endpoint endpoint;

    /** Message endpoint uri - either bean name or dynamic endpoint uri */
    private String endpointUri;
    
    /** Receive timeout */
    private long receiveTimeout = 0L;

    /** Builder constructing a control message */
    private MessageContentBuilder messageBuilder = new PayloadTemplateMessageBuilder();

    /** MessageValidator responsible for message validation */
    private List<MessageValidator<? extends ValidationContext>> validators = new ArrayList<>();

    /** Optional data dictionary that explicitly modifies message content before validation */
    private DataDictionary dataDictionary;
    
    /** Callback able to additionally validate received message */
    private ValidationCallback validationCallback;
    
    /** List of validation contexts for this receive action */
    private List<ValidationContext> validationContexts = new ArrayList<>();
    
    /** List of variable extractors responsible for creating variables from received message content */
    private List<VariableExtractor> variableExtractors = new ArrayList<VariableExtractor>();
    
    /** The expected message type to arrive in this receive action - this information is needed to find a proper
     * message validator for this message */
    private String messageType = Citrus.DEFAULT_MESSAGE_TYPE;

    /** Logger */
    private static Logger log = LoggerFactory.getLogger(ReceiveMessageAction.class);

    /**
     * Default constructor.
     */
    public ReceiveMessageAction() {
        setName("receive");
    }

    /**
     * Method receives a message via {@link com.consol.citrus.endpoint.Endpoint} instance
     * constructs a validation context and starts the message validation
     * via {@link MessageValidator}.
     *
     * @throws CitrusRuntimeException
     */
    @Override
    public void doExecute(TestContext context) {
        try {
            Message receivedMessage;
            String selector = MessageSelectorBuilder.build(messageSelector, messageSelectorMap, context);

            //receive message either selected or plain with message receiver
            if (StringUtils.hasText(selector)) {
                receivedMessage = receiveSelected(context, selector);
            } else {
                receivedMessage = receive(context);
            }

            if (receivedMessage == null) {
                throw new CitrusRuntimeException("Failed to receive message - message is not available");
            }

            //validate the message
            validateMessage(receivedMessage, context);
        } catch (IOException e) {
            throw new CitrusRuntimeException(e);
        }
    }

    /**
     * Receives the message with respective message receiver implementation.
     * @return
     */
    private Message receive(TestContext context) {
        Endpoint messageEndpoint = getOrCreateEndpoint(context);
        return receiveTimeout > 0 ? messageEndpoint.createConsumer().receive(context, receiveTimeout) :
                messageEndpoint.createConsumer().receive(context, messageEndpoint.getEndpointConfiguration().getTimeout());
    }

    /**
     * Receives the message with the respective message receiver implementation 
     * also using a message selector.
     * @param context the test context.
     * @param selectorString the message selector string.
     * @return
     */
    private Message receiveSelected(TestContext context, String selectorString) {
        if (log.isDebugEnabled()) {
            log.debug("Setting message selector: '" + selectorString + "'");
        }

        Endpoint messageEndpoint = getOrCreateEndpoint(context);
        Consumer consumer = messageEndpoint.createConsumer();
        if (consumer instanceof SelectiveConsumer) {
            if (receiveTimeout > 0) {
                return ((SelectiveConsumer) messageEndpoint.createConsumer()).receive(
                        context.replaceDynamicContentInString(selectorString),
                        context, receiveTimeout);
            } else {
                return ((SelectiveConsumer) messageEndpoint.createConsumer()).receive(
                        context.replaceDynamicContentInString(selectorString),
                        context, messageEndpoint.getEndpointConfiguration().getTimeout());
            }
        } else {
            log.warn(String.format("Unable to receive selective with consumer implementation: '%s'", consumer.getClass()));
            return receive(context);
        }
    }

    /**
     * Override this message if you want to add additional message validation
     * @param receivedMessage
     */
    protected void validateMessage(Message receivedMessage, TestContext context) throws IOException {
        // extract variables from received message content
        for (VariableExtractor variableExtractor : variableExtractors) {
            variableExtractor.extractVariables(receivedMessage, context);
        }

        if (validationCallback != null) {
            if (StringUtils.hasText(receivedMessage.getName())) {
                context.getMessageStore().storeMessage(receivedMessage.getName(), receivedMessage);
            } else {
                context.getMessageStore().storeMessage(context.getMessageStore().constructMessageName(this, getOrCreateEndpoint(context)), receivedMessage);
            }

            validationCallback.validate(receivedMessage, context);
        } else {
            Message controlMessage = createControlMessage(context, messageType);
            if (StringUtils.hasText(controlMessage.getName())) {
                context.getMessageStore().storeMessage(controlMessage.getName(), receivedMessage);
            } else {
                context.getMessageStore().storeMessage(context.getMessageStore().constructMessageName(this, getOrCreateEndpoint(context)), receivedMessage);
            }

            if (!CollectionUtils.isEmpty(validators)) {
                for (MessageValidator<? extends ValidationContext> messageValidator : validators) {
                    messageValidator.validateMessage(receivedMessage, controlMessage, context, validationContexts);
                }

                if (validators.parallelStream()
                                .map(Object::getClass)
                                .noneMatch(DefaultMessageHeaderValidator.class::isAssignableFrom)) {
                    MessageValidator defaultMessageHeaderValidator = context.getMessageValidatorRegistry().getDefaultMessageHeaderValidator();
                    if (defaultMessageHeaderValidator != null) {
                        defaultMessageHeaderValidator.validateMessage(receivedMessage, controlMessage, context, validationContexts);
                    }
                }
            } else {
                List<MessageValidator<? extends ValidationContext>> validators =
                        context.getMessageValidatorRegistry().findMessageValidators(messageType, receivedMessage);

                if (validators.isEmpty()) {
                    if (controlMessage.getPayload() instanceof String &&
                            StringUtils.hasText(controlMessage.getPayload(String.class))) {
                        throw new CitrusRuntimeException(String.format("Unable to find proper message validator for message type '%s' and validation contexts '%s'", messageType, validationContexts));
                    } else if (validationContexts.stream().anyMatch(item -> JsonPathMessageValidationContext.class.isAssignableFrom(item.getClass())
                            || XpathMessageValidationContext.class.isAssignableFrom(item.getClass())
                            || ScriptValidationContext.class.isAssignableFrom(item.getClass()))) {
                        throw new CitrusRuntimeException(String.format("Unable to find proper message validator for message type '%s' and validation contexts '%s'", messageType, validationContexts));
                    } else {
                        log.warn(String.format("Unable to find proper message validator for message type '%s' and validation contexts '%s'", messageType, validationContexts));
                    }
                }

                for (MessageValidator<? extends ValidationContext> messageValidator : validators) {
                    messageValidator.validateMessage(receivedMessage, controlMessage, context, validationContexts);
                }
            }
        }
    }

    /**
     * Create control message that is expected.
     * @param context
     * @param messageType
     * @return
     */
    protected Message createControlMessage(TestContext context, String messageType) {
        if (dataDictionary != null) {
            messageBuilder.setDataDictionary(dataDictionary);
        }

        return messageBuilder.buildMessageContent(context, messageType, MessageDirection.INBOUND);
    }
    
    @Override
    public boolean isDisabled(TestContext context) {
        Endpoint messageEndpoint = getOrCreateEndpoint(context);
        if (getActor() == null && messageEndpoint.getActor() != null) {
            return messageEndpoint.getActor().isDisabled();
        }
        
        return super.isDisabled(context);
    }

    /**
     * Setter for messageSelectorMap.
     * @param messageSelectorMap
     */
    public ReceiveMessageAction setMessageSelectorMap(Map<String, Object> messageSelectorMap) {
        this.messageSelectorMap = messageSelectorMap;
        return this;
    }

    /**
     * Set message selector string.
     * @param messageSelector
     */
    public ReceiveMessageAction setMessageSelector(String messageSelector) {
        this.messageSelector = messageSelector;
        return this;
    }

    /**
     * Set list of message validators.
     * @param validators the message validators to set
     */
    public ReceiveMessageAction setValidators(List<MessageValidator<? extends ValidationContext>> validators) {
        this.validators.clear();
        this.validators.addAll(validators);
        return this;
    }

    /**
     * Adds message validator to the list of explicit validators.
     * @param validator the message validator to set
     */
    public ReceiveMessageAction addValidator(MessageValidator<? extends ValidationContext> validator) {
        this.validators.add(validator);
        return this;
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
     * Set message endpoint instance.
     * @param endpoint the message endpoint
     */
    public ReceiveMessageAction setEndpoint(Endpoint endpoint) {
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
    public ReceiveMessageAction setEndpointUri(String endpointUri) {
        this.endpointUri = endpointUri;
        return this;
    }

    /**
     * Set the receive timeout.
     * @param receiveTimeout the receiveTimeout to set
     */
    public ReceiveMessageAction setReceiveTimeout(long receiveTimeout) {
        this.receiveTimeout = receiveTimeout;
        return this;
    }
    
    /**
     * Adds a new variable extractor.
     * @param variableExtractor the variableExtractor to set
     */
    public ReceiveMessageAction addVariableExtractors(VariableExtractor variableExtractor) {
        this.variableExtractors.add(variableExtractor);
        return this;
    }

    /**
     * Set the list of variable extractors.
     * @param variableExtractors the variableExtractors to set
     */
    public ReceiveMessageAction setVariableExtractors(List<VariableExtractor> variableExtractors) {
        this.variableExtractors = variableExtractors;
        return this;
    }

    /**
     * Sets the list of available validation contexts for this action.
     * @param validationContexts the validationContexts to set
     */
    public ReceiveMessageAction setValidationContexts(List<ValidationContext> validationContexts) {
        this.validationContexts = validationContexts;
        return this;
    }

    /**
     * Gets the variable extractors.
     * @return the variableExtractors
     */
    public List<VariableExtractor> getVariableExtractors() {
        return variableExtractors;
    }

    /**
     * Sets the expected message type for this receive action.
     * @param messageType the messageType to set
     */
    public ReceiveMessageAction setMessageType(String messageType) {
        this.messageType = messageType;
        return this;
    }

    /**
     * Gets the message type for this receive action.
     * @return the messageType
     */
    public String getMessageType() {
        return messageType;
    }

    /**
     * Gets the messageSelectorMap.
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
     * Gets the validator.
     * @return the validator
     */
    public List<MessageValidator<? extends ValidationContext>> getValidators() {
        return Collections.unmodifiableList(validators);
    }

    /**
     * Gets the validationContexts.
     * @return the validationContexts
     */
    public List<ValidationContext> getValidationContexts() {
        return validationContexts;
    }

    /**
     * Gets the validationCallback.
     * @return the validationCallback the validationCallback to get.
     */
    public ValidationCallback getValidationCallback() {
        return validationCallback;
    }

    /**
     * Sets the validationCallback.
     * @param validationCallback the validationCallback to set
     */
    public ReceiveMessageAction setValidationCallback(ValidationCallback validationCallback) {
        this.validationCallback = validationCallback;
        return this;
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
    public ReceiveMessageAction setDataDictionary(DataDictionary dataDictionary) {
        this.dataDictionary = dataDictionary;
        return this;
    }

    /**
     * Gets the messageBuilder.
     * @return the messageBuilder
     */
    public MessageContentBuilder getMessageBuilder() {
        return messageBuilder;
    }

    /**
     * Sets the message builder implementation.
     * @param messageBuilder the messageBuilder to set
     */
    public ReceiveMessageAction setMessageBuilder(MessageContentBuilder messageBuilder) {
        this.messageBuilder = messageBuilder;
        return this;
    }
}
