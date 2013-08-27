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

package com.consol.citrus.validation.xml;

import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.exceptions.ValidationException;
import com.consol.citrus.testng.AbstractTestNGUnitTest;
import com.consol.citrus.xml.XsdSchemaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.integration.Message;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.xml.xsd.SimpleXsdSchema;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.*;

/**
 * @author Christoph Deppisch
 */
public class DomXmlMessageValidatorTest extends AbstractTestNGUnitTest {
    
    @Autowired
    private DomXmlMessageValidator validator;
    
    @Test
    public void validateXMLSchema() throws SAXException, IOException, ParserConfigurationException {
        Message<?> message = MessageBuilder.withPayload("<message xmlns='http://citrus'>"
                        + "<correlationId>Kx1R123456789</correlationId>"
                        + "<bookingId>Bx1G987654321</bookingId>"
                        + "<test>Hello TestFramework</test>"
                    + "</message>").build();
        
        DomXmlMessageValidator validator = new DomXmlMessageValidator();
        
        XsdSchemaRepository schemaRepository = new XsdSchemaRepository();
        Resource schemaResource = new ClassPathResource("com/consol/citrus/validation/test.xsd");
        SimpleXsdSchema schema = new SimpleXsdSchema(schemaResource);
        schema.afterPropertiesSet();
        
        schemaRepository.getSchemas().add(schema);
        
        validator.addSchemaRepository(schemaRepository);
        
        validator.validateXMLSchema(message, new XmlMessageValidationContext());
    }
    
    @Test
    public void validateWithExplicitXMLSchema() throws SAXException, IOException, ParserConfigurationException {
        Message<?> message = MessageBuilder.withPayload("<message xmlns='http://citrus'>"
                        + "<correlationId>Kx1R123456789</correlationId>"
                        + "<bookingId>Bx1G987654321</bookingId>"
                        + "<test>Hello TestFramework</test>"
                    + "</message>").build();
        
        XmlMessageValidationContext validationContext = new XmlMessageValidationContext();
        validationContext.setSchema("testSchema2"); // defined as bean in application context
        validator.validateXMLSchema(message, validationContext);
    }
    
    @Test
    public void validateWithExplicitSpringSchemaRepository() throws SAXException, IOException, ParserConfigurationException {
        Message<?> message = MessageBuilder.withPayload("<message xmlns='http://citrus'>"
                        + "<correlationId>Kx1R123456789</correlationId>"
                        + "<bookingId>Bx1G987654321</bookingId>"
                        + "<test>Hello TestFramework</test>"
                    + "</message>").build();
        
        XmlMessageValidationContext validationContext = new XmlMessageValidationContext();
        validationContext.setSchemaRepository("testSchemaRepository1"); // defined as bean in application context
        validator.validateXMLSchema(message, validationContext);
    }
    
    @Test
    public void validateWithExplicitCitrusSchemaRepository() throws SAXException, IOException, ParserConfigurationException {
        Message<?> message = MessageBuilder.withPayload("<message xmlns='http://citrus'>"
                        + "<correlationId>Kx1R123456789</correlationId>"
                        + "<bookingId>Bx1G987654321</bookingId>"
                        + "<test>Hello TestFramework</test>"
                    + "</message>").build();

        XmlMessageValidationContext validationContext = new XmlMessageValidationContext();
        validationContext.setSchemaRepository("testSchemaRepository2"); // defined as bean in application context
        validator.validateXMLSchema(message, validationContext);
    }

    @Test
    public void validateWithDefaultSchemaRepository() throws SAXException, IOException, ParserConfigurationException {
        Message<?> message = MessageBuilder.withPayload("<message xmlns='http://citrus'>"
                        + "<correlationId>Kx1R123456789</correlationId>"
                        + "<bookingId>Bx1G987654321</bookingId>"
                        + "<test>Hello TestFramework</test>"
                    + "</message>").build();
        
        validator.validateXMLSchema(message, new XmlMessageValidationContext());
    }
    
    @Test
    public void validateMissingDefaultSchemaRepository() throws SAXException, IOException, ParserConfigurationException {
        Message<?> message = MessageBuilder.withPayload("<message xmlns='http://citrus'>"
                        + "<correlationId>Kx1R123456789</correlationId>"
                        + "<bookingId>Bx1G987654321</bookingId>"
                        + "<test>Hello TestFramework</test>"
                    + "</message>").build();
        
        DomXmlMessageValidator validator = new DomXmlMessageValidator();
        
        XsdSchemaRepository schemaRepository = new XsdSchemaRepository();
        schemaRepository.setBeanName("schemaRepository1");
        Resource schemaResource = new ClassPathResource("com/consol/citrus/validation/test.xsd");
        SimpleXsdSchema schema = new SimpleXsdSchema(schemaResource);
        schema.afterPropertiesSet();
        
        schemaRepository.getSchemas().add(schema);
        
        validator.addSchemaRepository(schemaRepository);
        
        XsdSchemaRepository schemaRepository2 = new XsdSchemaRepository();
        schemaRepository2.setBeanName("schemaRepository2");
        validator.addSchemaRepository(schemaRepository2);
        
        try {
            validator.validateXMLSchema(message, new XmlMessageValidationContext());
            Assert.fail("Missing exception due to missing default schema repository");
        } catch (CitrusRuntimeException e) {
            Assert.assertTrue(e.getMessage().contains("either define the repository to be used or define a default repository"));
        }
    }

