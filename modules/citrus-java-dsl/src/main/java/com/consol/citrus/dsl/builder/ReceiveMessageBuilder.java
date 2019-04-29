/*
 * Copyright 2006-2019 the original author or authors.
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

package com.consol.citrus.dsl.builder;

import com.consol.citrus.TestAction;
import com.consol.citrus.actions.ReceiveMessageAction;
import com.consol.citrus.dsl.actions.DelegatingTestAction;
import com.consol.citrus.endpoint.Endpoint;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.message.Message;
import com.consol.citrus.message.MessageType;
import com.consol.citrus.util.FileUtils;
import com.consol.citrus.validation.HeaderValidator;
import com.consol.citrus.validation.MessageValidator;
import com.consol.citrus.validation.builder.AbstractMessageContentBuilder;
import com.consol.citrus.validation.builder.MessageContentBuilder;
import com.consol.citrus.validation.builder.PayloadTemplateMessageBuilder;
import com.consol.citrus.validation.builder.StaticMessageContentBuilder;
import com.consol.citrus.validation.callback.ValidationCallback;
import com.consol.citrus.validation.context.HeaderValidationContext;
import com.consol.citrus.validation.context.ValidationContext;
import com.consol.citrus.validation.json.JsonMessageValidationContext;
import com.consol.citrus.validation.json.JsonPathMessageValidationContext;
import com.consol.citrus.validation.json.JsonPathVariableExtractor;
import com.consol.citrus.validation.script.ScriptValidationContext;
import com.consol.citrus.validation.xml.XmlMessageValidationContext;
import com.consol.citrus.validation.xml.XpathMessageValidationContext;
import com.consol.citrus.validation.xml.XpathPayloadVariableExtractor;
import com.consol.citrus.variable.MessageHeaderVariableExtractor;
import com.consol.citrus.variable.dictionary.DataDictionary;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.io.Resource;
import org.springframework.oxm.Marshaller;
import org.springframework.oxm.XmlMappingException;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.xml.transform.StringResult;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Receive message action builder offers configuration methods for a receive test action. Build options
 * include construction of control message payload and headers as well as value extraction.
 *
 * @since 2.3
 */
public class ReceiveMessageBuilder<A extends ReceiveMessageAction, T extends ReceiveMessageBuilder> extends AbstractTestActionBuilder<DelegatingTestAction<TestAction>> {

    /** Self reference for generics support */
    private final T self;

    /** Message type for this action builder */
    private String messageType;

    /** Validation context used in this action builder */
    private HeaderValidationContext headerValidationContext = new HeaderValidationContext();
    private XmlMessageValidationContext xmlMessageValidationContext = new XmlMessageValidationContext();
    private JsonMessageValidationContext jsonMessageValidationContext = new JsonMessageValidationContext();

    /** JSON validation context used in this action builder */
    private JsonPathMessageValidationContext jsonPathValidationContext;

    /** Script validation context used in this action builder */
    private ScriptValidationContext scriptValidationContext;

    /** Variable extractors filled within this action builder */
    private MessageHeaderVariableExtractor headerExtractor;
    private XpathPayloadVariableExtractor xpathExtractor;
    private JsonPathVariableExtractor jsonPathExtractor;

    /** Basic application context */
    private ApplicationContext applicationContext;

    /**
     * Default constructor using test action, basic application context and position handle.
     * @param action
     */
    public ReceiveMessageBuilder(final A action) {
        this(new DelegatingTestAction<>(action));
    }

    /**
     * Default constructor.
     */
    public ReceiveMessageBuilder() {
        this((A) new ReceiveMessageAction());
    }

    /**
     * Constructor using delegate test action.
     * @param action
     */
    public ReceiveMessageBuilder(final DelegatingTestAction<TestAction> action) {
        super(action);
        this.self = (T) this;
    }

    /**
     * Sets the message endpoint to receive messages from.
     * @param messageEndpoint
     * @return
     */
    public ReceiveMessageBuilder endpoint(final Endpoint messageEndpoint) {
        getAction().setEndpoint(messageEndpoint);
        return this;
    }

