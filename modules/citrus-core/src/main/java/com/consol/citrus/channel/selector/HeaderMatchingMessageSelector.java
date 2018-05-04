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

    /**
     * Default constructor using fields.
     */
    public HeaderMatchingMessageSelector(String name, String value) {
        this.name = name;
        this.value = value;
    }
    
    @Override
    public boolean accept(Message<?> message) {
        MessageHeaders messageHeaders = message.getHeaders();

        Map<String, Object> nestedMessageHeaders = new HashMap<>();
        if (message.getPayload() instanceof com.consol.citrus.message.Message) {
            nestedMessageHeaders = ((com.consol.citrus.message.Message) message.getPayload()).getHeaders();
        }

        if (nestedMessageHeaders.containsKey(name)) {
            return Optional.ofNullable(nestedMessageHeaders.get(name))
                    .map(header -> header.equals(value))
                    .orElse(false);
        } else if (messageHeaders.containsKey(name)) {
            return Optional.ofNullable(messageHeaders.get(name))
                    .map(Object::toString)
                    .map(header -> header.equals(value))
                    .orElse(false);
        } else {
            return false;
        }
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
        public HeaderMatchingMessageSelector create(String key, String value) {
            return new HeaderMatchingMessageSelector(key, value);
        }
    }
}
