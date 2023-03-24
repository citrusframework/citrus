/*
 * Copyright 2006-2011 the original author or authors.
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.citrusframework.CitrusSettings;
import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.exceptions.ValidationException;
import org.citrusframework.json.JsonSchemaRepository;
import org.citrusframework.json.JsonSettings;
import org.citrusframework.message.Message;
import org.citrusframework.message.MessageType;
import org.citrusframework.util.MessageUtils;
import org.citrusframework.validation.AbstractMessageValidator;
import org.citrusframework.validation.ValidationUtils;
import org.citrusframework.validation.json.schema.JsonSchemaValidation;
import org.citrusframework.validation.matcher.ValidationMatcherUtils;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.ReadContext;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * This message validator implementation is able to validate two JSON text objects. The order of JSON entries can differ
 * as specified in JSON protocol. Tester defines an expected control JSON text with optional ignored entries.
 *
 * JSONArray as well as nested JSONObjects are supported, too.
 *
 * Validator offers two different modes to operate. By default strict mode is set and the validator will also check the exact amount of
 * control object fields to match. No additional fields in received JSON data structure will be accepted. In soft mode validator
 * allows additional fields in received JSON data structure so the control JSON object can be a partial subset.
 *
 * @author Christoph Deppisch
 */
public class JsonTextMessageValidator extends AbstractMessageValidator<JsonMessageValidationContext> {

    /** Should also check exact amount of object fields */
    private boolean strict = JsonSettings.isStrict();

    /** Permissive mode to use on the Json parser */
    private int permissiveMode = JsonSettings.getPermissiveMoe();

    /** Schema validator */
    private JsonSchemaValidation jsonSchemaValidation = new JsonSchemaValidation();

    @Override
    @SuppressWarnings("unchecked")
    public void validateMessage(Message receivedMessage, Message controlMessage,
                                TestContext context, JsonMessageValidationContext validationContext) {
        if (controlMessage == null || controlMessage.getPayload() == null) {
            log.debug("Skip message payload validation as no control message was defined");
            return;
        }

        log.debug("Start JSON message validation ...");

        if (validationContext.isSchemaValidationEnabled()) {
            jsonSchemaValidation.validate(receivedMessage, context, validationContext);
        }

        String receivedJsonText = receivedMessage.getPayload(String.class);
        String controlJsonText = context.replaceDynamicContentInString(controlMessage.getPayload(String.class));

        try {
            if (!StringUtils.hasText(controlJsonText)) {
                log.debug("Skip message payload validation as no control message was defined");
                return;
            } else {
                Assert.isTrue(StringUtils.hasText(receivedJsonText), "Validation failed - " +
                		"expected message contents, but received empty message!");
            }

            JSONParser parser = new JSONParser(permissiveMode);

            Object receivedJson = parser.parse(receivedJsonText);
            ReadContext readContext = JsonPath.parse(receivedJson);
            Object controlJson = parser.parse(controlJsonText);
            if (receivedJson instanceof JSONObject) {
                validateJson("$.", (JSONObject) receivedJson, (JSONObject) controlJson, validationContext, context, readContext);
            } else if (receivedJson instanceof JSONArray) {
                JSONObject tempReceived = new JSONObject();
                tempReceived.put("array", receivedJson);
                JSONObject tempControl = new JSONObject();
                tempControl.put("array", controlJson);

                validateJson("$.", tempReceived, tempControl, validationContext, context, readContext);
            } else {
                throw new CitrusRuntimeException("Unsupported json type " + receivedJson.getClass());
            }
        } catch (IllegalArgumentException e) {
            throw new ValidationException(String.format("Failed to validate JSON text:%n%s", receivedJsonText), e);
        } catch (ParseException e) {
            throw new CitrusRuntimeException("Failed to parse JSON text", e);
        }

        log.info("JSON message validation successful: All values OK");
    }

    /**
     * Find json schema repositories in test context.
     * @param context
     * @return
     */
    private List<JsonSchemaRepository> findSchemaRepositories(TestContext context) {
        return new ArrayList<>(context.getReferenceResolver().resolveAll(JsonSchemaRepository.class).values());
    }