    /**
     * Sets the message endpoint uri to receive messages from.
     * @param messageEndpointUri
     * @return
     */
    public ReceiveMessageBuilder endpoint(final String messageEndpointUri) {
        getAction().setEndpointUri(messageEndpointUri);
        return this;
    }

    /**
     * Adds a custom timeout to this message receiving action.
     * @param receiveTimeout
     * @return
     */
    public T timeout(final long receiveTimeout) {
        getAction().setReceiveTimeout(receiveTimeout);
        return self;
    }
    
    /**
     * Expect a control message in this receive action.
     * @param controlMessage
     * @return
     */
    public T message(final Message controlMessage) {
        final StaticMessageContentBuilder staticMessageContentBuilder = StaticMessageContentBuilder.withMessage(controlMessage);
        staticMessageContentBuilder.setMessageHeaders(getMessageContentBuilder().getMessageHeaders());
        getAction().setMessageBuilder(staticMessageContentBuilder);
        return self;
    }

    /**
     * Sets the payload data on the message builder implementation.
     * @param payload
     * @return
     */
    protected void setPayload(final String payload) {
        final MessageContentBuilder messageContentBuilder = getMessageContentBuilder();

        if (messageContentBuilder instanceof PayloadTemplateMessageBuilder) {
            ((PayloadTemplateMessageBuilder) messageContentBuilder).setPayloadData(payload);
        } else if (messageContentBuilder instanceof StaticMessageContentBuilder) {
            ((StaticMessageContentBuilder) messageContentBuilder).getMessage().setPayload(payload);
        } else {
            throw new CitrusRuntimeException("Unable to set payload on message builder type: " + messageContentBuilder.getClass());
        }
    }

    /**
     * Sets the message name.
     * @param name
     * @return
     */
    public T name(final String name) {
        getMessageContentBuilder().setMessageName(name);
        return self;
    }
    
    /**
     * Expect this message payload data in received message.
     * @param payload
     * @return
     */
    public T payload(final String payload) {
        setPayload(payload);
        return self;
    }
    
    /**
     * Expect this message payload data in received message.
     * @param payloadResource
     * @return
     */
    public T payload(final Resource payloadResource) {
        return payload(payloadResource, FileUtils.getDefaultCharset());
    }

