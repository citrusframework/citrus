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

import java.io.IOException;
import java.text.ParseException;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.integration.core.Message;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.message.MessageReceiver;
import com.consol.citrus.message.MessageSelectorBuilder;
import com.consol.citrus.validation.ControlMessageValidationContext;
import com.consol.citrus.validation.MessageValidator;
import com.consol.citrus.validation.context.ValidationContext;
import com.consol.citrus.validation.script.*;
import com.consol.citrus.validation.xml.*;
import com.consol.citrus.variable.VariableExtractor;

/**
 * This action receives messages from a service destination. Action uses a {@link MessageReceiver} 
 * to receive the message, this means that action is independent from any message transport.
 * 
 * The received message is validated using a {@link MessageValidator} supporting expected 
 * control message payload and header templates.
 *
 * @author Christoph Deppisch
 * @since 2008
 */
public class ReceiveMessageAction extends AbstractTestAction implements XmlMessageValidationAware, ScriptValidationAware {
    /** Build message selector with name value pairs */
    private Map<String, String> messageSelector = new HashMap<String, String>();

    /** Select messages via message selector string */
    private String messageSelectorString;

    /** Message receiver */
    private MessageReceiver messageReceiver;
    
    /** Receive timeout */
    private long receiveTimeout = 0L;

    /** MessageValidator responsible for message validation */
    private MessageValidator<? extends ValidationContext> validator;
    
    /** Builds a script validation context */
    private ScriptValidationContextBuilder scriptValidationContextBuilder;
    
    /** Builds a xml message validation context */
    private XmlMessageValidationContextBuilder xmlMessageValidationContextBuilder;
    
    /** List of variable extractors responsible for creating variables from received message content */
    private List<VariableExtractor> variableExtractors = new ArrayList<VariableExtractor>();

    /**
     * Logger
     */
    private static final Logger log = LoggerFactory.getLogger(ReceiveMessageAction.class);

    /**
     * Method receives a message via {@link MessageReceiver} instance
     * constructs a validation context and starts the message validation
     * via {@link MessageValidator}.
     * 
     * @throws CitrusRuntimeException
     */
    @Override
    public void execute(TestContext context) {
        Message<?> receivedMessage;
        
        try {
            //receive message either selected or plain with message receiver
            if (StringUtils.hasText(messageSelectorString)) {
                if (log.isDebugEnabled()) {
                    log.debug("Setting message selector: '" + messageSelectorString + "'");
                }
                
                if(receiveTimeout > 0) {
                    receivedMessage = messageReceiver.receiveSelected(
                            context.replaceDynamicContentInString(messageSelectorString), 
                            receiveTimeout);
                } else {
                    receivedMessage = messageReceiver.receiveSelected(
                            context.replaceDynamicContentInString(messageSelectorString));
                }
            } else if (!CollectionUtils.isEmpty(messageSelector)) {
                String constructedMessageSelector = MessageSelectorBuilder.fromKeyValueMap(
                        context.replaceVariablesInMap(messageSelector)).build();
                        
                if (log.isDebugEnabled()) {
                    log.debug("Setting message selector: '" + constructedMessageSelector + "'");
                }
                
                if(receiveTimeout > 0) {
                    receivedMessage = messageReceiver
                            .receiveSelected(constructedMessageSelector, receiveTimeout);
                } else {
                    receivedMessage = messageReceiver
                            .receiveSelected(constructedMessageSelector);
                }
            } else {
                receivedMessage = receiveTimeout > 0 ? messageReceiver.receive(receiveTimeout) : messageReceiver.receive();
            }

            if (receivedMessage == null) {
                throw new CitrusRuntimeException("Unable to process received message - message is null");
            }

            // extract variables from received message content
            for (VariableExtractor variableExtractor : variableExtractors) {
                variableExtractor.extractVariables(receivedMessage, context);
            }
            
            //validate the message
            validateMessage(receivedMessage, context);
        } catch (ParseException e) {
            throw new CitrusRuntimeException(e);
        } catch (IOException e) {
            throw new CitrusRuntimeException(e);
        }
    }

