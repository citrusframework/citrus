/*
 * Copyright the original author or authors.
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

package org.citrusframework.message.builder;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.citrusframework.AbstractTestActionBuilder;
import org.citrusframework.CitrusSettings;
import org.citrusframework.TestAction;
import org.citrusframework.TestActionBuilder;
import org.citrusframework.actions.MessageBuilderFactory;
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
import org.citrusframework.validation.builder.DefaultMessageBuilder;
import org.citrusframework.validation.builder.StaticMessageBuilder;
import org.citrusframework.variable.VariableExtractor;
import org.citrusframework.variable.VariableExtractorAdapter;
import org.citrusframework.variable.dictionary.DataDictionary;

import static java.util.Collections.singletonMap;
import static org.citrusframework.util.FileUtils.getDefaultCharset;
import static org.citrusframework.util.FileUtils.readToString;

public abstract class MessageBuilderSupport<T extends TestAction, B extends MessageBuilderSupport.MessageActionBuilder<T, S, B>, S extends MessageBuilderSupport<T, B, S>>
        implements TestActionBuilder<T>, ReferenceResolverAware, MessageBuilderFactory<T, S> {
    protected final S self;

    protected MessageBuilder messageBuilder = new DefaultMessageBuilder();

    protected final B delegate;

    protected String messageType = CitrusSettings.DEFAULT_MESSAGE_TYPE;

    /**
     * Set to true, if explicitly set. Can be used to distinguish from CitrusSettings.DEFAULT_MESSAGE_TYPE
     */
    private boolean isExplicitMessageType = false;

    protected DataDictionary<?> dataDictionary;
    protected String dataDictionaryName;

    protected MessageBuilderSupport(B delegate) {
        this.self = (S) this;
        this.delegate = delegate;
    }

    @Override
    public S from(final MessageBuilder messageBuilder) {
        this.messageBuilder = messageBuilder;
        return self;
    }

    @Override
    public S from(final Message controlMessage) {
        this.messageBuilder = StaticMessageBuilder.withMessage(controlMessage);
        type(controlMessage.getType());
        return self;
    }

    @Override
    public S type(final MessageType messageType) {
        type(messageType.name());
        return self;
    }

    @Override
    public S type(final String messageType) {
        this.messageType = messageType;
        isExplicitMessageType = true;
        return self;
    }

    @Override
    public S body(final MessagePayloadBuilder.Builder<?, ?> payloadBuilder) {
        body(payloadBuilder.build());
        return self;
    }

    @Override
    public S body(final MessagePayloadBuilder payloadBuilder) {
        if (messageBuilder instanceof WithPayloadBuilder withPayloadBuilder) {
            withPayloadBuilder.setPayloadBuilder(payloadBuilder);
        } else {
            throw new CitrusRuntimeException("Unable to set payload builder on message builder type: " + messageBuilder.getClass());
        }
        return self;
    }

    @Override
    public S body(final String payload) {
        body(new DefaultPayloadBuilder(payload));
        return self;
    }

    @Override
    public S body(final Resource payloadResource) {
        return body(payloadResource, getDefaultCharset());
    }

    @Override
    public S body(final Resource payloadResource, final Charset charset) {
        try {
            body(readToString(payloadResource, charset));
        } catch (IOException e) {
            throw new CitrusRuntimeException("Failed to read payload resource", e);
        }
        return self;
    }

    @Override
    public S header(final String name, final Object value) {
        if (messageBuilder instanceof WithHeaderBuilder withHeaderBuilder) {
            withHeaderBuilder.addHeaderBuilder(new DefaultHeaderBuilder(singletonMap(name, value)));
        } else {
            throw new CitrusRuntimeException("Unable to set message header on builder type: " + messageBuilder.getClass());
        }
        return self;
    }

    @Override
    public S headers(final Map<String, Object> headers) {
        if (messageBuilder instanceof WithHeaderBuilder withHeaderBuilder) {
            withHeaderBuilder.addHeaderBuilder(new DefaultHeaderBuilder(headers));
        } else {
            throw new CitrusRuntimeException("Unable to set message header on builder type: " + messageBuilder.getClass());
        }
        return self;
    }

    @Override
    public S header(final String data) {
        header(new DefaultHeaderDataBuilder(data));
        return self;
    }

    @Override
    public S header(final MessageHeaderDataBuilder headerDataBuilder) {
        if (messageBuilder instanceof WithHeaderBuilder withHeaderBuilder) {
            withHeaderBuilder.addHeaderBuilder(headerDataBuilder);
        } else {
            throw new CitrusRuntimeException("Unable to set message header data on builder type: " + messageBuilder.getClass());
        }
        return self;
    }

    @Override
    public S header(final Resource resource) {
        return header(resource, getDefaultCharset());
    }

    @Override
    public S header(final Resource resource, final Charset charset) {
        try {
            if (messageBuilder instanceof WithHeaderBuilder withHeaderBuilder) {
                withHeaderBuilder.addHeaderBuilder(new DefaultHeaderDataBuilder(readToString(resource, charset)));
            } else {
                throw new CitrusRuntimeException("Unable to set message header data on builder type: " + messageBuilder.getClass());
            }
        } catch (IOException e) {
            throw new CitrusRuntimeException("Failed to read header resource", e);
        }
        return self;
    }

    @Override
    public S name(final String name) {
        if (messageBuilder instanceof Named named) {
            named.setName(name);
        } else {
            throw new CitrusRuntimeException("Unable to set message name on builder type: " + messageBuilder.getClass());
        }
        return self;
    }

    @Override
    public S process(MessageProcessor processor) {
        delegate.process(processor);
        return self;
    }

    @Override
    public S process(MessageProcessor.Builder<?, ?> builder) {
        return process(builder.build());
    }

    @Override
    public S process(MessageProcessorAdapter adapter) {
        return process(adapter.asProcessor());
    }

    @Override
    public S extract(VariableExtractor extractor) {
        return process(extractor);
    }

    @Override
    public S extract(VariableExtractorAdapter adapter) {
        return extract(adapter.asExtractor());
    }

    @Override
    public S extract(VariableExtractor.Builder<?, ?> builder) {
        return extract(builder.build());
    }

    @Override
    public S dictionary(final DataDictionary<?> dictionary) {
        this.dataDictionary = dictionary;
        return self;
    }

    @Override
    public S dictionary(final String dictionaryName) {
        this.dataDictionaryName = dictionaryName;
        return self;
    }

    @Override
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
     * @return true if the messageType has been explicitly set (to distinguish from default)
     */
    public boolean isExplicitMessageType() {
        return isExplicitMessageType;
    }

    /**
     * Basic message action builder provides settings on a message object and common message related operations such as
     * processors.
     * @param <T>
     * @param <M>
     * @param <B>
     */
    public abstract static class MessageActionBuilder<T extends TestAction, M extends MessageBuilderSupport<T, B, M>, B extends MessageActionBuilder<T, M, B>> extends AbstractTestActionBuilder<T, B>
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
         * Adds message processor on the message as fluent builder.
         */
        public M extract(VariableExtractor.Builder<?, ?> builder) {
            return message().extract(builder);
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
            if (processor instanceof VariableExtractor variableExtractor) {
                this.variableExtractors.add(variableExtractor);
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
