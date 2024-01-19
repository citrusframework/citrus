/*
 * Copyright 2006-2024 the original author or authors.
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

package org.citrusframework.validation.xml;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.xml.parsers.ParserConfigurationException;

import org.citrusframework.UnitTestSupport;
import org.citrusframework.context.TestContextFactory;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.exceptions.ValidationException;
import org.citrusframework.message.DefaultMessage;
import org.citrusframework.message.Message;
import org.citrusframework.message.MessageType;
import org.citrusframework.validation.SchemaValidator;
import org.citrusframework.validation.context.HeaderValidationContext;
import org.citrusframework.validation.context.SchemaValidationContext;
import org.citrusframework.validation.context.ValidationContext;
import org.citrusframework.validation.json.JsonMessageValidationContext;
import org.citrusframework.validation.script.ScriptValidationContext;
import org.citrusframework.validation.xml.schema.XmlSchemaValidation;
import org.citrusframework.xml.XsdSchemaRepository;
import org.citrusframework.xml.schema.XsdSchemaCollection;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.xml.xsd.SimpleXsdSchema;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.xml.sax.SAXException;

import static org.mockito.Mockito.mock;
import static org.springframework.test.util.ReflectionTestUtils.getField;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;

/**
 * @author Christoph Deppisch
 */
public class DomXmlMessageValidatorTest extends UnitTestSupport {

    private final DomXmlMessageValidator validator = new DomXmlMessageValidator();

    private final XsdSchemaRepository schemaRepository = new XsdSchemaRepository();
    private final XsdSchemaRepository testSchemaRepository1 = new XsdSchemaRepository();
    private final XsdSchemaRepository testSchemaRepository2 = new XsdSchemaRepository();

    private final SimpleXsdSchema testSchema = new SimpleXsdSchema();
    private final SimpleXsdSchema testSchema1 = new SimpleXsdSchema();
    private final SimpleXsdSchema testSchema2 = new SimpleXsdSchema();
    private final SimpleXsdSchema testSchema3 = new SimpleXsdSchema();

    @BeforeClass
    public void setupMocks() throws Exception {
        testSchema.setXsd(new ClassPathResource("org/citrusframework/validation/test.xsd"));
        testSchema.afterPropertiesSet();
        testSchema1.setXsd(new ClassPathResource("org/citrusframework/validation/test.xsd"));
        testSchema1.afterPropertiesSet();
        testSchema2.setXsd(new ClassPathResource("org/citrusframework/validation/test.xsd"));
        testSchema2.afterPropertiesSet();
        testSchema3.setXsd(new ClassPathResource("org/citrusframework/validation/test.xsd"));
        testSchema3.afterPropertiesSet();

        schemaRepository.getSchemas().add(testSchema);
        testSchemaRepository1.getSchemas().add(testSchema1);
        testSchemaRepository2.getSchemas().add(testSchema1);
    }

    @Override
    protected TestContextFactory createTestContextFactory() {
        TestContextFactory factory = super.createTestContextFactory();
        factory.getMessageValidatorRegistry().addMessageValidator("defaultXmlMessageValidator", validator);

        factory.getReferenceResolver().bind("schemaRepository", schemaRepository);
        factory.getReferenceResolver().bind("testSchemaRepository1", testSchemaRepository1);
        factory.getReferenceResolver().bind("testSchemaRepository2", testSchemaRepository2);
        factory.getReferenceResolver().bind("testSchema", testSchema);
        factory.getReferenceResolver().bind("testSchema1", testSchema1);
        factory.getReferenceResolver().bind("testSchema2", testSchema2);
        factory.getReferenceResolver().bind("testSchema3", testSchema3);
        return factory;
    }

    @Test
    public void constructorWithoutArguments() {
        Object xmlSchemaValidation = getField(validator, DomXmlMessageValidator.class, "schemaValidator");

        assertTrue(xmlSchemaValidation instanceof XmlSchemaValidation);
        assertNotNull(xmlSchemaValidation);
    }

    @Test
    public void allArgsConstructor() {
        XmlSchemaValidation xmlSchemaValidationMock = mock(XmlSchemaValidation.class);
        DomXmlMessageValidator domXmlMessageValidator = new DomXmlMessageValidator(xmlSchemaValidationMock);

        Object xmlSchemaValidation = getField(domXmlMessageValidator, DomXmlMessageValidator.class, "schemaValidator");

        assertTrue(xmlSchemaValidation instanceof XmlSchemaValidation);
        assertNotNull(xmlSchemaValidation);
        assertEquals(xmlSchemaValidationMock, xmlSchemaValidation);
    }

    @Test
    public void validateXMLSchema() throws SAXException, IOException, ParserConfigurationException {
        Message message = new DefaultMessage("<message xmlns='http://citrusframework.org/test'>"
                        + "<correlationId>Kx1R123456789</correlationId>"
                        + "<bookingId>Bx1G987654321</bookingId>"
                        + "<test>Hello TestFramework</test>"
                    + "</message>");

        DomXmlMessageValidator validator = new DomXmlMessageValidator();

        XsdSchemaRepository schemaRepository = new XsdSchemaRepository();
        Resource schemaResource = new ClassPathResource("org/citrusframework/validation/test.xsd");
        SimpleXsdSchema schema = new SimpleXsdSchema(schemaResource);
        schema.afterPropertiesSet();

        schemaRepository.getSchemas().add(schema);

        context.getReferenceResolver().bind("schemaRepository", schemaRepository);

        validator.validateXMLSchema(message, context, new XmlMessageValidationContext());
    }

    @Test
    public void validateXMLSchemaNested() throws Exception {
        Message message = new DefaultMessage("<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\">" +
                "<SOAP-ENV:Header/>" +
                "<SOAP-ENV:Body>" +
                "<message xmlns=\"http://citrusframework.org/test\">"
                + "<correlationId>Kx1R123456789</correlationId>"
                + "<bookingId>Bx1G987654321</bookingId>"
                + "<test>Hello TestFramework</test>"
                + "</message>" +
                "</SOAP-ENV:Body>" +
                "</SOAP-ENV:Envelope>");

        DomXmlMessageValidator validator = new DomXmlMessageValidator();

        XsdSchemaRepository schemaRepository = new XsdSchemaRepository();
        Resource schemaResource = new ClassPathResource("org/citrusframework/validation/test.xsd");
        SimpleXsdSchema schema = new SimpleXsdSchema(schemaResource);
        schema.afterPropertiesSet();

        schemaRepository.getSchemas().add(schema);
        schemaRepository.getLocations().add("schemas/soap-1.1.xsd");
        schemaRepository.initialize();

        context.getReferenceResolver().bind("schemaRepository", schemaRepository);

        validator.validateXMLSchema(message, context, new XmlMessageValidationContext());
    }

