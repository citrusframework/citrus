package com.consol.citrus.validation;

import java.util.HashMap;
import java.util.Map;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.integration.core.Message;
import org.springframework.integration.message.MessageBuilder;
import org.testng.annotations.Test;

import com.consol.citrus.AbstractBaseTest;
import com.consol.citrus.exceptions.ValidationException;

public class DefaultXMLMessageValidatorTest extends AbstractBaseTest {
    @Test
    public void validateXMLSchema() {
        Message message = MessageBuilder.withPayload("<message xmlns='http://testsuite'>"
                        + "<correlationId>Kx1R123456789</correlationId>"
                        + "<bookingId>Bx1G987654321</bookingId>"
                        + "<test>Hello TestFramework</test>"
                    + "</message>").build();
        
        Resource schemaResource = new ClassPathResource("com/consol/citrus/validation/test.xsd");
        
        DefaultXMLMessageValidator validator = new DefaultXMLMessageValidator();
        validator.validateXMLSchema(schemaResource, message);
    }
    
    @Test(expectedExceptions = {ValidationException.class})
    public void validateXMLSchemaError() {
        Message message = MessageBuilder.withPayload("<message xmlns='http://testsuite'>"
                        + "<correlationId>Kx1R123456789</correlationId>"
                        + "<bookingId>Bx1G987654321</bookingId>"
                        + "<test>Hello TestFramework</test>"
                        + "<wrongElement>totally wrong</wrongElement>"
                    + "</message>").build();
        
        Resource schemaResource = new ClassPathResource("com/consol/citrus/validation/test.xsd");
        
        DefaultXMLMessageValidator validator = new DefaultXMLMessageValidator();
        validator.validateXMLSchema(schemaResource, message);
    }
    
    @Test
    public void testExpectDefaultNamespace() {
        Message message = MessageBuilder.withPayload("<root xmlns='http://testsuite'>"
                        + "<element attributeA='attribute-value' attributeB='attribute-value'>"
                        + "<sub-element attribute='A'>text-value</sub-element>"
                        + "</element>" 
                    + "</root>").build();
        
        Map<String, String> expectedNamespaces = new HashMap<String, String>();
        expectedNamespaces.put("xmlns", "http://testsuite");
        
        DefaultXMLMessageValidator validator = new DefaultXMLMessageValidator();
        validator.validateNamespaces(expectedNamespaces, message);
    }
    
    @Test
    public void testExpectNamespace() {
        Message message = MessageBuilder.withPayload("<ns1:root xmlns:ns1='http://testsuite/ns1'>"
                        + "<ns1:element attributeA='attribute-value' attributeB='attribute-value'>"
                        + "<ns1:sub-element attribute='A'>text-value</ns1:sub-element>"
                        + "</ns1:element>" 
                    + "</ns1:root>").build();
        
        Map<String, String> expectedNamespaces = new HashMap<String, String>();
        expectedNamespaces.put("xmlns:ns1", "http://testsuite/ns1");
        
        DefaultXMLMessageValidator validator = new DefaultXMLMessageValidator();
        validator.validateNamespaces(expectedNamespaces, message);
    }
    
    @Test
    public void testExpectMixedNamespaces() {
        Message message = MessageBuilder.withPayload("<root xmlns='http://testsuite/default' xmlns:ns1='http://testsuite/ns1'>"
                        + "<element attributeA='attribute-value' attributeB='attribute-value'>"
                        + "<sub-element attribute='A'>text-value</sub-element>"
                        + "</element>" 
                    + "</root>").build();
        
        Map<String, String> expectedNamespaces = new HashMap<String, String>();
        expectedNamespaces.put("xmlns", "http://testsuite/default");
        expectedNamespaces.put("xmlns:ns1", "http://testsuite/ns1");
        
        DefaultXMLMessageValidator validator = new DefaultXMLMessageValidator();
        validator.validateNamespaces(expectedNamespaces, message);
    }
    
    @Test
    public void testExpectMultipleNamespaces() {
        Message message = MessageBuilder.withPayload("<root xmlns='http://testsuite/default' xmlns:ns1='http://testsuite/ns1' xmlns:ns2='http://testsuite/ns2'>"
                        + "<element attributeA='attribute-value' attributeB='attribute-value'>"
                        + "<sub-element attribute='A'>text-value</sub-element>"
                        + "</element>" 
                    + "</root>").build();
        
        Map<String, String> expectedNamespaces = new HashMap<String, String>();
        expectedNamespaces.put("xmlns", "http://testsuite/default");
        expectedNamespaces.put("xmlns:ns1", "http://testsuite/ns1");
        expectedNamespaces.put("xmlns:ns2", "http://testsuite/ns2");
        
        DefaultXMLMessageValidator validator = new DefaultXMLMessageValidator();
        validator.validateNamespaces(expectedNamespaces, message);
    }
    
