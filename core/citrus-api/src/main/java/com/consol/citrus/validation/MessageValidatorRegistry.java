/*
 * Copyright 2006-2011 the original author or authors.
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

package com.consol.citrus.validation;

import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.exceptions.NoSuchMessageValidatorException;
import com.consol.citrus.message.Message;
import com.consol.citrus.message.MessageType;
import com.consol.citrus.util.IsJsonPredicate;
import com.consol.citrus.util.IsXmlPredicate;
import com.consol.citrus.validation.context.SchemaValidationContext;
import com.consol.citrus.validation.context.ValidationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.util.*;

/**
 * Simple registry holding all available message validator implementations. Test context can ask this registry for
 * matching validator implementation according to the message type (e.g. xml, json, csv, plaintext).
 *
 * Registry tries to find a matching validator for the message.
 *
 * @author Christoph Deppisch
 */
public class MessageValidatorRegistry {

    /** Logger */
    private static final Logger LOG = LoggerFactory.getLogger(MessageValidatorRegistry.class);

    /** The default bean id in Spring application context*/
    public static final String BEAN_NAME = "citrusMessageValidatorRegistry";

    /** Registered message validators */
    private Map<String, MessageValidator<? extends ValidationContext>> messageValidators = new LinkedHashMap<>();

    /** Registered schema validators */
    private Map<String, SchemaValidator<? extends SchemaValidationContext>> schemaValidators = new LinkedHashMap<>();

    /** Default message header validator - gets looked up via resource path */
    private MessageValidator<? extends ValidationContext> defaultMessageHeaderValidator;

    /** Default empty message validator */
    private final DefaultEmptyMessageValidator defaultEmptyMessageValidator = new DefaultEmptyMessageValidator();

    /**
     * Finds matching message validators for this message type.
     *
     * @param messageType the message type
     * @param message the message object
     * @return the list of matching message validators.
     */
    public List<MessageValidator<? extends ValidationContext>> findMessageValidators(String messageType, Message message) {
        List<MessageValidator<? extends ValidationContext>> matchingValidators = new ArrayList<>();

        for (MessageValidator<? extends ValidationContext> validator : messageValidators.values()) {
            if (validator.supportsMessageType(messageType, message)) {
                matchingValidators.add(validator);
            }
        }

        if (isEmptyOrDefault(matchingValidators)) {
            // try to find fallback message validator for given message payload
            if (message.getPayload() instanceof String &&
                    StringUtils.hasText(message.getPayload(String.class))) {
                String payload = message.getPayload(String.class).trim();

                if (payload.startsWith("<") && !messageType.equals(MessageType.XML.name())) {
                    matchingValidators = findFallbackMessageValidators(MessageType.XML.name(), message);
                } else if ((payload.startsWith("{") || payload.startsWith("[")) && !messageType.equals(MessageType.JSON.name())) {
                    matchingValidators = findFallbackMessageValidators(MessageType.JSON.name(), message);
                } else if (!messageType.equals(MessageType.PLAINTEXT.name())) {
                    matchingValidators = findFallbackMessageValidators(MessageType.PLAINTEXT.name(), message);
                }
            }
        }

        if (isEmptyOrDefault(matchingValidators) && !StringUtils.hasText(message.getPayload(String.class))) {
            matchingValidators.add(defaultEmptyMessageValidator);
        }

        if (isEmptyOrDefault(matchingValidators)) {
            LOG.warn(String.format("Unable to find proper message validator. Message type is '%s' and message payload is '%s'", messageType, message.getPayload(String.class)));
            throw new CitrusRuntimeException("Failed to find proper message validator for message");
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug(String.format("Found %s message validators for message", matchingValidators.size()));
        }

        return matchingValidators;
    }

    /**
     * Checks if matching list of validators is empty or just contains default message validators.
     * @param matchingValidators
     * @return
     */
    private boolean isEmptyOrDefault(List<MessageValidator<? extends ValidationContext>> matchingValidators) {
        return matchingValidators.isEmpty() || matchingValidators.stream().allMatch(this::isDefaultMessageHeaderValidator);
    }

    /**
     * Verify if given message validator is a subclass of default message header validator.
     * @param messageValidator
     * @return
     */
    private boolean isDefaultMessageHeaderValidator(MessageValidator<? extends ValidationContext> messageValidator) {
        if (defaultMessageHeaderValidator == null) {
            defaultMessageHeaderValidator = MessageValidator.lookup("header")
                    .orElseThrow(() -> new CitrusRuntimeException("Unable to locate default message header validator"));
        }

        return defaultMessageHeaderValidator.getClass().isAssignableFrom(messageValidator.getClass());
    }

    private List<MessageValidator<? extends ValidationContext>> findFallbackMessageValidators(String messageType, Message message) {
        List<MessageValidator<? extends ValidationContext>> matchingValidators = new ArrayList<>();

        for (MessageValidator<? extends ValidationContext> validator : messageValidators.values()) {
            if (validator.supportsMessageType(messageType, message)) {
                matchingValidators.add(validator);
            }
        }

        return matchingValidators;
    }

