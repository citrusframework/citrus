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

package org.citrusframework.json;

import java.util.Optional;

import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import com.jayway.jsonpath.ReadContext;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.util.StringUtils;
import org.citrusframework.validation.json.JsonPathFunctions;

/**
 * @author Christoph Deppisch
 * @since 2.7.4
 */
public class JsonPathUtils {

    /**
     * Evaluate JsonPath expression on given payload string and return result as object.
     * @param payload
     * @param jsonPathExpression
     * @return
     */
    public static Object evaluate(String payload, String jsonPathExpression) {
        try {
            JSONParser parser = new JSONParser(JSONParser.MODE_JSON_SIMPLE);
            Object receivedJson = parser.parse(payload);
            ReadContext readerContext = JsonPath.parse(receivedJson);

            return evaluate(readerContext, jsonPathExpression);
        } catch (ParseException e) {
            throw new CitrusRuntimeException("Failed to parse JSON text", e);
        }
    }

    /**
     * Evaluate JsonPath expression using given read context and return result as object.
     * @param readerContext
     * @param jsonPathExpression
     * @return
     */
    public static Object evaluate(ReadContext readerContext, String jsonPathExpression) {
        String expression = jsonPathExpression;
        String jsonPathFunction = null;
        for (String name : JsonPathFunctions.getSupportedFunctions()) {
            if (expression.endsWith(String.format(".%s()", name))) {
                jsonPathFunction = name;
                expression = expression.substring(0, expression.length() - String.format(".%s()", name).length());
            }
        }

        Object jsonPathResult = null;
        PathNotFoundException pathNotFoundException = null;
        try {
            if (JsonPath.isPathDefinite(expression)) {
                jsonPathResult = readerContext.read(expression);
            } else {
                JSONArray values = readerContext.read(expression);
                if (values.size() == 1) {
                    jsonPathResult = values.get(0);
                } else {
                    jsonPathResult = values;
                }
            }
        } catch (PathNotFoundException e) {
            pathNotFoundException = e;
        }

        if (StringUtils.hasText(jsonPathFunction)) {
            jsonPathResult = JsonPathFunctions.evaluate(jsonPathResult, jsonPathFunction);
        }

        if (jsonPathResult == null && pathNotFoundException != null) {
            throw new CitrusRuntimeException(String.format("Failed to evaluate JSON path expression: %s", jsonPathExpression), pathNotFoundException);
        }

        return jsonPathResult;
    }

    /**
     * Evaluate JsonPath expression on given payload string and return result as string.
     * @param payload
     * @param jsonPathExpression
     * @return
     */
    public static String evaluateAsString(String payload, String jsonPathExpression) {
        try {
            JSONParser parser = new JSONParser(JSONParser.MODE_JSON_SIMPLE);
            Object receivedJson = parser.parse(payload);
            ReadContext readerContext = JsonPath.parse(receivedJson);

            return evaluateAsString(readerContext, jsonPathExpression);
        } catch (ParseException e) {
            throw new CitrusRuntimeException("Failed to parse JSON text", e);
        }
    }

    /**
     * Evaluate JsonPath expression using given read context and return result as string.
     * @param readerContext
     * @param jsonPathExpression
     * @return
     */
    public static String evaluateAsString(ReadContext readerContext, String jsonPathExpression) {
        Object jsonPathResult = evaluate(readerContext, jsonPathExpression);

        if (jsonPathResult instanceof JSONArray) {
            return ((JSONArray) jsonPathResult).toJSONString();
        } else if (jsonPathResult instanceof JSONObject) {
            return ((JSONObject) jsonPathResult).toJSONString();
        } else {
            return Optional.ofNullable(jsonPathResult).map(Object::toString).orElse("null");
        }
    }
}
