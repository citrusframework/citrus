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

import com.consol.citrus.admin.jaxb.JAXBHelper;
import com.consol.citrus.model.config.core.*;
import com.consol.citrus.util.FileUtils;
import org.springframework.core.io.ClassPathResource;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.*;
import java.util.List;

/**
 * @author Christoph Deppisch
 */
public class SpringBeanServiceTest {
    private SpringBeanService springBeanConfigService = new SpringBeanService();

    @BeforeMethod
    public void beforeMethod() {
        springBeanConfigService.jaxbHelper = new JAXBHelper();
        springBeanConfigService.init();
    }

    @Test
    public void testAddBeanDefinition() throws Exception {
        SchemaDefinition xsdSchema1 = new SchemaDefinitionBuilder().withId("1").withLocation("l1").build();
        SchemaDefinition xsdSchema2 = new SchemaDefinitionBuilder().withId("2").withLocation("l2").build();

        SchemaRepositoryDefinition schemaRepository = new SchemaRepositoryDefinitionBuilder().withId("x").addSchemaReference("1").addSchemaReference("2").build();

        File tempFile = createTempContextFile("citrus-context-add");
        
        springBeanConfigService.addBeanDefinition(tempFile, xsdSchema1);
        springBeanConfigService.addBeanDefinition(tempFile, xsdSchema2);
        springBeanConfigService.addBeanDefinition(tempFile, schemaRepository);

        String result = FileUtils.readToString(new FileInputStream(tempFile));

        Assert.assertTrue(result.contains("<citrus:schema id=\"1\" location=\"l1\"/>"), "Failed to validate " + result);
        Assert.assertTrue(result.contains("<citrus:schema id=\"2\" location=\"l2\"/>"), "Failed to validate " + result);
        Assert.assertTrue(result.contains("<citrus:schema-repository id=\"x\">"), "Failed to validate " + result);
    }
    
    @Test
    public void testRemoveBeanDefinition() throws Exception {
        File tempFile = createTempContextFile("citrus-context-remove");
        
        springBeanConfigService.removeBeanDefinition(tempFile, "deleteMe");
        springBeanConfigService.removeBeanDefinition(tempFile, "deleteMeName");
        
        springBeanConfigService.removeBeanDefinition(tempFile, "helloSchema");
        
        String result = FileUtils.readToString(new FileInputStream(tempFile));
        
        Assert.assertTrue(result.contains("id=\"preserveMe\""), "Failed to validate " + result);
        Assert.assertTrue(result.contains("name=\"preserveMeName\""), "Failed to validate " + result);
        
        Assert.assertFalse(result.contains("<bean id=\"deleteMe\""), "Failed to validate " + result);
        Assert.assertFalse(result.contains("<bean name=\"deleteMeName\""), "Failed to validate " + result);
    }
    
    @Test
    public void testUpdateBeanDefinition() throws Exception {
        File tempFile = createTempContextFile("citrus-context-update");

        SchemaDefinition helloSchema = new SchemaDefinitionBuilder().withId("helloSchema").withLocation("newLocation").build();
        
        springBeanConfigService.updateBeanDefinition(tempFile, "helloSchema", helloSchema);
        
        String result = FileUtils.readToString(new FileInputStream(tempFile));

        Assert.assertTrue(result.contains("<citrus:schema id=\"helloSchema\" location=\"newLocation\"/>"), "Failed to validate " + result);
    }
    
    @Test
    public void testGetBeanDefinition() throws Exception {
        File tempFile = createTempContextFile("citrus-context-find");
        
        SchemaDefinition schema = springBeanConfigService.getBeanDefinition(tempFile, "helloSchema", SchemaDefinition.class);
        
        Assert.assertEquals(schema.getId(), "helloSchema");
        Assert.assertEquals(schema.getLocation(), "classpath:com/consol/citrus/demo/sayHello.xsd");
        
        schema = springBeanConfigService.getBeanDefinition(tempFile, "helloSchemaExtended", SchemaDefinition.class);
        
        Assert.assertEquals(schema.getId(), "helloSchemaExtended");
        Assert.assertEquals(schema.getLocation(), "classpath:com/consol/citrus/demo/sayHelloExtended.xsd");
    }
    
    @Test
    public void testGetBeanDefinitions() throws Exception {
        File tempFile = createTempContextFile("citrus-context-find");
        
        List<SchemaDefinition> schemas = springBeanConfigService.getBeanDefinitions(tempFile, SchemaDefinition.class);
        
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
            writer.write(FileUtils.readToString(new ClassPathResource(templateName + ".xml", SpringBeanService.class)));
        } finally {
            if (writer != null) {
                writer.flush();
                writer.close();
            }
        }
        
        return tempFile;
    }

}
