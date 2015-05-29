/*
 * Copyright 2006-2013 the original author or authors.
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

package com.consol.citrus.admin.jaxb;

import com.consol.citrus.admin.exception.CitrusAdminRuntimeException;
import com.consol.citrus.model.config.core.*;
import org.testng.Assert;
import org.testng.annotations.Test;

import javax.xml.bind.JAXBContext;
import java.io.File;

/**
 * Tests JAXBHelper
 *
 * @author Martin.Maher@consol.de
 * @since 2013.04.19
 */
public class JAXBHelperTest {

    private JAXBHelper jaxbHelper = new JAXBHelper();

    private static final String[] CONTEXT_PATHS = {
            "com.consol.citrus.model.config.core"
    };

    @Test
    public void testBuildContextPath_multiplePaths() throws Exception {
        String contextPath = jaxbHelper.buildContextPath("a.b.c", "x.y.z");
        Assert.assertEquals(contextPath, "a.b.c:x.y.z");
    }

    @Test
    public void testBuildContextPath_singlePath() throws Exception {
        String contextPath = jaxbHelper.buildContextPath("a.b.c");
        Assert.assertEquals(contextPath, "a.b.c");
    }

    @Test
    public void testCreateJAXBContextByPath() {
        JAXBContext jaxbContext = jaxbHelper.createJAXBContextByPath(CONTEXT_PATHS);
        Assert.assertNotNull(jaxbContext);
    }

    @Test(expectedExceptions = CitrusAdminRuntimeException.class, expectedExceptionsMessageRegExp = "Error creating the JAXB context using path ''")
    public void testCreateJAXBContextByPath_emptyPath() {
        jaxbHelper.createJAXBContextByPath("");
    }

    @Test(expectedExceptions = CitrusAdminRuntimeException.class, expectedExceptionsMessageRegExp = "Error creating the JAXB context using path 'null:null'")
    public void testCreateJAXBContextByPath_nullPath() {
        jaxbHelper.createJAXBContextByPath(null, null);
    }

    @Test
    public void testMarshalClass() {
        JAXBContext jaxbContext = jaxbHelper.createJAXBContextByPath(CONTEXT_PATHS);

        SchemaDefinition xsdSchema = new SchemaDefinitionBuilder().withId("123").withLocation("this <location /> should be escaped").build();

        String marshalledXml = jaxbHelper.marshal(jaxbContext, xsdSchema);

        Assert.assertTrue(marshalledXml.contains("location=\"this &lt;location /&gt; should be escaped\""));
        Assert.assertTrue(marshalledXml.contains("id=\"123\""));
    }

    @Test(expectedExceptions = CitrusAdminRuntimeException.class, expectedExceptionsMessageRegExp = "Could not marshall element")
    public void testMarshalClass_invalidJAXBElement() {
        JAXBContext jaxbContext = jaxbHelper.createJAXBContextByPath(CONTEXT_PATHS);
        jaxbHelper.marshal(jaxbContext, "Could not marshall element");
    }

    @Test
    public void testUnmarshalFromString() throws Exception {
        String xml = "" +
                "<xsd-schema " +
                "location=\"this &lt;location /&gt; should be escaped\" " +
                "id=\"123\" " +
                "xmlns=\"http://www.citrusframework.org/schema/config\"/>";

        JAXBContext jaxbContext = jaxbHelper.createJAXBContextByPath(CONTEXT_PATHS);
        SchemaDefinition xsdSchema = jaxbHelper.unmarshal(jaxbContext, SchemaDefinition.class, xml);

        Assert.assertNotNull(xsdSchema);
        Assert.assertEquals(xsdSchema.getLocation(), "this <location /> should be escaped");
        Assert.assertEquals(xsdSchema.getId(), "123");

    }

    @Test(expectedExceptions = CitrusAdminRuntimeException.class, expectedExceptionsMessageRegExp = "Exception thrown during unmarshal")
    public void testUnmarshalFromFile_fileDoesNotExist() throws Exception {
        JAXBContext jaxbContext = jaxbHelper.createJAXBContextByPath(CONTEXT_PATHS);
        jaxbHelper.unmarshal(jaxbContext, SchemaDefinition.class, new File("someNonExistentFile.xml"));
    }

    @Test
    public void testMarshalToAndUnmarshalFromFile() throws Exception {
        JAXBContext jaxbContext = jaxbHelper.createJAXBContextByPath(CONTEXT_PATHS);

        SchemaRepositoryDefinition schemaRepository = new SchemaRepositoryDefinitionBuilder().withId("123").addSchemaReference("abc").addSchemaReference("def").build();

        File tmpXmlFile = File.createTempFile("marshalltest", ".xml");
        tmpXmlFile.deleteOnExit();

        jaxbHelper.marshal(jaxbContext, schemaRepository, tmpXmlFile);

        SchemaRepositoryDefinition loadedSchemaRepository = jaxbHelper.unmarshal(jaxbContext, SchemaRepositoryDefinition.class, tmpXmlFile);

        Assert.assertNotNull(loadedSchemaRepository);
        Assert.assertEquals(schemaRepository.getId(), loadedSchemaRepository.getId());
        Assert.assertEquals(schemaRepository.getSchemas().getRevesAndSchemas().size(), loadedSchemaRepository.getSchemas().getRevesAndSchemas().size());
        Assert.assertEquals(((SchemaRepositoryDefinition.Schemas.Ref)schemaRepository.getSchemas().getRevesAndSchemas().get(0)).getSchema(), ((SchemaRepositoryDefinition.Schemas.Ref)loadedSchemaRepository.getSchemas().getRevesAndSchemas().get(0)).getSchema());
        Assert.assertEquals(((SchemaRepositoryDefinition.Schemas.Ref)schemaRepository.getSchemas().getRevesAndSchemas().get(1)).getSchema(), ((SchemaRepositoryDefinition.Schemas.Ref)loadedSchemaRepository.getSchemas().getRevesAndSchemas().get(1)).getSchema());
    }
}
