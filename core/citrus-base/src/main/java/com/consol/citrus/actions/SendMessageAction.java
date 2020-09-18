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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import com.consol.citrus.AbstractTestActionBuilder;
import com.consol.citrus.CitrusSettings;
import com.consol.citrus.Completable;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.endpoint.Endpoint;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.message.Message;
import com.consol.citrus.message.MessageDirection;
import com.consol.citrus.message.MessageProcessor;
import com.consol.citrus.message.MessageType;
import com.consol.citrus.spi.ReferenceResolver;
import com.consol.citrus.spi.ReferenceResolverAware;
import com.consol.citrus.util.FileUtils;
import com.consol.citrus.validation.builder.AbstractMessageContentBuilder;
import com.consol.citrus.validation.builder.MessageContentBuilder;
import com.consol.citrus.validation.builder.PayloadTemplateMessageBuilder;
import com.consol.citrus.validation.builder.StaticMessageContentBuilder;
import com.consol.citrus.validation.json.JsonPathMessageProcessor;
import com.consol.citrus.validation.xml.XpathMessageProcessor;
import com.consol.citrus.variable.VariableExtractor;
import com.consol.citrus.variable.dictionary.DataDictionary;
import com.consol.citrus.xml.StringResult;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.oxm.Marshaller;
import org.springframework.oxm.XmlMappingException;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;


/**
 * This action sends a messages to a specified message endpoint. The action holds a reference to
 * a {@link com.consol.citrus.endpoint.Endpoint}, which is capable of the message transport implementation. So action is
 * independent of the message transport configuration.
 *
 * @author Christoph Deppisch
 * @since 2008
 */
public class SendMessageAction extends AbstractTestAction implements Completable {
    /** Message endpoint instance */
    private final Endpoint endpoint;

    /** Message endpoint uri - either bean name or dynamic uri */
    private final String endpointUri;

    /** List of message processors responsible for manipulating message to be sent */
    private final List<MessageProcessor> messageProcessors;

    /** Builder constructing a control message */
    private final MessageContentBuilder messageBuilder;

    /** Forks the message sending action so other actions can take place while this
     * message sender is waiting for the synchronous response */
    private final boolean forkMode;

    /** The message type to send in this action - this information is needed to find proper
     * message construction processors for this message */
    private final String messageType;

    /** Optional data dictionary that explicitly modifies message content before sending */
    private final DataDictionary<?> dataDictionary;

    /** Finished indicator either called when forked send action is finished or immediately when this action has finished */
    private CompletableFuture<Void> finished;

    /** Logger */
    private static Logger log = LoggerFactory.getLogger(SendMessageAction.class);

    /**
     * Default constructor.
     */
    public SendMessageAction(SendMessageActionBuilder<?, ?> builder) {
        super("send", builder);

        this.endpoint = builder.endpoint;
        this.endpointUri = builder.endpointUri;
        this.messageProcessors = builder.messageProcessors;
        this.messageBuilder = builder.messageBuilder;
        this.forkMode = builder.forkMode;
        this.messageType = builder.messageType;
        this.dataDictionary = builder.dataDictionary;
    }

