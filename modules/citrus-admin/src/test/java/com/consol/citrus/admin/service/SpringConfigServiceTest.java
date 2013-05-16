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

package com.consol.citrus.admin.service;

import java.io.*;
import java.util.List;

import org.springframework.core.io.ClassPathResource;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.consol.citrus.admin.util.JAXBHelperImpl;
import com.consol.citrus.model.config.core.*;
import com.consol.citrus.util.FileUtils;

/**
 * @author Christoph Deppisch
 */
public class SpringConfigServiceTest {
    private SpringConfigService springBeanConfigService = new SpringConfigService();

    @BeforeMethod
    public void beforeMethod() {
        springBeanConfigService.jaxbHelper = new JAXBHelperImpl();
        springBeanConfigService.initJaxbContext();
    }

    @Test
    public void testAddBeanDefinition() throws Exception {
        XsdSchema xsdSchema1 = new XsdSchemaBuilder().withId("1").setLocation("l1").build();
        XsdSchema xsdSchema2 = new XsdSchemaBuilder().withId("2").setLocation("l2").build();

        SchemaRepository schemaRepository = new SchemaRepositoryBuilder().withId("x").addSchema("1").addSchema("2").build();

        File tempFile = createTempContextFile("citrus-context-add");
        
        springBeanConfigService.addBeanDefinition(tempFile, xsdSchema1);
        springBeanConfigService.addBeanDefinition(tempFile, xsdSchema2);
        springBeanConfigService.addBeanDefinition(tempFile, schemaRepository);
        
        String result = FileUtils.readToString(new FileInputStream(tempFile));
        
        Assert.assertTrue(result.contains("<schema-repository xmlns=\"http://www.citrusframework.org/schema/config\" id=\"x\">"));
    }
    
    @Test
    public void testRemoveBeanDefinition() throws Exception {
        File tempFile = createTempContextFile("citrus-context-remove");
        
        springBeanConfigService.removeBeanDefinition(tempFile, "deleteMe");
        springBeanConfigService.removeBeanDefinition(tempFile, "deleteMeName");
        
        springBeanConfigService.removeBeanDefinition(tempFile, "helloSchema");
        
        String result = FileUtils.readToString(new FileInputStream(tempFile));
        
        Assert.assertTrue(result.contains("id=\"preserveMe\""));
        Assert.assertTrue(result.contains("name=\"preserveMeName\""));
        
        Assert.assertFalse(result.contains("<bean id=\"deleteMe\""));
        Assert.assertFalse(result.contains("<bean name=\"deleteMeName\""));
    }
    
    @Test
    public void testUpdateBeanDefinition() throws Exception {
        File tempFile = createTempContextFile("citrus-context-update");
        
        XsdSchema helloSchema = new XsdSchemaBuilder().withId("helloSchema").setLocation("newLocation").build();
        
        springBeanConfigService.updateBeanDefinition(tempFile, "helloSchema", helloSchema);
        
        String result = FileUtils.readToString(new FileInputStream(tempFile));
        
        Assert.assertTrue(result.contains("id=\"helloSchema\""));
        Assert.assertTrue(result.contains("location=\"newLocation\""));
    }
    
    @Test
    public void testGetBeanDefinition() throws Exception {
        File tempFile = createTempContextFile("citrus-context-find");
        
        XsdSchema schema = springBeanConfigService.getBeanDefinition(tempFile, "helloSchema", XsdSchema.class);
        
        Assert.assertEquals(schema.getId(), "helloSchema");
        Assert.assertEquals(schema.getLocation(), "classpath:com/consol/citrus/demo/sayHello.xsd");
        
        schema = springBeanConfigService.getBeanDefinition(tempFile, "helloSchemaExtended", XsdSchema.class);
        
        Assert.assertEquals(schema.getId(), "helloSchemaExtended");
        Assert.assertEquals(schema.getLocation(), "classpath:com/consol/citrus/demo/sayHelloExtended.xsd");
    }
    
    @Test
    public void testGetBeanDefinitions() throws Exception {
        File tempFile = createTempContextFile("citrus-context-find");
        
        List<XsdSchema> schemas = springBeanConfigService.getBeanDefinitions(tempFile, XsdSchema.class);
        
        Assert.assertEquals(schemas.size(), 2);
        Assert.assertEquals(schemas.get(0).getId(), "helloSchema");
        Assert.assertEquals(schemas.get(0).getLocation(), "classpath:com/consol/citrus/demo/sayHello.xsd");
        Assert.assertEquals(schemas.get(1).getId(), "helloSchemaExtended");
        Assert.assertEquals(schemas.get(1).getLocation(), "classpath:com/consol/citrus/demo/sayHelloExtended.xsd");
    }
    
    /**
     * Creates a temporary file in operating system and writes template content to file.
     * @param templateName
     * @return
     */
    private File createTempContextFile(String templateName) throws IOException {
        FileWriter writer = null;
        File tempFile;
        
        try {
            tempFile = File.createTempFile(templateName, ".xml");
            
            writer = new FileWriter(tempFile);
            writer.write(FileUtils.readToString(new ClassPathResource(templateName + ".xml", SpringConfigService.class)));
        } finally {
            if (writer != null) {
                writer.flush();
                writer.close();
            }
        }
        
        return tempFile;
    }

}
