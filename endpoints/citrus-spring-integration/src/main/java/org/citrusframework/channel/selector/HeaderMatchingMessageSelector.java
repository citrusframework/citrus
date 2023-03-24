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
package org.citrusframework.channel.selector;

import org.citrusframework.context.TestContext;
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
public class HeaderMatchingMessageSelector extends AbstractMessageSelector {

    /** Special selector key prefix identifying this message selector implementation */
    public static final String SELECTOR_PREFIX = "header:";

    /**
     * Default constructor using fields.
     */
    public HeaderMatchingMessageSelector(String selectKey, String matchingValue, TestContext context) {
        super(selectKey, matchingValue, context);
    }
    
    @Override
    public boolean accept(Message<?> message) {
        MessageHeaders messageHeaders = message.getHeaders();

        Map<String, Object> nestedMessageHeaders = new HashMap<>();
        if (message.getPayload() instanceof org.citrusframework.message.Message) {
            nestedMessageHeaders = ((org.citrusframework.message.Message) message.getPayload()).getHeaders();
        }

        if (nestedMessageHeaders.containsKey(selectKey)) {
            return matchHeader(nestedMessageHeaders);
        } else if (messageHeaders.containsKey(selectKey)) {
            return matchHeader(messageHeaders);
        } else {
            return false;
        }
    }

    private boolean matchHeader(Map<String, Object> messageHeaders) {
        return Optional.ofNullable(messageHeaders.get(selectKey))
                .map(Object::toString)
                .map(this::evaluate)
                .orElse(false);
    }

    /**
     * Message selector factory for this implementation.
     */
    public static class Factory implements MessageSelectorFactory<HeaderMatchingMessageSelector> {
        @Override
        public boolean supports(String key) {
            return key.startsWith(SELECTOR_PREFIX);
        }

        @Override
        public HeaderMatchingMessageSelector create(String key, String value, TestContext context) {
            if (key.startsWith(SELECTOR_PREFIX)) {
                return new HeaderMatchingMessageSelector(key.substring(SELECTOR_PREFIX.length()), value, context);
            } else {
                return new HeaderMatchingMessageSelector(key, value, context);
            }
        }
    }
}