    @Test
    public void validateXMLSchemaNestedWithNamespaceInRoot() throws Exception {
        Message message = new DefaultMessage("<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns=\"http://citrusframework.org/test\">" +
                "<SOAP-ENV:Header/>" +
                "<SOAP-ENV:Body>" +
                "<message>"
                + "<correlationId>Kx1R123456789</correlationId>"
                + "<bookingId>Bx1G987654321</bookingId>"
                + "<test>Hello TestFramework</test>"
                + "</message>" +
                "</SOAP-ENV:Body>" +
                "</SOAP-ENV:Envelope>");

        DomXmlMessageValidator validator = new DomXmlMessageValidator();

        XsdSchemaRepository schemaRepository = new XsdSchemaRepository();
        Resource schemaResource = new ClassPathResource("org/citrusframework/validation/test.xsd");
        SimpleXsdSchema schema = new SimpleXsdSchema(schemaResource);
        schema.afterPropertiesSet();

        schemaRepository.getSchemas().add(schema);
        schemaRepository.getLocations().add("schemas/soap-1.1.xsd");
        schemaRepository.initialize();

        context.getReferenceResolver().bind("schemaRepository", schemaRepository);

        validator.validateXMLSchema(message, context, new XmlMessageValidationContext());
    }

    @Test(expectedExceptions = ValidationException.class, expectedExceptionsMessageRegExp = ".*Invalid content was found starting with element '\\{\"http://citrusframework.org/test\":wrong\\}'.*")
    public void validateXMLSchemaNestedError() throws Exception {
        Message message = new DefaultMessage("<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\">" +
                "<SOAP-ENV:Header/>" +
                "<SOAP-ENV:Body>" +
                    "<message xmlns=\"http://citrusframework.org/test\">"
                        + "<correlationId>Kx1R123456789</correlationId>"
                        + "<wrong>Bx1G987654321</wrong>"
                        + "<test>Hello TestFramework</test>"
                    + "</message>" +
                "</SOAP-ENV:Body>" +
                "</SOAP-ENV:Envelope>");

        DomXmlMessageValidator validator = new DomXmlMessageValidator();

        XsdSchemaRepository schemaRepository = new XsdSchemaRepository();
        Resource schemaResource = new ClassPathResource("org/citrusframework/validation/test.xsd");
        SimpleXsdSchema schema = new SimpleXsdSchema(schemaResource);
        schema.afterPropertiesSet();

        schemaRepository.getSchemas().add(schema);
        schemaRepository.getLocations().add("schemas/soap-1.1.xsd");
        schemaRepository.initialize();

        context.getReferenceResolver().bind("schemaRepository", schemaRepository);

        validator.validateXMLSchema(message, context, new XmlMessageValidationContext());
    }

    @Test(expectedExceptions = ValidationException.class, expectedExceptionsMessageRegExp = ".*Invalid content was found starting with element '\\{\"http://citrusframework.org/test\":wrong\\}'.*")
    public void validateXMLSchemaNestedWithNamespaceInRootError() throws Exception {
        Message message = new DefaultMessage("<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns=\"http://citrusframework.org/test\">" +
                "<SOAP-ENV:Header/>" +
                "<SOAP-ENV:Body>" +
                "<message>"
                    + "<correlationId>Kx1R123456789</correlationId>"
                    + "<wrong>Bx1G987654321</wrong>"
                    + "<test>Hello TestFramework</test>"
                + "</message>" +
                "</SOAP-ENV:Body>" +
                "</SOAP-ENV:Envelope>");

        DomXmlMessageValidator validator = new DomXmlMessageValidator();

        XsdSchemaRepository schemaRepository = new XsdSchemaRepository();
        Resource schemaResource = new ClassPathResource("org/citrusframework/validation/test.xsd");
        SimpleXsdSchema schema = new SimpleXsdSchema(schemaResource);
        schema.afterPropertiesSet();

        schemaRepository.getSchemas().add(schema);
        schemaRepository.getLocations().add("schemas/soap-1.1.xsd");
        schemaRepository.initialize();

        context.getReferenceResolver().bind("schemaRepository", schemaRepository);

        validator.validateXMLSchema(message, context, new XmlMessageValidationContext());
    }

    @Test
    public void validateWithExplicitXMLSchema() throws SAXException, IOException, ParserConfigurationException {
        Message message = new DefaultMessage("<message xmlns='http://citrusframework.org/test'>"
                        + "<correlationId>Kx1R123456789</correlationId>"
                        + "<bookingId>Bx1G987654321</bookingId>"
                        + "<test>Hello TestFramework</test>"
                    + "</message>");

        XmlMessageValidationContext validationContext = new XmlMessageValidationContext.Builder()
                .schema("testSchema2") // defined as bean in application context
                .build();
        validator.validateXMLSchema(message, context, validationContext);
    }

    @Test
    public void validateWithExplicitSpringSchemaRepository() throws SAXException, IOException, ParserConfigurationException {
        Message message = new DefaultMessage("<message xmlns='http://citrusframework.org/test'>"
                        + "<correlationId>Kx1R123456789</correlationId>"
                        + "<bookingId>Bx1G987654321</bookingId>"
                        + "<test>Hello TestFramework</test>"
                    + "</message>");

        XmlMessageValidationContext validationContext = new XmlMessageValidationContext.Builder()
                .schemaRepository("testSchemaRepository1") // defined as bean in application context
                .build();
        validator.validateXMLSchema(message, context, validationContext);
    }

    @Test
    public void validateWithExplicitCitrusSchemaRepository() throws SAXException, IOException, ParserConfigurationException {
        Message message = new DefaultMessage("<message xmlns='http://citrusframework.org/test'>"
                        + "<correlationId>Kx1R123456789</correlationId>"
                        + "<bookingId>Bx1G987654321</bookingId>"
                        + "<test>Hello TestFramework</test>"
                    + "</message>");

        XmlMessageValidationContext validationContext = new XmlMessageValidationContext.Builder()
                .schemaRepository("testSchemaRepository2") // defined as bean in application context
                .build();
        validator.validateXMLSchema(message, context, validationContext);
    }

