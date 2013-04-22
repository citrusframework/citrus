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

package com.consol.citrus.admin.util;

import com.consol.citrus.model.config.core.SchemaRepository;
import com.consol.citrus.model.config.core.SchemaRepositoryBuilder;
import com.consol.citrus.model.config.core.XsdSchema;
import com.consol.citrus.model.config.core.XsdSchemaBuilder;
import com.consol.citrus.model.spring.beans.Beans;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.util.List;

/**
 * @author Martin.Maher@consol.de
 * @since 2013.04.20
 */
public class CitrusConfigHelperImplTest {
    private CitrusConfigHelperImpl citrusConfigHelper = new CitrusConfigHelperImpl();

    @BeforeMethod
    private void beforeMethod() {
        citrusConfigHelper.jaxbHelper = new JAXBHelperImpl();
    }

    @Test
    public void testGetConfigElementsByType_multipleMatches() throws Exception {
        Beans beans = getExampleCitrusConfig();
        List<XsdSchema> xsdSchemas = citrusConfigHelper.getConfigElementsByType(beans, XsdSchema.class);

        Assert.assertNotNull(xsdSchemas);
        Assert.assertEquals(xsdSchemas.size(), 2);
    }

    @Test
    public void testGetConfigElementsByType_noMatch() throws Exception {
        Beans beans = new Beans();
        List<XsdSchema> xsdSchemas = citrusConfigHelper.getConfigElementsByType(beans, XsdSchema.class);

        Assert.assertNotNull(xsdSchemas);
        Assert.assertEquals(xsdSchemas.size(), 0);
    }

    @Test
    public void testGetConfigElementsByType_singleMatch() throws Exception {
        Beans beans = getExampleCitrusConfig();
        List<SchemaRepository> schemaRepositories = citrusConfigHelper.getConfigElementsByType(beans, SchemaRepository.class);

        Assert.assertNotNull(schemaRepositories);
        Assert.assertEquals(schemaRepositories.size(), 1);
    }

    @Test
    public void testPersistAndLoadCitrusConfig() throws Exception {
        File tmpXmlFile = File.createTempFile("persistAndLoadTest", ".xml");
        tmpXmlFile.deleteOnExit();

        Beans beans = getExampleCitrusConfig();

        // init
        citrusConfigHelper.initJAXBContext();

        // persist config
        citrusConfigHelper.persistCitrusConfig(tmpXmlFile, beans);

        // load config
        Beans loaderBeans = citrusConfigHelper.loadCitrusConfig(tmpXmlFile);

        Assert.assertNotNull(loaderBeans);
        Assert.assertEquals(beans.getImportsAndAliasAndBeen().size(), loaderBeans.getImportsAndAliasAndBeen().size());
        Assert.assertEquals(((XsdSchema) beans.getImportsAndAliasAndBeen().get(0)).getId(), ((XsdSchema) loaderBeans.getImportsAndAliasAndBeen().get(0)).getId());
    }


    private Beans getExampleCitrusConfig() {
        XsdSchema xsdSchema1 = new XsdSchemaBuilder().withId("1").setLocation("l1").build();
        XsdSchema xsdSchema2 = new XsdSchemaBuilder().withId("2").setLocation("l2").build();

        SchemaRepository schemaRepository = new SchemaRepositoryBuilder().withId("x").addSchema("1").addSchema("2").build();

        Beans beans = new Beans();
        beans.getImportsAndAliasAndBeen().add(xsdSchema1);
        beans.getImportsAndAliasAndBeen().add(xsdSchema2);
        beans.getImportsAndAliasAndBeen().add(schemaRepository);

        return beans;
    }
}
