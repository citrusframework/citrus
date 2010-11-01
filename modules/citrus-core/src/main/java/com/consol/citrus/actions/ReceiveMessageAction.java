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
import java.util.Map.Entry;

import javax.xml.namespace.NamespaceContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.integration.core.Message;
import org.springframework.integration.message.MessageBuilder;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.xml.namespace.SimpleNamespaceContext;

import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.message.MessageReceiver;
import com.consol.citrus.message.MessageSelectorBuilder;
import com.consol.citrus.util.*;
import com.consol.citrus.util.XMLUtils;
import com.consol.citrus.validation.MessageValidator;
import com.consol.citrus.validation.context.ValidationContext;
import com.consol.citrus.validation.script.ScriptValidationAware;
import com.consol.citrus.validation.xml.XmlMessageValidationAware;

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
    /** Map extracting message elements to variables */
    private Map<String, String> extractMessageElements = new HashMap<String, String>();

    /** Map extracting header values to variables */
    private Map<String, String> extractHeaderValues = new HashMap<String, String>();

    /** Build message selector with name value pairs */
    private Map<String, String> messageSelector = new HashMap<String, String>();

    /** Select messages via message selector string */
    private String messageSelectorString;

    /** Message receiver */
    private MessageReceiver messageReceiver;
    
    /** Receive timeout */
    private long receiveTimeout = 0L;

    /** Control message payload defined in external file resource */
    private Resource messageResource;

    /** Inline control message payload */
    private String messageData;
    
    /** Control message payload defined in external file resource as Groovy MarkupBuilder script */
    private Resource scriptResource;

    /** Inline control message payload as Groovy MarkupBuilder script */
    private String scriptData;
    
    /** Overwrites message elements before validating (via XPath expressions) */
    private Map<String, String> messageElements = new HashMap<String, String>();

    /** MessageValidator responsible for message validation */
    private List<MessageValidator<ValidationContext>> validators;
    
    /** Validation script for message validation */ 
    private String validationScript;
    
    /** Validation script resource */
    private Resource validationScriptResource;
    
    /** XML namespace declaration used for XPath expression evaluation */
    private Map<String, String> namespaces = new HashMap<String, String>();

    /** XPath validation expressions */
    private Map<String, String> pathValidationExpressions;

    /** Ignored xml elements via XPath */
    private Set<String> ignoreExpressions;

    /** Control namespaces for message validation */
    private Map<String, String> controlNamespaces;

    /** The control headers expected for this message */
    private Map<String, Object> controlMessageHeaders = new HashMap<String, Object>();
    
    /** Mark schema validation enabled */
    private boolean schemaValidation = false;
    
    /** The expected control message */
    private Message<?> controlMessage;
    
    /** Namespace context */
    private NamespaceContext namespaceContext;

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
                throw new CitrusRuntimeException("Received message is null!");
            }

            //save variables from header values
            context.createVariablesFromHeaderValues(extractHeaderValues, receivedMessage.getHeaders());

            namespaceContext = buildNamespaceContext(receivedMessage);
            //save variables from message payload
            context.createVariablesFromMessageValues(extractMessageElements, receivedMessage, namespaceContext);
            
            //check if empty message was expected
            if (receivedMessage.getPayload() == null || receivedMessage.getPayload().toString().length() == 0) {
                if (messageResource == null && (messageData == null || messageData.length() == 0) &&
                    scriptResource == null && (scriptData == null || scriptData.length() == 0)) {
                    log.info("Received message body is empty as expected - therefore no message validation");
                    return;
                } else {
                    throw new CitrusRuntimeException("Validation error: Received message body is empty");
                }
            }

            buildControlMessage(context);

            //validate message
            validateMessage(receivedMessage, context);
        } catch (ParseException e) {
            throw new CitrusRuntimeException(e);
        } catch (IOException e) {
            throw new CitrusRuntimeException(e);
        }
    }

    /**
     * Construct a basic namespace context from the received message.
     * @param receivedMessage the actual message received.
     * @return the namespace context.
     */
    private NamespaceContext buildNamespaceContext(Message<?> receivedMessage) {
        //set namespaces to validate
        SimpleNamespaceContext simpleNamespaceContext = new SimpleNamespaceContext();
        Map<String, String> dynamicBindings = XMLUtils.lookupNamespaces(receivedMessage.getPayload().toString());
        if(!namespaces.isEmpty()) {
            //dynamic binding of namespaces declarations in root element of received message
            for (Entry<String, String> binding : dynamicBindings.entrySet()) {
                //only bind namespace that is not present in explicit namespace bindings
                if(!namespaces.containsValue(binding.getValue())) {
                    simpleNamespaceContext.bindNamespaceUri(binding.getKey(), binding.getValue());
                }
            }
            //add explicit namespace bindings
            simpleNamespaceContext.setBindings(namespaces);
        } else {
            simpleNamespaceContext.setBindings(dynamicBindings);
        }
        
        return simpleNamespaceContext;
    }

    /**
     * Constructs a control message with message content and message headers.
     */
    private void buildControlMessage(TestContext context) throws ParseException, IOException {
       //construct control message payload
        String messagePayload = "";
        if (messageResource != null) {
            messagePayload = context.replaceDynamicContentInString(FileUtils.readToString(messageResource));
        } else if (messageData != null){
            messagePayload = context.replaceDynamicContentInString(messageData);
        } else if (scriptResource != null){
            messagePayload = GroovyUtils.buildMarkupBuilderScript(context.replaceDynamicContentInString(FileUtils.readToString(scriptResource)));
        } else if (scriptData != null){
            messagePayload = GroovyUtils.buildMarkupBuilderScript(context.replaceDynamicContentInString(scriptData));
        }

        if (StringUtils.hasText(messagePayload)) {
            // TODO: Here add message manipulators
            messagePayload = context.replaceMessageValues(messageElements, messagePayload);
        }
        
        controlMessage = MessageBuilder.withPayload(messagePayload).copyHeaders(controlMessageHeaders).build();
    }

    /**
     * Override this message if you want to add additional message validation
     * @param receivedMessage
     */
    protected void validateMessage(Message<?> receivedMessage, TestContext context) throws ParseException, IOException {
        for (MessageValidator<ValidationContext> messageValidator : validators) {
            ValidationContext validationContext = messageValidator.getValidationContextBuilder().buildValidationContext(this, context);
            messageValidator.validateMessage(receivedMessage, context, validationContext);
        }
    }

    /**
     * Setter for XPath validation expressions.
     * @param validationExpressions
     */
    public void setPathValidationExpressions(Map<String, String> validationExpressions) {
        this.pathValidationExpressions = validationExpressions;
    }
    
    /**
     * Getter for XPath validation expressions.
     * return the XPath validationExpressions
     */
    public Map<String, String> getPathValidationExpressions() {
        return pathValidationExpressions;
    }

    /**
     * Setter for ignored message elements.
     * @param ignoreExpressions
     */
    public void setIgnoreExpressions(Set<String> ignoreExpressions) {
        this.ignoreExpressions = ignoreExpressions;
    }
    
    /**
     * Get the ignored message elements specified via XPath. 
     * @return the ignoreExpressions
     */
    public Set<String> getIgnoreExpressions() {
        return ignoreExpressions;
    }

    /**
     * Setter for control namespaces that must be present in the XML message.
     * @param controlNamespaces the controlNamespaces to set
     */
    public void setControlNamespaces(Map<String, String> controlNamespaces) {
        this.controlNamespaces = controlNamespaces;
    }
    
    /**
     * Get the control namesapces.
     * @return the control namespaces
     */
    public Map<String, String> getControlNamespaces() {
        return controlNamespaces;
    }

    /**
     * Extract variables from header.
     * @param getHeaderValues the getHeaderValues to set
     */
    public void setExtractHeaderValues(Map<String, String> extractHeaderValues) {
        this.extractHeaderValues = extractHeaderValues;
    }

    /**
     * Extract variables from message payload.
     * @param extractMessageElements the extractMessageElements to set
     */
    public void setExtractMessageElements(Map<String, String> extractMessageElements) {
        this.extractMessageElements = extractMessageElements;
    }

    /**
     * Set expected control headers.
     * @param controlMessageHeaders the controlMessageHeaders to set
     */
    public void setControlMessageHeaders(Map<String, Object> controlMessageHeaders) {
        this.controlMessageHeaders = controlMessageHeaders;
    }
    
    /**
     * Set message payload as inline data.
     * @param messageData the messageData to set
     */
    public void setMessageData(String messageData) {
        this.messageData = messageData;
    }

    /**
     * Message payload as external file resource.
     * @param messageResource the messageResource to set
     */
    public void setMessageResource(Resource messageResource) {
        this.messageResource = messageResource;
    }
    
    /**
     * Set message payload data as inline Groovy MarkupBuilder script.
     * @param scriptData the scriptData to set
     */
    public void setScriptData(String scriptData) {
        this.scriptData = scriptData;
    }

    /**
     * Message payload as external Groovy MarkupBuilder script file resource.
     * @param scriptResource the scriptResource to set
     */
    public void setScriptResource(Resource scriptResource) {
        this.scriptResource = scriptResource;
    }

    /**
     * Check if header values for extraction are present.
     * @return boolean flag to mark existence
     */
    public boolean hasExtractHeaderValues() {
        return (this.extractHeaderValues != null && !this.extractHeaderValues.isEmpty());
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
     * Set the list of message validators.
     * @param validators the list of message validators to set
     */
    public void setValidators(List<MessageValidator<ValidationContext>> validators) {
        this.validators = validators;
    }
    
    /**
     * Set single message validator.
     * @param validator the message validator to set
     */
    public void setValidator(MessageValidator<ValidationContext> validator) {
        if (validators == null) {
            List<MessageValidator<ValidationContext>> validatorList = new ArrayList<MessageValidator<ValidationContext>>();
            validatorList.add(validator);
            this.validators = validatorList;
        } else {
            this.validators.add(validator);
        }
    }
    
    /**
     * Set the validation-script.
     * @param validationScript the validationScript to set
     */
    public void setValidationScript(String validationScript){
    	this.validationScript = validationScript;
    }

	/**
	 * Set the validation-script as resource
	 * @param validationScriptResource the validationScriptResource to set
	 */
	public void setValidationScriptResource(Resource validationScriptResource) {
		this.validationScriptResource = validationScriptResource;
	}
    
    /**
     * List of expected namespaces.
     * @param namespaces the namespaces to set
     */
    public void setNamespaces(Map<String, String> namespaces) {
        this.namespaces = namespaces;
    }

    /**
     * Get expected namespaces.
     * @return the namespaces
     */
    public Map<String, String> getNamespaces() {
        return namespaces;
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
     * Enable/Disable schema validation.
     * @param enableSchemaValidation flag to enable/disable schema validation
     */
    public void setSchemaValidation(boolean schemaValidation) {
        this.schemaValidation = schemaValidation;
    }

    /**
     * Check schema validation enabled.
     * @return the schemaValidation
     */
    public boolean isSchemaValidation() {
        return schemaValidation;
    }
    
    /**
     * Set the receive timeout.
     * @param receiveTimeout the receiveTimeout to set
     */
    public void setReceiveTimeout(long receiveTimeout) {
        this.receiveTimeout = receiveTimeout;
    }

    /**
     * Set message elements to overwrite before validation.
     * @param messageElements the messageElements to set
     */
    public void setMessageElements(Map<String, String> messageElements) {
        this.messageElements = messageElements;
    }

    /**
     * Gets the validation script.
     */
    public String getValidationScript() {
        return validationScript;
    }

    /**
     * Gets the validation script resource.
     */
    public Resource getValidationScriptResource() {
        return validationScriptResource;
    }

    /**
     * Get the constructed control message.
     * @return the control message needed for validation.
     */
    public Message<?> getControlMessage() {
        return controlMessage;
    }

    /**
     * @return the namespaceContext
     */
    public NamespaceContext getNamespaceContext() {
        return namespaceContext;
    }
}
