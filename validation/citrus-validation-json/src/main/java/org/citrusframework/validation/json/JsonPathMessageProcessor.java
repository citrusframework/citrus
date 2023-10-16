/*
 * Copyright 2006-2015 the original author or authors.
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

package org.citrusframework.validation.json;

import java.util.LinkedHashMap;
import java.util.Map;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;
import org.citrusframework.builder.WithExpressions;
import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.exceptions.UnknownElementException;
import org.citrusframework.message.AbstractMessageProcessor;
import org.citrusframework.message.Message;
import org.citrusframework.message.MessageProcessor;
import org.citrusframework.message.MessageType;
import org.citrusframework.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Christoph Deppisch
 * @since 2.3
 */
public class JsonPathMessageProcessor extends AbstractMessageProcessor {

    /** Logger */
    private static final Logger logger = LoggerFactory.getLogger(JsonPathMessageProcessor.class);

    /** Overwrites message elements before validating (via JSONPath expressions) */
    private final Map<String, Object> jsonPathExpressions;

    /** Optional ignoring element not found errors */
    private final boolean ignoreNotFound;

    /**
     * Default constructor.
     */
    public JsonPathMessageProcessor() {
        this(new Builder());
    }

    /**
     * Constructor using fluent builder.
     * @param builder
     */
    private JsonPathMessageProcessor(Builder builder) {
        this.jsonPathExpressions = builder.expressions;
        this.ignoreNotFound = builder.ignoreNotFound;
    }

    /**
     * Intercept the message payload construction and replace elements identified
     * via Json path expressions.
     */
    @Override
    public void processMessage(Message message, TestContext context) {
        if (message.getPayload() == null || !StringUtils.hasText(message.getPayload(String.class))) {
            return;
        }

        String jsonPathExpression;
        try {
            JSONParser parser = new JSONParser(JSONParser.MODE_JSON_SIMPLE);
            Object jsonData = parser.parse(message.getPayload(String.class));
            DocumentContext documentContext = JsonPath.parse(jsonData);

            for (Map.Entry<String, Object> entry : jsonPathExpressions.entrySet()) {
                jsonPathExpression = entry.getKey();
                String valueExpression = context.replaceDynamicContentInString(entry.getValue().toString());

                Object value;
                if (valueExpression.equals("true")) {
                    value = true;
                } else if (valueExpression.equals("false")) {
                    value = false;
                } else {
                    try {
                        value = Integer.valueOf(valueExpression);
                    } catch (IllegalArgumentException e) {
                        value = valueExpression;
                    }
                }

                try {
                    documentContext.set(jsonPathExpression, value);
                } catch (PathNotFoundException e) {
                    if (!ignoreNotFound) {
                        throw new UnknownElementException(String.format("Could not find element for expression: %s", jsonPathExpression), e);
                    }
                }

                if (logger.isDebugEnabled()) {
                    logger.debug("Element " + jsonPathExpression + " was set to value: " + valueExpression);
                }
            }

            message.setPayload(jsonData.toString());
        } catch (ParseException e) {
            throw new CitrusRuntimeException("Failed to parse JSON text", e);
        }
    }

    @Override
    public boolean supportsMessageType(String messageType) {
        return MessageType.JSON.toString().equalsIgnoreCase(messageType);
    }

    /**
     * Fluent builder.
     */
    public static final class Builder implements MessageProcessor.Builder<JsonPathMessageProcessor, Builder>, WithExpressions<Builder> {
        private final Map<String, Object> expressions = new LinkedHashMap<>();
        private boolean ignoreNotFound = false;

        @Override
        public Builder expressions(Map<String, Object> expressions) {
            this.expressions.putAll(expressions);
            return this;
        }

        @Override
        public Builder expression(final String expression, final Object expectedValue) {
            this.expressions.put(expression, expectedValue);
            return this;
        }

        public Builder ignoreNotFound(boolean ignore) {
            this.ignoreNotFound = ignore;
            return this;
        }

        @Override
        public JsonPathMessageProcessor build() {
            return new JsonPathMessageProcessor(this);
        }
    }

    public Map<String, Object> getJsonPathExpressions() {
        return jsonPathExpressions;
    }

    /**
     * Gets the ignoreNotFound.
     *
     * @return
     */
    public boolean isIgnoreNotFound() {
        return ignoreNotFound;
    }
}
