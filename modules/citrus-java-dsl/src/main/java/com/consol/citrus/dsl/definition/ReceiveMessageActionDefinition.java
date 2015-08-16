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

import com.consol.citrus.CitrusConstants;
import com.consol.citrus.actions.ReceiveMessageAction;
import com.consol.citrus.dsl.util.PositionHandle;
import com.consol.citrus.endpoint.Endpoint;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.message.Message;
import com.consol.citrus.message.MessageType;
import com.consol.citrus.util.FileUtils;
import com.consol.citrus.validation.ControlMessageValidationContext;
import com.consol.citrus.validation.MessageValidator;
import com.consol.citrus.validation.builder.*;
import com.consol.citrus.validation.callback.ValidationCallback;
import com.consol.citrus.validation.context.ValidationContext;
import com.consol.citrus.validation.json.*;
import com.consol.citrus.validation.script.ScriptValidationContext;
import com.consol.citrus.validation.xml.*;
import com.consol.citrus.variable.MessageHeaderVariableExtractor;
import com.consol.citrus.ws.actions.ReceiveSoapMessageAction;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.oxm.Marshaller;
import org.springframework.oxm.XmlMappingException;
import org.springframework.util.Assert;
import org.springframework.xml.transform.StringResult;

import java.io.IOException;
import java.util.Map;

/**
 * Receive message action definition offers configuration methods for a receive test action. Build options 
 * include construction of control message payload and headers as well as value extraction.
 * 
 * @author Christoph Deppisch
 * @deprecated since 2.3 in favor of using {@link com.consol.citrus.dsl.builder.ReceiveMessageBuilder}
 */
public class ReceiveMessageActionDefinition<A extends ReceiveMessageAction, T extends ReceiveMessageActionDefinition> extends AbstractActionDefinition<A> {

    /** Self reference for generics support */
    private final T self;

    /** Message type for this action definition */
    private MessageType messageType = MessageType.valueOf(CitrusConstants.DEFAULT_MESSAGE_TYPE);

    /** Validation context used in this action definition */
    private ControlMessageValidationContext validationContext;

    /** JSON validation context used in this action builder */
    private JsonPathMessageValidationContext jsonPathValidationContext;

    /** Script validation context used in this action builder */
    private ScriptValidationContext scriptValidationContext;

    /** Variable extractors filled within this action definition */
    private MessageHeaderVariableExtractor headerExtractor;
    private XpathPayloadVariableExtractor xpathExtractor;
    private JsonPathVariableExtractor jsonPathExtractor;

    /** Basic application context */
    private ApplicationContext applicationContext;

    /** Handle for test action position in test case sequence use when switching to SOAP specific definition */
    private PositionHandle positionHandle;

    /**
     * Default constructor using test action, basic application context and position handle.
     * @param action
     */
    public ReceiveMessageActionDefinition(A action) {
        super(action);
        this.self = (T) this;
    }

    /**
     * Default constructor.
     */
    public ReceiveMessageActionDefinition() {
        this((A) new ReceiveMessageAction());
    }

    /**
     * Sets the message endpoint to receive messages from.
     * @param messageEndpoint
     * @return
     */
    public ReceiveMessageActionDefinition endpoint(Endpoint messageEndpoint) {
        action.setEndpoint(messageEndpoint);
        return this;
    }

    /**
     * Sets the message endpoint uri to receive messages from.
     * @param messageEndpointUri
     * @return
     */
    public ReceiveMessageActionDefinition endpoint(String messageEndpointUri) {
        action.setEndpointUri(messageEndpointUri);
        return this;
    }

    /**
     * Sets the position handle as internal marker where in test action sequence this action was set.
     * @param positionHandle
     * @return
     */
    public ReceiveMessageActionDefinition position(PositionHandle positionHandle) {
        this.positionHandle = positionHandle;
        return this;
    }

    /**
     * Adds a custom timeout to this message receiving action.
     * @param receiveTimeout
     * @return
     */
    public T timeout(long receiveTimeout) {
        action.setReceiveTimeout(receiveTimeout);
        return self;
    }

