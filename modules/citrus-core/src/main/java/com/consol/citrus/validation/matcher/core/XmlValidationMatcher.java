package com.consol.citrus.validation.matcher.core;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.support.MessageBuilder;

import com.consol.citrus.context.TestContext;
import com.consol.citrus.context.TestContextFactoryBean;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.exceptions.ValidationException;
import com.consol.citrus.validation.matcher.ValidationMatcher;
import com.consol.citrus.validation.xml.DomXmlMessageValidator;
import com.consol.citrus.validation.xml.XmlMessageValidationContext;

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

    @Autowired
    private DomXmlMessageValidator xmlMessageValidator;
    
    @Autowired
    private TestContextFactoryBean testContextFactory;
    
    /**
      * {@inheritDoc}
      */
    public void validate(String fieldName, String value, String control) throws ValidationException {
        XmlMessageValidationContext validationContext = new XmlMessageValidationContext();
        validationContext.setControlMessage(MessageBuilder.withPayload(control).build());
        
        TestContext context;
        try {
            context = (TestContext)testContextFactory.getObject();
        } catch (Exception e) {
            throw new CitrusRuntimeException("Unalble to create test context", e);
        }
        
        xmlMessageValidator.validateMessage(MessageBuilder.withPayload(removeCDataElements(value)).build(), context, validationContext);
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
