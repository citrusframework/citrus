/*
 * Copyright 2006-2014 the original author or authors.
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

import java.util.HashMap;

import org.citrusframework.config.util.BeanDefinitionParserUtils;
import org.citrusframework.variable.dictionary.DataDictionary;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

/**
 * @author Christoph Deppisch
 * @since 2.0
 */
public abstract class AbstractDataDictionaryParser implements BeanDefinitionParser {

    @Override
    public BeanDefinition parse(Element element, ParserContext parserContext) {
        BeanDefinitionBuilder builder = BeanDefinitionBuilder.rootBeanDefinition(getDictionaryClass());

        builder.addPropertyValue("name", element.getAttribute("id"));

        BeanDefinitionParserUtils.setPropertyValue(builder, element.getAttribute("global-scope"), "globalScope");
        BeanDefinitionParserUtils.setPropertyValue(builder, element.getAttribute("mapping-strategy"), "pathMappingStrategy");
        BeanDefinitionParserUtils.setPropertyValue(builder, element.getAttribute("direction"), "direction");

        Element mappings = DomUtils.getChildElementByTagName(element, "mappings");
        if (mappings != null) {
            parseMappingDefinitions(builder, mappings);
        }

        Element mappingFile = DomUtils.getChildElementByTagName(element, "mapping-file");
        if (mappingFile != null) {
            BeanDefinitionParserUtils.setPropertyValue(builder, mappingFile.getAttribute("path"), "mappingFile");
        }

        parserContext.getRegistry().registerBeanDefinition(element.getAttribute("id"), builder.getBeanDefinition());

        return null;
    }

    /**
     * Parses all mapping definitions and adds those to the bean definition
     * builder as property value.
     * @param builder the target bean definition builder.
     * @param element the source element.
     */
    private void parseMappingDefinitions(BeanDefinitionBuilder builder, Element element) {
        HashMap<String, String> mappings = new HashMap<String, String>();
        for (Element matcher : DomUtils.getChildElementsByTagName(element, "mapping")) {
            mappings.put(matcher.getAttribute("path"), matcher.getAttribute("value"));
        }

        if (!mappings.isEmpty()) {
            builder.addPropertyValue("mappings", mappings);
        }
    }

    /**
     * Subclasses provide suite container class.
     * @return
     */
    protected abstract Class<? extends DataDictionary<?>> getDictionaryClass();
}
