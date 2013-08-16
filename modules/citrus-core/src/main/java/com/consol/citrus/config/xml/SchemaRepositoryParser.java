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

package com.consol.citrus.config.xml;

import com.consol.citrus.config.util.BeanDefinitionParserUtils;
import com.consol.citrus.xml.XsdSchemaRepository;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.ManagedList;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.List;

/**
 * Bean definition parser for schema-repository configuration.
 *
 * @author Martin.Maher@consol.de
 * @since 1.3.1
 */
public class SchemaRepositoryParser implements BeanDefinitionParser {
    public BeanDefinition parse(Element element, ParserContext parserContext) {
        BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(XsdSchemaRepository.class);

        BeanDefinitionParserUtils.setPropertyReference(builder, element.getAttribute("schema-mapping-strategy"), "schemaMappingStrategy");

        ManagedList<RuntimeBeanReference> schemas = new ManagedList<RuntimeBeanReference>();

        Element schemasElement = DomUtils.getChildElementByTagName(element, "schemas");
        if (schemasElement != null) {
            List<Element> schemaElements = DomUtils.getChildElements(schemasElement);
            for (Element schemaElement : schemaElements) {
                if (schemaElement.hasAttribute("schema")) {
                    schemas.add(new RuntimeBeanReference(schemaElement.getAttribute("schema")));
                } else if (schemaElement.hasAttribute("id") && schemaElement.hasAttribute("location")) {
                    new SchemaParser().parse(schemaElement, parserContext);
                    schemas.add(new RuntimeBeanReference(schemaElement.getAttribute("id")));
                }
            }
        }

        if (schemas.size() > 0) {
            builder.addPropertyValue("schemas", schemas);
        }

        List<String> locations = new ArrayList<String>();
        Element locationsElement = DomUtils.getChildElementByTagName(element, "locations");
        if (locationsElement != null) {
            List<Element> locationElements = DomUtils.getChildElementsByTagName(locationsElement, "location");
            for (Element locationElement : locationElements) {
                locations.add(locationElement.getAttribute("path"));
            }
        }

        if (locations.size() > 0) {
            builder.addPropertyValue("locations", locations);
        }

        parserContext.getRegistry().registerBeanDefinition(element.getAttribute("id"), builder.getBeanDefinition());

        return null;
    }
}
