/*
 * Copyright 2006-2012 the original author or authors.
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

package org.citrusframework.validation.script;

import org.citrusframework.message.Message;
import org.citrusframework.message.MessageType;
import org.citrusframework.spi.Resources;
import org.citrusframework.util.MessageUtils;

/**
 * Extended groovy message validator providing specific Json slurper support.
 * With Json slurper the tester can validate the message payload with closures for instance.
 *
 * @author DanielP
 * @since 1.2
 */
public class GroovyJsonMessageValidator extends GroovyScriptMessageValidator {

    /**
     * Default constructor using default script template.
     */
    public GroovyJsonMessageValidator() {
        super(Resources.fromClasspath("org/citrusframework/validation/json-validation-template.groovy"));
    }

    @Override
    public boolean supportsMessageType(String messageType, Message message) {
        // only support json message type
        return messageType.equalsIgnoreCase(MessageType.JSON.name()) && MessageUtils.hasJsonPayload(message);
    }
}
