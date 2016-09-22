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

import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.exceptions.UnknownElementException;
import com.consol.citrus.message.Message;
import com.consol.citrus.variable.VariableExtractor;
import com.jayway.jsonpath.*;
import net.minidev.json.JSONArray;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Extractor implementation reads message elements via JSONPath expressions and saves the
 * values as new test variables. JSONObject and JSONArray items will be saved as String representation.
 *
 * @author Christoph Deppisch
 * @since 2.3
 */
public class JsonPathVariableExtractor implements VariableExtractor {

    /** Map defines xpath expressions and target variable names */
    private Map<String, String> jsonPathExpressions = new HashMap<>();

    /** Logger */
    private static Logger log = LoggerFactory.getLogger(JsonPathVariableExtractor.class);

    @Override
    public void extractVariables(Message message, TestContext context) {
        if (CollectionUtils.isEmpty(jsonPathExpressions)) {return;}

        if (log.isDebugEnabled()) {
            log.debug("Reading JSON elements with JSONPath");
        }

        String jsonPathExpression = null;
        try {
            JSONParser parser = new JSONParser(JSONParser.MODE_JSON_SIMPLE);
            Object receivedJson = parser.parse(message.getPayload(String.class));
            ReadContext readerContext = JsonPath.parse(receivedJson);

            for (Map.Entry<String, String> entry : jsonPathExpressions.entrySet()) {
                jsonPathExpression = context.replaceDynamicContentInString(entry.getKey());
                String variableName = entry.getValue();

                if (log.isDebugEnabled()) {
                    log.debug("Evaluating JSONPath expression: " + jsonPathExpression);
                }

                String jsonPathFunction = null;
                for (String name : JsonPathFunctions.getSupportedFunctions()) {
                    if (jsonPathExpression.endsWith(String.format(".%s()", name))) {
                        jsonPathFunction = name;
                        jsonPathExpression = jsonPathExpression.substring(0, jsonPathExpression.length() - String.format(".%s()", name).length());
                    }
                }

                Object jsonPathResult;
                if (JsonPath.isPathDefinite(jsonPathExpression)) {
                    jsonPathResult = readerContext.read(jsonPathExpression);
                } else {
                    JSONArray values = readerContext.read(jsonPathExpression);
                    if (values.size() == 1) {
                        jsonPathResult = values.get(0);
                    } else {
                        jsonPathResult = values.toJSONString();
                    }
                }

                if (StringUtils.hasText(jsonPathFunction)) {
                    jsonPathResult = JsonPathFunctions.evaluate(jsonPathResult, jsonPathFunction);
                }

                context.setVariable(variableName, jsonPathResult.toString());
            }
        } catch (ParseException e) {
            throw new CitrusRuntimeException("Failed to parse JSON text", e);
        } catch (PathNotFoundException e) {
            throw new UnknownElementException(String.format("Could not find element for expression: %s", jsonPathExpression), e);
        }
    }

    /**
     * Sets the JSONPath expressions.
     * @param jsonPathExpressions
     */
    public void setJsonPathExpressions(Map<String, String> jsonPathExpressions) {
        this.jsonPathExpressions = jsonPathExpressions;
    }

    /**
     * Gets the JSONPath expressions.
     * @return
     */
    public Map<String, String> getJsonPathExpressions() {
        return jsonPathExpressions;
    }
}
