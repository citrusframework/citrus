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
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import com.consol.citrus.AbstractTestActionBuilder;
import com.consol.citrus.CitrusSettings;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.endpoint.Endpoint;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.message.Message;
import com.consol.citrus.message.MessageDirection;
import com.consol.citrus.message.MessageSelectorBuilder;
import com.consol.citrus.message.MessageType;
import com.consol.citrus.messaging.Consumer;
import com.consol.citrus.messaging.SelectiveConsumer;
import com.consol.citrus.spi.ReferenceResolver;
import com.consol.citrus.spi.ReferenceResolverAware;
import com.consol.citrus.util.FileUtils;
import com.consol.citrus.validation.DefaultMessageHeaderValidator;
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
import com.consol.citrus.variable.VariableExtractor;
import com.consol.citrus.variable.dictionary.DataDictionary;
import com.consol.citrus.xml.StringResult;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.oxm.Marshaller;
import org.springframework.oxm.XmlMappingException;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

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
    private final Map<String, Object> messageSelectorMap;

    /** Select messages via message selector string */
    private final String messageSelector;

    /** Message endpoint */
    private final Endpoint endpoint;

    /** Message endpoint uri - either bean name or dynamic endpoint uri */
    private final String endpointUri;

    /** Receive timeout */
    private final long receiveTimeout;

    /** Builder constructing a control message */
    private final MessageContentBuilder messageBuilder;

    /** MessageValidator responsible for message validation */
    private final List<MessageValidator<? extends ValidationContext>> validators;

    /** Optional data dictionary that explicitly modifies message content before validation */
    private final DataDictionary<?> dataDictionary;

    /** Callback able to additionally validate received message */
    private final ValidationCallback validationCallback;

    /** List of validation contexts for this receive action */
    private final List<ValidationContext> validationContexts;

    /** List of variable extractors responsible for creating variables from received message content */
    private final List<VariableExtractor> variableExtractors;

    /** The expected message type to arrive in this receive action - this information is needed to find a proper
     * message validator for this message */
    private final String messageType;

    /** Logger */
    private static Logger log = LoggerFactory.getLogger(ReceiveMessageAction.class);

    /**
     * Default constructor.
     */
    public ReceiveMessageAction(ReceiveMessageActionBuilder<?, ?> builder) {
        super("receive", builder);

        this.endpoint = builder.endpoint;
        this.endpointUri = builder.endpointUri;
        this.receiveTimeout = builder.receiveTimeout;
        this.messageSelector = builder.messageSelector;
        this.messageSelectorMap = builder.messageSelectorMap;
        this.messageBuilder = builder.messageBuilder;
        this.validators = builder.validators;
        this.dataDictionary = builder.dataDictionary;
        this.validationCallback = builder.validationCallback;
        this.validationContexts = builder.validationContexts;
        this.variableExtractors = builder.variableExtractors;
        this.messageType = Optional.ofNullable(builder.messageType).orElse(CitrusSettings.DEFAULT_MESSAGE_TYPE);
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
                    MessageValidator<?extends ValidationContext> defaultMessageHeaderValidator = context.getMessageValidatorRegistry().getDefaultMessageHeaderValidator();
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
     * Gets the variable extractors.
     * @return the variableExtractors
     */
    public List<VariableExtractor> getVariableExtractors() {
        return variableExtractors;
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
     * Gets the data dictionary.
     * @return
     */
    public DataDictionary getDataDictionary() {
        return dataDictionary;
    }

    /**
     * Gets the messageBuilder.
     * @return the messageBuilder
     */
    public MessageContentBuilder getMessageBuilder() {
        return messageBuilder;
    }

    /**
     * Action builder.
     */
    public static final class Builder extends ReceiveMessageActionBuilder<ReceiveMessageAction, Builder> {

        /**
         * Fluent API action building entry method used in Java DSL.
         * @return
         */
        public static Builder receive() {
            return new Builder();
        }

        /**
         * Fluent API action building entry method used in Java DSL.
         *
         * @param messageEndpoint
         * @return
         */
        public static Builder receive(Endpoint messageEndpoint) {
            Builder builder = new Builder();
            builder.endpoint(messageEndpoint);
            return builder;
        }

        /**
         * Fluent API action building entry method used in Java DSL.
         *
         * @param messageEndpointUri
         * @return
         */
        public static Builder receive(String messageEndpointUri) {
            Builder builder = new Builder();
            builder.endpoint(messageEndpointUri);
            return builder;
        }

        @Override
        public ReceiveMessageAction doBuild() {
            return new ReceiveMessageAction(this);
        }
    }

    public static abstract class ReceiveMessageActionBuilder<T extends ReceiveMessageAction, B extends ReceiveMessageActionBuilder<T, B>> extends AbstractTestActionBuilder<T, B> implements ReferenceResolverAware {
        private Endpoint endpoint;
        private String endpointUri;
        private long receiveTimeout = 0L;
        private Map<String, Object> messageSelectorMap = new HashMap<>();
        private String messageSelector;
        private AbstractMessageContentBuilder messageBuilder = new PayloadTemplateMessageBuilder();
        private List<MessageValidator<? extends ValidationContext>> validators = new ArrayList<>();
        private DataDictionary<?> dataDictionary;
        private String dataDictionaryName;
        private ValidationCallback validationCallback;
        private List<ValidationContext> validationContexts = new ArrayList<>();
        private List<VariableExtractor> variableExtractors = new ArrayList<>();
        private String messageType = CitrusSettings.DEFAULT_MESSAGE_TYPE;

        /** Validation context used in this action builder */
        private HeaderValidationContext headerValidationContext;
        private XmlMessageValidationContext xmlMessageValidationContext;
        private JsonMessageValidationContext jsonMessageValidationContext;

        /** JSON validation context used in this action builder */
        private JsonPathMessageValidationContext jsonPathValidationContext;

        /** Script validation context used in this action builder */
        private ScriptValidationContext scriptValidationContext;

        /** Variable extractors filled within this action builder */
        private MessageHeaderVariableExtractor headerExtractor;
        private XpathPayloadVariableExtractor xpathExtractor;
        private JsonPathVariableExtractor jsonPathExtractor;

        private final List<String> validatorNames = new ArrayList<>();
        private final List<String> headerValidatorNames = new ArrayList<>();
        private final Map<String, List<Object>> headerFragmentMappers = new HashMap<>();
        private final Map<String, List<Object>> payloadMappers = new HashMap<>();

        /**
         * Basic bean reference resolver.
         */
        private ReferenceResolver referenceResolver;

        /**
         * Sets the message endpoint to receive messages from.
         *
         * @param messageEndpoint
         * @return
         */
        public B endpoint(final Endpoint messageEndpoint) {
            this.endpoint = messageEndpoint;
            return self;
        }

        /**
         * Sets the message endpoint uri to receive messages from.
         *
         * @param messageEndpointUri
         * @return
         */
        public B endpoint(final String messageEndpointUri) {
            this.endpointUri = messageEndpointUri;
            return self;
        }

        /**
         * Adds a custom timeout to this message receiving action.
         *
         * @param receiveTimeout
         * @return
         */
        public B timeout(final long receiveTimeout) {
            this.receiveTimeout = receiveTimeout;
            return self;
        }

        /**
         * Sets the message builder to use.
         * @param messageBuilder
         * @return
         */
        public B messageBuilder(AbstractMessageContentBuilder messageBuilder) {
            this.messageBuilder = messageBuilder;
            return self;
        }

        /**
         * Expect a control message in this receive action.
         *
         * @param controlMessage
         * @return
         */
        public B message(final Message controlMessage) {
            final StaticMessageContentBuilder staticMessageContentBuilder = StaticMessageContentBuilder.withMessage(controlMessage);
            staticMessageContentBuilder.setMessageHeaders(getMessageContentBuilder().getMessageHeaders());
            messageBuilder(staticMessageContentBuilder);
            return self;
        }

        /**
         * Sets the payload data on the message builder implementation.
         *
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
         *
         * @param name
         * @return
         */
        public B messageName(final String name) {
            getMessageContentBuilder().setMessageName(name);
            return self;
        }

        /**
         * Expect this message payload data in received message.
         *
         * @param payload
         * @return
         */
        public B payload(final String payload) {
            setPayload(payload);
            return self;
        }

        /**
         * Expect this message payload data in received message.
         *
         * @param payloadResource
         * @return
         */
        public B payload(final Resource payloadResource) {
            return payload(payloadResource, FileUtils.getDefaultCharset());
        }

        /**
         * Expect this message payload data in received message.
         *
         * @param payloadResource
         * @param charset
         * @return
         */
        public B payload(final Resource payloadResource, final Charset charset) {
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
         *
         * @param payload
         * @param marshaller
         * @return
         */
        public B payload(final Object payload, final Marshaller marshaller) {
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
         *
         * @param payload
         * @param objectMapper
         * @return
         */
        public B payload(final Object payload, final ObjectMapper objectMapper) {
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
        public B payloadModel(final Object payload) {
            this.payloadMappers.putIfAbsent("", new ArrayList<>());
            this.payloadMappers.get("").add(payload);
            return self;
        }

        /**
         * Expect this message payload as model object which is marshalled to a character sequence using the given object to xml mapper that
         * is accessed by its bean name in Spring bean application context.
         *
         * @param payload
         * @param mapperName
         * @return
         */
        public B payload(final Object payload, final String mapperName) {
            this.payloadMappers.putIfAbsent(mapperName, new ArrayList<>());
            this.payloadMappers.get(mapperName).add(payload);
            return self;
        }

        /**
         * Expect this message header entry in received message.
         *
         * @param name
         * @param value
         * @return
         */
        public B header(final String name, final Object value) {
            getMessageContentBuilder().getMessageHeaders().put(name, value);
            return self;
        }

        /**
         * Expect this message header entries in received message.
         *
         * @param headers
         * @return
         */
        public B headers(final Map<String, Object> headers) {
            getMessageContentBuilder().getMessageHeaders().putAll(headers);
            return self;
        }

        /**
         * Expect this message header data in received message. Message header data is used in
         * SOAP messages as XML fragment for instance.
         *
         * @param data
         * @return
         */
        public B header(final String data) {
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
        public B headerFragment(final Object model) {
            this.headerFragmentMappers.putIfAbsent("", new ArrayList<>());
            this.headerFragmentMappers.get("").add(model);
            return self;
        }

        /**
         * Expect this message header data as model object which is marshalled to a character sequence using the given object to xml mapper that
         * is accessed by its bean name in Spring bean application context.
         *
         * @param model
         * @param mapperName
         * @return
         */
        public B headerFragment(final Object model, final String mapperName) {
            this.headerFragmentMappers.putIfAbsent(mapperName, new ArrayList<>());
            this.headerFragmentMappers.get(mapperName).add(model);
            return self;
        }

        /**
         * Expect this message header data as model object which is marshalled to a character sequence
         * using the default object to xml mapper before validation is performed.
         *
         * @param model
         * @param marshaller
         * @return
         */
        public B headerFragment(final Object model, final Marshaller marshaller) {
            final StringResult result = new StringResult();
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
         *
         * @param model
         * @param objectMapper
         * @return
         */
        public B headerFragment(final Object model, final ObjectMapper objectMapper) {
            try {
                return header(objectMapper.writer().writeValueAsString(model));
            } catch (final JsonProcessingException e) {
                throw new CitrusRuntimeException("Failed to map object graph for message header data", e);
            }
        }

        /**
         * Expect this message header data in received message from file resource. Message header data is used in
         * SOAP messages as XML fragment for instance.
         *
         * @param resource
         * @return
         */
        public B header(final Resource resource) {
            return header(resource, FileUtils.getDefaultCharset());
        }

        /**
         * Expect this message header data in received message from file resource. Message header data is used in
         * SOAP messages as XML fragment for instance.
         *
         * @param resource
         * @param charset
         * @return
         */
        public B header(final Resource resource, final Charset charset) {
            try {
                getMessageContentBuilder().getHeaderData().add(FileUtils.readToString(resource, charset));
            } catch (final IOException e) {
                throw new CitrusRuntimeException("Failed to read header resource", e);
            }

            return self;
        }

        /**
         * Validate header names with case insensitive keys.
         *
         * @param value
         * @return
         */
        public B headerNameIgnoreCase(final boolean value) {
            getHeaderValidationContext().setHeaderNameIgnoreCase(value);
            return self;
        }

        /**
         * Adds script validation.
         *
         * @param validationScript
         * @return
         */
        public B validateScript(final String validationScript) {
            getScriptValidationContext().setValidationScript(validationScript);
            return self;
        }

        /**
         * Reads validation script file resource and sets content as validation script.
         *
         * @param scriptResource
         * @return
         */
        public B validateScript(final Resource scriptResource) {
            return validateScript(scriptResource, FileUtils.getDefaultCharset());
        }

        /**
         * Reads validation script file resource and sets content as validation script.
         *
         * @param scriptResource
         * @param charset
         * @return
         */
        public B validateScript(final Resource scriptResource, final Charset charset) {
            try {
                validateScript(FileUtils.readToString(scriptResource, charset));
            } catch (final IOException e) {
                throw new CitrusRuntimeException("Failed to read script resource file", e);
            }

            return self;
        }

        /**
         * Adds script validation file resource.
         *
         * @param fileResourcePath
         * @return
         */
        public B validateScriptResource(final String fileResourcePath) {
            getScriptValidationContext().setValidationScriptResourcePath(fileResourcePath);
            return self;
        }

        /**
         * Adds custom validation script type.
         *
         * @param type
         * @return
         */
        public B validateScriptType(final String type) {
            getScriptValidationContext().setScriptType(type);
            return self;
        }

        /**
         * Sets a explicit message type for this receive action.
         *
         * @param messageType
         * @return
         */
        public B messageType(final MessageType messageType) {
            messageType(messageType.name());
            return self;
        }

        /**
         * Sets a explicit message type for this receive action.
         *
         * @param messageType
         * @return
         */
        public B messageType(final String messageType) {
            this.messageType = messageType;

            if (MessageType.JSON.name().equalsIgnoreCase(messageType)) {
                getJsonMessageValidationContext();
            }

            if (MessageType.XML.name().equalsIgnoreCase(messageType)
                || MessageType.XHTML.name().equalsIgnoreCase(messageType)) {
                getXmlMessageValidationContext();
            }

            if (scriptValidationContext != null) {
                scriptValidationContext.setMessageType(messageType);
            }

            return self;
        }

        /**
         * Adds a validation context.
         * @param validationContext
         * @return
         */
        public B validationContext(final ValidationContext validationContext) {
            this.validationContexts.add(validationContext);
            return self;
        }

        /**
         * Sets validation contexts.
         * @param validationContexts
         * @return
         */
        public B validationContexts(final List<ValidationContext> validationContexts) {
            this.validationContexts = validationContexts;
            return self;
        }

        /**
         * Sets schema validation enabled/disabled for this message.
         *
         * @param enabled
         * @return
         */
        public B schemaValidation(final boolean enabled) {
            getXmlMessageValidationContext().setSchemaValidation(enabled);
            getJsonMessageValidationContext().setSchemaValidation(enabled);
            return self;
        }

        /**
         * Validates XML namespace with prefix and uri.
         *
         * @param prefix
         * @param namespaceUri
         * @return
         */
        public B validateNamespace(final String prefix, final String namespaceUri) {
            getXmlMessageValidationContext().getControlNamespaces().put(prefix, namespaceUri);
            return self;
        }

        /**
         * Adds message element validation.
         *
         * @param path
         * @param controlValue
         * @return
         */
        public B validate(final String path, final Object controlValue) {
            if (JsonPathMessageValidationContext.isJsonPathExpression(path)) {
                getJsonPathValidationContext().getJsonPathExpressions().put(path, controlValue);
            } else {
                getXPathValidationContext().getXpathExpressions().put(path, controlValue);
            }

            return self;
        }

        /**
         * Adds the given map of paths with their corresponding control values for validation.
         *
         * @param map Map of paths with control values
         * @return The modified builder
         */
        public B validate(final Map<String, Object> map) {
            for (final Map.Entry<String, Object> validationMapping : map.entrySet()) {
                validate(validationMapping.getKey(), validationMapping.getValue());
            }
            return self;
        }

        /**
         * Adds ignore path expression for message element.
         *
         * @param path
         * @return
         */
        public B ignore(final String path) {
            if (messageType.equalsIgnoreCase(MessageType.XML.name())
                    || messageType.equalsIgnoreCase(MessageType.XHTML.name())) {
                getXmlMessageValidationContext().getIgnoreExpressions().add(path);
            } else if (messageType.equalsIgnoreCase(MessageType.JSON.name())) {
                getJsonMessageValidationContext().getIgnoreExpressions().add(path);
            }
            return self;
        }

        /**
         * Adds XPath message element validation.
         *
         * @param xPathExpression
         * @param controlValue
         * @return
         */
        public B xpath(final String xPathExpression, final Object controlValue) {
            validate(xPathExpression, controlValue);
            return self;
        }

        /**
         * Adds JsonPath message element validation.
         *
         * @param jsonPathExpression
         * @param controlValue
         * @return
         */
        public B jsonPath(final String jsonPathExpression, final Object controlValue) {
            validate(jsonPathExpression, controlValue);
            return self;
        }

        /**
         * Sets explicit schema instance name to use for schema validation.
         *
         * @param schemaName
         * @return
         */
        public B xsd(final String schemaName) {
            getXmlMessageValidationContext().setSchema(schemaName);
            return self;
        }

        /**
         * Sets explicit schema instance name to use for schema validation.
         *
         * @param schemaName The name of the schema bean
         */
        public B jsonSchema(final String schemaName) {
            getJsonMessageValidationContext().setSchema(schemaName);
            return self;
        }

        /**
         * Sets explicit xsd schema repository instance to use for validation.
         *
         * @param schemaRepository
         * @return
         */
        public B xsdSchemaRepository(final String schemaRepository) {
            getXmlMessageValidationContext().setSchemaRepository(schemaRepository);
            return self;
        }

        /**
         * Sets explicit json schema repository instance to use for validation.
         *
         * @param schemaRepository The name of the schema repository bean
         * @return
         */
        public B jsonSchemaRepository(final String schemaRepository) {
            getJsonMessageValidationContext().setSchemaRepository(schemaRepository);
            return self;
        }

        /**
         * Adds explicit namespace declaration for later path validation expressions.
         *
         * @param prefix
         * @param namespaceUri
         * @return
         */
        public B namespace(final String prefix, final String namespaceUri) {
            getXpathVariableExtractor().getNamespaces().put(prefix, namespaceUri);
            getXmlMessageValidationContext().getNamespaces().put(prefix, namespaceUri);
            return self;
        }

        /**
         * Sets default namespace declarations on this action builder.
         *
         * @param namespaceMappings
         * @return
         */
        public B namespaces(final Map<String, String> namespaceMappings) {
            getXpathVariableExtractor().getNamespaces().putAll(namespaceMappings);

            getXmlMessageValidationContext().getNamespaces().putAll(namespaceMappings);
            return self;
        }

        /**
         * Sets message selector string.
         *
         * @param messageSelector
         * @return
         */
        public B selector(final String messageSelector) {
            this.messageSelector = messageSelector;
            return self;
        }

        /**
         * Sets message selector elements.
         *
         * @param messageSelector
         * @return
         */
        public B selector(final Map<String, String> messageSelector) {
            this.messageSelectorMap.putAll(messageSelector);
            return self;
        }

        /**
         * Sets explicit message validators for this receive action.
         *
         * @param validator
         * @return
         */
        public B validator(final MessageValidator<? extends ValidationContext> validator) {
            this.validators.add(validator);
            return self;
        }

        /**
         * Sets explicit message validators for this receive action.
         *
         * @param validators
         * @return
         */
        public B validators(MessageValidator<? extends ValidationContext>... validators) {
            return validators(Arrays.asList(validators));
        }

        /**
         * Sets explicit message validators for this receive action.
         *
         * @param validators
         * @return
         */
        public B validators(final List<MessageValidator<? extends ValidationContext>> validators) {
            this.validators.addAll(validators);
            return self;
        }

        /**
         * Sets explicit message validators by name.
         *
         * @param validatorNames
         * @return
         */
        @SuppressWarnings("unchecked")
        public B validator(final String... validatorNames) {
            this.validatorNames.addAll(Arrays.asList(validatorNames));
            return self;
        }

        /**
         * Sets explicit header validator for this receive action.
         *
         * @param validators
         * @return
         */
        public B headerValidator(final HeaderValidator... validators) {
            Stream.of(validators).forEach(getHeaderValidationContext()::addHeaderValidator);
            return self;
        }

        /**
         * Sets explicit header validators by name.
         *
         * @param validatorNames
         * @return
         */
        public B headerValidator(final String... validatorNames) {
            this.headerValidatorNames.addAll(Arrays.asList(validatorNames));
            return self;
        }

        /**
         * Sets explicit data dictionary for this receive action.
         *
         * @param dictionary
         * @return
         */
        public B dictionary(final DataDictionary<?> dictionary) {
            this.dataDictionary = dictionary;
            return self;
        }

        /**
         * Sets explicit data dictionary by name.
         *
         * @param dictionaryName
         * @return
         */
        public B dictionary(final String dictionaryName) {
            this.dataDictionaryName = dictionaryName;
            return self;
        }

        /**
         * Extract message header entry as variable.
         *
         * @param headerName
         * @param variable
         * @return
         */
        public B extractFromHeader(final String headerName, final String variable) {
            if (headerExtractor == null) {
                headerExtractor = new MessageHeaderVariableExtractor();

                variableExtractor(headerExtractor);
            }

            headerExtractor.getHeaderMappings().put(headerName, variable);
            return self;
        }

        /**
         * Extract message element via XPath or JSONPath from message payload as new test variable.
         *
         * @param path
         * @param variable
         * @return
         */
        public B extractFromPayload(final String path, final String variable) {
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
         *
         * @param callback
         * @return
         */
        public B validationCallback(final ValidationCallback callback) {
            this.validationCallback = callback;
            return self;
        }

        /**
         * Adds variable extractor.
         * @param extractor
         * @return
         */
        public B variableExtractor(VariableExtractor extractor) {
            this.variableExtractors.add(extractor);
            return self;
        }

        /**
         * Sets the bean reference resolver.
         *
         * @param referenceResolver
         */
        public B withReferenceResolver(final ReferenceResolver referenceResolver) {
            this.referenceResolver = referenceResolver;
            return self;
        }

        /**
         * Specifies the referenceResolver.
         *
         * @param referenceResolver
         */
        @Override
        public void setReferenceResolver(ReferenceResolver referenceResolver) {
            this.referenceResolver = referenceResolver;
        }

        @Override
        public final T build() {
            reconcileValidationContexts();

            if (referenceResolver != null) {
                if (validationCallback != null &&
                        validationCallback instanceof ReferenceResolverAware) {
                    ((ReferenceResolverAware) validationCallback).setReferenceResolver(referenceResolver);
                }

                while (!validatorNames.isEmpty()) {
                    final String validatorName = validatorNames.remove(0);
                    this.validators.add(referenceResolver.resolve(validatorName, MessageValidator.class));
                }

                while (!headerValidatorNames.isEmpty()) {
                    final String validatorName = headerValidatorNames.remove(0);
                    getHeaderValidationContext().addHeaderValidator(referenceResolver.resolve(validatorName, HeaderValidator.class));
                }

                if (dataDictionaryName != null) {
                    this.dataDictionary = referenceResolver.resolve(dataDictionaryName, DataDictionary.class);
                }

                for (Map.Entry<String, List<Object>> mapperEntry : headerFragmentMappers.entrySet()) {
                    String mapperName = mapperEntry.getKey();
                    final Object mapper = findMapperOrMarshaller(mapperName);

                    for (Object model : mapperEntry.getValue()) {
                        if (Marshaller.class.isAssignableFrom(mapper.getClass())) {
                            headerFragment(model, (Marshaller) mapper);
                        } else if (ObjectMapper.class.isAssignableFrom(mapper.getClass())) {
                            headerFragment(model, (ObjectMapper) mapper);
                        } else {
                            throw new CitrusRuntimeException(String.format("Invalid bean type for mapper '%s' expected ObjectMapper or Marshaller but was '%s'", mapperName, mapper.getClass()));
                        }
                    }
                }
                headerFragmentMappers.clear();

                for (Map.Entry<String, List<Object>> mapperEntry : payloadMappers.entrySet()) {
                    String mapperName = mapperEntry.getKey();
                    final Object mapper = findMapperOrMarshaller(mapperName);

                    for (Object model : mapperEntry.getValue()) {
                        if (Marshaller.class.isAssignableFrom(mapper.getClass())) {
                            payload(model, (Marshaller) mapper);
                        } else if (ObjectMapper.class.isAssignableFrom(mapper.getClass())) {
                            payload(model, (ObjectMapper) mapper);
                        } else {
                            throw new CitrusRuntimeException(String.format("Invalid bean type for mapper '%s' expected ObjectMapper or Marshaller but was '%s'", mapperName, mapper.getClass()));
                        }
                    }
                }
                payloadMappers.clear();
            }

            return doBuild();
        }

        /**
         * Find mapper or marshaller for given name using the reference resolver in this builder.
         * @param mapperName
         * @return
         */
        private Object findMapperOrMarshaller(String mapperName) {
            if (mapperName.equals("")) {
                if (!CollectionUtils.isEmpty(referenceResolver.resolveAll(Marshaller.class))) {
                    return referenceResolver.resolve(Marshaller.class);
                } else if (!CollectionUtils.isEmpty(referenceResolver.resolveAll(ObjectMapper.class))) {
                    return referenceResolver.resolve(ObjectMapper.class);
                } else {
                    throw createUnableToFindMapperException();
                }
            } else if (referenceResolver.isResolvable(mapperName)) {
                return referenceResolver.resolve(mapperName);
            } else {
                throw createUnableToFindMapperException();
            }
        }

        /**
         * Build method implemented by subclasses.
         * @return
         */
        protected abstract T doBuild();

        /**
         * Get message builder, if already registered or create a new message builder and register it
         *
         * @return the message builder in use
         */
        private AbstractMessageContentBuilder getMessageContentBuilder() {
            if (this.messageBuilder == null) {
                messageBuilder(new PayloadTemplateMessageBuilder());
            }

            return messageBuilder;
        }

        /**
         * Creates new variable extractor and adds it to test action.
         */
        private XpathPayloadVariableExtractor getXpathVariableExtractor() {
            if (xpathExtractor == null) {
                xpathExtractor = new XpathPayloadVariableExtractor();

                variableExtractor(xpathExtractor);
            }

            return xpathExtractor;
        }

        /**
         * Creates new variable extractor and adds it to test action.
         */
        private JsonPathVariableExtractor getJsonPathVariableExtractor() {
            if (jsonPathExtractor == null) {
                jsonPathExtractor = new JsonPathVariableExtractor();

                variableExtractor(jsonPathExtractor);
            }

            return jsonPathExtractor;
        }

        /**
         * Gets the validation context as XML validation context an raises exception if existing validation context is
         * not a XML validation context.
         *
         * @return
         */
        private XpathMessageValidationContext getXPathValidationContext() {
            if (getXmlMessageValidationContext() instanceof XpathMessageValidationContext) {
                return ((XpathMessageValidationContext) getXmlMessageValidationContext());
            } else {
                final XpathMessageValidationContext xPathContext = new XpathMessageValidationContext();
                xPathContext.setNamespaces(getXmlMessageValidationContext().getNamespaces());
                xPathContext.setControlNamespaces(getXmlMessageValidationContext().getControlNamespaces());
                xPathContext.setIgnoreExpressions(getXmlMessageValidationContext().getIgnoreExpressions());
                xPathContext.setSchema(getXmlMessageValidationContext().getSchema());
                xPathContext.setSchemaRepository(getXmlMessageValidationContext().getSchemaRepository());
                xPathContext.setSchemaValidation(getXmlMessageValidationContext().isSchemaValidationEnabled());
                xPathContext.setDTDResource(getXmlMessageValidationContext().getDTDResource());

                this.validationContexts.remove(getXmlMessageValidationContext());
                validationContext(xPathContext);

                xmlMessageValidationContext = xPathContext;
                return xPathContext;
            }
        }

        /**
         * Creates new xml validation context if not done before and gets the xml validation context.
         */
        private XmlMessageValidationContext getXmlMessageValidationContext() {
            if (xmlMessageValidationContext == null) {
                xmlMessageValidationContext = new XmlMessageValidationContext();

                validationContext(xmlMessageValidationContext);
            }

            return xmlMessageValidationContext;
        }

        /**
         * Creates new json validation context if not done before and gets the json validation context.
         */
        private JsonMessageValidationContext getJsonMessageValidationContext() {
            if (jsonMessageValidationContext == null) {
                jsonMessageValidationContext = new JsonMessageValidationContext();

                validationContext(jsonMessageValidationContext);
            }

            return jsonMessageValidationContext;
        }

        /**
         * Creates new header validation context if not done before and gets the header validation context.
         */
        private HeaderValidationContext getHeaderValidationContext() {
            if (headerValidationContext == null) {
                headerValidationContext = new HeaderValidationContext();

                validationContext(headerValidationContext);
            }

            return headerValidationContext;
        }

        /**
         * Creates new script validation context if not done before and gets the script validation context.
         */
        private ScriptValidationContext getScriptValidationContext() {
            if (scriptValidationContext == null) {
                scriptValidationContext = new ScriptValidationContext(messageType);

                validationContext(scriptValidationContext);
            }

            return scriptValidationContext;
        }

        /**
         * Creates new JSONPath validation context if not done before and gets the validation context.
         */
        private JsonPathMessageValidationContext getJsonPathValidationContext() {
            if (jsonPathValidationContext == null) {
                jsonPathValidationContext = new JsonPathMessageValidationContext();

                validationContext(jsonPathValidationContext);
            }

            return jsonPathValidationContext;
        }

        private CitrusRuntimeException createUnableToFindMapperException() {
            return new CitrusRuntimeException("Unable to resolve default object mapper or marshaller");
        }

        /**
         * Revisit configured validation context list and automatically add context based on message payload and path
         * expression contexts if any.
         *
         * This method makes sure that validation contexts are configured. If no validation context has been set yet the method
         * automatically adds proper validation contexts for Json and XML message payloads.
         *
         * In case a path expression (JsonPath, XPath) context is set but no proper message validation context (Json, Xml) the
         * method automatically adds the proper message validation context.
         *
         * Only when validation contexts are set properly according to the message type and content the message validation
         * steps will execute later on.
         */
        protected void reconcileValidationContexts() {
            if (validationContexts.isEmpty()
                    || validationContexts.stream().allMatch(HeaderValidationContext.class::isInstance)) {
                getXmlMessageValidationContext();
                getJsonMessageValidationContext();
            }

            if (validationContexts.stream().noneMatch(HeaderValidationContext.class::isInstance)) {
                getHeaderValidationContext();
            }

            if (validationContexts.stream().anyMatch(JsonPathMessageValidationContext.class::isInstance)
                    && validationContexts.stream().noneMatch(JsonMessageValidationContext.class::isInstance)) {
                getJsonMessageValidationContext();
            }

            // if still no Json or Xml message validation context is set check the message payload and set proper context
            if (validationContexts.stream().noneMatch(XmlMessageValidationContext.class::isInstance)
                && validationContexts.stream().noneMatch(JsonMessageValidationContext.class::isInstance)) {
                Optional<String> payload = getMessagePayload();
                if (payload.isPresent()) {
                    if (payload.get().startsWith("<")) {
                        getXmlMessageValidationContext();
                    } else if ((payload.get().startsWith("{") || payload.get().startsWith("["))) {
                        getJsonMessageValidationContext();
                    }
                }
            }
        }

        /**
         * Gets message payload String representation from configured message builder.
         * @return
         */
        protected Optional<String> getMessagePayload() {
            final MessageContentBuilder messageContentBuilder = getMessageContentBuilder();
            if (messageContentBuilder instanceof PayloadTemplateMessageBuilder) {
                return Optional.ofNullable(((PayloadTemplateMessageBuilder) messageContentBuilder).getPayloadData());
            } else if (messageContentBuilder instanceof StaticMessageContentBuilder) {
                Message message = ((StaticMessageContentBuilder) messageContentBuilder).getMessage();
                if (message.getPayload() instanceof String) {
                    return Optional.of(message.getPayload(String.class));
                }
            }

            return Optional.empty();
        }
    }
}