    /**
     * Expect a control message in this receive action.
     * @param controlMessage
     * @return
     */
    public T message(Message controlMessage) {
        if (validationContext != null) {
            throw new CitrusRuntimeException("Unable to set control message object when header and/or payload was set before");
        }

        getValidationContext().setControlMessage(controlMessage);

        return self;
    }

    /**
     * Expect this message payload data in received message.
     * @param payload
     * @return
     */
    public T payload(String payload) {
        getPayloadTemplateMessageBuilder().setPayloadData(payload);
        return self;
    }

    /**
     * Expect this message payload data in received message.
     * @param payloadResource
     * @return
     */
    public T payload(Resource payloadResource) {
        try {
            getPayloadTemplateMessageBuilder().setPayloadData(FileUtils.readToString(payloadResource));
        } catch (IOException e) {
            throw new CitrusRuntimeException("Failed to read payload resource", e);
        }

        return self;
    }

    /**
     * Expect this message payload as model object which is marshalled to a character sequence
     * using the default object to xml mapper before validation is performed.
     * @param payload
     * @param marshaller
     * @return
     */
    public T payload(Object payload, Marshaller marshaller) {
        StringResult result = new StringResult();

        try {
            marshaller.marshal(payload, result);
        } catch (XmlMappingException e) {
            throw new CitrusRuntimeException("Failed to marshal object graph for message payload", e);
        } catch (IOException e) {
            throw new CitrusRuntimeException("Failed to marshal object graph for message payload", e);
        }

        getPayloadTemplateMessageBuilder().setPayloadData(result.toString());

        return self;
    }

    /**
     * Expect this message payload as model object which is marshalled to a character sequence using the default object to xml mapper that
     * is available in Spring bean application context.
     *
     * @param payload
     * @return
     */
    public T payloadModel(Object payload) {
        Assert.notNull(applicationContext, "Citrus application context is not initialized!");
        return payload(payload, applicationContext.getBean(Marshaller.class));
    }

    /**
     * Expect this message payload as model object which is marshalled to a character sequence using the given object to xml mapper that
     * is accessed by its bean name in Spring bean application context.
     *
     * @param payload
     * @param marshallerName
     * @return
     */
    public T payload(Object payload, String marshallerName) {
        Assert.notNull(applicationContext, "Citrus application context is not initialized!");
        return payload(payload, applicationContext.getBean(marshallerName, Marshaller.class));
    }

    /**
     * Expect this message header entry in received message.
     * @param name
     * @param value
     * @return
     */
    public T header(String name, Object value) {
        getMessageContentBuilder().getMessageHeaders().put(name, value);
        return self;
    }

    /**
     * Expect this message header data in received message. Message header data is used in
     * SOAP messages as XML fragment for instance.
     * @param data
     * @return
     */
    public T header(String data) {
        getMessageContentBuilder().getHeaderData().add(data);
        return self;
    }

    /**
     * Expect this message header data in received message from file resource. Message header data is used in
     * SOAP messages as XML fragment for instance.
     * @param resource
     * @return
     */
    public T header(Resource resource) {
        try {
            getMessageContentBuilder().getHeaderData().add(FileUtils.readToString(resource));
        } catch (IOException e) {
            throw new CitrusRuntimeException("Failed to read header resource", e);
        }

        return self;
    }

    /**
     * Adds script validation.
     * @param validationScript
     * @return
     */
    public T validateScript(String validationScript) {
        getScriptValidationContext().setValidationScript(validationScript);

        return self;
    }

    /**
     * Adds script validation by file resource.
     * @param scriptResource
     * @return
     */
    public T validateScript(Resource scriptResource) {
        try {
            getScriptValidationContext().setValidationScriptResourcePath(scriptResource.getFile().getAbsolutePath());
        } catch (IOException e) {
            throw new CitrusRuntimeException("Failed to read script resource file", e);
        }

        return self;
    }

    /**
     * Adds custom validation script type.
     * @param type
     * @return
     */
    public T validateScriptType(String type) {
        getScriptValidationContext().setScriptType(type);

        return self;
    }