    @Test
    public void validateNoSchemaRepositoryAtAll() throws SAXException, IOException, ParserConfigurationException {
        Message<?> message = MessageBuilder.withPayload("<message xmlns='http://citrus'>"
                + "<correlationId>Kx1R123456789</correlationId>"
                + "<bookingId>Bx1G987654321</bookingId>"
                + "<test>Hello TestFramework</test>"
                + "</message>").build();

        DomXmlMessageValidator validator = new DomXmlMessageValidator();
        validator.validateXMLSchema(message, new XmlMessageValidationContext());
    }
    
    @Test(expectedExceptions = {ValidationException.class})
    public void validateXMLSchemaError() throws SAXException, IOException, ParserConfigurationException {
        Message<?> message = MessageBuilder.withPayload("<message xmlns='http://citrus'>"
                        + "<correlationId>Kx1R123456789</correlationId>"
                        + "<bookingId>Bx1G987654321</bookingId>"
                        + "<test>Hello TestFramework</test>"
                        + "<wrongElement>totally wrong</wrongElement>"
                    + "</message>").build();
        
        DomXmlMessageValidator validator = new DomXmlMessageValidator();
        
        XsdSchemaRepository schemaRepository = new XsdSchemaRepository();
        Resource schemaResource = new ClassPathResource("com/consol/citrus/validation/test.xsd");
        SimpleXsdSchema schema = new SimpleXsdSchema(schemaResource);
        schema.afterPropertiesSet();
        
        schemaRepository.getSchemas().add(schema);
        
        validator.addSchemaRepository(schemaRepository);
        
        validator.validateXMLSchema(message, new XmlMessageValidationContext());
    }
    
    @Test
    public void testExpectDefaultNamespace() {
        Message<?> message = MessageBuilder.withPayload("<root xmlns='http://citrus'>"
                        + "<element attributeA='attribute-value' attributeB='attribute-value'>"
                        + "<sub-element attribute='A'>text-value</sub-element>"
                        + "</element>" 
                    + "</root>").build();
        
        Map<String, String> expectedNamespaces = new HashMap<String, String>();
        expectedNamespaces.put("", "http://citrus");
        
        DomXmlMessageValidator validator = new DomXmlMessageValidator();
        validator.validateNamespaces(expectedNamespaces, message);
    }
    
    @Test
    public void testExpectNamespace() {
    	Message<?> message = MessageBuilder.withPayload("<ns1:root xmlns:ns1='http://citrus/ns1'>"
                        + "<ns1:element attributeA='attribute-value' attributeB='attribute-value'>"
                        + "<ns1:sub-element attribute='A'>text-value</ns1:sub-element>"
                        + "</ns1:element>" 
                    + "</ns1:root>").build();
        
        Map<String, String> expectedNamespaces = new HashMap<String, String>();
        expectedNamespaces.put("ns1", "http://citrus/ns1");
        
        DomXmlMessageValidator validator = new DomXmlMessageValidator();
        validator.validateNamespaces(expectedNamespaces, message);
    }
    
    @Test
    public void testExpectMixedNamespaces() {
    	Message<?> message = MessageBuilder.withPayload("<root xmlns='http://citrus/default' xmlns:ns1='http://citrus/ns1'>"
                        + "<element attributeA='attribute-value' attributeB='attribute-value'>"
                        + "<sub-element attribute='A'>text-value</sub-element>"
                        + "</element>" 
                    + "</root>").build();
        
        Map<String, String> expectedNamespaces = new HashMap<String, String>();
        expectedNamespaces.put("", "http://citrus/default");
        expectedNamespaces.put("ns1", "http://citrus/ns1");
        
        DomXmlMessageValidator validator = new DomXmlMessageValidator();
        validator.validateNamespaces(expectedNamespaces, message);
    }
    