    /**
     * Expect this message payload data in received message.
     * @param payloadResource
     * @param charset
     * @return
     */
    public T payload(final Resource payloadResource, final Charset charset) {
        try {
            setPayload(FileUtils.readToString(payloadResource, charset));
        } catch (final IOException e) {
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
    public T payload(final Object payload, final Marshaller marshaller) {
        final StringResult result = new StringResult();

        try {
            marshaller.marshal(payload, result);
        } catch (final XmlMappingException | IOException e) {
            throw new CitrusRuntimeException("Failed to marshal object graph for message payload", e);
        }

        setPayload(result.toString());

        return self;
    }

    /**
     * Expect this message payload as model object which is mapped to a character sequence
     * using the default object to json mapper before validation is performed.
     * @param payload
     * @param objectMapper
     * @return
     */
    public T payload(final Object payload, final ObjectMapper objectMapper) {
        try {
            setPayload(objectMapper.writer().writeValueAsString(payload));
        } catch (final JsonProcessingException e) {
            throw new CitrusRuntimeException("Failed to map object graph for message payload", e);
        }

        return self;
    }

    /**
     * Expect this message payload as model object which is marshalled to a character sequence using the default object to xml mapper that
     * is available in Spring bean application context.
     *
     * @param payload
     * @return
     */
    public T payloadModel(final Object payload) {
        validateApplicationContext();

        if (!CollectionUtils.isEmpty(applicationContext.getBeansOfType(Marshaller.class))) {
            return payload(payload, applicationContext.getBean(Marshaller.class));
        } else if (!CollectionUtils.isEmpty(applicationContext.getBeansOfType(ObjectMapper.class))) {
            return payload(payload, applicationContext.getBean(ObjectMapper.class));
        }

        throw createUnableToFindMapperException();
    }

    /**
     * Expect this message payload as model object which is marshalled to a character sequence using the given object to xml mapper that
     * is accessed by its bean name in Spring bean application context.
     *
     * @param payload
     * @param mapperName
     * @return
     */
    public T payload(final Object payload, final String mapperName) {
        validateApplicationContext();

        if (applicationContext.containsBean(mapperName)) {
            final Object mapper = applicationContext.getBean(mapperName);

            if (Marshaller.class.isAssignableFrom(mapper.getClass())) {
                return payload(payload, (Marshaller) mapper);
            } else if (ObjectMapper.class.isAssignableFrom(mapper.getClass())) {
                return payload(payload, (ObjectMapper) mapper);
            } else {
                throw new CitrusRuntimeException(String.format("Invalid bean type for mapper '%s' expected ObjectMapper or Marshaller but was '%s'", mapperName, mapper.getClass()));
            }
        }

        throw createUnableToFindMapperException();
    }
    
    /**
     * Expect this message header entry in received message.
     * @param name
     * @param value
     * @return
     */
    public T header(final String name, final Object value) {
        getMessageContentBuilder().getMessageHeaders().put(name, value);
        return self;
    }

    /**
     * Expect this message header entries in received message.
     * @param headers
     * @return
     */
    public T headers(final Map<String, Object> headers) {
        getMessageContentBuilder().getMessageHeaders().putAll(headers);
        return self;
    }
    
    /**
     * Expect this message header data in received message. Message header data is used in 
     * SOAP messages as XML fragment for instance.
     * @param data
     * @return
     */
    public T header(final String data) {
        getMessageContentBuilder().getHeaderData().add(data);
        return self;
    }

    /**
     * Expect this message header data as model object which is marshalled to a character sequence using the default object to xml mapper that
     * is available in Spring bean application context.
     *
     * @param model
     * @return
     */
    public T headerFragment(final Object model) {
    	return doHeaderFragment(model, null);
    }
    
    T doHeaderFragment(final Object model, final StringResult stringResult) {
        validateApplicationContext();

        if (!CollectionUtils.isEmpty(applicationContext.getBeansOfType(Marshaller.class))) {
            return doHeaderFragment(model, applicationContext.getBean(Marshaller.class), stringResult);
        } else if (!CollectionUtils.isEmpty(applicationContext.getBeansOfType(ObjectMapper.class))) {
            return headerFragment(model, applicationContext.getBean(ObjectMapper.class));
        }

        throw createUnableToFindMapperException();
    }

    /**
     * Expect this message header data as model object which is marshalled to a character sequence using the given object to xml mapper that
     * is accessed by its bean name in Spring bean application context.
     *
     * @param model
     * @param mapperName
     * @return
     */
    public T headerFragment(final Object model, final String mapperName) {
    	return doHeaderFragment(model, mapperName, null);
    }
    
    T doHeaderFragment(final Object model, final String mapperName, final StringResult stringResult) {
        validateApplicationContext();

        if (applicationContext.containsBean(mapperName)) {
            final Object mapper = applicationContext.getBean(mapperName);

            if (Marshaller.class.isAssignableFrom(mapper.getClass())) {
                return doHeaderFragment(model, (Marshaller) mapper, stringResult);
            } else if (ObjectMapper.class.isAssignableFrom(mapper.getClass())) {
                return headerFragment(model, (ObjectMapper) mapper);
            } else {
                throw new CitrusRuntimeException(String.format("Invalid bean type for mapper '%s' expected ObjectMapper or Marshaller but was '%s'", mapperName, mapper.getClass()));
            }
        }

        throw createUnableToFindMapperException();
    }

    /**
     * Expect this message header data as model object which is marshalled to a character sequence
     * using the default object to xml mapper before validation is performed.
     * @param model
     * @param marshaller
     * @return
     */
    public T headerFragment(final Object model, final Marshaller marshaller) {
    	return doHeaderFragment(model, marshaller, null);
    }
    
    T doHeaderFragment(final Object model, final Marshaller marshaller, final StringResult stringResult) {
         final StringResult result = stringResult == null ? new StringResult() : stringResult;
        try {
            marshaller.marshal(model, result);
        } catch (final XmlMappingException | IOException e) {
            throw new CitrusRuntimeException("Failed to marshal object graph for message header data", e);
        }

        return header(result.toString());
    }

    /**
     * Expect this message header data as model object which is mapped to a character sequence
     * using the default object to json mapper before validation is performed.
     * @param model
     * @param objectMapper
     * @return
     */
    T headerFragment(final Object model, final ObjectMapper objectMapper) {
        try {
            return header(objectMapper.writer().writeValueAsString(model));
        } catch (final JsonProcessingException e) {
            throw new CitrusRuntimeException("Failed to map object graph for message header data", e);
        }
    }

    /**
     * Expect this message header data in received message from file resource. Message header data is used in 
     * SOAP messages as XML fragment for instance.
     * @param resource
     * @return
     */
    public T header(final Resource resource) {
        return header(resource, FileUtils.getDefaultCharset());
    }

    /**
     * Expect this message header data in received message from file resource. Message header data is used in
     * SOAP messages as XML fragment for instance.
     * @param resource
     * @param charset
     * @return
     */
    public T header(final Resource resource, final Charset charset) {
        try {
            getMessageContentBuilder().getHeaderData().add(FileUtils.readToString(resource, charset));
        } catch (final IOException e) {
            throw new CitrusRuntimeException("Failed to read header resource", e);
        }

        return self;
    }

    /**
     * Validate header names with case insensitive keys.
     * @param value
     * @return
     */
    T headerNameIgnoreCase(final boolean value) {
        headerValidationContext.setHeaderNameIgnoreCase(value);
        return self;
    }
    
    /**
     * Adds script validation.
     * @param validationScript
     * @return
     */
    public T validateScript(final String validationScript) {
        getScriptValidationContext().setValidationScript(validationScript);

        return self;
    }
    
    /**
     * Reads validation script file resource and sets content as validation script.
     * @param scriptResource
     * @return
     */
    public T validateScript(final Resource scriptResource) {
        return validateScript(scriptResource, FileUtils.getDefaultCharset());
    }

    /**
     * Reads validation script file resource and sets content as validation script.
     * @param scriptResource
     * @param charset
     * @return
     */
    public T validateScript(final Resource scriptResource, final Charset charset) {
        try {
            validateScript(FileUtils.readToString(scriptResource, charset));
        } catch (final IOException e) {
            throw new CitrusRuntimeException("Failed to read script resource file", e);
        }

        return self;
    }

    /**
     * Adds script validation file resource.
     * @param fileResourcePath
     * @return
     */
    public T validateScriptResource(final String fileResourcePath) {
        getScriptValidationContext().setValidationScriptResourcePath(fileResourcePath);
        return self;
    }
    
    /**
     * Adds custom validation script type.
     * @param type
     * @return
     */
    T validateScriptType(final String type) {
        getScriptValidationContext().setScriptType(type);

        return self;
    }

    /**
     * Sets a explicit message type for this receive action.
     * @param messageType
     * @return
     */
    public T messageType(final MessageType messageType) {
        messageType(messageType.name());
        return self;
    }
    
    /**
     * Sets a explicit message type for this receive action.
     * @param messageType
     * @return
     */
    public T messageType(final String messageType) {
        this.messageType = messageType;
        getAction().setMessageType(messageType);

        if (getAction().getValidationContexts().isEmpty()) {
            getAction().getValidationContexts().add(headerValidationContext);
            getAction().getValidationContexts().add(xmlMessageValidationContext);
            getAction().getValidationContexts().add(jsonMessageValidationContext);
        }

        return self;
    }
    
    /**
     * Sets schema validation enabled/disabled for this message.
     * @param enabled
     * @return
     */
    public T schemaValidation(final boolean enabled) {
        xmlMessageValidationContext.setSchemaValidation(enabled);
        jsonMessageValidationContext.setSchemaValidation(enabled);
        return self;
    }

    /**
     * Validates XML namespace with prefix and uri.
     * @param prefix
     * @param namespaceUri
     * @return
     */
    public T validateNamespace(final String prefix, final String namespaceUri) {
        xmlMessageValidationContext.getControlNamespaces().put(prefix, namespaceUri);
        return self;
    }
    
    /**
     * Adds message element validation.
     * @param path
     * @param controlValue
     * @return
     */
    public T validate(final String path, final Object controlValue) {
        if (JsonPathMessageValidationContext.isJsonPathExpression(path)) {
            getJsonPathValidationContext().getJsonPathExpressions().put(path, controlValue);
        } else {
            getXPathValidationContext().getXpathExpressions().put(path, controlValue);
        }

        return self;
    }
    
    /**
     * Adds the given map of paths with their corresponding control values for validation.
     * @param map Map of paths with control values
     * @return The modified builder
     */
    public T validate(final Map<String, Object> map) {
        for(final Map.Entry<String, Object> validationMapping : map.entrySet()){
            validate(validationMapping.getKey(), validationMapping.getValue());
        }
        return self;
    }
    
    /**
     * Adds ignore path expression for message element.
     * @param path
     * @return
     */
    public T ignore(final String path) {
        if (messageType.equalsIgnoreCase(MessageType.XML.name())
                || messageType.equalsIgnoreCase(MessageType.XHTML.name())) {
            xmlMessageValidationContext.getIgnoreExpressions().add(path);
        } else if (messageType.equalsIgnoreCase(MessageType.JSON.name())) {
            jsonMessageValidationContext.getIgnoreExpressions().add(path);
        }
        return self;
    }
    
    /**
     * Adds XPath message element validation.
     * @param xPathExpression
     * @param controlValue
     * @return
     */
    public T xpath(final String xPathExpression, final Object controlValue) {
        validate(xPathExpression, controlValue);
        return self;
    }

    /**
     * Adds JsonPath message element validation.
     * @param jsonPathExpression
     * @param controlValue
     * @return
     */
    T jsonPath(final String jsonPathExpression, final Object controlValue) {
        validate(jsonPathExpression, controlValue);
        return self;
    }
    
    /**
     * Sets explicit schema instance name to use for schema validation.
     * @param schemaName
     * @return
     */
    public T xsd(final String schemaName) {
        xmlMessageValidationContext.setSchema(schemaName);
        return self;
    }

    /**
     * Sets explicit schema instance name to use for schema validation.
     * @param schemaName The name of the schema bean
     */
    public T jsonSchema(final String schemaName) {
        jsonMessageValidationContext.setSchema(schemaName);
        return self;
    }
    
    /**
     * Sets explicit xsd schema repository instance to use for validation.
     * @param schemaRepository
     * @return
     */
    public T xsdSchemaRepository(final String schemaRepository) {
        xmlMessageValidationContext.setSchemaRepository(schemaRepository);
        return self;
    }

    /**
     * Sets explicit json schema repository instance to use for validation.
     * @param schemaRepository The name of the schema repository bean
     * @return
     */
    public T jsonSchemaRepository(final String schemaRepository) {
        jsonMessageValidationContext.setSchemaRepository(schemaRepository);
        return self;
    }
    
    /**
     * Adds explicit namespace declaration for later path validation expressions.
     * @param prefix
     * @param namespaceUri
     * @return
     */
    public T namespace(final String prefix, final String namespaceUri) {
        getXpathVariableExtractor().getNamespaces().put(prefix, namespaceUri);
        xmlMessageValidationContext.getNamespaces().put(prefix, namespaceUri);
        return self;
    }
    
    /**
     * Sets default namespace declarations on this action builder.
     * @param namespaceMappings
     * @return
     */
    public T namespaces(final Map<String, String> namespaceMappings) {
        getXpathVariableExtractor().getNamespaces().putAll(namespaceMappings);

        xmlMessageValidationContext.getNamespaces().putAll(namespaceMappings);
        return self;
    }
    
    /**
     * Sets message selector string.
     * @param messageSelector
     * @return
     */
    public T selector(final String messageSelector) {
        getAction().setMessageSelector(messageSelector);

        return self;
    }
    
    /**
     * Sets message selector elements.
     * @param messageSelector
     * @return
     */
    public T selector(final Map<String, Object> messageSelector) {
        getAction().setMessageSelectorMap(messageSelector);

        return self;
    }
    
    /**
     * Sets explicit message validators for this receive action.
     * @param validators
     * @return
     */
    public T validator(final MessageValidator<? extends ValidationContext> ... validators) {
        Stream.of(validators).forEach(getAction()::addValidator);
        return self;
    }
    
    /**
     * Sets explicit message validators by name.
     * @param validatorNames
     * @return
     */
    @SuppressWarnings("unchecked")
    public T validator(final String ... validatorNames) {
        validateApplicationContext();

        for (final String validatorName : validatorNames) {
            getAction().addValidator(applicationContext.getBean(validatorName, MessageValidator.class));
        }

        return self;
    }

    /**
     * Sets explicit header validator for this receive action.
     * @param validators
     * @return
     */
    T headerValidator(final HeaderValidator... validators) {
        Stream.of(validators).forEach(headerValidationContext::addHeaderValidator);
        return self;
    }

    /**
     * Sets explicit header validators by name.
     * @param validatorNames
     * @return
     */
    T headerValidator(final String... validatorNames) {
        validateApplicationContext();

        for (final String validatorName : validatorNames) {
            headerValidationContext.addHeaderValidator(applicationContext.getBean(validatorName, HeaderValidator.class));
        }

        return self;
    }

    /**
     * Sets explicit data dictionary for this receive action.
     * @param dictionary
     * @return
     */
    public T dictionary(final DataDictionary dictionary) {
        getAction().setDataDictionary(dictionary);
        return self;
    }

    /**
     * Sets explicit data dictionary by name.
     * @param dictionaryName
     * @return
     */
    public T dictionary(final String dictionaryName) {
        validateApplicationContext();
        final DataDictionary dictionary = applicationContext.getBean(dictionaryName, DataDictionary.class);

        getAction().setDataDictionary(dictionary);
        return self;
    }
    
    /**
     * Extract message header entry as variable.
     * @param headerName
     * @param variable
     * @return
     */
    public T extractFromHeader(final String headerName, final String variable) {
        if (headerExtractor == null) {
            headerExtractor = new MessageHeaderVariableExtractor();

            getAction().getVariableExtractors().add(headerExtractor);
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
    public T extractFromPayload(final String path, final String variable) {
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
    public T validationCallback(final ValidationCallback callback) {
        if (callback instanceof ApplicationContextAware) {
            ((ApplicationContextAware) callback).setApplicationContext(applicationContext);
        }

        getAction().setValidationCallback(callback);
        return self;
    }

    /**
     * Sets the Spring bean application context.
     * @param applicationContext
     */
    public T withApplicationContext(final ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
        return self;
    }

    /**
     * Get message builder, if already registered or create a new message builder and register it
     *
     * @return the message builder in use
     */
    AbstractMessageContentBuilder getMessageContentBuilder() {
        if (getAction().getMessageBuilder() instanceof AbstractMessageContentBuilder) {
            return (AbstractMessageContentBuilder) getAction().getMessageBuilder();
        } else {
            final PayloadTemplateMessageBuilder messageBuilder = new PayloadTemplateMessageBuilder();
            getAction().setMessageBuilder(messageBuilder);
            return messageBuilder;
        }
    }

    /**
     * Creates new variable extractor and adds it to test action.
     */
    private XpathPayloadVariableExtractor getXpathVariableExtractor() {
        if (xpathExtractor == null) {
            xpathExtractor = new XpathPayloadVariableExtractor();

            getAction().getVariableExtractors().add(xpathExtractor);
        }

        return xpathExtractor;
    }

    /**
     * Creates new variable extractor and adds it to test action.
     */
    private JsonPathVariableExtractor getJsonPathVariableExtractor() {
        if (jsonPathExtractor == null) {
            jsonPathExtractor = new JsonPathVariableExtractor();

            getAction().getVariableExtractors().add(jsonPathExtractor);
        }

        return jsonPathExtractor;
    }

    /**
     * Gets the validation context as XML validation context an raises exception if existing validation context is
     * not a XML validation context.
     * @return
     */
    private XpathMessageValidationContext getXPathValidationContext() {
        if (xmlMessageValidationContext instanceof XpathMessageValidationContext) {
            return ((XpathMessageValidationContext)xmlMessageValidationContext);
        } else {
            final XpathMessageValidationContext xPathContext = new XpathMessageValidationContext();
            xPathContext.setNamespaces(xmlMessageValidationContext.getNamespaces());
            xPathContext.setControlNamespaces(xmlMessageValidationContext.getControlNamespaces());
            xPathContext.setIgnoreExpressions(xmlMessageValidationContext.getIgnoreExpressions());
            xPathContext.setSchema(xmlMessageValidationContext.getSchema());
            xPathContext.setSchemaRepository(xmlMessageValidationContext.getSchemaRepository());
            xPathContext.setSchemaValidation(xmlMessageValidationContext.isSchemaValidationEnabled());
            xPathContext.setDTDResource(xmlMessageValidationContext.getDTDResource());

            getAction().getValidationContexts().remove(xmlMessageValidationContext);
            getAction().getValidationContexts().add(xPathContext);

            xmlMessageValidationContext = xPathContext;
            return xPathContext;
        }
    }

    /**
     * Creates new script validation context if not done before and gets the script validation context.
     */
    private ScriptValidationContext getScriptValidationContext() {
        Assert.notNull(this.messageType, "Message Type is not initialized!");
        if (scriptValidationContext == null) {
            scriptValidationContext = new ScriptValidationContext(messageType);

            getAction().getValidationContexts().add(scriptValidationContext);
        }

        return scriptValidationContext;
    }

    /**
     * Creates new JSONPath validation context if not done before and gets the validation context.
     */
    private JsonPathMessageValidationContext getJsonPathValidationContext() {
        if (jsonPathValidationContext == null) {
            jsonPathValidationContext = new JsonPathMessageValidationContext();

            getAction().getValidationContexts().add(jsonPathValidationContext);
        }

        return jsonPathValidationContext;
    }

    /**
     * Provides access to receive message action delegate.
     * @return
     */
    protected ReceiveMessageAction getAction() {
        return (ReceiveMessageAction) action.getDelegate();
    }

    /**
     * Sets the message type.
     * @param messageType
     */
    protected void setMessageType(final MessageType messageType) {
        this.messageType = messageType.name();
    }

    /**
     * Sets the message type.
     * @param messageType
     */
    protected void setMessageType(final String messageType) {
        this.messageType = messageType;
    }

    /**
     * Sets the xpath extractor.
     * @param xpathExtractor
     */
    protected void setXpathExtractor(final XpathPayloadVariableExtractor xpathExtractor) {
        this.xpathExtractor = xpathExtractor;
    }

    /**
     * Sets the jsonPath extractor.
     * @param jsonPathExtractor
     */
    protected void setJsonPathExtractor(final JsonPathVariableExtractor jsonPathExtractor) {
        this.jsonPathExtractor = jsonPathExtractor;
    }

    /**
     * Sets the header extractor.
     * @param headerExtractor
     */
    protected void setHeaderExtractor(final MessageHeaderVariableExtractor headerExtractor) {
        this.headerExtractor = headerExtractor;
    }

    /**
     * Sets the script message validator.
     * @param scriptValidationContext
     */
    protected void setScriptValidationContext(final ScriptValidationContext scriptValidationContext) {
        this.scriptValidationContext = scriptValidationContext;
    }

    /**
     * Sets the script message validator.
     * @param jsonPathValidationContext
     */
    protected void setJsonPathValidationContext(final JsonPathMessageValidationContext jsonPathValidationContext) {
        this.jsonPathValidationContext = jsonPathValidationContext;
    }

    /**
     * Sets the XML validation context.
     * @param validationContext
     */
    protected void setXmlMessageValidationContext(final XmlMessageValidationContext validationContext) {
        this.xmlMessageValidationContext = validationContext;
    }

    /**
     * Sets the JSON validation context.
     * @param validationContext
     */
    protected void setJsonMessageValidationContext(final JsonMessageValidationContext validationContext) {
        this.jsonMessageValidationContext = validationContext;
    }

    /**
     * Sets the header validation context.
     * @param validationContext
     */
    protected void setHeaderValidationContext(final HeaderValidationContext validationContext) {
        this.headerValidationContext = validationContext;
    }

    private CitrusRuntimeException createUnableToFindMapperException() {
        return new CitrusRuntimeException("Unable to find default object mapper or marshaller in application context");
    }

    private void validateApplicationContext() {
        Assert.notNull(applicationContext, "Citrus application context is not initialized!");
    }
}
