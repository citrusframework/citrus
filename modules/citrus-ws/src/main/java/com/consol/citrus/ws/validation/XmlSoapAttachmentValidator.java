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

package com.consol.citrus.ws.validation;

import com.consol.citrus.message.DefaultMessage;
import com.consol.citrus.message.Message;
import com.consol.citrus.validation.MessageValidator;
import com.consol.citrus.validation.context.ValidationContext;
import com.consol.citrus.validation.xml.XmlMessageValidationContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

/**
 * Soap attachment validator delegating attachment content validation to a {@link MessageValidator}.
 * Through {@link XmlMessageValidationContext} this class supports message validation for XML payload.
 * 
 * @author Christoph Deppisch
 */
public class XmlSoapAttachmentValidator extends SimpleSoapAttachmentValidator {

    @Autowired(required = false)
    @Qualifier("soapAttachmentValidator")
    private MessageValidator<ValidationContext> validator;

    /** validation context holding information like expected message payload, ignored elements and so on */
    private XmlMessageValidationContext validationContext = new XmlMessageValidationContext();

	@Override
    protected void validateAttachmentContentData(String receivedContent, String controlContent, String controlContentId) {
        Message controlMessage = new DefaultMessage(controlContent);
        validationContext.setControlMessage(controlMessage);

        Message receivedMessage = new DefaultMessage(receivedContent);
        validator.validateMessage(receivedMessage, null, validationContext);
    }
}