    @Test
    public void testExpectMultipleNamespaces() {
    	Message<?> message = MessageBuilder.withPayload("<root xmlns='http://citrus/default' xmlns:ns1='http://citrus/ns1' xmlns:ns2='http://citrus/ns2'>"
                        + "<element attributeA='attribute-value' attributeB='attribute-value'>"
                        + "<sub-element attribute='A'>text-value</sub-element>"
                        + "</element>" 
                    + "</root>").build();
        
        Map<String, String> expectedNamespaces = new HashMap<String, String>();
        expectedNamespaces.put("", "http://citrus/default");
        expectedNamespaces.put("ns1", "http://citrus/ns1");
        expectedNamespaces.put("ns2", "http://citrus/ns2");
        
        DomXmlMessageValidator validator = new DomXmlMessageValidator();
        validator.validateNamespaces(expectedNamespaces, message);
    }
    
    @Test(expectedExceptions = {ValidationException.class})
    public void testExpectDefaultNamespaceError() {
    	Message<?> message = MessageBuilder.withPayload("<root xmlns='http://citrus'>"
                        + "<element attributeA='attribute-value' attributeB='attribute-value'>"
                        + "<sub-element attribute='A'>text-value</sub-element>"
                        + "</element>" 
                    + "</root>").build();
        
        Map<String, String> expectedNamespaces = new HashMap<String, String>();
        expectedNamespaces.put("", "http://citrus/wrong");
        
        DomXmlMessageValidator validator = new DomXmlMessageValidator();
        validator.validateNamespaces(expectedNamespaces, message);
    }
    
    @Test(expectedExceptions = {ValidationException.class})
    public void testExpectNamespaceError() {
    	Message<?> message = MessageBuilder.withPayload("<ns1:root xmlns:ns1='http://citrus/ns1'>"
                        + "<ns1:element attributeA='attribute-value' attributeB='attribute-value'>"
                        + "<ns1:sub-element attribute='A'>text-value</ns1:sub-element>"
                        + "</ns1:element>" 
                    + "</ns1:root>").build();
        
        Map<String, String> expectedNamespaces = new HashMap<String, String>();
        expectedNamespaces.put("ns1", "http://citrus/ns1/wrong");
        
        DomXmlMessageValidator validator = new DomXmlMessageValidator();
        validator.validateNamespaces(expectedNamespaces, message);
    }
    
    @Test(expectedExceptions = {ValidationException.class})
    public void testExpectMixedNamespacesError() {
    	Message<?> message = MessageBuilder.withPayload("<root xmlns='http://citrus/default' xmlns:ns1='http://citrus/ns1'>"
                        + "<element attributeA='attribute-value' attributeB='attribute-value'>"
                        + "<sub-element attribute='A'>text-value</sub-element>"
                        + "</element>" 
                    + "</root>").build();
        
        Map<String, String> expectedNamespaces = new HashMap<String, String>();
        expectedNamespaces.put("", "http://citrus/default/wrong");
        expectedNamespaces.put("ns1", "http://citrus/ns1");
        
        DomXmlMessageValidator validator = new DomXmlMessageValidator();
        validator.validateNamespaces(expectedNamespaces, message);
    }
    
    @Test(expectedExceptions = {ValidationException.class})
    public void testExpectMultipleNamespacesError() {
    	Message<?> message = MessageBuilder.withPayload("<root xmlns='http://citrus/default' xmlns:ns1='http://citrus/ns1' xmlns:ns2='http://citrus/ns2'>"
                        + "<element attributeA='attribute-value' attributeB='attribute-value'>"
                        + "<sub-element attribute='A'>text-value</sub-element>"
                        + "</element>" 
                    + "</root>").build();
        
        Map<String, String> expectedNamespaces = new HashMap<String, String>();
        expectedNamespaces.put("", "http://citrus/default");
        expectedNamespaces.put("ns1", "http://citrus/ns1/wrong");
        expectedNamespaces.put("ns2", "http://citrus/ns2");
     
        DomXmlMessageValidator validator = new DomXmlMessageValidator();
        validator.validateNamespaces(expectedNamespaces, message);
    }
    
    @Test(expectedExceptions = {ValidationException.class})
    public void testExpectWrongNamespacePrefix() {
    	Message<?> message = MessageBuilder.withPayload("<root xmlns='http://citrus/default' xmlns:ns1='http://citrus/ns1' xmlns:ns2='http://citrus/ns2'>"
                        + "<element attributeA='attribute-value' attributeB='attribute-value'>"
                        + "<sub-element attribute='A'>text-value</sub-element>"
                        + "</element>" 
                    + "</root>").build();
        
        Map<String, String> expectedNamespaces = new HashMap<String, String>();
        expectedNamespaces.put("", "http://citrus/default");
        expectedNamespaces.put("nswrong", "http://citrus/ns1");
        expectedNamespaces.put("ns2", "http://citrus/ns2");
        
        DomXmlMessageValidator validator = new DomXmlMessageValidator();
        validator.validateNamespaces(expectedNamespaces, message);
    }
    