    @Test(expectedExceptions = {ValidationException.class})
    public void testExpectDefaultNamespaceError() {
        Message message = MessageBuilder.withPayload("<root xmlns='http://testsuite'>"
                        + "<element attributeA='attribute-value' attributeB='attribute-value'>"
                        + "<sub-element attribute='A'>text-value</sub-element>"
                        + "</element>" 
                    + "</root>").build();
        
        Map<String, String> expectedNamespaces = new HashMap<String, String>();
        expectedNamespaces.put("xmlns", "http://testsuite/wrong");
        
        DefaultXMLMessageValidator validator = new DefaultXMLMessageValidator();
        validator.validateNamespaces(expectedNamespaces, message);
    }
    
    @Test(expectedExceptions = {ValidationException.class})
    public void testExpectNamespaceError() {
        Message message = MessageBuilder.withPayload("<ns1:root xmlns:ns1='http://testsuite/ns1'>"
                        + "<ns1:element attributeA='attribute-value' attributeB='attribute-value'>"
                        + "<ns1:sub-element attribute='A'>text-value</ns1:sub-element>"
                        + "</ns1:element>" 
                    + "</ns1:root>").build();
        
        Map<String, String> expectedNamespaces = new HashMap<String, String>();
        expectedNamespaces.put("xmlns:ns1", "http://testsuite/ns1/wrong");
        
        DefaultXMLMessageValidator validator = new DefaultXMLMessageValidator();
        validator.validateNamespaces(expectedNamespaces, message);
    }
    
    @Test(expectedExceptions = {ValidationException.class})
    public void testExpectMixedNamespacesError() {
        Message message = MessageBuilder.withPayload("<root xmlns='http://testsuite/default' xmlns:ns1='http://testsuite/ns1'>"
                        + "<element attributeA='attribute-value' attributeB='attribute-value'>"
                        + "<sub-element attribute='A'>text-value</sub-element>"
                        + "</element>" 
                    + "</root>").build();
        
        Map<String, String> expectedNamespaces = new HashMap<String, String>();
        expectedNamespaces.put("xmlns", "http://testsuite/default/wrong");
        expectedNamespaces.put("xmlns:ns1", "http://testsuite/ns1");
        
        DefaultXMLMessageValidator validator = new DefaultXMLMessageValidator();
        validator.validateNamespaces(expectedNamespaces, message);
    }
    
    @Test(expectedExceptions = {ValidationException.class})
    public void testExpectMultipleNamespacesError() {
        Message message = MessageBuilder.withPayload("<root xmlns='http://testsuite/default' xmlns:ns1='http://testsuite/ns1' xmlns:ns2='http://testsuite/ns2'>"
                        + "<element attributeA='attribute-value' attributeB='attribute-value'>"
                        + "<sub-element attribute='A'>text-value</sub-element>"
                        + "</element>" 
                    + "</root>").build();
        
        Map<String, String> expectedNamespaces = new HashMap<String, String>();
        expectedNamespaces.put("xmlns", "http://testsuite/default");
        expectedNamespaces.put("xmlns:ns1", "http://testsuite/ns1/wrong");
        expectedNamespaces.put("xmlns:ns2", "http://testsuite/ns2");
     
        DefaultXMLMessageValidator validator = new DefaultXMLMessageValidator();
        validator.validateNamespaces(expectedNamespaces, message);
    }
    
    @Test(expectedExceptions = {ValidationException.class})
    public void testExpectWrongNamespacePrefix() {
        Message message = MessageBuilder.withPayload("<root xmlns='http://testsuite/default' xmlns:ns1='http://testsuite/ns1' xmlns:ns2='http://testsuite/ns2'>"
                        + "<element attributeA='attribute-value' attributeB='attribute-value'>"
                        + "<sub-element attribute='A'>text-value</sub-element>"
                        + "</element>" 
                    + "</root>").build();
        
        Map<String, String> expectedNamespaces = new HashMap<String, String>();
        expectedNamespaces.put("xmlns", "http://testsuite/default");
        expectedNamespaces.put("xmlns:nswrong", "http://testsuite/ns1");
        expectedNamespaces.put("xmlns:ns2", "http://testsuite/ns2");
        
        DefaultXMLMessageValidator validator = new DefaultXMLMessageValidator();
        validator.validateNamespaces(expectedNamespaces, message);
    }
    
