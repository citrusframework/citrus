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
import com.consol.citrus.message.Message;
import com.consol.citrus.message.MessageType;
import com.consol.citrus.validation.context.ValidationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Simple registry holding all available message validator implementations. Test context can ask this registry for
 * matching validator implementation according to the message type (e.g. xml, json, csv, plaintext).
 * 
 * Registry tries to find a matching validator for the message.
 * 
 * @author Christoph Deppisch
 */
public class MessageValidatorRegistry implements InitializingBean {

    /** Logger */
    private static Logger log = LoggerFactory.getLogger(MessageValidatorRegistry.class);

    /** The default bean id in Spring application context*/
    public static final String BEAN_NAME = "citrusMessageValidatorRegistry";

    /** List of registered message validator implementations */
    private List<MessageValidator<? extends ValidationContext>> messageValidators = new ArrayList<>();

    /**
     * Finds matching message validators for this message type.
     * 
     * @param messageType the message type
     * @param message the message object
     * @return the list of matching message validators.
     */
    public List<MessageValidator<? extends ValidationContext>> findMessageValidators(String messageType, Message message) {
        List<MessageValidator<? extends ValidationContext>> matchingValidators = new ArrayList<>();

        for (MessageValidator<? extends ValidationContext> validator : messageValidators) {
            if (validator.supportsMessageType(messageType, message)) {
                matchingValidators.add(validator);
            }
        }

        if (matchingValidators.isEmpty() || matchingValidators.stream().allMatch(validator -> DefaultMessageHeaderValidator.class.isAssignableFrom(validator.getClass()))) {
            // try to find fallback message validator for given message payload
            if (message.getPayload() instanceof String &&
                    StringUtils.hasText(message.getPayload(String.class))) {
                String payload = message.getPayload(String.class).trim();

                if (payload.startsWith("<") && !messageType.equals(MessageType.XML.name())) {
                    matchingValidators = findFallbackMessageValidators(MessageType.XML.name(), message);
                } else if ((payload.trim().startsWith("{") || payload.trim().startsWith("[")) && !messageType.equals(MessageType.JSON.name())) {
                    matchingValidators = findFallbackMessageValidators(MessageType.JSON.name(), message);
                } else if (!messageType.equals(MessageType.PLAINTEXT.name())) {
                    matchingValidators = findFallbackMessageValidators(MessageType.PLAINTEXT.name(), message);
                }
            }
        }

        if (matchingValidators.isEmpty() || matchingValidators.stream().allMatch(validator -> DefaultMessageHeaderValidator.class.isAssignableFrom(validator.getClass()))) {
            throw new CitrusRuntimeException("Could not find proper message validator for message type '" +
                    messageType + "', please define a capable message validator for this message type");
        }

        if (log.isDebugEnabled()) {
            log.debug(String.format("Found %s message validators for message type: %s", matchingValidators.size(), messageType));
        }
        
        return matchingValidators;
    }

    private List<MessageValidator<? extends ValidationContext>> findFallbackMessageValidators(String messageType, Message message) {
        List<MessageValidator<? extends ValidationContext>> matchingValidators = new ArrayList<>();

        for (MessageValidator<? extends ValidationContext> validator : messageValidators) {
            if (validator.supportsMessageType(messageType, message)) {
                matchingValidators.add(validator);
            }
        }

        return matchingValidators;
    }

    /**
     * Check if we have at least one message validator available.
     */
    public void afterPropertiesSet() throws Exception {
        if (messageValidators.isEmpty()) {
            throw new CitrusRuntimeException("No message validators available in Spring bean context - " +
                    "please define message validators!");
        }
    }

    /**
     * Sets available message validator implementations.
     * @param messageValidators the messageValidators to set
     */
    public void setMessageValidators(
            List<MessageValidator<? extends ValidationContext>> messageValidators) {
        this.messageValidators = messageValidators;
    }

    /**
     * Gets the message validators.
     * @return
     */
    public List<MessageValidator<? extends ValidationContext>> getMessageValidators() {
        return messageValidators;
    }

    /**
     * Gets the default message header validator.
     * @return
     */
    public MessageValidator getDefaultMessageHeaderValidator() {
        return messageValidators
                .stream()
                .filter(validator -> DefaultMessageHeaderValidator.class.isAssignableFrom(validator.getClass()))
                .findFirst()
                .orElse(null);
    }
}
