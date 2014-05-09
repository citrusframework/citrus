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

import com.consol.citrus.CitrusConstants;
import com.consol.citrus.actions.ReceiveMessageAction;
import com.consol.citrus.config.util.BeanDefinitionParserUtils;
import com.consol.citrus.validation.builder.AbstractMessageContentBuilder;
import com.consol.citrus.validation.context.ValidationContext;
import com.consol.citrus.validation.script.ScriptValidationContext;
import com.consol.citrus.validation.xml.XmlMessageValidationContext;
import com.consol.citrus.variable.VariableExtractor;
import com.consol.citrus.variable.XpathPayloadVariableExtractor;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.StringUtils;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

import java.util.*;

/**
 * Bean definition parser for receive action in test case.
 * 
 * @author Christoph Deppisch
 */
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public class ReceiveMessageActionParser extends AbstractMessageActionParser {

    /**
     * @see org.springframework.beans.factory.xml.BeanDefinitionParser#parse(org.w3c.dom.Element, org.springframework.beans.factory.xml.ParserContext)
     */
    public BeanDefinition parse(Element element, ParserContext parserContext) {
        String endpointUri;
        if (element.hasAttribute("with")) {
            endpointUri = element.getAttribute("with");
        } else if (element.hasAttribute("endpoint")) {
            endpointUri = element.getAttribute("endpoint");
        } else {
            throw new BeanCreationException("Missing proper message endpoint reference for receiving action - 'endpoint' attribute is required and should not be empty");
        }

        if (!StringUtils.hasText(endpointUri)) {
            throw new BeanCreationException("Endpoint reference must not be empty");
        }

        BeanDefinitionBuilder builder = parseComponent(element, parserContext);
        builder.addPropertyValue("name", element.getLocalName());

        if (endpointUri.contains(":")) {
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
        
        parseMessageSelector(element, builder);

        List<ValidationContext> validationContexts = new ArrayList<ValidationContext>();
        
        validationContexts.add(getXmlMessageValidationContext(element));

        Element messageElement = DomUtils.getChildElementByTagName(element, "message");
        if (messageElement != null) {
            String messageType = messageElement.getAttribute("type");
            if (!StringUtils.hasText(messageType)) {
                messageType = CitrusConstants.DEFAULT_MESSAGE_TYPE;
            } else {
                builder.addPropertyValue("messageType", messageType);
            }

            validationContexts.add(getScriptValidationContext(messageElement, messageType));

            String messageValidator = messageElement.getAttribute("validator");
            if (StringUtils.hasText(messageValidator)) {
                builder.addPropertyReference("validator", messageValidator);
            }

            String dataDictionary = messageElement.getAttribute("data-dictionary");
            if (StringUtils.hasText(dataDictionary)) {
                builder.addPropertyReference("dataDictionary", dataDictionary);
            }
        }

        builder.addPropertyValue("validationContexts", validationContexts);
        
        builder.addPropertyValue("variableExtractors", getVariableExtractors(element));

        return builder.getBeanDefinition();
    }

    /**
     * Added message selector if set.
     * @param element
     * @param builder
     */
    private void parseMessageSelector(Element element, BeanDefinitionBuilder builder) {
        Element messageSelectorElement = DomUtils.getChildElementByTagName(element, "selector");
        if (messageSelectorElement != null) {
            Element selectorStringElement = DomUtils.getChildElementByTagName(messageSelectorElement, "value");
            if (selectorStringElement != null) {
                builder.addPropertyValue("messageSelectorString", DomUtils.getTextValue(selectorStringElement));
            }

            Map<String, String> messageSelector = new HashMap<String, String>();
            List<?> messageSelectorElements = DomUtils.getChildElementsByTagName(messageSelectorElement, "element");
            for (Iterator<?> iter = messageSelectorElements.iterator(); iter.hasNext();) {
                Element selectorElement = (Element) iter.next();
                messageSelector.put(selectorElement.getAttribute("name"), selectorElement.getAttribute("value"));
            }
            builder.addPropertyValue("messageSelector", messageSelector);
        }
    }

    /**
     * Constructs a list of variable extractors.
     * @param element
     * @return
     */
    private List<VariableExtractor> getVariableExtractors(Element element) {
        List<VariableExtractor> variableExtractors = new ArrayList<VariableExtractor>();

        parseExtractHeaderElements(element, variableExtractors);
        
        Element extractElement = DomUtils.getChildElementByTagName(element, "extract");
        Map<String, String> extractMessageValues = new HashMap<String, String>();
        if (extractElement != null) {
            List<?> messageValueElements = DomUtils.getChildElementsByTagName(extractElement, "message");
            for (Iterator<?> iter = messageValueElements.iterator(); iter.hasNext();) {
                Element messageValue = (Element) iter.next();
                String pathExpression = messageValue.getAttribute("path");
                
                //construct pathExpression with explicit result-type, like boolean:/TestMessage/Value
                if (messageValue.hasAttribute("result-type")) {
                    pathExpression = messageValue.getAttribute("result-type") + ":" + pathExpression;
                }
                
                extractMessageValues.put(pathExpression, messageValue.getAttribute("variable"));
            }
            
            XpathPayloadVariableExtractor payloadVariableExtractor = new XpathPayloadVariableExtractor();
            payloadVariableExtractor.setxPathExpressions(extractMessageValues);
            
            Map<String, String> namespaces = new HashMap<String, String>();
            Element messageElement = DomUtils.getChildElementByTagName(element, "message");
            if (messageElement != null) {
                List<?> namespaceElements = DomUtils.getChildElementsByTagName(messageElement, "namespace");
                if (namespaceElements.size() > 0) {
                    for (Iterator<?> iter = namespaceElements.iterator(); iter.hasNext();) {
                        Element namespaceElement = (Element) iter.next();
                        namespaces.put(namespaceElement.getAttribute("prefix"), namespaceElement.getAttribute("value"));
                    }
                    payloadVariableExtractor.setNamespaces(namespaces);
                }
            }
            
            variableExtractors.add(payloadVariableExtractor);
        }
        
        return variableExtractors;
    }

    /**
     * Construct the message validation context builder.
     * @param element
     * @return
     */
    private XmlMessageValidationContext getXmlMessageValidationContext(Element element) {
        XmlMessageValidationContext context = new XmlMessageValidationContext();
        
        Element messageElement = DomUtils.getChildElementByTagName(element, "message");

        if (messageElement != null) {
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
            
            Set<String> ignoreExpressions = new HashSet<String>();
            List<?> ignoreElements = DomUtils.getChildElementsByTagName(messageElement, "ignore");
            for (Iterator<?> iter = ignoreElements.iterator(); iter.hasNext();) {
                Element ignoreValue = (Element) iter.next();
                ignoreExpressions.add(ignoreValue.getAttribute("path"));
            }
            context.setIgnoreExpressions(ignoreExpressions);
            
            parseValidationElements(messageElement, context);
            
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

        AbstractMessageContentBuilder<?> messageBuilder = constructMessageBuilder(messageElement);
        parseHeaderElements(element, messageBuilder);
        
        context.setMessageBuilder(messageBuilder);
        
        return context;
    }

    /**
     * Construct the message validation context.
     * @param messageElement
     * @return
     */
    private ScriptValidationContext getScriptValidationContext(Element messageElement, String messageType) {
        ScriptValidationContext context = new ScriptValidationContext(messageType);

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

                    String type = scriptElement.getAttribute("type");
                    context.setScriptType(type);

                    String filePath = scriptElement.getAttribute("file");
                    if (StringUtils.hasText(filePath)) {
                        context.setValidationScriptResourcePath(filePath);
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
    private void parseValidationElements(Element messageElement, XmlMessageValidationContext context) {
        //check for validate elements, these elements can either have script, xpath or namespace validation information
        //script validation is handled separately for now we only handle xpath and namepsace validation
        Map<String, String> validateNamespaces = new HashMap<String, String>();
        Map<String, String> validateXpathExpressions = new HashMap<String, String>();

        List<?> validateElements = DomUtils.getChildElementsByTagName(messageElement, "validate");
        if (validateElements.size() > 0) {
            for (Iterator<?> iter = validateElements.iterator(); iter.hasNext();) {
                Element validateElement = (Element) iter.next();

                extractXPathValidateExpressions(validateElement, validateXpathExpressions);
                
                //check for namespace validation elements
                List<?> validateNamespaceElements = DomUtils.getChildElementsByTagName(validateElement, "namespace");
                if (validateNamespaceElements.size() > 0) {
                    for (Iterator<?> namespaceIterator = validateNamespaceElements.iterator(); namespaceIterator.hasNext();) {
                        Element namespaceElement = (Element) namespaceIterator.next();
                        validateNamespaces.put(namespaceElement.getAttribute("prefix"), namespaceElement.getAttribute("value"));
                    }
                }
            }
            context.setPathValidationExpressions(validateXpathExpressions);
            context.setControlNamespaces(validateNamespaces);
        }
    }

    /**
     * Extracts xpath validation expressions and fills map with them
     * @param validateElement
     * @param validateXpathExpressions
     */
    private void extractXPathValidateExpressions(
            Element validateElement, Map<String, String> validateXpathExpressions) {
        //check for xpath validation - old style with direct attribute TODO: remove with next major version
        String pathExpression = validateElement.getAttribute("path");
        if (StringUtils.hasText(pathExpression)) {
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
     * Parse component returning generic bean definition.
     *
     * @param element
     * @return
     */
    protected BeanDefinitionBuilder parseComponent(Element element, ParserContext parserContext) {
        return BeanDefinitionBuilder.genericBeanDefinition(ReceiveMessageAction.class);
    }
}
