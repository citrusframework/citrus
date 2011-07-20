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

import java.util.*;

import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.StringUtils;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

import com.consol.citrus.util.FileUtils;
import com.consol.citrus.validation.builder.AbstractMessageContentBuilder;
import com.consol.citrus.validation.builder.PayloadTemplateMessageBuilder;
import com.consol.citrus.validation.context.ValidationContext;
import com.consol.citrus.validation.interceptor.XpathMessageConstructionInterceptor;
import com.consol.citrus.validation.script.GroovyScriptMessageBuilder;
import com.consol.citrus.validation.script.ScriptValidationContext;
import com.consol.citrus.validation.xml.XmlMessageValidationContext;
import com.consol.citrus.variable.*;

/**
 * Bean definition parser for receive action in test case.
 * 
 * @author Christoph Deppisch
 */
public class ReceiveMessageActionParser implements BeanDefinitionParser {

    /**
     * @see org.springframework.beans.factory.xml.BeanDefinitionParser#parse(org.w3c.dom.Element, org.springframework.beans.factory.xml.ParserContext)
     */
    public BeanDefinition parse(Element element, ParserContext parserContext) {
        String messageReceiverReference = element.getAttribute("with");
        
        BeanDefinitionBuilder builder;

        if (StringUtils.hasText(messageReceiverReference)) {
            builder = parseComponent(element, parserContext);
            builder.addPropertyValue("name", element.getLocalName());
            
            builder.addPropertyReference("messageReceiver", messageReceiverReference);
        } else {
            throw new BeanCreationException("Mandatory 'with' attribute has to be set!");
        }
        
        DescriptionElementParser.doParse(element, builder);

        String receiveTimeout = element.getAttribute("timeout");
        if (StringUtils.hasText(receiveTimeout)) {
            builder.addPropertyValue("receiveTimeout", Long.valueOf(receiveTimeout));
        }
        
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

        List<ValidationContext> validationContexts = new ArrayList<ValidationContext>();
        
        validationContexts.add(getXmlMessageValidationContext(element));
        validationContexts.add(getScriptValidationContext(element));
        
        builder.addPropertyValue("validationContexts", validationContexts);
        
        Element messageElement = DomUtils.getChildElementByTagName(element, "message");
        if (messageElement != null) {
            String messageValidator = messageElement.getAttribute("validator");
            if (StringUtils.hasText(messageValidator)) {
                builder.addPropertyReference("validator", messageValidator);
            }
            
            String messageType = messageElement.getAttribute("type");
            if (StringUtils.hasText(messageType)) {
                builder.addPropertyValue("messageType", messageType);
            }
        }
        
        builder.addPropertyValue("variableExtractors", getVariableExtractors(element));

        return builder.getBeanDefinition();
    }