    /**
     * Sets a explicit message type for this receive action.
     * @param messageType
     * @return
     */
    public T messageType(MessageType messageType) {
        this.messageType = messageType;
        action.setMessageType(messageType.toString());
        return self;
    }

    /**
     * Sets schema validation enabled/disabled for this message.
     * @param enabled
     * @return
     */
    public T schemaValidation(boolean enabled) {
        getXmlValidationContext().setSchemaValidation(enabled);
        return self;
    }

    /**
     * Validates XML namespace with prefix and uri.
     * @param prefix
     * @param namespaceUri
     * @return
     */
    public T validateNamespace(String prefix, String namespaceUri) {
        getXmlValidationContext().getControlNamespaces().put(prefix, namespaceUri);
        return self;
    }

    /**
     * Adds message element validation.
     * @param path
     * @param controlValue
     * @return
     */
    public T validate(String path, String controlValue) {
        if (JsonPathMessageValidationContext.isJsonPathExpression(path)) {
            if (!messageType.equals(MessageType.JSON)) {
                throw new CitrusRuntimeException(String.format("Failed to set JSONPath validation expression on message type '%s' - please use JSON message type", messageType));
            }

            getJsonPathValidationContext().getJsonPathExpressions().put(path, controlValue);
        } else {
            getXPathValidationContext().getXpathExpressions().put(path, controlValue);
        }

        return self;
    }

    /**
     * Adds ignore path expression for message element.
     * @param path
     * @return
     */
    public T ignore(String path) {
        if (messageType.equals(MessageType.XML)) {
        getXmlValidationContext().getIgnoreExpressions().add(path);
        } else if (messageType.equals(MessageType.JSON)) {
            getJsonValidationContext().getIgnoreExpressions().add(path);
        }
        return self;
    }

    /**
     * Adds XPath message element validation.
     * @param xPathExpression
     * @param controlValue
     * @return
     */
    public T xpath(String xPathExpression, String controlValue) {
        validate(xPathExpression, controlValue);
        return self;
    }

    /**
     * Sets explicit schema instance name to use for schema validation.
     * @param schemaName
     * @return
     */
    public T xsd(String schemaName) {
        getXmlValidationContext().setSchema(schemaName);
        return self;
    }

    /**
     * Sets explicit xsd schema repository instance to use for validation.
     * @param schemaRepository
     * @return
     */
    public T xsdSchemaRepository(String schemaRepository) {
        getXmlValidationContext().setSchemaRepository(schemaRepository);
        return self;
    }

    /**
     * Adds explicit namespace declaration for later path validation expressions.
     * @param prefix
     * @param namespaceUri
     * @return
     */
    public T namespace(String prefix, String namespaceUri) {
        getXpathVariableExtractor().getNamespaces().put(prefix, namespaceUri);
        getXmlValidationContext().getNamespaces().put(prefix, namespaceUri);
        return self;
    }

    /**
     * Sets default namespace declarations on this action definition.
     * @param namespaceMappings
     * @return
     */
    public T namespaces(Map<String, String> namespaceMappings) {
        getXpathVariableExtractor().getNamespaces().putAll(namespaceMappings);

        getXmlValidationContext().getNamespaces().putAll(namespaceMappings);
        return self;
    }

    /**
     * Sets message selector string.
     * @param messageSelector
     * @return
     */
    public T selector(String messageSelector) {
        action.setMessageSelectorString(messageSelector);

        return self;
    }

    /**
     * Sets message selector elements.
     * @param messageSelector
     * @return
     */
    public T selector(Map<String, Object> messageSelector) {
        action.setMessageSelector(messageSelector);

        return self;
    }

    /**
     * Sets explicit message validator for this receive action.
     * @param validator
     * @return
     */
    public T validator(MessageValidator<? extends ValidationContext> validator) {
        action.setValidator(validator);
        return self;
    }

