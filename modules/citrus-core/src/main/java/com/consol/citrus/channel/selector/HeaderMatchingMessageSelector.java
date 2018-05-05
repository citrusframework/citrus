/*
 * Copyright 2006-2012 the original author or authors.
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
import org.springframework.messaging.MessageHeaders;

import java.util.*;

/**
 * Message selector matches one or more header elements with the message header. Only in case all 
 * matching header elements are present in message header and its value matches the expected value
 * the message is accepted.
 * 
 * @author Christoph Deppisch
 */
public class HeaderMatchingMessageSelector implements MessageSelector {

    /** List of header elements to match */
    private final String name;
    private final String value;

    /** Test context */
    private final TestContext context;

    /**
     * Default constructor using fields.
     */
    public HeaderMatchingMessageSelector(String name, String value, TestContext context) {
        this.name = name;
        this.value = value;
        this.context = context;
    }
    
    @Override
    public boolean accept(Message<?> message) {
        MessageHeaders messageHeaders = message.getHeaders();

        Map<String, Object> nestedMessageHeaders = new HashMap<>();
        if (message.getPayload() instanceof com.consol.citrus.message.Message) {
            nestedMessageHeaders = ((com.consol.citrus.message.Message) message.getPayload()).getHeaders();
        }

        if (nestedMessageHeaders.containsKey(name)) {
            return matchHeader(nestedMessageHeaders);
        } else if (messageHeaders.containsKey(name)) {
            return matchHeader(messageHeaders);
        } else {
            return false;
        }
    }

    private boolean matchHeader(Map<String, Object> messageHeaders) {
        return Optional.ofNullable(messageHeaders.get(name))
                .map(Object::toString)
                .map(header -> {
                    if (ValidationMatcherUtils.isValidationMatcherExpression(value)) {
                        try {
                            ValidationMatcherUtils.resolveValidationMatcher(name, header, value, context);
                            return true;
                        } catch (ValidationException e) {
                            return false;
                        }
                    } else {
                        return header.equals(value);
                    }
                })
                .orElse(false);
    }

    /**
     * Message selector factory for this implementation.
     */
    public static class Factory implements MessageSelectorFactory<HeaderMatchingMessageSelector> {
        @Override
        public boolean supports(String key) {
            return true;
        }

        @Override
        public HeaderMatchingMessageSelector create(String key, String value, TestContext context) {
            return new HeaderMatchingMessageSelector(key, value, context);
        }
    }
}
