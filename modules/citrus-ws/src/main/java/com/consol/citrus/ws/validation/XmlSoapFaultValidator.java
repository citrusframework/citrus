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

import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.ValidationException;
import com.consol.citrus.message.*;
import com.consol.citrus.validation.MessageValidator;
import com.consol.citrus.validation.MessageValidatorRegistry;
import com.consol.citrus.validation.context.ValidationContext;
import com.consol.citrus.validation.xml.DomXmlMessageValidator;
import com.consol.citrus.validation.xml.XmlMessageValidationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * Soap fault validator implementation that delegates soap fault detail validation to default XML message validator
 * in order to support XML fault detail content validation.
 * 
 * @author Christoph Deppisch
 */
public class XmlSoapFaultValidator extends AbstractFaultDetailValidator implements InitializingBean, ApplicationContextAware {

    /** Logger */
    private static Logger log = LoggerFactory.getLogger(XmlSoapFaultValidator.class);

    @Autowired
    private MessageValidatorRegistry messageValidatorRegistry;

    /** Xml message validator */
    private DomXmlMessageValidator messageValidator;

    /** Spring bean application context */
    private ApplicationContext applicationContext;

    /**
     * Delegates to XML message validator for validation of fault detail.
     */
    @Override
    protected void validateFaultDetailString(String receivedDetailString, String controlDetailString, 
            TestContext context, ValidationContext validationContext) throws ValidationException {
        XmlMessageValidationContext xmlMessageValidationContext;
        
        if (validationContext instanceof XmlMessageValidationContext) {
            xmlMessageValidationContext = (XmlMessageValidationContext) validationContext;
        } else {
            xmlMessageValidationContext = new XmlMessageValidationContext();
        }
        
        messageValidator.validateMessage(new DefaultMessage(receivedDetailString), new DefaultMessage(controlDetailString), context, xmlMessageValidationContext);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        // try to find xml message validator in registry
        for (MessageValidator<? extends ValidationContext> validator : messageValidatorRegistry.getMessageValidators()) {
            if (validator instanceof DomXmlMessageValidator &&
                    validator.supportsMessageType(MessageType.XML.name(), new DefaultMessage(""))) {
                messageValidator = (DomXmlMessageValidator) validator;
            }
        }

        if (messageValidator == null) {
            log.warn("No XML message validator found in Spring bean context - setting default validator");
            messageValidator = new DomXmlMessageValidator();
            messageValidator.setApplicationContext(applicationContext);
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