    @Test(expectedExceptions = {ValidationException.class})
    public void testExpectDefaultNamespaceButNamespace() {
    	Message<?> message = MessageBuilder.withPayload("<ns0:root xmlns:ns0='http://citrus/default' xmlns:ns1='http://citrus/ns1' xmlns:ns2='http://citrus/ns2'>"
                        + "<ns0:element attributeA='attribute-value' attributeB='attribute-value'>"
                        + "<ns0:sub-element attribute='A'>text-value</ns0:sub-element>"
                        + "</ns0:element>" 
                    + "</ns0:root>").build();
        
        Map<String, String> expectedNamespaces = new HashMap<String, String>();
        expectedNamespaces.put("", "http://citrus/default");
        expectedNamespaces.put("ns1", "http://citrus/ns1");
        expectedNamespaces.put("ns2", "http://citrus/ns2");
        
        DomXmlMessageValidator validator = new DomXmlMessageValidator();
        validator.validateNamespaces(expectedNamespaces, message);
    }
    
    @Test(expectedExceptions = {ValidationException.class})
    public void testExpectNamespaceButDefaultNamespace() {
    	Message<?> message = MessageBuilder.withPayload("<root xmlns='http://citrus/default' xmlns:ns1='http://citrus/ns1' xmlns:ns2='http://citrus/ns2'>"
                        + "<element attributeA='attribute-value' attributeB='attribute-value'>"
                        + "<sub-element attribute='A'>text-value</sub-element>"
                        + "</element>" 
                    + "</root>").build();
        
        Map<String, String> expectedNamespaces = new HashMap<String, String>();
        expectedNamespaces.put("ns0", "http://citrus/default");
        expectedNamespaces.put("ns1", "http://citrus/ns1");
        expectedNamespaces.put("ns2", "http://citrus/ns2");
        
        DomXmlMessageValidator validator = new DomXmlMessageValidator();
        validator.validateNamespaces(expectedNamespaces, message);
    }
    
    @Test(expectedExceptions = {ValidationException.class})
    public void testExpectAdditionalNamespace() {
    	Message<?> message = MessageBuilder.withPayload("<root xmlns='http://citrus/default' xmlns:ns1='http://citrus/ns1' xmlns:ns2='http://citrus/ns2'>"
                        + "<element attributeA='attribute-value' attributeB='attribute-value'>"
                        + "<sub-element attribute='A'>text-value</sub-element>"
                        + "</element>" 
                    + "</root>").build();
        
        Map<String, String> expectedNamespaces = new HashMap<String, String>();
        expectedNamespaces.put("", "http://citrus/default");
        expectedNamespaces.put("ns1", "http://citrus/ns1");
        expectedNamespaces.put("ns2", "http://citrus/ns2");
        expectedNamespaces.put("ns4", "http://citrus/ns4");
        
        DomXmlMessageValidator validator = new DomXmlMessageValidator();
        validator.validateNamespaces(expectedNamespaces, message);
    }
    
    @Test(expectedExceptions = {ValidationException.class})
    public void testExpectNamespaceButNamespaceMissing() {
    	Message<?> message = MessageBuilder.withPayload("<root xmlns='http://citrus/default' xmlns:ns1='http://citrus/ns1' xmlns:ns2='http://citrus/ns2' xmlns:ns4='http://citrus/ns4'>"
                        + "<element attributeA='attribute-value' attributeB='attribute-value'>"
                        + "<sub-element attribute='A'>text-value</sub-element>"
                        + "</element>" 
                    + "</root>").build();
        
        Map<String, String> expectedNamespaces = new HashMap<String, String>();
        expectedNamespaces.put("", "http://citrus/default");
        expectedNamespaces.put("ns1", "http://citrus/ns1");
        expectedNamespaces.put("ns2", "http://citrus/ns2");
        
        DomXmlMessageValidator validator = new DomXmlMessageValidator();
        validator.validateNamespaces(expectedNamespaces, message);
    }
    
    @Test
    public void testValidateMessageElementsWithXPathSuccessful() {
        Message<?> message = MessageBuilder.withPayload("<root>"
                        + "<element attributeA='attribute-value' attributeB='attribute-value'>"
                        + "<sub-element attribute='A'>text-value</sub-element>"
                        + "</element>"
                    + "</root>").build();

        XmlMessageValidationContext validationContext = new XmlMessageValidationContext();
        validationContext.setPathValidationExpressions(Collections.singletonMap("//element/sub-element", "text-value"));

        DomXmlMessageValidator validator = new DomXmlMessageValidator();

        validator.validateMessageElements(message, validationContext, context);
    }

