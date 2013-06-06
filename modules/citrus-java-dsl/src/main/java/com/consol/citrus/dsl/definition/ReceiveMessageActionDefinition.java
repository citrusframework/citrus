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

package com.consol.citrus.dsl.definition;

import java.io.IOException;
import java.util.Map;

import com.consol.citrus.xml.namespace.NamespaceContextBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.integration.Message;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.util.Assert;

import com.consol.citrus.CitrusConstants;
import com.consol.citrus.actions.ReceiveMessageAction;
import com.consol.citrus.dsl.util.PositionHandle;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.message.MessageType;
import com.consol.citrus.util.FileUtils;
import com.consol.citrus.validation.ControlMessageValidationContext;
import com.consol.citrus.validation.MessageValidator;
import com.consol.citrus.validation.builder.*;
import com.consol.citrus.validation.callback.ValidationCallback;
import com.consol.citrus.validation.context.ValidationContext;
import com.consol.citrus.validation.script.ScriptValidationContext;
import com.consol.citrus.validation.xml.XmlMessageValidationContext;
import com.consol.citrus.variable.MessageHeaderVariableExtractor;
import com.consol.citrus.variable.XpathPayloadVariableExtractor;
import com.consol.citrus.ws.actions.ReceiveSoapMessageAction;

/**
 * Receive message action definition offers configuration methods for a receive test action. Build options 
 * include construction of control message payload and headers as well as value extraction.
 * 
 * @author Christoph Deppisch
 */
public class ReceiveMessageActionDefinition extends AbstractActionDefinition<ReceiveMessageAction> {

    /** Message type for this action definition */
    private MessageType messageType = MessageType.valueOf(CitrusConstants.DEFAULT_MESSAGE_TYPE);
    
    /** Validation context used in this action definition */
    private ControlMessageValidationContext validationContext;
    
    /** Script validation context used in this action definition */
    private ScriptValidationContext scriptValidationContext;
    
    /** Variable extractors filled within this action definition */
    private MessageHeaderVariableExtractor headerExtractor;
    private XpathPayloadVariableExtractor xpathExtractor;
    
    /** Basic application context */
    private ApplicationContext applicationContext;
    
    /** Handle for test action position in test case sequence use when switching to SOAP specific definition */
    private PositionHandle positionHandle;
    
