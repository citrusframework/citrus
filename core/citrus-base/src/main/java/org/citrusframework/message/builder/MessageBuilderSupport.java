/*
 * Copyright 2022 the original author or authors.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.citrusframework.message.builder;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.citrusframework.AbstractTestActionBuilder;
import org.citrusframework.CitrusSettings;
import org.citrusframework.TestAction;
import org.citrusframework.TestActionBuilder;
import org.citrusframework.common.Named;
import org.citrusframework.endpoint.Endpoint;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.message.Message;
import org.citrusframework.message.MessageBuilder;
import org.citrusframework.message.MessageHeaderDataBuilder;
import org.citrusframework.message.MessagePayloadBuilder;
import org.citrusframework.message.MessageProcessor;
import org.citrusframework.message.MessageProcessorAdapter;
import org.citrusframework.message.MessageType;
import org.citrusframework.message.WithHeaderBuilder;
import org.citrusframework.message.WithPayloadBuilder;
import org.citrusframework.spi.ReferenceResolver;
import org.citrusframework.spi.ReferenceResolverAware;
import org.citrusframework.spi.Resource;
import org.citrusframework.util.FileUtils;
import org.citrusframework.validation.builder.DefaultMessageBuilder;
import org.citrusframework.validation.builder.StaticMessageBuilder;
import org.citrusframework.variable.VariableExtractor;
import org.citrusframework.variable.VariableExtractorAdapter;
import org.citrusframework.variable.dictionary.DataDictionary;

/**
 * @author Christoph Deppisch
 */
