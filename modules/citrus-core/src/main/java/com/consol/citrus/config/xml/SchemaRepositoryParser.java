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

import java.util.List;
import java.util.stream.Collectors;

/**
 * Bean definition parser for schema-repository configuration.
 *
 * @author Martin.Maher@consol.de
 * @since 1.3.1
 */
public class SchemaRepositoryParser implements BeanDefinitionParser {

    private static final String LOCATION = "location";
    private static final String SCHEMAS = "schemas";
    private static final String SCHEMA = "schema";
    private static final String ID = "id";
    private static final String LOCATIONS = "locations";

    private final SchemaParser schemaParser = new SchemaParser();

    public BeanDefinition parse(Element element, ParserContext parserContext) {
        BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(XsdSchemaRepository.class);

        BeanDefinitionParserUtils.setPropertyReference(builder, element.getAttribute("schema-mapping-strategy"), "schemaMappingStrategy");

        parseSchemasElement(element, builder, parserContext);
        addLocationsToBuilder(element, builder);

        parserContext.getRegistry().registerBeanDefinition(element.getAttribute(ID), builder.getBeanDefinition());

        return null;
    }

    /**
     * Parses the given schema element to RuntimeBeanReference in consideration of the given context
     * and adds them to the builder
     * @param element The element from where the schemas will be parsed
     * @param builder The builder to add the resulting RuntimeBeanReference to
     * @param parserContext The context to parse the schema elements in
     */
    private void parseSchemasElement( Element element,
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
     * Adds the locations contained in the given locations element to the BeanDefinitionBuilder
     * @param element the element containing the locations to be added to the builder
     * @param builder the BeanDefinitionBuilder to add the locations to
     */
    private void addLocationsToBuilder(Element element, BeanDefinitionBuilder builder) {
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
     * Construct a List of RuntimeBeanReferences from the given list of schema elements under
     * consideration of the given parser context
     * @param parserContext The context to parse the elements in
     * @param schemaElements The element to be parsed
     * @return A list of RuntimeBeanReferences that have been defined in the xml document
     */
    private ManagedList<RuntimeBeanReference> constructRuntimeBeanReferences(
            ParserContext parserContext,
            List<Element> schemaElements) {

        ManagedList<RuntimeBeanReference> runtimeBeanReferences = new ManagedList<>();

        for (Element schemaElement : schemaElements) {
            if (schemaElement.hasAttribute(SCHEMA)) {
                runtimeBeanReferences.add(
                        new RuntimeBeanReference(schemaElement.getAttribute(SCHEMA)));
            } else if (isXmlSchemaLocation(schemaElement)) {
                schemaParser.parse(schemaElement, parserContext);
                runtimeBeanReferences.add(
                        new RuntimeBeanReference(schemaElement.getAttribute(ID)));
            }
        }

        return runtimeBeanReferences;
    }

    /**
     * Checks whether the schema element is a location for a xml schema
     * @param schemaElement Schema element to check
     * @return whether the schema element is a location for a xml schema
     */
    private boolean isXmlSchemaLocation(Element schemaElement) {
        return isLocation(schemaElement) && isWsdlOrXsd(schemaElement);
    }

    /**
     * Checks whether the schema element references a schema location
     * @param schemaElement Schema element to check
     * @return whether the schema element references a schema location
     */
    private boolean isLocation(Element schemaElement) {
        return schemaElement.hasAttribute(ID) && schemaElement.hasAttribute(LOCATION);
    }

    /**
     * Checks whether the schema element references a xml schema
     * @param schemaElement Schema element to check. Has to be a LOCATION element
     * @return whether the schema element references a xml schema
     */
    private boolean isWsdlOrXsd(Element schemaElement) {
        return schemaElement.getAttribute(LOCATION).endsWith(".xsd") ||
                schemaElement.getAttribute(LOCATION).endsWith(".wsdl");
    }
}
