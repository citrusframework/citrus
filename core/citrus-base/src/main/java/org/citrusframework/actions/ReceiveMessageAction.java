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

package org.citrusframework.actions;

import org.citrusframework.CitrusSettings;
import org.citrusframework.context.TestContext;
import org.citrusframework.endpoint.Endpoint;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.exceptions.ValidationException;
import org.citrusframework.message.Message;
import org.citrusframework.message.MessageBuilder;
import org.citrusframework.message.MessageDirection;
import org.citrusframework.message.MessagePayload;
import org.citrusframework.message.MessagePayloadUtils;
import org.citrusframework.message.MessageProcessor;
import org.citrusframework.message.MessageSelectorBuilder;
import org.citrusframework.message.MessageType;
import org.citrusframework.message.WithPayloadBuilder;
import org.citrusframework.message.builder.DefaultPayloadBuilder;
import org.citrusframework.message.builder.FileResourcePayloadBuilder;
import org.citrusframework.message.builder.MessageBuilderSupport;
import org.citrusframework.message.builder.ReceiveMessageBuilderSupport;
import org.citrusframework.messaging.Consumer;
import org.citrusframework.messaging.SelectiveConsumer;
import org.citrusframework.spi.ReferenceResolverAware;
import org.citrusframework.util.FileUtils;
import org.citrusframework.util.TypeConverter;
import org.citrusframework.validation.DefaultMessageHeaderValidator;
import org.citrusframework.validation.HeaderValidator;
import org.citrusframework.validation.MessageValidator;
import org.citrusframework.validation.ValidationContextAdapter;
import org.citrusframework.validation.ValidationProcessor;
import org.citrusframework.validation.builder.StaticMessageBuilder;
import org.citrusframework.validation.context.DefaultMessageValidationContext;
import org.citrusframework.validation.context.HeaderValidationContext;
import org.citrusframework.validation.context.MessageValidationContext;
import org.citrusframework.validation.context.ValidationContext;
import org.citrusframework.validation.context.ValidationStatus;
import org.citrusframework.validation.json.JsonMessageValidationContext;
import org.citrusframework.validation.xml.XmlMessageValidationContext;
import org.citrusframework.validation.yaml.YamlMessageValidationContext;
import org.citrusframework.variable.VariableExtractor;
import org.citrusframework.variable.dictionary.DataDictionary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.joining;
import static org.citrusframework.CitrusSettings.getCustomValidatorStrategy;
import static org.citrusframework.message.MessageType.mapToMessageType;
import static org.citrusframework.util.StringUtils.hasText;
import static org.citrusframework.validation.CustomValidatorStrategy.EXCLUSIVE;

/**
 * This action receives messages from a service destination. Action uses a {@link org.citrusframework.endpoint.Endpoint}
 * to receive the message, this means that this action is independent of any message transport.
 * <p>
 * The received message is validated using a {@link MessageValidator} supporting expected
 * control message payload and header templates.
 *
 * @since 2008
 */
public class ReceiveMessageAction extends AbstractTestAction {

    /**
     * Build message selector with name value pairs
     */
    private final Map<String, Object> messageSelectorMap;

    /**
     * Select messages via message selector string
     */
    private final String messageSelector;

    private final Endpoint endpoint;

    /**
     * Message endpoint uri - either bean name or dynamic endpoint uri
     */
    private final String endpointUri;

    private final long receiveTimeout;

    /**
     * Builder constructing a control message
     */
    private final MessageBuilder messageBuilder;

    /**
     * MessageValidator responsible for message validation
     */
    private final List<MessageValidator<? extends ValidationContext>> validators;

    /**
     * Optional data dictionary that explicitly modifies message content before validation
     */
    private final DataDictionary<?> dataDictionary;

    /**
     * Callback able to additionally validate received message
     */
    private final ValidationProcessor validationProcessor;

    /**
     * List of validation contexts for this receive action
     */
    private final List<ValidationContext> validationContexts;

