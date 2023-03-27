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

package org.citrusframework.citrus.validation.xhtml;

import org.citrusframework.citrus.common.InitializingPhase;
import org.citrusframework.citrus.context.TestContext;
import org.citrusframework.citrus.exceptions.ValidationException;
import org.citrusframework.citrus.message.DefaultMessage;
import org.citrusframework.citrus.message.Message;
import org.citrusframework.citrus.message.MessageType;
import org.citrusframework.citrus.util.MessageUtils;
import org.citrusframework.citrus.validation.xml.XpathMessageValidationContext;
import org.citrusframework.citrus.validation.xml.XpathMessageValidator;

/**
 * @author Christoph Deppisch
 * @since 2.6
 */
public class XhtmlXpathMessageValidator extends XpathMessageValidator implements InitializingPhase {

    /** Message converter for XHTML content */
    private XhtmlMessageConverter messageConverter = new XhtmlMessageConverter();

    @Override
    public void validateMessage(Message receivedMessage, Message controlMessage,
            TestContext context, XpathMessageValidationContext validationContext)
            throws ValidationException {

        String messagePayload = receivedMessage.getPayload(String.class);
        super.validateMessage(new DefaultMessage(messageConverter.convert(messagePayload), receivedMessage.getHeaders()),
                controlMessage, context, validationContext);
    }

    @Override
    public boolean supportsMessageType(String messageType, Message message) {
        return messageType.equalsIgnoreCase(MessageType.XHTML.name()) && MessageUtils.hasXmlPayload(message);
    }

    @Override
    public void initialize() {
        messageConverter.initialize();
    }

    /**
     * Sets the messageConverter property.
     *
     * @param messageConverter
     */
    public void setMessageConverter(XhtmlMessageConverter messageConverter) {
        this.messageConverter = messageConverter;
    }

    /**
     * Gets the value of the messageConverter property.
     *
     * @return the messageConverter
     */
    public XhtmlMessageConverter getMessageConverter() {
        return messageConverter;
    }
}
