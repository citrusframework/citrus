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

import org.citrusframework.message.MessageDirection;
import org.citrusframework.testng.AbstractBeanDefinitionParserTest;
import org.citrusframework.variable.dictionary.DataDictionary;
import org.citrusframework.variable.dictionary.xml.NodeMappingDataDictionary;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @since 2.0
 */
public class XmlDataDictionaryParserTest extends AbstractBeanDefinitionParserTest {

    @Test
    public void testDataDictionaryParser() throws Exception {
        Map<String, NodeMappingDataDictionary> dictionaries = beanDefinitionContext.getBeansOfType(NodeMappingDataDictionary.class);

        Assert.assertEquals(dictionaries.size(), 3L);

        NodeMappingDataDictionary dictionary = dictionaries.get("dataDictionary1");
        Assert.assertEquals(dictionary.getName(), "dataDictionary1");
        Assert.assertTrue(dictionary.isGlobalScope());
        Assert.assertEquals(dictionary.getPathMappingStrategy(), DataDictionary.PathMappingStrategy.EXACT);
        Assert.assertEquals(dictionary.getDirection(), MessageDirection.UNBOUND);
        Assert.assertNull(dictionary.getMappingFile());
        Assert.assertEquals(dictionary.getMappings().size(), 3L);

        Assert.assertEquals(dictionary.getMappings().get("node.name.1"), "value1");
        Assert.assertEquals(dictionary.getMappings().get("node.name.2"), "value2");
        Assert.assertEquals(dictionary.getMappings().get("node.name.3"), "value3");

        dictionary = dictionaries.get("dataDictionary2");
        Assert.assertEquals(dictionary.getName(), "dataDictionary2");
        Assert.assertFalse(dictionary.isGlobalScope());
        Assert.assertEquals(dictionary.getPathMappingStrategy(), DataDictionary.PathMappingStrategy.STARTS_WITH);
        Assert.assertEquals(dictionary.getDirection(), MessageDirection.INBOUND);
        Assert.assertNull(dictionary.getMappingFile());
        Assert.assertEquals(dictionary.getMappings().size(), 1L);

        Assert.assertEquals(dictionary.getMappings().get("node.name.1"), "value1");

        dictionary = dictionaries.get("dataDictionary3");
        Assert.assertEquals(dictionary.getName(), "dataDictionary3");
        Assert.assertTrue(dictionary.isGlobalScope());
        Assert.assertEquals(dictionary.getPathMappingStrategy(), DataDictionary.PathMappingStrategy.EXACT);
        Assert.assertNotNull(dictionary.getMappingFile());
        Assert.assertEquals(dictionary.getMappings().size(), 2L);

        Assert.assertEquals(dictionary.getMappings().get("node.name.1"), "value1");
        Assert.assertEquals(dictionary.getMappings().get("node.name.2"), "value2");
    }
}
