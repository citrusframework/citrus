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
import com.consol.citrus.validation.*;
import com.consol.citrus.validation.script.ScriptValidationContextBuilder;
import com.consol.citrus.validation.xml.XmlMessageValidationContextBuilder;
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
        if(StringUtils.hasText(receiveTimeout)) {
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

        builder.addPropertyValue("xmlMessageValidationContextBuilder", getMessagevalidationValidationContextBuilder(element, parserContext));
        builder.addPropertyValue("scriptValidationContextBuilder", getScriptValidationContextBuilder(element, parserContext));
        
        Element messageElement = DomUtils.getChildElementByTagName(element, "message");
        if (messageElement != null) {
            String messageValidator = messageElement.getAttribute("validator");
            if(StringUtils.hasText(messageValidator)) {
                builder.addPropertyReference("validator", messageValidator);
            }
        }
        
        builder.addPropertyValue("variableExtractors", getVariableExtractors(element, parserContext));

        return builder.getBeanDefinition();
    }

    /**
     * @param element
     * @param parserContext
     * @return
     */
    private List<VariableExtractor> getVariableExtractors(Element element, ParserContext parserContext) {
        List<VariableExtractor> variableExtractors = new ArrayList<VariableExtractor>();
        
        Element extractElement = DomUtils.getChildElementByTagName(element, "extract");
        Map<String, String> getMessageValues = new HashMap<String, String>();
        Map<String, String> getHeaderValues = new HashMap<String, String>();
        if (extractElement != null) {
            List<?> headerValueElements = DomUtils.getChildElementsByTagName(extractElement, "header");
            for (Iterator<?> iter = headerValueElements.iterator(); iter.hasNext();) {
                Element headerValue = (Element) iter.next();
                getHeaderValues.put(headerValue.getAttribute("name"), headerValue.getAttribute("variable"));
            }
            MessageHeaderVariableExtractor headerVariableExtractor = new MessageHeaderVariableExtractor();
            headerVariableExtractor.setHeaderMappings(getHeaderValues);
            
            variableExtractors.add(headerVariableExtractor);

            List<?> messageValueElements = DomUtils.getChildElementsByTagName(extractElement, "message");
            for (Iterator<?> iter = messageValueElements.iterator(); iter.hasNext();) {
                Element messageValue = (Element) iter.next();
                String pathExpression = messageValue.getAttribute("path");
                
                //construct pathExpression with explicit result-type, like boolean:/TestMessage/Value
                if(messageValue.hasAttribute("result-type")) {
                    pathExpression = messageValue.getAttribute("result-type") + ":" + pathExpression;
                }
                
                getMessageValues.put(pathExpression, messageValue.getAttribute("variable"));
            }
            
            XpathPayloadVariableExtractor payloadVariableExtractor = new XpathPayloadVariableExtractor();
            payloadVariableExtractor.setxPathExpressions(getMessageValues);
            
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
     * @param parserContext
     * @return
     */
    private XmlMessageValidationContextBuilder getMessagevalidationValidationContextBuilder(Element element, ParserContext parserContext) {
        XmlMessageValidationContextBuilder contextBuilder = new XmlMessageValidationContextBuilder();
        
        PayloadTemplateControlMessageBuilder payloadTemplateControlMessageBuilder = null;
        GroovyScriptControlMessageBuilder scriptControlMessageBuilder = null;
        
        Element messageElement = DomUtils.getChildElementByTagName(element, "message");
        
        if (messageElement != null) {
            String schemaValidation = messageElement.getAttribute("schema-validation");
            if(StringUtils.hasText(schemaValidation)) {
                contextBuilder.setSchemaValidation(Boolean.valueOf(schemaValidation));
            }
            
            Element payloadElement = DomUtils.getChildElementByTagName(messageElement, "payload");
            if (payloadElement != null) {
                payloadTemplateControlMessageBuilder = new PayloadTemplateControlMessageBuilder();
                payloadTemplateControlMessageBuilder.setPayloadData(PayloadElementParser.parseMessagePayload(payloadElement));
            }
            
            Element xmlDataElement = DomUtils.getChildElementByTagName(messageElement, "data");
            if (xmlDataElement != null) {
                payloadTemplateControlMessageBuilder = new PayloadTemplateControlMessageBuilder();
                payloadTemplateControlMessageBuilder.setPayloadData(DomUtils.getTextValue(xmlDataElement));
                
                Map<String, String> setMessageValues = new HashMap<String, String>();
                List<?> messageValueElements = DomUtils.getChildElementsByTagName(messageElement, "element");
                for (Iterator<?> iter = messageValueElements.iterator(); iter.hasNext();) {
                    Element messageValue = (Element) iter.next();
                    setMessageValues.put(messageValue.getAttribute("path"), messageValue.getAttribute("value"));
                }
                
                if (!setMessageValues.isEmpty()) {
                    XpathExpressionMessageConstructingInterceptor interceptor = new XpathExpressionMessageConstructingInterceptor(setMessageValues);
                    payloadTemplateControlMessageBuilder.addMessageConstructingInterceptor(interceptor);
                }
            }
    
            Element xmlResourceElement = DomUtils.getChildElementByTagName(messageElement, "resource");
            if (xmlResourceElement != null) {
                payloadTemplateControlMessageBuilder = new PayloadTemplateControlMessageBuilder();
                payloadTemplateControlMessageBuilder.setPayloadResource(FileUtils.getResourceFromFilePath(xmlResourceElement.getAttribute("file")));
                
                Map<String, String> setMessageValues = new HashMap<String, String>();
                List<?> messageValueElements = DomUtils.getChildElementsByTagName(messageElement, "element");
                for (Iterator<?> iter = messageValueElements.iterator(); iter.hasNext();) {
                    Element messageValue = (Element) iter.next();
                    setMessageValues.put(messageValue.getAttribute("path"), messageValue.getAttribute("value"));
                }
                
                if (!setMessageValues.isEmpty()) {
                    XpathExpressionMessageConstructingInterceptor interceptor = new XpathExpressionMessageConstructingInterceptor(setMessageValues);
                    payloadTemplateControlMessageBuilder.addMessageConstructingInterceptor(interceptor);
                }
            }
            
            Element scriptElement = DomUtils.getChildElementByTagName(messageElement, "script");
            if (scriptElement != null) {
                scriptControlMessageBuilder = new GroovyScriptControlMessageBuilder();
                scriptControlMessageBuilder.setScriptData(DomUtils.getTextValue(scriptElement));
            }
            
            Element scriptResourceElement = DomUtils.getChildElementByTagName(messageElement, "script-resource");
            if (scriptResourceElement != null) {
                scriptControlMessageBuilder = new GroovyScriptControlMessageBuilder();
                scriptControlMessageBuilder.setScriptResource(FileUtils.getResourceFromFilePath(scriptResourceElement.getAttribute("file")));
            }
            
            Set<String> ignoreExpressions = new HashSet<String>();
            List<?> ignoreElements = DomUtils.getChildElementsByTagName(messageElement, "ignore");
            for (Iterator<?> iter = ignoreElements.iterator(); iter.hasNext();) {
                Element ignoreValue = (Element) iter.next();
                ignoreExpressions.add(ignoreValue.getAttribute("path"));
            }
            contextBuilder.setIgnoreExpressions(ignoreExpressions);
            
            Map<String, String> validateExpressions = new HashMap<String, String>();
            List<?> validateElements = DomUtils.getChildElementsByTagName(messageElement, "validate");
            if (validateElements.size() > 0) {
                for (Iterator<?> iter = validateElements.iterator(); iter.hasNext();) {
                    Element validateValue = (Element) iter.next();
                    String pathExpression = validateValue.getAttribute("path");
                    
                    //construct pathExpression with explicit result-type, like boolean:/TestMessage/Value
                    if(validateValue.hasAttribute("result-type")) {
                        pathExpression = validateValue.getAttribute("result-type") + ":" + pathExpression;
                    }
                    
                    validateExpressions.put(pathExpression, validateValue.getAttribute("value"));
                }
                contextBuilder.setPathValidationExpressions(validateExpressions);
            }
            
            Map<String, String> namespaces = new HashMap<String, String>();
            List<?> namespaceElements = DomUtils.getChildElementsByTagName(messageElement, "namespace");
            if (namespaceElements.size() > 0) {
                for (Iterator<?> iter = namespaceElements.iterator(); iter.hasNext();) {
                    Element namespaceElement = (Element) iter.next();
                    namespaces.put(namespaceElement.getAttribute("prefix"), namespaceElement.getAttribute("value"));
                }
                contextBuilder.setNamespaces(namespaces);
            }
        }
        
        AbstractHeaderAwareControlMessageBuilder<String> controlMessageBuilder;
        
        if (payloadTemplateControlMessageBuilder != null) {
            controlMessageBuilder = payloadTemplateControlMessageBuilder;
        } else if (scriptControlMessageBuilder != null) {
            controlMessageBuilder = scriptControlMessageBuilder;
        } else {
            controlMessageBuilder = new PayloadTemplateControlMessageBuilder();
        }
        
        Element headerElement = DomUtils.getChildElementByTagName(element, "header");
        Map<String, Object> controlMessageHeaders = new HashMap<String, Object>();
        if (headerElement != null) {
            List<?> elements = DomUtils.getChildElementsByTagName(headerElement, "element");
            for (Iterator<?> iter = elements.iterator(); iter.hasNext();) {
                Element headerValue = (Element) iter.next();
                controlMessageHeaders.put(headerValue.getAttribute("name"), headerValue.getAttribute("value"));
            }
            
            controlMessageBuilder.setControlMessageHeaders(controlMessageHeaders);
        }
        
        contextBuilder.setMessageBuilder(controlMessageBuilder);
        
        return contextBuilder;
    }

    /**
     * Construct the message validation context builder.
     * @param messageElement
     * @param parserContext
     * @return
     */
    private ScriptValidationContextBuilder getScriptValidationContextBuilder(Element messageElement, ParserContext parserContext) {
        ScriptValidationContextBuilder contextBuilder = new ScriptValidationContextBuilder();
        
        Element validationScriptElement = DomUtils.getChildElementByTagName(messageElement, "validation-script");
        if (validationScriptElement != null) {
            contextBuilder.setValidationScript(DomUtils.getTextValue(validationScriptElement));
            
            String filePath = validationScriptElement.getAttribute("file");
            if (StringUtils.hasText(filePath)) {
                contextBuilder.setValidationScriptResource(FileUtils.getResourceFromFilePath(filePath));
            }
        }
        
        return contextBuilder;
    }

    /**
     * Parse component returning generic bean definition.
     * 
     * @param element
     * @param parserContext
     * @return
     */
    protected BeanDefinitionBuilder parseComponent(Element element, ParserContext parserContext) {
        return BeanDefinitionBuilder.genericBeanDefinition("com.consol.citrus.actions.ReceiveMessageAction");
    }
}