    /**
     * Sets explicit message validator by name.
     * @param validatorName
     * @return
     */
    @SuppressWarnings("unchecked")
    public T validator(String validatorName) {
        Assert.notNull(applicationContext, "Citrus application context is not initialized!");
        MessageValidator<? extends ValidationContext> validator = applicationContext.getBean(validatorName, MessageValidator.class);

        action.setValidator(validator);
        return self;
    }

    /**
     * Extract message header entry as variable.
     * @param headerName
     * @param variable
     * @return
     */
    public T extractFromHeader(String headerName, String variable) {
        if (headerExtractor == null) {
            headerExtractor = new MessageHeaderVariableExtractor();

            action.getVariableExtractors().add(headerExtractor);
        }

        headerExtractor.getHeaderMappings().put(headerName, variable);
        return self;
    }

    /**
     * Extract message element via XPath or JSONPath from message payload as new test variable.
     * @param path
     * @param variable
     * @return
     */
    public T extractFromPayload(String path, String variable) {
        if (JsonPathMessageValidationContext.isJsonPathExpression(path)) {
            getJsonPathVariableExtractor().getJsonPathExpressions().put(path, variable);
        } else {
            getXpathVariableExtractor().getXpathExpressions().put(path, variable);
        }
        return self;
    }

    /**
     * Adds validation callback to the receive action for validating
     * the received message with Java code.
     * @param callback
     * @return
     */
    public T validationCallback(ValidationCallback callback) {
        callback.setApplicationContext(applicationContext);
        action.setValidationCallback(callback);
        return self;
    }

