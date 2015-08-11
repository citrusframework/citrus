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

import com.consol.citrus.CitrusConstants;
import com.consol.citrus.actions.SendMessageAction;
import com.consol.citrus.dsl.util.PositionHandle;
import com.consol.citrus.endpoint.Endpoint;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.message.*;
import com.consol.citrus.util.FileUtils;
import com.consol.citrus.validation.builder.*;
import com.consol.citrus.validation.json.*;
import com.consol.citrus.validation.xml.XpathMessageConstructionInterceptor;
import com.consol.citrus.variable.MessageHeaderVariableExtractor;
import com.consol.citrus.validation.xml.XpathPayloadVariableExtractor;
import com.consol.citrus.ws.actions.SendSoapMessageAction;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.oxm.Marshaller;
import org.springframework.oxm.XmlMappingException;
import org.springframework.util.Assert;
import org.springframework.xml.transform.StringResult;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Action builder creates a send message action with several message payload and header
 * constructing build methods.
 * 
 * @author Christoph Deppisch
 * @since 2.3
 */
public class SendMessageBuilder<A extends SendMessageAction, T extends SendMessageBuilder> extends AbstractTestActionBuilder<A> {

    /** Self reference for generics support */
    private final T self;

    /** Message type for this action builder */
    private MessageType messageType = MessageType.valueOf(CitrusConstants.DEFAULT_MESSAGE_TYPE);

    /** Variable extractors filled within this builder */
    private MessageHeaderVariableExtractor headerExtractor;
    private XpathPayloadVariableExtractor xpathExtractor;
    private JsonPathVariableExtractor jsonPathExtractor;

    /** Message constructing interceptor */
    private XpathMessageConstructionInterceptor xpathMessageConstructionInterceptor;
    private JsonPathMessageConstructionInterceptor jsonPathMessageConstructionInterceptor;

    /** Basic application context */
    private ApplicationContext applicationContext;

    /** Handle for test action position in test case sequence use when switching to SOAP specific builder */
    private PositionHandle positionHandle;

    /**
     * Default constructor with test action.
     * @param action
     */
    public SendMessageBuilder(A action) {
        super(action);
        this.self = (T) this;
    }

    /**
     * Default constructor.
     */
    public SendMessageBuilder() {
        this((A) new SendMessageAction());
    }

    /**
     * Sets the message endpoint to send messages to.
     * @param messageEndpoint
     * @return
     */
    public SendMessageBuilder endpoint(Endpoint messageEndpoint) {
        action.setEndpoint(messageEndpoint);
        return this;
    }

    /**
     * Sets the message endpoint uri to send messages to.
     * @param messageEndpointUri
     * @return
     */
    public SendMessageBuilder endpoint(String messageEndpointUri) {
        action.setEndpointUri(messageEndpointUri);
        return this;
    }

    /**
     * Sets the position handle as internal marker where in test action sequence this action was set.
     * @param positionHandle
     * @return
     */
    public SendMessageBuilder position(PositionHandle positionHandle) {
        this.positionHandle = positionHandle;
        return this;
    }

    /**
     * Sets the fork mode for this send action builder.
     * @param forkMode
     * @return
     */
    public T fork(boolean forkMode) {
        action.setForkMode(forkMode);
        return self;
    }
    
    /**
     * Sets the message instance to send.
     * @param message
     * @return
     */
    public T message(Message message) {
        if (message.getPayload() != null && message.getPayload() instanceof String) {
            PayloadTemplateMessageBuilder messageBuilder = getPayloadTemplateMessageBuilder();
            messageBuilder.setPayloadData(message.getPayload(String.class));

            Map<String, Object> headers = new HashMap<String, Object>();
            for (String headerName : message.copyHeaders().keySet()) {
                if (!MessageHeaderUtils.isSpringInternalHeader(headerName) &&
                        !headerName.startsWith(MessageHeaders.MESSAGE_PREFIX)) {
                    headers.put(headerName, message.getHeader(headerName));
                }
            }

            messageBuilder.getMessageHeaders().putAll(headers);
            action.setMessageBuilder(messageBuilder);
        } else {
            action.setMessageBuilder(new StaticMessageContentBuilder(message));
        }

        return self;
    }
    
    /**
     * Adds message payload data to this builder.
     * @param payload
     * @return
     */
    public T payload(String payload) {
        getPayloadTemplateMessageBuilder().setPayloadData(payload);
        return self;
    }
    