    @Test
    public void validateWithDefaultSchemaRepository() throws SAXException, IOException, ParserConfigurationException {
        Message message = new DefaultMessage("<message xmlns='http://citrusframework.org/test'>"
                        + "<correlationId>Kx1R123456789</correlationId>"
                        + "<bookingId>Bx1G987654321</bookingId>"
                        + "<test>Hello TestFramework</test>"
                    + "</message>");

        validator.validateXMLSchema(message, context, new XmlMessageValidationContext());
    }

    @Test
    public void validateNoDefaultSchemaRepository() throws SAXException, IOException, ParserConfigurationException {
        Message message = new DefaultMessage("<message xmlns='http://citrusframework.org/test'>"
                        + "<correlationId>Kx1R123456789</correlationId>"
                        + "<bookingId>Bx1G987654321</bookingId>"
                        + "<test>Hello TestFramework</test>"
                    + "</message>");

        DomXmlMessageValidator validator = new DomXmlMessageValidator();

        XsdSchemaRepository schemaRepository = new XsdSchemaRepository();
        schemaRepository.setName("schemaRepository1");
        Resource schemaResource = new ClassPathResource("org/citrusframework/validation/test.xsd");
        SimpleXsdSchema schema = new SimpleXsdSchema(schemaResource);
        schema.afterPropertiesSet();

        schemaRepository.getSchemas().add(schema);

        context.getReferenceResolver().bind("schemaRepository", schemaRepository);

        XsdSchemaRepository schemaRepository2 = new XsdSchemaRepository();
        schemaRepository2.setName("schemaRepository2");
        Resource schemaResource2 = new ClassPathResource("org/citrusframework/validation/sample.xsd");
        SimpleXsdSchema schema2 = new SimpleXsdSchema(schemaResource2);
        schema2.afterPropertiesSet();

        schemaRepository2.getSchemas().add(schema2);

        context.getReferenceResolver().bind("schemaRepository2", schemaRepository2);

        validator.validateXMLSchema(message, context, new XmlMessageValidationContext());

        message = new DefaultMessage("<message xmlns='http://citrusframework.org/sample'>"
                + "<correlationId>Kx1R123456789</correlationId>"
                + "<bookingId>Bx1G987654321</bookingId>"
                + "<test>Hello TestFramework</test>"
                + "</message>");

        validator.validateXMLSchema(message, context, new XmlMessageValidationContext());
    }

    @Test
    public void validateNoMatchingSchemaRepository() throws SAXException, IOException, ParserConfigurationException {
        Message message = new DefaultMessage("<message xmlns='http://citrusframework.org/special'>"
                + "<correlationId>Kx1R123456789</correlationId>"
                + "<bookingId>Bx1G987654321</bookingId>"
                + "<test>Hello TestFramework</test>"
                + "</message>");

        DomXmlMessageValidator validator = new DomXmlMessageValidator();

        XsdSchemaRepository schemaRepository = new XsdSchemaRepository();
        schemaRepository.setName("schemaRepository1");
        Resource schemaResource = new ClassPathResource("org/citrusframework/validation/test.xsd");
        SimpleXsdSchema schema = new SimpleXsdSchema(schemaResource);
        schema.afterPropertiesSet();

        schemaRepository.getSchemas().add(schema);

        context.getReferenceResolver().bind("schemaRepository", schemaRepository);

        XsdSchemaRepository schemaRepository2 = new XsdSchemaRepository();
        schemaRepository2.setName("schemaRepository2");
        Resource schemaResource2 = new ClassPathResource("org/citrusframework/validation/sample.xsd");
        SimpleXsdSchema schema2 = new SimpleXsdSchema(schemaResource2);
        schema2.afterPropertiesSet();

        schemaRepository2.getSchemas().add(schema2);

        context.getReferenceResolver().bind("schemaRepository2", schemaRepository2);

        try {
            validator.validateXMLSchema(message, context, new XmlMessageValidationContext());
            Assert.fail("Missing exception due to no matching schema repository error");
        } catch (CitrusRuntimeException e) {
            Assert.assertTrue(e.getMessage().startsWith("Failed to find proper schema repository"), e.getMessage());
        }
    }

    @Test
    public void validateNoMatchingSchema() throws SAXException, IOException, ParserConfigurationException {
        Message message = new DefaultMessage("<message xmlns='http://citrusframework.org/special'>"
                + "<correlationId>Kx1R123456789</correlationId>"
                + "<bookingId>Bx1G987654321</bookingId>"
                + "<test>Hello TestFramework</test>"
                + "</message>");

        DomXmlMessageValidator validator = new DomXmlMessageValidator();

        XsdSchemaRepository schemaRepository = new XsdSchemaRepository();
        schemaRepository.setName("schemaRepository");
        Resource schemaResource = new ClassPathResource("org/citrusframework/validation/test.xsd");
        SimpleXsdSchema schema = new SimpleXsdSchema(schemaResource);
        schema.afterPropertiesSet();
        Resource schemaResource2 = new ClassPathResource("org/citrusframework/validation/sample.xsd");
        SimpleXsdSchema schema2 = new SimpleXsdSchema(schemaResource2);
        schema2.afterPropertiesSet();

        schemaRepository.getSchemas().add(schema);
        schemaRepository.getSchemas().add(schema2);

        context.getReferenceResolver().bind("schemaRepository", schemaRepository);

        XmlMessageValidationContext validationContext = new XmlMessageValidationContext.Builder()
                .schemaRepository("schemaRepository")
                .build();

        try {
            validator.validateXMLSchema(message, context, validationContext);
            Assert.fail("Missing exception due to no matching schema repository error");
        } catch (CitrusRuntimeException e) {
            Assert.assertTrue(e.getMessage().startsWith("Unable to find proper XML schema definition"), e.getMessage());
        }
    }

    @Test
    public void validateNoSchemaRepositoryAtAll() throws SAXException, IOException, ParserConfigurationException {
        Message message = new DefaultMessage("<message xmlns='http://citrusframework.org/test'>"
                + "<correlationId>Kx1R123456789</correlationId>"
                + "<bookingId>Bx1G987654321</bookingId>"
                + "<test>Hello TestFramework</test>"
                + "</message>");

        DomXmlMessageValidator validator = new DomXmlMessageValidator();
        validator.validateXMLSchema(message, context, new XmlMessageValidationContext());
    }

