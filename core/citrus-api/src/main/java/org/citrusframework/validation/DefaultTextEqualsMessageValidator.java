/*
 * Copyright 2006-2023 the original author or authors.
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

package org.citrusframework.validation;

import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.ValidationException;
import org.citrusframework.message.Message;
import org.citrusframework.validation.context.ValidationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default message validator implementation performing text equals on given message payloads.
 * Validator auto converts message payloads into a String representation in order to perform text equals validation.
 * Both received and control message should have textual message payloads.
 * By default, the validator ignores leading and trailing whitespaces and normalizes the line endings before the validation.
 * Usually this validator implementation is used as a fallback option when no other matching validator implementation could be found.
 *
 * @author Christoph Deppisch
 */
public class DefaultTextEqualsMessageValidator extends DefaultMessageValidator {

    private static final Logger logger = LoggerFactory.getLogger(DefaultTextEqualsMessageValidator.class);

    private boolean normalizeLineEndings = true;
    private boolean trim = true;

    @Override
    public void validateMessage(Message receivedMessage, Message controlMessage,
                                TestContext context, ValidationContext validationContext) {
        if (controlMessage == null || controlMessage.getPayload() == null || controlMessage.getPayload(String.class).isEmpty()) {
            logger.debug("Skip message payload validation as no control message was defined");
            return;
        }

        logger.debug("Start to verify message payload ...");

        String controlPayload = controlMessage.getPayload(String.class);
        String receivedPayload = receivedMessage.getPayload(String.class);

        if (trim) {
            controlPayload = controlPayload.trim();
            receivedPayload = receivedPayload.trim();
        }

        if (normalizeLineEndings) {
            controlPayload = normalizeLineEndings(controlPayload);
            receivedPayload = normalizeLineEndings(receivedPayload);
        }

        if (!receivedPayload.equals(controlPayload)) {
            throw new ValidationException("Validation failed - message payload not equal " + getFirstDiff(receivedPayload, controlPayload));
        }
    }

    public String getFirstDiff(String received, String control) {
        int position;
        for (position = 0; position < received.length() && position < control.length(); position++) {
            if (received.charAt(position) != control.charAt(position)) {
                break;
            }
        }

        if (position < control.length() || position < received.length()) {
            int controlEnd = Math.min(position + 25, control.length());
            int receivedEnd = Math.min(position + 25, received.length());

            return String.format("at position %d expected '%s', but was '%s'", position + 1, control.substring(position, controlEnd), received.substring(position, receivedEnd));
        }

        return "";
    }

    public DefaultTextEqualsMessageValidator normalizeLineEndings() {
        this.normalizeLineEndings = true;
        return this;
    }

    public DefaultTextEqualsMessageValidator enableTrim() {
        this.trim = true;
        return this;
    }

    /**
     * Normalize the text by replacing line endings by a linux representation.
     */
    private static String normalizeLineEndings(String text) {
        return text.replace("\r\n", "\n").replace("&#13;", "");
    }
}
