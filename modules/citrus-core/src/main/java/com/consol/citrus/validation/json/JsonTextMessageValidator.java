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

package com.consol.citrus.validation.json;

import com.consol.citrus.CitrusConstants;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.exceptions.ValidationException;
import com.consol.citrus.message.Message;
import com.consol.citrus.message.MessageType;
import com.consol.citrus.validation.ControlMessageValidator;
import com.consol.citrus.validation.ValidationUtils;
import com.consol.citrus.validation.context.ValidationContext;
import com.consol.citrus.validation.matcher.ValidationMatcherUtils;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.ReadContext;
import net.minidev.json.*;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;
import org.springframework.util.*;

import java.util.*;

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
public class JsonTextMessageValidator extends ControlMessageValidator<JsonMessageValidationContext> {

    /** Should also check exact amount of object fields */
    private boolean strict = true;

    @Override
    @SuppressWarnings("unchecked")
    public void validateMessagePayload(Message receivedMessage, Message controlMessage,
                                       JsonMessageValidationContext validationContext, TestContext context) throws ValidationException {
        log.info("Start JSON message validation");
        
        if (log.isDebugEnabled()) {
            log.debug("Received message:\n" + receivedMessage);
            log.debug("Control message:\n" + controlMessage);
        }

        String receivedJsonText = receivedMessage.getPayload(String.class);
        String controlJsonText = context.replaceDynamicContentInString(controlMessage.getPayload(String.class));
        
        try {
            if (!StringUtils.hasText(controlJsonText)) {
                log.info("Skip message payload validation as no control message was defined");
                return;
            } else {
                Assert.isTrue(StringUtils.hasText(receivedJsonText), "Validation failed - " +
                		"expected message contents, but received empty message!");
            }
            
            JSONParser parser = new JSONParser(JSONParser.MODE_JSON_SIMPLE);
        
            Object receivedJson = parser.parse(receivedJsonText);
            ReadContext readContext = JsonPath.parse(receivedJson);
            Object controlJson = parser.parse(controlJsonText);
            
            if (receivedJson instanceof JSONObject) {
                validateJson((JSONObject) receivedJson, (JSONObject) controlJson, validationContext, context, readContext);
            } else if (receivedJson instanceof JSONArray) {
                JSONObject tempReceived = new JSONObject();
                tempReceived.put("array", receivedJson);
                JSONObject tempControl = new JSONObject();
                tempControl.put("array", controlJson);
                
                validateJson(tempReceived, tempControl, validationContext, context, readContext);
            } else {
                throw new CitrusRuntimeException("Unsupported json type " + receivedJson.getClass());
            }
        } catch (IllegalArgumentException e) {
            throw new ValidationException(String.format("Failed to validate JSON text:%n%s", receivedJsonText), e);
        } catch (ParseException e) {
            throw new CitrusRuntimeException("Failed to parse JSON text", e);
        }
        
        log.info("JSON message validation finished successfully: All values OK");
    }
    
