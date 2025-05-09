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

package org.citrusframework.validation.text;

import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.citrusframework.CitrusSettings;
import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.ValidationException;
import org.citrusframework.message.Message;
import org.citrusframework.message.MessageType;
import org.citrusframework.util.StringUtils;
import org.citrusframework.validation.DefaultMessageValidator;
import org.citrusframework.validation.context.MessageValidationContext;
import org.citrusframework.validation.context.ValidationContext;
import org.citrusframework.validation.matcher.ValidationMatcherUtils;

import static java.lang.Boolean.parseBoolean;
import static java.lang.Integer.parseInt;
import static org.citrusframework.util.StringUtils.normalizeWhitespace;

/**
 * Plain text validator using simple String comparison.
 */
public class PlainTextMessageValidator extends DefaultMessageValidator {

    public static final String IGNORE_NEWLINE_TYPE_PROPERTY = "citrus.plaintext.validation.ignore.newline.type";
    public static final String IGNORE_NEWLINE_TYPE_ENV = "CITRUS_PLAINTEXT_VALIDATION_IGNORE_NEWLINE_TYPE";
    public static final String IGNORE_WHITESPACE_PROPERTY = "citrus.plaintext.validation.ignore.whitespace";
    public static final String IGNORE_WHITESPACE_ENV = "CITRUS_PLAINTEXT_VALIDATION_IGNORE_WHITESPACE";

    private boolean ignoreNewLineType = parseBoolean(System.getProperty(IGNORE_NEWLINE_TYPE_PROPERTY, System.getenv(IGNORE_NEWLINE_TYPE_ENV) != null ?
            System.getenv(IGNORE_NEWLINE_TYPE_ENV) : "false"));
    private boolean ignoreWhitespace = parseBoolean(System.getProperty(IGNORE_WHITESPACE_PROPERTY, System.getenv(IGNORE_WHITESPACE_ENV) != null ?
            System.getenv(IGNORE_WHITESPACE_ENV) : "false"));

    @Override
    public void validateMessage(Message receivedMessage, Message controlMessage,
                                TestContext context, ValidationContext validationContext) throws ValidationException {
        if (controlMessage == null || controlMessage.getPayload() == null) {
            logger.debug("Skip message payload validation as no control message was defined");
            return;
        }

        logger.debug("Start text message validation");

        try {
            String resultValue = normalizeWhitespace(receivedMessage.getPayload(String.class).trim(), ignoreWhitespace, ignoreNewLineType);
            String controlValue = normalizeWhitespace(context.replaceDynamicContentInString(controlMessage.getPayload(String.class).trim()), ignoreWhitespace, ignoreNewLineType);

            controlValue = processIgnoreStatements(controlValue, resultValue);
            controlValue = processVariableStatements(controlValue, resultValue, context);

            if (ValidationMatcherUtils.isValidationMatcherExpression(controlValue)) {
                ValidationMatcherUtils.resolveValidationMatcher("payload", resultValue, controlValue, context);
                return;
            } else {
                validateText(resultValue, controlValue);
            }
        } catch (IllegalArgumentException e) {
            throw new ValidationException("Failed to validate text content", e);
        }

        logger.debug("Text validation successful: All values OK");
    }

    /**
     * Processes nested ignore statements in control value and replaces that ignore placeholder with the actual value at this position.
     * This way we can ignore words in a plaintext value.
     */
    private String processIgnoreStatements(String control, String result) {
        if (control.equals(CitrusSettings.IGNORE_PLACEHOLDER)) {
            return control;
        }

        Pattern whitespacePattern = Pattern.compile("[\\W]");
        Pattern ignorePattern = Pattern.compile("@ignore\\(?(\\d*)\\)?@");

        Matcher ignoreMatcher = ignorePattern.matcher(control);
        while (ignoreMatcher.find()) {
            String actualValue;

            if (ignoreMatcher.groupCount() > 0 && StringUtils.hasText(ignoreMatcher.group(1))) {
                int end = ignoreMatcher.start() + parseInt(ignoreMatcher.group(1));
                if (end > result.length()) {
                    end = result.length();
                }

                if (ignoreMatcher.start() > result.length()) {
                    actualValue = "";
                } else {
                    actualValue = result.substring(ignoreMatcher.start(), end);
                }
            } else {
                actualValue = result.substring(ignoreMatcher.start());
                Matcher whitespaceMatcher = whitespacePattern.matcher(actualValue);
                if (whitespaceMatcher.find()) {
                    actualValue = actualValue.substring(0, whitespaceMatcher.start());
                }
            }

            control = ignoreMatcher.replaceFirst(actualValue);
            ignoreMatcher = ignorePattern.matcher(control);
        }

        return control;
    }

