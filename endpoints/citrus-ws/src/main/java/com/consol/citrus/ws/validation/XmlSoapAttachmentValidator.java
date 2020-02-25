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

import java.util.Collections;

import com.consol.citrus.context.TestContextFactory;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.message.DefaultMessage;
import com.consol.citrus.spi.ResourcePathTypeResolver;
import com.consol.citrus.spi.TypeResolver;
import com.consol.citrus.validation.MessageValidator;
import com.consol.citrus.validation.MessageValidatorRegistry;
import com.consol.citrus.validation.context.ValidationContext;
import com.consol.citrus.validation.xml.XmlMessageValidationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * Soap attachment validator delegating attachment content validation to a {@link MessageValidator}.
 * Through {@link XmlMessageValidationContext} this class supports message validation for XML payload.
 *
 * @author Christoph Deppisch
 */
public class XmlSoapAttachmentValidator extends SimpleSoapAttachmentValidator implements ApplicationContextAware {

    /** Logger */
    private static Logger log = LoggerFactory.getLogger(XmlSoapFaultValidator.class);

    private TestContextFactory testContextFactory;

    /** Xml message validator */
    private MessageValidator<? extends ValidationContext> messageValidator;

    /** Type resolver for message validator lookup via resource path */
    private static final TypeResolver TYPE_RESOLVER = new ResourcePathTypeResolver(MessageValidatorRegistry.RESOURCE_PATH);

    private ApplicationContext applicationContext;

    public static final String DEFAULT_XML_MESSAGE_VALIDATOR = "defaultXmlMessageValidator";

	@Override
    protected void validateAttachmentContentData(String receivedContent, String controlContent, String controlContentId) {
        getMessageValidator().validateMessage(new DefaultMessage(receivedContent), new DefaultMessage(controlContent),
                getTestContextFactory().getObject(), Collections.singletonList(new XmlMessageValidationContext()));
    }

    private TestContextFactory getTestContextFactory() {
	    if (testContextFactory == null) {
            if (applicationContext != null && !applicationContext.getBeansOfType(TestContextFactory.class).isEmpty()) {
                testContextFactory = applicationContext.getBean(TestContextFactory.class);
            } else {
                testContextFactory = TestContextFactory.newInstance();
            }
        }

	    return testContextFactory;
    }

    private MessageValidator<? extends ValidationContext> getMessageValidator() {
	    if (messageValidator != null) {
	        return messageValidator;
        }

        // try to find xml message validator in registry
        messageValidator = getTestContextFactory().getMessageValidatorRegistry().getMessageValidators().get(DEFAULT_XML_MESSAGE_VALIDATOR);

        if (messageValidator == null) {
            try {
                messageValidator = getTestContextFactory().getReferenceResolver().resolve(DEFAULT_XML_MESSAGE_VALIDATOR, MessageValidator.class);
            } catch (CitrusRuntimeException e) {
                log.warn("Unable to find default XML message validator in message validator registry");
            }
        }

	    if (messageValidator == null) {
            // try to find xml message validator via resource path lookup
            messageValidator = TYPE_RESOLVER.resolve("xml");
        }

        return messageValidator;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
