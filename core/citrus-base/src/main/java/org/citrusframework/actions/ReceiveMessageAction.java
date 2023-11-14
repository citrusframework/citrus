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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.citrusframework.context.TestContext;
import org.citrusframework.endpoint.Endpoint;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.message.Message;
import org.citrusframework.message.MessageBuilder;
import org.citrusframework.message.MessageDirection;
import org.citrusframework.message.MessagePayloadBuilder;
import org.citrusframework.message.MessagePayloadUtils;
import org.citrusframework.message.MessageProcessor;
import org.citrusframework.message.MessageSelectorBuilder;
import org.citrusframework.message.WithPayloadBuilder;
import org.citrusframework.message.builder.DefaultPayloadBuilder;
import org.citrusframework.message.builder.MessageBuilderSupport;
import org.citrusframework.message.builder.ReceiveMessageBuilderSupport;
import org.citrusframework.messaging.Consumer;
import org.citrusframework.messaging.SelectiveConsumer;
import org.citrusframework.spi.ReferenceResolverAware;
import org.citrusframework.util.StringUtils;
import org.citrusframework.validation.DefaultMessageHeaderValidator;
import org.citrusframework.validation.HeaderValidator;
import org.citrusframework.validation.MessageValidator;
import org.citrusframework.validation.ValidationContextAdapter;
import org.citrusframework.validation.ValidationProcessor;
import org.citrusframework.validation.builder.StaticMessageBuilder;
import org.citrusframework.validation.context.HeaderValidationContext;
import org.citrusframework.validation.context.ValidationContext;
import org.citrusframework.validation.json.JsonMessageValidationContext;
import org.citrusframework.validation.json.JsonPathMessageValidationContext;
import org.citrusframework.validation.script.ScriptValidationContext;
import org.citrusframework.validation.xml.XmlMessageValidationContext;
import org.citrusframework.validation.xml.XpathMessageValidationContext;
import org.citrusframework.variable.VariableExtractor;
import org.citrusframework.variable.dictionary.DataDictionary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This action receives messages from a service destination. Action uses a {@link org.citrusframework.endpoint.Endpoint}
 * to receive the message, this means that this action is independent of any message transport.
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
    private final MessageBuilder messageBuilder;

    /** MessageValidator responsible for message validation */
    private final List<MessageValidator<? extends ValidationContext>> validators;

    /** Optional data dictionary that explicitly modifies message content before validation */
    private final DataDictionary<?> dataDictionary;

    /** Callback able to additionally validate received message */
    private final ValidationProcessor validationProcessor;

    /** List of validation contexts for this receive action */
    private final List<ValidationContext> validationContexts;

    /** List of variable extractors responsible for creating variables from received message content */
    private final List<VariableExtractor> variableExtractors;

    /** List of processors that handle the received message */
    private final List<MessageProcessor> messageProcessors;

    /** List of processors that handle the control message builder */
    private final List<MessageProcessor> controlMessageProcessors;

    /** The expected message type to arrive in this receive action - this information is needed to find a proper
     * message validator for this message */
    private final String messageType;

    /** Logger */
    private static final Logger logger = LoggerFactory.getLogger(ReceiveMessageAction.class);

    /**
     * Default constructor.
     */
    public ReceiveMessageAction(ReceiveMessageActionBuilder<?, ?, ?> builder) {
        super("receive", builder);

        this.endpoint = builder.getEndpoint();
        this.endpointUri = builder.getEndpointUri();
        this.receiveTimeout = builder.receiveTimeout;
        this.messageSelector = builder.messageSelector;
        this.messageSelectorMap = builder.messageSelectorMap;
        this.validators = builder.validators;
        this.validationProcessor = builder.validationProcessor;
        this.validationContexts = builder.getValidationContexts();
        this.variableExtractors = builder.getVariableExtractors();
        this.messageProcessors = builder.getMessageProcessors();

        this.messageBuilder = builder.getMessageBuilderSupport().getMessageBuilder();
        this.dataDictionary = builder.getMessageBuilderSupport().getDataDictionary();
        this.controlMessageProcessors = builder.getMessageBuilderSupport().getControlMessageProcessors();
        this.messageType = builder.getMessageBuilderSupport().getMessageType();
    }

    /**
     * Method receives a message via {@link org.citrusframework.endpoint.Endpoint} instance
     * constructs a validation context and starts the message validation
     * via {@link MessageValidator}.
     */
    @Override
    public void doExecute(TestContext context) {
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
        if (logger.isDebugEnabled()) {
            logger.debug("Setting message selector: '" + selectorString + "'");
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
            logger.warn(String.format("Unable to receive selective with consumer implementation: '%s'", consumer.getClass()));
            return receive(context);
        }
    }

    /**
     * Override this message if you want to add additional message validation
     * @param message
     */
    protected void validateMessage(Message message, TestContext context) {
        messageProcessors.forEach(processor -> processor.process(message, context));

        if (logger.isDebugEnabled()) {
            logger.debug("Received message:\n" + message.print(context));
        }

        // extract variables from received message content
        for (VariableExtractor variableExtractor : variableExtractors) {
            variableExtractor.extractVariables(message, context);
        }

        Message controlMessage = createControlMessage(context, messageType);
        if (StringUtils.hasText(controlMessage.getName())) {
            context.getMessageStore().storeMessage(controlMessage.getName(), message);
        } else {
            context.getMessageStore().storeMessage(context.getMessageStore().constructMessageName(this, getOrCreateEndpoint(context)), message);
        }

        if (validationProcessor != null) {
            validationProcessor.validate(message, context);
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("Control message:\n" + controlMessage.print(context));
            }

            if (!validators.isEmpty()) {
                for (MessageValidator<? extends ValidationContext> messageValidator : validators) {
                    messageValidator.validateMessage(message, controlMessage, context, validationContexts);
                }

                if (validators.parallelStream()
                        .map(Object::getClass)
                        .noneMatch(DefaultMessageHeaderValidator.class::isAssignableFrom)) {
                    MessageValidator<?extends ValidationContext> defaultMessageHeaderValidator = context.getMessageValidatorRegistry().getDefaultMessageHeaderValidator();
                    if (defaultMessageHeaderValidator != null) {
                        defaultMessageHeaderValidator.validateMessage(message, controlMessage, context, validationContexts);
                    }
                }
            } else {
                boolean mustFindValidator = validationContexts.stream()
                        .anyMatch(item -> JsonPathMessageValidationContext.class.isAssignableFrom(item.getClass()) ||
                                XpathMessageValidationContext.class.isAssignableFrom(item.getClass()) ||
                                ScriptValidationContext.class.isAssignableFrom(item.getClass()));

                List<MessageValidator<? extends ValidationContext>> validators =
                        context.getMessageValidatorRegistry().findMessageValidators(messageType, message, mustFindValidator);

                for (MessageValidator<? extends ValidationContext> messageValidator : validators) {
                    messageValidator.validateMessage(message, controlMessage, context, validationContexts);
                }
            }
        }
    }

    /**
     * Create control message that is expected. Apply global and local message processors and data dictionaries.
     * @param context
     * @param messageType
     * @return
     */
    protected Message createControlMessage(TestContext context, String messageType) {
        Message message = messageBuilder.build(context, messageType);

        if (message.getPayload() != null) {
            context.getMessageProcessors(MessageDirection.INBOUND)
                    .forEach(processor -> processor.process(message, context));

            if (dataDictionary != null) {
                dataDictionary.process(message, context);
            }

            controlMessageProcessors.forEach(processor -> processor.process(message, context));
        }

        return message;
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
     * Obtains the message processors.
     * @return
     */
    public List<MessageProcessor> getMessageProcessors() {
        return messageProcessors;
    }

    /**
     * Obtains the control message processors.
     * @return
     */
    public List<MessageProcessor> getControlMessageProcessors() {
        return controlMessageProcessors;
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
     * Gets the validationProcessor.
     * @return the validationProcessor to get.
     */
    public ValidationProcessor getValidationProcessor() {
        return validationProcessor;
    }

    /**
     * Gets the data dictionary.
     * @return
     */
    public DataDictionary<?> getDataDictionary() {
        return dataDictionary;
    }

    /**
     * Gets the messageBuilder.
     * @return the messageBuilder
     */
    public MessageBuilder getMessageBuilder() {
        return messageBuilder;
    }

    /**
     * Action builder.
     */
    public static final class Builder extends ReceiveMessageActionBuilder<ReceiveMessageAction, ReceiveMessageActionBuilderSupport, Builder> {

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
        public ReceiveMessageActionBuilderSupport getMessageBuilderSupport() {
            if (messageBuilderSupport == null) {
                messageBuilderSupport = new ReceiveMessageActionBuilderSupport(self);
            }
            return super.getMessageBuilderSupport();
        }

        @Override
        public ReceiveMessageAction doBuild() {
            return new ReceiveMessageAction(this);
        }
    }

    public static class ReceiveMessageActionBuilderSupport extends ReceiveMessageBuilderSupport<ReceiveMessageAction, Builder, ReceiveMessageActionBuilderSupport> {

        public ReceiveMessageActionBuilderSupport(Builder delegate) {
            super(delegate);
        }
    }

    public static abstract class ReceiveMessageActionBuilder<T extends ReceiveMessageAction, M extends ReceiveMessageBuilderSupport<T, B, M>, B extends ReceiveMessageActionBuilder<T, M, B>>
            extends MessageBuilderSupport.MessageActionBuilder<T, M, B> {

        private long receiveTimeout = 0L;

        private final Map<String, Object> messageSelectorMap = new HashMap<>();
        private String messageSelector;

        private final List<MessageValidator<? extends ValidationContext>> validators = new ArrayList<>();
        private ValidationProcessor validationProcessor;
        private final List<ValidationContext.Builder<?, ?>> validationContexts = new ArrayList<>();

        /** Validation context used in this action builder */
        private HeaderValidationContext headerValidationContext;

        private final List<String> validatorNames = new ArrayList<>();

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
         * Adds a validation context.
         * @param validationContext
         * @return
         */
        public B validate(final ValidationContext.Builder<?, ?> validationContext) {
            this.validationContexts.add(validationContext);
            return self;
        }

        /**
         * Adds a validation context.
         * @param validationContext
         * @return
         */
        public B validate(final ValidationContext validationContext) {
            return validate((ValidationContext.Builder) () -> validationContext);
        }

        /**
         * Adds an expression based validation context.
         * @param adapter
         * @return
         */
        public B validate(final ValidationContextAdapter adapter) {
            return validate(adapter.asValidationContext());
        }

        /**
         * Sets validation contexts.
         * @param validationContexts
         * @return
         */
        public B validate(final List<ValidationContext.Builder<?, ?>> validationContexts) {
            this.validationContexts.addAll(validationContexts);
            return self;
        }

        /**
         * Sets validation contexts.
         * @param validationContexts
         * @return
         */
        public B validate(ValidationContext.Builder<?, ?> ... validationContexts) {
            return validate(Arrays.asList(validationContexts));
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
        public final B validators(final String... validators) {
            Arrays.stream(validators).forEach(this::validator);
            return self;
        }

        /**
         * Sets explicit message validators for this receive action.
         *
         * @param validators
         * @return
         */
        @SafeVarargs
        public final B validators(final MessageValidator<? extends ValidationContext>... validators) {
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
         * Sets explicit message validators for this receive action.
         *
         * @param validators
         * @return
         */
        public final B validators(final HeaderValidator... validators) {
            Stream.of(validators).forEach(this::validator);
            return self;
        }

        /**
         * Sets explicit message validator by name.
         *
         * @param validatorName
         * @return
         */
        @SuppressWarnings("unchecked")
        public B validator(final String validatorName) {
            this.validatorNames.add(validatorName);
            return self;
        }

        /**
         * Sets explicit header validator for this receive action.
         *
         * @param validators
         * @return
         */
        public B validator(final HeaderValidator validators) {
            Stream.of(validators).forEach(getHeaderValidationContext()::addHeaderValidator);
            return self;
        }

        /**
         * Adds validation processor to the receive action for validating
         * the received message with Java code.
         *
         * @param processor
         * @return
         */
        public B validate(final ValidationProcessor processor) {
            this.validationProcessor = processor;
            return self;
        }

        @Override
        public B process(MessageProcessor processor) {
            if (processor instanceof VariableExtractor) {
                this.variableExtractors.add((VariableExtractor) processor);
            } else if (processor instanceof ValidationProcessor) {
                validate((ValidationProcessor) processor);
            } else {
                this.messageProcessors.add(processor);
            }

            return self;
        }

        @Override
        public final T build() {
            if (messageBuilderSupport == null) {
                messageBuilderSupport = getMessageBuilderSupport();
            }

            reconcileValidationContexts();

            if (referenceResolver != null) {
                if (validationProcessor != null &&
                        validationProcessor instanceof ReferenceResolverAware) {
                    ((ReferenceResolverAware) validationProcessor).setReferenceResolver(referenceResolver);
                }

                while (!validatorNames.isEmpty()) {
                    final String validatorName = validatorNames.remove(0);

                    Object validator = referenceResolver.resolve(validatorName);
                    if (validator instanceof HeaderValidator) {
                        getHeaderValidationContext().addHeaderValidator((HeaderValidator) validator);
                    } else {
                        this.validators.add((MessageValidator<? extends ValidationContext>) validator);
                    }
                }

                if (messageBuilderSupport.getDataDictionaryName() != null) {
                    messageBuilderSupport.dictionary(
                            referenceResolver.resolve(messageBuilderSupport.getDataDictionaryName(), DataDictionary.class));
                }
            }

            return doBuild();
        }

        /**
         * Creates new header validation context if not done before and gets the header validation context.
         */
        public HeaderValidationContext getHeaderValidationContext() {
            if (headerValidationContext == null) {
                headerValidationContext = new HeaderValidationContext();

                validate(headerValidationContext);
            }

            return headerValidationContext;
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
            List<ValidationContext> validationContexts = getValidationContexts();
            if (validationContexts.stream().noneMatch(HeaderValidationContext.class::isInstance)) {
                getHeaderValidationContext();
            }

            if (validationContexts.stream().allMatch(HeaderValidationContext.class::isInstance)) {
                validate(new XmlMessageValidationContext());
                validate(new JsonMessageValidationContext());
            } else if (validationContexts.stream().anyMatch(JsonPathMessageValidationContext.class::isInstance)
                    && validationContexts.stream().noneMatch(JsonMessageValidationContext.class::isInstance)) {
                validate(new JsonMessageValidationContext());
            } else if (validationContexts.stream().noneMatch(XmlMessageValidationContext.class::isInstance)
                        && validationContexts.stream().noneMatch(JsonMessageValidationContext.class::isInstance)) {
                // if still no Json or Xml message validation context is set check the message payload and set proper context
                Optional<String> payload = getMessagePayload();
                if (payload.isPresent()) {
                    if (MessagePayloadUtils.isXml(payload.get())) {
                        validate(new XmlMessageValidationContext());
                    } else if (MessagePayloadUtils.isJson(payload.get())) {
                        validate(new JsonMessageValidationContext());
                    }
                }
            }

            validationContexts.stream()
                    .filter(HeaderValidationContext.class::isInstance)
                    .forEach(c -> ((HeaderValidationContext) c).setHeaderNameIgnoreCase(getMessageBuilderSupport().isHeaderNameIgnoreCase()));
        }

        /**
         * Gets message payload String representation from configured message builder.
         * @return
         */
        protected Optional<String> getMessagePayload() {
            if (messageBuilderSupport == null) {
                return Optional.empty();
            }

            if (messageBuilderSupport.getMessageBuilder() instanceof StaticMessageBuilder) {
                Message message = ((StaticMessageBuilder) messageBuilderSupport.getMessageBuilder()).getMessage();
                if (message.getPayload() instanceof String) {
                    return Optional.of(message.getPayload(String.class));
                }
            } else if (messageBuilderSupport.getMessageBuilder() instanceof WithPayloadBuilder) {
                MessagePayloadBuilder payloadBuilder = ((WithPayloadBuilder) messageBuilderSupport.getMessageBuilder()).getPayloadBuilder();
                if (payloadBuilder instanceof DefaultPayloadBuilder) {
                    return Optional.ofNullable(((DefaultPayloadBuilder) payloadBuilder).getPayload())
                            .map(Object::toString);
                }
            }

            return Optional.empty();
        }

        /**
         * Obtains the validationContexts.
         * @return
         */
        public List<ValidationContext> getValidationContexts() {
            return validationContexts.stream()
                    .map(ValidationContext.Builder::build)
                    .collect(Collectors.toList());
        }

        /**
         * Obtains the validationContext builders.
         * @return
         */
        public List<ValidationContext.Builder<?, ?>> getValidationContextBuilders() {
            return validationContexts;
        }
    }
}