    /**
     * Validates JSON text with comparison to expected control JSON object.
     * JSON entries can be ignored with ignore placeholder.
     *
     * @param elementName the current element name that is under verification in this method
     * @param receivedJson the received JSON text object.
     * @param controlJson the expected control JSON text.
     * @param validationContext the JSON message validation context.
     * @param context the current test context.
     * @param readContext the JSONPath read context.
     */
    @SuppressWarnings("rawtypes")
    public void validateJson(String elementName, JSONObject receivedJson, JSONObject controlJson, JsonMessageValidationContext validationContext, TestContext context, ReadContext readContext) {
        if (strict) {
            Assert.isTrue(controlJson.size() == receivedJson.size(),
                          ValidationUtils.buildValueMismatchErrorMessage("Number of JSON entries not equal for element: '" + elementName + "'", controlJson.size(), receivedJson.size()));
        }

        for (Map.Entry<String, Object> controlJsonEntry : controlJson.entrySet()) {
            String controlKey = controlJsonEntry.getKey();

            Assert.isTrue(receivedJson.containsKey(controlKey),
                    "Missing JSON entry: + '" + controlKey + "'");

            Object controlValue = controlJsonEntry.getValue();
            Object receivedValue = receivedJson.get(controlKey);

            // check if entry is ignored by placeholder
            if (isIgnored(controlKey, controlValue, receivedValue, validationContext.getIgnoreExpressions(), readContext)) {
                continue;
            }

            if (controlValue == null) {
                Assert.isTrue(receivedValue == null,
                        ValidationUtils.buildValueMismatchErrorMessage("Values not equal for entry: '" + controlKey + "'",
                                null, receivedValue));
            } else if (receivedValue != null) {
                if (ValidationMatcherUtils.isValidationMatcherExpression(controlValue.toString())) {
                    ValidationMatcherUtils.resolveValidationMatcher(controlKey,
                            receivedValue.toString(),
                            controlValue.toString(), context);
                } else if (controlValue instanceof JSONObject) {
                    Assert.isTrue(receivedValue instanceof JSONObject,
                            ValidationUtils.buildValueMismatchErrorMessage("Type mismatch for JSON entry '" + controlKey + "'",
                                    JSONObject.class.getSimpleName(), receivedValue.getClass().getSimpleName()));

                    validateJson(controlKey, (JSONObject) receivedValue,
                            (JSONObject) controlValue, validationContext, context, readContext);
                } else if (controlValue instanceof JSONArray) {
                    Assert.isTrue(receivedValue instanceof JSONArray,
                            ValidationUtils.buildValueMismatchErrorMessage("Type mismatch for JSON entry '" + controlKey + "'",
                                    JSONArray.class.getSimpleName(), receivedValue.getClass().getSimpleName()));

                    JSONArray jsonArrayControl = (JSONArray) controlValue;
                    JSONArray jsonArrayReceived = (JSONArray) receivedValue;

                    if (log.isDebugEnabled()) {
                        log.debug("Validating JSONArray containing " + jsonArrayControl.size() + " entries");
                    }

                    if (strict) {
                        Assert.isTrue(jsonArrayControl.size() == jsonArrayReceived.size(),
                                ValidationUtils.buildValueMismatchErrorMessage("JSONArray size mismatch for JSON entry '" + controlKey + "'",
                                        jsonArrayControl.size(), jsonArrayReceived.size()));
                    }
                    for (int i = 0; i < jsonArrayControl.size(); i++) {
                        if (jsonArrayControl.get(i).getClass().isAssignableFrom(JSONObject.class)) {
                            Assert.isTrue(jsonArrayReceived.get(i).getClass().isAssignableFrom(JSONObject.class),
                                    ValidationUtils.buildValueMismatchErrorMessage("Value types not equal for entry: '" + jsonArrayControl.get(i) + "'",
                                            JSONObject.class.getName(), jsonArrayReceived.get(i).getClass().getName()));

                            validateJson(controlKey, (JSONObject) jsonArrayReceived.get(i),
                                    (JSONObject) jsonArrayControl.get(i), validationContext, context, readContext);
                        } else {
                            Assert.isTrue(jsonArrayControl.get(i).equals(jsonArrayReceived.get(i)),
                                    ValidationUtils.buildValueMismatchErrorMessage("Values not equal for entry: '" + jsonArrayControl.get(i) + "'",
                                            jsonArrayControl.get(i), jsonArrayReceived.get(i)));
                        }
                    }
                } else {
                    Assert.isTrue(controlValue.equals(receivedValue),
                            ValidationUtils.buildValueMismatchErrorMessage("Values not equal for entry: '" + controlKey + "'",
                                    controlValue, receivedValue));
                }
            } else if (ValidationMatcherUtils.isValidationMatcherExpression(controlValue.toString())) {
                ValidationMatcherUtils.resolveValidationMatcher(controlKey,
                        null,
                        controlValue.toString(), context);
            } else {
                Assert.isTrue(!StringUtils.hasText(controlValue.toString()),
                        ValidationUtils.buildValueMismatchErrorMessage(
                                "Values not equal for entry '" + controlKey + "'", controlValue.toString(), null));
            }

            if (log.isDebugEnabled()) {
                log.debug("Validation successful for JSON entry '" + controlKey + "' (" + controlValue + ")");
            }
        }
    }

