/*
 * Copyright 2006-2010 ConSol* Software GmbH.
 * 
 * This file is part of Citrus.
 * 
 * Citrus is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Citrus is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Citrus. If not, see <http://www.gnu.org/licenses/>.
 */

package com.consol.citrus.ws.validation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.core.Message;
import org.springframework.integration.message.MessageBuilder;

import com.consol.citrus.exceptions.ValidationException;
import com.consol.citrus.validation.MessageValidator;
import com.consol.citrus.validation.XmlValidationContext;

/**
 * Soap fault validator implementation that delegates soap fault detail validation to default XML message validator
 * in order to support XML fault detail content validation.
 * 
 * @author Christoph Deppisch
 */
public class XmlSoapFaultValidator extends AbstractFaultDetailStringValidator {

    @Autowired
    private MessageValidator validator;
    
    /** validation context holding information like expected message payload, ignored elements and so on */
    private XmlValidationContext validationContext = new XmlValidationContext();
    
    /**
     * @see com.consol.citrus.ws.validation.AbstractFaultDetailStringValidator#validateFaultDetailString(java.lang.String, java.lang.String)
     */
    @Override
    protected void validateFaultDetailString(String receivedDetailString, String controlDetailString) 
        throws ValidationException {
        Message<String> controlMessage = MessageBuilder.withPayload(controlDetailString).build();
        validationContext.setExpectedMessage(controlMessage);

        Message<String> receivedMessage = MessageBuilder.withPayload(receivedDetailString).build();
        validator.validateMessage(receivedMessage, null, validationContext);
    }
}
