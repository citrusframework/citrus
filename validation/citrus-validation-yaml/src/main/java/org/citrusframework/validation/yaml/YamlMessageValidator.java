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

package org.citrusframework.validation.yaml;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.ValidationException;
import org.citrusframework.message.Message;
import org.citrusframework.validation.AbstractMessageValidator;
import org.citrusframework.validation.ValidationUtils;
import org.citrusframework.validation.context.DefaultMessageValidationContext;
import org.citrusframework.validation.context.MessageValidationContext;
import org.citrusframework.validation.context.ValidationContext;
import org.yaml.snakeyaml.scanner.ScannerException;

import static org.citrusframework.message.MessageType.YAML;
import static org.citrusframework.util.MessageUtils.hasYamlPayload;
import static org.citrusframework.util.StringUtils.hasText;

/**
 * This message validator implementation is able to validate two YAML text objects. The order of YAML entries can differ
 * as specified in YAML protocol. Tester defines an expected control YAML text with optional ignored entries.
 * Validator offers two different modes to operate. By default, strict mode is set and the validator will also check the exact amount of
 * control object fields to match. No additional fields in received YAML data structure will be accepted. In soft mode validator
 * allows additional fields in received YAML data structure so the control YAML object can be a partial subset.
 */
public class YamlMessageValidator extends AbstractMessageValidator<MessageValidationContext> {

    private boolean strict = YamlSettings.isStrict();

    private YamlNodeValidator.Provider nodeValidatorProvider = YamlNodeValidator.Provider.DEFAULT;

    @Override
    public void validateMessage(Message receivedMessage,
                                Message controlMessage,
                                TestContext context,
                                MessageValidationContext validationContext) {
        logger.debug("Start YAML message validation ...");

        var receivedYaml = receivedMessage.getPayload(String.class);
        var controlYaml = context.replaceDynamicContentInString(controlMessage.getPayload(String.class));

        if (!hasText(controlYaml)) {
            logger.debug("Skip message payload validation as no control message was defined");
            return;
        } else if (!hasText(receivedYaml)) {
            throw new ValidationException("Validation failed - expected message contents, but received empty message!");
        }

        var parsedYaml = YamlSupport.parseYaml(receivedYaml, controlYaml);

        List<Object> receivedDocuments = new ArrayList<>();
        List<Object> expectedDocuments = new ArrayList<>();

        try {
            parsedYaml.actual.iterator().forEachRemaining(receivedDocuments::add);
            parsedYaml.expected.iterator().forEachRemaining(expectedDocuments::add);
        } catch (ScannerException e) {
            throw new ValidationException("Failed to read YAML sources", e);
        }

        if (receivedDocuments.size() != expectedDocuments.size()) {
            throw new ValidationException(ValidationUtils.buildValueMismatchErrorMessage(
                    "Validation failed - number of YAML documents not equal", expectedDocuments.size(), receivedDocuments.size()));
        }

        var validator = nodeValidatorProvider.getValidator(strict, context, validationContext);
        for (int i = 0; i < expectedDocuments.size(); i++) {
            validator.validate(new YamlNodeValidatorItem<>("$", receivedDocuments.get(i), expectedDocuments.get(i)));
        }

        logger.debug("YAML message validation successful: All values OK");
    }

    @Override
    protected Class<MessageValidationContext> getRequiredValidationContextType() {
        return MessageValidationContext.class;
    }

    @Override
    public boolean supportsMessageType(String messageType, Message message) {
        return messageType.equalsIgnoreCase(YAML.name()) && hasYamlPayload(message);
    }

    @Override
    public MessageValidationContext findValidationContext(List<ValidationContext> validationContexts) {
        Optional<MessageValidationContext> yamlMessageValidationContext = validationContexts.stream()
                .filter(YamlMessageValidationContext.class::isInstance)
                .map(MessageValidationContext.class::cast)
                .findFirst();

        if (yamlMessageValidationContext.isPresent()) {
            return yamlMessageValidationContext.get();
        }

        Optional<MessageValidationContext> defaultMessageValidationContext = validationContexts.stream()
                .filter(it -> it.getClass().equals(DefaultMessageValidationContext.class))
                .map(MessageValidationContext.class::cast)
                .findFirst();

        return defaultMessageValidationContext.orElseGet(() -> super.findValidationContext(validationContexts));
    }

    public void setStrict(boolean strict) {
        this.strict = strict;
    }

    public YamlMessageValidator strict(boolean strict) {
        setStrict(strict);
        return this;
    }

    public void setNodeValidatorProvider(YamlNodeValidator.Provider nodeValidatorProvider) {
        this.nodeValidatorProvider = nodeValidatorProvider;
    }

    public YamlMessageValidator nodeValidatorProvider(YamlNodeValidator.Provider nodeValidatorProvider) {
        setNodeValidatorProvider(nodeValidatorProvider);
        return this;
    }
}
