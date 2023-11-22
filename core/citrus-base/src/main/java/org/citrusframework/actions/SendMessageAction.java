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

package org.citrusframework.actions;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.citrusframework.CitrusSettings;
import org.citrusframework.Completable;
import org.citrusframework.context.TestContext;
import org.citrusframework.endpoint.Endpoint;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.message.Message;
import org.citrusframework.message.MessageBuilder;
import org.citrusframework.message.MessageDirection;
import org.citrusframework.message.MessageProcessor;
import org.citrusframework.message.MessageType;
import org.citrusframework.message.builder.MessageBuilderSupport;
import org.citrusframework.message.builder.SendMessageBuilderSupport;
import org.citrusframework.util.IsJsonPredicate;
import org.citrusframework.util.IsXmlPredicate;
import org.citrusframework.validation.SchemaValidator;
import org.citrusframework.validation.context.SchemaValidationContext;
import org.citrusframework.validation.json.JsonMessageValidationContext;
import org.citrusframework.validation.xml.XmlMessageValidationContext;
import org.citrusframework.variable.VariableExtractor;
import org.citrusframework.variable.dictionary.DataDictionary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.citrusframework.util.StringUtils;


/**
 * This action sends a messages to a specified message endpoint. The action holds a reference to
 * a {@link org.citrusframework.endpoint.Endpoint}, which is capable of the message transport implementation. So action is
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

    /**
     * Should message be validated with its schema definition
     */
    private final boolean schemaValidation;

    /** Explicit schema repository to use for this validation */
    private final String schemaRepository;

    /** Explicit schema instance to use for this validation */
    private final String schema;

    /** List of variable extractors responsible for creating variables from received message content */
    private final List<VariableExtractor> variableExtractors;

    /** List of message processors responsible for manipulating message to be sent */
    private final List<MessageProcessor> messageProcessors;

    /** Builder constructing a control message */
    private final MessageBuilder messageBuilder;

    /** Forks the message sending action so other actions can take place while this
     * message sender is waiting for the synchronous response */
    private final boolean forkMode;

    /** The message type to send in this action - this information is needed to find proper
     * message construction processors for this message */
    private final String messageType;

    /** Optional data dictionary that explicitly modifies message content before sending */
    private final DataDictionary<?> dataDictionary;

    /** Finished indicator either called when forked send action is finished or immediately when this action has finished */
    private CompletableFuture<TestContext> finished;

    /** Logger */
    private static final Logger logger = LoggerFactory.getLogger(SendMessageAction.class);

    /**
     * Default constructor.
     */
    public SendMessageAction(SendMessageActionBuilder<?, ?, ?> builder) {
        super("send", builder);

        this.forkMode = builder.forkMode;
        this.endpoint = builder.getEndpoint();
        this.endpointUri = builder.getEndpointUri();
        this.schemaValidation = builder.getMessageBuilderSupport().isSchemaValidation();
        this.schema = builder.getMessageBuilderSupport().getSchema();
        this.schemaRepository = builder.getMessageBuilderSupport().getSchemaRepository();
        this.variableExtractors = builder.getVariableExtractors();
        this.messageProcessors = builder.getMessageProcessors();
        this.messageBuilder = builder.getMessageBuilderSupport().getMessageBuilder();
        this.messageType = builder.getMessageBuilderSupport().getMessageType();
        this.dataDictionary = builder.getMessageBuilderSupport().getDataDictionary();
    }

    /**
     * Message is constructed with payload and header entries and sent via
     * {@link org.citrusframework.endpoint.Endpoint} instance.
     */
    @Override
    public void doExecute(final TestContext context) {
        final Message message = createMessage(context, messageType);
        finished = new CompletableFuture<>();

        finished.whenComplete((ctx, ex) -> {
            if (ex != null) {
                logger.warn("Failure in forked send action: " + ex.getMessage());
            } else {
                for (Exception ctxEx : ctx.getExceptions()) {
                    logger.warn(ctxEx.getMessage());
                }
            }
        });

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
            logger.debug("Forking message sending action ...");

            ExecutorService taskExecutor = Executors.newSingleThreadExecutor();
            taskExecutor.execute(() -> {
                try {
                    validateMessage(message, context);
                    messageEndpoint.createProducer().send(message, context);
                } catch (Exception e) {
                    if (e instanceof CitrusRuntimeException runtimeEx) {
                        context.addException(runtimeEx);
                    } else {
                        context.addException(new CitrusRuntimeException(e));
                    }
                } finally {
                    finished.complete(context);
                }
            });
        } else {
            try {
                validateMessage(message, context);
                messageEndpoint.createProducer().send(message, context);
            } finally {
                finished.complete(context);
            }
        }
    }

    /**
     * Validate the message against registered schemas.
     * @param message
     */
    protected void validateMessage(Message message, TestContext context) {
        List<SchemaValidator<? extends SchemaValidationContext>> schemaValidators = null;
        SchemaValidationContext validationContext = null;
        String  payload = message.getPayload(String.class);

        if ((isSchemaValidation() || isJsonSchemaValidationEnabled()) && IsJsonPredicate.getInstance().test(payload)) {
            schemaValidators = context.getMessageValidatorRegistry()
                    .findSchemaValidators(MessageType.JSON.name(), message);
            validationContext = JsonMessageValidationContext.Builder.json()
                    .schemaValidation(this.schemaValidation)
                    .schema(this.schema)
                    .schemaRepository(this.schemaRepository).build();
        } else if ((isSchemaValidation() || isXmlSchemaValidationEnabled()) && IsXmlPredicate.getInstance().test(payload)) {
            schemaValidators = context.getMessageValidatorRegistry()
                    .findSchemaValidators(MessageType.XML.name(), message);
            validationContext = XmlMessageValidationContext.Builder.xml()
                    .schemaValidation(this.schemaValidation)
                    .schema(this.schema)
                    .schemaRepository(this.schemaRepository).build();
        }

        if (schemaValidators != null) {
            for (SchemaValidator validator : schemaValidators) {
                validator.validate(message, context, validationContext);
            }
        }

    }

    /**
     * Get setting to determine if json schema validation is enabled by default.
     * @return
     */
    private static boolean isJsonSchemaValidationEnabled() {
        return Boolean.getBoolean(CitrusSettings.OUTBOUND_SCHEMA_VALIDATION_ENABLED_PROPERTY)
                || Boolean.getBoolean(CitrusSettings.OUTBOUND_JSON_SCHEMA_VALIDATION_ENABLED_PROPERTY)
                || Boolean.parseBoolean(System.getenv(CitrusSettings.OUTBOUND_SCHEMA_VALIDATION_ENABLED_ENV))
                || Boolean.parseBoolean(System.getenv(CitrusSettings.OUTBOUND_JSON_SCHEMA_VALIDATION_ENABLED_ENV));
    }

    /**
     * Get setting to determine if xml schema validation is enabled by default.
     * @return
     */
    private static boolean isXmlSchemaValidationEnabled() {
        return Boolean.getBoolean(CitrusSettings.OUTBOUND_SCHEMA_VALIDATION_ENABLED_PROPERTY)
                || Boolean.getBoolean(CitrusSettings.OUTBOUND_XML_SCHEMA_VALIDATION_ENABLED_PROPERTY)
                || Boolean.parseBoolean(System.getenv(CitrusSettings.OUTBOUND_SCHEMA_VALIDATION_ENABLED_ENV))
                || Boolean.parseBoolean(System.getenv(CitrusSettings.OUTBOUND_XML_SCHEMA_VALIDATION_ENABLED_ENV));
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
                .orElseGet(() -> isDisabled(context));
    }

    /**
     * Create message to be sent.
     * @param context
     * @param messageType
     * @return
     */
    protected Message createMessage(TestContext context, String messageType) {
        Message message = messageBuilder.build(context, messageType);

        if (message.getPayload() != null) {
            context.getMessageProcessors(MessageDirection.OUTBOUND)
                    .forEach(processor -> processor.process(message, context));

            if (dataDictionary != null) {
                dataDictionary.process(message, context);
            }

            messageProcessors.forEach(processor -> processor.process(message, context));
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
     * Get
     * @return true if schema validation is active for this message
     */
    public boolean isSchemaValidation() {
        return schemaValidation;
    }

    /**
     * Get the name of the schema repository used for validation
     * @return the schema repository name
     */
    public String getSchemaRepository() {
        return schemaRepository;
    }

    /**
     * Get the name of the schema used for validation
     * @return the schema
     */
    public String getSchema() {
        return schema;
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
    public MessageBuilder getMessageBuilder() {
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
    public static final class Builder extends SendMessageActionBuilder<SendMessageAction, SendMessageActionBuilderSupport, Builder> {

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
        public SendMessageActionBuilderSupport getMessageBuilderSupport() {
            if (messageBuilderSupport == null) {
                messageBuilderSupport = new SendMessageActionBuilderSupport(self);
            }
            return super.getMessageBuilderSupport();
        }

        @Override
        public SendMessageAction doBuild() {
            return new SendMessageAction(this);
        }

    }

    public static class SendMessageActionBuilderSupport extends SendMessageBuilderSupport<SendMessageAction, SendMessageAction.Builder, SendMessageAction.SendMessageActionBuilderSupport> {

        public SendMessageActionBuilderSupport(SendMessageAction.Builder delegate) {
            super(delegate);
        }
    }

    /**
     * Base send message action builder also used by subclasses of base send message action.
     */
    public static abstract class SendMessageActionBuilder<T extends SendMessageAction, M extends SendMessageBuilderSupport<T, B, M>, B extends SendMessageActionBuilder<T, M, B>>
            extends MessageBuilderSupport.MessageActionBuilder<T, M, B> {

        protected boolean forkMode = false;
        protected CompletableFuture<Void> finished;

        /**
         * Sets the fork mode for this send action builder.
         * @param forkMode
         * @return
         */
        public B fork(boolean forkMode) {
            this.forkMode = forkMode;
            return self;
        }

        @Override
        public final T build() {
            if (messageBuilderSupport == null) {
                messageBuilderSupport = getMessageBuilderSupport();
            }

            if (referenceResolver != null) {
                if (messageBuilderSupport.getDataDictionaryName() != null) {
                    this.messageBuilderSupport.dictionary(
                            referenceResolver.resolve(messageBuilderSupport.getDataDictionaryName(), DataDictionary.class));
                }
            }

            return doBuild();
        }
    }
}
