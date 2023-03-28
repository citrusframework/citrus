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

package org.citrusframework.http.validation;

import org.citrusframework.context.TestContext;
import org.citrusframework.message.Message;
import org.citrusframework.validation.DefaultMessageValidator;
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

    private boolean trim = false;

    @Override
    public void validateMessage(Message receivedMessage, Message controlMessage, TestContext context, ValidationContext validationContext) {
        Logger log = LoggerFactory.getLogger("TextEqualsMessageValidator");

        if (controlMessage == null || controlMessage.getPayload() == null || controlMessage.getPayload(String.class).isEmpty()) {
            LOG.debug("Skip message payload validation as no control message was defined");
            return;
        }

        log.debug("Start text equals validation ...");

        String controlPayload = controlMessage.getPayload(String.class);
        String receivedPayload = receivedMessage.getPayload(String.class);

        if (trim) {
            controlPayload = controlPayload.trim();
            receivedPayload = receivedPayload.trim();
        }

        Assert.assertEquals(receivedPayload, controlPayload, "Validation failed - " +
                "expected message contents not equal!");

        log.info("Text validation successful: All values OK");
    }

    @Override
    public boolean supportsMessageType(String messageType, Message message) {
        return true;
    }

    public TextEqualsMessageValidator enableTrim() {
        this.trim = true;
        return this;
    }
}
