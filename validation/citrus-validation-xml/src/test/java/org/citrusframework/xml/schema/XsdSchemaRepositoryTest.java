/*
 * Copyright the original author or authors.
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

package org.citrusframework.xml.schema;

import javax.xml.parsers.ParserConfigurationException;

import org.springframework.xml.xsd.SimpleXsdSchema;
import org.junit.jupiter.api.Test;
import org.xml.sax.SAXException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class XsdSchemaRepositoryTest {

    @Test
    public void testResourceLocation() {
        XsdSchemaRepository schemaRepository = new XsdSchemaRepository();

        schemaRepository.getLocations().add("classpath:org/citrusframework/schema/citrus-config.xsd");

        schemaRepository.initialize();

        assertEquals(1, schemaRepository.getSchemas().size());
        assertEquals(SimpleXsdSchema.class, schemaRepository.getSchemas().get(0).getClass());
    }

    @Test
    public void testUnknownLocation() {
        XsdSchemaRepository schemaRepository = new XsdSchemaRepository();

        schemaRepository.getLocations().add("classpath:org/citrusframework/unknown/unknown.xsd");

        schemaRepository.initialize();
        assertEquals(0, schemaRepository.getSchemas().size());
    }

    @Test
    public void testResourceLocationPattern() {
        XsdSchemaRepository schemaRepository = new XsdSchemaRepository();

        schemaRepository.getLocations().add("classpath:org/citrusframework/schema/*.xsd");

        schemaRepository.initialize();

        assertTrue(schemaRepository.getSchemas().size() == 4 || schemaRepository.getSchemas().size() == 6, "Expecting schema repository size to be either 4 or 6");
        schemaRepository.getSchemas().forEach(xsdSchema -> assertEquals(SimpleXsdSchema.class, xsdSchema.getClass()));
    }

    @Test
    public void testResourceLocationPatternNothingFound() {
        XsdSchemaRepository schemaRepository = new XsdSchemaRepository();

        schemaRepository.getLocations().add("classpath:org/citrusframework/tests/*.xsd");

        schemaRepository.initialize();

        assertEquals(0, schemaRepository.getSchemas().size());
    }

    @Test
    public void testResourceLocationPatternWithExclusion() {
        XsdSchemaRepository schemaRepository = new XsdSchemaRepository();
        schemaRepository.getLocations().add("classpath:org/citrusframework/validation/*");

        schemaRepository.initialize();

        assertEquals(17, schemaRepository.getSchemas().size());

        schemaRepository = new XsdSchemaRepository();
        schemaRepository.getLocations().add("classpath:org/citrusframework/validation/*.xsd");

        schemaRepository.initialize();

        assertEquals(7, schemaRepository.getSchemas().size());
        assertEquals(SimpleXsdSchema.class, schemaRepository.getSchemas().get(0).getClass());
        assertEquals(SimpleXsdSchema.class, schemaRepository.getSchemas().get(1).getClass());
        assertEquals(SimpleXsdSchema.class, schemaRepository.getSchemas().get(2).getClass());

        schemaRepository = new XsdSchemaRepository();
        schemaRepository.getLocations().add("classpath:org/citrusframework/validation/*.wsdl");

        schemaRepository.initialize();

        assertEquals(10, schemaRepository.getSchemas().size());
        assertEquals(WsdlXsdSchema.class, schemaRepository.getSchemas().get(0).getClass());
        assertEquals(WsdlXsdSchema.class, schemaRepository.getSchemas().get(1).getClass());
        assertEquals(WsdlXsdSchema.class, schemaRepository.getSchemas().get(2).getClass());
        assertEquals(WsdlXsdSchema.class, schemaRepository.getSchemas().get(3).getClass());
        assertEquals(WsdlXsdSchema.class, schemaRepository.getSchemas().get(4).getClass());
        assertEquals(WsdlXsdSchema.class, schemaRepository.getSchemas().get(5).getClass());
        assertEquals(WsdlXsdSchema.class, schemaRepository.getSchemas().get(6).getClass());
        assertEquals(WsdlXsdSchema.class, schemaRepository.getSchemas().get(7).getClass());
        assertEquals(WsdlXsdSchema.class, schemaRepository.getSchemas().get(8).getClass());
        assertEquals(WsdlXsdSchema.class, schemaRepository.getSchemas().get(9).getClass());
    }

    @Test
    public void testWsdlResourceLocation() {
        XsdSchemaRepository schemaRepository = new XsdSchemaRepository();

        schemaRepository.getLocations().add("classpath:org/citrusframework/xml/BookStore.wsdl");

        schemaRepository.initialize();

        assertEquals(1, schemaRepository.getSchemas().size());
        assertEquals(WsdlXsdSchema.class, schemaRepository.getSchemas().get(0).getClass());
    }

    @Test
    public void testDefaultCitrusSchemas() throws ParserConfigurationException, SAXException {
        XsdSchemaRepository schemaRepository = new XsdSchemaRepository();

        schemaRepository.addCitrusSchema("citrus-unknown-config");

        assertEquals(0, schemaRepository.getSchemas().size());

        schemaRepository.addCitrusSchema("citrus-config");

        assertEquals(1, schemaRepository.getSchemas().size());
        assertEquals(SimpleXsdSchema.class, schemaRepository.getSchemas().get(0).getClass());
    }
}
