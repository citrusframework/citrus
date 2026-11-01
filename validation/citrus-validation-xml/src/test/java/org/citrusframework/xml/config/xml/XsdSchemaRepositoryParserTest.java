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

package org.citrusframework.xml.config.xml;

import java.util.Map;

import org.citrusframework.junit.jupiter.AbstractBeanDefinitionParserTest;
import org.citrusframework.xml.schema.XsdSchemaRepository;
import org.citrusframework.xml.schema.RootQNameSchemaMappingStrategy;
import org.citrusframework.xml.schema.TargetNamespaceSchemaMappingStrategy;
import org.citrusframework.xml.schema.WsdlXsdSchema;
import org.citrusframework.xml.schema.XsdSchemaCollection;
import org.springframework.xml.xsd.SimpleXsdSchema;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class XsdSchemaRepositoryParserTest extends AbstractBeanDefinitionParserTest {

    @Test
    public void testSchemaRepositoryParser() {
        Map<String, XsdSchemaRepository> schemaRepositories = beanDefinitionContext.getBeansOfType(XsdSchemaRepository.class);

        Assertions.assertEquals(5, schemaRepositories.size());

        // 1st schema repository
        XsdSchemaRepository schemaRepository = schemaRepositories.get("schemaRepository1");
        Assertions.assertEquals(TargetNamespaceSchemaMappingStrategy.class, schemaRepository.getSchemaMappingStrategy().getClass());
        Assertions.assertNotNull(schemaRepository.getSchemas());
        Assertions.assertEquals(5, schemaRepository.getSchemas().size());
        Assertions.assertEquals(SimpleXsdSchema.class, schemaRepository.getSchemas().get(0).getClass());
        Assertions.assertEquals(WsdlXsdSchema.class, schemaRepository.getSchemas().get(1).getClass());
        Assertions.assertEquals(SimpleXsdSchema.class, schemaRepository.getSchemas().get(2).getClass());
        Assertions.assertEquals(WsdlXsdSchema.class, schemaRepository.getSchemas().get(3).getClass());
        Assertions.assertEquals(XsdSchemaCollection.class, schemaRepository.getSchemas().get(4).getClass());
        Assertions.assertNotNull(schemaRepository.getLocations());
        Assertions.assertEquals(0, schemaRepository.getLocations().size());

        // 2nd schema repository
        schemaRepository = schemaRepositories.get("schemaRepository2");
        Assertions.assertNotNull(schemaRepository.getSchemas());
        Assertions.assertEquals(17, schemaRepository.getSchemas().size());
        Assertions.assertNotNull(schemaRepository.getLocations());
        Assertions.assertEquals(1, schemaRepository.getLocations().size());
        Assertions.assertEquals("classpath:org/citrusframework/validation/*", schemaRepository.getLocations().get(0));

        // 3rd schema repository
        schemaRepository = schemaRepositories.get("schemaRepository3");
        Assertions.assertEquals(RootQNameSchemaMappingStrategy.class, schemaRepository.getSchemaMappingStrategy().getClass());

        // 4th schema repository
        schemaRepository = schemaRepositories.get("xmlSchemaRepository");
        Assertions.assertEquals(TargetNamespaceSchemaMappingStrategy.class, schemaRepository.getSchemaMappingStrategy().getClass());
        Assertions.assertNotNull(schemaRepository.getSchemas());
        Assertions.assertEquals(1, schemaRepository.getSchemas().size());

        // 5th schema repository
        schemaRepository = schemaRepositories.get("testSchemaRepositoryBean");
        Assertions.assertEquals(TargetNamespaceSchemaMappingStrategy.class, schemaRepository.getSchemaMappingStrategy().getClass());
        Assertions.assertNotNull(schemaRepository.getSchemas());
        Assertions.assertEquals(1, schemaRepository.getSchemas().size());

        Assertions.assertTrue(beanDefinitionContext.containsBean("schema1"));
        Assertions.assertTrue(beanDefinitionContext.containsBean("schema2"));
        Assertions.assertTrue(beanDefinitionContext.containsBean("wsdl1"));
        Assertions.assertTrue(beanDefinitionContext.containsBean("wsdl2"));
        Assertions.assertTrue(beanDefinitionContext.containsBean("schemaCollection1"));
    }
}
