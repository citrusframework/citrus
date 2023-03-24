/*
 * Copyright 2006-2013 the original author or authors.
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

import java.util.ArrayList;
import java.util.List;

import org.citrusframework.xml.schema.XsdSchemaCollection;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

/**
 * Bean definition parser for schema configuration.
 *
 * @author Christoph Deppisch
 * @since 1.3.1
 */
public class SchemaCollectionParser implements BeanDefinitionParser {

    @Override
    public BeanDefinition parse(Element element, ParserContext parserContext) {
        BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(XsdSchemaCollection.class);

        List<String> schemas = new ArrayList<>();
        Element schemasElement = DomUtils.getChildElementByTagName(element, "schemas");
        if (schemasElement != null) {
            List<Element> schemaElements = DomUtils.getChildElementsByTagName(schemasElement, "schema");
            for (Element schemaElement : schemaElements) {
                schemas.add(schemaElement.getAttribute("location"));
            }
        }

        builder.addPropertyValue("schemas", schemas);
        parserContext.getRegistry().registerBeanDefinition(element.getAttribute("id"), builder.getBeanDefinition());

        return null;
    }
}
