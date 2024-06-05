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

/**
 * ==================================================
 * GENERATED CLASS, ALL CHANGES WILL BE LOST
 * ==================================================
 */

package org.citrusframework.openapi.generator.rest.multiparttest.citrus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.processing.Generated;

import org.citrusframework.exceptions.CitrusRuntimeException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.core.Conventions;
import org.springframework.util.Assert;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;


@Generated(value = "org.citrusframework.openapi.generator.JavaCitrusCodegen")
public class MultipartTestBeanDefinitionParser implements BeanDefinitionParser {

    private static final String COOKIE = "cookie";
    private static final String HEADER = "header";
    private static final String SOAP_HEADER = "soapHeader";
    private static final String MIME_HEADER = "mimeHeader";
    private static final String NAME = "name";
    private static final String REQUEST_BODY = "body";
    private static final String REQUEST_BODY_LITERAL = "bodyLiteral";
    private static final String MULTIPART_BODY = "multipartBody";
    private static final String RESPONSE = "response";
    private static final String RESPONSE_JSONPATH = "json-path";
    private static final String RESPONSE_XPATH = "xpath";
    private static final String EXPRESSION = "expression";
    private static final String VALUE = "value";
    private static final String RESPONSE_RESOURCE = "resource";
    private static final String FILE = "file";
    private static final String RESPONSE_VARIABLE = "responseVariable";
    private static final String RESPONSE_VALUE = "responseValue";
    private static final String SCRIPT = "script";
    private static final String TYPE = "type";
    private static final String SQL = "sql";
    private static final String COLUMN = "column";
    private static final String VARIABLE = "variable";
    // new
    private static final String SCHEMA = "schema";
    // new
    private static final String SCHEMA_VALIDATION = "schemaValidation";

    private final Class<?> beanClass;

    public MultipartTestBeanDefinitionParser(Class<?> beanClass) {
        this.beanClass = beanClass;
    }

    public BeanDefinition parse(Element element) {
        return parse(element, null);
    }

    /**
     * Note: The {@link MultipartTestBeanDefinitionParser#parse(Element element)} allows access direct
     * access without the {@link org.springframework.beans.factory.xml.ParserContext} for convenience.
     */
    @Override
    public BeanDefinition parse(Element element, ParserContext parserContext) {
        BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(beanClass);
        retrieveRootNodeAttributes(element, builder);
        retrieveOptionalNodeAttributes(element, REQUEST_BODY, builder);
        retrieveTextContentAndNodeAttributes(element, REQUEST_BODY_LITERAL, builder);
        retrieveOptionalNodeAttributes(element, RESPONSE, builder);
        retrieveParamNodeData(element, builder, COOKIE);
        retrieveParamNodeData(element, builder, HEADER);
        retrieveParamNodeData(element, builder, SOAP_HEADER);
        retrieveParamNodeData(element, builder, MIME_HEADER);
        retrieveOptionalNodeAttributes(element, SCHEMA, builder);
        retrieveOptionalNodeAttributes(element, SCHEMA_VALIDATION, builder);
        retrieveOptionalMultipartElements(element, builder);
        retrieveResponseNodeData(element, builder);
        builder.addPropertyValue("name", element.getTagName());
        return builder.getBeanDefinition();
    }

    private void retrieveOptionalMultipartElements(Element element, BeanDefinitionBuilder builder) {
        var multipartBodyElement = DomUtils.getChildElementByTagName(element, MULTIPART_BODY);
        if (multipartBodyElement != null) {
            var multipartBodyChildElements = DomUtils.getChildElements(multipartBodyElement);
            for(int i = 0; i < multipartBodyChildElements.size(); i++){
                var multipartBodyChildElement = multipartBodyChildElements.get(i);
                String propertyName = Conventions.attributeNameToPropertyName(multipartBodyChildElement.getLocalName());
                builder.addPropertyValue(propertyName, multipartBodyChildElement.getTextContent());
            }
        }
    }

    private void retrieveRootNodeAttributes(Element element, BeanDefinitionBuilder builder) {
        NamedNodeMap attributes = element.getAttributes();
        for (int x = 0; x < attributes.getLength(); x++) {
            Attr attribute = (Attr) attributes.item(x);
            String propertyName = Conventions.attributeNameToPropertyName(attribute.getLocalName());
            Assert.state(StringUtils.isNotBlank(propertyName), "Illegal property name returned, it must not be null or empty.");
            builder.addPropertyValue(propertyName, attribute.getValue());
        }
    }