    /**
     * List of variable extractors responsible for creating variables from received message content
     */
    private final List<VariableExtractor> variableExtractors;

    /**
     * List of processors that handle the received message
     */
    private final List<MessageProcessor> messageProcessors;

    /**
     * List of processors that handle the control message builder
     */
    private final List<MessageProcessor> controlMessageProcessors;

    /**
     * The expected message type to arrive in this receive action.
     * This information is needed to find a proper message validator for this message
     */
    private String messageType;

    private static final Logger logger = LoggerFactory.getLogger(ReceiveMessageAction.class);

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
        if (hasText(selector)) {
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
     */
    private Message receive(TestContext context) {
        Endpoint messageEndpoint = getOrCreateEndpoint(context);
        return receiveTimeout > 0 ? messageEndpoint.createConsumer().receive(context, receiveTimeout) :
                messageEndpoint.createConsumer().receive(context, messageEndpoint.getEndpointConfiguration().getTimeout());
    }

    /**
     * Receives the message with the respective message receiver implementation
     * also using a message selector.
     *
     * @param context        the test context.
     * @param selectorString the message selector string.
     */
    private Message receiveSelected(TestContext context, String selectorString) {
        logger.debug("Setting message selector: '{}'", selectorString);

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
            logger.warn("Unable to receive selective with consumer implementation: '{}'", consumer.getClass());
            return receive(context);
        }
    }

    /**
     * Override this message if you want to add additional message validation
     */
    protected void validateMessage(Message message, TestContext context) {
        messageProcessors.forEach(processor -> processor.process(message, context));

        logger.debug("Received message:\n{}", message.print(context));

        // extract variables from received message content
        for (VariableExtractor variableExtractor : variableExtractors) {
            variableExtractor.extractVariables(message, context);
        }

        Message controlMessage = createControlMessage(context, messageType);
        if (hasText(controlMessage.getName())) {
            context.getMessageStore().storeMessage(controlMessage.getName(), message);
        } else {
            context.getMessageStore().storeMessage(context.getMessageStore().constructMessageName(this, getOrCreateEndpoint(context)), message);
        }

        if (nonNull(validationProcessor)) {
            validationProcessor.validate(message, context);

            if (EXCLUSIVE.equals(getCustomValidatorStrategy())) {
                if (!validationContexts.isEmpty()) {
                    logger.debug("Note that custom validator has been called exclusively! Other validators will be disregarded.");
                }

                validationContexts.clear();
            }
        }

        if (!validationContexts.isEmpty()) {
            logger.debug("Control message:\n{}", controlMessage.print(context));

            if (hasText(controlMessage.getPayload(String.class))) {
                assumeMessageType(controlMessage);
            } else {
                assumeMessageType(message);
            }

            if (nonNull(validators) && !validators.isEmpty()) {
                for (MessageValidator<? extends ValidationContext> messageValidator : validators) {
                    messageValidator.validateMessage(message, controlMessage, context, validationContexts);
                }

                if (validators.parallelStream()
                        .map(Object::getClass)
                        .noneMatch(DefaultMessageHeaderValidator.class::isAssignableFrom)) {
                    MessageValidator<? extends ValidationContext> defaultMessageHeaderValidator = context.getMessageValidatorRegistry().getDefaultMessageHeaderValidator();
                    if (defaultMessageHeaderValidator != null) {
                        defaultMessageHeaderValidator.validateMessage(message, controlMessage, context, validationContexts);
                    }
                }
            } else {
                boolean mustFindValidator = validationContexts.stream()
                        .anyMatch(ValidationContext::requiresValidator);

                List<MessageValidator<? extends ValidationContext>> activeValidators =
                        context.getMessageValidatorRegistry().findMessageValidators(messageType, message, mustFindValidator);

                for (MessageValidator<? extends ValidationContext> messageValidator : activeValidators) {
                    messageValidator.validateMessage(message, controlMessage, context, validationContexts);
                }

                if (CitrusSettings.isPerformDefaultValidation() &&
                        validationContexts.stream().anyMatch(validationContext -> validationContext.getStatus() == ValidationStatus.UNKNOWN)) {
                    var defaultValidator = context.getMessageValidatorRegistry().getDefaultMessageValidator();
                    if (activeValidators.stream().noneMatch(validator -> defaultValidator.getClass().isInstance(validator))) {
                        defaultValidator.validateMessage(message, controlMessage, context, validationContexts);
                    }
                }
            }

            List<ValidationContext> unknown = validationContexts.stream()
                    .filter(validationContext -> validationContext.getStatus() == ValidationStatus.UNKNOWN)
                    .toList();
            if (!unknown.isEmpty()) {
                unknown.forEach(validationContext -> logger.warn("Found validation context that has not been processed: {}", validationContext.getClass().getName()));
                throw new ValidationException("Incomplete message validation of message with type %s - no validation has been performed on the following contexts: %s".formatted(messageType, unknown.stream().map(validationContext -> validationContext.getClass().getSimpleName()).collect(joining(", "))));
            }
        }
    }

