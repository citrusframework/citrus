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

package org.citrusframework.config.xml;

import java.util.Map;

import org.citrusframework.json.JsonSchemaRepository;
import org.citrusframework.json.schema.SimpleJsonSchema;
import org.citrusframework.testng.AbstractBeanDefinitionParserTest;
import org.testng.Assert;
import org.testng.annotations.Test;

public class JsonSchemaRepositoryParserTest extends AbstractBeanDefinitionParserTest {

    @Test
    public void testJsonSchemaRepositoryParser() {
        //GIVEN

        //WHEN
        Map<String, JsonSchemaRepository> schemaRepositories = beanDefinitionContext.getBeansOfType(JsonSchemaRepository.class);

        //THEN
        Assert.assertEquals(schemaRepositories.size(), 2);

        // 1st schema repository
        JsonSchemaRepository schemaRepository = schemaRepositories.get("jsonSchemaRepository1");
        Assert.assertNotNull(schemaRepository.getSchemas());
        Assert.assertEquals(schemaRepository.getSchemas().size(), 2);
        Assert.assertEquals(schemaRepository.getSchemas().get(0).getClass(), SimpleJsonSchema.class);
        Assert.assertEquals(schemaRepository.getSchemas().get(1).getClass(), SimpleJsonSchema.class);
        Assert.assertNotNull(schemaRepository.getLocations());
        Assert.assertEquals(schemaRepository.getLocations().size(), 0);

        // 2nd schema repository
        schemaRepository = schemaRepositories.get("jsonSchemaRepository2");
        Assert.assertNotNull(schemaRepository.getSchemas());
        Assert.assertEquals(schemaRepository.getSchemas().size(), 2);
        Assert.assertNotNull(schemaRepository.getLocations());
        Assert.assertEquals(schemaRepository.getLocations().size(), 1);
        Assert.assertEquals(schemaRepository.getLocations().get(0), "classpath:org/citrusframework/validation/*");

        Assert.assertTrue(beanDefinitionContext.containsBean("jsonSchema1"));
        Assert.assertTrue(beanDefinitionContext.containsBean("jsonSchema2"));
    }
}