    /**
     * Constructs a list of variable extractors.
     * @param element
     * @return
     */
    private List<VariableExtractor> getVariableExtractors(Element element) {
        List<VariableExtractor> variableExtractors = new ArrayList<VariableExtractor>();
        
        Element extractElement = DomUtils.getChildElementByTagName(element, "extract");
        Map<String, String> extractMessageValues = new HashMap<String, String>();
        Map<String, String> extractHeaderValues = new HashMap<String, String>();
        if (extractElement != null) {
            List<?> headerValueElements = DomUtils.getChildElementsByTagName(extractElement, "header");
            for (Iterator<?> iter = headerValueElements.iterator(); iter.hasNext();) {
                Element headerValue = (Element) iter.next();
                extractHeaderValues.put(headerValue.getAttribute("name"), headerValue.getAttribute("variable"));
            }
            MessageHeaderVariableExtractor headerVariableExtractor = new MessageHeaderVariableExtractor();
            headerVariableExtractor.setHeaderMappings(extractHeaderValues);
            
            variableExtractors.add(headerVariableExtractor);

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
     * @param messageElement
     * @return
     */
    private XmlMessageValidationContext getXmlMessageValidationContext(Element element) {
        XmlMessageValidationContext context = new XmlMessageValidationContext();
        
        PayloadTemplateMessageBuilder payloadTemplateMessageBuilder = null;
        GroovyScriptMessageBuilder scriptMessageBuilder = null;
        
        Element messageElement = DomUtils.getChildElementByTagName(element, "message");
        
        if (messageElement != null) {
            String schemaValidation = messageElement.getAttribute("schema-validation");
            if(StringUtils.hasText(schemaValidation)) {
                context.setSchemaValidation(Boolean.valueOf(schemaValidation));
            }
            
            Element payloadElement = DomUtils.getChildElementByTagName(messageElement, "payload");
            if (payloadElement != null) {
                payloadTemplateMessageBuilder = new PayloadTemplateMessageBuilder();
                payloadTemplateMessageBuilder.setPayloadData(PayloadElementParser.parseMessagePayload(payloadElement));
            }
            
            Element xmlDataElement = DomUtils.getChildElementByTagName(messageElement, "data");
            if (xmlDataElement != null) {
                payloadTemplateMessageBuilder = new PayloadTemplateMessageBuilder();
                payloadTemplateMessageBuilder.setPayloadData(DomUtils.getTextValue(xmlDataElement));
            }
    
            Element xmlResourceElement = DomUtils.getChildElementByTagName(messageElement, "resource");
            if (xmlResourceElement != null) {
                payloadTemplateMessageBuilder = new PayloadTemplateMessageBuilder();
                payloadTemplateMessageBuilder.setPayloadResource(FileUtils.getResourceFromFilePath(xmlResourceElement.getAttribute("file")));
            }
            
            if (payloadElement != null || xmlDataElement != null || xmlResourceElement != null) {
                Map<String, String> setMessageValues = new HashMap<String, String>();
                List<?> messageValueElements = DomUtils.getChildElementsByTagName(messageElement, "element");
                for (Iterator<?> iter = messageValueElements.iterator(); iter.hasNext();) {
                    Element messageValue = (Element) iter.next();
                    setMessageValues.put(messageValue.getAttribute("path"), messageValue.getAttribute("value"));
                }
                
                if (!setMessageValues.isEmpty()) {
                    XpathMessageConstructionInterceptor interceptor = new XpathMessageConstructionInterceptor(setMessageValues);
                    payloadTemplateMessageBuilder.addMessageConstructingInterceptor(interceptor);
                }
            }
            
            Element builderElement = DomUtils.getChildElementByTagName(messageElement, "builder");
            if (builderElement != null) {
                String builderType = builderElement.getAttribute("type");
                
                if (!StringUtils.hasText(builderType)) {
                    throw new BeanCreationException("Missing message builder type - please define valid type " +
                            "attribute for message builder");
                } else if (builderType.equals("groovy")) {
                    scriptMessageBuilder = new GroovyScriptMessageBuilder();
                } else {
                    throw new BeanCreationException("Unsupported message builder type: '" + builderType + "'");
                }
                
                String scriptResource = builderElement.getAttribute("file");
                
                if (StringUtils.hasText(scriptResource)) {
                    scriptMessageBuilder.setScriptResource(FileUtils.getResourceFromFilePath(scriptResource));
                } else {
                    scriptMessageBuilder.setScriptData(DomUtils.getTextValue(builderElement));
                }
            }
            
            Set<String> ignoreExpressions = new HashSet<String>();
            List<?> ignoreElements = DomUtils.getChildElementsByTagName(messageElement, "ignore");
            for (Iterator<?> iter = ignoreElements.iterator(); iter.hasNext();) {
                Element ignoreValue = (Element) iter.next();
                ignoreExpressions.add(ignoreValue.getAttribute("path"));
            }
            context.setIgnoreExpressions(ignoreExpressions);
            
            //check for validate elements, these elements can either have script, xpath or namespace validation information
            //script validation is handled separately for now we only handle xpath and namepsace validation
            Map<String, String> validateNamespaces = new HashMap<String, String>();
            Map<String, String> validateXpathExpressions = new HashMap<String, String>();
            List<?> validateElements = DomUtils.getChildElementsByTagName(messageElement, "validate");
            if (validateElements.size() > 0) {
                for (Iterator<?> iter = validateElements.iterator(); iter.hasNext();) {
                    Element validateElement = (Element) iter.next();
                    
                    //check for xpath validation - old style with direct attribute TODO: remove with next major version
                    String pathExpression = validateElement.getAttribute("path");
                    if (StringUtils.hasText(pathExpression)) {
                        //construct pathExpression with explicit result-type, like boolean:/TestMessage/Value
                        if(validateElement.hasAttribute("result-type")) {
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
                                if(xpathElement.hasAttribute("result-type")) {
                                    expression = xpathElement.getAttribute("result-type") + ":" + expression;
                                }
                                
                                validateXpathExpressions.put(expression, xpathElement.getAttribute("value"));
                            }
                        }
                    }
                    
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
        
        AbstractMessageContentBuilder<String> messageBuilder;
        
        if (payloadTemplateMessageBuilder != null) {
            messageBuilder = payloadTemplateMessageBuilder;
        } else if (scriptMessageBuilder != null) {
            messageBuilder = scriptMessageBuilder;
        } else {
            messageBuilder = new PayloadTemplateMessageBuilder();
        }
        
        Element headerElement = DomUtils.getChildElementByTagName(element, "header");
        Map<String, Object> controlMessageHeaders = new HashMap<String, Object>();
        if (headerElement != null) {
            List<?> elements = DomUtils.getChildElementsByTagName(headerElement, "element");
            for (Iterator<?> iter = elements.iterator(); iter.hasNext();) {
                Element headerValue = (Element) iter.next();
                controlMessageHeaders.put(headerValue.getAttribute("name"), headerValue.getAttribute("value"));
            }
            
            messageBuilder.setMessageHeaders(controlMessageHeaders);
        }
        
        context.setMessageBuilder(messageBuilder);
        
        return context;
    }

    /**
     * Construct the message validation context.
     * @param element
     * @return
     */
    private ScriptValidationContext getScriptValidationContext(Element element) {
        ScriptValidationContext context = new ScriptValidationContext();
        
        Element messageElement = DomUtils.getChildElementByTagName(element, "message");
        
        if (messageElement != null) {
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
                        
                        String filePath = scriptElement.getAttribute("file");
                        if (StringUtils.hasText(filePath)) {
                            context = new ScriptValidationContext(FileUtils.getResourceFromFilePath(filePath), type);
                        } else {
                            context = new ScriptValidationContext(DomUtils.getTextValue(scriptElement), type);
                        }
                    }
                }
            }
        }
        
        return context;
    }

    /**
     * Parse component returning generic bean definition.
     * 
     * @param element
     * @return
     */
    protected BeanDefinitionBuilder parseComponent(Element element, ParserContext parserContext) {
        return BeanDefinitionBuilder.genericBeanDefinition("com.consol.citrus.actions.ReceiveMessageAction");
    }
}