    /**
     * Sets the Spring bean application context.
     * @param applicationContext
     */
    public T withApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
        return self;
    }

    /**
     * Enable SOAP specific properties on this receiving message action.
     * @return
     */
    public ReceiveSoapMessageActionDefinition soap() {
        ReceiveSoapMessageAction receiveSoapMessageAction = new ReceiveSoapMessageAction();

        receiveSoapMessageAction.setActor(action.getActor());
        receiveSoapMessageAction.setDescription(action.getDescription());
        receiveSoapMessageAction.setEndpoint(action.getEndpoint());
        receiveSoapMessageAction.setEndpointUri(action.getEndpointUri());
        receiveSoapMessageAction.setMessageSelector(action.getMessageSelector());
        receiveSoapMessageAction.setMessageSelectorString(action.getMessageSelectorString());
        receiveSoapMessageAction.setMessageType(action.getMessageType());
        receiveSoapMessageAction.setReceiveTimeout(action.getReceiveTimeout());
        receiveSoapMessageAction.setValidationCallback(action.getValidationCallback());
        receiveSoapMessageAction.setValidationContexts(action.getValidationContexts());
        receiveSoapMessageAction.setValidator(action.getValidator());
        receiveSoapMessageAction.setVariableExtractors(action.getVariableExtractors());

        if (positionHandle != null) {
            positionHandle.switchTestAction(receiveSoapMessageAction);
        } else {
            action = (A) receiveSoapMessageAction;
        }

        ReceiveSoapMessageActionDefinition soapMessageActionDefinition = new ReceiveSoapMessageActionDefinition(receiveSoapMessageAction);
        soapMessageActionDefinition.withApplicationContext(applicationContext);
        soapMessageActionDefinition.setMessageType(messageType);
        soapMessageActionDefinition.setValidationContext(validationContext);
        soapMessageActionDefinition.setScriptValidationContext(scriptValidationContext);
        soapMessageActionDefinition.setJsonPathValidationContext(jsonPathValidationContext);
        soapMessageActionDefinition.setHeaderExtractor(headerExtractor);
        soapMessageActionDefinition.setXpathExtractor(xpathExtractor);
        soapMessageActionDefinition.setJsonPathExtractor(jsonPathExtractor);

        return soapMessageActionDefinition;
    }

    /**
     * Enable HTTP specific properties on this receiving message action.
     * @return
     */
    public ReceiveHttpMessageActionDefinition http() {
        ReceiveHttpMessageActionDefinition httpMessageActionDefinition = new ReceiveHttpMessageActionDefinition(action);
        httpMessageActionDefinition.position(positionHandle);
        httpMessageActionDefinition.withApplicationContext(applicationContext);
        httpMessageActionDefinition.setMessageType(messageType);
        httpMessageActionDefinition.setValidationContext(validationContext);
        httpMessageActionDefinition.setScriptValidationContext(scriptValidationContext);
        httpMessageActionDefinition.setJsonPathValidationContext(jsonPathValidationContext);
        httpMessageActionDefinition.setHeaderExtractor(headerExtractor);
        httpMessageActionDefinition.setXpathExtractor(xpathExtractor);
        httpMessageActionDefinition.setJsonPathExtractor(jsonPathExtractor);

        return httpMessageActionDefinition;
    }

    /**
     * Gets the message builder on the validation context. Constructs message content builder if necessary.
     * @return
     */
    protected AbstractMessageContentBuilder getMessageContentBuilder() {
        if (getValidationContext().getMessageBuilder() instanceof AbstractMessageContentBuilder) {
            return (AbstractMessageContentBuilder) getValidationContext().getMessageBuilder();
        } else {
            PayloadTemplateMessageBuilder messageBuilder = new PayloadTemplateMessageBuilder();
            getValidationContext().setMessageBuilder(messageBuilder);
            return messageBuilder;
        }
    }

    /**
     * Forces a payload template message builder.
     * @return
     */
    protected PayloadTemplateMessageBuilder getPayloadTemplateMessageBuilder() {
        MessageContentBuilder messageContentBuilder = getMessageContentBuilder();

        if (messageContentBuilder instanceof PayloadTemplateMessageBuilder) {
            return (PayloadTemplateMessageBuilder) messageContentBuilder;
        } else {
            PayloadTemplateMessageBuilder messageBuilder = new PayloadTemplateMessageBuilder();
            getValidationContext().setMessageBuilder(messageBuilder);
            return messageBuilder;
        }
    }

    /**
     * Creates new validation context according to message type.
     */
    private ControlMessageValidationContext getValidationContext() {
        if (validationContext == null) {
            if (messageType.equals(MessageType.XML)) {
                validationContext = new XmlMessageValidationContext();
            } else if (messageType.equals(MessageType.JSON)) {
                validationContext = new JsonMessageValidationContext();
            } else {
                validationContext = new ControlMessageValidationContext(messageType.toString());
            }

            action.getValidationContexts().add(validationContext);
        }

        return validationContext;
    }

    /**
     * Creates new variable extractor and adds it to test action.
     */
    private XpathPayloadVariableExtractor getXpathVariableExtractor() {
        if (xpathExtractor == null) {
            xpathExtractor = new XpathPayloadVariableExtractor();

            action.getVariableExtractors().add(xpathExtractor);
        }

        return xpathExtractor;
    }

    /**
     * Creates new variable extractor and adds it to test action.
     */
    private JsonPathVariableExtractor getJsonPathVariableExtractor() {
        if (jsonPathExtractor == null) {
            jsonPathExtractor = new JsonPathVariableExtractor();

            action.getVariableExtractors().add(jsonPathExtractor);
        }

        return jsonPathExtractor;
    }

    /**
     * Gets the validation context as XML validation context an raises exception if existing validation context is
     * not a XML validation context.
     * @return
     */
    private XmlMessageValidationContext getXmlValidationContext() {
        if (validationContext == null) {
            validationContext = new XmlMessageValidationContext();

            action.getValidationContexts().add(validationContext);
        }

        if (validationContext instanceof XmlMessageValidationContext) {
            return ((XmlMessageValidationContext)validationContext);
        } else {
            throw new CitrusRuntimeException("Unable to set XML property on validation context type " + validationContext);
        }
    }

    /**
     * Gets the validation context as XML validation context an raises exception if existing validation context is
     * not a XML validation context.
     * @return
     */
    private JsonMessageValidationContext getJsonValidationContext() {
        if (validationContext == null) {
            validationContext = new JsonMessageValidationContext();

            action.getValidationContexts().add(validationContext);
        }

        if (validationContext instanceof JsonMessageValidationContext) {
            return ((JsonMessageValidationContext)validationContext);
        } else {
            throw new CitrusRuntimeException("Unable to set JSON property on validation context type " + validationContext);
        }
    }

    /**
     * Gets the validation context as XML validation context an raises exception if existing validation context is
     * not a XML validation context.
     * @return
     */
    private XpathMessageValidationContext getXPathValidationContext() {
        if (validationContext == null) {
            validationContext = new XmlMessageValidationContext();

            action.getValidationContexts().add(validationContext);
        }

        if (validationContext instanceof XpathMessageValidationContext) {
            return ((XpathMessageValidationContext)validationContext);
        } else if (validationContext instanceof XmlMessageValidationContext) {
            XpathMessageValidationContext xPathContext = new XpathMessageValidationContext();
            xPathContext.setMessageBuilder(validationContext.getMessageBuilder());
            xPathContext.setNamespaces(((XmlMessageValidationContext) validationContext).getNamespaces());
            xPathContext.setControlNamespaces(((XmlMessageValidationContext) validationContext).getControlNamespaces());
            xPathContext.setIgnoreExpressions(((XmlMessageValidationContext) validationContext).getIgnoreExpressions());
            xPathContext.setSchema(((XmlMessageValidationContext) validationContext).getSchema());
            xPathContext.setSchemaRepository(((XmlMessageValidationContext) validationContext).getSchemaRepository());
            xPathContext.setSchemaValidation(((XmlMessageValidationContext) validationContext).isSchemaValidationEnabled());
            xPathContext.setDTDResource(((XmlMessageValidationContext) validationContext).getDTDResource());

            action.getValidationContexts().remove(validationContext);
            action.getValidationContexts().add(xPathContext);

            validationContext = xPathContext;
            return xPathContext;
        } else {
            throw new CitrusRuntimeException("Unable to set XML property on validation context type " + validationContext);
        }
    }

    /**
     * Creates new script validation context if not done before and gets the script validation context.
     */
    private ScriptValidationContext getScriptValidationContext() {
        if (scriptValidationContext == null) {
            scriptValidationContext = new ScriptValidationContext(messageType.toString());

            action.getValidationContexts().add(scriptValidationContext);
        }

        return scriptValidationContext;
    }

    /**
     * Creates new JSONPath validation context if not done before and gets the validation context.
     */
    private JsonPathMessageValidationContext getJsonPathValidationContext() {
        if (jsonPathValidationContext == null) {
            jsonPathValidationContext = new JsonPathMessageValidationContext();

            action.getValidationContexts().add(jsonPathValidationContext);
        }

        return jsonPathValidationContext;
    }

    /**
     * Sets the message type.
     * @param messageType
     */
    protected void setMessageType(MessageType messageType) {
        this.messageType = messageType;
    }

    /**
     * Sets the xpath extractor.
     * @param xpathExtractor
     */
    protected void setXpathExtractor(XpathPayloadVariableExtractor xpathExtractor) {
        this.xpathExtractor = xpathExtractor;
    }

    /**
     * Sets the jsonPath extractor.
     * @param jsonPathExtractor
     */
    protected void setJsonPathExtractor(JsonPathVariableExtractor jsonPathExtractor) {
        this.jsonPathExtractor = jsonPathExtractor;
    }

    /**
     * Sets the header extractor.
     * @param headerExtractor
     */
    protected void setHeaderExtractor(MessageHeaderVariableExtractor headerExtractor) {
        this.headerExtractor = headerExtractor;
    }

    /**
     * Sets the script message validator.
     * @param scriptValidationContext
     */
    protected void setScriptValidationContext(ScriptValidationContext scriptValidationContext) {
        this.scriptValidationContext = scriptValidationContext;
    }

    /**
     * Sets the script message validator.
     * @param jsonPathValidationContext
     */
    protected void setJsonPathValidationContext(JsonPathMessageValidationContext jsonPathValidationContext) {
        this.jsonPathValidationContext = jsonPathValidationContext;
    }

    /**
     * Sets the validation context.
     * @param validationContext
     */
    protected void setValidationContext(ControlMessageValidationContext validationContext) {
        this.validationContext = validationContext;
    }
}