    /**
     * Override this message if you want to add additional message validation
     * @param receivedMessage
     */
    protected void validateMessage(Message<?> receivedMessage, TestContext context) throws ParseException, IOException {
        if (validator != null) {
            ValidationContext validationContext = validator.createValidationContext(this, context);
            validator.validateMessage(receivedMessage, context, validationContext);
        } else {
            List<MessageValidator<? extends ValidationContext>> validators = context.getMessageValidators();
            
            for (MessageValidator<? extends ValidationContext> messageValidator : validators) {
                messageValidator.validateMessage(receivedMessage, context, 
                        messageValidator.createValidationContext(this, context));
            }
        }
    }

    /**
     * Setter for messageSelector.
     * @param messageSelector
     */
    public void setMessageSelector(Map<String, String> messageSelector) {
        this.messageSelector = messageSelector;
    }

    /**
     * Set message selector string.
     * @param messageSelectorString
     */
    public void setMessageSelectorString(String messageSelectorString) {
        this.messageSelectorString = messageSelectorString;
    }

    /**
     * Set single message validator.
     * @param validator the message validator to set
     */
    public void setValidator(MessageValidator<? extends ValidationContext> validator) {
        this.validator = validator;
    }
    
    /**
     * Set message receiver instance.
     * @param messageReceiver the messageReceiver to set
     */
    public void setMessageReceiver(MessageReceiver messageReceiver) {
        this.messageReceiver = messageReceiver;
    }

    /**
     * Get the message receiver.
     * @return the messageReceiver
     */
    public MessageReceiver getMessageReceiver() {
        return messageReceiver;
    }

    /**
     * Set the receive timeout.
     * @param receiveTimeout the receiveTimeout to set
     */
    public void setReceiveTimeout(long receiveTimeout) {
        this.receiveTimeout = receiveTimeout;
    }
    
    /**
     * Get the script validation context.
     */
    public ScriptValidationContext getScriptValidationContext(TestContext context) {
        if (scriptValidationContextBuilder == null) {
            throw new CitrusRuntimeException("Unable to perform script validation - no context builder available");
        }
        
        return scriptValidationContextBuilder.buildValidationContext(context);
    }

    /**
     * Get the xml message validation context.
     */
    public XmlMessageValidationContext getXmlMessageValidationContext(TestContext context) {
        if (xmlMessageValidationContextBuilder == null) {
            throw new CitrusRuntimeException("Unable to perform xml message validation - no context builder available");
        }
        
        return xmlMessageValidationContextBuilder.buildValidationContext(context);
    }

    /**
     * Get the control message validation context.
     */
    public ControlMessageValidationContext getControlMessageValidationContext(TestContext context) {
        if (xmlMessageValidationContextBuilder == null) {
            throw new CitrusRuntimeException("Unable to perform control message validation - no context builder available");
        }
        
        return xmlMessageValidationContextBuilder.buildValidationContext(context);
    }

    /**
     * Sets the xml validation context builder.
     * @param xmlMessageValidationContextBuilder the xmlMessageValidationContextBuilder to set
     */
    public void setXmlMessageValidationContextBuilder(
            XmlMessageValidationContextBuilder xmlMessageValidationContextBuilder) {
        this.xmlMessageValidationContextBuilder = xmlMessageValidationContextBuilder;
    }

    /**
     * Sets the script validation context builder.
     * @param scriptValidationContextBuilder the scriptValidationContextBuilder to set
     */
    public void setScriptValidationContextBuilder(
            ScriptValidationContextBuilder scriptValidationContextBuilder) {
        this.scriptValidationContextBuilder = scriptValidationContextBuilder;
    }

    /**
     * Adds a new variable extractor.
     * @param variableExtractor the variableExtractor to set
     */
    public void addVariableExtractors(VariableExtractor variableExtractor) {
        this.variableExtractors.add(variableExtractor);
    }

    /**
     * Set the list of variable extractors.
     * @param variableExtractors the variableExtractors to set
     */
    public void setVariableExtractors(List<VariableExtractor> variableExtractors) {
        this.variableExtractors = variableExtractors;
    }
}
