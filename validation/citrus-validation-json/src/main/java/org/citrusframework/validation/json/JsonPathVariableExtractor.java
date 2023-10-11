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
import java.util.Optional;

import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.ReadContext;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;
import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.json.JsonPathUtils;
import org.citrusframework.message.DelegatingPathExpressionProcessor;
import org.citrusframework.message.Message;
import org.citrusframework.message.MessageProcessor;
import org.citrusframework.message.MessageProcessorAdapter;
import org.citrusframework.validation.PathExpressionValidationContext;
import org.citrusframework.validation.ValidationContextAdapter;
import org.citrusframework.validation.context.ValidationContext;
import org.citrusframework.variable.VariableExtractor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Extractor implementation reads message elements via JSONPath expressions and saves the
 * values as new test variables. JSONObject and JSONArray items will be saved as String representation.
 *
 * @author Christoph Deppisch
 * @since 2.3
 */
public class JsonPathVariableExtractor implements VariableExtractor {

    /** Map defines json path expressions and target variable names */
    private final Map<String, Object> jsonPathExpressions;

    /** Logger */
    private static final Logger logger = LoggerFactory.getLogger(JsonPathVariableExtractor.class);

    public JsonPathVariableExtractor() {
        this(new Builder());
    }

    /**
     * Constructor using fluent builder.
     * @param builder
     */
    private JsonPathVariableExtractor(Builder builder) {
        this.jsonPathExpressions = builder.expressions;
    }

    @Override
    public void extractVariables(Message message, TestContext context) {
        if (jsonPathExpressions == null || jsonPathExpressions.isEmpty()) {
            return;
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Reading JSON elements with JSONPath");
        }

        try {
            JSONParser parser = new JSONParser(JSONParser.MODE_JSON_SIMPLE);
            Object receivedJson = parser.parse(message.getPayload(String.class));
            ReadContext readerContext = JsonPath.parse(receivedJson);

            for (Map.Entry<String, Object> entry : jsonPathExpressions.entrySet()) {
                String jsonPathExpression = context.replaceDynamicContentInString(entry.getKey());
                String variableName = Optional.ofNullable(entry.getValue())
                        .map(Object::toString)
                        .orElseThrow(() -> new CitrusRuntimeException(String.format("Variable name must be set on " +
                                "extractor path expression '%s'", jsonPathExpression)));

                if (logger.isDebugEnabled()) {
                    logger.debug("Evaluating JSONPath expression: " + jsonPathExpression);
                }

                Object jsonPathResult = JsonPathUtils.evaluate(readerContext, jsonPathExpression);
                if (jsonPathResult instanceof JSONArray) {
                    context.setVariable(variableName, ((JSONArray) jsonPathResult).toJSONString());
                } else if (jsonPathResult instanceof JSONObject) {
                    context.setVariable(variableName, ((JSONObject) jsonPathResult).toJSONString());
                } else {
                    context.setVariable(variableName, Optional.ofNullable(jsonPathResult).orElse("null"));
                }
            }
        } catch (ParseException e) {
            throw new CitrusRuntimeException("Failed to parse JSON text", e);
        }
    }

    /**
     * Fluent builder.
     */
    public static final class Builder implements VariableExtractor.Builder<JsonPathVariableExtractor, Builder>, MessageProcessorAdapter, ValidationContextAdapter {
        private final Map<String, Object> expressions = new LinkedHashMap<>();

        public static Builder fromJsonPath() {
            return new Builder();
        }

        @Override
        public Builder expressions(Map<String, Object> expressions) {
            this.expressions.putAll(expressions);
            return this;
        }

        @Override
        public Builder expression(final String expression, final Object variableName) {
            this.expressions.put(expression, variableName);
            return this;
        }

        @Override
        public MessageProcessor asProcessor() {
            return new DelegatingPathExpressionProcessor.Builder()
                    .expressions(expressions)
                    .build();
        }

        @Override
        public ValidationContext asValidationContext() {
            return new PathExpressionValidationContext.Builder()
                    .expressions(expressions)
                    .build();
        }

        @Override
        public JsonPathVariableExtractor build() {
            return new JsonPathVariableExtractor(this);
        }
    }

    /**
     * Gets the JSONPath expressions.
     * @return
     */
    public Map<String, Object> getJsonPathExpressions() {
        return jsonPathExpressions;
    }
}
