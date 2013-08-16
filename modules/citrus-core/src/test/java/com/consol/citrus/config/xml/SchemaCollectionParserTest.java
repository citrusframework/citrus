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

package com.consol.citrus.config.xml;

import com.consol.citrus.testng.AbstractBeanDefinitionParserTest;
import com.consol.citrus.xml.schema.MultiResourceXsdSchema;
import org.springframework.core.io.ClassPathResource;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Map;

/**
 * @author Christoph Deppisch
 */
public class SchemaCollectionParserTest extends AbstractBeanDefinitionParserTest {

    @Test
    public void testSchemaRepositoryParser() {
        Map<String, MultiResourceXsdSchema> schemaCollections = beanDefinitionContext.getBeansOfType(MultiResourceXsdSchema.class);
        
        Assert.assertEquals(schemaCollections.size(), 1);
        
        // 1st schema repository
        MultiResourceXsdSchema schema = schemaCollections.get("schemaCollection1");
        Assert.assertNotNull(schema.getSchemas());
        Assert.assertEquals(schema.getSchemas().length, 2);
        Assert.assertEquals(schema.getSchemas()[0].getClass(), ClassPathResource.class);
        Assert.assertEquals(schema.getSchemas()[1].getClass(), ClassPathResource.class);
    }
}
