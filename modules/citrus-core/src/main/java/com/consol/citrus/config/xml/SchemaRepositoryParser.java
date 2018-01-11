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
import com.consol.citrus.json.JsonSchemaRepository;
import com.consol.citrus.xml.XsdSchemaRepository;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.ManagedList;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.StringUtils;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Bean definition parser for schema-repository configuration.
 *
 * @author Martin.Maher@consol.de
 * @since 1.3.1
 */
public class SchemaRepositoryParser implements BeanDefinitionParser {

    private static final String LOCATION = "location";
    private static final String LOCATIONS = "locations";
    private static final String SCHEMA = "schema";
    private static final String SCHEMAS = "schemas";
    private static final String ID = "id";

    private final SchemaParser schemaParser = new SchemaParser();

    @Override
    public BeanDefinition parse(Element element, ParserContext parserContext) {

        if (isXmlSchemaRepository(element)) {
            registerXmlSchemaRepository(element, parserContext);
        } else if (isJsonSchemaRepository(element)) {
            registerJsonSchemaRepository(element, parserContext);
        }

        return null;
    }

    /**
     * Registers a JsonSchemaRepository definition in the parser context
     * @param element The element to be converted into a JsonSchemaRepository definition
     * @param parserContext The parser context to add the definitions to
     */
    private void registerJsonSchemaRepository(Element element, ParserContext parserContext) {
        BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(JsonSchemaRepository.class);
        addLocationsToBuilder(element, builder);
        parseSchemasElement(element, builder, parserContext);
        parserContext.getRegistry().registerBeanDefinition(element.getAttribute(ID), builder.getBeanDefinition());
    }

    /**
     * Registers a XsdSchemaRepository definition in the parser context
     * @param element The element to be converted into a XmlSchemaRepository definition
     * @param parserContext The parser context to add the definitions to
     */
    private void registerXmlSchemaRepository(Element element, ParserContext parserContext) {
        BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(XsdSchemaRepository.class);
        BeanDefinitionParserUtils.setPropertyReference(builder, element.getAttribute("schema-mapping-strategy"), "schemaMappingStrategy");
        addLocationsToBuilder(element, builder);
        parseSchemasElement(element, builder, parserContext);
        parserContext.getRegistry().registerBeanDefinition(element.getAttribute(ID), builder.getBeanDefinition());
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
     * Parses the given schema element to RuntimeBeanReference in consideration of the given context
     * and adds them to the builder
     * @param element The element from where the schemas will be parsed
     * @param builder The builder to add the resulting RuntimeBeanReference to
     * @param parserContext The context to parse the schema elements in
     */
    private void parseSchemasElement(Element element,
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
    private ManagedList<RuntimeBeanReference> constructRuntimeBeanReferences(
            ParserContext parserContext,
            List<Element> schemaElements) {

        ManagedList<RuntimeBeanReference> runtimeBeanReferences = new ManagedList<>();

        for (Element schemaElement : schemaElements) {
            if (schemaElement.hasAttribute(SCHEMA)) {
                runtimeBeanReferences.add(
                        new RuntimeBeanReference(schemaElement.getAttribute(SCHEMA)));
            } else  {
                schemaParser.parse(schemaElement, parserContext);
                runtimeBeanReferences.add(
                        new RuntimeBeanReference(schemaElement.getAttribute(ID)));
            }
        }

        return runtimeBeanReferences;
    }
}
