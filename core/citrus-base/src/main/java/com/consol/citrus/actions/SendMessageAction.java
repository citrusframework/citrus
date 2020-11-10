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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import com.consol.citrus.AbstractTestActionBuilder;
import com.consol.citrus.CitrusSettings;
import com.consol.citrus.Completable;
import com.consol.citrus.common.Named;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.endpoint.Endpoint;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.message.Message;
import com.consol.citrus.message.MessageContentBuilder;
import com.consol.citrus.message.MessageDirection;
import com.consol.citrus.message.MessageDirectionAware;
import com.consol.citrus.message.MessageHeaderDataBuilder;
import com.consol.citrus.message.MessagePayloadBuilder;
import com.consol.citrus.message.MessageProcessor;
import com.consol.citrus.message.MessageType;
import com.consol.citrus.message.WithHeaderBuilder;
import com.consol.citrus.message.WithPayloadBuilder;
import com.consol.citrus.message.builder.DefaultHeaderBuilder;
import com.consol.citrus.message.builder.DefaultHeaderDataBuilder;
import com.consol.citrus.message.builder.DefaultPayloadBuilder;
import com.consol.citrus.spi.ReferenceResolver;
import com.consol.citrus.spi.ReferenceResolverAware;
import com.consol.citrus.util.FileUtils;
import com.consol.citrus.validation.builder.DefaultMessageContentBuilder;
import com.consol.citrus.validation.builder.StaticMessageContentBuilder;
import com.consol.citrus.variable.VariableExtractor;
import com.consol.citrus.variable.dictionary.DataDictionary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
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

    /** List of variable extractors responsible for creating variables from received message content */
    private final List<VariableExtractor> variableExtractors;

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
    private static final Logger LOG = LoggerFactory.getLogger(SendMessageAction.class);

    /**
     * Default constructor.
     */
    public SendMessageAction(SendMessageActionBuilder<?, ?> builder) {
        super("send", builder);

        this.endpoint = builder.endpoint;
        this.endpointUri = builder.endpointUri;
        this.variableExtractors = builder.variableExtractors;
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
        final Message message = createMessage(context, messageType);
        finished = new CompletableFuture<>();

        // extract variables from before sending message so we can save dynamic message ids
        for (VariableExtractor variableExtractor : variableExtractors) {
            variableExtractor.extractVariables(message, context);
        }

        final Endpoint messageEndpoint = getOrCreateEndpoint(context);

        if (StringUtils.hasText(message.getName())) {
            context.getMessageStore().storeMessage(message.getName(), message);
        } else {
            context.getMessageStore().storeMessage(context.getMessageStore().constructMessageName(this, messageEndpoint), message);
        }

        if (forkMode) {
            LOG.debug("Forking message sending action ...");

            SimpleAsyncTaskExecutor taskExecutor = new SimpleAsyncTaskExecutor();
            taskExecutor.execute(() -> {
                try {
                    messageEndpoint.createProducer().send(message, context);
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
        Message message = messageBuilder.buildMessageContent(context, messageType);

        if (message.getPayload() != null) {
            for (final MessageProcessor processor: context.getMessageProcessors().getMessageProcessors()) {
                MessageDirection processorDirection = MessageDirection.UNBOUND;

                if (processor instanceof MessageDirectionAware) {
                    processorDirection = ((MessageDirectionAware) processor).getDirection();
                }

                if (processorDirection.equals(MessageDirection.OUTBOUND)
                        || processorDirection.equals(MessageDirection.UNBOUND)) {
                    processor.process(message, context);
                }
            }

            if (dataDictionary != null) {
                dataDictionary.process(message, context);
            }

            for (final MessageProcessor processor : messageProcessors) {
                MessageDirection processorDirection = MessageDirection.UNBOUND;

                if (processor instanceof MessageDirectionAware) {
                    processorDirection = ((MessageDirectionAware) processor).getDirection();
                }

                if (processorDirection.equals(MessageDirection.OUTBOUND)
                        || processorDirection.equals(MessageDirection.UNBOUND)) {
                    processor.process(message, context);
                }
            }
        }

        return message;
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
     * Get the variable extractors.
     * @return the variableExtractors
     */
    public List<VariableExtractor> getVariableExtractors() {
        return variableExtractors;
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
        protected List<VariableExtractor> variableExtractors = new ArrayList<>();
        protected List<MessageProcessor> messageProcessors = new ArrayList<>();
        protected MessageContentBuilder messageBuilder = new DefaultMessageContentBuilder();
        protected boolean forkMode = false;
        protected CompletableFuture<Void> finished;
        protected String messageType = CitrusSettings.DEFAULT_MESSAGE_TYPE;
        protected DataDictionary<?> dataDictionary;
        protected String dataDictionaryName;

        private final Map<String, List<Object>> headerFragmentMappers = new HashMap<>();
        private final Map<String, List<Object>> payloadMappers = new HashMap<>();

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
        public B message(MessageContentBuilder messageBuilder) {
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

            if (messageBuilder instanceof WithHeaderBuilder) {
                ((WithHeaderBuilder) messageBuilder).getHeaderBuilders().forEach(staticMessageContentBuilder::addHeaderBuilder);
            }

            message(staticMessageContentBuilder);
            messageType(message.getType());
            return self;
        }

        /**
         * Sets the message name.
         * @param name
         * @return
         */
        public B messageName(String name) {
            if (messageBuilder instanceof Named) {
                ((Named) messageBuilder).setName(name);
            } else {
                throw new CitrusRuntimeException("Unable to set message name on builder type: " + messageBuilder.getClass());
            }
            return self;
        }

        /**
         * Sets the payload data on the message builder implementation.
         * @param payloadBuilder
         * @return
         */
        public B payload(final MessagePayloadBuilder payloadBuilder) {
            if (messageBuilder instanceof WithPayloadBuilder) {
                ((WithPayloadBuilder) messageBuilder).setPayloadBuilder(payloadBuilder);
            } else {
                throw new CitrusRuntimeException("Unable to set payload builder on message builder type: " + messageBuilder.getClass());
            }
            return self;
        }

        /**
         * Adds message payload data to this builder.
         * @param payload
         * @return
         */
        public B payload(String payload) {
            payload(new DefaultPayloadBuilder(payload));
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
                payload(FileUtils.readToString(payloadResource, charset));
            } catch (IOException e) {
                throw new CitrusRuntimeException("Failed to read payload resource", e);
            }
            return self;
        }

        /**
         * Adds message header name value pair to this builder's message sending action.
         * @param name
         * @param value
         */
        public B header(String name, Object value) {
            if (messageBuilder instanceof WithHeaderBuilder) {
                ((WithHeaderBuilder) messageBuilder).addHeaderBuilder(new DefaultHeaderBuilder(Collections.singletonMap(name, value)));
            } else {
                throw new CitrusRuntimeException("Unable to set message header on builder type: " + messageBuilder.getClass());
            }
            return self;
        }

        /**
         * Adds message headers to this builder's message sending action.
         * @param headers
         */
        public B headers(Map<String, Object> headers) {
            if (messageBuilder instanceof WithHeaderBuilder) {
                ((WithHeaderBuilder) messageBuilder).addHeaderBuilder(new DefaultHeaderBuilder(headers));
            } else {
                throw new CitrusRuntimeException("Unable to set message header on builder type: " + messageBuilder.getClass());
            }
            return self;
        }

        /**
         * Adds message header data to this builder's message sending action. Message header data is used in SOAP
         * messages for instance as header XML fragment.
         * @param data
         */
        public B header(String data) {
            if (messageBuilder instanceof WithHeaderBuilder) {
                ((WithHeaderBuilder) messageBuilder).addHeaderBuilder(new DefaultHeaderDataBuilder(data));
            } else {
                throw new CitrusRuntimeException("Unable to set message header data on builder type: " + messageBuilder.getClass());
            }
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
                if (messageBuilder instanceof WithHeaderBuilder) {
                    ((WithHeaderBuilder) messageBuilder).addHeaderBuilder(new DefaultHeaderDataBuilder(FileUtils.readToString(resource, charset)));
                } else {
                    throw new CitrusRuntimeException("Unable to set message header data on builder type: " + messageBuilder.getClass());
                }
            } catch (IOException e) {
                throw new CitrusRuntimeException("Failed to read header resource", e);
            }
            return self;
        }

        /**
         * Adds message header data builder to this builder's message sending action. Message header data is used in
         * SOAP messages as XML fragment for instance.
         *
         * @param headerDataBuilder
         * @return
         */
        public B header(final MessageHeaderDataBuilder headerDataBuilder) {
            if (messageBuilder instanceof WithHeaderBuilder) {
                ((WithHeaderBuilder) messageBuilder).addHeaderBuilder(headerDataBuilder);
            } else {
                throw new CitrusRuntimeException("Unable to set message header data on builder type: " + messageBuilder.getClass());
            }
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
         * Adds message processor.
         * @param processor
         * @return
         */
        public B process(MessageProcessor processor) {
            if (processor instanceof VariableExtractor) {
                this.variableExtractors.add((VariableExtractor) processor);
            } else {
                this.messageProcessors.add(processor);
            }
            return self;
        }

        /**
         * Adds message processor as fluent builder.
         * @param builder
         * @return
         */
        public B process(MessageProcessor.Builder<?, ?> builder) {
            return process(builder.build());
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
        public B dictionary(DataDictionary<?> dictionary) {
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
            }

            return doBuild();
        }
    }
}