    @Test
    public void validateSchemaWithSchemaImport() throws SAXException, IOException, ParserConfigurationException {
        Message message = new DefaultMessage("<sampleRequest  xmlns:ns='http://citrusframework.org/SampleService/Message'>"
                + "<command>Cm123456789</command>"
                + "<message>FOO</message>"
                + "</sampleRequest>");

        XsdSchemaRepository schemaRepository = new XsdSchemaRepository();
        schemaRepository.setName("schemaRepository");

        XsdSchemaCollection schemaCollection = new XsdSchemaCollection();
        schemaCollection.setSchemas(List.of("org/citrusframework/validation/SampleMessage.xsd", "org/citrusframework/validation/SampleTypes.xsd"));
        schemaCollection.initialize();
        schemaCollection.afterPropertiesSet();

        schemaRepository.getSchemas().add(schemaCollection);

        context.getReferenceResolver().bind("schemaRepository", schemaRepository);

        validator.validateXMLSchema(message, context, new XmlMessageValidationContext());
    }

    @Test
    public void validateSchemaWithSchemaImportAndWildcard() throws ParserConfigurationException, IOException, SAXException {
        Message message = new DefaultMessage("<sampleRequest  xmlns:ns='http://citrusframework.org/SampleService/Message'>"
                + "<command>Cm123456789</command>"
                + "<message>FOO</message>"
                + "</sampleRequest>");

        XsdSchemaRepository schemaRepository = new XsdSchemaRepository();
        schemaRepository.setName("schemaRepository");

        XsdSchemaCollection schemaCollection = new XsdSchemaCollection();
        schemaCollection.setSchemas(List.of("org/citrusframework/validation/Sample*.xsd"));
        schemaCollection.initialize();
        schemaCollection.afterPropertiesSet();

        schemaRepository.getSchemas().add(schemaCollection);

        context.getReferenceResolver().bind("schemaRepository", schemaRepository);

        validator.validateXMLSchema(message, context, new XmlMessageValidationContext());
    }

    @Test(expectedExceptions = {ValidationException.class})
    public void validateXMLSchemaError() throws SAXException, IOException, ParserConfigurationException {
        Message message = new DefaultMessage("<message xmlns='http://citrusframework.org/test'>"
                        + "<correlationId>Kx1R123456789</correlationId>"
                        + "<bookingId>Bx1G987654321</bookingId>"
                        + "<test>Hello TestFramework</test>"
                        + "<wrongElement>totally wrong</wrongElement>"
                    + "</message>");

        DomXmlMessageValidator validator = new DomXmlMessageValidator();

        XsdSchemaRepository schemaRepository = new XsdSchemaRepository();
        Resource schemaResource = new ClassPathResource("org/citrusframework/validation/test.xsd");
        SimpleXsdSchema schema = new SimpleXsdSchema(schemaResource);
        schema.afterPropertiesSet();

        schemaRepository.getSchemas().add(schema);

        context.getReferenceResolver().bind("schemaRepository", schemaRepository);

        validator.validateXMLSchema(message, context, new XmlMessageValidationContext());
    }

    @Test
    public void testExpectDefaultNamespace() {
        Message message = new DefaultMessage("<root xmlns='http://citrusframework.org/test'>"
                        + "<element attributeA='attribute-value' attributeB='attribute-value'>"
                        + "<sub-element attribute='A'>text-value</sub-element>"
                        + "</element>"
                    + "</root>");

        Map<String, String> expectedNamespaces = new HashMap<String, String>();
        expectedNamespaces.put("", "http://citrusframework.org/test");

        DomXmlMessageValidator validator = new DomXmlMessageValidator();
        validator.validateNamespaces(expectedNamespaces, message);
    }

    @Test
    public void testExpectNamespace() {
    	Message message = new DefaultMessage("<ns1:root xmlns:ns1='http://citrusframework.org/ns1'>"
                        + "<ns1:element attributeA='attribute-value' attributeB='attribute-value'>"
                        + "<ns1:sub-element attribute='A'>text-value</ns1:sub-element>"
                        + "</ns1:element>"
                    + "</ns1:root>");

        Map<String, String> expectedNamespaces = new HashMap<String, String>();
        expectedNamespaces.put("ns1", "http://citrusframework.org/ns1");

        DomXmlMessageValidator validator = new DomXmlMessageValidator();
        validator.validateNamespaces(expectedNamespaces, message);
    }

    @Test
    public void testExpectMixedNamespaces() {
    	Message message = new DefaultMessage("<root xmlns='http://citrusframework.org/default' xmlns:ns1='http://citrusframework.org/ns1'>"
                        + "<element attributeA='attribute-value' attributeB='attribute-value'>"
                        + "<sub-element attribute='A'>text-value</sub-element>"
                        + "</element>"
                    + "</root>");

        Map<String, String> expectedNamespaces = new HashMap<String, String>();
        expectedNamespaces.put("", "http://citrusframework.org/default");
        expectedNamespaces.put("ns1", "http://citrusframework.org/ns1");

        DomXmlMessageValidator validator = new DomXmlMessageValidator();
        validator.validateNamespaces(expectedNamespaces, message);
    }

    @Test
    public void testExpectMultipleNamespaces() {
    	Message message = new DefaultMessage("<root xmlns='http://citrusframework.org/default' xmlns:ns1='http://citrusframework.org/ns1' xmlns:ns2='http://citrusframework.org/ns2'>"
                        + "<element attributeA='attribute-value' attributeB='attribute-value'>"
                        + "<sub-element attribute='A'>text-value</sub-element>"
                        + "</element>"
                    + "</root>");

        Map<String, String> expectedNamespaces = new HashMap<String, String>();
        expectedNamespaces.put("", "http://citrusframework.org/default");
        expectedNamespaces.put("ns1", "http://citrusframework.org/ns1");
        expectedNamespaces.put("ns2", "http://citrusframework.org/ns2");

        DomXmlMessageValidator validator = new DomXmlMessageValidator();
        validator.validateNamespaces(expectedNamespaces, message);
    }

    @Test(expectedExceptions = {ValidationException.class})
    public void testExpectDefaultNamespaceError() {
    	Message message = new DefaultMessage("<root xmlns='http://citrusframework.org'>"
                        + "<element attributeA='attribute-value' attributeB='attribute-value'>"
                        + "<sub-element attribute='A'>text-value</sub-element>"
                        + "</element>"
                    + "</root>");

        Map<String, String> expectedNamespaces = new HashMap<String, String>();
        expectedNamespaces.put("", "http://citrusframework.org/wrong");

        DomXmlMessageValidator validator = new DomXmlMessageValidator();
        validator.validateNamespaces(expectedNamespaces, message);
    }

