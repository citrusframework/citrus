/*
 * Copyright 2006-2012 the original author or authors.
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

package com.consol.citrus.xml;

import org.springframework.xml.xsd.SimpleXsdSchema;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.consol.citrus.xml.schema.WsdlXsdSchema;

/**
 * @author Christoph Deppisch
 */
public class XsdSchemaRepositoryTest {
    
    @Test
    public void testResourceLocation() throws Exception {
        XsdSchemaRepository schemaRepository = new XsdSchemaRepository();
        
        schemaRepository.getLocations().add("classpath:com/consol/citrus/schema/citrus-config.xsd");
        
        schemaRepository.afterPropertiesSet();
        
        Assert.assertEquals(schemaRepository.getSchemas().size(), 1);
        Assert.assertEquals(schemaRepository.getSchemas().get(0).getClass(), SimpleXsdSchema.class);
    }
    
    @Test(expectedExceptions = { IllegalArgumentException.class })
    public void testUnknownLocation() throws Exception {
        XsdSchemaRepository schemaRepository = new XsdSchemaRepository();
        
        schemaRepository.getLocations().add("classpath:com/consol/citrus/unknown/unknown.xsd");
        
        schemaRepository.afterPropertiesSet();
    }
    
    @Test
    public void testResourceLocationPattern() throws Exception {
        XsdSchemaRepository schemaRepository = new XsdSchemaRepository();
        
        schemaRepository.getLocations().add("classpath:com/consol/citrus/schema/*.xsd");
        
        schemaRepository.afterPropertiesSet();
        
        Assert.assertEquals(schemaRepository.getSchemas().size(), 4);
        Assert.assertEquals(schemaRepository.getSchemas().get(0).getClass(), SimpleXsdSchema.class);
        Assert.assertEquals(schemaRepository.getSchemas().get(1).getClass(), SimpleXsdSchema.class);
        Assert.assertEquals(schemaRepository.getSchemas().get(2).getClass(), SimpleXsdSchema.class);
        Assert.assertEquals(schemaRepository.getSchemas().get(3).getClass(), SimpleXsdSchema.class);
    }
    
    @Test
    public void testResourceLocationPatternNothingFound() throws Exception {
        XsdSchemaRepository schemaRepository = new XsdSchemaRepository();
        
        schemaRepository.getLocations().add("classpath:com/consol/citrus/*.xsd");
        
        schemaRepository.afterPropertiesSet();
        
        Assert.assertEquals(schemaRepository.getSchemas().size(), 0);
    }
    
    @Test
    public void testResourceLocationPatternWithExclusion() throws Exception {
        XsdSchemaRepository schemaRepository = new XsdSchemaRepository();
        schemaRepository.getLocations().add("classpath:com/consol/citrus/validation/*");
        
        schemaRepository.afterPropertiesSet();
        
        Assert.assertEquals(schemaRepository.getSchemas().size(), 15);

        schemaRepository = new XsdSchemaRepository();
        schemaRepository.getLocations().add("classpath:com/consol/citrus/validation/*.xsd");

        schemaRepository.afterPropertiesSet();

        Assert.assertEquals(schemaRepository.getSchemas().size(), 6);
        Assert.assertEquals(schemaRepository.getSchemas().get(0).getClass(), SimpleXsdSchema.class);
        Assert.assertEquals(schemaRepository.getSchemas().get(1).getClass(), SimpleXsdSchema.class);
        Assert.assertEquals(schemaRepository.getSchemas().get(2).getClass(), SimpleXsdSchema.class);

        schemaRepository = new XsdSchemaRepository();
        schemaRepository.getLocations().add("classpath:com/consol/citrus/validation/*.wsdl");

        schemaRepository.afterPropertiesSet();

        Assert.assertEquals(schemaRepository.getSchemas().size(), 9);
        Assert.assertEquals(schemaRepository.getSchemas().get(0).getClass(), WsdlXsdSchema.class);
        Assert.assertEquals(schemaRepository.getSchemas().get(1).getClass(), WsdlXsdSchema.class);
        Assert.assertEquals(schemaRepository.getSchemas().get(2).getClass(), WsdlXsdSchema.class);
        Assert.assertEquals(schemaRepository.getSchemas().get(3).getClass(), WsdlXsdSchema.class);
        Assert.assertEquals(schemaRepository.getSchemas().get(4).getClass(), WsdlXsdSchema.class);
        Assert.assertEquals(schemaRepository.getSchemas().get(5).getClass(), WsdlXsdSchema.class);
        Assert.assertEquals(schemaRepository.getSchemas().get(6).getClass(), WsdlXsdSchema.class);
        Assert.assertEquals(schemaRepository.getSchemas().get(7).getClass(), WsdlXsdSchema.class);
        Assert.assertEquals(schemaRepository.getSchemas().get(8).getClass(), WsdlXsdSchema.class);
    }
    
    @Test
    public void testWsdlResourceLocation() throws Exception {
        XsdSchemaRepository schemaRepository = new XsdSchemaRepository();
        
        schemaRepository.getLocations().add("classpath:com/consol/citrus/xml/BookStore.wsdl");
        
        schemaRepository.afterPropertiesSet();
        
        Assert.assertEquals(schemaRepository.getSchemas().size(), 1);
        Assert.assertEquals(schemaRepository.getSchemas().get(0).getClass(), WsdlXsdSchema.class);
    }

    @Test
    public void testDefaultCitrusSchemas() throws Exception {
        XsdSchemaRepository schemaRepository = new XsdSchemaRepository();

        schemaRepository.addCitrusSchema("citrus-unknown-config");

        Assert.assertEquals(schemaRepository.getSchemas().size(), 0);

        schemaRepository.addCitrusSchema("citrus-config");

        Assert.assertEquals(schemaRepository.getSchemas().size(), 1);
        Assert.assertEquals(schemaRepository.getSchemas().get(0).getClass(), SimpleXsdSchema.class);
    }
}
