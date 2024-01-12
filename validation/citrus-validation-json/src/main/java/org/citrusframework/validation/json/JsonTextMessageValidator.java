/*
 * Copyright 2006-2024 the original author or authors.
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

import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.ValidationException;
import org.citrusframework.json.JsonSettings;
import org.citrusframework.message.Message;
import org.citrusframework.validation.AbstractMessageValidator;
import org.citrusframework.validation.json.schema.JsonSchemaValidation;

import static org.citrusframework.message.MessageType.JSON;
import static org.citrusframework.util.MessageUtils.hasJsonPayload;
import static org.citrusframework.util.StringUtils.hasText;
import static org.citrusframework.validation.json.JsonElementValidatorItem.parseJson;

/**
 * This message validator implementation is able to validate two JSON text objects. The order of JSON entries can differ
 * as specified in JSON protocol. Tester defines an expected control JSON text with optional ignored entries.
 * <p>
 * JSONArray as well as nested JSONObjects are supported, too.
 * <p>
 * Validator offers two different modes to operate. By default strict mode is set and the validator will also check the exact amount of
 * control object fields to match. No additional fields in received JSON data structure will be accepted. In soft mode validator
 * allows additional fields in received JSON data structure so the control JSON object can be a partial subset.
 *
 * @author Christoph Deppisch
 */
public class JsonTextMessageValidator extends AbstractMessageValidator<JsonMessageValidationContext> {

    /**
     * Should also check exact amount of object fields
     */
    private boolean strict = JsonSettings.isStrict();

    /**
     * Permissive mode to use on the Json parser
     */
    private int permissiveMode = JsonSettings.getPermissiveMoe();

    /**
     * Schema validator
     */
    private JsonSchemaValidation jsonSchemaValidation = new JsonSchemaValidation();

    private JsonElementValidator.Provider elementValidatorProvider = JsonElementValidator.Provider.DEFAULT;

    @Override
    public void validateMessage(
            Message receivedMessage,
            Message controlMessage,
            TestContext context,
            JsonMessageValidationContext validationContext
    ) {
        logger.debug("Start JSON message validation ...");
        if (validationContext.isSchemaValidationEnabled()) {
            jsonSchemaValidation.validate(receivedMessage, context, validationContext);
        }

        String receivedJsonText = receivedMessage.getPayload(String.class);
        String controlJsonText = context.replaceDynamicContentInString(controlMessage.getPayload(String.class));

        if (!hasText(controlJsonText)) {
            logger.debug("Skip message payload validation as no control message was defined");
            return;
        } else if (!hasText(receivedJsonText)) {
            throw new ValidationException("Validation failed - expected message contents, but received empty message!");
        }

        elementValidatorProvider.getValidator(strict, context, validationContext).validate(
                parseJson(permissiveMode, receivedJsonText, controlJsonText)
        );
        logger.info("JSON message validation successful: All values OK");
    }

    @Override
    protected Class<JsonMessageValidationContext> getRequiredValidationContextType() {
        return JsonMessageValidationContext.class;
    }

    @Override
    public boolean supportsMessageType(String messageType, Message message) {
        return messageType.equalsIgnoreCase(JSON.name()) && hasJsonPayload(message);
    }

    /**
     * Set the validator strict mode.
     *
     * @param strict
     */
    public void setStrict(boolean strict) {
        this.strict = strict;
    }

    /**
     * Set the validator strict mode.
     *
     * @param strict
     * @return this object for chaining
     */
    public JsonTextMessageValidator strict(boolean strict) {
        setStrict(strict);
        return this;
    }

    /**
     * Sets the json schema validation.
     *
     * @param jsonSchemaValidation
     */
    void setJsonSchemaValidation(JsonSchemaValidation jsonSchemaValidation) {
        this.jsonSchemaValidation = jsonSchemaValidation;
    }

    /**
     * Sets the json schema validation.
     *
     * @param jsonSchemaValidation
     * @return this object for chaining
     */
    public JsonTextMessageValidator jsonSchemaValidation(JsonSchemaValidation jsonSchemaValidation) {
        setJsonSchemaValidation(jsonSchemaValidation);
        return this;
    }

    public void setElementValidatorProvider(JsonElementValidator.Provider elementValidatorProvider) {
        this.elementValidatorProvider = elementValidatorProvider;
    }

    public JsonTextMessageValidator elementValidatorProvider(JsonElementValidator.Provider elementValidatorProvider) {
        setElementValidatorProvider(elementValidatorProvider);
        return this;
    }

    /**
     * Sets the permissive mode.
     *
     * @param permissiveMode
     */
    public void setPermissiveMode(int permissiveMode) {
        this.permissiveMode = permissiveMode;
    }

    /**
     * Sets the permissive mode
     *
     * @param permissiveMode
     * @return this object for chaining
     */
    public JsonTextMessageValidator permissiveMode(int permissiveMode) {
        setPermissiveMode(permissiveMode);
        return this;
    }

}
