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

package org.citrusframework.config.xml;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import org.citrusframework.actions.ReceiveMessageAction;
import org.citrusframework.config.util.ValidateMessageParserUtil;
import org.citrusframework.config.util.VariableExtractorParserUtil;
import org.citrusframework.validation.builder.DefaultMessageBuilder;
import org.citrusframework.validation.context.DefaultMessageValidationContext;
import org.citrusframework.validation.context.HeaderValidationContext;
import org.citrusframework.validation.context.MessageValidationContext;
import org.citrusframework.validation.context.ValidationContext;
import org.citrusframework.validation.json.JsonPathMessageValidationContext;
import org.citrusframework.validation.script.ScriptValidationContext;
import org.citrusframework.validation.xml.XmlMessageValidationContext;
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

import static java.lang.Boolean.parseBoolean;
import static org.citrusframework.util.StringUtils.hasText;

/**
 * Bean definition parser for receive action in test case.
 *
 */
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public class ReceiveMessageActionParser extends AbstractMessageActionParser {

    @Override
    public BeanDefinition parse(Element element, ParserContext parserContext) {
        BeanDefinitionBuilder builder = getBeanDefinitionBuilder(element, parserContext);

        String receiveTimeout = element.getAttribute("timeout");
        if (hasText(receiveTimeout)) {
            builder.addPropertyValue("receiveTimeout", Long.valueOf(receiveTimeout));
        }

        MessageSelectorParser.doParse(element, builder);

        Element messageElement = DomUtils.getChildElementByTagName(element, "message");
        List<ValidationContext.Builder<?, ?>> validationContexts = parseValidationContexts(messageElement, builder);

        DefaultMessageBuilder messageBuilder = constructMessageBuilder(messageElement, builder);
        parseHeaderElements(element, messageBuilder, validationContexts);

        builder.addPropertyValue("messageBuilder", messageBuilder);
        builder.addPropertyValue("validationContextBuilder", validationContexts);
        builder.addPropertyValue("variableExtractors", getVariableExtractors(element));

        return builder.getBeanDefinition();
    }

    /**
     * Parse message validation contexts.
     * @param messageElement
     * @param builder
     * @return
     */
    protected List<ValidationContext.Builder<?, ?>> parseValidationContexts(Element messageElement, BeanDefinitionBuilder builder) {
        List<ValidationContext.Builder<?, ?>> validationContexts = new ArrayList<>();
        if (messageElement != null) {
            String messageType = messageElement.getAttribute("type");
            if (hasText(messageType)) {
                builder.addPropertyValue("messageType", messageType);
            }

            addHeaderValidationContext(messageElement, validationContexts);
            addXmlValidationContext(messageElement, validationContexts);
            addJsonMessageValidationContext(messageElement, validationContexts);
            addMessageValidationContext(messageElement, validationContexts);
            addScriptValidationContext(messageElement, validationContexts);

            ManagedList<RuntimeBeanReference> validators = new ManagedList<>();
            String messageValidator = messageElement.getAttribute("validator");
            if (hasText(messageValidator)) {
                validators.add(new RuntimeBeanReference(messageValidator));
            }

            String messageValidatorExpression = messageElement.getAttribute("validators");
            if (hasText(messageValidatorExpression)) {
                Stream.of(messageValidatorExpression.split(","))
                        .map(String::trim)
                        .map(RuntimeBeanReference::new)
                        .forEach(validators::add);
            }

            if (!validators.isEmpty()) {
                builder.addPropertyValue("validators", validators);
            }

            String dataDictionary = messageElement.getAttribute("data-dictionary");
            if (hasText(dataDictionary)) {
                builder.addPropertyReference("dataDictionary", dataDictionary);
            }
        }

        return validationContexts;
    }

    private void addHeaderValidationContext(Element messageElement, List<ValidationContext.Builder<?, ?>> validationContexts) {
        String headerValidator = messageElement.getAttribute("header-validator");
        if (hasText(headerValidator)) {
            getHeaderValidationContext(validationContexts).validator(headerValidator);
        }

        String headerValidatorExpression = messageElement.getAttribute("header-validators");
        if (hasText(headerValidatorExpression)) {
            Stream.of(headerValidatorExpression.split(","))
                    .map(String::trim)
                    .forEach(getHeaderValidationContext(validationContexts)::validator);
        }
    }

    protected HeaderValidationContext.Builder getHeaderValidationContext(List<ValidationContext.Builder<?, ?>> validationContexts) {
        if (validationContexts.stream().noneMatch(HeaderValidationContext.Builder.class::isInstance)) {
            validationContexts.add(new HeaderValidationContext.Builder());
        }

        return validationContexts.stream()
                .filter(HeaderValidationContext.Builder.class::isInstance)
                .map(HeaderValidationContext.Builder.class::cast)
                .findFirst()
                .orElseThrow(() -> new BeanCreationException("Unable to initialize header validation context"));
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

    private MessageValidationContext.Builder<?, ?> getMessageValidationContext(List<ValidationContext.Builder<?, ?>> validationContexts) {
        if (validationContexts.stream().noneMatch(MessageValidationContext.Builder.class::isInstance)) {
            validationContexts.add(new DefaultMessageValidationContext.Builder());
        }

        return validationContexts.stream()
                .filter(MessageValidationContext.Builder.class::isInstance)
                .map(MessageValidationContext.Builder.class::cast)
                .findFirst()
                .orElseThrow(() -> new BeanCreationException("Unable to initialize message validation context"));
    }

    /**
     * Construct the basic message validation context.
     */
    private void addMessageValidationContext(Element messageElement, List<ValidationContext.Builder<?, ?>> validationContexts) {
        Set<String> ignoreExpressions = new HashSet<>();
        List<?> ignoreElements = DomUtils.getChildElementsByTagName(messageElement, "ignore");
        for (Object ignoreElement : ignoreElements) {
            Element ignoreValue = (Element) ignoreElement;
            ignoreExpressions.add(ignoreValue.getAttribute("path"));
        }

        if (!ignoreExpressions.isEmpty()) {
            MessageValidationContext.Builder<?, ?> messageValidationContext = getMessageValidationContext(validationContexts);
            ignoreExpressions.forEach(messageValidationContext::ignore);
        }

        addSchemaInformationToValidationContext(messageElement, validationContexts);
    }

    /**
     * Adds information about the validation of the message against a certain schema to the context
     * @param messageElement The message element to get the configuration from
     */
    protected void addSchemaInformationToValidationContext(Element messageElement, List<ValidationContext.Builder<?, ?>> validationContexts) {
        String schemaValidation = messageElement.getAttribute("schema-validation");
        if (hasText(schemaValidation)) {
            getMessageValidationContext(validationContexts).schemaValidation(parseBoolean(schemaValidation));
        }

        String schema = messageElement.getAttribute("schema");
        if (hasText(schema)) {
            getMessageValidationContext(validationContexts).schema(schema);
        }

        String schemaRepository = messageElement.getAttribute("schema-repository");
        if (hasText(schemaRepository)) {
            getMessageValidationContext(validationContexts).schemaRepository(schemaRepository);
        }
    }

    /**
     * Construct the basic Xml message validation context.
     */
    private void addXmlValidationContext(Element messageElement, List<ValidationContext.Builder<?, ?>> validationContexts) {
        parseNamespaceValidationElements(messageElement, validationContexts);

        //Catch namespace declarations for namespace context
        Map<String, String> namespaces = new HashMap<>();
        List<Element> namespaceElements = DomUtils.getChildElementsByTagName(messageElement, "namespace");
        for (Element namespaceElement : namespaceElements) {
            namespaces.put(namespaceElement.getAttribute("prefix"), namespaceElement.getAttribute("value"));
        }

        if (!namespaces.isEmpty()) {
            getXmlValidationContext(validationContexts).setNamespaces(namespaces);
        }

        parseXPathValidationElements(messageElement, validationContexts);
    }

    private XmlMessageValidationContext.Builder getXmlValidationContext(List<ValidationContext.Builder<?, ?>> validationContexts) {
        if (validationContexts.stream().noneMatch(MessageValidationContext.Builder.class::isInstance)) {
            XmlMessageValidationContext.Builder builder = new XmlMessageValidationContext.Builder();
            validationContexts.add(builder);
            return builder;
        } else {
            MessageValidationContext.Builder<?, ?> messageValidationContext = getMessageValidationContext(validationContexts);
            if (messageValidationContext instanceof XmlMessageValidationContext.Builder xmlMessageValidationContext) {
                return xmlMessageValidationContext;
            }

            XmlMessageValidationContext.Builder builder = XmlMessageValidationContext.Builder.adapt(messageValidationContext);
            validationContexts.remove(messageValidationContext);
            validationContexts.add(builder);
            return builder;
        }
    }

    /**
     * Construct the JSONPath message validation context.
     */
    private void addJsonMessageValidationContext(Element messageElement, List<ValidationContext.Builder<?, ?>> validationContexts) {
        //check for validate elements, these elements can either have script, jsonPath or namespace validation information
        //for now we only handle jsonPath validation
        Map<String, Object> validateJsonPathExpressions = new HashMap<>();
        List<Element> validateElements = DomUtils.getChildElementsByTagName(messageElement, "validate");
        if (!validateElements.isEmpty()) {
            for (Element validateElement : validateElements) {
                extractJsonPathValidateExpressions(validateElement, validateJsonPathExpressions);
            }

            if (!validateJsonPathExpressions.isEmpty()) {
                JsonPathMessageValidationContext.Builder context = new JsonPathMessageValidationContext.Builder();
                context.expressions(validateJsonPathExpressions);
                validationContexts.add(context);
            }
        }
    }

    /**
     * Adds script validation context if specified.
     */
    private void addScriptValidationContext(Element messageElement, List<ValidationContext.Builder<?, ?>> validationContexts) {
        List<Element> validateElements = DomUtils.getChildElementsByTagName(messageElement, "validate");
        if (!validateElements.isEmpty()) {
            ScriptValidationContext.Builder context = null;

            for (Element validateElement : validateElements) {
                Element scriptElement = DomUtils.getChildElementByTagName(validateElement, "script");

                // check for nested validate script child node
                if (scriptElement != null) {
                    if (context != null) {
                        throw new BeanCreationException("Found multiple validation script definitions - " +
                                "only supporting a single validation script for message validation");
                    }

                    String type = scriptElement.getAttribute("type");
                    context = new ScriptValidationContext.Builder().scriptType(type);

                    String filePath = scriptElement.getAttribute("file");
                    if (hasText(filePath)) {
                        context.scriptResource(filePath);
                        if (scriptElement.hasAttribute("charset")) {
                            context.scriptResourceCharset(scriptElement.getAttribute("charset"));
                        }
                    } else {
                        context.script(DomUtils.getTextValue(scriptElement));
                    }

                    validationContexts.add(context);
                }
            }
        }
    }

    /**
     * Parses validation elements and adds information to the message validation context.
     */
    private void parseNamespaceValidationElements(Element messageElement, List<ValidationContext.Builder<?, ?>> validationContexts) {
        //check for validate elements, these elements can either have script, xpath or namespace validation information
        //for now we only handle namespace validation
        Map<String, String> validateNamespaces = new HashMap<>();

        List<Element> validateElements = DomUtils.getChildElementsByTagName(messageElement, "validate");
        if (!validateElements.isEmpty()) {
            for (Element validateElement : validateElements) {
                //check for namespace validation elements
                List<Element> validateNamespaceElements = DomUtils.getChildElementsByTagName(validateElement, "namespace");
                if (!validateNamespaceElements.isEmpty()) {
                    for (Element namespaceElement : validateNamespaceElements) {
                        validateNamespaces.put(namespaceElement.getAttribute("prefix"), namespaceElement.getAttribute("value"));
                    }
                }
            }

            if (!validateNamespaces.isEmpty()) {
                getXmlValidationContext(validationContexts).namespaces(validateNamespaces);
            }
        }
    }

    /**
     * Parses validation elements and adds information to the message validation context.
     */
    private void parseXPathValidationElements(Element messageElement, List<ValidationContext.Builder<?, ?>> validationContexts) {
        //check for validate elements, these elements can either have script, xpath or namespace validation information
        //for now we only handle xpath validation
        Map<String, Object> validateXpathExpressions = new HashMap<>();

        List<Element> validateElements = DomUtils.getChildElementsByTagName(messageElement, "validate");
            for (Element validateElement : validateElements) {
                extractXPathValidateExpressions(validateElement, validateXpathExpressions);
            }

        if (!validateXpathExpressions.isEmpty()) {
            XmlMessageValidationContext.Builder xmlValidationContext = getXmlValidationContext(validationContexts);
            validationContexts.remove(xmlValidationContext);
            validationContexts.add(xmlValidationContext.xpath().expressions(validateXpathExpressions));
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
        if (hasText(pathExpression) && !JsonPathMessageValidationContext.isJsonPathExpression(pathExpression)) {
            //construct pathExpression with explicit result-type, like boolean:/TestMessage/Value
            if (validateElement.hasAttribute("result-type")) {
                pathExpression = validateElement.getAttribute("result-type") + ":" + pathExpression;
            }

            validateXpathExpressions.put(pathExpression, validateElement.getAttribute("value"));
        }

        //check for xpath validation elements - new style preferred
        List<?> xpathElements = DomUtils.getChildElementsByTagName(validateElement, "xpath");
        if (!xpathElements.isEmpty()) {
            for (Object element : xpathElements) {
                Element xpathElement = (Element) element;
                String expression = xpathElement.getAttribute("expression");
                if (hasText(expression)) {
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
    private void extractJsonPathValidateExpressions(Element validateElement, Map<String, Object> validateJsonPathExpressions) {
        //check for jsonPath validation - old style with direct attribute
        String pathExpression = validateElement.getAttribute("path");
        if (JsonPathMessageValidationContext.isJsonPathExpression(pathExpression)) {
            validateJsonPathExpressions.put(pathExpression, validateElement.getAttribute("value"));
        }

        //check for jsonPath validation elements - new style preferred
        ValidateMessageParserUtil.parseJsonPathElements(validateElement, validateJsonPathExpressions);
    }

    @Override
    protected void parseHeaderElements(Element actionElement, DefaultMessageBuilder messageBuilder, List<ValidationContext.Builder<?, ?>> validationContexts) {
        super.parseHeaderElements(actionElement, messageBuilder, validationContexts);

        Element headerElement = DomUtils.getChildElementByTagName(actionElement, "header");
        if (headerElement != null && headerElement.hasAttribute("ignore-case")) {
            boolean ignoreCase = Boolean.parseBoolean(headerElement.getAttribute("ignore-case"));
            getHeaderValidationContext(validationContexts).ignoreCase(ignoreCase);
        }
    }

    @Override
    protected Class<? extends AbstractReceiveMessageActionFactoryBean<?, ?, ?>> getMessageFactoryClass() {
        return ReceiveMessageActionFactoryBean.class;
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
