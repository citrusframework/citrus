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

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;

import java.util.Objects;

/**
 * Bean definition parser for schema-repository configuration.
 *
 * @author Martin.Maher@consol.de
 * @since 1.3.1
 */
public class SchemaRepositoryParser implements BeanDefinitionParser {

    private XmlSchemaRepositoryParser xmlSchemaRepositoryParser;
    private JsonSchemaRepositoryParser jsonSchemaRepositoryParser;

    public SchemaRepositoryParser(){
        xmlSchemaRepositoryParser = new XmlSchemaRepositoryParser();
        jsonSchemaRepositoryParser = new JsonSchemaRepositoryParser();
    }

    SchemaRepositoryParser(XmlSchemaRepositoryParser xmlSchemaRepositoryParser,
                           JsonSchemaRepositoryParser jsonSchemaRepositoryParser) {
        this.xmlSchemaRepositoryParser = xmlSchemaRepositoryParser;
        this.jsonSchemaRepositoryParser = jsonSchemaRepositoryParser;
    }

    @Override
    public BeanDefinition parse(Element element, ParserContext parserContext) {
        if(isXmlSchemaRepository(element)){
            xmlSchemaRepositoryParser.parse(element, parserContext);
        }else if(isJsonSchemaRepository(element)){
            jsonSchemaRepositoryParser.parse(element, parserContext);
        }

        return null;
    }

    /**
     * Decides whether the given element is a xml schema repository.
     *
     * Note:
     * If the "type" attribute has not been set, the repository is interpreted as a xml repository by definition.
     * This is important to guarantee downwards compatibility.
     * @param element The element to be checked
     * @return Whether the given element is a xml schema repository
     */
    private boolean isXmlSchemaRepository(Element element) {
        String schemaRepositoryType = element.getAttribute("type");
        return StringUtils.isEmpty(schemaRepositoryType) || "xml".equals(schemaRepositoryType);
    }

    /**
     * Decides whether the given element is a json schema repository
     * @param element The element to be checked
     * @return  whether the given element is a json schema repository
     */
    private boolean isJsonSchemaRepository(Element element) {
        return Objects.equals(element.getAttribute("type"), "json");
    }
}
