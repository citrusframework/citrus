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
import org.citrusframework.xml.schema.XsdSchemaCollection;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class SchemaCollectionParserTest extends AbstractBeanDefinitionParserTest {

    @Test
    public void testSchemaRepositoryParser() {
        Map<String, XsdSchemaCollection> schemaCollections = beanDefinitionContext.getBeansOfType(XsdSchemaCollection.class);

        Assertions.assertEquals(1, schemaCollections.size());

        // 1st schema repository
        XsdSchemaCollection schema = schemaCollections.get("schemaCollection1");
        Assertions.assertNotNull(schema.getSchemas());
        Assertions.assertEquals(2, schema.getSchemas().size());
        Assertions.assertEquals("classpath:org/citrusframework/validation/test.xsd", schema.getSchemas().get(0));
        Assertions.assertEquals("classpath:org/citrusframework/validation/sample.xsd", schema.getSchemas().get(1));
    }
}
