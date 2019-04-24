/*
 * Copyright 2006-2019 the original author or authors.
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

import com.consol.citrus.Citrus;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.exceptions.ValidationException;
import com.consol.citrus.json.JsonSchemaRepository;
import com.consol.citrus.message.Message;
import com.consol.citrus.message.MessageType;
import com.consol.citrus.validation.AbstractMessageValidator;
import com.consol.citrus.validation.ValidationUtils;
import com.consol.citrus.validation.json.schema.JsonSchemaValidation;
import com.consol.citrus.validation.matcher.ValidationMatcherUtils;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.ReadContext;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
public class JsonTextMessageValidator extends AbstractMessageValidator<JsonMessageValidationContext> implements ApplicationContextAware {

    /** Should also check exact amount of object fields */
    @Value("${citrus.json.message.validation.strict:true}")
    private boolean strict = true;

    /** Root application context this validator is defined in */
    private ApplicationContext applicationContext;

    @Autowired(required = false)
    private List<JsonSchemaRepository> schemaRepositories = new ArrayList<>();

    /** Schema validator */
    private JsonSchemaValidation jsonSchemaValidation = new JsonSchemaValidation();

    @Override
    public void validateMessage(final Message receivedMessage,
                                final Message controlMessage,
                                final TestContext context,
                                final JsonMessageValidationContext validationContext) {
        if (controlMessage == null || controlMessage.getPayload() == null) {
            log.debug("Skip message payload validation as no control message was defined");
            return;
        }

        log.debug("Start JSON message validation ...");

        if (validationContext.isSchemaValidationEnabled()) {
            performSchemaValidation(receivedMessage, validationContext);
        }

        if (log.isDebugEnabled()) {
            log.debug("Received message:\n{}", receivedMessage);
            log.debug("Control message:\n{}", controlMessage);
        }

        final String receivedJsonText = receivedMessage.getPayload(String.class);
        final String controlJsonText = context.replaceDynamicContentInString(controlMessage.getPayload(String.class));
        
        try {
            if (!StringUtils.hasText(controlJsonText)) {
                log.debug("Skip message payload validation as no control message was defined");
                return;
            } else {
                Assert.isTrue(StringUtils.hasText(receivedJsonText), "Validation failed - " +
                		"expected message contents, but received empty message!");
            }
            
            final JSONParser parser = new JSONParser(JSONParser.MODE_JSON_SIMPLE);
        
            final Object receivedJson = parser.parse(receivedJsonText);
            final ReadContext readContext = JsonPath.parse(receivedJson);
            final Object controlJson = parser.parse(controlJsonText);
            if (receivedJson instanceof JSONObject) {
                validateJson("$.", (JSONObject) receivedJson, (JSONObject) controlJson, validationContext, context, readContext);
            } else if (receivedJson instanceof JSONArray) {
                final JSONObject tempReceived = new JSONObject();
                tempReceived.put("array", receivedJson);
                final JSONObject tempControl = new JSONObject();
                tempControl.put("array", controlJson);
                
                validateJson("$.", tempReceived, tempControl, validationContext, context, readContext);
            } else {
                throw new CitrusRuntimeException("Unsupported json type " + receivedJson.getClass());
            }
        } catch (final IllegalArgumentException e) {
            throw new ValidationException(String.format("Failed to validate JSON text:%n%s", receivedJsonText), e);
        } catch (final ParseException e) {
            throw new CitrusRuntimeException("Failed to parse JSON text", e);
        }
        
        log.info("JSON message validation successful: All values OK");
    }

    /**
     * Performs the schema validation for the given message under consideration of the given validation context
     * @param receivedMessage The message to be validated
     * @param validationContext The validation context of the current test
     */
    private void performSchemaValidation(final Message receivedMessage, final JsonMessageValidationContext validationContext) {
        log.debug("Starting Json schema validation ...");

        final ProcessingReport report = jsonSchemaValidation.validate(receivedMessage,
                                                                schemaRepositories,
                                                                validationContext,
                                                                applicationContext);
        if (!report.isSuccess()) {
            log.error("Failed to validate Json schema for message:\n{}", receivedMessage.getPayload(String.class));

            throw new ValidationException(constructErrorMessage(report));
        }

        log.info("Json schema validation successful: All values OK");
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
    public void validateJson(final String elementName,
                             final JSONObject receivedJson,
                             final JSONObject controlJson,
                             final JsonMessageValidationContext validationContext,
                             final TestContext context,
                             final ReadContext readContext) {
        if (strict) {
            Assert.isTrue(controlJson.size() == receivedJson.size(),
                          ValidationUtils.buildValueMismatchErrorMessage("Number of JSON entries not equal for element: '" + elementName + "'", controlJson.size(), receivedJson.size()));
        }

        for (final Map.Entry<String, Object> controlJsonEntry : controlJson.entrySet()) {
            final String controlKey = controlJsonEntry.getKey();

            Assert.isTrue(receivedJson.containsKey(controlKey),
                    "Missing JSON entry: + '" + controlKey + "'");

            final Object controlValue = controlJsonEntry.getValue();
            final Object receivedValue = receivedJson.get(controlKey);

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

                    final JSONArray jsonArrayControl = (JSONArray) controlValue;
                    final JSONArray jsonArrayReceived = (JSONArray) receivedValue;

                    if (log.isDebugEnabled()) {
                        log.debug("Validating JSONArray containing {} entries", jsonArrayControl.size());
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
                    final String processedControlValue = ValidationMatcherUtils.substituteIgnoreStatements(
                                    controlValue.toString(),
                                    receivedValue.toString());

                    Assert.isTrue(processedControlValue.equals(receivedValue),
                            ValidationUtils.buildValueMismatchErrorMessage("Values not equal for entry: '" + controlKey + "'",
                                    processedControlValue, receivedValue));
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
                log.debug("Validation successful for JSON entry '{}' ({})", controlKey, controlValue);
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
    public boolean isIgnored(final String controlKey,
                             final Object controlValue,
                             final Object receivedJson,
                             final Set<String> ignoreExpressions,
                             final ReadContext readContext) {
        if (controlValue != null && controlValue.toString().trim().equals(Citrus.IGNORE_PLACEHOLDER)) {
            if (log.isDebugEnabled()) {
                log.debug("JSON entry: '{}' is ignored by placeholder '{}'", controlKey,Citrus.IGNORE_PLACEHOLDER );
            }
            return true;
        }

        for (final String jsonPathExpression : ignoreExpressions) {
            final Object foundEntry = readContext.read(jsonPathExpression);

            if (foundEntry instanceof JSONArray && ((JSONArray) foundEntry).contains(receivedJson)) {
                if (log.isDebugEnabled()) {
                    log.debug("JSON entry: '{}' is ignored - skip value validation", controlKey);
                }
                return true;
            }

            if (foundEntry != null && foundEntry.equals(receivedJson)) {
                if (log.isDebugEnabled()) {
                    log.debug("JSON entry: '{}' is ignored - skip value validation", controlKey);
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
    public boolean supportsMessageType(final String messageType, final Message message) {
        if (!messageType.equalsIgnoreCase(MessageType.JSON.name())) {
            return false;
        }

        if (!(message.getPayload() instanceof String)) {
            return false;
        }

        if (StringUtils.hasText(message.getPayload(String.class)) &&
                !message.getPayload(String.class).trim().startsWith("{") &&
                !message.getPayload(String.class).trim().startsWith("[")) {
            return false;
        }

        return true;
    }

    /**
     * Set the validator strict mode.
     * @param strict
     */
    public void setStrict(final boolean strict) {
        this.strict = strict;
    }

    /**
     * Set the validator strict mode.
     * @param strict
     * @return this object for chaining
     */
    public JsonTextMessageValidator strict(final boolean strict) {
        setStrict(strict);
        return this;
    }

    void setSchemaRepositories(final List<JsonSchemaRepository> schemaRepositories) {
        this.schemaRepositories = schemaRepositories;
    }

    /**
     * Constructs the error message of a failed validation based on the processing report passed from
     * com.github.fge.jsonschema.core.report
     * @param report The report containing the error message
     * @return A string representation of all messages contained in the report
     */
    private String constructErrorMessage(final ProcessingReport report) {
        final StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Json validation failed: ");
        report.forEach(processingMessage -> stringBuilder.append(processingMessage.getMessage()));
        return stringBuilder.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setApplicationContext(final ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    void setJsonSchemaValidation(final JsonSchemaValidation jsonSchemaValidation) {
        this.jsonSchemaValidation = jsonSchemaValidation;
    }
}
