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

package org.citrusframework.json.config.xml;

import java.util.Map;

import org.citrusframework.json.JsonSchemaRepository;
import org.citrusframework.json.schema.SimpleJsonSchema;
import org.citrusframework.junit.jupiter.AbstractBeanDefinitionParserTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class JsonSchemaRepositoryParserTest extends AbstractBeanDefinitionParserTest {

    @Test
    public void testJsonSchemaRepositoryParser() {
        //GIVEN

        //WHEN
        Map<String, JsonSchemaRepository> schemaRepositories = beanDefinitionContext.getBeansOfType(JsonSchemaRepository.class);

        //THEN
        Assertions.assertEquals(2, schemaRepositories.size());

        // 1st schema repository
        JsonSchemaRepository schemaRepository = schemaRepositories.get("jsonSchemaRepository1");
        Assertions.assertNotNull(schemaRepository.getSchemas());
        Assertions.assertEquals(2, schemaRepository.getSchemas().size());
        Assertions.assertEquals(SimpleJsonSchema.class, schemaRepository.getSchemas().get(0).getClass());
        Assertions.assertEquals(SimpleJsonSchema.class, schemaRepository.getSchemas().get(1).getClass());
        Assertions.assertNotNull(schemaRepository.getLocations());
        Assertions.assertEquals(0, schemaRepository.getLocations().size());

        // 2nd schema repository
        schemaRepository = schemaRepositories.get("jsonSchemaRepository2");
        Assertions.assertNotNull(schemaRepository.getSchemas());
        Assertions.assertEquals(2, schemaRepository.getSchemas().size());
        Assertions.assertNotNull(schemaRepository.getLocations());
        Assertions.assertEquals(1, schemaRepository.getLocations().size());
        Assertions.assertEquals("classpath:org/citrusframework/validation/*", schemaRepository.getLocations().get(0));

        Assertions.assertTrue(beanDefinitionContext.containsBean("jsonSchema1"));
        Assertions.assertTrue(beanDefinitionContext.containsBean("jsonSchema2"));
    }
}