    @Test(expectedExceptions = {ValidationException.class})
    public void testValidateMessageElementsWithXPathNotSuccessful() {
        Message<?> message = MessageBuilder.withPayload("<root>"
                        + "<element attributeA='attribute-value' attributeB='attribute-value'>"
                        + "<sub-element attribute='A'>text-value</sub-element>"
                        + "</element>"
                    + "</root>").build();

        XmlMessageValidationContext validationContext = new XmlMessageValidationContext();
        validationContext.setPathValidationExpressions(Collections.singletonMap(
                "//element/sub-element", "false-value"));

        DomXmlMessageValidator validator = new DomXmlMessageValidator();

        validator.validateMessageElements(message, validationContext, context);
    }

    @Test
    public void testValidateMessageElementsWithDotNotationSuccessful() {
        Message<?> message = MessageBuilder.withPayload("<root>"
                        + "<element attributeA='attribute-value' attributeB='attribute-value'>"
                        + "<sub-element attribute='A'>text-value</sub-element>"
                        + "</element>"
                    + "</root>").build();

        XmlMessageValidationContext validationContext = new XmlMessageValidationContext();
        validationContext.setPathValidationExpressions(Collections.singletonMap(
                "root.element.sub-element", "text-value"));

        DomXmlMessageValidator validator = new DomXmlMessageValidator();

        validator.validateMessageElements(message, validationContext, context);
    }

    @Test(expectedExceptions = {ValidationException.class})
    public void testValidateMessageElementsWithDotNotationNotSuccessful() {
        Message<?> message = MessageBuilder.withPayload("<root>"
                        + "<element attributeA='attribute-value' attributeB='attribute-value'>"
                        + "<sub-element attribute='A'>text-value</sub-element>"
                        + "</element>"
                    + "</root>").build();

        XmlMessageValidationContext validationContext = new XmlMessageValidationContext();
        validationContext.setPathValidationExpressions(Collections.singletonMap(
                "root.element.sub-element", "false-value"));

        DomXmlMessageValidator validator = new DomXmlMessageValidator();

        validator.validateMessageElements(message, validationContext, context);
    }

    @Test
    public void testValidateMessageElementsWithMixedNotationsSuccessful() {
        Message<?> message = MessageBuilder.withPayload("<root>"
                        + "<element attributeA='attribute-value' attributeB='attribute-value'>"
                        + "<sub-element attribute='A'>text-value</sub-element>"
                        + "</element>"
                    + "</root>").build();

        XmlMessageValidationContext validationContext = new XmlMessageValidationContext();
        //mix of xpath and dot-notation
        Map<String, String> validationExpressions = new HashMap<String, String>();
        validationExpressions.put("//element/sub-element", "text-value");
        validationExpressions.put("root.element.sub-element", "text-value");
        validationContext.setPathValidationExpressions(validationExpressions);

        DomXmlMessageValidator validator = new DomXmlMessageValidator();

        validator.validateMessageElements(message, validationContext, context);
    }

    @Test
    public void testValidateMessagePayloadSuccess() {
        Message<?> message = MessageBuilder.withPayload("<root>"
                        + "<element attributeA='attribute-value' attributeB='attribute-value'>"
                        + "<sub-element attribute='A'>text-value</sub-element>"
                        + "</element>"
                    + "</root>").build();

        Message<?> controlMessage = MessageBuilder.withPayload("<root>"
                        + "<element attributeA='attribute-value' attributeB='attribute-value'>"
                        + "<sub-element attribute='A'>text-value</sub-element>"
                        + "</element>"
                    + "</root>").build();

        XmlMessageValidationContext validationContext = new XmlMessageValidationContext();
        validationContext.setControlMessage(controlMessage);

        DomXmlMessageValidator validator = new DomXmlMessageValidator();
        validator.validateMessagePayload(message, validationContext, context);
    }
    
    @Test
    public void testValidateMessagePayloadWithIgnoresSuccess() {
        Message<?> message = MessageBuilder.withPayload("<root>"
                        + "<element attributeA='attribute-value' attributeB='attribute-value'>"
                        + "<sub-element1 attribute='A'>THIS_IS_IGNORED_BY_XPATH</sub-element1>"
                        + "<sub-element2 attribute='A'>THIS IS IGNORED BY IGNORE-EXPR</sub-element2>"
                        + "<sub-element3 attribute='A'>a text</sub-element3>"
                        + "</element>"
                    + "</root>").build();

        Message<?> controlMessage = MessageBuilder.withPayload("<root>"
                        + "<element attributeA='attribute-value' attributeB='attribute-value'>"
                        + "<sub-element1 attribute='A'>text-value</sub-element1>"
                        + "<sub-element2 attribute='A'>@ignore@</sub-element2>"
                        + "<sub-element3 attribute='A'>a text</sub-element3>"
                        + "</element>"
                    + "</root>").build();

        XmlMessageValidationContext validationContext = new XmlMessageValidationContext();

        Set<String> ignoreExpressions = new HashSet<String>();
        ignoreExpressions.add("//root/element/sub-element1");

        validationContext.setControlMessage(controlMessage);
        validationContext.setIgnoreExpressions(ignoreExpressions);
        DomXmlMessageValidator validator = new DomXmlMessageValidator();
        validator.validateMessagePayload(message, validationContext, context);
    }
    
