/*
 * Copyright 2006-2010 the original author or authors.
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

package org.citrusframework.config.xml;

import java.util.Map;

import org.citrusframework.testng.AbstractBeanDefinitionParserTest;
import org.citrusframework.xml.XsdSchemaRepository;
import org.citrusframework.xml.schema.RootQNameSchemaMappingStrategy;
import org.citrusframework.xml.schema.TargetNamespaceSchemaMappingStrategy;
import org.citrusframework.xml.schema.WsdlXsdSchema;
import org.citrusframework.xml.schema.XsdSchemaCollection;
import org.springframework.xml.xsd.SimpleXsdSchema;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 */
public class XsdSchemaRepositoryParserTest extends AbstractBeanDefinitionParserTest {

    @Test
    public void testSchemaRepositoryParser() {
        Map<String, XsdSchemaRepository> schemaRepositories = beanDefinitionContext.getBeansOfType(XsdSchemaRepository.class);

        Assert.assertEquals(schemaRepositories.size(), 5);

        // 1st schema repository
        XsdSchemaRepository schemaRepository = schemaRepositories.get("schemaRepository1");
        Assert.assertEquals(schemaRepository.getSchemaMappingStrategy().getClass(), TargetNamespaceSchemaMappingStrategy.class);
        Assert.assertNotNull(schemaRepository.getSchemas());
        Assert.assertEquals(schemaRepository.getSchemas().size(), 5);
        Assert.assertEquals(schemaRepository.getSchemas().get(0).getClass(), SimpleXsdSchema.class);
        Assert.assertEquals(schemaRepository.getSchemas().get(1).getClass(), WsdlXsdSchema.class);
        Assert.assertEquals(schemaRepository.getSchemas().get(2).getClass(), SimpleXsdSchema.class);
        Assert.assertEquals(schemaRepository.getSchemas().get(3).getClass(), WsdlXsdSchema.class);
        Assert.assertEquals(schemaRepository.getSchemas().get(4).getClass(), XsdSchemaCollection.class);
        Assert.assertNotNull(schemaRepository.getLocations());
        Assert.assertEquals(schemaRepository.getLocations().size(), 0);

        // 2nd schema repository
        schemaRepository = schemaRepositories.get("schemaRepository2");
        Assert.assertNotNull(schemaRepository.getSchemas());
        Assert.assertEquals(schemaRepository.getSchemas().size(), 15);
        Assert.assertNotNull(schemaRepository.getLocations());
        Assert.assertEquals(schemaRepository.getLocations().size(), 1);
        Assert.assertEquals(schemaRepository.getLocations().get(0), "classpath:org/citrusframework/validation/*");

        // 3rd schema repository
        schemaRepository = schemaRepositories.get("schemaRepository3");
        Assert.assertEquals(schemaRepository.getSchemaMappingStrategy().getClass(), RootQNameSchemaMappingStrategy.class);

        // 4th schema repository
        schemaRepository = schemaRepositories.get("xmlSchemaRepository");
        Assert.assertEquals(schemaRepository.getSchemaMappingStrategy().getClass(), TargetNamespaceSchemaMappingStrategy.class);
        Assert.assertNotNull(schemaRepository.getSchemas());
        Assert.assertEquals(schemaRepository.getSchemas().size(), 1);

        // 5th schema repository
        schemaRepository = schemaRepositories.get("testSchemaRepositoryBean");
        Assert.assertEquals(schemaRepository.getSchemaMappingStrategy().getClass(), TargetNamespaceSchemaMappingStrategy.class);
        Assert.assertNotNull(schemaRepository.getSchemas());
        Assert.assertEquals(schemaRepository.getSchemas().size(), 1);

        Assert.assertTrue(beanDefinitionContext.containsBean("schema1"));
        Assert.assertTrue(beanDefinitionContext.containsBean("schema2"));
        Assert.assertTrue(beanDefinitionContext.containsBean("wsdl1"));
        Assert.assertTrue(beanDefinitionContext.containsBean("wsdl2"));
        Assert.assertTrue(beanDefinitionContext.containsBean("schemaCollection1"));
    }
}