    private void retrieveOptionalNodeAttributes(Element element, String elementName, BeanDefinitionBuilder builder) {
        if (!DomUtils.getChildElementsByTagName(element, elementName).isEmpty()) {
            Element el = DomUtils.getChildElementsByTagName(element, elementName).get(0);
            NamedNodeMap attributes = el.getAttributes();
            for (int x = 0; x < attributes.getLength(); x++) {
                Attr attribute = (Attr) attributes.item(x);
                String propertyName = Conventions.attributeNameToPropertyName(attribute.getLocalName());
                Assert.state(StringUtils.isNotBlank(propertyName), "Illegal property name returned, it must not be null or empty.");
                String variableName = el.getLocalName() + propertyName.substring(0, 1).toUpperCase() + propertyName.substring(1);
                builder.addPropertyValue(variableName, attribute.getValue());
            }
        }
    }

    private void retrieveTextContentAndNodeAttributes(Element element, String elementName, BeanDefinitionBuilder builder) {
        if (!DomUtils.getChildElementsByTagName(element, elementName).isEmpty()) {
            Element el1 = DomUtils.getChildElementsByTagName(element, elementName).get(0);
            NamedNodeMap attributes = el1.getAttributes();
            for (int x = 0; x < attributes.getLength(); x++) {
                Attr attribute = (Attr) attributes.item(x);
                String propertyName1 = Conventions.attributeNameToPropertyName(attribute.getLocalName());
                Assert.state(StringUtils.isNotBlank(propertyName1), "Illegal property name returned, it must not be null or empty.");
                String variableName = el1.getLocalName() + propertyName1.substring(0, 1).toUpperCase() + propertyName1.substring(1);
                builder.addPropertyValue(variableName, attribute.getValue());
            }
            Element el = DomUtils.getChildElementsByTagName(element, elementName).get(0);
            builder.addPropertyValue(elementName, el.getTextContent());
        }
    }

    private void retrieveParamNodeData(Element element, BeanDefinitionBuilder builder, String paramType) {
        if (!DomUtils.getChildElementsByTagName(element, paramType).isEmpty()) {
            Map<String, String> params = new HashMap<>();
            List<Element> elements = DomUtils.getChildElementsByTagName(element, paramType);
            elements.forEach(e -> {
                String name = e.getAttribute(NAME);
                String value = e.getAttribute(VALUE);

                Assert.state(StringUtils.isNotBlank(name), "Illegal attribute value returned. The 'name' attribute must not be null or empty.");
                Assert.state(StringUtils.isNotBlank(value), "Illegal attribute value returned. The 'value' attribute must not be null or empty.");

                params.put(name, value);
            });
            builder.addPropertyValue(paramType, params);
        }
    }

    private void retrieveResponseNodeData(Element element, BeanDefinitionBuilder builder) {

        if (!DomUtils.getChildElementsByTagName(element, RESPONSE).isEmpty()) {
            Element response = DomUtils.getChildElementsByTagName(element, RESPONSE).get(0);
            List<Element> elements = DomUtils.getChildElements(response);

            Map<String, String> responseVariable = new HashMap<>();
            Map<String, String> responseValue = new HashMap<>();

            for (int i = 0; i < elements.size(); i++) {
                Element e = elements.get(i);

                if (e.getTagName().contains(RESPONSE_JSONPATH) || e.getTagName().contains(RESPONSE_XPATH)) {
                    String expression = e.getAttribute(EXPRESSION);
                    String value = e.getAttribute(VALUE);

                    Assert.state(StringUtils.isNotBlank(expression), "Illegal attribute value returned. The 'expression' attribute must not be null or empty.");
                    Assert.state(StringUtils.isNotBlank(value), "Illegal attribute value returned. The 'value' attribute must not be null or empty.");

                    // variable to save @variable('ebid')@ else value to validate
                    if (value.matches("\\@variable\\('.*'\\)\\@")) {
                        Matcher match = Pattern.compile("\\'(.*?)\\'").matcher(value);
                        if (match.find()) {
                            responseVariable.put(expression, value.substring(match.start() + 1, match.end() - 1));
                        }
                    } else {
                        responseValue.put(expression, value);
                    }
                } else if (e.getTagName().contains(SCRIPT)) {
                    String script = e.getTextContent();
                    Assert.state(StringUtils.isNotBlank(script), "Illegal attribute value returned. The 'script' attribute must not be null or empty.");
                    builder.addPropertyValue(SCRIPT, script);

                    if (!e.getAttribute(TYPE).isEmpty()) {
                        String type = e.getAttribute(TYPE);
                        Assert.state(StringUtils.isNotBlank(type), "Illegal attribute value returned. The 'type' attribute must not be null or empty.");
                        builder.addPropertyValue(TYPE, type);
                    }
                } else if (e.getTagName().contains(RESPONSE_RESOURCE)) {
                    String filePath = e.getAttribute(FILE);
                    Assert.state(StringUtils.isNotBlank(filePath), "Illegal attribute value returned. The 'file' attribute must not be null or empty.");
                    builder.addPropertyValue(RESPONSE_RESOURCE, filePath);
                }

            }

            builder.addPropertyValue(RESPONSE_VARIABLE, responseVariable);
            builder.addPropertyValue(RESPONSE_VALUE, responseValue);
        }
    }
}
