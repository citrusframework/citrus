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

package org.citrusframework.xml;

import javax.xml.parsers.ParserConfigurationException;

import org.citrusframework.xml.schema.WsdlXsdSchema;
import org.springframework.xml.xsd.SimpleXsdSchema;
import org.testng.annotations.Test;
import org.xml.sax.SAXException;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class XsdSchemaRepositoryTest {

    @Test
    public void testResourceLocation() {
        XsdSchemaRepository schemaRepository = new XsdSchemaRepository();

        schemaRepository.getLocations().add("classpath:org/citrusframework/schema/citrus-config.xsd");

        schemaRepository.initialize();

        assertEquals(schemaRepository.getSchemas().size(), 1);
        assertEquals(schemaRepository.getSchemas().get(0).getClass(), SimpleXsdSchema.class);
    }

    @Test
    public void testUnknownLocation() {
        XsdSchemaRepository schemaRepository = new XsdSchemaRepository();

        schemaRepository.getLocations().add("classpath:org/citrusframework/unknown/unknown.xsd");

        schemaRepository.initialize();
        assertEquals(schemaRepository.getSchemas().size(), 0);
    }

    @Test
    public void testResourceLocationPattern() {
        XsdSchemaRepository schemaRepository = new XsdSchemaRepository();

        schemaRepository.getLocations().add("classpath:org/citrusframework/schema/*.xsd");

        schemaRepository.initialize();

        assertTrue(schemaRepository.getSchemas().size() == 4 || schemaRepository.getSchemas().size() == 6, "Expecting schema repository size to be either 4 or 6");
        schemaRepository.getSchemas().forEach(xsdSchema -> assertEquals(xsdSchema.getClass(), SimpleXsdSchema.class));
    }

    @Test
    public void testResourceLocationPatternNothingFound() {
        XsdSchemaRepository schemaRepository = new XsdSchemaRepository();

        schemaRepository.getLocations().add("classpath:org/citrusframework/tests/*.xsd");

        schemaRepository.initialize();

        assertEquals(schemaRepository.getSchemas().size(), 0);
    }

    @Test
    public void testResourceLocationPatternWithExclusion() {
        XsdSchemaRepository schemaRepository = new XsdSchemaRepository();
        schemaRepository.getLocations().add("classpath:org/citrusframework/validation/*");

        schemaRepository.initialize();

        assertEquals(schemaRepository.getSchemas().size(), 17);

        schemaRepository = new XsdSchemaRepository();
        schemaRepository.getLocations().add("classpath:org/citrusframework/validation/*.xsd");

        schemaRepository.initialize();

        assertEquals(schemaRepository.getSchemas().size(), 7);
        assertEquals(schemaRepository.getSchemas().get(0).getClass(), SimpleXsdSchema.class);
        assertEquals(schemaRepository.getSchemas().get(1).getClass(), SimpleXsdSchema.class);
        assertEquals(schemaRepository.getSchemas().get(2).getClass(), SimpleXsdSchema.class);

        schemaRepository = new XsdSchemaRepository();
        schemaRepository.getLocations().add("classpath:org/citrusframework/validation/*.wsdl");

        schemaRepository.initialize();

        assertEquals(schemaRepository.getSchemas().size(), 10);
        assertEquals(schemaRepository.getSchemas().get(0).getClass(), WsdlXsdSchema.class);
        assertEquals(schemaRepository.getSchemas().get(1).getClass(), WsdlXsdSchema.class);
        assertEquals(schemaRepository.getSchemas().get(2).getClass(), WsdlXsdSchema.class);
        assertEquals(schemaRepository.getSchemas().get(3).getClass(), WsdlXsdSchema.class);
        assertEquals(schemaRepository.getSchemas().get(4).getClass(), WsdlXsdSchema.class);
        assertEquals(schemaRepository.getSchemas().get(5).getClass(), WsdlXsdSchema.class);
        assertEquals(schemaRepository.getSchemas().get(6).getClass(), WsdlXsdSchema.class);
        assertEquals(schemaRepository.getSchemas().get(7).getClass(), WsdlXsdSchema.class);
        assertEquals(schemaRepository.getSchemas().get(8).getClass(), WsdlXsdSchema.class);
        assertEquals(schemaRepository.getSchemas().get(9).getClass(), WsdlXsdSchema.class);
    }

    @Test
    public void testWsdlResourceLocation() {
        XsdSchemaRepository schemaRepository = new XsdSchemaRepository();

        schemaRepository.getLocations().add("classpath:org/citrusframework/xml/BookStore.wsdl");

        schemaRepository.initialize();

        assertEquals(schemaRepository.getSchemas().size(), 1);
        assertEquals(schemaRepository.getSchemas().get(0).getClass(), WsdlXsdSchema.class);
    }

    @Test
    public void testDefaultCitrusSchemas() throws ParserConfigurationException, SAXException {
        XsdSchemaRepository schemaRepository = new XsdSchemaRepository();

        schemaRepository.addCitrusSchema("citrus-unknown-config");

        assertEquals(schemaRepository.getSchemas().size(), 0);

        schemaRepository.addCitrusSchema("citrus-config");

        assertEquals(schemaRepository.getSchemas().size(), 1);
        assertEquals(schemaRepository.getSchemas().get(0).getClass(), SimpleXsdSchema.class);
    }
}