    /**
     * Adds message payload resource to this builder.
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
     * Sets payload POJO object which is marshalled to a character sequence using the given object to xml mapper.
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
        
        if (action.getMessageBuilder() != null && action.getMessageBuilder() instanceof PayloadTemplateMessageBuilder) {
            ((PayloadTemplateMessageBuilder)action.getMessageBuilder()).setPayloadData(result.toString());
        } else {
            PayloadTemplateMessageBuilder messageBuilder = new PayloadTemplateMessageBuilder();
            messageBuilder.setPayloadData(result.toString());
            
            action.setMessageBuilder(messageBuilder);
        }
        
        return self;
    }

    /**
     * Sets payload POJO object which is marshalled to a character sequence using the default object to xml mapper that
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
     * Sets payload POJO object which is marshalled to a character sequence using the given object to xml mapper that
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
     * Adds message header name value pair to this builder's message sending action.
     * @param name
     * @param value
     */
    public T header(String name, Object value) {
        getMessageContentBuilder().getMessageHeaders().put(name, value);
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
        try {
            getMessageContentBuilder().getHeaderData().add(FileUtils.readToString(resource));
        } catch (IOException e) {
            throw new CitrusRuntimeException("Failed to read header resource", e);
        }
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
     * Get message builder, if already registered or create a new message builder and register it
     *
     * @return the message builder in use
     */
    protected AbstractMessageContentBuilder getMessageContentBuilder() {
        if (action.getMessageBuilder() != null && action.getMessageBuilder() instanceof AbstractMessageContentBuilder) {
            return (AbstractMessageContentBuilder) action.getMessageBuilder();
        } else {
            PayloadTemplateMessageBuilder messageBuilder = new PayloadTemplateMessageBuilder();
            action.setMessageBuilder(messageBuilder);
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
            action.setMessageBuilder(messageBuilder);
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
            
            action.getVariableExtractors().add(headerExtractor);
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

            if (action.getMessageBuilder() != null) {
                (action.getMessageBuilder()).add(xpathMessageConstructionInterceptor);
            } else {
                PayloadTemplateMessageBuilder messageBuilder = new PayloadTemplateMessageBuilder();
                messageBuilder.getMessageInterceptors().add(xpathMessageConstructionInterceptor);

                action.setMessageBuilder(messageBuilder);
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

            if (action.getMessageBuilder() != null) {
                (action.getMessageBuilder()).add(jsonPathMessageConstructionInterceptor);
            } else {
                PayloadTemplateMessageBuilder messageBuilder = new PayloadTemplateMessageBuilder();
                messageBuilder.getMessageInterceptors().add(jsonPathMessageConstructionInterceptor);

                action.setMessageBuilder(messageBuilder);
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
     * Sets the Spring bean application context.
     * @param applicationContext
     */
    public T withApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
        return self;
    }

    /**
     * Enable SOAP specific properties on this message sending action.
     * @return
     */
    public SendSoapMessageBuilder soap() {
        SendSoapMessageAction sendSoapMessageAction = new SendSoapMessageAction();
        sendSoapMessageAction.setActor(action.getActor());
        sendSoapMessageAction.setMessageType(messageType.toString());
        sendSoapMessageAction.setDescription(action.getDescription());
        sendSoapMessageAction.setMessageBuilder(action.getMessageBuilder());
        sendSoapMessageAction.setEndpoint(action.getEndpoint());
        sendSoapMessageAction.setEndpointUri(action.getEndpointUri());
        sendSoapMessageAction.setVariableExtractors(action.getVariableExtractors());

        if (positionHandle != null) {
            positionHandle.switchTestAction(sendSoapMessageAction);
        } else {
            action = (A) sendSoapMessageAction;
        }

        SendSoapMessageBuilder builder = new SendSoapMessageBuilder(sendSoapMessageAction);
        builder.withApplicationContext(applicationContext);

        return builder;
    }

    /**
     * Enable features specific for an HTTP REST endpoint. This includes setting the
     * HTTP method and the endpoint URI.
     *
     * Example:
     * <pre>
     *     send("httpClient").method(HttpMethod.GET).uri("http://localhost:8080/jolokia");
     * </pre>
     *
     *
     * @return HTTP specific builder.
     */
    public SendHttpMessageBuilder http() {
        SendHttpMessageBuilder builder = new SendHttpMessageBuilder(action);
        builder.position(positionHandle);
        builder.withApplicationContext(applicationContext);

        return builder;
    }
}