    /**
     * Checks if given element node is either on ignore list or
     * contains @ignore@ tag inside control message
     * @param controlKey
     * @param controlValue
     * @param receivedJson
     * @param ignoreExpressions
     * @param readContext
     * @return
     */
    public boolean isIgnored(String controlKey, Object controlValue, Object receivedJson, Set<String> ignoreExpressions, ReadContext readContext) {
        if (controlValue != null && controlValue.toString().trim().equals(CitrusSettings.IGNORE_PLACEHOLDER)) {
            if (log.isDebugEnabled()) {
                log.debug("JSON entry: '" + controlKey + "' is ignored by placeholder '" +
                        CitrusSettings.IGNORE_PLACEHOLDER + "'");
            }
            return true;
        }

        for (String jsonPathExpression : ignoreExpressions) {
            Object foundEntry = readContext.read(jsonPathExpression);

            if (foundEntry instanceof JSONArray && ((JSONArray) foundEntry).contains(receivedJson)) {
                if (log.isDebugEnabled()) {
                    log.debug("JSON entry: '" + controlKey + "' is ignored - skip value validation");
                }
                return true;
            }

            if (foundEntry != null && foundEntry.equals(receivedJson)) {
                if (log.isDebugEnabled()) {
                    log.debug("JSON entry: '" + controlKey + "' is ignored - skip value validation");
                }
                return true;
            }
        }

        return false;
    }

    @Override
    protected Class<JsonMessageValidationContext> getRequiredValidationContextType() {
        return JsonMessageValidationContext.class;
    }

    @Override
    public boolean supportsMessageType(String messageType, Message message) {
        return messageType.equalsIgnoreCase(MessageType.JSON.name()) && MessageUtils.hasJsonPayload(message);
    }

    /**
     * Set the validator strict mode.
     * @param strict
     */
    public void setStrict(boolean strict) {
        this.strict = strict;
    }

    /**
     * Set the validator strict mode.
     * @param strict
     * @return this object for chaining
     */
    public JsonTextMessageValidator strict(boolean strict) {
        setStrict(strict);
        return this;
    }



    /**
     * Sets the json schema validation.
     * @param jsonSchemaValidation
     */
    void setJsonSchemaValidation(JsonSchemaValidation jsonSchemaValidation) {
        this.jsonSchemaValidation = jsonSchemaValidation;
    }

    /**
     * Sets the json schema validation.
     * @param jsonSchemaValidation
     * @return this object for chaining
     */
    public JsonTextMessageValidator jsonSchemaValidation(JsonSchemaValidation jsonSchemaValidation) {
        setJsonSchemaValidation(jsonSchemaValidation);
        return this;
    }

    /**
     * Sets the permissive mode.
     * @param permissiveMode
     */
    public void setPermissiveMode(int permissiveMode) {
        this.permissiveMode = permissiveMode;
    }

    /**
     * Sets the permissive mode
     * @param permissiveMode
     * @return this object for chaining
     */
    public JsonTextMessageValidator permissiveMode(int permissiveMode) {
        setPermissiveMode(permissiveMode);
        return this;
    }
}