    @Test(expectedExceptions = {ValidationException.class})
    public void testExpectDefaultNamespaceButNamespace() {
        Message message = MessageBuilder.withPayload("<ns0:root xmlns:ns0='http://testsuite/default' xmlns:ns1='http://testsuite/ns1' xmlns:ns2='http://testsuite/ns2'>"
                        + "<ns0:element attributeA='attribute-value' attributeB='attribute-value'>"
                        + "<ns0:sub-element attribute='A'>text-value</ns0:sub-element>"
                        + "</ns0:element>" 
                    + "</ns0:root>").build();
        
        Map<String, String> expectedNamespaces = new HashMap<String, String>();
        expectedNamespaces.put("xmlns", "http://testsuite/default");
        expectedNamespaces.put("xmlns:ns1", "http://testsuite/ns1");
        expectedNamespaces.put("xmlns:ns2", "http://testsuite/ns2");
        
        DefaultXMLMessageValidator validator = new DefaultXMLMessageValidator();
        validator.validateNamespaces(expectedNamespaces, message);
    }
    
    @Test(expectedExceptions = {ValidationException.class})
    public void testExpectNamespaceButDefaultNamespace() {
        Message message = MessageBuilder.withPayload("<root xmlns='http://testsuite/default' xmlns:ns1='http://testsuite/ns1' xmlns:ns2='http://testsuite/ns2'>"
                        + "<element attributeA='attribute-value' attributeB='attribute-value'>"
                        + "<sub-element attribute='A'>text-value</sub-element>"
                        + "</element>" 
                    + "</root>").build();
        
        Map<String, String> expectedNamespaces = new HashMap<String, String>();
        expectedNamespaces.put("xmlns:ns0", "http://testsuite/default");
        expectedNamespaces.put("xmlns:ns1", "http://testsuite/ns1");
        expectedNamespaces.put("xmlns:ns2", "http://testsuite/ns2");
        
        DefaultXMLMessageValidator validator = new DefaultXMLMessageValidator();
        validator.validateNamespaces(expectedNamespaces, message);
    }
    
    @Test(expectedExceptions = {ValidationException.class})
    public void testExpectAdditionalNamespace() {
        Message message = MessageBuilder.withPayload("<root xmlns='http://testsuite/default' xmlns:ns1='http://testsuite/ns1' xmlns:ns2='http://testsuite/ns2'>"
                        + "<element attributeA='attribute-value' attributeB='attribute-value'>"
                        + "<sub-element attribute='A'>text-value</sub-element>"
                        + "</element>" 
                    + "</root>").build();
        
        Map<String, String> expectedNamespaces = new HashMap<String, String>();
        expectedNamespaces.put("xmlns", "http://testsuite/default");
        expectedNamespaces.put("xmlns:ns1", "http://testsuite/ns1");
        expectedNamespaces.put("xmlns:ns2", "http://testsuite/ns2");
        expectedNamespaces.put("xmlns:ns4", "http://testsuite/ns4");
        
        DefaultXMLMessageValidator validator = new DefaultXMLMessageValidator();
        validator.validateNamespaces(expectedNamespaces, message);
    }
    
    @Test(expectedExceptions = {ValidationException.class})
    public void testExpectNamespaceButNamespaceMissing() {
        Message message = MessageBuilder.withPayload("<root xmlns='http://testsuite/default' xmlns:ns1='http://testsuite/ns1' xmlns:ns2='http://testsuite/ns2' xmlns:ns4='http://testsuite/ns4'>"
                        + "<element attributeA='attribute-value' attributeB='attribute-value'>"
                        + "<sub-element attribute='A'>text-value</sub-element>"
                        + "</element>" 
                    + "</root>").build();
        
        Map<String, String> expectedNamespaces = new HashMap<String, String>();
        expectedNamespaces.put("xmlns", "http://testsuite/default");
        expectedNamespaces.put("xmlns:ns1", "http://testsuite/ns1");
        expectedNamespaces.put("xmlns:ns2", "http://testsuite/ns2");
        
        DefaultXMLMessageValidator validator = new DefaultXMLMessageValidator();
        validator.validateNamespaces(expectedNamespaces, message);
    }
}
