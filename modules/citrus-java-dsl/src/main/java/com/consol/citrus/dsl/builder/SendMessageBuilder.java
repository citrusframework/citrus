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

package com.consol.citrus.dsl.builder;

import com.consol.citrus.Citrus;
import com.consol.citrus.TestAction;
import com.consol.citrus.actions.SendMessageAction;
import com.consol.citrus.dsl.actions.DelegatingTestAction;
import com.consol.citrus.endpoint.Endpoint;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.message.Message;
import com.consol.citrus.message.MessageType;
import com.consol.citrus.util.FileUtils;
import com.consol.citrus.validation.builder.AbstractMessageContentBuilder;
import com.consol.citrus.validation.builder.MessageContentBuilder;
import com.consol.citrus.validation.builder.PayloadTemplateMessageBuilder;
import com.consol.citrus.validation.builder.StaticMessageContentBuilder;
import com.consol.citrus.validation.interceptor.BinaryMessageConstructionInterceptor;
import com.consol.citrus.validation.interceptor.GzipMessageConstructionInterceptor;
import com.consol.citrus.validation.json.JsonPathMessageConstructionInterceptor;
import com.consol.citrus.validation.json.JsonPathMessageValidationContext;
import com.consol.citrus.validation.json.JsonPathVariableExtractor;
import com.consol.citrus.validation.xml.XpathMessageConstructionInterceptor;
import com.consol.citrus.validation.xml.XpathPayloadVariableExtractor;
import com.consol.citrus.variable.MessageHeaderVariableExtractor;
import com.consol.citrus.variable.dictionary.DataDictionary;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.oxm.Marshaller;
import org.springframework.oxm.XmlMappingException;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.xml.transform.StringResult;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Map;

/**
 * Action builder creates a send message action with several message payload and header
 * constructing build methods.
 *
 * @author Christoph Deppisch
 * @since 2.3
 */
public class SendMessageBuilder<A extends SendMessageAction, T extends SendMessageBuilder> extends AbstractTestActionBuilder<DelegatingTestAction<TestAction>> {

    /** Self reference for generics support */
    private final T self;

    /** Message type for this action builder */
    private String messageType = Citrus.DEFAULT_MESSAGE_TYPE;

    /** Variable extractors filled within this builder */
    private MessageHeaderVariableExtractor headerExtractor;
    private XpathPayloadVariableExtractor xpathExtractor;
    private JsonPathVariableExtractor jsonPathExtractor;

    /** Message constructing interceptor */
    private XpathMessageConstructionInterceptor xpathMessageConstructionInterceptor;
    private JsonPathMessageConstructionInterceptor jsonPathMessageConstructionInterceptor;
    private final GzipMessageConstructionInterceptor gzipMessageConstructionInterceptor = new GzipMessageConstructionInterceptor();
    private final BinaryMessageConstructionInterceptor binaryMessageConstructionInterceptor = new BinaryMessageConstructionInterceptor();

    /** Basic application context */
    private ApplicationContext applicationContext;

    /**
     * Default constructor with test action.
     * @param action
     */
    public SendMessageBuilder(A action) {
        this(new DelegatingTestAction(action));
    }

    /**
     * Default constructor.
     */
    public SendMessageBuilder() {
        this((A) new SendMessageAction());
    }

    /**
     * Constructor using delegate test action.
     * @param action
     */
    public SendMessageBuilder(DelegatingTestAction<TestAction> action) {
        super(action);
        this.self = (T) this;
    }

    /**
     * Sets the message endpoint to send messages to.
     * @param messageEndpoint
     * @return
     */
    public SendMessageBuilder endpoint(Endpoint messageEndpoint) {
        getAction().setEndpoint(messageEndpoint);
        return this;
    }

    /**
     * Sets the message endpoint uri to send messages to.
     * @param messageEndpointUri
     * @return
     */
    public SendMessageBuilder endpoint(String messageEndpointUri) {
        getAction().setEndpointUri(messageEndpointUri);
        return this;
    }

    /**
     * Sets the fork mode for this send action builder.
     * @param forkMode
     * @return
     */
    public T fork(boolean forkMode) {
        getAction().setForkMode(forkMode);
        return self;
    }

