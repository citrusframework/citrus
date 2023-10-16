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

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.ReadContext;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;
import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.exceptions.ValidationException;
import org.citrusframework.json.JsonPathUtils;
import org.citrusframework.message.Message;
import org.citrusframework.util.StringUtils;
import org.citrusframework.validation.AbstractMessageValidator;
import org.citrusframework.validation.ValidationUtils;
import org.citrusframework.validation.context.ValidationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Message validator evaluates set of JSONPath expressions on message payload and checks that values are as expected.
 * @author Christoph Deppisch
 * @since 2.3
 */
public class JsonPathMessageValidator extends AbstractMessageValidator<JsonPathMessageValidationContext> {
    /** Logger */
    private static final Logger logger = LoggerFactory.getLogger(JsonPathMessageValidator.class);

    @Override
    public void validateMessage(Message receivedMessage, Message controlMessage, TestContext context, JsonPathMessageValidationContext validationContext) throws ValidationException {
        if (validationContext.getJsonPathExpressions() == null || validationContext.getJsonPathExpressions().isEmpty()) {
            return;
        }

        if (receivedMessage.getPayload() == null || !StringUtils.hasText(receivedMessage.getPayload(String.class))) {
            throw new ValidationException("Unable to validate message elements - receive message payload was empty");
        }

        logger.debug("Start JSONPath element validation ...");

        String jsonPathExpression;
        try {
            JSONParser parser = new JSONParser(JSONParser.MODE_JSON_SIMPLE);
            Object receivedJson = parser.parse(receivedMessage.getPayload(String.class));
            ReadContext readerContext = JsonPath.parse(receivedJson);

            for (Map.Entry<String, Object> entry : validationContext.getJsonPathExpressions().entrySet()) {
                Object expectedValue = entry.getValue();
                if (expectedValue instanceof String) {
                    //check if expected value is variable or function (and resolve it, if yes)
                    expectedValue = context.replaceDynamicContentInString(String.valueOf(expectedValue));
                }

                jsonPathExpression = context.replaceDynamicContentInString(entry.getKey());
                Object jsonPathResult = JsonPathUtils.evaluate(readerContext, jsonPathExpression);
                //do the validation of actual and expected value for element
                ValidationUtils.validateValues(jsonPathResult, expectedValue, jsonPathExpression, context);

                if (logger.isDebugEnabled()) {
                    logger.debug("Validating element: " + jsonPathExpression + "='" + expectedValue + "': OK.");
                }
            }

            logger.info("JSONPath element validation successful: All values OK");
        } catch (ParseException e) {
            throw new CitrusRuntimeException("Failed to parse JSON text", e);
        }
    }

    @Override
    protected Class<JsonPathMessageValidationContext> getRequiredValidationContextType() {
        return JsonPathMessageValidationContext.class;
    }

    @Override
    public JsonPathMessageValidationContext findValidationContext(List<ValidationContext> validationContexts) {
        List<JsonPathMessageValidationContext> jsonPathMessageValidationContexts = validationContexts.stream()
                .filter(JsonPathMessageValidationContext.class::isInstance)
                .map(JsonPathMessageValidationContext.class::cast)
                .collect(Collectors.toList());

        if (jsonPathMessageValidationContexts.size() > 1) {
            // Collect all jsonPath expressions and combine into one single validation context
            Map<String, Object> jsonPathExpressions = jsonPathMessageValidationContexts.stream()
                    .map(JsonPathMessageValidationContext::getJsonPathExpressions)
                    .reduce((collect, map) -> {
                        collect.putAll(map);
                        return collect;
                    })
                    .orElseGet(Collections::emptyMap);

            if (!jsonPathExpressions.isEmpty()) {
                return new JsonPathMessageValidationContext.Builder().expressions(jsonPathExpressions).build();
            }
        }

        return super.findValidationContext(validationContexts);
    }

    @Override
    public boolean supportsMessageType(String messageType, Message message) {
        return new JsonTextMessageValidator().supportsMessageType(messageType, message);

    }
}