    private void assumeMessageType(Message message) {
        Object contentType = message.getHeaders().get("Content-Type");
        if (contentType instanceof String contentTypeString) {
            Object encoding = message.getHeaders().get("Content-Transfer-Encoding");
            MessageType messageType = mapToMessageType(
                    contentTypeString,
                    encoding != null ? encoding.toString() : null);
            if (messageType != null) {
                setMessageType(messageType);
                return;
            }
        }

        if (MessageType.isBinary(getMessageType()) ||
                MessageType.FORM_URL_ENCODED.equalsIgnoreCase(getMessageType())) {
            return;
        }

        if (isEffectivelyNullishPayload(message)) {
            // A plain message validator is always present (at least), thus we assign the PLAINTEXT message type on nullish payloads
            setMessageType(MessageType.PLAINTEXT);
            return;
        }

        var payload = message.getPayload(String.class).trim();

        if (MessagePayloadUtils.isXml(payload)
                && (isNull(getMessageType()) || !MessageType.isXml(getMessageType()))) {
            logger.warn("Detected XML message payload type, but non-XML message type '{}' configured! Assuming message type {}",
                    getMessageType(), MessageType.XML);

            setMessageType(MessageType.XML);
        } else if (MessagePayloadUtils.isJson(payload)
                && (isNull(getMessageType()) || !getMessageType().equalsIgnoreCase(MessageType.JSON.name()))) {
            logger.warn("Detected JSON message payload type, but non-JSON message type '{}' configured! Assuming message type {}",
                    getMessageType(), MessageType.JSON);

            setMessageType(MessageType.JSON);
        } else if (MessagePayloadUtils.isYaml(payload)
                && (isNull(getMessageType()) || !getMessageType().equalsIgnoreCase(MessageType.YAML.name()))) {
            logger.warn("Detected YAML message payload type, but non-YAML message type '{}' configured! Assuming message type {}",
                    getMessageType(), MessageType.YAML);

            setMessageType(MessageType.YAML);
        }
    }

    /**
     * The check `isNull(message.getPayload())` is not sufficient for empty payloads.
     * E.g. an empty JSON object passes `isNull(message.getPayload())`, but not `!hasText(message.getPayload(String.class))`, because an empty JSON object is equal to `{}`.
     * Thus, an "effective nullish payload" has both `null` and "null" as `Object` and `String` representation.
     */
    private static boolean isEffectivelyNullishPayload(Message message) {
        var payload = isNull(message.getPayload()) && "null".equals(message.getPayload(String.class)) ? null : message.getPayload(String.class);
        return !hasText(payload);
    }