    /**
     * Validates JSON text with comparison to expected control JSON object.
     * JSON entries can be ignored with ignore placeholder.
     * 
     * @param receivedJson the received JSON text object.
     * @param controlJson the expected control JSON text.
     * @param validationContext the JSON message validation context.
     * @param context the current test context.
     * @param readContext the JSONPath read context.
     */
    @SuppressWarnings("rawtypes")
    public void validateJson(JSONObject receivedJson, JSONObject controlJson, JsonMessageValidationContext validationContext, TestContext context, ReadContext readContext) {
        if (strict) {
            Assert.isTrue(controlJson.size() == receivedJson.size(),
                          ValidationUtils.buildValueMismatchErrorMessage("Number of JSON entries not equal", controlJson.size(), receivedJson.size()));
        }
        
        for (Iterator it = controlJson.entrySet().iterator(); it.hasNext();) {
            Map.Entry controlJsonEntry = (Map.Entry) it.next();
            
            Assert.isTrue(receivedJson.containsKey(controlJsonEntry.getKey()), 
                    "Missing JSON entry: + '" + controlJsonEntry.getKey() + "'");
            
            if (controlJsonEntry.getValue() == null) {
                Assert.isTrue(receivedJson.get(controlJsonEntry.getKey()) == null, 
                        ValidationUtils.buildValueMismatchErrorMessage("Values not equal for entry: '" + controlJsonEntry.getKey() + "'", 
                                controlJsonEntry.getValue(), receivedJson.get(controlJsonEntry.getKey())));
                
                if (log.isDebugEnabled()) {
                    log.debug("Validation successful for JSON entry '" + controlJsonEntry.getKey() + "' (" + controlJsonEntry.getValue() + ")");
                }
                
                continue;
            }
            
            // check if entry is ignored by placeholder
            if (isIgnored(controlJsonEntry, receivedJson.get(controlJsonEntry.getKey()), validationContext.getIgnoreExpressions(), readContext)) {
                continue;
            }
            
            if (ValidationMatcherUtils.isValidationMatcherExpression(controlJsonEntry.getValue().toString())) {
                ValidationMatcherUtils.resolveValidationMatcher(controlJsonEntry.getKey().toString(), 
                        receivedJson.get(controlJsonEntry.getKey()).toString(), 
                        controlJsonEntry.getValue().toString(), context);
            } else if (controlJsonEntry.getValue() instanceof JSONObject) {
                Assert.isTrue(receivedJson.get(controlJsonEntry.getKey()) instanceof JSONObject, 
                        ValidationUtils.buildValueMismatchErrorMessage("Type mismatch for JSON entry '" + controlJsonEntry.getKey() + "'", 
                                JSONObject.class.getSimpleName(), receivedJson.get(controlJsonEntry.getKey()).getClass().getSimpleName()));
                
                validateJson((JSONObject) receivedJson.get(controlJsonEntry.getKey()), 
                             (JSONObject) controlJsonEntry.getValue(), validationContext, context, readContext);
            } else if (controlJsonEntry.getValue() instanceof JSONArray) {
                Assert.isTrue(receivedJson.get(controlJsonEntry.getKey()) instanceof JSONArray, 
                        ValidationUtils.buildValueMismatchErrorMessage("Type mismatch for JSON entry '" + controlJsonEntry.getKey() + "'", 
                                JSONArray.class.getSimpleName(), receivedJson.get(controlJsonEntry.getKey()).getClass().getSimpleName()));
                
                JSONArray jsonArrayControl = (JSONArray) controlJsonEntry.getValue();
                JSONArray jsonArrayReceived = (JSONArray) receivedJson.get(controlJsonEntry.getKey());
                
                if (log.isDebugEnabled()) {
                    log.debug("Validating JSONArray containing " + jsonArrayControl.size() + " entries");
                }

                if (strict) {
                    Assert.isTrue(jsonArrayControl.size() == jsonArrayReceived.size(),
                                  ValidationUtils.buildValueMismatchErrorMessage("JSONArray size mismatch for JSON entry '" + controlJsonEntry.getKey() + "'",
                                                                                 jsonArrayControl.size(), jsonArrayReceived.size()));
                }
                for (int i = 0; i < jsonArrayControl.size(); i++) {
                    if (jsonArrayControl.get(i).getClass().isAssignableFrom(JSONObject.class)) {
                        Assert.isTrue(jsonArrayReceived.get(i).getClass().isAssignableFrom(JSONObject.class), 
                                ValidationUtils.buildValueMismatchErrorMessage("Value types not equal for entry: '" + jsonArrayControl.get(i) + "'",
                                        JSONObject.class.getName(), jsonArrayReceived.get(i).getClass().getName()));
                        
                        validateJson((JSONObject) jsonArrayReceived.get(i),
                                (JSONObject) jsonArrayControl.get(i), validationContext, context, readContext);
                    } else {
                        Assert.isTrue(jsonArrayControl.get(i).equals(jsonArrayReceived.get(i)),
                                ValidationUtils.buildValueMismatchErrorMessage("Values not equal for entry: '" + jsonArrayControl.get(i) + "'", 
                                        jsonArrayControl.get(i), jsonArrayReceived.get(i)));
                    }
                }
            } else {
                Assert.isTrue(controlJsonEntry.getValue().equals(receivedJson.get(controlJsonEntry.getKey())), 
                        ValidationUtils.buildValueMismatchErrorMessage("Values not equal for entry: '" + controlJsonEntry.getKey() + "'", 
                                controlJsonEntry.getValue(), receivedJson.get(controlJsonEntry.getKey())));
            }
            
            if (log.isDebugEnabled()) {
                log.debug("Validation successful for JSON entry '" + controlJsonEntry.getKey() + "' (" + controlJsonEntry.getValue() + ")");
            }
        }
    }

    /**
     * Checks if given element node is either on ignore list or
     * contains @ignore@ tag inside control message
     * @param controlJsonEntry
     * @param receivedJson
     * @param ignoreExpressions
     * @param readContext
     * @return
     */
    public boolean isIgnored(Map.Entry controlJsonEntry, Object receivedJson, Set<String> ignoreExpressions, ReadContext readContext) {
        if (controlJsonEntry.getValue().toString().trim().equals(CitrusConstants.IGNORE_PLACEHOLDER)) {
            if (log.isDebugEnabled()) {
                log.debug("JSON entry: '" + controlJsonEntry.getKey() + "' is ignored by placeholder '" +
                        CitrusConstants.IGNORE_PLACEHOLDER + "'");
            }
            return true;
        }

        for (String jsonPathExpression : ignoreExpressions) {
            Object foundEntry = readContext.read(jsonPathExpression);

            if (foundEntry instanceof JSONArray && ((JSONArray) foundEntry).contains(receivedJson)) {
                if (log.isDebugEnabled()) {
                    log.debug("JSON entry: '" + controlJsonEntry.getKey() + "' is ignored - skip value validation");
                }
                return true;
            }

            if (foundEntry != null && foundEntry.equals(receivedJson)) {
                if (log.isDebugEnabled()) {
                    log.debug("JSON entry: '" + controlJsonEntry.getKey() + "' is ignored - skip value validation");
                }
                return true;
            }
        }

        return false;
    }

    @Override
    public JsonMessageValidationContext findValidationContext(List<ValidationContext> validationContexts) {
        for (ValidationContext validationContext : validationContexts) {
            if (validationContext instanceof JsonMessageValidationContext) {
                return (JsonMessageValidationContext) validationContext;
            }
        }

        return null;
    }
    
    @Override
    public boolean supportsMessageType(String messageType, Message message) {
        return messageType.equalsIgnoreCase(MessageType.JSON.toString());
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

}
