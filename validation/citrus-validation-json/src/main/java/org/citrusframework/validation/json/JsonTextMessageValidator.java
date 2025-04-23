/*
 * Copyright the original author or authors.
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

import java.util.List;
import java.util.Optional;

import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.ValidationException;
import org.citrusframework.json.JsonSettings;
import org.citrusframework.message.Message;
import org.citrusframework.validation.AbstractMessageValidator;
import org.citrusframework.validation.context.DefaultMessageValidationContext;
import org.citrusframework.validation.context.MessageValidationContext;
import org.citrusframework.validation.context.ValidationContext;
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
 * Validator offers two different modes to operate. By default, strict mode is set and the validator will also check the exact amount of
 * control object fields to match. No additional fields in received JSON data structure will be accepted. In soft mode validator
 * allows additional fields in received JSON data structure so the control JSON object can be a partial subset.
 *
 */
public class JsonTextMessageValidator extends AbstractMessageValidator<MessageValidationContext> {

    private boolean strict = JsonSettings.isStrict();

    private int permissiveMode = JsonSettings.getPermissiveMoe();

    private JsonSchemaValidation jsonSchemaValidation = new JsonSchemaValidation();

    private JsonElementValidator.Provider elementValidatorProvider = JsonElementValidator.Provider.DEFAULT;

    @Override
    public void validateMessage(Message receivedMessage,
                                Message controlMessage,
                                TestContext context,
                                MessageValidationContext validationContext) {
        logger.debug("Start JSON message validation ...");

        if (validationContext.isSchemaValidationEnabled()) {
            jsonSchemaValidation.validate(receivedMessage, context, validationContext);
        }

        var receivedJsonText = receivedMessage.getPayload(String.class);
        var controlJsonText = context.replaceDynamicContentInString(controlMessage.getPayload(String.class));

        if (!hasText(controlJsonText)) {
            logger.debug("Skip message payload validation as no control message was defined");
            return;
        } else if (!hasText(receivedJsonText)) {
            throw new ValidationException("Validation failed - expected message contents, but received empty message!");
        }

        elementValidatorProvider.getValidator(strict, context, validationContext)
            .validate(parseJson(permissiveMode, receivedJsonText, controlJsonText));

        logger.debug("JSON message validation successful: All values OK");
    }

    @Override
    protected Class<MessageValidationContext> getRequiredValidationContextType() {
        return MessageValidationContext.class;
    }

    @Override
    public boolean supportsMessageType(String messageType, Message message) {
        return messageType.equalsIgnoreCase(JSON.name()) && hasJsonPayload(message);
    }

    public void setStrict(boolean strict) {
        this.strict = strict;
    }

    public JsonTextMessageValidator strict(boolean strict) {
        setStrict(strict);
        return this;
    }

    @Override
    public MessageValidationContext findValidationContext(List<ValidationContext> validationContexts) {
        Optional<MessageValidationContext> jsonMessageValidationContext = validationContexts.stream()
                .filter(JsonMessageValidationContext.class::isInstance)
                .map(MessageValidationContext.class::cast)
                .findFirst();

        if (jsonMessageValidationContext.isPresent()) {
            return jsonMessageValidationContext.get();
        }

        Optional<MessageValidationContext> defaultMessageValidationContext = validationContexts.stream()
                .filter(it -> it.getClass().equals(DefaultMessageValidationContext.class))
                .map(MessageValidationContext.class::cast)
                .findFirst();

        return defaultMessageValidationContext.orElseGet(() -> super.findValidationContext(validationContexts));
    }

    public void setJsonSchemaValidation(JsonSchemaValidation jsonSchemaValidation) {
        this.jsonSchemaValidation = jsonSchemaValidation;
    }

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

    public void setPermissiveMode(int permissiveMode) {
        this.permissiveMode = permissiveMode;
    }

    public JsonTextMessageValidator permissiveMode(int permissiveMode) {
        setPermissiveMode(permissiveMode);
        return this;
    }
}
