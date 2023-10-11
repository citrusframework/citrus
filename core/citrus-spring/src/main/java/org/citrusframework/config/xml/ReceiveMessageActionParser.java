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

package org.citrusframework.config.xml;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import org.citrusframework.CitrusSettings;
import org.citrusframework.actions.ReceiveMessageAction;
import org.citrusframework.config.util.BeanDefinitionParserUtils;
import org.citrusframework.config.util.ValidateMessageParserUtil;
import org.citrusframework.config.util.VariableExtractorParserUtil;
import org.citrusframework.util.StringUtils;
import org.citrusframework.validation.builder.DefaultMessageBuilder;
import org.citrusframework.validation.context.HeaderValidationContext;
import org.citrusframework.validation.context.SchemaValidationContext;
import org.citrusframework.validation.context.ValidationContext;
import org.citrusframework.validation.json.JsonMessageValidationContext;
import org.citrusframework.validation.json.JsonPathMessageValidationContext;
import org.citrusframework.validation.script.ScriptValidationContext;
import org.citrusframework.validation.xml.XmlMessageValidationContext;
import org.citrusframework.validation.xml.XpathMessageValidationContext;
import org.citrusframework.variable.VariableExtractor;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.ManagedList;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.CollectionUtils;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

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

        if (endpointUri.contains(":") || (endpointUri.contains(CitrusSettings.VARIABLE_PREFIX) && endpointUri.contains(CitrusSettings.VARIABLE_SUFFIX))) {
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

        DefaultMessageBuilder messageBuilder = constructMessageBuilder(messageElement, builder);
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
            if (StringUtils.hasText(messageType)) {
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

            ScriptValidationContext scriptValidationContext = getScriptValidationContext(messageElement);
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
            Map<String, Object> extractFromPath = new LinkedHashMap<>();

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
        JsonMessageValidationContext.Builder context = new JsonMessageValidationContext.Builder();

        if (messageElement != null) {
            Set<String> ignoreExpressions = new HashSet<String>();
            List<?> ignoreElements = DomUtils.getChildElementsByTagName(messageElement, "ignore");
            for (Iterator<?> iter = ignoreElements.iterator(); iter.hasNext(); ) {
                Element ignoreValue = (Element) iter.next();
                ignoreExpressions.add(ignoreValue.getAttribute("path"));
            }
            ignoreExpressions.forEach(context::ignore);

            addSchemaInformationToValidationContext(messageElement, context);
        }

        return context.build();
    }

    /**
     * Construct the basic Xml message validation context.
     * @param messageElement
     * @return
     */
    private XmlMessageValidationContext getXmlMessageValidationContext(Element messageElement) {
        XmlMessageValidationContext.Builder context = new XmlMessageValidationContext.Builder();

        if (messageElement != null) {
            addSchemaInformationToValidationContext(messageElement, context);

            Set<String> ignoreExpressions = new HashSet<String>();
            List<Element> ignoreElements = DomUtils.getChildElementsByTagName(messageElement, "ignore");
            for (Element ignoreValue : ignoreElements) {
                ignoreExpressions.add(ignoreValue.getAttribute("path"));
            }
            ignoreExpressions.forEach(context::ignore);

            parseNamespaceValidationElements(messageElement, context);

            //Catch namespace declarations for namespace context
            Map<String, String> namespaces = new HashMap<String, String>();
            List<Element> namespaceElements = DomUtils.getChildElementsByTagName(messageElement, "namespace");
            if (namespaceElements.size() > 0) {
                for (Element namespaceElement : namespaceElements) {
                    namespaces.put(namespaceElement.getAttribute("prefix"), namespaceElement.getAttribute("value"));
                }
                context.setNamespaces(namespaces);
            }
        }

        return context.build();
    }

    /**
     * Adds information about the validation of the message against a certain schema to the context
     * @param messageElement The message element to get the configuration from
     * @param context The context to set the schema validation configuration to
     */
    private void addSchemaInformationToValidationContext(Element messageElement, SchemaValidationContext.Builder<?> context) {
        String schemaValidation = messageElement.getAttribute("schema-validation");
        if (StringUtils.hasText(schemaValidation)) {
            context.schemaValidation(Boolean.valueOf(schemaValidation));
        }

        String schema = messageElement.getAttribute("schema");
        if (StringUtils.hasText(schema)) {
            context.schema(schema);
        }

        String schemaRepository = messageElement.getAttribute("schema-repository");
        if (StringUtils.hasText(schemaRepository)) {
            context.schemaRepository(schemaRepository);
        }
    }

    /**
     * Construct the XPath message validation context.
     * @param messageElement
     * @param parentContext
     * @return
     */
    private XpathMessageValidationContext getXPathMessageValidationContext(Element messageElement, XmlMessageValidationContext parentContext) {
        XpathMessageValidationContext.Builder context = new XpathMessageValidationContext.Builder();

        parseXPathValidationElements(messageElement, context);

        context.setNamespaces(parentContext.getNamespaces());
        context.namespaces(parentContext.getControlNamespaces());
        parentContext.getIgnoreExpressions().forEach(context::ignore);
        context.schema(parentContext.getSchema());
        context.schemaRepository(parentContext.getSchemaRepository());
        context.schemaValidation(parentContext.isSchemaValidationEnabled());

        return context.build();
    }

    /**
     * Construct the JSONPath message validation context.
     * @param messageElement
     * @return
     */
    private JsonPathMessageValidationContext getJsonPathMessageValidationContext(Element messageElement) {
        JsonPathMessageValidationContext.Builder context = new JsonPathMessageValidationContext.Builder();

        //check for validate elements, these elements can either have script, jsonPath or namespace validation information
        //for now we only handle jsonPath validation
        Map<String, Object> validateJsonPathExpressions = new HashMap<>();
        List<Element> validateElements = DomUtils.getChildElementsByTagName(messageElement, "validate");
        if (validateElements.size() > 0) {
            for (Element validateElement : validateElements) {
                extractJsonPathValidateExpressions(validateElement, validateJsonPathExpressions);
            }

            context.expressions(validateJsonPathExpressions);
        }

        return context.build();
    }

    /**
     * Construct the message validation context.
     * @param messageElement
     * @return
     */
    private ScriptValidationContext getScriptValidationContext(Element messageElement) {
        ScriptValidationContext.Builder context;

        boolean done = false;
        List<Element> validateElements = DomUtils.getChildElementsByTagName(messageElement, "validate");
        if (validateElements.size() > 0) {
            for (Element validateElement : validateElements) {
                Element scriptElement = DomUtils.getChildElementByTagName(validateElement, "script");

                // check for nested validate script child node
                if (scriptElement != null) {
                    if (!done) {
                        done = true;
                    } else {
                        throw new BeanCreationException("Found multiple validation script definitions - " +
                                "only supporting a single validation script for message validation");
                    }

                    String type = scriptElement.getAttribute("type");
                    context = new ScriptValidationContext.Builder()
                            .scriptType(type);

                    String filePath = scriptElement.getAttribute("file");
                    if (StringUtils.hasText(filePath)) {
                        context.scriptResource(filePath);
                        if (scriptElement.hasAttribute("charset")) {
                            context.scriptResourceCharset(scriptElement.getAttribute("charset"));
                        }
                    } else {
                        context.script(DomUtils.getTextValue(scriptElement));
                    }
                    return context.build();
                }
            }
        }

        return null;
    }

    /**
     * Parses validation elements and adds information to the message validation context.
     *
     * @param messageElement the message DOM element.
     * @param context the message validation context.
     */
    private void parseNamespaceValidationElements(Element messageElement, XmlMessageValidationContext.Builder context) {
        //check for validate elements, these elements can either have script, xpath or namespace validation information
        //for now we only handle namespace validation
        Map<String, String> validateNamespaces = new HashMap<>();

        List<Element> validateElements = DomUtils.getChildElementsByTagName(messageElement, "validate");
        if (validateElements.size() > 0) {
            for (Element validateElement : validateElements) {
                //check for namespace validation elements
                List<Element> validateNamespaceElements = DomUtils.getChildElementsByTagName(validateElement, "namespace");
                if (validateNamespaceElements.size() > 0) {
                    for (Element namespaceElement : validateNamespaceElements) {
                        validateNamespaces.put(namespaceElement.getAttribute("prefix"), namespaceElement.getAttribute("value"));
                    }
                }
            }
            context.namespaces(validateNamespaces);
        }
    }

    /**
     * Parses validation elements and adds information to the message validation context.
     *
     * @param messageElement the message DOM element.
     * @param context the message validation context.
     */
    private void parseXPathValidationElements(Element messageElement, XpathMessageValidationContext.Builder context) {
        //check for validate elements, these elements can either have script, xpath or namespace validation information
        //for now we only handle xpath validation
        Map<String, Object> validateXpathExpressions = new HashMap<>();

        List<Element> validateElements = DomUtils.getChildElementsByTagName(messageElement, "validate");
        if (validateElements.size() > 0) {
            for (Element validateElement : validateElements) {
                extractXPathValidateExpressions(validateElement, validateXpathExpressions);
            }

            context.expressions(validateXpathExpressions);
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
        return BeanDefinitionBuilder.genericBeanDefinition(ReceiveMessageActionFactoryBean.class);
    }

    /**
     * Test action factory bean.
     */
    public static class ReceiveMessageActionFactoryBean extends AbstractReceiveMessageActionFactoryBean<ReceiveMessageAction, ReceiveMessageAction.ReceiveMessageActionBuilderSupport, ReceiveMessageAction.Builder> {

        private final ReceiveMessageAction.Builder builder = new ReceiveMessageAction.Builder();

        @Override
        public ReceiveMessageAction getObject() throws Exception {
            return builder.build();
        }

        @Override
        public Class<?> getObjectType() {
            return ReceiveMessageAction.class;
        }

        /**
         * Obtains the builder.
         * @return the builder implementation.
         */
        @Override
        public ReceiveMessageAction.Builder getBuilder() {
            return builder;
        }
    }
}
