/*
 * Copyright 2020 the original author or authors.
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

package com.consol.citrus.message.builder;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.consol.citrus.CitrusSettings;
import com.consol.citrus.TestActionBuilder;
import com.consol.citrus.actions.SendMessageAction;
import com.consol.citrus.common.Named;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.message.Message;
import com.consol.citrus.message.MessageBuilder;
import com.consol.citrus.message.MessageHeaderDataBuilder;
import com.consol.citrus.message.MessagePayloadBuilder;
import com.consol.citrus.message.MessageProcessor;
import com.consol.citrus.message.MessageProcessorAdapter;
import com.consol.citrus.message.MessageType;
import com.consol.citrus.message.WithHeaderBuilder;
import com.consol.citrus.message.WithPayloadBuilder;
import com.consol.citrus.spi.ReferenceResolver;
import com.consol.citrus.spi.ReferenceResolverAware;
import com.consol.citrus.util.FileUtils;
import com.consol.citrus.validation.ValidationContextAdapter;
import com.consol.citrus.validation.builder.DefaultMessageBuilder;
import com.consol.citrus.validation.builder.StaticMessageBuilder;
import com.consol.citrus.validation.context.ValidationContext;
import com.consol.citrus.variable.VariableExtractor;
import com.consol.citrus.variable.VariableExtractorAdapter;
import com.consol.citrus.variable.dictionary.DataDictionary;
import org.springframework.core.io.Resource;

/**
 * @author Christoph Deppisch
 */
