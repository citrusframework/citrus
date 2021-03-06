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

package com.consol.citrus.validation.json;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.json.JsonPathUtils;
import com.consol.citrus.message.Message;
import com.consol.citrus.variable.VariableExtractor;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.ReadContext;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

/**
 * Extractor implementation reads message elements via JSONPath expressions and saves the
 * values as new test variables. JSONObject and JSONArray items will be saved as String representation.
 *
 * @author Christoph Deppisch
 * @since 2.3
 */
public class JsonPathVariableExtractor implements VariableExtractor {

    /** Map defines json path expressions and target variable names */
    private final Map<String, String> jsonPathExpressions;

    /** Logger */
    private static final Logger LOG = LoggerFactory.getLogger(JsonPathVariableExtractor.class);

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
        if (CollectionUtils.isEmpty(jsonPathExpressions)) {return;}

        if (LOG.isDebugEnabled()) {
            LOG.debug("Reading JSON elements with JSONPath");
        }

        String jsonPathExpression;
        try {
            JSONParser parser = new JSONParser(JSONParser.MODE_JSON_SIMPLE);
            Object receivedJson = parser.parse(message.getPayload(String.class));
            ReadContext readerContext = JsonPath.parse(receivedJson);

            for (Map.Entry<String, String> entry : jsonPathExpressions.entrySet()) {
                jsonPathExpression = context.replaceDynamicContentInString(entry.getKey());
                String variableName = entry.getValue();

                if (LOG.isDebugEnabled()) {
                    LOG.debug("Evaluating JSONPath expression: " + jsonPathExpression);
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
    public static final class Builder implements VariableExtractor.Builder<JsonPathVariableExtractor, Builder> {
        private final Map<String, String> expressions = new HashMap<>();

        @Override
        public Builder expressions(Map<String, String> expressions) {
            this.expressions.putAll(expressions);
            return this;
        }

        @Override
        public Builder expression(final String expression, final String variableName) {
            this.expressions.put(expression, variableName);
            return this;
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
    public Map<String, String> getJsonPathExpressions() {
        return jsonPathExpressions;
    }
}