    /**
     * Sets the message instance to send.
     * @param message
     * @return
     */
    public T message(Message message) {
        StaticMessageContentBuilder staticMessageContentBuilder = StaticMessageContentBuilder.withMessage(message);
        staticMessageContentBuilder.setMessageHeaders(getMessageContentBuilder().getMessageHeaders());
        getAction().setMessageBuilder(staticMessageContentBuilder);
        return self;
    }

    /**
     * Sets the payload data on the message builder implementation.
     * @param payload
     * @return
     */
    protected void setPayload(String payload) {
        MessageContentBuilder messageContentBuilder = getMessageContentBuilder();

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
    public T name(String name) {
        getMessageContentBuilder().setMessageName(name);
        return self;
    }

    /**
     * Adds message payload data to this builder.
     * @param payload
     * @return
     */
    public T payload(String payload) {
        setPayload(payload);
        return self;
    }

    /**
     * Adds message payload resource to this builder.
     * @param payloadResource
     * @return
     */
    public T payload(Resource payloadResource) {
        return payload(payloadResource, FileUtils.getDefaultCharset());
    }

    /**
     * Adds message payload resource to this builder.
     * @param payloadResource
     * @param charset
     * @return
     */
    public T payload(Resource payloadResource, Charset charset) {
        try {
            setPayload(FileUtils.readToString(payloadResource, charset));
        } catch (IOException e) {
            throw new CitrusRuntimeException("Failed to read payload resource", e);
        }

        return self;
    }

    /**
     * Sets payload POJO object which is marshalled to a character sequence using the given object to xml mapper.
     * @param payload
     * @param marshaller
     * @return
     */
    public T payload(Object payload, Marshaller marshaller) {
        StringResult result = new StringResult();

        try {
            marshaller.marshal(payload, result);
        } catch (XmlMappingException | IOException e) {
            throw new CitrusRuntimeException("Failed to marshal object graph for message payload", e);
        }

        setPayload(result.toString());
        return self;
    }

    /**
     * Sets payload POJO object which is mapped to a character sequence using the given object to json mapper.
     * @param payload
     * @param objectMapper
     * @return
     */
    public T payload(Object payload, ObjectMapper objectMapper) {
        try {
            setPayload(objectMapper.writer().writeValueAsString(payload));
        } catch (JsonProcessingException e) {
            throw new CitrusRuntimeException("Failed to map object graph for message payload", e);
        }

        return self;
    }

    /**
     * Sets payload POJO object which is marshalled to a character sequence using the default object to xml or object
     * to json mapper that is available in Spring bean application context.
     *
     * @param payload
     * @return
     */
    public T payloadModel(Object payload) {
        Assert.notNull(applicationContext, "Citrus application context is not initialized!");

        if (!CollectionUtils.isEmpty(applicationContext.getBeansOfType(Marshaller.class))) {
            return payload(payload, applicationContext.getBean(Marshaller.class));
        } else if (!CollectionUtils.isEmpty(applicationContext.getBeansOfType(ObjectMapper.class))) {
            return payload(payload, applicationContext.getBean(ObjectMapper.class));
        }

        throw new CitrusRuntimeException("Unable to find default object mapper or marshaller in application context");
    }

    /**
     * Sets payload POJO object which is marshalled to a character sequence using the given object to xml mapper that
     * is accessed by its bean name in Spring bean application context.
     *
     * @param payload
     * @param mapperName
     * @return
     */
    public T payload(Object payload, String mapperName) {
        Assert.notNull(applicationContext, "Citrus application context is not initialized!");

        if (applicationContext.containsBean(mapperName)) {
            Object mapper = applicationContext.getBean(mapperName);

            if (Marshaller.class.isAssignableFrom(mapper.getClass())) {
                return payload(payload, (Marshaller) mapper);
            } else if (ObjectMapper.class.isAssignableFrom(mapper.getClass())) {
                return payload(payload, (ObjectMapper) mapper);
            } else {
                throw new CitrusRuntimeException(String.format("Invalid bean type for mapper '%s' expected ObjectMapper or Marshaller but was '%s'", mapperName, mapper.getClass()));
            }
        }

        throw new CitrusRuntimeException("Unable to find default object mapper or marshaller in application context");
    }

    /**
     * Adds message header name value pair to this builder's message sending action.
     * @param name
     * @param value
     */
    public T header(String name, Object value) {
        getMessageContentBuilder().getMessageHeaders().put(name, value);
        return self;
    }

    /**
     * Adds message headers to this builder's message sending action.
     * @param headers
     */
    public T headers(Map<String, Object> headers) {
        getMessageContentBuilder().getMessageHeaders().putAll(headers);
        return self;
    }

    /**
     * Adds message header data to this builder's message sending action. Message header data is used in SOAP
     * messages for instance as header XML fragment.
     * @param data
     */
    public T header(String data) {
        getMessageContentBuilder().getHeaderData().add(data);
        return self;
    }

    /**
     * Adds message header data as file resource to this builder's message sending action. Message header data is used in SOAP
     * messages for instance as header XML fragment.
     * @param resource
     */
    public T header(Resource resource) {
        return header(resource, FileUtils.getDefaultCharset());
    }

    /**
     * Adds message header data as file resource to this builder's message sending action. Message header data is used in SOAP
     * messages for instance as header XML fragment.
     * @param resource
     * @param charset
     */
    public T header(Resource resource, Charset charset) {
        try {
            getMessageContentBuilder().getHeaderData().add(FileUtils.readToString(resource, charset));
        } catch (IOException e) {
            throw new CitrusRuntimeException("Failed to read header resource", e);
        }
        return self;
    }

    /**
     * Sets header data POJO object which is marshalled to a character sequence using the given object to xml mapper.
     * @param model
     * @param marshaller
     * @return
     */
    public T headerFragment(Object model, Marshaller marshaller) {
        StringResult result = new StringResult();

        try {
            marshaller.marshal(model, result);
        } catch (XmlMappingException e) {
            throw new CitrusRuntimeException("Failed to marshal object graph for message header data", e);
        } catch (IOException e) {
            throw new CitrusRuntimeException("Failed to marshal object graph for message header data", e);
        }

        return header(result.toString());
    }

    /**
     * Sets header data POJO object which is mapped to a character sequence using the given object to json mapper.
     * @param model
     * @param objectMapper
     * @return
     */
    public T headerFragment(Object model, ObjectMapper objectMapper) {
        try {
            return header(objectMapper.writer().writeValueAsString(model));
        } catch (JsonProcessingException e) {
            throw new CitrusRuntimeException("Failed to map object graph for message header data", e);
        }
    }

    /**
     * Sets header data POJO object which is marshalled to a character sequence using the default object to xml or object
     * to json mapper that is available in Spring bean application context.
     *
     * @param model
     * @return
     */
    public T headerFragment(Object model) {
        Assert.notNull(applicationContext, "Citrus application context is not initialized!");

        if (!CollectionUtils.isEmpty(applicationContext.getBeansOfType(Marshaller.class))) {
            return headerFragment(model, applicationContext.getBean(Marshaller.class));
        } else if (!CollectionUtils.isEmpty(applicationContext.getBeansOfType(ObjectMapper.class))) {
            return headerFragment(model, applicationContext.getBean(ObjectMapper.class));
        }

        throw new CitrusRuntimeException("Unable to find default object mapper or marshaller in application context");
    }

    /**
     * Sets header data POJO object which is marshalled to a character sequence using the given object to xml mapper that
     * is accessed by its bean name in Spring bean application context.
     *
     * @param model
     * @param mapperName
     * @return
     */
    public T headerFragment(Object model, String mapperName) {
        Assert.notNull(applicationContext, "Citrus application context is not initialized!");

        if (applicationContext.containsBean(mapperName)) {
            Object mapper = applicationContext.getBean(mapperName);

            if (Marshaller.class.isAssignableFrom(mapper.getClass())) {
                return headerFragment(model, (Marshaller) mapper);
            } else if (ObjectMapper.class.isAssignableFrom(mapper.getClass())) {
                return headerFragment(model, (ObjectMapper) mapper);
            } else {
                throw new CitrusRuntimeException(String.format("Invalid bean type for mapper '%s' expected ObjectMapper or Marshaller but was '%s'", mapperName, mapper.getClass()));
            }
        }

        throw new CitrusRuntimeException("Unable to find default object mapper or marshaller in application context");
    }

    /**
     * Sets a explicit message type for this send action.
     * @param messageType
     * @return
     */
    public T messageType(MessageType messageType) {
        messageType(messageType.name());
        return self;
    }

    /**
     * Sets a explicit message type for this send action.
     * @param messageType The message type to send the message in
     * @return The modified send message
     */
    public T messageType(String messageType) {
        this.messageType = messageType;
        getAction().setMessageType(messageType);

        if (binaryMessageConstructionInterceptor.supportsMessageType(messageType)) {
            getMessageContentBuilder().add(binaryMessageConstructionInterceptor);
        }

        if (gzipMessageConstructionInterceptor.supportsMessageType(messageType)) {
            getMessageContentBuilder().add(gzipMessageConstructionInterceptor);
        }

        return self;
    }

    /**
     * Get message builder, if already registered or create a new message builder and register it
     *
     * @return the message builder in use
     */
    protected AbstractMessageContentBuilder getMessageContentBuilder() {
        if (getAction().getMessageBuilder() != null && getAction().getMessageBuilder() instanceof AbstractMessageContentBuilder) {
            return (AbstractMessageContentBuilder) getAction().getMessageBuilder();
        } else {
            PayloadTemplateMessageBuilder messageBuilder = new PayloadTemplateMessageBuilder();
            getAction().setMessageBuilder(messageBuilder);
            return messageBuilder;
        }
    }

    /**
     * Extract message header entry as variable before message is sent.
     * @param headerName
     * @param variable
     * @return
     */
    public T extractFromHeader(String headerName, String variable) {
        if (headerExtractor == null) {
            headerExtractor = new MessageHeaderVariableExtractor();

            getAction().getVariableExtractors().add(headerExtractor);
        }

        headerExtractor.getHeaderMappings().put(headerName, variable);
        return self;
    }

    /**
     * Extract message element via XPath or JSONPath from payload before message is sent.
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
     * Adds XPath manipulating expression that evaluates to message payload before sending.
     * @param expression
     * @param value
     * @return
     */
    public T xpath(String expression, String value) {
        if (xpathMessageConstructionInterceptor == null) {
            xpathMessageConstructionInterceptor = new XpathMessageConstructionInterceptor();

            if (getAction().getMessageBuilder() != null) {
                (getAction().getMessageBuilder()).add(xpathMessageConstructionInterceptor);
            } else {
                PayloadTemplateMessageBuilder messageBuilder = new PayloadTemplateMessageBuilder();
                messageBuilder.getMessageInterceptors().add(xpathMessageConstructionInterceptor);

                getAction().setMessageBuilder(messageBuilder);
            }
        }

        xpathMessageConstructionInterceptor.getXPathExpressions().put(expression, value);
        return self;
    }

    /**
     * Adds JSONPath manipulating expression that evaluates to message payload before sending.
     * @param expression
     * @param value
     * @return
     */
    public T jsonPath(String expression, String value) {
        if (jsonPathMessageConstructionInterceptor == null) {
            jsonPathMessageConstructionInterceptor = new JsonPathMessageConstructionInterceptor();

            if (getAction().getMessageBuilder() != null) {
                (getAction().getMessageBuilder()).add(jsonPathMessageConstructionInterceptor);
            } else {
                PayloadTemplateMessageBuilder messageBuilder = new PayloadTemplateMessageBuilder();
                messageBuilder.getMessageInterceptors().add(jsonPathMessageConstructionInterceptor);

                getAction().setMessageBuilder(messageBuilder);
            }
        }

        jsonPathMessageConstructionInterceptor.getJsonPathExpressions().put(expression, value);
        return self;
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
     * Sets the Spring bean application context.
     * @param applicationContext
     */
    public T withApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
        return self;
    }

    /**
     * Sets explicit data dictionary for this receive action.
     * @param dictionary
     * @return
     */
    public T dictionary(DataDictionary dictionary) {
        getAction().setDataDictionary(dictionary);
        return self;
    }

    /**
     * Sets explicit data dictionary by name.
     * @param dictionaryName
     * @return
     */
    @SuppressWarnings("unchecked")
    public T dictionary(String dictionaryName) {
        Assert.notNull(applicationContext, "Citrus application context is not initialized!");
        DataDictionary dictionary = applicationContext.getBean(dictionaryName, DataDictionary.class);

        getAction().setDataDictionary(dictionary);
        return self;
    }

    /**
     * Provides access to receive message action delegate.
     * @return
     */
    protected SendMessageAction getAction() {
        return (SendMessageAction) action.getDelegate();
    }
}
