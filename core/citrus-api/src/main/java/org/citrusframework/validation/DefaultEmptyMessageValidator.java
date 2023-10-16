/*
 * Copyright 2006-2017 the original author or authors.
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

import java.util.Optional;

import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.ValidationException;
import org.citrusframework.message.Message;
import org.citrusframework.validation.context.ValidationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Basic message validator is able to verify empty message payloads. Both received and control message must have
 * empty message payloads otherwise ths validator will raise some exception.
 *
 * @author Christoph Deppisch
 */
public class DefaultEmptyMessageValidator extends DefaultMessageValidator {

    private static final Logger logger = LoggerFactory.getLogger(DefaultEmptyMessageValidator.class);

    @Override
    public void validateMessage(Message receivedMessage, Message controlMessage,
                                TestContext context, ValidationContext validationContext) {
        if (controlMessage == null || controlMessage.getPayload() == null) {
            logger.debug("Skip message payload validation as no control message was defined");
            return;
        }

        if (!Optional.ofNullable(controlMessage.getPayload(String.class)).orElse("").isEmpty()) {
            throw new ValidationException("Empty message validation failed - control message is not empty!");
        }

        logger.debug("Start to verify empty message payload ...");

        if (!Optional.ofNullable(receivedMessage.getPayload(String.class)).orElse("").isEmpty()) {
            throw new ValidationException("Validation failed - received message content is not empty!") ;
        }

        logger.info("Message payload is empty as expected: All values OK");
    }
}