    /**
     * Default constructor using test action, basic application context and position handle.
     * @param action
     * @param ctx
     * @param positionHandle
     */
    public ReceiveMessageActionDefinition(ReceiveMessageAction action, ApplicationContext ctx, PositionHandle positionHandle) {
        super(action);
        this.applicationContext = ctx;
        this.positionHandle = positionHandle;
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
    public ReceiveMessageActionDefinition message(Message<?> controlMessage) {
        if (validationContext != null) {
            throw new CitrusRuntimeException("Unable to set control message object when header and/or payload was set before");
        }
        
        initializeValidationContext();
        
        validationContext.setControlMessage(controlMessage);
        
        return this;
    }
    
    /**
     * Expect this message payload data in received message.
     * @param payload
     * @return
     */
    public ReceiveMessageActionDefinition payload(String payload) {
        if (validationContext != null) {
            if (validationContext.getMessageBuilder() instanceof PayloadTemplateMessageBuilder) {
                ((PayloadTemplateMessageBuilder)validationContext.getMessageBuilder()).setPayloadData(payload);
            } else if (validationContext.getMessageBuilder() instanceof StaticMessageContentBuilder<?>) {
                Message<?> message = ((StaticMessageContentBuilder<?>)validationContext.getMessageBuilder()).buildMessageContent(null);
                validationContext.setControlMessage(MessageBuilder.withPayload(payload).copyHeaders(message.getHeaders()).build());
            }
        } else {
            initializeValidationContext();
            
            PayloadTemplateMessageBuilder messageBuilder = new PayloadTemplateMessageBuilder();
            messageBuilder.setPayloadData(payload);
            validationContext.setMessageBuilder(messageBuilder);
        }
        
        return this;
    }
    
    /**
     * Creates new validation context according to message type.
     */
    private void initializeValidationContext() {
        if (validationContext == null) {
            if (messageType.equals(MessageType.XML)) {
                validationContext = new XmlMessageValidationContext();
            } else {
                validationContext = new ControlMessageValidationContext();
            }
            
            action.getValidationContexts().add(validationContext);
        }
    }
    
    /**
     * Creates new script validation context.
     */
    private void initializeScriptValidationContext() {
        if (scriptValidationContext == null) {
            scriptValidationContext = new ScriptValidationContext();
            
            action.getValidationContexts().add(scriptValidationContext);
        }
    }
    
    /**
     * Creates new variable extractor and adds it to test action.
     */
    private void initializeXpathVariableExtractor() {
        if (xpathExtractor == null) {
            xpathExtractor = new XpathPayloadVariableExtractor();

            if (applicationContext.getBeansOfType(NamespaceContextBuilder.class).size() > 0) {
                xpathExtractor.setNamespaceContextBuilder(applicationContext.getBean(NamespaceContextBuilder.class));
            }

            action.getVariableExtractors().add(xpathExtractor);
        }
    }

    /**
     * Expect this message payload data in received message.
     * @param payloadResource
     * @return
     */
    public ReceiveMessageActionDefinition payload(Resource payloadResource) {
        try {
            if (validationContext != null) {
                if (validationContext.getMessageBuilder() instanceof PayloadTemplateMessageBuilder) {
                    ((PayloadTemplateMessageBuilder)validationContext.getMessageBuilder()).setPayloadData(FileUtils.readToString(payloadResource));
                } else if (validationContext.getMessageBuilder() instanceof StaticMessageContentBuilder<?>) {
                    PayloadTemplateMessageBuilder messageBuilder = new PayloadTemplateMessageBuilder();
                    messageBuilder.setPayloadData(FileUtils.readToString(payloadResource));
    
                    Message<?> message = ((StaticMessageContentBuilder<?>)validationContext.getMessageBuilder()).buildMessageContent(null);
                    messageBuilder.setMessageHeaders(message.getHeaders());
                    
                    validationContext.setMessageBuilder(messageBuilder);
                }
            } else {
                initializeValidationContext();
                
                PayloadTemplateMessageBuilder messageBuilder = new PayloadTemplateMessageBuilder();
                messageBuilder.setPayloadData(FileUtils.readToString(payloadResource));
                validationContext.setMessageBuilder(messageBuilder);
            }
        } catch (IOException e) {
            throw new CitrusRuntimeException("Failed to read payload resource", e);
        }
        
        return this;
    }
    
    /**
     * Expect this message header entry in received message.
     * @param name
     * @param value
     * @return
     */
    public ReceiveMessageActionDefinition header(String name, Object value) {
        if (validationContext != null) {
            if (validationContext.getMessageBuilder() instanceof AbstractMessageContentBuilder<?>) {
                ((AbstractMessageContentBuilder<?>)validationContext.getMessageBuilder()).getMessageHeaders().put(name, value);
            } else if (validationContext.getMessageBuilder() instanceof StaticMessageContentBuilder<?>) {
                Message<?> message = ((StaticMessageContentBuilder<?>)validationContext.getMessageBuilder()).buildMessageContent(null);
                validationContext.setControlMessage(MessageBuilder.fromMessage(message).setHeader(name, value).build());
            }
        } else {
            initializeValidationContext();
            
            PayloadTemplateMessageBuilder messageBuilder = new PayloadTemplateMessageBuilder();
            messageBuilder.getMessageHeaders().put(name, value);
            validationContext.setMessageBuilder(messageBuilder);
        }
        
        return this;
    }
    
    /**
     * Expect this message header data in received message. Message header data is used in 
     * SOAP messages as XML fragment for instance.
     * @param data
     * @return
     */
    public ReceiveMessageActionDefinition header(String data) {
        if (validationContext != null) {
            if (validationContext.getMessageBuilder() instanceof AbstractMessageContentBuilder<?>) {
                ((AbstractMessageContentBuilder<?>)validationContext.getMessageBuilder()).setMessageHeaderData(data);
            } else if (validationContext.getMessageBuilder() instanceof StaticMessageContentBuilder<?>) {
                // convert to payload template message builder
                PayloadTemplateMessageBuilder messageBuilder = new PayloadTemplateMessageBuilder();
                
                Message<?> message = ((StaticMessageContentBuilder<?>)validationContext.getMessageBuilder()).buildMessageContent(null);
                messageBuilder.setPayloadData(message.getPayload().toString());
                messageBuilder.getMessageHeaders().putAll(message.getHeaders());
                messageBuilder.setMessageHeaderData(data);
                validationContext.setMessageBuilder(messageBuilder);
            }
        } else {
            initializeValidationContext();
            
            PayloadTemplateMessageBuilder messageBuilder = new PayloadTemplateMessageBuilder();
            messageBuilder.setMessageHeaderData(data);
            validationContext.setMessageBuilder(messageBuilder);
        }
        
        return this;
    }
    
    /**
     * Expect this message header data in received message from file resource. Message header data is used in 
     * SOAP messages as XML fragment for instance.
     * @param resource
     * @return
     */
    public ReceiveMessageActionDefinition header(Resource resource) {
        try {
            if (validationContext != null) {
                if (validationContext.getMessageBuilder() instanceof AbstractMessageContentBuilder<?>) {
                    ((AbstractMessageContentBuilder<?>)validationContext.getMessageBuilder()).setMessageHeaderData(FileUtils.readToString(resource));
                } else if (validationContext.getMessageBuilder() instanceof StaticMessageContentBuilder<?>) {
                    // convert to payload template message builder
                    PayloadTemplateMessageBuilder messageBuilder = new PayloadTemplateMessageBuilder();
                    
                    Message<?> message = ((StaticMessageContentBuilder<?>)validationContext.getMessageBuilder()).buildMessageContent(null);
                    messageBuilder.setPayloadData(message.getPayload().toString());
                    messageBuilder.getMessageHeaders().putAll(message.getHeaders());
                    messageBuilder.setMessageHeaderData(FileUtils.readToString(resource));
                    validationContext.setMessageBuilder(messageBuilder);
                }
            } else {
                initializeValidationContext();
                
                PayloadTemplateMessageBuilder messageBuilder = new PayloadTemplateMessageBuilder();
                messageBuilder.setMessageHeaderData(FileUtils.readToString(resource));
                validationContext.setMessageBuilder(messageBuilder);
            }
        } catch (IOException e) {
            throw new CitrusRuntimeException("Failed to read header resource", e);
        }    
        
        return this;
    }
    
    /**
     * Adds script validation.
     * @param validationScript
     * @return
     */
    public ReceiveMessageActionDefinition validateScript(String validationScript) {
        initializeScriptValidationContext();
        
        scriptValidationContext.setValidationScript(validationScript);
        
        return this;
    }
    
    /**
     * Adds script validation by file resource.
     * @param scriptResource
     * @return
     */
    public ReceiveMessageActionDefinition validateScript(Resource scriptResource) {
        initializeScriptValidationContext();
        
        try {
            scriptValidationContext.setValidationScriptResourcePath(scriptResource.getFile().getAbsolutePath());
        } catch (IOException e) {
            throw new CitrusRuntimeException("Failed to read script resource file", e);
        }
        
        return this;
    }
    
    /**
     * Adds custom validation script type.
     * @param type
     * @return
     */
    public ReceiveMessageActionDefinition validateScriptType(String type) {
        initializeScriptValidationContext();
        scriptValidationContext.setScriptType(type);
        
        return this;
    }
    
    /**
     * Sets a explicit message type for this receive action.
     * @param messageType
     * @return
     */
    public ReceiveMessageActionDefinition messageType(MessageType messageType) {
        this.messageType = messageType;
        action.setMessageType(messageType.toString());
        return this;
    }
    
    /**
     * Sets schema validation enabled/disabled for this message.
     * @param enabled
     * @return
     */
    public ReceiveMessageActionDefinition schemaValidation(boolean enabled) {
        initializeValidationContext();
        
        if (validationContext instanceof XmlMessageValidationContext) {
            ((XmlMessageValidationContext)validationContext).setSchemaValidation(enabled);
        } else {
            throw new CitrusRuntimeException("Unable to enable/disable schema validation on non XML message type");
        }
        
        return this;
    }
    
    /**
     * Validates XML namespace with prefix and uri.
     * @param prefix
     * @param namespaceUri
     * @return
     */
    public ReceiveMessageActionDefinition validateNamespace(String prefix, String namespaceUri) {
        initializeValidationContext();
        
        if (validationContext instanceof XmlMessageValidationContext) {
            ((XmlMessageValidationContext)validationContext).getControlNamespaces().put(prefix, namespaceUri);
        } else {
            throw new CitrusRuntimeException("Unable to validate namespaces on non XML message type");
        }
        
        return this;
    }
    
    /**
     * Adds message element validation.
     * @param path
     * @param controlValue
     * @return
     */
    public ReceiveMessageActionDefinition validate(String path, String controlValue) {
        initializeValidationContext();
        
        if (validationContext instanceof XmlMessageValidationContext) {
            ((XmlMessageValidationContext)validationContext).getPathValidationExpressions().put(path, controlValue);
        } else {
            throw new CitrusRuntimeException("Unable to set path validation expression on non XML message type");
        }
        
        return this;
    }
    
    /**
     * Adds ignore path expression for message element.
     * @param path
     * @return
     */
    public ReceiveMessageActionDefinition ignore(String path) {
        initializeValidationContext();
        
        if (validationContext instanceof XmlMessageValidationContext) {
            ((XmlMessageValidationContext)validationContext).getIgnoreExpressions().add(path);
        } else {
            throw new CitrusRuntimeException("Unable to ignore path expression on non XML message type");
        }
        
        return this;
    }
    
    /**
     * Adds XPath message element validation.
     * @param xPathExpression
     * @param controlValue
     * @return
     */
    public ReceiveMessageActionDefinition xpath(String xPathExpression, String controlValue) {
        validate(xPathExpression, controlValue);
        return this;
    }
    
    /**
     * Sets explicit schema instance name to use for schema validation.
     * @param schemaName
     * @return
     */
    public ReceiveMessageActionDefinition xsd(String schemaName) {
        initializeValidationContext();
        
        if (validationContext instanceof XmlMessageValidationContext) {
            ((XmlMessageValidationContext)validationContext).setSchema(schemaName);
        } else {
            throw new CitrusRuntimeException("Unable to xsd schema on non XML message type");
        }
        
        return this;
    }
    
    /**
     * Sets explicit xsd schema repository instance to use for validation.
     * @param schemaRepository
     * @return
     */
    public ReceiveMessageActionDefinition xsdSchemaRepository(String schemaRepository) {
        initializeValidationContext();
        
        if (validationContext instanceof XmlMessageValidationContext) {
            ((XmlMessageValidationContext)validationContext).setSchemaRepository(schemaRepository);
        } else {
            throw new CitrusRuntimeException("Unable to xsd schema repository on non XML message type");
        }
        
        return this;
    }
    
    /**
     * Adds explicit namespace declaration for later path validation expressions.
     * @param prefix
     * @param namespaceUri
     * @return
     */
    public ReceiveMessageActionDefinition namespace(String prefix, String namespaceUri) {
        initializeValidationContext();
        initializeXpathVariableExtractor();
        
        xpathExtractor.getNamespaces().put(prefix, namespaceUri);
        
        if (validationContext instanceof XmlMessageValidationContext) {
            ((XmlMessageValidationContext)validationContext).getNamespaces().put(prefix, namespaceUri);
        } else {
            throw new CitrusRuntimeException("Unable to set namespace declaration on non XML message type");
        }
        
        return this;
    }
    
    /**
     * Sets default namespace declarations on this action definition.
     * @param namespaceMappings
     * @return
     */
    public ReceiveMessageActionDefinition namespaces(Map<String, String> namespaceMappings) {
        initializeValidationContext();
        initializeXpathVariableExtractor();
        
        xpathExtractor.getNamespaces().putAll(namespaceMappings);
        
        if (validationContext instanceof XmlMessageValidationContext) {
            ((XmlMessageValidationContext)validationContext).getNamespaces().putAll(namespaceMappings);
        } else {
            throw new CitrusRuntimeException("Unable to set namespace declaration on non XML message type");
        }
        
        return this;
    }
    
    /**
     * Sets message selector string.
     * @param messageSelector
     * @return
     */
    public ReceiveMessageActionDefinition selector(String messageSelector) {
        action.setMessageSelectorString(messageSelector);
        
        return this;
    }
    
    /**
     * Sets message selector elements.
     * @param messageSelector
     * @return
     */
    public ReceiveMessageActionDefinition selector(Map<String, String> messageSelector) {
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
     * Sets explicit message validator by name.
     * @param validatorName
     * @return
     */
    @SuppressWarnings("unchecked")
    public ReceiveMessageActionDefinition validator(String validatorName) {
        Assert.notNull(applicationContext, "Citrus application context is not initialized!");
        
        MessageValidator<? extends ValidationContext> validator = applicationContext.getBean(validatorName, MessageValidator.class);
        
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
     * Extract message element via XPath from message payload as new test variable.
     * @param xpath
     * @param variable
     * @return
     */
    public ReceiveMessageActionDefinition extractFromPayload(String xpath, String variable) {
        initializeXpathVariableExtractor();
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
        callback.setApplicationContext(applicationContext);
        action.setValidationCallback(callback);
        
        return this;
    }
    
    /**
     * Enable SOAP specific properties on this receiving message action.
     * @return
     */
    public ReceiveSoapMessageActionDefinition soap() {
        ReceiveSoapMessageAction receiveSoapMessageAction = new ReceiveSoapMessageAction();
        
        receiveSoapMessageAction.setActor(action.getActor());
        receiveSoapMessageAction.setDescription(action.getDescription());
        receiveSoapMessageAction.setMessageReceiver(action.getMessageReceiver());
        receiveSoapMessageAction.setMessageSelector(action.getMessageSelector());
        receiveSoapMessageAction.setMessageSelectorString(action.getMessageSelectorString());
        receiveSoapMessageAction.setMessageType(action.getMessageType());
        receiveSoapMessageAction.setReceiveTimeout(action.getReceiveTimeout());
        receiveSoapMessageAction.setValidationCallback(action.getValidationCallback());
        receiveSoapMessageAction.setValidationContexts(action.getValidationContexts());
        receiveSoapMessageAction.setValidator(action.getValidator());
        receiveSoapMessageAction.setVariableExtractors(action.getVariableExtractors());
        
        positionHandle.switchTestAction(receiveSoapMessageAction);
        
        return new ReceiveSoapMessageActionDefinition(receiveSoapMessageAction, applicationContext);
    }

}
