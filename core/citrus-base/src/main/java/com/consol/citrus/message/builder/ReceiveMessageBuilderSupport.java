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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.consol.citrus.CitrusSettings;
import com.consol.citrus.TestActionBuilder;
import com.consol.citrus.actions.ReceiveMessageAction;
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
import com.consol.citrus.validation.HeaderValidator;
import com.consol.citrus.validation.MessageValidator;
import com.consol.citrus.validation.ValidationContextAdapter;
import com.consol.citrus.validation.ValidationProcessor;
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
public class ReceiveMessageBuilderSupport<T extends ReceiveMessageAction, B extends ReceiveMessageAction.ReceiveMessageActionBuilder<T, S, B>, S extends ReceiveMessageBuilderSupport<T, B, S>>
        implements TestActionBuilder<T>, ReferenceResolverAware {
    private final S self;

    private MessageBuilder messageBuilder = new DefaultMessageBuilder();

    private final List<MessageProcessor> controlMessageProcessors = new ArrayList<>();
    private DataDictionary<?> dataDictionary;
    private String dataDictionaryName;

    private String messageType = CitrusSettings.DEFAULT_MESSAGE_TYPE;

    private boolean headerNameIgnoreCase = false;

    protected final B delegate;

    protected ReceiveMessageBuilderSupport(B delegate) {
        this.self = (S) this;
        this.delegate = delegate;
    }

    /**
     * Adds a custom timeout to this message receiving action.
     *
     * @param receiveTimeout
     * @return The modified receive message action builder
     */
    public S timeout(final long receiveTimeout) {
        delegate.timeout(receiveTimeout);
        return self;
    }

    /**
     * Build message from given message builder.
     * @param messageBuilder
     * @return The modified receive message action builder
     */
    public S from(final MessageBuilder messageBuilder) {
        this.messageBuilder = messageBuilder;
        return self;
    }

    /**
     * Build message from given message template.
     * @param controlMessage
     * @return The modified receive message action builder
     */
    public S from(final Message controlMessage) {
        this.messageBuilder = StaticMessageBuilder.withMessage(controlMessage);
        type(controlMessage.getType());
        return self;
    }

    /**
     * Sets the message name.
     * @param name
     * @return The modified receive message action builder
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
     * Expect this message payload data in received message.
     *
     * @param payloadBuilder
     * @return The modified receive message action builder
     */
    public S body(final MessagePayloadBuilder.Builder<?, ?> payloadBuilder) {
        body(payloadBuilder.build());
        return self;
    }

    /**
     * Expect this message payload data in received message.
     *
     * @param payloadBuilder
     * @return The modified receive message action builder
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
     * Expect this message payload data in received message.
     *
     * @param payload
     * @return The modified receive message action builder
     */
    public S body(final String payload) {
        body(new DefaultPayloadBuilder(payload));
        return self;
    }

    /**
     * Expect this message payload data in received message.
     *
     * @param payloadResource
     * @return The modified receive message action builder
     */
    public S body(final Resource payloadResource) {
        return body(payloadResource, FileUtils.getDefaultCharset());
    }

    /**
     * Expect this message payload data in received message.
     *
     * @param payloadResource
     * @param charset
     * @return The modified receive message action builder
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
     * Expect this message header entry in received message.
     *
     * @param name
     * @param value
     * @return The modified receive message action builder
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
     * Expect this message header entries in received message.
     *
     * @param headers
     * @return The modified receive message action builder
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
     * Expect this message header data in received message. Message header data is used in
     * SOAP messages as XML fragment for instance.
     *
     * @param data
     * @return The modified receive message action builder
     */
    public S header(final String data) {
        header(new DefaultHeaderDataBuilder(data));
        return self;
    }

    /**
     * Expect this message header data in received message. Message header data is used in
     * SOAP messages as XML fragment for instance.
     *
     * @param headerDataBuilder
     * @return The modified receive message action builder
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
     * Expect this message header data in received message from file resource. Message header data is used in
     * SOAP messages as XML fragment for instance.
     *
     * @param resource
     * @return The modified receive message action builder
     */
    public S header(final Resource resource) {
        return header(resource, FileUtils.getDefaultCharset());
    }

    /**
     * Expect this message header data in received message from file resource. Message header data is used in
     * SOAP messages as XML fragment for instance.
     *
     * @param resource
     * @param charset
     * @return The modified receive message action builder
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
     * Validate header names with case insensitive keys.
     *
     * @param value
     * @return The modified receive message action builder
     */
    public S headerNameIgnoreCase(final boolean value) {
        this.headerNameIgnoreCase = value;
        return self;
    }

    /**
     * Sets a explicit message type for this receive action.
     *
     * @param messageType
     * @return The modified receive message action builder
     */
    public S type(final MessageType messageType) {
        type(messageType.name());
        return self;
    }

    /**
     * Sets a explicit message type for this receive action.
     * @param messageType the type of the message indicates the content type (e.g. Xml, Json, binary).
     * @return The modified receive message action builder
     */
    public S type(final String messageType) {
        this.messageType = messageType;
        return self;
    }

    /**
     * Adds a validation context.
     * @param validationContext
     * @return The modified receive message action builder
     */
    public S validate(final ValidationContext.Builder<?, ?> validationContext) {
        delegate.validate(validationContext);
        return self;
    }

    /**
     * Adds a validation context.
     * @param validationContext
     * @return The modified receive message action builder
     */
    public S validate(final ValidationContext validationContext) {
        return validate((ValidationContext.Builder) () -> validationContext);
    }

    /**
     * Adds a validation context.
     * @param adapter
     * @return The modified receive message action builder
     */
    public S validate(final ValidationContextAdapter adapter) {
        return validate(adapter.asValidationContext());
    }

    /**
     * Sets validation contexts.
     * @param validationContexts
     * @return The modified receive message action builder
     */
    public S validate(final List<ValidationContext.Builder<?, ?>> validationContexts) {
        delegate.validate(validationContexts);
        return self;
    }

    /**
     * Sets validation contexts.
     * @param validationContexts
     * @return The modified receive message action builder
     */
    public S validate(ValidationContext.Builder<?, ?> ... validationContexts) {
        return validate(Arrays.asList(validationContexts));
    }

    /**
     * Sets message selector string.
     *
     * @param messageSelector
     * @return The modified receive message action builder
     */
    public S selector(final String messageSelector) {
        delegate.selector(messageSelector);
        return self;
    }

    /**
     * Sets message selector elements.
     *
     * @param messageSelector
     * @return The modified receive message action builder
     */
    public S selector(final Map<String, String> messageSelector) {
        delegate.selector(messageSelector);
        return self;
    }

    /**
     * Sets explicit message validators for this receive action.
     *
     * @param validator
     * @return The modified receive message action builder
     */
    public S validator(final MessageValidator<? extends ValidationContext> validator) {
        delegate.validator(validator);
        return self;
    }

    /**
     * Sets explicit message validators for this receive action.
     *
     * @param validators
     * @return The modified receive message action builder
     */
    @SafeVarargs
    public final S validators(final MessageValidator<? extends ValidationContext>... validators) {
        return validators(Arrays.asList(validators));
    }

    /**
     * Sets explicit message validators for this receive action.
     *
     * @param validators
     * @return The modified receive message action builder
     */
    public S validators(final List<MessageValidator<? extends ValidationContext>> validators) {
        delegate.validators(validators);
        return self;
    }

    /**
     * Sets explicit message validators by name.
     *
     * @param validatorNames
     * @return The modified receive message action builder
     */
    public S validator(final String... validatorNames) {
        delegate.validator(validatorNames);
        return self;
    }

    /**
     * Sets explicit header validator for this receive action.
     *
     * @param validators
     * @return The modified receive message action builder
     */
    public S validator(final HeaderValidator... validators) {
        delegate.validator(validators);
        return self;
    }

    /**
     * Sets explicit data dictionary for this receive action.
     * @param dictionary
     * @return The modified receive message action builder
     */
    public S dictionary(final DataDictionary<?> dictionary) {
        this.dataDictionary = dictionary;
        return self;
    }

    /**
     * Sets explicit data dictionary by name.
     * @param dictionaryName
     * @return The modified receive message action builder
     */
    public S dictionary(final String dictionaryName) {
        this.dataDictionaryName = dictionaryName;
        return self;
    }

    /**
     * Adds validation processor to the receive action for validating
     * the received message with Java code.
     *
     * @param processor
     * @return The modified receive message action builder
     */
    public B validate(final ValidationProcessor processor) {
        return delegate.validate(processor);
    }

    /**
     * Adds message processor on the control message.
     * @param processor
     * @return The modified receive message action builder
     */
    public S process(MessageProcessor processor) {
        if (processor instanceof VariableExtractor) {
            delegate.process(processor);
        } else {
            this.controlMessageProcessors.add(processor);
        }
        return self;
    }

    /**
     * Adds message processor on the control message as fluent builder.
     * @param builder
     * @return The modified receive message action builder
     */
    public S process(MessageProcessor.Builder<?, ?> builder) {
        return process(builder.build());
    }

    /**
     * Adds message processor on the control message as fluent builder.
     * @param adapter
     * @return The modified receive message action builder
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
     * Sets the bean reference resolver.
     *
     * @param referenceResolver
     * @return The modified receive message action builder
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

    public List<MessageProcessor> getControlMessageProcessors() {
        return controlMessageProcessors;
    }

    public String getMessageType() {
        return messageType;
    }

    public boolean isHeaderNameIgnoreCase() {
        return headerNameIgnoreCase;
    }
}