    /**
     * Create control message that is expected. Apply global and local message processors and data dictionaries.
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
     */
    public Endpoint getOrCreateEndpoint(TestContext context) {
        if (endpoint != null) {
            return endpoint;
        } else if (hasText(endpointUri)) {
            return context.getEndpointFactory().create(endpointUri, context);
        } else {
            throw new CitrusRuntimeException("Neither endpoint nor endpoint uri is set properly!");
        }
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

    public List<MessageProcessor> getControlMessageProcessors() {
        return controlMessageProcessors;
    }

    public String getMessageType() {
        return messageType;
    }

    private void setMessageType(MessageType messageType) {
        this.messageType = messageType.name();
    }

    public Map<String, Object> getMessageSelectorMap() {
        return messageSelectorMap;
    }

    public String getMessageSelector() {
        return messageSelector;
    }

    public long getReceiveTimeout() {
        return receiveTimeout;
    }

    public List<MessageValidator<? extends ValidationContext>> getValidators() {
        return Collections.unmodifiableList(validators);
    }

    public List<ValidationContext> getValidationContexts() {
        return validationContexts;
    }

    public ValidationProcessor getValidationProcessor() {
        return validationProcessor;
    }

    public DataDictionary<?> getDataDictionary() {
        return dataDictionary;
    }

    public MessageBuilder getMessageBuilder() {
        return messageBuilder;
    }

    /**
     * Action builder.
     */
    public static class Builder extends ReceiveMessageActionBuilder<ReceiveMessageAction, ReceiveMessageActionBuilderSupport, Builder> {

        /**
         * Fluent API action building entry method used in Java DSL.
         */
        public static Builder receive() {
            return new Builder();
        }

        /**
         * Fluent API action building entry method used in Java DSL.
         */
        public static Builder receive(Endpoint messageEndpoint) {
            Builder builder = new Builder();
            builder.endpoint(messageEndpoint);
            return builder;
        }

        /**
         * Fluent API action building entry method used in Java DSL.
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

    public abstract static class ReceiveMessageActionBuilder<T extends ReceiveMessageAction, M extends ReceiveMessageBuilderSupport<T, B, M>, B extends ReceiveMessageActionBuilder<T, M, B>>
            extends MessageBuilderSupport.MessageActionBuilder<T, M, B> implements ReceiveActionBuilder<T, M, B> {

        private long receiveTimeout = 0L;

        private final Map<String, Object> messageSelectorMap = new HashMap<>();
        private String messageSelector;

        private final List<MessageValidator<? extends ValidationContext>> validators = new ArrayList<>();
        private ValidationProcessor validationProcessor;
        private final List<ValidationContext.Builder<?, ?>> validationContexts = new ArrayList<>();

        /**
         * Validation context used in this action builder
         */
        private HeaderValidationContext.Builder headerValidationContext;

        private final List<String> validatorNames = new ArrayList<>();

        @Override
        public B timeout(final long receiveTimeout) {
            this.receiveTimeout = receiveTimeout;
            return self;
        }

        @Override
        public B validate(final ValidationContext.Builder<?, ?> validationContext) {
            this.validationContexts.add(validationContext);
            return self;
        }

        @Override
        public B validate(final ValidationContext validationContext) {
            return validate((ValidationContext.Builder) () -> validationContext);
        }

        @Override
        public B validate(final ValidationContextAdapter adapter) {
            return validate(adapter.asValidationContext());
        }

        @Override
        public B validate(final List<ValidationContext.Builder<?, ?>> validationContexts) {
            this.validationContexts.addAll(validationContexts);
            return self;
        }

        @Override
        public B validate(ValidationContext.Builder<?, ?>... validationContexts) {
            return validate(Arrays.asList(validationContexts));
        }

        @Override
        public B selector(final String messageSelector) {
            this.messageSelector = messageSelector;
            return self;
        }

        @Override
        public B selector(final Map<String, String> messageSelector) {
            this.messageSelectorMap.putAll(messageSelector);
            return self;
        }

        @Override
        public B validator(final MessageValidator<? extends ValidationContext> validator) {
            this.validators.add(validator);
            return self;
        }

        @Override
        public final B validators(final String... validators) {
            Arrays.stream(validators).forEach(this::validator);
            return self;
        }

        @Override
        @SafeVarargs
        public final B validators(final MessageValidator<? extends ValidationContext>... validators) {
            return validators(Arrays.asList(validators));
        }

        @Override
        public B validators(final List<MessageValidator<? extends ValidationContext>> validators) {
            this.validators.addAll(validators);
            return self;
        }

        @Override
        public final B validators(final HeaderValidator... validators) {
            Stream.of(validators).forEach(this::validator);
            return self;
        }

        @Override
        public B validator(final String validatorName) {
            this.validatorNames.add(validatorName);
            return self;
        }

        @Override
        public B validator(final HeaderValidator validators) {
            Stream.of(validators).forEach(getHeaderValidationContext()::validator);
            return self;
        }

        @Override
        public B validate(final ValidationProcessor processor) {
            this.validationProcessor = processor;
            return self;
        }

        @Override
        public B process(MessageProcessor processor) {
            if (processor instanceof VariableExtractor variableExtractor) {
                this.variableExtractors.add(variableExtractor);
            } else if (processor instanceof ValidationProcessor validationProcessor) {
                validate(validationProcessor);
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

            validationContexts.stream()
                    .filter(HeaderValidationContext.Builder.class::isInstance)
                    .forEach(c -> ((HeaderValidationContext.Builder) c).ignoreCase(messageBuilderSupport.isHeaderNameIgnoreCase()));

            if (referenceResolver != null) {
                if (validationProcessor instanceof ReferenceResolverAware referenceResolverAware) {
                    referenceResolverAware.setReferenceResolver(referenceResolver);
                }

                while (!validatorNames.isEmpty()) {
                    final String validatorName = validatorNames.remove(0);

                    Object validator = referenceResolver.resolve(validatorName);
                    if (validator instanceof HeaderValidator) {
                        getHeaderValidationContext().validator((HeaderValidator) validator);
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
        public HeaderValidationContext.Builder getHeaderValidationContext() {
            if (headerValidationContext == null) {
                headerValidationContext = new HeaderValidationContext.Builder();

                validate(headerValidationContext);
            }

            return headerValidationContext;
        }

        /**
         * Revisit configured validation context list and automatically add context based on message payload and path
         * expression contexts if any.
         * <p>
         * This method makes sure that validation contexts are configured. If no validation context has been set yet the method
         * automatically adds proper validation contexts for Json and XML message payloads.
         * <p>
         * In case a path expression (JsonPath, XPath) context is set but no proper message validation context (Json, Xml) the
         * method automatically adds the proper message validation context.
         * <p>
         * Only when validation contexts are set properly according to the message type and content the message validation
         * steps will execute later on.
         */
        protected void reconcileValidationContexts() {
            List<ValidationContext> validationContexts = getValidationContexts();
            if (validationContexts.stream().noneMatch(HeaderValidationContext.class::isInstance)) {
                getHeaderValidationContext();
            }

            if (validationContexts.stream().noneMatch(MessageValidationContext.class::isInstance)) {
                injectMessageValidationContext();
            }
        }

        private void injectMessageValidationContext() {
            // If we have a non-empty payload (in control message), we need a respective context for validating it.
            ValidationContext validationContext = null;
            Optional<String> payload = getMessagePayload();
            if (payload.isPresent() && hasText(payload.get())) {
                if (MessagePayloadUtils.isXml(payload.get())) {
                    validationContext = new XmlMessageValidationContext();
                } else if (MessagePayloadUtils.isJson(payload.get())) {
                    validationContext = new JsonMessageValidationContext();
                } else {
                    validationContext = new DefaultMessageValidationContext();
                }
            }

            // If we have not yet added a context, we might have a typed builder capable to create a
            // message. In that case add a context for the respective type.
            if (validationContext == null && messageBuilderSupport != null) {
                MessageType type = getMessageType(messageBuilderSupport);
                if (type == MessageType.XML || type == MessageType.XHTML) {
                    validationContext = new XmlMessageValidationContext();
                } else if (type == MessageType.JSON) {
                    validationContext = new JsonMessageValidationContext();
                } else if (type == MessageType.YAML) {
                    validationContext = new YamlMessageValidationContext();
                } else if (type == MessageType.PLAINTEXT) {
                    validationContext = new DefaultMessageValidationContext();
                }
            }

            if (validationContext == null) {
                Optional<String> resource = getMessageResource();
                if (resource.isPresent()) {
                    String fileExt = FileUtils.getFileExtension(resource.get());
                    if ("xml".equalsIgnoreCase(fileExt)) {
                        validationContext = new XmlMessageValidationContext();
                    } else if ("json".equalsIgnoreCase(fileExt)) {
                        validationContext = new JsonMessageValidationContext();
                    } else {
                        validationContext = new DefaultMessageValidationContext();
                    }
                }
            }

            if (validationContext != null) {
                validate(validationContext);
            }
        }

        /**
         * Determine the message type for relevant messageBuilderSupports. The type is relevant, as
         * the context should match with the validator, which might later be chosen for validation.
         */
        private MessageType getMessageType(M messageBuilderSupport) {
            if (messageBuilderSupport.getMessageBuilder().getClass()
                    .isAnnotationPresent(MessagePayload.class)) {
                return messageBuilderSupport.getMessageBuilder().getClass()
                        .getAnnotation(MessagePayload.class).value();
            } else if (messageBuilderSupport.getMessageBuilder() instanceof StaticMessageBuilder
                    || (
                    messageBuilderSupport.getMessageBuilder() instanceof WithPayloadBuilder payloadBuilder
                            && payloadBuilder.getPayloadBuilder() != null)) {
                MessageType messageType = MessageType.PLAINTEXT;
                try {
                    // Determine the message type from builder support if it has EXPLICITLY been set.
                    // If it has not been set explicitly, we rely on validation context reconciliation
                    // on validation, based on the received message type.
                    messageType = hasText(messageBuilderSupport.getMessageType()) && messageBuilderSupport.isExplicitMessageType()
                            ? MessageType.valueOf(messageBuilderSupport.getMessageType().toUpperCase())
                            : messageType;
                } catch (IllegalArgumentException e) {
                    // Unknown message type
                    return null;
                }
                return messageType;
            }

            return null;
        }

        /**
         * Gets message payload String representation from configured message builder.
         */
        protected Optional<String> getMessagePayload() {
            if (messageBuilderSupport == null) {
                return Optional.empty();
            }

            if (messageBuilderSupport.getMessageBuilder() instanceof StaticMessageBuilder staticMessageBuilder) {
                Message message = staticMessageBuilder.getMessage();
                if (message.getPayload() instanceof String) {
                    return Optional.of(message.getPayload(String.class));
                }
            } else if (messageBuilderSupport.getMessageBuilder() instanceof WithPayloadBuilder withPayloadBuilder) {
                if (withPayloadBuilder.getPayloadBuilder() instanceof DefaultPayloadBuilder defaultPayloadBuilder) {
                    return Optional.ofNullable(defaultPayloadBuilder.getPayload())
                            .map(object -> TypeConverter.lookupDefault().convertIfNecessary(object, String.class));
                }
            }

            return Optional.empty();
        }

        /**
         * Gets message resource file path from configured message builder.
         */
        protected Optional<String> getMessageResource() {
            if (messageBuilderSupport == null) {
                return Optional.empty();
            }

            if (messageBuilderSupport.getMessageBuilder() instanceof WithPayloadBuilder withPayloadBuilder) {
                if (withPayloadBuilder.getPayloadBuilder() instanceof FileResourcePayloadBuilder filePayloadBuilder) {
                    return Optional.ofNullable(filePayloadBuilder.getResourcePath());
                }
            }

            return Optional.empty();
        }

        public List<ValidationContext> getValidationContexts() {
            return validationContexts.stream()
                    .map(ValidationContext.Builder::build)
                    .collect(Collectors.toList());
        }

        public List<ValidationContext.Builder<?, ?>> getValidationContextBuilders() {
            return validationContexts;
        }
    }
}