    @Test(expectedExceptions = {ValidationException.class})
    public void testValidateMessagePayloadWithValidationMatchersFailsBecauseOfAttribute() {
        Message<?> message = MessageBuilder.withPayload("<root>"
                        + "<element attributeA='attribute-value' attributeB='attribute-value'>"
                        + "<sub-element attribute='text-attribute'>text-element</sub-element>"
                        + "</element>"
                    + "</root>").build();

        Message<?> controlMessage = MessageBuilder.withPayload("<root>"
                        + "<element attributeA='attribute-value' attributeB='attribute-value'>"
                        + "<sub-element attribute='@startsWith(FAIL)@'>@startsWith(text)@</sub-element>"
                        + "</element>"
                    + "</root>").build();

        XmlMessageValidationContext validationContext = new XmlMessageValidationContext();

        validationContext.setControlMessage(controlMessage);
        DomXmlMessageValidator validator = new DomXmlMessageValidator();
        validator.validateMessagePayload(message, validationContext, context);
    }
    
    @Test(expectedExceptions = {ValidationException.class})
    public void testValidateMessagePayloadWithValidationMatcherOnElementFails() {
        Message<?> message = MessageBuilder.withPayload("<root>"
                        + "<element attributeA='attribute-value' attributeB='attribute-value'>"
                        + "<sub-element attribute='text-attribute'>text-element</sub-element>"
                        + "</element>"
                    + "</root>").build();

        Message<?> controlMessage = MessageBuilder.withPayload("<root>"
                        + "<element attributeA='attribute-value' attributeB='attribute-value'>"
                        + "<sub-element attribute='text-attribute'>@startsWith(FAIL)@</sub-element>"
                        + "</element>"
                    + "</root>").build();

        XmlMessageValidationContext validationContext = new XmlMessageValidationContext();

        validationContext.setControlMessage(controlMessage);
        DomXmlMessageValidator validator = new DomXmlMessageValidator();
        validator.validateMessagePayload(message, validationContext, context);
    }
    
    @Test(expectedExceptions = {ValidationException.class})
    public void testValidateMessagePayloadWithValidationMatcherOnAttributeFails() {
        Message<?> message = MessageBuilder.withPayload("<root>"
                        + "<element attributeA='attribute-value' attributeB='attribute-value'>"
                        + "<sub-element attribute='text-attribute'>text-element</sub-element>"
                        + "</element>"
                    + "</root>").build();

        Message<?> controlMessage = MessageBuilder.withPayload("<root>"
                        + "<element attributeA='attribute-value' attributeB='attribute-value'>"
                        + "<sub-element attribute='@startsWith(FAIL)@'>text-element</sub-element>"
                        + "</element>"
                    + "</root>").build();

        XmlMessageValidationContext validationContext = new XmlMessageValidationContext();

        validationContext.setControlMessage(controlMessage);
        DomXmlMessageValidator validator = new DomXmlMessageValidator();
        validator.validateMessagePayload(message, validationContext, context);
    }

    @Test
    public void testNamespaceQualifiedAttributeValue() {
        Message<?> message = MessageBuilder.withPayload("<root xmlns='http://citrus/default' xmlns:ns1='http://citrus/ns1' xmlns:ns2='http://citrus/ns2' xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance'>"
                        + "<element xsi:type='ns1:attribute-value' attributeB='attribute-value'>"
                        + "<sub-element xsi:type='ns2:AType'>text-value</sub-element>"
                        + "</element>"
                    + "</root>").build();

        Message<?> controlMessage = MessageBuilder.withPayload("<root xmlns='http://citrus/default' xmlns:ns1='http://citrus/ns1' xmlns:ns2='http://citrus/ns2' xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance'>"
                        + "<element xsi:type='ns1:attribute-value' attributeB='attribute-value'>"
                        + "<sub-element xsi:type='ns2:AType'>text-value</sub-element>"
                        + "</element>"
                    + "</root>").build();

        XmlMessageValidationContext validationContext = new XmlMessageValidationContext();

        validationContext.setControlMessage(controlMessage);
        DomXmlMessageValidator validator = new DomXmlMessageValidator();
        validator.validateMessagePayload(message, validationContext, context);
    }

