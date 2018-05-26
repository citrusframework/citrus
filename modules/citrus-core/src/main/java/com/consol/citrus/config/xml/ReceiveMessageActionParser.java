/*
 * Copyright 2006-2010 the original author or authors.
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

import com.consol.citrus.Citrus;
import com.consol.citrus.actions.ReceiveMessageAction;
import com.consol.citrus.config.util.*;
import com.consol.citrus.validation.builder.AbstractMessageContentBuilder;
import com.consol.citrus.validation.context.*;
import com.consol.citrus.validation.json.JsonMessageValidationContext;
import com.consol.citrus.validation.json.JsonPathMessageValidationContext;
import com.consol.citrus.validation.script.ScriptValidationContext;
import com.consol.citrus.validation.xml.XmlMessageValidationContext;
import com.consol.citrus.validation.xml.XpathMessageValidationContext;
import com.consol.citrus.variable.VariableExtractor;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.ManagedList;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

import java.util.*;
import java.util.stream.Stream;

/**
 * Bean definition parser for receive action in test case.
 * 
 * @author Christoph Deppisch
 */
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public class ReceiveMessageActionParser extends AbstractMessageActionParser {

    @Override
    public BeanDefinition parse(Element element, ParserContext parserContext) {
        String endpointUri = element.getAttribute("endpoint");

        if (!StringUtils.hasText(endpointUri)) {
            throw new BeanCreationException("Endpoint reference must not be empty");
        }

        BeanDefinitionBuilder builder = parseComponent(element, parserContext);
        builder.addPropertyValue("name", element.getLocalName());

        if (endpointUri.contains(":") || (endpointUri.contains(Citrus.VARIABLE_PREFIX) && endpointUri.contains(Citrus.VARIABLE_SUFFIX))) {
            builder.addPropertyValue("endpointUri", endpointUri);
        } else {
            builder.addPropertyReference("endpoint", endpointUri);
        }

        DescriptionElementParser.doParse(element, builder);
        
        BeanDefinitionParserUtils.setPropertyReference(builder, element.getAttribute("actor"), "actor");

        String receiveTimeout = element.getAttribute("timeout");
        if (StringUtils.hasText(receiveTimeout)) {
            builder.addPropertyValue("receiveTimeout", Long.valueOf(receiveTimeout));
        }
        
        MessageSelectorParser.doParse(element, builder);

        Element messageElement = DomUtils.getChildElementByTagName(element, "message");
        List<ValidationContext> validationContexts = parseValidationContexts(messageElement, builder);

        AbstractMessageContentBuilder messageBuilder = constructMessageBuilder(messageElement);
        parseHeaderElements(element, messageBuilder, validationContexts);

        builder.addPropertyValue("messageBuilder", messageBuilder);
        builder.addPropertyValue("validationContexts", validationContexts);
        builder.addPropertyValue("variableExtractors", getVariableExtractors(element));

        return builder.getBeanDefinition();
    }

    /**
     * Parse message validation contexts.
     * @param messageElement
     * @param builder
     * @return
     */
    protected List<ValidationContext> parseValidationContexts(Element messageElement, BeanDefinitionBuilder builder) {
        List<ValidationContext> validationContexts = new ArrayList<>();
        if (messageElement != null) {
            String messageType = messageElement.getAttribute("type");
            if (!StringUtils.hasText(messageType)) {
                messageType = Citrus.DEFAULT_MESSAGE_TYPE;
            } else {
                builder.addPropertyValue("messageType", messageType);
            }

            HeaderValidationContext headerValidationContext = new HeaderValidationContext();
            validationContexts.add(headerValidationContext);

            String headerValidator = messageElement.getAttribute("header-validator");
            if (StringUtils.hasText(headerValidator)) {
                headerValidationContext.addHeaderValidator(headerValidator);
            }

            String headerValidatorExpression = messageElement.getAttribute("header-validators");
            if (StringUtils.hasText(headerValidatorExpression)) {
                Stream.of(headerValidatorExpression.split(","))
                        .map(String::trim)
                        .forEach(headerValidationContext::addHeaderValidator);
            }

            XmlMessageValidationContext xmlMessageValidationContext = getXmlMessageValidationContext(messageElement);
            validationContexts.add(xmlMessageValidationContext);

            XpathMessageValidationContext xPathMessageValidationContext = getXPathMessageValidationContext(messageElement, xmlMessageValidationContext);
            if (!xPathMessageValidationContext.getXpathExpressions().isEmpty()) {
                validationContexts.add(xPathMessageValidationContext);
            }

            JsonMessageValidationContext jsonMessageValidationContext = getJsonMessageValidationContext(messageElement);
            validationContexts.add(jsonMessageValidationContext);

            JsonPathMessageValidationContext jsonPathMessageValidationContext = getJsonPathMessageValidationContext(messageElement);
            if (!jsonPathMessageValidationContext.getJsonPathExpressions().isEmpty()) {
                validationContexts.add(jsonPathMessageValidationContext);
            }

            ScriptValidationContext scriptValidationContext = getScriptValidationContext(messageElement, messageType);
            if (scriptValidationContext != null) {
                validationContexts.add(scriptValidationContext);
            }

            ManagedList<RuntimeBeanReference> validators = new ManagedList<>();
            String messageValidator = messageElement.getAttribute("validator");
            if (StringUtils.hasText(messageValidator)) {
                validators.add(new RuntimeBeanReference(messageValidator));
            }

            String messageValidatorExpression = messageElement.getAttribute("validators");
            if (StringUtils.hasText(messageValidatorExpression)) {
                Stream.of(messageValidatorExpression.split(","))
                        .map(String::trim)
                        .map(RuntimeBeanReference::new)
                        .forEach(validators::add);
            }

            if (!validators.isEmpty()) {
                builder.addPropertyValue("validators", validators);
            }

            String dataDictionary = messageElement.getAttribute("data-dictionary");
            if (StringUtils.hasText(dataDictionary)) {
                builder.addPropertyReference("dataDictionary", dataDictionary);
            }
        } else {
            validationContexts.add(new HeaderValidationContext());
        }

        return validationContexts;
    }

    /**
     * Constructs a list of variable extractors.
     * @param element
     * @return
     */
    protected List<VariableExtractor> getVariableExtractors(Element element) {
        List<VariableExtractor> variableExtractors = new ArrayList<>();

        parseExtractHeaderElements(element, variableExtractors);

        Element extractElement = DomUtils.getChildElementByTagName(element, "extract");
        if (extractElement != null) {
            Map<String, String> extractFromPath = new HashMap<>();

            List<Element> messageValueElements = DomUtils.getChildElementsByTagName(extractElement, "message");
            messageValueElements.addAll(DomUtils.getChildElementsByTagName(extractElement, "body"));
            VariableExtractorParserUtil.parseMessageElement(messageValueElements, extractFromPath);

            if (!CollectionUtils.isEmpty(extractFromPath)) {
                VariableExtractorParserUtil.addPayloadVariableExtractors(element, variableExtractors, extractFromPath);
            }
        }
        return variableExtractors;
    }

    /**
     * Construct the basic Json message validation context.
     * @param messageElement
     * @return
     */
    private JsonMessageValidationContext getJsonMessageValidationContext(Element messageElement) {
        JsonMessageValidationContext context = new JsonMessageValidationContext();

        if (messageElement != null) {
            Set<String> ignoreExpressions = new HashSet<String>();
            List<?> ignoreElements = DomUtils.getChildElementsByTagName(messageElement, "ignore");
            for (Iterator<?> iter = ignoreElements.iterator(); iter.hasNext(); ) {
                Element ignoreValue = (Element) iter.next();
                ignoreExpressions.add(ignoreValue.getAttribute("path"));
            }
            context.setIgnoreExpressions(ignoreExpressions);

            addSchemaInformationToValidationContext(messageElement, context);
        }

        return context;
    }

    /**
     * Construct the basic Xml message validation context.
     * @param messageElement
     * @return
     */
    private XmlMessageValidationContext getXmlMessageValidationContext(Element messageElement) {
        XmlMessageValidationContext context = new XmlMessageValidationContext();

        if (messageElement != null) {
            addSchemaInformationToValidationContext(messageElement, context);

            Set<String> ignoreExpressions = new HashSet<String>();
            List<?> ignoreElements = DomUtils.getChildElementsByTagName(messageElement, "ignore");
            for (Iterator<?> iter = ignoreElements.iterator(); iter.hasNext();) {
                Element ignoreValue = (Element) iter.next();
                ignoreExpressions.add(ignoreValue.getAttribute("path"));
            }
            context.setIgnoreExpressions(ignoreExpressions);

            parseNamespaceValidationElements(messageElement, context);

            //Catch namespace declarations for namespace context
            Map<String, String> namespaces = new HashMap<String, String>();
            List<?> namespaceElements = DomUtils.getChildElementsByTagName(messageElement, "namespace");
            if (namespaceElements.size() > 0) {
                for (Iterator<?> iter = namespaceElements.iterator(); iter.hasNext();) {
                    Element namespaceElement = (Element) iter.next();
                    namespaces.put(namespaceElement.getAttribute("prefix"), namespaceElement.getAttribute("value"));
                }
                context.setNamespaces(namespaces);
            }
        }

        return context;
    }

    /**
     * Adds information about the validation of the message against a certain schema to the context
     * @param messageElement The message element to get the configuration from
     * @param context The context to set the schema validation configuration to
     */
    private void addSchemaInformationToValidationContext(Element messageElement, SchemaValidationContext context) {
        String schemaValidation = messageElement.getAttribute("schema-validation");
        if (StringUtils.hasText(schemaValidation)) {
            context.setSchemaValidation(Boolean.valueOf(schemaValidation));
        }

        String schema = messageElement.getAttribute("schema");
        if (StringUtils.hasText(schema)) {
            context.setSchema(schema);
        }

        String schemaRepository = messageElement.getAttribute("schema-repository");
        if (StringUtils.hasText(schemaRepository)) {
            context.setSchemaRepository(schemaRepository);
        }
    }

    /**
     * Construct the XPath message validation context.
     * @param messageElement
     * @param parentContext
     * @return
     */
    private XpathMessageValidationContext getXPathMessageValidationContext(Element messageElement, XmlMessageValidationContext parentContext) {
        XpathMessageValidationContext context = new XpathMessageValidationContext();
        
        parseXPathValidationElements(messageElement, context);

        context.setControlNamespaces(parentContext.getControlNamespaces());
        context.setNamespaces(parentContext.getNamespaces());
        context.setIgnoreExpressions(parentContext.getIgnoreExpressions());
        context.setSchema(parentContext.getSchema());
        context.setSchemaRepository(parentContext.getSchemaRepository());
        context.setSchemaValidation(parentContext.isSchemaValidationEnabled());
        context.setDTDResource(parentContext.getDTDResource());

        return context;
    }

    /**
     * Construct the JSONPath message validation context.
     * @param messageElement
     * @return
     */
    private JsonPathMessageValidationContext getJsonPathMessageValidationContext(Element messageElement) {
        JsonPathMessageValidationContext context = new JsonPathMessageValidationContext();

        //check for validate elements, these elements can either have script, jsonPath or namespace validation information
        //for now we only handle jsonPath validation
        Map<String, Object> validateJsonPathExpressions = new HashMap<>();
        List<?> validateElements = DomUtils.getChildElementsByTagName(messageElement, "validate");
        if (validateElements.size() > 0) {
            for (Iterator<?> iter = validateElements.iterator(); iter.hasNext();) {
                Element validateElement = (Element) iter.next();
                extractJsonPathValidateExpressions(validateElement, validateJsonPathExpressions);
            }

            context.setJsonPathExpressions(validateJsonPathExpressions);
        }

        return context;
    }

    /**
     * Construct the message validation context.
     * @param messageElement
     * @return
     */
    private ScriptValidationContext getScriptValidationContext(Element messageElement, String messageType) {
        ScriptValidationContext context = null;

        boolean done = false;
        List<?> validateElements = DomUtils.getChildElementsByTagName(messageElement, "validate");
        if (validateElements.size() > 0) {
            for (Iterator<?> iter = validateElements.iterator(); iter.hasNext();) {
                Element validateElement = (Element) iter.next();

                Element scriptElement = DomUtils.getChildElementByTagName(validateElement, "script");

                // check for nested validate script child node
                if (scriptElement != null) {
                    if (!done) {
                        done = true;
                    } else {
                        throw new BeanCreationException("Found multiple validation script definitions - " +
                                "only supporting a single validation script for message validation");
                    }

                    context = new ScriptValidationContext(messageType);
                    String type = scriptElement.getAttribute("type");
                    context.setScriptType(type);

                    String filePath = scriptElement.getAttribute("file");
                    if (StringUtils.hasText(filePath)) {
                        context.setValidationScriptResourcePath(filePath);
                        if (scriptElement.hasAttribute("charset")) {
                            context.setValidationScriptResourceCharset(scriptElement.getAttribute("charset"));
                        }
                    } else {
                        context.setValidationScript(DomUtils.getTextValue(scriptElement));
                    }
                }
            }
        }

        return context;
    }
    
    /**
     * Parses validation elements and adds information to the message validation context.
     * 
     * @param messageElement the message DOM element.
     * @param context the message validation context.
     */
    private void parseNamespaceValidationElements(Element messageElement, XmlMessageValidationContext context) {
        //check for validate elements, these elements can either have script, xpath or namespace validation information
        //for now we only handle namespace validation
        Map<String, String> validateNamespaces = new HashMap<String, String>();

        List<?> validateElements = DomUtils.getChildElementsByTagName(messageElement, "validate");
        if (validateElements.size() > 0) {
            for (Iterator<?> iter = validateElements.iterator(); iter.hasNext();) {
                Element validateElement = (Element) iter.next();

                //check for namespace validation elements
                List<?> validateNamespaceElements = DomUtils.getChildElementsByTagName(validateElement, "namespace");
                if (validateNamespaceElements.size() > 0) {
                    for (Iterator<?> namespaceIterator = validateNamespaceElements.iterator(); namespaceIterator.hasNext();) {
                        Element namespaceElement = (Element) namespaceIterator.next();
                        validateNamespaces.put(namespaceElement.getAttribute("prefix"), namespaceElement.getAttribute("value"));
                    }
                }
            }
            context.setControlNamespaces(validateNamespaces);
        }
    }

    /**
     * Parses validation elements and adds information to the message validation context.
     *
     * @param messageElement the message DOM element.
     * @param context the message validation context.
     */
    private void parseXPathValidationElements(Element messageElement, XpathMessageValidationContext context) {
        //check for validate elements, these elements can either have script, xpath or namespace validation information
        //for now we only handle xpath validation
        Map<String, Object> validateXpathExpressions = new HashMap<>();

        List<?> validateElements = DomUtils.getChildElementsByTagName(messageElement, "validate");
        if (validateElements.size() > 0) {
            for (Iterator<?> iter = validateElements.iterator(); iter.hasNext();) {
                Element validateElement = (Element) iter.next();
                extractXPathValidateExpressions(validateElement, validateXpathExpressions);
            }

            context.setXpathExpressions(validateXpathExpressions);
        }
    }

    /**
     * Extracts xpath validation expressions and fills map with them
     * @param validateElement
     * @param validateXpathExpressions
     */
    private void extractXPathValidateExpressions(
            Element validateElement, Map<String, Object> validateXpathExpressions) {
        //check for xpath validation - old style with direct attribute
        String pathExpression = validateElement.getAttribute("path");
        if (StringUtils.hasText(pathExpression) && !JsonPathMessageValidationContext.isJsonPathExpression(pathExpression)) {
            //construct pathExpression with explicit result-type, like boolean:/TestMessage/Value
            if (validateElement.hasAttribute("result-type")) {
                pathExpression = validateElement.getAttribute("result-type") + ":" + pathExpression;
            }

            validateXpathExpressions.put(pathExpression, validateElement.getAttribute("value"));
        }

        //check for xpath validation elements - new style preferred
        List<?> xpathElements = DomUtils.getChildElementsByTagName(validateElement, "xpath");
        if (xpathElements.size() > 0) {
            for (Iterator<?> xpathIterator = xpathElements.iterator(); xpathIterator.hasNext();) {
                Element xpathElement = (Element) xpathIterator.next();
                String expression = xpathElement.getAttribute("expression");
                if (StringUtils.hasText(expression)) {
                    //construct expression with explicit result-type, like boolean:/TestMessage/Value
                    if (xpathElement.hasAttribute("result-type")) {
                        expression = xpathElement.getAttribute("result-type") + ":" + expression;
                    }

                    validateXpathExpressions.put(expression, xpathElement.getAttribute("value"));
                }
            }
        }
    }

    /**
     * Extracts jsonPath validation expressions and fills map with them
     * @param validateElement
     * @param validateJsonPathExpressions
     */
    private void extractJsonPathValidateExpressions(
            Element validateElement, Map<String, Object> validateJsonPathExpressions) {
        //check for jsonPath validation - old style with direct attribute
        String pathExpression = validateElement.getAttribute("path");
        if (JsonPathMessageValidationContext.isJsonPathExpression(pathExpression)) {
            validateJsonPathExpressions.put(pathExpression, validateElement.getAttribute("value"));
        }

        //check for jsonPath validation elements - new style preferred
        ValidateMessageParserUtil.parseJsonPathElements(validateElement, validateJsonPathExpressions);
    }


    /**
     * Parse component returning generic bean definition.
     *
     * @param element
     * @return
     */
    protected BeanDefinitionBuilder parseComponent(Element element, ParserContext parserContext) {
        return BeanDefinitionBuilder.genericBeanDefinition(ReceiveMessageAction.class);
    }
}
