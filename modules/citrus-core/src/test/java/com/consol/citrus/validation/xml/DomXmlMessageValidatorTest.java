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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.integration.core.Message;
import org.springframework.integration.message.MessageBuilder;
import org.springframework.xml.xsd.SimpleXsdSchema;
import org.testng.annotations.Test;
import org.xml.sax.SAXException;

import com.consol.citrus.exceptions.ValidationException;
import com.consol.citrus.testng.AbstractBaseTest;
import com.consol.citrus.validation.xml.DomXmlMessageValidator;
import com.consol.citrus.xml.XsdSchemaRepository;

/**
 * @author Christoph Deppisch
 */
public class DomXmlMessageValidatorTest extends AbstractBaseTest {
    @Test
    public void validateXMLSchema() throws SAXException, IOException, ParserConfigurationException {
        Message<?> message = MessageBuilder.withPayload("<message xmlns='http://testsuite'>"
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
        
        validator.setSchemaRepository(schemaRepository);
        
        validator.validateXMLSchema(message);
    }
    
    @Test(expectedExceptions = {ValidationException.class})
    public void validateXMLSchemaError() throws SAXException, IOException, ParserConfigurationException {
        Message<?> message = MessageBuilder.withPayload("<message xmlns='http://testsuite'>"
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
        
        validator.setSchemaRepository(schemaRepository);
        
        validator.validateXMLSchema(message);
    }
    
    @Test
    public void testExpectDefaultNamespace() {
        Message<?> message = MessageBuilder.withPayload("<root xmlns='http://testsuite'>"
                        + "<element attributeA='attribute-value' attributeB='attribute-value'>"
                        + "<sub-element attribute='A'>text-value</sub-element>"
                        + "</element>" 
                    + "</root>").build();
        
        Map<String, String> expectedNamespaces = new HashMap<String, String>();
        expectedNamespaces.put("", "http://testsuite");
        
        DomXmlMessageValidator validator = new DomXmlMessageValidator();
        validator.validateNamespaces(expectedNamespaces, message);
    }
    
    @Test
    public void testExpectNamespace() {
    	Message<?> message = MessageBuilder.withPayload("<ns1:root xmlns:ns1='http://testsuite/ns1'>"
                        + "<ns1:element attributeA='attribute-value' attributeB='attribute-value'>"
                        + "<ns1:sub-element attribute='A'>text-value</ns1:sub-element>"
                        + "</ns1:element>" 
                    + "</ns1:root>").build();
        
        Map<String, String> expectedNamespaces = new HashMap<String, String>();
        expectedNamespaces.put("ns1", "http://testsuite/ns1");
        
        DomXmlMessageValidator validator = new DomXmlMessageValidator();
        validator.validateNamespaces(expectedNamespaces, message);
    }
    
    @Test
    public void testExpectMixedNamespaces() {
    	Message<?> message = MessageBuilder.withPayload("<root xmlns='http://testsuite/default' xmlns:ns1='http://testsuite/ns1'>"
                        + "<element attributeA='attribute-value' attributeB='attribute-value'>"
                        + "<sub-element attribute='A'>text-value</sub-element>"
                        + "</element>" 
                    + "</root>").build();
        
        Map<String, String> expectedNamespaces = new HashMap<String, String>();
        expectedNamespaces.put("", "http://testsuite/default");
        expectedNamespaces.put("ns1", "http://testsuite/ns1");
        
        DomXmlMessageValidator validator = new DomXmlMessageValidator();
        validator.validateNamespaces(expectedNamespaces, message);
    }
    
    @Test
    public void testExpectMultipleNamespaces() {
    	Message<?> message = MessageBuilder.withPayload("<root xmlns='http://testsuite/default' xmlns:ns1='http://testsuite/ns1' xmlns:ns2='http://testsuite/ns2'>"
                        + "<element attributeA='attribute-value' attributeB='attribute-value'>"
                        + "<sub-element attribute='A'>text-value</sub-element>"
                        + "</element>" 
                    + "</root>").build();
        
        Map<String, String> expectedNamespaces = new HashMap<String, String>();
        expectedNamespaces.put("", "http://testsuite/default");
        expectedNamespaces.put("ns1", "http://testsuite/ns1");
        expectedNamespaces.put("ns2", "http://testsuite/ns2");
        
        DomXmlMessageValidator validator = new DomXmlMessageValidator();
        validator.validateNamespaces(expectedNamespaces, message);
    }
    
    @Test(expectedExceptions = {ValidationException.class})
    public void testExpectDefaultNamespaceError() {
    	Message<?> message = MessageBuilder.withPayload("<root xmlns='http://testsuite'>"
                        + "<element attributeA='attribute-value' attributeB='attribute-value'>"
                        + "<sub-element attribute='A'>text-value</sub-element>"
                        + "</element>" 
                    + "</root>").build();
        
        Map<String, String> expectedNamespaces = new HashMap<String, String>();
        expectedNamespaces.put("", "http://testsuite/wrong");
        
        DomXmlMessageValidator validator = new DomXmlMessageValidator();
        validator.validateNamespaces(expectedNamespaces, message);
    }
    
    @Test(expectedExceptions = {ValidationException.class})
    public void testExpectNamespaceError() {
    	Message<?> message = MessageBuilder.withPayload("<ns1:root xmlns:ns1='http://testsuite/ns1'>"
                        + "<ns1:element attributeA='attribute-value' attributeB='attribute-value'>"
                        + "<ns1:sub-element attribute='A'>text-value</ns1:sub-element>"
                        + "</ns1:element>" 
                    + "</ns1:root>").build();
        
        Map<String, String> expectedNamespaces = new HashMap<String, String>();
        expectedNamespaces.put("ns1", "http://testsuite/ns1/wrong");
        
        DomXmlMessageValidator validator = new DomXmlMessageValidator();
        validator.validateNamespaces(expectedNamespaces, message);
    }
    
    @Test(expectedExceptions = {ValidationException.class})
    public void testExpectMixedNamespacesError() {
    	Message<?> message = MessageBuilder.withPayload("<root xmlns='http://testsuite/default' xmlns:ns1='http://testsuite/ns1'>"
                        + "<element attributeA='attribute-value' attributeB='attribute-value'>"
                        + "<sub-element attribute='A'>text-value</sub-element>"
                        + "</element>" 
                    + "</root>").build();
        
        Map<String, String> expectedNamespaces = new HashMap<String, String>();
        expectedNamespaces.put("", "http://testsuite/default/wrong");
        expectedNamespaces.put("ns1", "http://testsuite/ns1");
        
        DomXmlMessageValidator validator = new DomXmlMessageValidator();
        validator.validateNamespaces(expectedNamespaces, message);
    }
    
    @Test(expectedExceptions = {ValidationException.class})
    public void testExpectMultipleNamespacesError() {
    	Message<?> message = MessageBuilder.withPayload("<root xmlns='http://testsuite/default' xmlns:ns1='http://testsuite/ns1' xmlns:ns2='http://testsuite/ns2'>"
                        + "<element attributeA='attribute-value' attributeB='attribute-value'>"
                        + "<sub-element attribute='A'>text-value</sub-element>"
                        + "</element>" 
                    + "</root>").build();
        
        Map<String, String> expectedNamespaces = new HashMap<String, String>();
        expectedNamespaces.put("", "http://testsuite/default");
        expectedNamespaces.put("ns1", "http://testsuite/ns1/wrong");
        expectedNamespaces.put("ns2", "http://testsuite/ns2");
     
        DomXmlMessageValidator validator = new DomXmlMessageValidator();
        validator.validateNamespaces(expectedNamespaces, message);
    }
    
    @Test(expectedExceptions = {ValidationException.class})
    public void testExpectWrongNamespacePrefix() {
    	Message<?> message = MessageBuilder.withPayload("<root xmlns='http://testsuite/default' xmlns:ns1='http://testsuite/ns1' xmlns:ns2='http://testsuite/ns2'>"
                        + "<element attributeA='attribute-value' attributeB='attribute-value'>"
                        + "<sub-element attribute='A'>text-value</sub-element>"
                        + "</element>" 
                    + "</root>").build();
        
        Map<String, String> expectedNamespaces = new HashMap<String, String>();
        expectedNamespaces.put("", "http://testsuite/default");
        expectedNamespaces.put("nswrong", "http://testsuite/ns1");
        expectedNamespaces.put("ns2", "http://testsuite/ns2");
        
        DomXmlMessageValidator validator = new DomXmlMessageValidator();
        validator.validateNamespaces(expectedNamespaces, message);
    }
    
    @Test(expectedExceptions = {ValidationException.class})
    public void testExpectDefaultNamespaceButNamespace() {
    	Message<?> message = MessageBuilder.withPayload("<ns0:root xmlns:ns0='http://testsuite/default' xmlns:ns1='http://testsuite/ns1' xmlns:ns2='http://testsuite/ns2'>"
                        + "<ns0:element attributeA='attribute-value' attributeB='attribute-value'>"
                        + "<ns0:sub-element attribute='A'>text-value</ns0:sub-element>"
                        + "</ns0:element>" 
                    + "</ns0:root>").build();
        
        Map<String, String> expectedNamespaces = new HashMap<String, String>();
        expectedNamespaces.put("", "http://testsuite/default");
        expectedNamespaces.put("ns1", "http://testsuite/ns1");
        expectedNamespaces.put("ns2", "http://testsuite/ns2");
        
        DomXmlMessageValidator validator = new DomXmlMessageValidator();
        validator.validateNamespaces(expectedNamespaces, message);
    }
    
    @Test(expectedExceptions = {ValidationException.class})
    public void testExpectNamespaceButDefaultNamespace() {
    	Message<?> message = MessageBuilder.withPayload("<root xmlns='http://testsuite/default' xmlns:ns1='http://testsuite/ns1' xmlns:ns2='http://testsuite/ns2'>"
                        + "<element attributeA='attribute-value' attributeB='attribute-value'>"
                        + "<sub-element attribute='A'>text-value</sub-element>"
                        + "</element>" 
                    + "</root>").build();
        
        Map<String, String> expectedNamespaces = new HashMap<String, String>();
        expectedNamespaces.put("ns0", "http://testsuite/default");
        expectedNamespaces.put("ns1", "http://testsuite/ns1");
        expectedNamespaces.put("ns2", "http://testsuite/ns2");
        
        DomXmlMessageValidator validator = new DomXmlMessageValidator();
        validator.validateNamespaces(expectedNamespaces, message);
    }
    
    @Test(expectedExceptions = {ValidationException.class})
    public void testExpectAdditionalNamespace() {
    	Message<?> message = MessageBuilder.withPayload("<root xmlns='http://testsuite/default' xmlns:ns1='http://testsuite/ns1' xmlns:ns2='http://testsuite/ns2'>"
                        + "<element attributeA='attribute-value' attributeB='attribute-value'>"
                        + "<sub-element attribute='A'>text-value</sub-element>"
                        + "</element>" 
                    + "</root>").build();
        
        Map<String, String> expectedNamespaces = new HashMap<String, String>();
        expectedNamespaces.put("", "http://testsuite/default");
        expectedNamespaces.put("ns1", "http://testsuite/ns1");
        expectedNamespaces.put("ns2", "http://testsuite/ns2");
        expectedNamespaces.put("ns4", "http://testsuite/ns4");
        
        DomXmlMessageValidator validator = new DomXmlMessageValidator();
        validator.validateNamespaces(expectedNamespaces, message);
    }
    
    @Test(expectedExceptions = {ValidationException.class})
    public void testExpectNamespaceButNamespaceMissing() {
    	Message<?> message = MessageBuilder.withPayload("<root xmlns='http://testsuite/default' xmlns:ns1='http://testsuite/ns1' xmlns:ns2='http://testsuite/ns2' xmlns:ns4='http://testsuite/ns4'>"
                        + "<element attributeA='attribute-value' attributeB='attribute-value'>"
                        + "<sub-element attribute='A'>text-value</sub-element>"
                        + "</element>" 
                    + "</root>").build();
        
        Map<String, String> expectedNamespaces = new HashMap<String, String>();
        expectedNamespaces.put("", "http://testsuite/default");
        expectedNamespaces.put("ns1", "http://testsuite/ns1");
        expectedNamespaces.put("ns2", "http://testsuite/ns2");
        
        DomXmlMessageValidator validator = new DomXmlMessageValidator();
        validator.validateNamespaces(expectedNamespaces, message);
    }
}
