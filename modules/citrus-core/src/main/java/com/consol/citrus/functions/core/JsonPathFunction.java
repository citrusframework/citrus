/*
 * Copyright 2006-2016 the original author or authors.
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

package com.consol.citrus.functions.core;

import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.*;
import com.consol.citrus.functions.Function;
import com.consol.citrus.validation.json.JsonPathFunctions;
import com.jayway.jsonpath.*;
import net.minidev.json.JSONArray;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * @author Christoph Deppisch
 * @since 2.6.2
 */
public class JsonPathFunction implements Function {

    @Override
    public String execute(List<String> parameterList, TestContext context) {
        if (CollectionUtils.isEmpty(parameterList)) {
            throw new InvalidFunctionUsageException("Function parameters must not be empty");
        }

        if (parameterList.size() < 2) {
            throw new InvalidFunctionUsageException("Missing parameter for function - usage jsonPath('jsonSource', 'expression')");
        }

        String jsonSource;
        String jsonPathExpression;
        if (parameterList.size() > 2) {
            StringBuilder sb = new StringBuilder();
            sb.append(parameterList.get(0));
            for (int i = 1; i < parameterList.size() -1; i++) {
                sb.append(", ").append(parameterList.get(i));
            }

            jsonSource = sb.toString();
            jsonPathExpression = parameterList.get(parameterList.size() - 1);
        } else {
            jsonSource = parameterList.get(0);
            jsonPathExpression = parameterList.get(1);
        }

        try {
            JSONParser parser = new JSONParser(JSONParser.MODE_JSON_SIMPLE);
            Object receivedJson = parser.parse(context.replaceDynamicContentInString(jsonSource));
            ReadContext readerContext = JsonPath.parse(receivedJson);

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
                        jsonPathResult = values.toJSONString();
                    }
                }
            } catch (PathNotFoundException e) {
                pathNotFoundException = e;
            }

            if (StringUtils.hasText(jsonPathFunction)) {
                jsonPathResult = JsonPathFunctions.evaluate(jsonPathResult, jsonPathFunction);
            }

            if (jsonPathResult == null) {
                throw new CitrusRuntimeException(String.format("Failed to find JSON element for path: %s", jsonPathExpression), pathNotFoundException);
            }

            return jsonPathResult.toString();
        } catch (ParseException e) {
            throw new CitrusRuntimeException("Failed to parse JSON text", e);
        }
    }
}
