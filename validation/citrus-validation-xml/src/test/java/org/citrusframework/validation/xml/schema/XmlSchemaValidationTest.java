/*
 * Copyright 2024 the original author or authors.
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

package org.citrusframework.validation.xml.schema;

import org.citrusframework.context.TestContext;
import org.citrusframework.context.TestContextFactory;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.message.DefaultMessage;
import org.citrusframework.message.Message;
import org.citrusframework.util.SystemProvider;
import org.citrusframework.validation.xml.XmlMessageValidationContext;
import org.citrusframework.xml.XsdSchemaRepository;
import org.mockito.Mock;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.xml.xsd.SimpleXsdSchema;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.Optional;

import static java.lang.String.format;
import static org.citrusframework.validation.xml.schema.ValidationStrategy.FAIL;
import static org.citrusframework.validation.xml.schema.ValidationStrategy.IGNORE;
import static org.citrusframework.validation.xml.schema.XmlSchemaValidation.NO_SCHEMA_FOUND_STRATEGY_ENV_VAR_NAME;
import static org.citrusframework.validation.xml.schema.XmlSchemaValidation.NO_SCHEMA_FOUND_STRATEGY_PROPERTY_NAME;
import static org.mockito.Mockito.doReturn;
import static org.mockito.MockitoAnnotations.openMocks;
import static org.springframework.test.util.ReflectionTestUtils.getField;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertThrows;

public class XmlSchemaValidationTest {

    @Mock
    private SystemProvider systemProviderMock;

    @BeforeMethod
    public void beforeMethodSetup() {
        openMocks(this);
    }

    @DataProvider(name = "validParameters")
    public Object[][] dataProvider() {
        return new Object[][] {
                {"Ignore", IGNORE},
                {"Fail", FAIL},
                {"IGNORE", IGNORE},
                {"FAIL", FAIL},
                {"ignore", IGNORE},
                {"fail", FAIL},
                {null, FAIL},
        };
    }

    @Test
    public void configurationFromEnvTakesPrecedenceOverProperties() {
        doReturn(Optional.of("IGNORE")).when(systemProviderMock).getEnv(NO_SCHEMA_FOUND_STRATEGY_ENV_VAR_NAME);
        doReturn(Optional.of("FAIL")).when(systemProviderMock).getProperty(NO_SCHEMA_FOUND_STRATEGY_PROPERTY_NAME);

        XmlSchemaValidation fixture = new XmlSchemaValidation(systemProviderMock);

        assertEquals(getField(fixture, "noSchemaFoundStrategy"), IGNORE);
    }

    @Test
    public void configurationFromEnv() {
        doReturn(Optional.of("IGNORE")).when(systemProviderMock).getEnv(NO_SCHEMA_FOUND_STRATEGY_ENV_VAR_NAME);

        XmlSchemaValidation fixture = new XmlSchemaValidation(systemProviderMock);

        assertEquals(getField(fixture, "noSchemaFoundStrategy"), IGNORE);
    }

    @Test
    public void configurationFromProp() {
        doReturn(Optional.of("IGNORE")).when(systemProviderMock).getProperty(NO_SCHEMA_FOUND_STRATEGY_PROPERTY_NAME);

        XmlSchemaValidation fixture = new XmlSchemaValidation(systemProviderMock);

        assertEquals(getField(fixture, "noSchemaFoundStrategy"), IGNORE);
    }

    @Test
    public void configurationDefaultValues() {
        XmlSchemaValidation fixture = new XmlSchemaValidation();
        assertEquals(getField(fixture, "noSchemaFoundStrategy"), FAIL);
    }

    @Test(dataProvider = "validParameters")
    public void testValidPropertyValues(String value, ValidationStrategy expectedValidationStrategy) throws ParserConfigurationException, IOException, SAXException {
        doReturn(Optional.ofNullable(value)).when(systemProviderMock).getProperty(NO_SCHEMA_FOUND_STRATEGY_PROPERTY_NAME);
        XmlSchemaValidation fixture = new XmlSchemaValidation(systemProviderMock);

        Message message = new DefaultMessage("<message xmlns='http://citrusframework.org/otherXsd'>"
                + "<attribut>Hello Citrus</attribut>"
                + "</message>");

        XsdSchemaRepository schemaRepository = new XsdSchemaRepository();
        Resource schemaResource = new ClassPathResource("org/citrusframework/validation/test.xsd");
        SimpleXsdSchema schema = new SimpleXsdSchema(schemaResource);
        schema.afterPropertiesSet();

        schemaRepository.getSchemas().add(schema);

        TestContextFactory testContextFactory = TestContextFactory.newInstance();
        TestContext context = testContextFactory.getObject();

        context.getReferenceResolver().bind("schemaRepository", schemaRepository);

        switch (expectedValidationStrategy) {
            case FAIL -> assertThrows("Unable to find proper XML schema definition for element 'message(http://citrusframework.org/otherXsd)' in schema repository 'schemaRepository'", CitrusRuntimeException.class, () -> fixture.validate(message, context, new XmlMessageValidationContext()));
            case IGNORE -> fixture.validate(message, context, new XmlMessageValidationContext());
            default -> throw new IllegalArgumentException("Unexpected ValidationStrategy: " + expectedValidationStrategy + "!");
        }
    }

    @Test
    public void testInvalidPropertyValue() {
        String invalidValue = "DEACTIVATED";

        doReturn(Optional.of(invalidValue)).when(systemProviderMock).getProperty(NO_SCHEMA_FOUND_STRATEGY_PROPERTY_NAME);

        assertThrows(format("Invalid property value '%s' for no schema found strategy", invalidValue), CitrusRuntimeException.class,() -> new XmlSchemaValidation(systemProviderMock));
    }
}
