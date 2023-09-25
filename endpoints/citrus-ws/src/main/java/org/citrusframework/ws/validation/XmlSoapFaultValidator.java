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

package org.citrusframework.ws.validation;

import java.util.Collections;
import java.util.Optional;

import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.exceptions.ValidationException;
import org.citrusframework.message.DefaultMessage;
import org.citrusframework.validation.MessageValidator;
import org.citrusframework.validation.context.ValidationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Soap fault validator implementation that delegates soap fault detail validation to default XML message validator
 * in order to support XML fault detail content validation.
 *
 * @author Christoph Deppisch
 */
public class XmlSoapFaultValidator extends AbstractFaultDetailValidator {

    /** Logger */
    private static final Logger logger = LoggerFactory.getLogger(XmlSoapFaultValidator.class);

    /** Xml message validator */
    private MessageValidator<? extends ValidationContext> messageValidator;

    public static final String DEFAULT_XML_MESSAGE_VALIDATOR = "defaultXmlMessageValidator";

    /**
     * Delegates to XML message validator for validation of fault detail.
     */
    @Override
    protected void validateFaultDetailString(String receivedDetailString, String controlDetailString,
            TestContext context, SoapFaultDetailValidationContext validationContext) throws ValidationException {
        getMessageValidator(context).validateMessage(new DefaultMessage(receivedDetailString), new DefaultMessage(controlDetailString),
                context, Collections.singletonList(validationContext));
    }

    /**
     * Find proper XML message validator. Uses several strategies to lookup default XML message validator. Caches found validator for
     * future usage once the lookup is done.
     * @param context
     * @return
     */
    private MessageValidator<? extends ValidationContext> getMessageValidator(TestContext context) {
        if (messageValidator != null) {
            return messageValidator;
        }

        // try to find xml message validator in registry
        Optional<MessageValidator<? extends ValidationContext>> defaultMessageValidator = context.getMessageValidatorRegistry().findMessageValidator(DEFAULT_XML_MESSAGE_VALIDATOR);

        if (!defaultMessageValidator.isPresent()) {
            try {
                defaultMessageValidator = Optional.of(context.getReferenceResolver().resolve(DEFAULT_XML_MESSAGE_VALIDATOR, MessageValidator.class));
            } catch (CitrusRuntimeException e) {
                logger.warn("Unable to find default XML message validator in message validator registry");
            }
        }

        if (!defaultMessageValidator.isPresent()) {
            // try to find xml message validator via resource path lookup
            defaultMessageValidator = MessageValidator.lookup("xml");
        }

        if (defaultMessageValidator.isPresent()) {
            messageValidator = defaultMessageValidator.get();
            return messageValidator;
        }

        throw new CitrusRuntimeException("Unable to locate proper JSON message validator - please add validator to project");
    }
}