    @Test(expectedExceptions = {ValidationException.class})
    public void testExpectNamespaceError() {
    	Message message = new DefaultMessage("<ns1:root xmlns:ns1='http://citrusframework.org/ns1'>"
                        + "<ns1:element attributeA='attribute-value' attributeB='attribute-value'>"
                        + "<ns1:sub-element attribute='A'>text-value</ns1:sub-element>"
                        + "</ns1:element>"
                    + "</ns1:root>");

        Map<String, String> expectedNamespaces = new HashMap<String, String>();
        expectedNamespaces.put("ns1", "http://citrusframework.org/ns1/wrong");

        DomXmlMessageValidator validator = new DomXmlMessageValidator();
        validator.validateNamespaces(expectedNamespaces, message);
    }

    @Test(expectedExceptions = {ValidationException.class})
    public void testExpectMixedNamespacesError() {
    	Message message = new DefaultMessage("<root xmlns='http://citrusframework.org/default' xmlns:ns1='http://citrusframework.org/ns1'>"
                        + "<element attributeA='attribute-value' attributeB='attribute-value'>"
                        + "<sub-element attribute='A'>text-value</sub-element>"
                        + "</element>"
                    + "</root>");

        Map<String, String> expectedNamespaces = new HashMap<String, String>();
        expectedNamespaces.put("", "http://citrusframework.org/default/wrong");
        expectedNamespaces.put("ns1", "http://citrusframework.org/ns1");

        DomXmlMessageValidator validator = new DomXmlMessageValidator();
        validator.validateNamespaces(expectedNamespaces, message);
    }

    @Test(expectedExceptions = {ValidationException.class})
    public void testExpectMultipleNamespacesError() {
    	Message message = new DefaultMessage("<root xmlns='http://citrusframework.org/default' xmlns:ns1='http://citrusframework.org/ns1' xmlns:ns2='http://citrusframework.org/ns2'>"
                        + "<element attributeA='attribute-value' attributeB='attribute-value'>"
                        + "<sub-element attribute='A'>text-value</sub-element>"
                        + "</element>"
                    + "</root>");

        Map<String, String> expectedNamespaces = new HashMap<String, String>();
        expectedNamespaces.put("", "http://citrusframework.org/default");
        expectedNamespaces.put("ns1", "http://citrusframework.org/ns1/wrong");
        expectedNamespaces.put("ns2", "http://citrusframework.org/ns2");

        DomXmlMessageValidator validator = new DomXmlMessageValidator();
        validator.validateNamespaces(expectedNamespaces, message);
    }

    @Test(expectedExceptions = {ValidationException.class})
    public void testExpectWrongNamespacePrefix() {
    	Message message = new DefaultMessage("<root xmlns='http://citrusframework.org/default' xmlns:ns1='http://citrusframework.org/ns1' xmlns:ns2='http://citrusframework.org/ns2'>"
                        + "<element attributeA='attribute-value' attributeB='attribute-value'>"
                        + "<sub-element attribute='A'>text-value</sub-element>"
                        + "</element>"
                    + "</root>");

        Map<String, String> expectedNamespaces = new HashMap<String, String>();
        expectedNamespaces.put("", "http://citrusframework.org/default");
        expectedNamespaces.put("nswrong", "http://citrusframework.org/ns1");
        expectedNamespaces.put("ns2", "http://citrusframework.org/ns2");

        DomXmlMessageValidator validator = new DomXmlMessageValidator();
        validator.validateNamespaces(expectedNamespaces, message);
    }

    @Test(expectedExceptions = {ValidationException.class})
    public void testExpectDefaultNamespaceButNamespace() {
    	Message message = new DefaultMessage("<ns0:root xmlns:ns0='http://citrusframework.org/default' xmlns:ns1='http://citrusframework.org/ns1' xmlns:ns2='http://citrusframework.org/ns2'>"
                        + "<ns0:element attributeA='attribute-value' attributeB='attribute-value'>"
                        + "<ns0:sub-element attribute='A'>text-value</ns0:sub-element>"
                        + "</ns0:element>"
                    + "</ns0:root>");

        Map<String, String> expectedNamespaces = new HashMap<String, String>();
        expectedNamespaces.put("", "http://citrusframework.org/default");
        expectedNamespaces.put("ns1", "http://citrusframework.org/ns1");
        expectedNamespaces.put("ns2", "http://citrusframework.org/ns2");

        DomXmlMessageValidator validator = new DomXmlMessageValidator();
        validator.validateNamespaces(expectedNamespaces, message);
    }

    @Test(expectedExceptions = {ValidationException.class})
    public void testExpectNamespaceButDefaultNamespace() {
    	Message message = new DefaultMessage("<root xmlns='http://citrusframework.org/default' xmlns:ns1='http://citrusframework.org/ns1' xmlns:ns2='http://citrusframework.org/ns2'>"
                        + "<element attributeA='attribute-value' attributeB='attribute-value'>"
                        + "<sub-element attribute='A'>text-value</sub-element>"
                        + "</element>"
                    + "</root>");

        Map<String, String> expectedNamespaces = new HashMap<String, String>();
        expectedNamespaces.put("ns0", "http://citrusframework.org/default");
        expectedNamespaces.put("ns1", "http://citrusframework.org/ns1");
        expectedNamespaces.put("ns2", "http://citrusframework.org/ns2");

        DomXmlMessageValidator validator = new DomXmlMessageValidator();
        validator.validateNamespaces(expectedNamespaces, message);
    }

    @Test(expectedExceptions = {ValidationException.class})
    public void testExpectAdditionalNamespace() {
    	Message message = new DefaultMessage("<root xmlns='http://citrusframework.org/default' xmlns:ns1='http://citrusframework.org/ns1' xmlns:ns2='http://citrusframework.org/ns2'>"
                        + "<element attributeA='attribute-value' attributeB='attribute-value'>"
                        + "<sub-element attribute='A'>text-value</sub-element>"
                        + "</element>"
                    + "</root>");

        Map<String, String> expectedNamespaces = new HashMap<String, String>();
        expectedNamespaces.put("", "http://citrusframework.org/default");
        expectedNamespaces.put("ns1", "http://citrusframework.org/ns1");
        expectedNamespaces.put("ns2", "http://citrusframework.org/ns2");
        expectedNamespaces.put("ns4", "http://citrusframework.org/ns4");

        DomXmlMessageValidator validator = new DomXmlMessageValidator();
        validator.validateNamespaces(expectedNamespaces, message);
    }