    /**
     * Processes nested ignore statements in control value and replaces that ignore placeholder with the actual value at this position.
     * This way we can ignore words in a plaintext value.
     */
    private String processVariableStatements(String control, String result, TestContext context) {
        if (control.equals(CitrusSettings.IGNORE_PLACEHOLDER)) {
            return control;
        }

        Pattern whitespacePattern = Pattern.compile("[^a-zA-Z_0-9\\-\\.]");
        Pattern variablePattern = Pattern.compile("@variable\\(?'?([a-zA-Z_0-9\\-\\.]*)'?\\)?@");

        Matcher variableMatcher = variablePattern.matcher(control);
        while (variableMatcher.find()) {
            String actualValue = result.substring(variableMatcher.start());
            Matcher whitespaceMatcher = whitespacePattern.matcher(actualValue);
            if (whitespaceMatcher.find()) {
                actualValue = actualValue.substring(0, whitespaceMatcher.start());
            }

            control = variableMatcher.replaceFirst(actualValue);
            context.setVariable(variableMatcher.group(1), actualValue);
            variableMatcher = variablePattern.matcher(control);
        }

        return control;
    }

    /**
     * Compares two string with each other in order to validate plain text.
     */
    private void validateText(String receivedMessagePayload, String controlMessagePayload) {
        if (!StringUtils.hasText(controlMessagePayload)) {
            logger.debug("Skip message payload validation as no control message was defined");
            return;
        } else if (!StringUtils.hasText(receivedMessagePayload)) {
            throw new ValidationException("Validation failed - " +
                    "expected message contents, but received empty message!");
        }

        if (!receivedMessagePayload.equals(controlMessagePayload)) {
            if (receivedMessagePayload.replaceAll("\\s", "").equals(controlMessagePayload.replaceAll("\\s", ""))) {
                throw new ValidationException("Text values not equal (only whitespaces!), expected '" + controlMessagePayload + "' " +
                        "but was '" + receivedMessagePayload + "'");
            } else {
                throw new ValidationException("Text values not equal, expected '" + controlMessagePayload + "' " +
                        "but was '" + receivedMessagePayload + "'");
            }
        }
    }


    @Override
    public boolean supportsMessageType(String messageType, Message message) {
        return messageType.equalsIgnoreCase(MessageType.PLAINTEXT.toString());
    }

    @Override
    public ValidationContext findValidationContext(List<ValidationContext> validationContexts) {
        Optional<ValidationContext> messageValidationContext = validationContexts.stream()
                .filter(MessageValidationContext.class::isInstance)
                .findFirst();

        return messageValidationContext.orElseGet(() -> super.findValidationContext(validationContexts));
    }

    /**
     * Gets the ignoreWhitespace.
     */
    public boolean isIgnoreWhitespace() {
        return ignoreWhitespace;
    }

    /**
     * Sets the ignoreWhitespace.
     */
    public void setIgnoreWhitespace(boolean ignoreWhitespace) {
        this.ignoreWhitespace = ignoreWhitespace;
    }

    /**
     * Gets the ignoreNewLineType.
     */
    public boolean isIgnoreNewLineType() {
        return ignoreNewLineType;
    }

    /**
     * Sets the ignoreNewLineType.
     */
    public void setIgnoreNewLineType(boolean ignoreNewLineType) {
        this.ignoreNewLineType = ignoreNewLineType;
    }
}
