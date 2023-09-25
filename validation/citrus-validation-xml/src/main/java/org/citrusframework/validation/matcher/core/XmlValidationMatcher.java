/*
 * Copyright 2006-2018 the original author or authors.
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

package org.citrusframework.validation.matcher.core;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.exceptions.ValidationException;
import org.citrusframework.message.DefaultMessage;
import org.citrusframework.validation.MessageValidator;
import org.citrusframework.validation.context.ValidationContext;
import org.citrusframework.validation.matcher.ValidationMatcher;
import org.citrusframework.validation.xml.XmlMessageValidationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Validation matcher receives a XML data and validates it against expected XML with full
 * XML validation support (e.g. ignoring elements, namespace support, ...).
 *
 * @author Christoph Deppisch
 */
public class XmlValidationMatcher implements ValidationMatcher {

    /** CDATA section starting and ending in XML */
    private static final String CDATA_SECTION_START = "<![CDATA[";
    private static final String CDATA_SECTION_END = "]]>";

    /** Xml message validator */
    private MessageValidator<? extends ValidationContext> messageValidator;

    public static final String DEFAULT_XML_MESSAGE_VALIDATOR = "defaultXmlMessageValidator";

    /** Logger */
    private static final Logger logger = LoggerFactory.getLogger(XmlValidationMatcher.class);

    @Override
    public void validate(String fieldName, String value, List<String> controlParameters, TestContext context) throws ValidationException {
        String control = controlParameters.get(0);
        XmlMessageValidationContext validationContext = new XmlMessageValidationContext();
        getMessageValidator(context).validateMessage(new DefaultMessage(removeCDataElements(value)), new DefaultMessage(control), context, Collections.singletonList(validationContext));
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

        throw new CitrusRuntimeException("Unable to locate proper XML message validator - please add validator to project");
    }

    /**
     * Cut off CDATA elements.
     * @param value
     * @return
     */
    private String removeCDataElements(String value) {
        String data = value.trim();

        if (data.startsWith(CDATA_SECTION_START)) {
            data = value.substring(CDATA_SECTION_START.length());
            data = data.substring(0, data.length() - CDATA_SECTION_END.length());
        }

        return data;
    }

}
