/*
 * Copyright 2006-2012 the original author or authors.
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

package com.consol.citrus.dsl;

import java.util.Map;

import org.springframework.context.ApplicationContext;
import org.springframework.integration.Message;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.util.Assert;

import com.consol.citrus.CitrusConstants;
import com.consol.citrus.actions.ReceiveMessageAction;
import com.consol.citrus.message.MessageReceiver;
import com.consol.citrus.message.MessageType;
import com.consol.citrus.validation.ControlMessageValidationContext;
import com.consol.citrus.validation.MessageValidator;
import com.consol.citrus.validation.builder.AbstractMessageContentBuilder;
import com.consol.citrus.validation.builder.StaticMessageContentBuilder;
import com.consol.citrus.validation.callback.ValidationCallback;
import com.consol.citrus.validation.context.ValidationContext;
import com.consol.citrus.validation.xml.XmlMessageValidationContext;
import com.consol.citrus.variable.MessageHeaderVariableExtractor;
import com.consol.citrus.variable.XpathPayloadVariableExtractor;

/**
 * Receive message action definition offers configuration methods for a receive test action. Build options 
 * include construction of control message payload and headers as well as value extraction.
 * 
 * @author Christoph Deppisch
 */
public class ReceiveMessageActionDefinition extends AbstractActionDefinition<ReceiveMessageAction> {

    private MessageType messageType = MessageType.valueOf(CitrusConstants.DEFAULT_MESSAGE_TYPE);
    
    private ControlMessageValidationContext validationContext;
    
    private MessageHeaderVariableExtractor headerExtractor;
    
    private XpathPayloadVariableExtractor xpathExtractor;
    
    private ApplicationContext applicationContext;
    
    /**
     * Default constructor using test action.
     * @param action
     */
    public ReceiveMessageActionDefinition(ReceiveMessageAction action, ApplicationContext ctx) {
        super(action);
        this.applicationContext = ctx;
    }
    
    public ReceiveMessageActionDefinition with(String messageReceiverName) {
        Assert.notNull(applicationContext, "Citrus application context is not initialized!");
        
        action.setMessageReceiver(applicationContext.getBean(messageReceiverName, MessageReceiver.class));
        return this;
    }
    
    /**
     * Adds message receiver reference to this definitions test action.
     * @param messageReceiver
     * @return
     */
    public ReceiveMessageActionDefinition with(MessageReceiver messageReceiver) {
        action.setMessageReceiver(messageReceiver);
        return this;
    }
    
    /**
     * Adds a custom timeout to this message receiving action. 
     * @param receiveTimeout
     * @return
     */
    public ReceiveMessageActionDefinition timeout(long receiveTimeout) {
        action.setReceiveTimeout(receiveTimeout);
        return this;
    }
    
    /**
     * Expect a control message in this receive action.
     * @param controlMessage
     * @return
     */
    public ReceiveMessageActionDefinition validate(Message<?> controlMessage) {
        if (messageType.equals(MessageType.XML)) {
            validationContext = new XmlMessageValidationContext();
            validationContext.setControlMessage(controlMessage);
            
            action.getValidationContexts().add(validationContext);
        } else {
            validationContext = new ControlMessageValidationContext();
            validationContext.setControlMessage(controlMessage);
            
            action.getValidationContexts().add(validationContext);
        }
        
        return this;
    }
    
    /**
     * Expect message payload.
     * @param payload
     * @return
     */
    public ReceiveMessageActionDefinition validatePayload(String payload) {
        return validate(MessageBuilder.withPayload(payload).build());
    }
    
    /**
     * Expect message header entry.
     * @param name
     * @param value
     * @return
     */
    public ReceiveMessageActionDefinition validateHeader(String name, Object value) {
        if (validationContext != null) {
            if (validationContext.getMessageBuilder() instanceof AbstractMessageContentBuilder<?>) {
                ((AbstractMessageContentBuilder<?>)validationContext.getMessageBuilder()).getMessageHeaders().put(name, value);
            } else if (validationContext.getMessageBuilder() instanceof StaticMessageContentBuilder<?>) {
                Message<?> message = ((StaticMessageContentBuilder<?>)validationContext.getMessageBuilder()).buildMessageContent(null);
                validationContext.setControlMessage(MessageBuilder.fromMessage(message).setHeader(name, value).build());
            }
        } else {
            validationContext = new ControlMessageValidationContext();
            validationContext.setControlMessage(MessageBuilder.withPayload("").setHeader(name, value).build());
            action.getValidationContexts().add(validationContext);
        }
        
        return this;
    }
    
    /**
     * Sets a explicit message type for this receive action.
     * @param messageType
     * @return
     */
    public ReceiveMessageActionDefinition type(MessageType messageType) {
        this.messageType = messageType;
        action.setMessageType(messageType.toString().toLowerCase());
        return this;
    }
    
    /**
     * Sets message selector string.
     * @param messageSelector
     * @return
     */
    public ReceiveMessageActionDefinition select(String messageSelector) {
        action.setMessageSelectorString(messageSelector);
        
        return this;
    }
    
    /**
     * Sets message selector elements.
     * @param messageSelector
     * @return
     */
    public ReceiveMessageActionDefinition select(Map<String, String> messageSelector) {
        action.setMessageSelector(messageSelector);
        
        return this;
    }
    
    /**
     * Sets explicit message validator for this receive action.
     * @param validator
     * @return
     */
    public ReceiveMessageActionDefinition validator(MessageValidator<? extends ValidationContext> validator) {
        action.setValidator(validator);
        return this;
    }
    
    /**
     * Extract message header entry as variable.
     * @param headerName
     * @param variable
     * @return
     */
    public ReceiveMessageActionDefinition extractFromHeader(String headerName, String variable) {
        if (headerExtractor == null) {
            headerExtractor = new MessageHeaderVariableExtractor();
            
            action.getVariableExtractors().add(headerExtractor);
        }
        
        headerExtractor.getHeaderMappings().put(headerName, variable);
        return this;
    }
    
    /**
     * Extract message element vie XPath from message payload as new test variable.
     * @param headerName
     * @param variable
     * @return
     */
    public ReceiveMessageActionDefinition extractFromPayload(String xpath, String variable) {
        if (xpathExtractor == null) {
            xpathExtractor = new XpathPayloadVariableExtractor();
            
            action.getVariableExtractors().add(xpathExtractor);
        }
        
        xpathExtractor.getxPathExpressions().put(xpath, variable);
        return this;
    }
    
    /**
     * Adds validation callback to the receive action for validating 
     * the received message with Java code.
     * @param callback
     * @return
     */
    public ReceiveMessageActionDefinition validationCallback(ValidationCallback callback) {
        action.setValidationCallback(callback);
        
        return this;
    }

}