    @Test(expectedExceptions = {ValidationException.class})
    public void testExpectNamespaceButNamespaceMissing() {
    	Message message = new DefaultMessage("<root xmlns='http://citrusframework.org/default' xmlns:ns1='http://citrusframework.org/ns1' xmlns:ns2='http://citrusframework.org/ns2' xmlns:ns4='http://citrusframework.org/ns4'>"
                        + "<element attributeA='attribute-value' attributeB='attribute-value'>"
                        + "<sub-element attribute='A'>text-value</sub-element>"
                        + "</element>"
                    + "</root>");

        Map<String, String> expectedNamespaces = new HashMap<String, String>();
        expectedNamespaces.put("", "http://citrusframework.org/default");
        expectedNamespaces.put("ns1", "http://citrusframework.org/ns1");
        expectedNamespaces.put("ns2", "http://citrusframework.org/ns2");

        DomXmlMessageValidator validator = new DomXmlMessageValidator();
        validator.validateNamespaces(expectedNamespaces, message);
    }

    @Test
    public void testValidateMessagePayloadSuccess() {
        Message message = new DefaultMessage("<root>"
                        + "<element attributeA='attribute-value' attributeB='attribute-value'>"
                        + "<sub-element attribute='A'>text-value</sub-element>"
                        + "</element>"
                    + "</root>");

        Message controlMessage = new DefaultMessage("<root>"
                        + "<element attributeA='attribute-value' attributeB='attribute-value'>"
                        + "<sub-element attribute='A'>text-value</sub-element>"
                        + "</element>"
                    + "</root>");

        XmlMessageValidationContext validationContext = new XmlMessageValidationContext();

        DomXmlMessageValidator validator = new DomXmlMessageValidator();
        validator.validateMessage(message, controlMessage, context, validationContext);
    }

    @Test
    public void testValidateMessagePayloadWithIgnoresSuccess() {
        Message message = new DefaultMessage("<root>"
                        + "<element attributeA='attribute-value' attributeB='attribute-value'>"
                        + "<sub-element1 attribute='A'>THIS_IS_IGNORED_BY_XPATH</sub-element1>"
                        + "<sub-element2 attribute='A'>THIS IS IGNORED BY IGNORE-EXPR</sub-element2>"
                        + "<sub-element3 attribute='A'>a text</sub-element3>"
                        + "</element>"
                    + "</root>");

        Message controlMessage = new DefaultMessage("<root>"
                        + "<element attributeA='attribute-value' attributeB='attribute-value'>"
                        + "<sub-element1 attribute='A'>text-value</sub-element1>"
                        + "<sub-element2 attribute='A'>@ignore@</sub-element2>"
                        + "<sub-element3 attribute='A'>a text</sub-element3>"
                        + "</element>"
                    + "</root>");


        Set<String> ignoreExpressions = new HashSet<String>();
        ignoreExpressions.add("//root/element/sub-element1");

        XmlMessageValidationContext validationContext = new XmlMessageValidationContext.Builder()
                .ignore(ignoreExpressions)
                .build();

        DomXmlMessageValidator validator = new DomXmlMessageValidator();
        validator.validateMessage(message, controlMessage, context, validationContext);
    }

    @Test(expectedExceptions = {ValidationException.class})
    public void testValidateMessagePayloadWithValidationMatchersFailsBecauseOfAttribute() {
        Message message = new DefaultMessage("<root>"
                        + "<element attributeA='attribute-value' attributeB='attribute-value'>"
                        + "<sub-element attribute='text-attribute'>text-element</sub-element>"
                        + "</element>"
                    + "</root>");

        Message controlMessage = new DefaultMessage("<root>"
                        + "<element attributeA='attribute-value' attributeB='attribute-value'>"
                        + "<sub-element attribute='@startsWith(FAIL)@'>@startsWith(text)@</sub-element>"
                        + "</element>"
                    + "</root>");

        XmlMessageValidationContext validationContext = new XmlMessageValidationContext();

        DomXmlMessageValidator validator = new DomXmlMessageValidator();
        validator.validateMessage(message, controlMessage, context, validationContext);
    }

    @Test(expectedExceptions = {ValidationException.class})
    public void testValidateMessagePayloadWithValidationMatcherOnElementFails() {
        Message message = new DefaultMessage("<root>"
                        + "<element attributeA='attribute-value' attributeB='attribute-value'>"
                        + "<sub-element attribute='text-attribute'>text-element</sub-element>"
                        + "</element>"
                    + "</root>");

        Message controlMessage = new DefaultMessage("<root>"
                        + "<element attributeA='attribute-value' attributeB='attribute-value'>"
                        + "<sub-element attribute='text-attribute'>@startsWith(FAIL)@</sub-element>"
                        + "</element>"
                    + "</root>");

        XmlMessageValidationContext validationContext = new XmlMessageValidationContext();

        DomXmlMessageValidator validator = new DomXmlMessageValidator();
        validator.validateMessage(message, controlMessage, context, validationContext);
    }

    @Test(expectedExceptions = {ValidationException.class})
    public void testValidateMessagePayloadWithValidationMatcherOnAttributeFails() {
        Message message = new DefaultMessage("<root>"
                        + "<element attributeA='attribute-value' attributeB='attribute-value'>"
                        + "<sub-element attribute='text-attribute'>text-element</sub-element>"
                        + "</element>"
                    + "</root>");

        Message controlMessage = new DefaultMessage("<root>"
                        + "<element attributeA='attribute-value' attributeB='attribute-value'>"
                        + "<sub-element attribute='@startsWith(FAIL)@'>text-element</sub-element>"
                        + "</element>"
                    + "</root>");

        XmlMessageValidationContext validationContext = new XmlMessageValidationContext();

        DomXmlMessageValidator validator = new DomXmlMessageValidator();
        validator.validateMessage(message, controlMessage, context, validationContext);
    }

