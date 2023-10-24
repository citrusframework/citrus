/*
 * Copyright 2006-2010 the original author or authors.
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
 * Extended groovy message validator providing specific XML slurper support.
 * With XML slurper the tester can validate the message payload with closures and without having
 * to deal with XPath for instance.
 *
 * @author Christoph Deppisch
 */
public class GroovyXmlMessageValidator extends GroovyScriptMessageValidator {

    /**
     * Default constructor using default script template.
     */
    public GroovyXmlMessageValidator() {
        super(Resources.fromClasspath("org/citrusframework/validation/xml-validation-template.groovy"));
    }

    @Override
    public boolean supportsMessageType(String messageType, Message message) {
        // only support xml message type
        return (messageType.equalsIgnoreCase(MessageType.XML.name()) ||
                messageType.equalsIgnoreCase(MessageType.XHTML.name()))&& MessageUtils.hasXmlPayload(message);
    }
}
