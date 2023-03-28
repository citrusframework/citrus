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
import org.citrusframework.exceptions.ValidationException;
import org.citrusframework.validation.matcher.ValidationMatcherUtils;
import org.springframework.integration.core.MessageSelector;
import org.springframework.messaging.Message;

/**
 * @author Christoph Deppisch
 * @since 2.7.5
 */
public abstract class AbstractMessageSelector implements MessageSelector {

    /** Key and value to evaluate selection with */
    protected final String selectKey;
    protected final String matchingValue;

    /** Test context */
    protected final TestContext context;

    /**
     * Default constructor using fields.
     * @param selectKey
     * @param matchingValue
     * @param context
     */
    public AbstractMessageSelector(String selectKey, String matchingValue, TestContext context) {
        this.selectKey = selectKey;
        this.matchingValue = matchingValue;
        this.context = context;
    }

    /**
     * Reads message payload as String either from message object directly or from nested Citrus message representation.
     * @param message
     * @return
     */
    String getPayloadAsString(Message<?> message) {
        if (message.getPayload() instanceof org.citrusframework.message.Message) {
            return  ((org.citrusframework.message.Message) message.getPayload()).getPayload(String.class);
        } else {
            return message.getPayload().toString();
        }
    }

    /**
     * Evaluates given value to match this selectors matching condition. Automatically supports validation matcher expressions.
     * @param value
     * @return
     */
    protected boolean evaluate(String value) {
        if (ValidationMatcherUtils.isValidationMatcherExpression(matchingValue)) {
            try {
                ValidationMatcherUtils.resolveValidationMatcher(selectKey, value, matchingValue, context);
                return true;
            } catch (ValidationException e) {
                return false;
            }
        } else {
            return value.equals(matchingValue);
        }
    }
}
