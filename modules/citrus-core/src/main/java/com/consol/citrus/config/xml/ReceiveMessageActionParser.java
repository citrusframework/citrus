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

        Element messageElement = DomUtils.getChildElementByTagName(element, "message");
        if (messageElement != null) {
            String schemaValidation = messageElement.getAttribute("schema-validation");
            if(StringUtils.hasText(schemaValidation)) {
                builder.addPropertyValue("schemaValidationEnabled", schemaValidation);
            }
            
            String messageValidator = messageElement.getAttribute("validator");
            if(StringUtils.hasText(messageValidator)) {
                builder.addPropertyReference("validator", messageValidator);
            } else { //set default message validator defined in root-context
                builder.addPropertyReference("validator", "messageValidator");
            }
            
            // parse payload with xs-any element
            PayloadElementParser.doParse(DomUtils.getChildElementByTagName(messageElement, "payload"), builder);
            
            Element xmlDataElement = DomUtils.getChildElementByTagName(messageElement, "data");
            if (xmlDataElement != null) {
                builder.addPropertyValue("messageData", DomUtils.getTextValue(xmlDataElement));
            }

            Element xmlResourceElement = DomUtils.getChildElementByTagName(messageElement, "resource");
            if (xmlResourceElement != null) {
                builder.addPropertyValue("messageResource", FileUtils.getResourceFromFilePath(xmlResourceElement.getAttribute("file")));
            }
            
            Element scriptElement = DomUtils.getChildElementByTagName(messageElement, "script");
            if (scriptElement != null) {
                builder.addPropertyValue("scriptData", DomUtils.getTextValue(scriptElement));
            }
            
            Element scriptResourceElement = DomUtils.getChildElementByTagName(messageElement, "script-resource");
            if (scriptResourceElement != null) {
                builder.addPropertyValue("scriptResource", FileUtils.getResourceFromFilePath(scriptResourceElement.getAttribute("file")));
            }

            Map<String, String> setMessageValues = new HashMap<String, String>();
            List<?> messageValueElements = DomUtils.getChildElementsByTagName(messageElement, "element");
            for (Iterator<?> iter = messageValueElements.iterator(); iter.hasNext();) {
                Element messageValue = (Element) iter.next();
                setMessageValues.put(messageValue.getAttribute("path"), messageValue.getAttribute("value"));
            }
            builder.addPropertyValue("messageElements", setMessageValues);

            List<String> ignoreExpressions = new ArrayList<String>();
            List<?> ignoreElements = DomUtils.getChildElementsByTagName(messageElement, "ignore");
            for (Iterator<?> iter = ignoreElements.iterator(); iter.hasNext();) {
                Element ignoreValue = (Element) iter.next();
                ignoreExpressions.add(ignoreValue.getAttribute("path"));
            }
            builder.addPropertyValue("ignoreExpressions", ignoreExpressions);

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
                builder.addPropertyValue("pathValidationExpressions", validateExpressions);
            }
            
            Element validationScriptElement = DomUtils.getChildElementByTagName(messageElement, "validation-script");
            if (validationScriptElement != null) {
            	builder.addPropertyValue("validationScript", DomUtils.getTextValue(validationScriptElement));
            	String filePath = validationScriptElement.getAttribute("file");
            	if (StringUtils.hasText(filePath)) {
                    builder.addPropertyValue("validationScriptResource", FileUtils.getResourceFromFilePath(filePath));
            	}
            }
            
            Map<String, String> namespaces = new HashMap<String, String>();
            List<?> namespaceElements = DomUtils.getChildElementsByTagName(messageElement, "namespace");
            if (namespaceElements.size() > 0) {
                for (Iterator<?> iter = namespaceElements.iterator(); iter.hasNext();) {
                    Element namespaceElement = (Element) iter.next();
                    namespaces.put(namespaceElement.getAttribute("prefix"), namespaceElement.getAttribute("value"));
                }
                builder.addPropertyValue("namespaces", namespaces);
            }
        }

        Element headerElement = DomUtils.getChildElementByTagName(element, "header");
        Map<String, String> controlMessageHeaders = new HashMap<String, String>();
        if (headerElement != null) {
            List<?> elements = DomUtils.getChildElementsByTagName(headerElement, "element");
            for (Iterator<?> iter = elements.iterator(); iter.hasNext();) {
                Element headerValue = (Element) iter.next();
                controlMessageHeaders.put(headerValue.getAttribute("name"), headerValue.getAttribute("value"));
            }
            builder.addPropertyValue("controlMessageHeaders", controlMessageHeaders);
        }

        Element extractElement = DomUtils.getChildElementByTagName(element, "extract");
        Map<String, String> getMessageValues = new HashMap<String, String>();
        Map<String, String> getHeaderValues = new HashMap<String, String>();
        if (extractElement != null) {
            List<?> headerValueElements = DomUtils.getChildElementsByTagName(extractElement, "header");
            for (Iterator<?> iter = headerValueElements.iterator(); iter.hasNext();) {
                Element headerValue = (Element) iter.next();
                getHeaderValues.put(headerValue.getAttribute("name"), headerValue.getAttribute("variable"));
            }
            builder.addPropertyValue("extractHeaderValues", getHeaderValues);

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
            builder.addPropertyValue("extractMessageElements", getMessageValues);
        }

        return builder.getBeanDefinition();
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
