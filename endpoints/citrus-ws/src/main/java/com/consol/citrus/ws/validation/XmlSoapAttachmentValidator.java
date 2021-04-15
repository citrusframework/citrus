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
import java.util.Optional;

import com.consol.citrus.context.TestContextFactory;
import com.consol.citrus.context.TestContextFactoryBean;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.message.DefaultMessage;
import com.consol.citrus.spi.ReferenceResolver;
import com.consol.citrus.spi.ReferenceResolverAware;
import com.consol.citrus.validation.MessageValidator;
import com.consol.citrus.validation.context.ValidationContext;
import com.consol.citrus.validation.xml.XmlMessageValidationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Soap attachment validator delegating attachment content validation to a {@link MessageValidator}.
 * Through {@link XmlMessageValidationContext} this class supports message validation for XML payload.
 *
 * @author Christoph Deppisch
 */
public class XmlSoapAttachmentValidator extends SimpleSoapAttachmentValidator implements ReferenceResolverAware {

    /** Logger */
    private static final Logger LOG = LoggerFactory.getLogger(XmlSoapAttachmentValidator.class);

    private TestContextFactory testContextFactory;

    /** Xml message validator */
    private MessageValidator<? extends ValidationContext> messageValidator;

    private ReferenceResolver referenceResolver;

    public static final String DEFAULT_XML_MESSAGE_VALIDATOR = "defaultXmlMessageValidator";

	@Override
    protected void validateAttachmentContentData(String receivedContent, String controlContent, String controlContentId) {
        getMessageValidator().validateMessage(new DefaultMessage(receivedContent), new DefaultMessage(controlContent),
                getTestContextFactory().getObject(), Collections.singletonList(new XmlMessageValidationContext()));
    }

    private TestContextFactory getTestContextFactory() {
	    if (testContextFactory == null) {
            if (referenceResolver != null && referenceResolver.isResolvable(TestContextFactoryBean.class)) {
                testContextFactory = referenceResolver.resolve(TestContextFactoryBean.class);
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
        Optional<MessageValidator<? extends ValidationContext>> defaultMessageValidator = getTestContextFactory().getMessageValidatorRegistry().findMessageValidator(DEFAULT_XML_MESSAGE_VALIDATOR);

        if (defaultMessageValidator.isEmpty()) {
            try {
                defaultMessageValidator = Optional.of(getTestContextFactory().getReferenceResolver().resolve(DEFAULT_XML_MESSAGE_VALIDATOR, MessageValidator.class));
            } catch (CitrusRuntimeException e) {
                LOG.warn("Unable to find default XML message validator in message validator registry");
            }
        }

	    if (defaultMessageValidator.isEmpty()) {
            // try to find xml message validator via resource path lookup
            defaultMessageValidator = MessageValidator.lookup("xml");
        }

	    if (defaultMessageValidator.isPresent()) {
	        messageValidator = defaultMessageValidator.get();
            return messageValidator;
        }

	    throw new CitrusRuntimeException("Unable to locate proper XML message validator - please add validator to project");
    }

    @Override
    public void setReferenceResolver(ReferenceResolver referenceResolver) {
        this.referenceResolver = referenceResolver;
    }
}
