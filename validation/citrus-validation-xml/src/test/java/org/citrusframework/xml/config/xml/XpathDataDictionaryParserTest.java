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

import org.citrusframework.message.MessageDirection;
import org.citrusframework.junit.jupiter.AbstractBeanDefinitionParserTest;
import org.citrusframework.variable.dictionary.DataDictionary;
import org.citrusframework.xml.variable.dictionary.XpathMappingDataDictionary;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @since 2.0
 */
public class XpathDataDictionaryParserTest extends AbstractBeanDefinitionParserTest {

    @Test
    public void testDataDictionaryParser() {
        Map<String, XpathMappingDataDictionary> dictionaries = beanDefinitionContext.getBeansOfType(XpathMappingDataDictionary.class);

        Assertions.assertEquals(3L, dictionaries.size());

        XpathMappingDataDictionary dictionary = dictionaries.get("dataDictionary1");
        Assertions.assertEquals("dataDictionary1", dictionary.getName());
        Assertions.assertTrue(dictionary.isGlobalScope());
        Assertions.assertEquals(DataDictionary.PathMappingStrategy.EXACT, dictionary.getPathMappingStrategy());
        Assertions.assertEquals(MessageDirection.UNBOUND, dictionary.getDirection());
        Assertions.assertNull(dictionary.getMappingFile());
        Assertions.assertEquals(3L, dictionary.getMappings().size());

        Assertions.assertEquals("value1", dictionary.getMappings().get("//root/element/1"));
        Assertions.assertEquals("value2", dictionary.getMappings().get("//root/element/2"));
        Assertions.assertEquals("value3", dictionary.getMappings().get("//root/element/3"));

        dictionary = dictionaries.get("dataDictionary2");
        Assertions.assertEquals("dataDictionary2", dictionary.getName());
        Assertions.assertFalse(dictionary.isGlobalScope());
        Assertions.assertEquals(DataDictionary.PathMappingStrategy.STARTS_WITH, dictionary.getPathMappingStrategy());
        Assertions.assertEquals(MessageDirection.INBOUND, dictionary.getDirection());
        Assertions.assertNull(dictionary.getMappingFile());
        Assertions.assertEquals(1L, dictionary.getMappings().size());

        Assertions.assertEquals("value1", dictionary.getMappings().get("//root/element/1"));

        dictionary = dictionaries.get("dataDictionary3");
        Assertions.assertEquals("dataDictionary3", dictionary.getName());
        Assertions.assertTrue(dictionary.isGlobalScope());
        Assertions.assertEquals(DataDictionary.PathMappingStrategy.EXACT, dictionary.getPathMappingStrategy());
        Assertions.assertNotNull(dictionary.getMappingFile());
        Assertions.assertEquals(2L, dictionary.getMappings().size());

        Assertions.assertEquals("value1", dictionary.getMappings().get("//root/element/1"));
        Assertions.assertEquals("value2", dictionary.getMappings().get("//root/element/2"));
    }
}
