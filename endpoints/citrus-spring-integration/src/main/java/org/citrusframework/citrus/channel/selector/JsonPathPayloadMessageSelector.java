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

package org.citrusframework.citrus.channel.selector;

import org.citrusframework.citrus.context.TestContext;
import org.citrusframework.citrus.exceptions.CitrusRuntimeException;
import org.citrusframework.citrus.json.JsonPathUtils;
import org.springframework.messaging.Message;
import org.springframework.util.StringUtils;

/**
 * Message selector accepts JSON messages in case JsonPath expression evaluation result matches
 * the expected value. With this selector someone can select messages according to a message payload JSON
 * element value for instance.
 *
 * Syntax is jsonPath:$.root.element
 *
 * @author Christoph Deppisch
 * @since 2.7.5
 */
public class JsonPathPayloadMessageSelector extends AbstractMessageSelector {

    /** Special selector key prefix identifying this message selector implementation */
    public static final String SELECTOR_PREFIX = "jsonPath:";

    /**
     * Default constructor using fields.
     */
    public JsonPathPayloadMessageSelector(String expression, String control, TestContext context) {
        super(expression.substring(SELECTOR_PREFIX.length()), control, context);
    }

    @Override
    public boolean accept(Message<?> message) {
        String payload = getPayloadAsString(message);
        if (StringUtils.hasText(payload) &&
                !payload.trim().startsWith("{") &&
                !payload.trim().startsWith("[")) {
            return false;
        }

        try {
            return evaluate(JsonPathUtils.evaluateAsString(payload, selectKey));
        } catch (CitrusRuntimeException e) {
            return false;
        }
    }

    /**
     * Message selector factory for this implementation.
     */
    public static class Factory implements MessageSelectorFactory<JsonPathPayloadMessageSelector> {
        @Override
        public boolean supports(String key) {
            return key.startsWith(SELECTOR_PREFIX);
        }

        @Override
        public JsonPathPayloadMessageSelector create(String key, String value, TestContext context) {
            return new JsonPathPayloadMessageSelector(key, value, context);
        }
    }
}
