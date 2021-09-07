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

package com.consol.citrus.validation.text;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.consol.citrus.CitrusSettings;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.ValidationException;
import com.consol.citrus.message.Message;
import com.consol.citrus.message.MessageType;
import com.consol.citrus.validation.DefaultMessageValidator;
import com.consol.citrus.validation.context.ValidationContext;
import com.consol.citrus.validation.matcher.ValidationMatcherUtils;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * Plain text validator using simple String comparison.
 *
 * @author Christoph Deppisch
 */
public class PlainTextMessageValidator extends DefaultMessageValidator {

    public static final String IGNORE_NEWLINE_TYPE_PROPERTY = "citrus.plaintext.validation.ignore.newline.type";
    public static final String IGNORE_NEWLINE_TYPE_ENV = "CITRUS_PLAINTEXT_VALIDATION_IGNORE_NEWLINE_TYPE";
    public static final String IGNORE_WHITESPACE_PROPERTY = "citrus.plaintext.validation.ignore.whitespace";
    public static final String IGNORE_WHITESPACE_ENV = "CITRUS_PLAINTEXT_VALIDATION_IGNORE_WHITESPACE";

    private boolean ignoreNewLineType = Boolean.valueOf(System.getProperty(IGNORE_NEWLINE_TYPE_PROPERTY, System.getenv(IGNORE_NEWLINE_TYPE_ENV) != null ?
            System.getenv(IGNORE_NEWLINE_TYPE_ENV) : "false"));
    private boolean ignoreWhitespace = Boolean.valueOf(System.getProperty(IGNORE_WHITESPACE_PROPERTY, System.getenv(IGNORE_WHITESPACE_ENV) != null ?
            System.getenv(IGNORE_WHITESPACE_ENV) : "false"));

    @Override
    public void validateMessage(Message receivedMessage, Message controlMessage,
                                TestContext context, ValidationContext validationContext) throws ValidationException {
        if (controlMessage == null || controlMessage.getPayload() == null) {
            log.debug("Skip message payload validation as no control message was defined");
            return;
        }

        log.debug("Start text message validation");

        if (log.isDebugEnabled()) {
            log.debug("Received message:\n" + receivedMessage.print(context));
            log.debug("Control message:\n" + controlMessage.print(context));
        }

        try {
            String resultValue = normalizeWhitespace(receivedMessage.getPayload(String.class).trim());
            String controlValue = normalizeWhitespace(context.replaceDynamicContentInString(controlMessage.getPayload(String.class).trim()));

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

        log.info("Text validation successful: All values OK");
    }

    /**
     * Processes nested ignore statements in control value and replaces that ignore placeholder with the actual value at this position.
     * This way we can ignore words in a plaintext value.
     * @param control
     * @param result
     * @return
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
                int end = ignoreMatcher.start() + Integer.valueOf(ignoreMatcher.group(1));
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
     * @param control
     * @param result
     * @param context
     * @return
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
     *
     * @param receivedMessagePayload
     * @param controlMessagePayload
     */
    private void validateText(String receivedMessagePayload, String controlMessagePayload) {
        if (!StringUtils.hasText(controlMessagePayload)) {
            log.debug("Skip message payload validation as no control message was defined");
            return;
        } else {
            Assert.isTrue(StringUtils.hasText(receivedMessagePayload), "Validation failed - " +
                    "expected message contents, but received empty message!");
        }

        if (!receivedMessagePayload.equals(controlMessagePayload)) {
            if (StringUtils.trimAllWhitespace(receivedMessagePayload).equals(StringUtils.trimAllWhitespace(controlMessagePayload))) {
                throw new ValidationException("Text values not equal (only whitespaces!), expected '" + controlMessagePayload + "' " +
                        "but was '" + receivedMessagePayload + "'");
            } else {
                throw new ValidationException("Text values not equal, expected '" + controlMessagePayload + "' " +
                        "but was '" + receivedMessagePayload + "'");
            }
        }
    }

    /**
     * Normalize whitespace characters if appropriate. Based on system property settings this method normalizes
     * new line characters exclusively or filters all whitespaces such as double whitespaces and new lines.
     *
     * @param payload
     * @return
     */
    private String normalizeWhitespace(String payload) {
        if (ignoreWhitespace) {
            StringBuilder result = new StringBuilder();
            boolean lastWasSpace = true;
            for (int i = 0; i < payload.length(); i++) {
                char c = payload.charAt(i);
                if (Character.isWhitespace(c)) {
                    if (!lastWasSpace) {
                        result.append(' ');
                    }
                    lastWasSpace = true;
                } else {
                    result.append(c);
                    lastWasSpace = false;
                }
            }
            return result.toString().trim();
        }

        if (ignoreNewLineType) {
            return payload.replaceAll("\\r(\\n)?", "\n");
        }

        return payload;
    }

    @Override
    public boolean supportsMessageType(String messageType, Message message) {
        return messageType.equalsIgnoreCase(MessageType.PLAINTEXT.toString());
    }

    /**
     * Gets the ignoreWhitespace.
     *
     * @return
     */
    public boolean isIgnoreWhitespace() {
        return ignoreWhitespace;
    }

    /**
     * Sets the ignoreWhitespace.
     *
     * @param ignoreWhitespace
     */
    public void setIgnoreWhitespace(boolean ignoreWhitespace) {
        this.ignoreWhitespace = ignoreWhitespace;
    }

    /**
     * Gets the ignoreNewLineType.
     *
     * @return
     */
    public boolean isIgnoreNewLineType() {
        return ignoreNewLineType;
    }

    /**
     * Sets the ignoreNewLineType.
     *
     * @param ignoreNewLineType
     */
    public void setIgnoreNewLineType(boolean ignoreNewLineType) {
        this.ignoreNewLineType = ignoreNewLineType;
    }
}