    @Test
    public void testNamespaceQualifiedAttributeValueParentDeclaration() {
        Message<?> message = MessageBuilder.withPayload("<root xmlns='http://citrus/default' xmlns:ns2='http://citrus/ns2' xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance'>"
                + "<element xmlns:ns1='http://citrus/ns1' xsi:type='ns1:attribute-value' attributeB='attribute-value'>"
                + "<sub-element xsi:type='ns2:AType'>text-value</sub-element>"
                + "</element>"
                + "</root>").build();

        Message<?> controlMessage = MessageBuilder.withPayload("<root xmlns='http://citrus/default' xmlns:ns1='http://citrus/ns1' xmlns:ns2='http://citrus/ns2' xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance'>"
                + "<element xsi:type='ns1:attribute-value' attributeB='attribute-value'>"
                + "<sub-element xsi:type='ns2:AType'>text-value</sub-element>"
                + "</element>"
                + "</root>").build();

        XmlMessageValidationContext validationContext = new XmlMessageValidationContext();

        validationContext.setControlMessage(controlMessage);
        DomXmlMessageValidator validator = new DomXmlMessageValidator();
        validator.validateMessagePayload(message, validationContext, context);
    }

    @Test
    public void testNamespaceQualifiedAttributeValueParentDeclarationInSource() {
        Message<?> message = MessageBuilder.withPayload("<root xmlns='http://citrus/default' xmlns:ns2='http://citrus/ns2' xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance'>"
                + "<element xmlns:ns1='http://citrus/ns1' xsi:type='ns1:attribute-value' attributeB='attribute-value'>"
                + "<sub-element xsi:type='ns2:AType'>text-value</sub-element>"
                + "</element>"
                + "</root>").build();

        Message<?> controlMessage = MessageBuilder.withPayload("<root xmlns='http://citrus/default' xmlns:ns2='http://citrus/ns2' xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance'>"
                + "<element xmlns:ns1='http://citrus/ns1' xsi:type='ns1:attribute-value' attributeB='attribute-value'>"
                + "<sub-element xsi:type='ns2:AType'>text-value</sub-element>"
                + "</element>"
                + "</root>").build();

        XmlMessageValidationContext validationContext = new XmlMessageValidationContext();

        validationContext.setControlMessage(controlMessage);
        DomXmlMessageValidator validator = new DomXmlMessageValidator();
        validator.validateMessagePayload(message, validationContext, context);
    }

    @Test
    public void testNamespaceQualifiedAttributeValueDifferentPrefix() {
        Message<?> message = MessageBuilder.withPayload("<root xmlns='http://citrus/default' xmlns:ns1='http://citrus/ns1' xmlns:ns2='http://citrus/ns2' xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance'>"
                + "<element xsi:type='ns1:attribute-value' attributeB='attribute-value'>"
                + "<sub-element xsi:type='ns2:AType'>text-value</sub-element>"
                + "</element>"
                + "</root>").build();

        Message<?> controlMessage = MessageBuilder.withPayload("<root xmlns='http://citrus/default' xmlns:cit='http://citrus/ns1' xmlns:ns2='http://citrus/ns2' xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance'>"
                + "<element xsi:type='cit:attribute-value' attributeB='attribute-value'>"
                + "<sub-element xsi:type='ns2:AType'>text-value</sub-element>"
                + "</element>"
                + "</root>").build();

        XmlMessageValidationContext validationContext = new XmlMessageValidationContext();

        validationContext.setControlMessage(controlMessage);
        DomXmlMessageValidator validator = new DomXmlMessageValidator();
        validator.validateMessagePayload(message, validationContext, context);
    }

    @Test
    public void testNamespaceQualifiedLikeAttributeValues() {
        Message<?> message = MessageBuilder.withPayload("<root xmlns='http://citrus/default' xmlns:ns1='http://citrus/ns1' xmlns:ns2='http://citrus/ns2' xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance'>"
                + "<element credentials='username:password' attributeB='attribute-value'>"
                + "<sub-element>text-value</sub-element>"
                + "</element>"
                + "</root>").build();

        Message<?> controlMessage = MessageBuilder.withPayload("<root xmlns='http://citrus/default' xmlns:ns1='http://citrus/ns1' xmlns:ns2='http://citrus/ns2' xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance'>"
                + "<element credentials='username:password' attributeB='attribute-value'>"
                + "<sub-element>text-value</sub-element>"
                + "</element>"
                + "</root>").build();

        XmlMessageValidationContext validationContext = new XmlMessageValidationContext();

        validationContext.setControlMessage(controlMessage);
        DomXmlMessageValidator validator = new DomXmlMessageValidator();
        validator.validateMessagePayload(message, validationContext, context);
    }

