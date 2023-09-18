/*
 * Copyright 2022 the original author or authors.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.citrusframework.validation;

import org.citrusframework.context.TestContext;
import org.citrusframework.message.Message;
import org.citrusframework.util.TestUtils;
import org.citrusframework.validation.context.ValidationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;

/**
 * Basic message validator performs String equals on received message payloads. We add this validator in order to have a
 * matching message validation strategy for integration tests in this module.
 * @author Christoph Deppisch
 */
public class TextEqualsMessageValidator extends DefaultMessageValidator {

    private static final Logger logger = LoggerFactory.getLogger(TextEqualsMessageValidator.class);

    private boolean normalizeLineEndings = false;
    private boolean trim = false;

    @Override
    public void validateMessage(Message receivedMessage, Message controlMessage, TestContext context, ValidationContext validationContext) {
        if (controlMessage == null || controlMessage.getPayload() == null || controlMessage.getPayload(String.class).isEmpty()) {
            logger.debug("Skip message payload validation as no control message was defined");
            return;
        }

        logger.debug("Start text equals validation ...");

        String controlPayload = controlMessage.getPayload(String.class);
        String receivedPayload = receivedMessage.getPayload(String.class);

        if (trim) {
            controlPayload = controlPayload.trim();
            receivedPayload = receivedPayload.trim();
        }

        if (normalizeLineEndings) {
            controlPayload = TestUtils.normalizeLineEndings(controlPayload);
            receivedPayload = TestUtils.normalizeLineEndings(receivedPayload);
        }

        Assert.assertEquals(receivedPayload, controlPayload, "Validation failed - " +
                "expected message contents not equal!");

        logger.info("Text validation successful: All values OK");
    }

    @Override
    public boolean supportsMessageType(String messageType, Message message) {
        return true;
    }

    public TextEqualsMessageValidator normalizeLineEndings() {
        this.normalizeLineEndings = true;
        return this;
    }

    public TextEqualsMessageValidator enableTrim() {
        this.trim = true;
        return this;
    }
}