public abstract class MessageBuilderSupport<T extends TestAction, B extends MessageBuilderSupport.MessageActionBuilder<T, S, B>, S extends MessageBuilderSupport<T, B, S>>
        implements TestActionBuilder<T>, ReferenceResolverAware {
    protected final S self;

    protected MessageBuilder messageBuilder = new DefaultMessageBuilder();

    protected final B delegate;

    protected String messageType = CitrusSettings.DEFAULT_MESSAGE_TYPE;

    protected DataDictionary<?> dataDictionary;
    protected String dataDictionaryName;

    protected MessageBuilderSupport(B delegate) {
        this.self = (S) this;
        this.delegate = delegate;
    }

    /**
     * Build message from given message builder.
     * @param messageBuilder
     * @return The modified message action builder
     */
    public S from(final MessageBuilder messageBuilder) {
        this.messageBuilder = messageBuilder;
        return self;
    }

    /**
     * Build message from given message template.
     * @param controlMessage
     * @return The modified message action builder
     */
    public S from(final Message controlMessage) {
        this.messageBuilder = StaticMessageBuilder.withMessage(controlMessage);
        type(controlMessage.getType());
        return self;
    }

    /**
     * Sets a explicit message type for this message.
     * @param messageType
     * @return The modified message action builder
     */
    public S type(final MessageType messageType) {
        type(messageType.name());
        return self;
    }

    /**
     * Sets an explicit message type for this message.
     * @param messageType the type of the message indicates the content type (e.g. Xml, Json, binary).
     * @return The modified message action builder
     */
    public S type(final String messageType) {
        this.messageType = messageType;
        return self;
    }

    /**
     * Sets the payload data on the message builder implementation.
     * @param payloadBuilder
     * @return The modified message action builder
     */
    public S body(final MessagePayloadBuilder.Builder<?, ?> payloadBuilder) {
        body(payloadBuilder.build());
        return self;
    }

    /**
     * Sets the payload data on the message builder implementation.
     * @param payloadBuilder
     * @return The modified message action builder
     */
    public S body(final MessagePayloadBuilder payloadBuilder) {
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
     * @return The modified message action builder
     */
    public S body(final String payload) {
        body(new DefaultPayloadBuilder(payload));
        return self;
    }

    /**
     * Adds message payload resource to this builder.
     * @param payloadResource
     * @return The modified message action builder
     */
    public S body(final Resource payloadResource) {
        return body(payloadResource, FileUtils.getDefaultCharset());
    }

    /**
     * Adds message payload resource to this builder.
     * @param payloadResource
     * @param charset
     * @return The modified message action builder
     */
    public S body(final Resource payloadResource, final Charset charset) {
        try {
            body(FileUtils.readToString(payloadResource, charset));
        } catch (IOException e) {
            throw new CitrusRuntimeException("Failed to read payload resource", e);
        }
        return self;
    }

    /**
     * Adds message header name value pair to this builder's message.
     * @param name
     * @param value
     * @return The modified message action builder
     */
    public S header(final String name, final Object value) {
        if (messageBuilder instanceof WithHeaderBuilder) {
            ((WithHeaderBuilder) messageBuilder).addHeaderBuilder(new DefaultHeaderBuilder(Collections.singletonMap(name, value)));
        } else {
            throw new CitrusRuntimeException("Unable to set message header on builder type: " + messageBuilder.getClass());
        }
        return self;
    }

    /**
     * Adds message headers to this builder's message.
     * @param headers
     * @return The modified message action builder
     */
    public S headers(final Map<String, Object> headers) {
        if (messageBuilder instanceof WithHeaderBuilder) {
            ((WithHeaderBuilder) messageBuilder).addHeaderBuilder(new DefaultHeaderBuilder(headers));
        } else {
            throw new CitrusRuntimeException("Unable to set message header on builder type: " + messageBuilder.getClass());
        }
        return self;
    }

    /**
     * Adds message header data to this builder's message. Message header data is used in SOAP
     * messages for instance as header XML fragment.
     * @param data
     * @return The modified message action builder
     */
    public S header(final String data) {
        header(new DefaultHeaderDataBuilder(data));
        return self;
    }

    /**
     * Adds message header data builder to this builder's message. Message header data is used in
     * SOAP messages as XML fragment for instance.
     *
     * @param headerDataBuilder
     * @return The modified message action builder
     */
    public S header(final MessageHeaderDataBuilder headerDataBuilder) {
        if (messageBuilder instanceof WithHeaderBuilder) {
            ((WithHeaderBuilder) messageBuilder).addHeaderBuilder(headerDataBuilder);
        } else {
            throw new CitrusRuntimeException("Unable to set message header data on builder type: " + messageBuilder.getClass());
        }
        return self;
    }

    /**
     * Adds message header data as file resource to this builder's message. Message header data is used in SOAP
     * messages for instance as header XML fragment.
     * @param resource
     * @return The modified message action builder
     */
    public S header(final Resource resource) {
        return header(resource, FileUtils.getDefaultCharset());
    }

    /**
     * Adds message header data as file resource to this builder's message. Message header data is used in SOAP
     * messages for instance as header XML fragment.
     * @param resource
     * @param charset
     * @return The modified message action builder
     */
    public S header(final Resource resource, final Charset charset) {
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
     * Sets the message name.
     * @param name
     * @return The modified message action builder
     */
    public S name(final String name) {
        if (messageBuilder instanceof Named) {
            ((Named) messageBuilder).setName(name);
        } else {
            throw new CitrusRuntimeException("Unable to set message name on builder type: " + messageBuilder.getClass());
        }
        return self;
    }

    /**
     * Adds message processor on the message.
     * @param processor
     * @return The modified message action builder
     */
    public S process(MessageProcessor processor) {
        delegate.process(processor);
        return self;
    }

    /**
     * Adds message processor on the message as fluent builder.
     * @param builder
     * @return The modified message action builder
     */
    public S process(MessageProcessor.Builder<?, ?> builder) {
        return process(builder.build());
    }

    /**
     * Adds message processor on the message as fluent builder.
     * @param adapter
     * @return The modified message action builder
     */
    public S process(MessageProcessorAdapter adapter) {
        return process(adapter.asProcessor());
    }

    /**
     * Adds variable extractor on the message.
     * @param extractor
     * @return
     */
    public S extract(VariableExtractor extractor) {
        return process(extractor);
    }

    /**
     * Adds message processor on the message.
     * @param adapter
     * @return
     */
    public S extract(VariableExtractorAdapter adapter) {
        return extract(adapter.asExtractor());
    }

    /**
     * Adds message processor on the message as fluent builder.
     * @param builder
     * @return
     */
    public S extract(VariableExtractor.Builder<?, ?> builder) {
        return extract(builder.build());
    }

    /**
     * Sets explicit data dictionary for this action.
     * @param dictionary
     * @return The modified message action builder
     */
    public S dictionary(final DataDictionary<?> dictionary) {
        this.dataDictionary = dictionary;
        return self;
    }

    /**
     * Sets explicit data dictionary by name.
     * @param dictionaryName
     * @return The modified message action builder
     */
    public S dictionary(final String dictionaryName) {
        this.dataDictionaryName = dictionaryName;
        return self;
    }

    /**
     * Sets the bean reference resolver.
     *
     * @param referenceResolver
     */
    public S withReferenceResolver(final ReferenceResolver referenceResolver) {
        delegate.withReferenceResolver(referenceResolver);
        return self;
    }

    @Override
    public T build() {
        return delegate.build();
    }

    @Override
    public void setReferenceResolver(ReferenceResolver referenceResolver) {
        delegate.setReferenceResolver(referenceResolver);
    }

    public String getDataDictionaryName() {
        return dataDictionaryName;
    }

    public DataDictionary<?> getDataDictionary() {
        return dataDictionary;
    }

    public MessageBuilder getMessageBuilder() {
        return messageBuilder;
    }

    public String getMessageType() {
        return messageType;
    }

    /**
     * Basic message action builder provides settings on a message object and common message related operations such as
     * processors.
     * @param <T>
     * @param <M>
     * @param <B>
     */
    public static abstract class MessageActionBuilder<T extends TestAction, M extends MessageBuilderSupport<T, B, M>, B extends MessageActionBuilder<T, M, B>> extends AbstractTestActionBuilder<T, B>
            implements ReferenceResolverAware {

        protected Endpoint endpoint;
        protected String endpointUri;

        protected final List<VariableExtractor> variableExtractors = new ArrayList<>();
        protected final List<MessageProcessor> messageProcessors = new ArrayList<>();

        protected M messageBuilderSupport;

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
         * Construct the control message for this action.
         * @return
         */
        public M message() {
            return getMessageBuilderSupport();
        }

        /**
         * Sets the control message for this action.
         * @param messageBuilder
         * @return
         */
        public M message(MessageBuilder messageBuilder) {
            return getMessageBuilderSupport().from(messageBuilder);
        }

        /**
         * Builds message from given message.
         *
         * @param message
         * @return
         */
        public M message(final Message message) {
            return getMessageBuilderSupport().from(message);
        }

        /**
         * Adds message processor on the message.
         * @param processor
         * @return
         */
        public B transform(MessageProcessor processor) {
            return process(processor);
        }

        /**
         * Adds message processor on the message.
         * @param adapter
         * @return
         */
        public B transform(MessageProcessorAdapter adapter) {
            return process(adapter);
        }

        /**
         * Adds message processor on the message as fluent builder.
         * @param builder
         * @return
         */
        public B transform(MessageProcessor.Builder<?, ?> builder) {
            return transform(builder.build());
        }

        /**
         * Adds message processor on the message.
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
         * Adds message processor on the message as fluent builder.
         * @param builder
         * @return
         */
        public B process(MessageProcessor.Builder<?, ?> builder) {
            return process(builder.build());
        }

        /**
         * Adds message processor on the message as fluent builder.
         * @param adapter
         * @return
         */
        public B process(MessageProcessorAdapter adapter) {
            return process(adapter.asProcessor());
        }

        @Override
        public void setReferenceResolver(ReferenceResolver referenceResolver) {
            this.referenceResolver = referenceResolver;
        }

        public B withReferenceResolver(final ReferenceResolver referenceResolver) {
            this.referenceResolver = referenceResolver;
            return self;
        }

        public M getMessageBuilderSupport() {
            return messageBuilderSupport;
        }

        public Endpoint getEndpoint() {
            return endpoint;
        }

        public String getEndpointUri() {
            return endpointUri;
        }

        public List<VariableExtractor> getVariableExtractors() {
            return variableExtractors;
        }

        public List<MessageProcessor> getMessageProcessors() {
            return messageProcessors;
        }

        /**
         * Build method implemented by subclasses.
         * @return
         */
        protected abstract T doBuild();
    }
}
