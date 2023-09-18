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

import java.util.HashMap;
import java.util.Map;

import org.citrusframework.spi.ResourcePathTypeResolver;
import org.citrusframework.util.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

/**
 * Bean definition parser for schema configuration.
 *
 * @author Martin.Maher
 * @since 1.3.1
 */
public class SchemaParser implements BeanDefinitionParser {

    /** Logger */
    private static final Logger logger = LoggerFactory.getLogger(SchemaParser.class);

    /** Resource path where to find custom schema parsers via lookup */
    private static final String RESOURCE_PATH = "META-INF/citrus/schema/parser";

    /** Type resolver for dynamic schema parser lookup via resource path */
    private static final ResourcePathTypeResolver TYPE_RESOLVER = new ResourcePathTypeResolver(RESOURCE_PATH);

    /** Local cache to hold already looked up parsers */
    private static final Map<String, BeanDefinitionParser> SCHEMA_PARSER = new HashMap<>();

    @Override
    public BeanDefinition parse(Element element, ParserContext parserContext) {
        String location = element.getAttribute("location");
        return lookupSchemaParser(location).parse(element, parserContext);
    }

    /**
     * Lookup schema parser based on the file extension used in the schema location. Each file type should have a schema parser
     * that is able to create a proper bean definition form the given element.
     * @param location
     * @return
     */
    private BeanDefinitionParser lookupSchemaParser(String location) {
        // extract file extension from schema location and use this as key to lookup the parser for these files
        String fileExtension = FileUtils.getFileExtension(location);

        if (SCHEMA_PARSER.containsKey(fileExtension)) {
            return SCHEMA_PARSER.get(fileExtension);
        }

        BeanDefinitionParser parser = TYPE_RESOLVER.resolve(fileExtension);
        logger.info(String.format("Found schema bean definition parser %s from resource %s", parser.getClass(), RESOURCE_PATH + "/" + fileExtension));
        SCHEMA_PARSER.put(fileExtension, parser);
        return parser;
    }
}