    /**
     * Message is constructed with payload and header entries and sent via
     * {@link com.consol.citrus.endpoint.Endpoint} instance.
     */
    @Override
    public void doExecute(final TestContext context) {
        Message message = createMessage(context, messageType);
        finished = new CompletableFuture<>();

        for (MessageProcessor processor : messageProcessors) {
            processor.process(message, context);
        }

        final Endpoint messageEndpoint = getOrCreateEndpoint(context);

        if (StringUtils.hasText(message.getName())) {
            context.getMessageStore().storeMessage(message.getName(), message);
        } else {
            context.getMessageStore().storeMessage(context.getMessageStore().constructMessageName(this, messageEndpoint), message);
        }

        if (forkMode) {
            log.debug("Forking message sending action ...");

            SimpleAsyncTaskExecutor taskExecutor = new SimpleAsyncTaskExecutor();
            final Message finalMessage = message;
            taskExecutor.execute(() -> {
                try {
                    messageEndpoint.createProducer().send(finalMessage, context);
                } catch (Exception e) {
                    if (e instanceof CitrusRuntimeException) {
                        context.addException((CitrusRuntimeException) e);
                    } else {
                        context.addException(new CitrusRuntimeException(e));
                    }
                } finally {
                    finished.complete(null);
                }
            });
        } else {
            try {
                messageEndpoint.createProducer().send(message, context);
            } finally {
                finished.complete(null);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isDisabled(TestContext context) {
        Endpoint messageEndpoint = getOrCreateEndpoint(context);
        if (getActor() == null && messageEndpoint.getActor() != null) {
            return messageEndpoint.getActor().isDisabled();
        }

        return super.isDisabled(context);
    }

    @Override
    public boolean isDone(TestContext context) {
        return Optional.ofNullable(finished)
                .map(future -> future.isDone() || isDisabled(context))
                .orElse(isDisabled(context));
    }

    /**
     * Create message to be sent.
     * @param context
     * @param messageType
     * @return
     */
    protected Message createMessage(TestContext context, String messageType) {
        if (dataDictionary != null) {
            messageBuilder.setDataDictionary(dataDictionary);
        }

        return messageBuilder.buildMessageContent(context, messageType, MessageDirection.OUTBOUND);
    }

    /**
     * Creates or gets the message endpoint instance.
     * @return the message endpoint
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
     * Gets the message endpoint.
     * @return
     */
    public Endpoint getEndpoint() {
        return endpoint;
    }

    /**
     * Obtains the message processors.
     * @return
     */
    public List<MessageProcessor> getMessageProcessors() {
        return messageProcessors;
    }

    /**
     * Gets the messageBuilder.
     * @return the messageBuilder
     */
    public MessageContentBuilder getMessageBuilder() {
        return messageBuilder;
    }

    /**
     * Gets the forkMode.
     * @return the forkMode the forkMode to get.
     */
    public boolean isForkMode() {
        return forkMode;
    }

    /**
     * Gets the message type for this receive action.
     * @return the messageType
     */
    public String getMessageType() {
        return messageType;
    }

    /**
     * Gets the data dictionary.
     * @return
     */
    public DataDictionary<?> getDataDictionary() {
        return dataDictionary;
    }

    /**
     * Gets the endpoint uri.
     * @return
     */
    public String getEndpointUri() {
        return endpointUri;
    }

    /**
     * Action builder.
     */
    public static final class Builder extends SendMessageActionBuilder<SendMessageAction, Builder> {

        /**
         * Fluent API action building entry method used in Java DSL.
         * @return
         */
        public static Builder send() {
            return new Builder();
        }

        /**
         * Fluent API action building entry method used in Java DSL.
         * @param messageEndpoint
         * @return
         */
        public static Builder send(Endpoint messageEndpoint) {
            Builder builder = new Builder();
            builder.endpoint(messageEndpoint);
            return builder;
        }

        /**
         * Fluent API action building entry method used in Java DSL.
         * @param messageEndpointUri
         * @return
         */
        public static Builder send(String messageEndpointUri) {
            Builder builder = new Builder();
            builder.endpoint(messageEndpointUri);
            return builder;
        }

        @Override
        public SendMessageAction doBuild() {
            return new SendMessageAction(this);
        }

    }

    /**
     * Base send message action builder also used by subclasses of base send message action.
     */
    public static abstract class SendMessageActionBuilder<T extends SendMessageAction, B extends SendMessageActionBuilder<T, B>> extends AbstractTestActionBuilder<T, B> implements ReferenceResolverAware {

        protected Endpoint endpoint;
        protected String endpointUri;
        protected List<MessageProcessor> messageProcessors = new ArrayList<>();
        protected MessageContentBuilder messageBuilder = new PayloadTemplateMessageBuilder();
        protected boolean forkMode = false;
        protected CompletableFuture<Void> finished;
        protected String messageType = CitrusSettings.DEFAULT_MESSAGE_TYPE;
        protected DataDictionary<?> dataDictionary;
        protected String dataDictionaryName;

        private final Map<String, List<Object>> headerFragmentMappers = new HashMap<>();
        private final Map<String, List<Object>> payloadMappers = new HashMap<>();

        /** Message processor */
        private XpathMessageProcessor xpathMessageProcessor;
        private JsonPathMessageProcessor jsonPathMessageProcessor;

        /** Basic bean reference resolver */
        protected ReferenceResolver referenceResolver;

        /**
         * Sets the message endpoint to send messages to.
         * @param messageEndpoint
         * @return
         */
        public B endpoint(Endpoint messageEndpoint) {
            this.endpoint = messageEndpoint;
            return self;
        }

        /**
         * Sets the message endpoint uri to send messages to.
         * @param messageEndpointUri
         * @return
         */
        public B endpoint(String messageEndpointUri) {
            this.endpointUri = messageEndpointUri;
            return self;
        }

        /**
         * Sets the fork mode for this send action builder.
         * @param forkMode
         * @return
         */
        public B fork(boolean forkMode) {
            this.forkMode = forkMode;
            return self;
        }

        /**
         * Sets the message builder to use.
         * @param messageBuilder
         * @return
         */
        public B messageBuilder(MessageContentBuilder messageBuilder) {
            this.messageBuilder = messageBuilder;
            return self;
        }

        /**
         * Sets the message instance to send.
         * @param message
         * @return
         */
        public B message(Message message) {
            StaticMessageContentBuilder staticMessageContentBuilder = StaticMessageContentBuilder.withMessage(message);
            staticMessageContentBuilder.setMessageHeaders(getMessageContentBuilder().getMessageHeaders());
            messageBuilder(staticMessageContentBuilder);
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
        public B messageName(String name) {
            getMessageContentBuilder().setMessageName(name);
            return self;
        }

        /**
         * Adds message payload data to this builder.
         * @param payload
         * @return
         */
        public B payload(String payload) {
            setPayload(payload);
            return self;
        }

        /**
         * Adds message payload resource to this builder.
         * @param payloadResource
         * @return
         */
        public B payload(Resource payloadResource) {
            return payload(payloadResource, FileUtils.getDefaultCharset());
        }

        /**
         * Adds message payload resource to this builder.
         * @param payloadResource
         * @param charset
         * @return
         */
        public B payload(Resource payloadResource, Charset charset) {
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
        public B payload(Object payload, Marshaller marshaller) {
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
        public B payload(Object payload, ObjectMapper objectMapper) {
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
        public B payloadModel(Object payload) {
            this.payloadMappers.putIfAbsent("", new ArrayList<>());
            this.payloadMappers.get("").add(payload);
            return self;
        }

        /**
         * Sets payload POJO object which is marshalled to a character sequence using the given object to xml mapper that
         * is accessed by its bean name in Spring bean application context.
         *
         * @param payload
         * @param mapperName
         * @return
         */
        public B payload(Object payload, String mapperName) {
            this.payloadMappers.putIfAbsent(mapperName, new ArrayList<>());
            this.payloadMappers.get(mapperName).add(payload);
            return self;
        }

        /**
         * Adds message header name value pair to this builder's message sending action.
         * @param name
         * @param value
         */
        public B header(String name, Object value) {
            getMessageContentBuilder().getMessageHeaders().put(name, value);
            return self;
        }

        /**
         * Adds message headers to this builder's message sending action.
         * @param headers
         */
        public B headers(Map<String, Object> headers) {
            getMessageContentBuilder().getMessageHeaders().putAll(headers);
            return self;
        }

        /**
         * Adds message header data to this builder's message sending action. Message header data is used in SOAP
         * messages for instance as header XML fragment.
         * @param data
         */
        public B header(String data) {
            getMessageContentBuilder().getHeaderData().add(data);
            return self;
        }

        /**
         * Adds message header data as file resource to this builder's message sending action. Message header data is used in SOAP
         * messages for instance as header XML fragment.
         * @param resource
         */
        public B header(Resource resource) {
            return header(resource, FileUtils.getDefaultCharset());
        }

        /**
         * Adds message header data as file resource to this builder's message sending action. Message header data is used in SOAP
         * messages for instance as header XML fragment.
         * @param resource
         * @param charset
         */
        public B header(Resource resource, Charset charset) {
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
        public B headerFragment(Object model, Marshaller marshaller) {
            StringResult result = new StringResult();

            try {
                marshaller.marshal(model, result);
            } catch (XmlMappingException | IOException e) {
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
        public B headerFragment(Object model, ObjectMapper objectMapper) {
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
        public B headerFragment(Object model) {
            this.headerFragmentMappers.putIfAbsent("", new ArrayList<>());
            this.headerFragmentMappers.get("").add(model);
            return self;
        }

        /**
         * Sets header data POJO object which is marshalled to a character sequence using the given object to xml mapper that
         * is accessed by its bean name in Spring bean application context.
         *
         * @param model
         * @param mapperName
         * @return
         */
        public B headerFragment(Object model, String mapperName) {
            this.headerFragmentMappers.putIfAbsent(mapperName, new ArrayList<>());
            this.headerFragmentMappers.get(mapperName).add(model);
            return self;
        }

        /**
         * Sets a explicit message type for this send action.
         * @param messageType
         * @return
         */
        public B messageType(MessageType messageType) {
            messageType(messageType.name());
            return self;
        }

        /**
         * Sets an explicit message type for this send action.
         * @param messageType the type of the message indicates the content type (e.g. Xml, Json, binary).
         * @return The modified send message
         */
        public B messageType(String messageType) {
            this.messageType = messageType;
            return self;
        }

        /**
         * Get message builder, if already registered or create a new message builder and register it
         *
         * @return the message builder in use
         */
        public AbstractMessageContentBuilder getMessageContentBuilder() {
            if (this.messageBuilder != null && this.messageBuilder instanceof AbstractMessageContentBuilder) {
                return (AbstractMessageContentBuilder) this.messageBuilder;
            } else {
                PayloadTemplateMessageBuilder messageBuilder = new PayloadTemplateMessageBuilder();
                messageBuilder(messageBuilder);
                return messageBuilder;
            }
        }

        /**
         * Adds message processor.
         * @param processor
         * @return
         */
        public B process(MessageProcessor processor) {
            this.messageProcessors.add(processor);
            return self;
        }

        /**
         * Adds message processor as fluent builder.
         * @param builder
         * @return
         */
        public B process(MessageProcessor.Builder<?, ?> builder) {
            this.messageProcessors.add(builder.build());
            return self;
        }

        /**
         * Adds variable extractor.
         * @param extractor
         * @return
         */
        public B extract(VariableExtractor extractor) {
            this.messageProcessors.add(extractor);
            return self;
        }

        /**
         * Adds variable extractor as fluent builder.
         * @param builder
         * @return
         */
        public B extract(VariableExtractor.Builder<?, ?> builder) {
            this.messageProcessors.add(builder.build());
            return self;
        }

        /**
         * Adds XPath manipulating expression that evaluates to message payload before sending.
         * @param expression
         * @param value
         * @return
         */
        public B xpath(String expression, String value) {
            if (xpathMessageProcessor == null) {
                xpathMessageProcessor = new XpathMessageProcessor();

                if (this.messageBuilder != null) {
                    this.messageBuilder.add(xpathMessageProcessor);
                } else {
                    PayloadTemplateMessageBuilder messageBuilder = new PayloadTemplateMessageBuilder();
                    messageBuilder.add(xpathMessageProcessor);
                    messageBuilder(messageBuilder);
                }
            }

            xpathMessageProcessor.getXPathExpressions().put(expression, value);
            return self;
        }

        /**
         * Adds JSONPath manipulating expression that evaluates to message payload before sending.
         * @param expression
         * @param value
         * @return
         */
        public B jsonPath(String expression, String value) {
            if (jsonPathMessageProcessor == null) {
                jsonPathMessageProcessor = new JsonPathMessageProcessor();

                if (this.messageBuilder != null) {
                    this.messageBuilder.add(jsonPathMessageProcessor);
                } else {
                    PayloadTemplateMessageBuilder messageBuilder = new PayloadTemplateMessageBuilder();
                    messageBuilder.add(jsonPathMessageProcessor);
                    messageBuilder(messageBuilder);
                }
            }

            jsonPathMessageProcessor.getJsonPathExpressions().put(expression, value);
            return self;
        }

        /**
         * Sets the bean reference resolver.
         * @param referenceResolver
         */
        public B withReferenceResolver(ReferenceResolver referenceResolver) {
            this.referenceResolver = referenceResolver;
            return self;
        }

        /**
         * Sets explicit data dictionary for this receive action.
         * @param dictionary
         * @return
         */
        public B dictionary(DataDictionary dictionary) {
            this.dataDictionary = dictionary;
            return self;
        }

        /**
         * Sets explicit data dictionary by name.
         * @param dictionaryName
         * @return
         */
        public B dictionary(String dictionaryName) {
            this.dataDictionaryName = dictionaryName;
            return self;
        }

        @Override
        public void setReferenceResolver(ReferenceResolver referenceResolver) {
            this.referenceResolver = referenceResolver;
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

        private CitrusRuntimeException createUnableToFindMapperException() {
            return new CitrusRuntimeException("Unable to resolve default object mapper or marshaller");
        }

        /**
         * Build method implemented by subclasses.
         * @return
         */
        protected abstract T doBuild();

        @Override
        public final T build() {
            if (referenceResolver != null) {
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
    }
}
