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
package org.citrusframework.channel.selector;

import org.citrusframework.context.TestContext;
import org.springframework.messaging.Message;
import org.springframework.util.Assert;

/**
 * Message selector matches one or more header elements with the message header. Only in case all 
 * matching header elements are present in message header and its value matches the expected value
 * the message is accepted.
 * 
 * @author Christoph Deppisch
 */
public class PayloadMatchingMessageSelector extends AbstractMessageSelector {

    /** Special selector identifying key for this message selector implementation */
    public static final String SELECTOR_ID = "payload";

    /**
     * Default constructor using fields.
     */
    public PayloadMatchingMessageSelector(String selectKey, String matchingValue, TestContext context) {
        super(selectKey, matchingValue, context);

        Assert.isTrue(selectKey.equals(SELECTOR_ID),
                String.format("Invalid usage of payload matching message selector - " +
                        "usage restricted to key '%s' but was '%s'",  SELECTOR_ID, selectKey));
    }
    
    @Override
    public boolean accept(Message<?> message) {
        return evaluate(getPayloadAsString(message));
    }

    /**
     * Message selector factory for this implementation.
     */
    public static class Factory implements MessageSelectorFactory<PayloadMatchingMessageSelector> {
        @Override
        public boolean supports(String key) {
            return key.equals(SELECTOR_ID);
        }

        @Override
        public PayloadMatchingMessageSelector create(String key, String value, TestContext context) {
            return new PayloadMatchingMessageSelector(key, value, context);
        }
    }
}
