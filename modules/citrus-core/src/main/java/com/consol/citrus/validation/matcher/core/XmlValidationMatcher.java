package com.consol.citrus.validation.matcher.core;

import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.ValidationException;
import com.consol.citrus.message.DefaultMessage;
import com.consol.citrus.message.MessageType;
import com.consol.citrus.validation.MessageValidator;
import com.consol.citrus.validation.MessageValidatorRegistry;
import com.consol.citrus.validation.context.ValidationContext;
import com.consol.citrus.validation.matcher.ValidationMatcher;
import com.consol.citrus.validation.xml.DomXmlMessageValidator;
import com.consol.citrus.validation.xml.XmlMessageValidationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.List;

/**
 * Validation matcher receives a XML data and validates it against expected XML with full
 * XML validation support (e.g. ignoring elements, namespace support, ...).
 * 
 * @author Christoph Deppisch
 */
public class XmlValidationMatcher implements ValidationMatcher, ApplicationContextAware, InitializingBean {

    /** CDATA section starting and ending in XML */
    private static final String CDATA_SECTION_START = "<![CDATA[";
    private static final String CDATA_SECTION_END = "]]>";

    @Autowired(required = false)
    private MessageValidatorRegistry messageValidatorRegistry;

    /** Xml message validator */
    private DomXmlMessageValidator xmlMessageValidator;
    
    /** Spring bean application context */
    private ApplicationContext applicationContext;

    /** Logger */
    private static final Logger LOG = LoggerFactory.getLogger(XmlValidationMatcher.class);
    
    /**
      * {@inheritDoc}
      */
    public void validate(String fieldName, String value, List<String> controlParameters, TestContext context) throws ValidationException {
        String control = controlParameters.get(0);
        XmlMessageValidationContext validationContext = new XmlMessageValidationContext();
        xmlMessageValidator.validateMessage(new DefaultMessage(removeCDataElements(value)), new DefaultMessage(control), context, validationContext);
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

    /**
     * Inject Spring bean application context
     * @param applicationContext
     * @throws BeansException
     */
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    /**
     * Initialize xml message validator if not injected by Spring bean context.
     * @throws Exception
     */
    public void afterPropertiesSet() throws Exception {
        // try to find xml message validator in registry
        for (MessageValidator<? extends ValidationContext> messageValidator : messageValidatorRegistry.getMessageValidators()) {
            if (messageValidator instanceof DomXmlMessageValidator &&
                    messageValidator.supportsMessageType(MessageType.XML.name(), new DefaultMessage(""))) {
                xmlMessageValidator = (DomXmlMessageValidator) messageValidator;
            }
        }

        if (xmlMessageValidator == null) {
            LOG.warn("No XML message validator found in Spring bean context - setting default validator");
            xmlMessageValidator = new DomXmlMessageValidator();
            xmlMessageValidator.setApplicationContext(applicationContext);
        }
    }
}