    @Test
    public void testNamespaceQualifiedAttributeValue() {
        Message message = new DefaultMessage("<root xmlns='http://citrusframework.org/default' xmlns:ns1='http://citrusframework.org/ns1' xmlns:ns2='http://citrusframework.org/ns2' xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance'>"
                        + "<element xsi:type='ns1:attribute-value' attributeB='attribute-value'>"
                        + "<sub-element xsi:type='ns2:AType'>text-value</sub-element>"
                        + "</element>"
                    + "</root>");

        Message controlMessage = new DefaultMessage("<root xmlns='http://citrusframework.org/default' xmlns:ns1='http://citrusframework.org/ns1' xmlns:ns2='http://citrusframework.org/ns2' xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance'>"
                        + "<element xsi:type='ns1:attribute-value' attributeB='attribute-value'>"
                        + "<sub-element xsi:type='ns2:AType'>text-value</sub-element>"
                        + "</element>"
                    + "</root>");

        XmlMessageValidationContext validationContext = new XmlMessageValidationContext.Builder()
                .schemaValidation(false)
                .build();

        DomXmlMessageValidator validator = new DomXmlMessageValidator();
        validator.validateMessage(message, controlMessage, context, validationContext);
    }

    @Test
    public void testNamespaceQualifiedAttributeValueParentDeclaration() {
        Message message = new DefaultMessage("<root xmlns='http://citrusframework.org/default' xmlns:ns2='http://citrusframework.org/ns2' xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance'>"
                + "<element xmlns:ns1='http://citrusframework.org/ns1' xsi:type='ns1:attribute-value' attributeB='attribute-value'>"
                + "<sub-element xsi:type='ns2:AType'>text-value</sub-element>"
                + "</element>"
                + "</root>");

        Message controlMessage = new DefaultMessage("<root xmlns='http://citrusframework.org/default' xmlns:ns1='http://citrusframework.org/ns1' xmlns:ns2='http://citrusframework.org/ns2' xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance'>"
                + "<element xsi:type='ns1:attribute-value' attributeB='attribute-value'>"
                + "<sub-element xsi:type='ns2:AType'>text-value</sub-element>"
                + "</element>"
                + "</root>");

        XmlMessageValidationContext validationContext = new XmlMessageValidationContext.Builder()
                .schemaValidation(false)
                .build();

        DomXmlMessageValidator validator = new DomXmlMessageValidator();
        validator.validateMessage(message, controlMessage, context, validationContext);
    }

    @Test
    public void testNamespaceQualifiedAttributeValueParentDeclarationInSource() {
        Message message = new DefaultMessage("<root xmlns='http://citrusframework.org/default' xmlns:ns2='http://citrusframework.org/ns2' xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance'>"
                + "<element xmlns:ns1='http://citrusframework.org/ns1' xsi:type='ns1:attribute-value' attributeB='attribute-value'>"
                + "<sub-element xsi:type='ns2:AType'>text-value</sub-element>"
                + "</element>"
                + "</root>");

        Message controlMessage = new DefaultMessage("<root xmlns='http://citrusframework.org/default' xmlns:ns2='http://citrusframework.org/ns2' xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance'>"
                + "<element xmlns:ns1='http://citrusframework.org/ns1' xsi:type='ns1:attribute-value' attributeB='attribute-value'>"
                + "<sub-element xsi:type='ns2:AType'>text-value</sub-element>"
                + "</element>"
                + "</root>");

        XmlMessageValidationContext validationContext = new XmlMessageValidationContext.Builder()
                .schemaValidation(false)
                .build();

        DomXmlMessageValidator validator = new DomXmlMessageValidator();
        validator.validateMessage(message, controlMessage, context, validationContext);
    }

    @Test
    public void testNamespaceQualifiedAttributeValueDifferentPrefix() {
        Message message = new DefaultMessage("<root xmlns='http://citrusframework.org/default' xmlns:ns1='http://citrusframework.org/ns1' xmlns:ns2='http://citrusframework.org/ns2' xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance'>"
                + "<element xsi:type='ns1:attribute-value' attributeB='attribute-value'>"
                + "<sub-element xsi:type='ns2:AType'>text-value</sub-element>"
                + "</element>"
                + "</root>");

        Message controlMessage = new DefaultMessage("<root xmlns='http://citrusframework.org/default' xmlns:cit='http://citrusframework.org/ns1' xmlns:cit2='http://citrusframework.org/ns2' xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance'>"
                + "<element xsi:type='cit:attribute-value' attributeB='attribute-value'>"
                + "<sub-element xsi:type='cit2:AType'>text-value</sub-element>"
                + "</element>"
                + "</root>");

        XmlMessageValidationContext validationContext = new XmlMessageValidationContext.Builder()
                .schemaValidation(false)
                .build();

        DomXmlMessageValidator validator = new DomXmlMessageValidator();
        validator.validateMessage(message, controlMessage, context, validationContext);
    }

    @Test
    public void testNamespaceQualifiedLikeAttributeValues() {
        Message message = new DefaultMessage("<root xmlns='http://citrusframework.org/default' xmlns:ns1='http://citrusframework.org/ns1' xmlns:ns2='http://citrusframework.org/ns2' xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance'>"
                + "<element credentials='username:password' attributeB='attribute-value'>"
                + "<sub-element>text-value</sub-element>"
                + "</element>"
                + "</root>");

        Message controlMessage = new DefaultMessage("<root xmlns='http://citrusframework.org/default' xmlns:ns1='http://citrusframework.org/ns1' xmlns:ns2='http://citrusframework.org/ns2' xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance'>"
                + "<element credentials='username:password' attributeB='attribute-value'>"
                + "<sub-element>text-value</sub-element>"
                + "</element>"
                + "</root>");

        XmlMessageValidationContext validationContext = new XmlMessageValidationContext.Builder()
                .schemaValidation(false)
                .build();

        DomXmlMessageValidator validator = new DomXmlMessageValidator();
        validator.validateMessage(message, controlMessage, context, validationContext);
    }

    @Test
    public void testCommentBeforeRootElement() {
        Message message = new DefaultMessage("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "<!-- some comment -->"
                + "<root>"
                + "<element>test</element>"
                + "</root>");

        Message controlMessage = new DefaultMessage("<root>"
                + "<element>test</element>"
                + "</root>");

        XmlMessageValidationContext validationContext = new XmlMessageValidationContext();

        DomXmlMessageValidator validator = new DomXmlMessageValidator();
        validator.validateMessage(message, controlMessage, context, validationContext);
    }

