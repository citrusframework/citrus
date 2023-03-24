/*
 * Copyright 2006-2016 the original author or authors.
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

package org.citrusframework.xml;

import org.citrusframework.context.TestContext;
import org.citrusframework.message.AbstractMessageProcessor;
import org.citrusframework.message.Message;
import org.citrusframework.message.MessageType;
import org.citrusframework.util.XMLUtils;

/**
 * @author Christoph Deppisch
 * @since 2.6.2
 */
public class XmlFormattingMessageProcessor extends AbstractMessageProcessor {

    @Override
    public void processMessage(Message message, TestContext context) {
        message.setPayload(XMLUtils.prettyPrint(message.getPayload(String.class)));
    }

    @Override
    public boolean supportsMessageType(String messageType) {
        return messageType.equalsIgnoreCase(MessageType.XML.name());
    }
}
