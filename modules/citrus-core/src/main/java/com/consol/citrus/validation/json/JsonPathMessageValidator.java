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
import com.consol.citrus.exceptions.ValidationException;
import com.consol.citrus.message.Message;
import com.consol.citrus.message.MessageType;
import com.consol.citrus.validation.AbstractMessageValidator;
import com.consol.citrus.validation.ValidationUtils;
import com.jayway.jsonpath.*;
import net.minidev.json.JSONArray;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.Map;

/**
 * Message validator evaluates set of JSONPath expressions on message payload and checks that values are as expected.
 * @author Christoph Deppisch
 * @since 2.3
 */
public class JsonPathMessageValidator extends AbstractMessageValidator<JsonPathMessageValidationContext> {
    /** Logger */
    private static Logger log = LoggerFactory.getLogger(JsonPathMessageValidator.class);

    @Override
    public void validateMessage(Message receivedMessage, Message controlMessage, TestContext context, JsonPathMessageValidationContext validationContext) throws ValidationException {
        if (CollectionUtils.isEmpty(validationContext.getJsonPathExpressions())) { return; }

        if (receivedMessage.getPayload() == null || !StringUtils.hasText(receivedMessage.getPayload(String.class))) {
            throw new ValidationException("Unable to validate message elements - receive message payload was empty");
        }

        log.debug("Start JSONPath element validation");

        String jsonPathExpression = null;
        try {
            JSONParser parser = new JSONParser(JSONParser.MODE_JSON_SIMPLE);
            Object receivedJson = parser.parse(receivedMessage.getPayload(String.class));
            ReadContext readerContext = JsonPath.parse(receivedJson);

            for (Map.Entry<String, Object> entry : validationContext.getJsonPathExpressions().entrySet()) {
                jsonPathExpression = context.replaceDynamicContentInString(entry.getKey());

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
                        jsonPathResult = values;
                    }
                }

                if (StringUtils.hasText(jsonPathFunction)) {
                    jsonPathResult = JsonPathFunctions.evaluate(jsonPathResult, jsonPathFunction);
                }

                Object expectedValue = entry.getValue();
                if (expectedValue instanceof String) {
                    //check if expected value is variable or function (and resolve it, if yes)
                    expectedValue = context.replaceDynamicContentInString(String.valueOf(expectedValue));
                }

                //do the validation of actual and expected value for element
                ValidationUtils.validateValues(jsonPathResult, expectedValue, jsonPathExpression, context);

                if (log.isDebugEnabled()) {
                    log.debug("Validating element: " + jsonPathExpression + "='" + expectedValue + "': OK.");
                }
            }

            log.info("JSONPath element validation successful: All elements OK");
        } catch (ParseException e) {
            throw new CitrusRuntimeException("Failed to parse JSON text", e);
        } catch (PathNotFoundException e) {
            throw new ValidationException(String.format("Failed to validate JSON element for path: %s", jsonPathExpression), e);
        }
    }

    @Override
    protected Class<JsonPathMessageValidationContext> getRequiredValidationContextType() {
        return JsonPathMessageValidationContext.class;
    }

    @Override
    public boolean supportsMessageType(String messageType, Message message) {
        return messageType.equalsIgnoreCase(MessageType.JSON.toString());

    }
}
