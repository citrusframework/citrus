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

package com.consol.citrus.endpoint.adapter.mapping;

import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.exceptions.ValidationException;
import com.consol.citrus.message.Message;
import com.consol.citrus.validation.json.JsonPathFunctions;
import com.jayway.jsonpath.*;
import net.minidev.json.JSONArray;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;
import org.springframework.util.StringUtils;

/**
 * @author Christoph Deppisch
 * @since 2.6
 */
public class JsonPayloadMappingKeyExtractor extends AbstractMappingKeyExtractor {

    /** XPath expression evaluated on message payload */
    private String jsonPathExpression = "$.keySet()";

    @Override
    public String getMappingKey(Message request) {
        try {
            JSONParser parser = new JSONParser(JSONParser.MODE_JSON_SIMPLE);
            Object receivedJson = parser.parse(request.getPayload(String.class));
            ReadContext readerContext = JsonPath.parse(receivedJson);

            String jsonPathFunction = null;
            String expression = jsonPathExpression;
            for (String name : JsonPathFunctions.getSupportedFunctions()) {
                if (expression.endsWith(String.format(".%s()", name))) {
                    jsonPathFunction = name;
                    expression = expression.substring(0, expression.length() - String.format(".%s()", name).length());
                }
            }

            Object jsonPathResult;
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

            if (StringUtils.hasText(jsonPathFunction)) {
                jsonPathResult = JsonPathFunctions.evaluate(jsonPathResult, jsonPathFunction);
            }

            return jsonPathResult.toString();
        } catch (ParseException e) {
            throw new CitrusRuntimeException("Failed to parse JSON text", e);
        } catch (PathNotFoundException e) {
            throw new ValidationException(String.format("Failed to extract JSON element for path: %s", jsonPathExpression), e);
        }
    }

    /**
     * Sets the jsonPathExpression property.
     *
     * @param jsonPathExpression
     */
    public void setJsonPathExpression(String jsonPathExpression) {
        this.jsonPathExpression = jsonPathExpression;
    }
}
