/*
 * Copyright 2006-2018 the original author or authors.
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
package com.consol.citrus.channel.selector;

import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.ValidationException;
import com.consol.citrus.validation.matcher.ValidationMatcherUtils;
import org.springframework.integration.core.MessageSelector;
import org.springframework.messaging.Message;

/**
 * Message selector matches one or more header elements with the message header. Only in case all 
 * matching header elements are present in message header and its value matches the expected value
 * the message is accepted.
 * 
 * @author Christoph Deppisch
 */
public class PayloadMatchingMessageSelector implements MessageSelector {

    /** List of header elements to match */
    private final String name;
    private final String value;

    /** Special selector element name identifying this message selector implementation */
    public static final String PAYLOAD_SELECTOR_ELEMENT = "payload";

    /** Test context */
    private final TestContext context;

    /**
     * Default constructor using fields.
     */
    public PayloadMatchingMessageSelector(String name, String value, TestContext context) {
        this.name = name;
        this.value = value;
        this.context = context;
    }
    
    @Override
    public boolean accept(Message<?> message) {
        String payload;
        if (message.getPayload() instanceof com.consol.citrus.message.Message) {
            payload = ((com.consol.citrus.message.Message) message.getPayload()).getPayload(String.class);
        } else {
            payload = message.getPayload().toString();
        }

        if (ValidationMatcherUtils.isValidationMatcherExpression(value)) {
            try {
                ValidationMatcherUtils.resolveValidationMatcher(name, payload, value, context);
                return true;
            } catch (ValidationException e) {
                return false;
            }
        } else {
            return payload.equals(value);
        }
    }

    /**
     * Message selector factory for this implementation.
     */
    public static class Factory implements MessageSelectorFactory<PayloadMatchingMessageSelector> {
        @Override
        public boolean supports(String key) {
            return key.equals(PAYLOAD_SELECTOR_ELEMENT);
        }

        @Override
        public PayloadMatchingMessageSelector create(String key, String value, TestContext context) {
            return new PayloadMatchingMessageSelector(key, value, context);
        }
    }
}
