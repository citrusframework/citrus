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
import org.citrusframework.xml.schema.XsdSchemaCollection;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 */
public class SchemaCollectionParserTest extends AbstractBeanDefinitionParserTest {

    @Test
    public void testSchemaRepositoryParser() {
        Map<String, XsdSchemaCollection> schemaCollections = beanDefinitionContext.getBeansOfType(XsdSchemaCollection.class);

        Assert.assertEquals(schemaCollections.size(), 1);

        // 1st schema repository
        XsdSchemaCollection schema = schemaCollections.get("schemaCollection1");
        Assert.assertNotNull(schema.getSchemas());
        Assert.assertEquals(schema.getSchemas().size(), 2);
        Assert.assertEquals(schema.getSchemas().get(0), "classpath:org/citrusframework/validation/test.xsd");
        Assert.assertEquals(schema.getSchemas().get(1), "classpath:org/citrusframework/validation/sample.xsd");
    }
}