    @Test
    public void testComment() {
        Message message = new DefaultMessage("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "<root>"
                + "<!-- some comment -->"
                + "<element>test</element>"
                + "</root>");

        Message controlMessage = new DefaultMessage("<root>"
                + "<element>test</element>"
                + "</root>");

        XmlMessageValidationContext validationContext = new XmlMessageValidationContext();

        DomXmlMessageValidator validator = new DomXmlMessageValidator();
        validator.validateMessage(message, controlMessage, context, validationContext);
    }

    @Test(expectedExceptions = {ValidationException.class})
    public void testNamespaceQualifiedAttributeValueFails() {
        Message message = new DefaultMessage("<root xmlns='http://citrusframework.org/default' xmlns:ns1='http://citrusframework.org/ns1' xmlns:ns2='http://citrusframework.org/ns2' xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance'>"
                + "<element xsi:type='ns1:attribute-value' attributeB='attribute-value'>"
                + "<sub-element xsi:type='ns2:AType'>text-value</sub-element>"
                + "</element>"
                + "</root>");

        Message controlMessage = new DefaultMessage("<root xmlns='http://citrusframework.org/default' xmlns:ns1='http://citrusframework.org/ns1' xmlns:ns2='http://citrusframework.org/ns2' xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance'>"
                + "<element xsi:type='ns1:wrong-value' attributeB='attribute-value'>"
                + "<sub-element xsi:type='ns2:AType'>text-value</sub-element>"
                + "</element>"
                + "</root>");

        XmlMessageValidationContext validationContext = new XmlMessageValidationContext.Builder()
                .schemaValidation(false)
                .build();

        DomXmlMessageValidator validator = new DomXmlMessageValidator();
        validator.validateMessage(message, controlMessage, context, validationContext);
    }

    @Test(expectedExceptions = {ValidationException.class})
    public void testNamespaceQualifiedAttributeValueUriMismatch() {
        Message message = new DefaultMessage("<root xmlns='http://citrusframework.org/default' xmlns:ns1='http://citrusframework.org/ns1' xmlns:ns2='http://citrusframework.org/ns2' xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance'>"
                + "<element xsi:type='ns1:attribute-value' attributeB='attribute-value'>"
                + "<sub-element xsi:type='ns2:AType'>text-value</sub-element>"
                + "</element>"
                + "</root>");

        Message controlMessage = new DefaultMessage("<root xmlns='http://citrusframework.org/default' xmlns:cit='http://citrusframework.org/cit' xmlns:ns2='http://citrusframework.org/ns2' xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance'>"
                + "<element xsi:type='cit:attribute-value' attributeB='attribute-value'>"
                + "<sub-element xsi:type='ns2:AType'>text-value</sub-element>"
                + "</element>"
                + "</root>");

        XmlMessageValidationContext validationContext = new XmlMessageValidationContext.Builder()
                .schemaValidation(false)
                .build();

        DomXmlMessageValidator validator = new DomXmlMessageValidator();
        validator.validateMessage(message, controlMessage, context, validationContext);
    }

    @Test(expectedExceptions = {ValidationException.class})
    public void testNamespaceQualifiedAttributeMissingPrefix() {
        Message message = new DefaultMessage("<root xmlns='http://citrusframework.org/default' xmlns:ns1='http://citrusframework.org/ns1' xmlns:ns2='http://citrusframework.org/ns2' xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance'>"
                + "<element xsi:type='attribute-value' attributeB='attribute-value'>"
                + "<sub-element xsi:type='ns2:AType'>text-value</sub-element>"
                + "</element>"
                + "</root>");

        Message controlMessage = new DefaultMessage("<root xmlns='http://citrusframework.org/default' xmlns:ns1='http://citrusframework.org/cit' xmlns:ns2='http://citrusframework.org/ns2' xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance'>"
                + "<element xsi:type='ns1:attribute-value' attributeB='attribute-value'>"
                + "<sub-element xsi:type='ns2:AType'>text-value</sub-element>"
                + "</element>"
                + "</root>");

        XmlMessageValidationContext validationContext = new XmlMessageValidationContext.Builder()
                .schemaValidation(false)
                .build();

        DomXmlMessageValidator validator = new DomXmlMessageValidator();
        validator.validateMessage(message, controlMessage, context, validationContext);
    }

    @Test(expectedExceptions = {ValidationException.class})
    public void testNamespaceQualifiedAttributeValueMissingDeclaration() {
        Message message = new DefaultMessage("<root xmlns='http://citrusframework.org/default' xmlns:ns1='http://citrusframework.org/ns1' xmlns:ns2='http://citrusframework.org/ns2' xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance'>"
                + "<element xsi:type='ns1:attribute-value' attributeB='attribute-value'>"
                + "<sub-element xsi:type='ns2:AType'>text-value</sub-element>"
                + "</element>"
                + "</root>");

        Message controlMessage = new DefaultMessage("<root xmlns='http://citrusframework.org/default' xmlns:ns2='http://citrusframework.org/ns2' xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance'>"
                + "<element xsi:type='cit:attribute-value' attributeB='attribute-value'>"
                + "<sub-element xsi:type='ns2:AType'>text-value</sub-element>"
                + "</element>"
                + "</root>");

        XmlMessageValidationContext validationContext = new XmlMessageValidationContext.Builder()
                .schemaValidation(false)
                .build();

        DomXmlMessageValidator validator = new DomXmlMessageValidator();
        validator.validateMessage(message, controlMessage, context, validationContext);
    }

    @Test
    public void shouldFindProperValidationContext() {
        List<ValidationContext> validationContexts = new ArrayList<>();
        validationContexts.add(new HeaderValidationContext());
        validationContexts.add(new XmlMessageValidationContext());

        Assert.assertNotNull(validator.findValidationContext(validationContexts));

        validationContexts.clear();
        validationContexts.add(new JsonMessageValidationContext());
        validationContexts.add(new ScriptValidationContext(MessageType.PLAINTEXT.name()));

        Assert.assertNull(validator.findValidationContext(validationContexts));
    }

    @Test
    public void testLookup() {
        Map<String, SchemaValidator<? extends SchemaValidationContext>> validators = SchemaValidator.lookup();
        Assert.assertEquals(validators.size(), 1L);
        Assert.assertNotNull(validators.get("defaultXmlSchemaValidator"));
        Assert.assertEquals(validators.get("defaultXmlSchemaValidator").getClass(), XmlSchemaValidation.class);
    }

    @Test
    public void testTestLookup() {
        Assert.assertTrue(SchemaValidator.lookup("xml").isPresent());
    }

}
