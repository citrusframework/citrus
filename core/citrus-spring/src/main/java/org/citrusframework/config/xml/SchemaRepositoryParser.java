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
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.citrusframework.spi.ResourcePathTypeResolver;
import org.citrusframework.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.ManagedList;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

/**
 * Bean definition parser for schema-repository configuration.
 *
 * @author Martin.Maher
 * @since 1.3.1
 */
public class SchemaRepositoryParser implements BeanDefinitionParser {

    /** Logger */
    private static final Logger logger = LoggerFactory.getLogger(SchemaRepositoryParser.class);

    private static final String LOCATION = "location";
    private static final String LOCATIONS = "locations";
    private static final String SCHEMA = "schema";
    private static final String SCHEMAS = "schemas";
    private static final String ID = "id";

    private final static SchemaParser SCHEMA_PARSER = new SchemaParser();

    /** Resource path where to find custom schema repository parsers via lookup */
    private static final String RESOURCE_PATH = "META-INF/citrus/schema-repository/parser";

    /** Type resolver for dynamic schema repository parser lookup via resource path */
    private static final ResourcePathTypeResolver TYPE_RESOLVER = new ResourcePathTypeResolver(RESOURCE_PATH);

    /** Local cache to hold already looked up parsers */
    private static final Map<String, BeanDefinitionParser> SCHEMA_REPOSITORY_PARSER = new HashMap<>();

    @Override
    public BeanDefinition parse(Element element, ParserContext parserContext) {
        String schemaRepositoryType = element.getAttribute("type");

        if (StringUtils.isEmpty(schemaRepositoryType)) {
            schemaRepositoryType = "xml";
        }

        return lookupSchemaRepositoryParser(schemaRepositoryType).parse(element, parserContext);
    }

    /**
     * Lookup schema repository parser based on the given type. Each repository type should have a schema repository parser
     * that is able to create a proper bean definition form the given element.
     * @param type
     * @return
     */
    private BeanDefinitionParser lookupSchemaRepositoryParser(String type) {
        if (SCHEMA_REPOSITORY_PARSER.containsKey(type)) {
            return SCHEMA_REPOSITORY_PARSER.get(type);
        }

        BeanDefinitionParser parser = TYPE_RESOLVER.resolve(type);
        logger.info(String.format("Found schema repository bean definition parser %s from resource %s", parser.getClass(), RESOURCE_PATH + "/" + type));
        SCHEMA_REPOSITORY_PARSER.put(type, parser);
        return parser;
    }

    /**
     * Adds the locations contained in the given locations element to the BeanDefinitionBuilder
     * @param element the element containing the locations to be added to the builder
     * @param builder the BeanDefinitionBuilder to add the locations to
     */
    static void addLocationsToBuilder(Element element, BeanDefinitionBuilder builder) {
        Element locationsElement = DomUtils.getChildElementByTagName(element, LOCATIONS);
        if (locationsElement != null) {
            List<Element> locationElements = DomUtils.getChildElementsByTagName(locationsElement, LOCATION);

            List<String> locations = locationElements.stream()
                    .map(locationElement -> locationElement.getAttribute("path"))
                    .collect(Collectors.toList());

            if (!locations.isEmpty()) {
                builder.addPropertyValue(LOCATIONS, locations);
            }
        }
    }

    /**
     * Parses the given schema element to RuntimeBeanReference in consideration of the given context
     * and adds them to the builder
     * @param element The element from where the schemas will be parsed
     * @param builder The builder to add the resulting RuntimeBeanReference to
     * @param parserContext The context to parse the schema elements in
     */
    static void parseSchemasElement(Element element,
                                     BeanDefinitionBuilder builder,
                                     ParserContext parserContext) {
        Element schemasElement = DomUtils.getChildElementByTagName(element, SCHEMAS);

        if (schemasElement != null) {
            List<Element> schemaElements = DomUtils.getChildElements(schemasElement);
            ManagedList<RuntimeBeanReference> beanReferences = constructRuntimeBeanReferences(parserContext, schemaElements);

            if (!beanReferences.isEmpty()) {
                builder.addPropertyValue(SCHEMAS, beanReferences);
            }
        }
    }

    /**
     * Construct a List of RuntimeBeanReferences from the given list of schema elements under
     * consideration of the given parser context
     * @param parserContext The context to parse the elements in
     * @param schemaElements The element to be parsed
     * @return A list of RuntimeBeanReferences that have been defined in the xml document
     */
    private static ManagedList<RuntimeBeanReference> constructRuntimeBeanReferences(
            ParserContext parserContext,
            List<Element> schemaElements) {

        ManagedList<RuntimeBeanReference> runtimeBeanReferences = new ManagedList<>();

        for (Element schemaElement : schemaElements) {
            if (schemaElement.hasAttribute(SCHEMA)) {
                runtimeBeanReferences.add(
                        new RuntimeBeanReference(schemaElement.getAttribute(SCHEMA)));
            } else  {
                SCHEMA_PARSER.parse(schemaElement, parserContext);
                runtimeBeanReferences.add(
                        new RuntimeBeanReference(schemaElement.getAttribute(ID)));
            }
        }

        return runtimeBeanReferences;
    }
}
