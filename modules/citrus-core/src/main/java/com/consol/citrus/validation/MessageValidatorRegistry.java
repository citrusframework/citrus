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
import com.consol.citrus.message.MessageHeaders;
import com.consol.citrus.validation.context.ValidationContext;
import com.consol.citrus.validation.text.PlainTextMessageValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import com.consol.citrus.message.Message;

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

    /** List of registered message validator implementations */
    @Autowired(required = false)
    private List<MessageValidator<? extends ValidationContext>> messageValidators = new ArrayList<MessageValidator<? extends ValidationContext>>();

    /** Logger */
    private static Logger log = LoggerFactory.getLogger(MessageValidatorRegistry.class);

    /**
     * Finds proper message validators for this message.
     *  
     * @param message the message to validate.
     * @return  the list of matching message validators.
     */
    public List<MessageValidator<? extends ValidationContext>> findMessageValidators(Message message) {
        return findMessageValidators(message.getHeaders().get(MessageHeaders.MESSAGE_TYPE).toString());
    }


    /**
     * Finds matching message validators for this message type.
     * 
     * @param messageType the message type
     * @return the list of matching message validators.
     */
    public List<MessageValidator<? extends ValidationContext>> findMessageValidators(String messageType) {
        List<MessageValidator<? extends ValidationContext>> matchingValidators = new ArrayList<MessageValidator<? extends ValidationContext>>();
        
        for (MessageValidator<? extends ValidationContext> validator : messageValidators) {
            if (validator.supportsMessageType(messageType)) {
                matchingValidators.add(validator);
            }
        }
        
        if (matchingValidators.isEmpty()) {
            throw new CitrusRuntimeException("Could not find proper message validator for message type '" + 
                    messageType + "', please define a capable message validator for this message type");
        }
        
        return matchingValidators;
    }

    /**
     * Check if we have at least one message validator available.
     */
    public void afterPropertiesSet() throws Exception {
        if (messageValidators.isEmpty()) {
            log.warn("No message validators available in Spring bean context - " +
                    "adding default message validator!");

            messageValidators.add(new PlainTextMessageValidator());
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
}