public class SendMessageBuilderSupport<T extends SendMessageAction, B extends SendMessageAction.SendMessageActionBuilder<T, S, B>, S extends SendMessageBuilderSupport<T, B, S>>
        implements TestActionBuilder<T>, ReferenceResolverAware {
    protected final S self;

    private MessageBuilder messageBuilder = new DefaultMessageBuilder();

    private DataDictionary<?> dataDictionary;
    private String dataDictionaryName;

    protected boolean schemaValidation;
    protected String  schema;
    protected String  schemaRepository;

    private String messageType = CitrusSettings.DEFAULT_MESSAGE_TYPE;
    protected final B delegate;

    protected SendMessageBuilderSupport(B delegate) {
        this.self = (S) this;
        this.delegate = delegate;
    }

    /**
     * Sets the fork mode for this send action builder.
     * @param forkMode
     * @return The modified send message action builder
     */
    public S fork(boolean forkMode) {
        delegate.fork(forkMode);
        return self;
    }

    /**
     * Build message from given message builder.
     * @param messageBuilder
     * @return The modified send message action builder
     */
    public S from(final MessageBuilder messageBuilder) {
        this.messageBuilder = messageBuilder;
        return self;
    }

    /**
     * Build message from given message template.
     * @param controlMessage
     * @return The modified send message action builder
     */
    public S from(final Message controlMessage) {
        this.messageBuilder = StaticMessageBuilder.withMessage(controlMessage);
        type(controlMessage.getType());
        return self;
    }

    /**
     * Sets the message name.
     * @param name
     * @return The modified send message action builder
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
     * Sets the payload data on the message builder implementation.
     * @param payloadBuilder
     * @return The modified send message action builder
     */
    public S body(final MessagePayloadBuilder.Builder<?, ?> payloadBuilder) {
        body(payloadBuilder.build());
        return self;
    }

    /**
     * Sets the payload data on the message builder implementation.
     * @param payloadBuilder
     * @return The modified send message action builder
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
     * @return The modified send message action builder
     */
    public S body(final String payload) {
        body(new DefaultPayloadBuilder(payload));
        return self;
    }

    /**
     * Adds message payload resource to this builder.
     * @param payloadResource
     * @return The modified send message action builder
     */
    public S body(final Resource payloadResource) {
        return body(payloadResource, FileUtils.getDefaultCharset());
    }

    /**
     * Adds message payload resource to this builder.
     * @param payloadResource
     * @param charset
     * @return The modified send message action builder
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
     * Adds message header name value pair to this builder's message sending action.
     * @param name
     * @param value
     * @return The modified send message action builder
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
     * Adds message headers to this builder's message sending action.
     * @param headers
     * @return The modified send message action builder
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
     * Adds message header data to this builder's message sending action. Message header data is used in SOAP
     * messages for instance as header XML fragment.
     * @param data
     * @return The modified send message action builder
     */
    public S header(final String data) {
        header(new DefaultHeaderDataBuilder(data));
        return self;
    }

    /**
     * Adds message header data builder to this builder's message sending action. Message header data is used in
     * SOAP messages as XML fragment for instance.
     *
     * @param headerDataBuilder
     * @return The modified send message action builder
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
     * Adds message header data as file resource to this builder's message sending action. Message header data is used in SOAP
     * messages for instance as header XML fragment.
     * @param resource
     * @return The modified send message action builder
     */
    public S header(final Resource resource) {
        return header(resource, FileUtils.getDefaultCharset());
    }

    /**
     * Adds message header data as file resource to this builder's message sending action. Message header data is used in SOAP
     * messages for instance as header XML fragment.
     * @param resource
     * @param charset
     * @return The modified send message action builder
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
     * Sets a explicit message type for this send action.
     * @param messageType
     * @return The modified send message action builder
     */
    public S type(final MessageType messageType) {
        type(messageType.name());
        return self;
    }

    /**
     * Sets an explicit message type for this send action.
     * @param messageType the type of the message indicates the content type (e.g. Xml, Json, binary).
     * @return The modified send message action builder
     */
    public S type(final String messageType) {
        this.messageType = messageType;
        return self;
    }

    /**
     * Sets schema validation enabled/disabled for this message.
     *
     * @param enabled
     * @return
     */
    public S schemaValidation(final boolean enabled) {
        this.schemaValidation = enabled;
        return self;
    }

    /**
     * Get the is schema validation flag
     * @return the schema validation flag
     */
    public boolean isSchemaValidation() {
        return schemaValidation;
    }

    /**
     * Sets explicit schema instance name to use for schema validation.
     *
     * @param schemaName
     * @return
     */
    public S schema(final String schemaName) {
        this.schema = schemaName;
        return self;
    }

    /**
     * Get the schema
     * @return the schema
     */
    public String getSchema() {
        return schema;
    }

    /**
     * Sets explicit schema repository instance to use for validation.
     *
     * @param schemaRepository
     * @return
     */
    public S schemaRepository(final String schemaRepository) {
        this.schemaRepository = schemaRepository;
        return self;
    }

    /**
     * Get the schema repository
     * @return the schema-repository
     */
    public String getSchemaRepository() {
        return schemaRepository;
    }

    /**
     * Adds message processor on the message to be sent.
     * @param processor
     * @return The modified send message action builder
     */
    public S transform(MessageProcessor processor) {
        return process(processor);
    }

    /**
     * Adds message processor on the message to be sent as fluent builder.
     * @param builder
     * @return The modified send message action builder
     */
    public S transform(MessageProcessor.Builder<?, ?> builder) {
        return transform(builder.build());
    }

    /**
     * Adds message processor on the message to be sent.
     * @param processor
     * @return The modified send message action builder
     */
    public S process(MessageProcessor processor) {
        delegate.process(processor);
        return self;
    }

    /**
     * Adds message processor on the message to be sent as fluent builder.
     * @param builder
     * @return The modified send message action builder
     */
    public S process(MessageProcessor.Builder<?, ?> builder) {
        return process(builder.build());
    }

    /**
     * Adds message processor on the message to be sent as fluent builder.
     * @param adapter
     * @return The modified send message action builder
     */
    public S process(MessageProcessorAdapter adapter) {
        return process(adapter.asProcessor());
    }

    /**
     * Adds variable extractor on the received message.
     * @param extractor
     * @return
     */
    public S extract(VariableExtractor extractor) {
        return process(extractor);
    }

    /**
     * Adds message processor on the received message.
     * @param adapter
     * @return
     */
    public S extract(VariableExtractorAdapter adapter) {
        return extract(adapter.asExtractor());
    }

    /**
     * Adds message processor on the received message as fluent builder.
     * @param builder
     * @return
     */
    public S extract(VariableExtractor.Builder<?, ?> builder) {
        return extract(builder.build());
    }

    /**
     * Sets explicit data dictionary for this receive action.
     * @param dictionary
     * @return The modified send message action builder
     */
    public S dictionary(final DataDictionary<?> dictionary) {
        this.dataDictionary = dictionary;
        return self;
    }

    /**
     * Sets explicit data dictionary by name.
     * @param dictionaryName
     * @return The modified send message action builder
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
}