    @Test(expectedExceptions = {IllegalArgumentException.class})
    public void testNamespaceQualifiedAttributeValueFails() {
        Message<?> message = MessageBuilder.withPayload("<root xmlns='http://citrus/default' xmlns:ns1='http://citrus/ns1' xmlns:ns2='http://citrus/ns2' xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance'>"
                + "<element xsi:type='ns1:attribute-value' attributeB='attribute-value'>"
                + "<sub-element xsi:type='ns2:AType'>text-value</sub-element>"
                + "</element>"
                + "</root>").build();

        Message<?> controlMessage = MessageBuilder.withPayload("<root xmlns='http://citrus/default' xmlns:ns1='http://citrus/ns1' xmlns:ns2='http://citrus/ns2' xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance'>"
                + "<element xsi:type='ns1:wrong-value' attributeB='attribute-value'>"
                + "<sub-element xsi:type='ns2:AType'>text-value</sub-element>"
                + "</element>"
                + "</root>").build();

        XmlMessageValidationContext validationContext = new XmlMessageValidationContext();

        validationContext.setControlMessage(controlMessage);
        DomXmlMessageValidator validator = new DomXmlMessageValidator();
        validator.validateMessagePayload(message, validationContext, context);
    }

    @Test(expectedExceptions = {IllegalArgumentException.class})
    public void testNamespaceQualifiedAttributeValueUriMismatch() {
        Message<?> message = MessageBuilder.withPayload("<root xmlns='http://citrus/default' xmlns:ns1='http://citrus/ns1' xmlns:ns2='http://citrus/ns2' xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance'>"
                + "<element xsi:type='ns1:attribute-value' attributeB='attribute-value'>"
                + "<sub-element xsi:type='ns2:AType'>text-value</sub-element>"
                + "</element>"
                + "</root>").build();

        Message<?> controlMessage = MessageBuilder.withPayload("<root xmlns='http://citrus/default' xmlns:cit='http://citrus/cit' xmlns:ns2='http://citrus/ns2' xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance'>"
                + "<element xsi:type='cit:attribute-value' attributeB='attribute-value'>"
                + "<sub-element xsi:type='ns2:AType'>text-value</sub-element>"
                + "</element>"
                + "</root>").build();

        XmlMessageValidationContext validationContext = new XmlMessageValidationContext();

        validationContext.setControlMessage(controlMessage);
        DomXmlMessageValidator validator = new DomXmlMessageValidator();
        validator.validateMessagePayload(message, validationContext, context);
    }

    @Test(expectedExceptions = {IllegalArgumentException.class})
    public void testNamespaceQualifiedAttributeMissingPrefix() {
        Message<?> message = MessageBuilder.withPayload("<root xmlns='http://citrus/default' xmlns:ns1='http://citrus/ns1' xmlns:ns2='http://citrus/ns2' xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance'>"
                + "<element xsi:type='attribute-value' attributeB='attribute-value'>"
                + "<sub-element xsi:type='ns2:AType'>text-value</sub-element>"
                + "</element>"
                + "</root>").build();

        Message<?> controlMessage = MessageBuilder.withPayload("<root xmlns='http://citrus/default' xmlns:ns1='http://citrus/cit' xmlns:ns2='http://citrus/ns2' xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance'>"
                + "<element xsi:type='ns1:attribute-value' attributeB='attribute-value'>"
                + "<sub-element xsi:type='ns2:AType'>text-value</sub-element>"
                + "</element>"
                + "</root>").build();

        XmlMessageValidationContext validationContext = new XmlMessageValidationContext();

        validationContext.setControlMessage(controlMessage);
        DomXmlMessageValidator validator = new DomXmlMessageValidator();
        validator.validateMessagePayload(message, validationContext, context);
    }

    @Test(expectedExceptions = {ValidationException.class})
    public void testNamespaceQualifiedAttributeValueMissingDeclaration() {
        Message<?> message = MessageBuilder.withPayload("<root xmlns='http://citrus/default' xmlns:ns1='http://citrus/ns1' xmlns:ns2='http://citrus/ns2' xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance'>"
                + "<element xsi:type='ns1:attribute-value' attributeB='attribute-value'>"
                + "<sub-element xsi:type='ns2:AType'>text-value</sub-element>"
                + "</element>"
                + "</root>").build();

        Message<?> controlMessage = MessageBuilder.withPayload("<root xmlns='http://citrus/default' xmlns:ns2='http://citrus/ns2' xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance'>"
                + "<element xsi:type='cit:attribute-value' attributeB='attribute-value'>"
                + "<sub-element xsi:type='ns2:AType'>text-value</sub-element>"
                + "</element>"
                + "</root>").build();

        XmlMessageValidationContext validationContext = new XmlMessageValidationContext();

        validationContext.setControlMessage(controlMessage);
        DomXmlMessageValidator validator = new DomXmlMessageValidator();
        validator.validateMessagePayload(message, validationContext, context);
    }

}