    private List<SchemaValidator<? extends SchemaValidationContext>> findFallbackSchemaValidators(String messageType, Message message) {
        List<SchemaValidator<? extends SchemaValidationContext>> matchingValidators = new ArrayList<>();

        for (SchemaValidator<? extends SchemaValidationContext> validator : schemaValidators.values()) {
            if (validator.supportsMessageType(messageType, message)) {
                matchingValidators.add(validator);
            }
        }

        return matchingValidators;
    }

    /**
     * Try to find validator for given name. Returns optional validator if any with that name present.
     * @param name to be searched for
     * @return optional message validator instance
     */
    public Optional<MessageValidator<? extends ValidationContext>> findMessageValidator(String name) {
        if (this.messageValidators.containsKey(name)) {
            return Optional.of(this.messageValidators.get(name));
        }

        return Optional.empty();
    }

    /**
     * Get validator for given name.
     * @param name to be searched for
     * @return message validator instance
     */
    public MessageValidator<? extends ValidationContext> getMessageValidator(String name) {
        if (this.messageValidators.containsKey(name)) {
            return this.messageValidators.get(name);
        }

        throw new NoSuchMessageValidatorException(String.format("Unable to find message validator with name '%s'", name));
    }

    /**
     * Adds given message validator and allows overwrite of existing message validators in registry with same name.
     * @param name
     * @param messageValidator
     */
    public void addMessageValidator(String name, MessageValidator<? extends ValidationContext> messageValidator) {
        if (this.messageValidators.containsKey(name) && LOG.isDebugEnabled()) {
            LOG.debug(String.format("Overwriting message validator '%s' in registry", name));
        }

        this.messageValidators.put(name, messageValidator);
    }

    /**
     * Adds given schema validator and allows overwrite of existing message validators in registry with same name.
     * @param name
     * @param schemaValidator
     */
    public void addSchemaValidator(String name, SchemaValidator<? extends SchemaValidationContext> schemaValidator) {
        if (this.schemaValidators.containsKey(name) && LOG.isDebugEnabled()) {
            LOG.debug(String.format("Overwriting message validator '%s' in registry", name));
        }

        this.schemaValidators.put(name, schemaValidator);
    }

    /**
     * Sets available message validator implementations.
     * @param messageValidators the messageValidators to set
     */
    public void setMessageValidators(
            Map<String, MessageValidator<? extends ValidationContext>> messageValidators) {
        this.messageValidators = messageValidators;
    }

    /**
     * Gets the message validators.
     * @return
     */
    public Map<String, MessageValidator<? extends ValidationContext>> getMessageValidators() {
        return messageValidators;
    }

    /**
     * Gets the default message header validator.
     * @return
     */
    public MessageValidator<? extends ValidationContext> getDefaultMessageHeaderValidator() {
        return messageValidators.values()
                .stream()
                .filter(this::isDefaultMessageHeaderValidator)
                .findFirst()
                .orElse(defaultMessageHeaderValidator);
    }

    /**
     * Finds matching schema validators for this message type.
     *
     * @param messageType the message type
     * @param message the message object
     * @return the list of matching schema validators.
     */
    public List<SchemaValidator<? extends SchemaValidationContext>> findSchemaValidators(String messageType, Message message) {
        List<SchemaValidator<? extends SchemaValidationContext>> matchingSchemaValidators = new ArrayList<>();

        for (SchemaValidator<? extends SchemaValidationContext> validator : schemaValidators.values()) {
            if (validator.supportsMessageType(messageType, message)) {
                matchingSchemaValidators.add(validator);
            }
        }

        if (matchingSchemaValidators.isEmpty()) {
            // try to find fallback message validator for given message payload
            if (message.getPayload() instanceof String &&
                    StringUtils.hasText(message.getPayload(String.class))) {
                String payload = message.getPayload(String.class).trim();

                if (IsXmlPredicate.getInstance().test(payload) && !messageType.equals(MessageType.XML.name())) {
                    matchingSchemaValidators = findFallbackSchemaValidators(MessageType.XML.name(), message);
                } else if (IsJsonPredicate.getInstance().test(payload) && !messageType.equals(MessageType.JSON.name())) {
                    matchingSchemaValidators = findFallbackSchemaValidators(MessageType.JSON.name(), message);
                }
            }
        }

        return matchingSchemaValidators;
    }

    /**
     * Try to find schema validator for given name. Returns optional validator if any with that name present.
     * @param name to be searched for
     * @return optional message validator instance
     */
    public Optional<SchemaValidator<? extends SchemaValidationContext>> findSchemaValidator(String name) {
        if (this.schemaValidators.containsKey(name)) {
            return Optional.of(this.schemaValidators.get(name));
        }

        return Optional.empty();
    }

    /**
     * Sets available schema validator implementations.
     * @param schemaValidators the messageValidators to set
     */
    public void setSchemaValidators(Map<String, SchemaValidator<? extends SchemaValidationContext>> schemaValidators) {
        this.schemaValidators = schemaValidators;
    }
}
