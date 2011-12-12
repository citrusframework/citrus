/*
 * Copyright 2006-2011 the original author or authors.
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
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.util.StringUtils;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

import com.consol.citrus.util.FileUtils;
import com.consol.citrus.validation.builder.AbstractMessageContentBuilder;
import com.consol.citrus.validation.builder.PayloadTemplateMessageBuilder;
import com.consol.citrus.validation.interceptor.XpathMessageConstructionInterceptor;
import com.consol.citrus.validation.script.GroovyScriptMessageBuilder;
import com.consol.citrus.variable.MessageHeaderVariableExtractor;
import com.consol.citrus.variable.VariableExtractor;

/**
 * Parser providing basic message element configurations used in send and receive actions.
 * 
 * @author Christoph Deppisch
 */
public abstract class AbstractMessageActionParser implements BeanDefinitionParser {

    /**
     * Static parse method taking care of basic message element parsing.
     * 
     * @param messageElement
     */
    public AbstractMessageContentBuilder<?> constructMessageBuilder(Element messageElement) {
        AbstractMessageContentBuilder<?> messageBuilder = null;
        
        if (messageElement != null) {
            messageBuilder = parsePayloadTemplateBuilder(messageElement);
            
            if (messageBuilder == null) {
                messageBuilder = parseScriptBuilder(messageElement);
            }
        }
        
        return messageBuilder != null ? messageBuilder : new PayloadTemplateMessageBuilder();
    }
    
    /**
     * @param messageElement
     * @return
     */
    private GroovyScriptMessageBuilder parseScriptBuilder(Element messageElement) {
        GroovyScriptMessageBuilder scriptMessageBuilder = null;
        
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
        
        return scriptMessageBuilder;
    }

    /**
     * Parses message payload template information given in message element.
     * @param messageElement
     */
    private PayloadTemplateMessageBuilder parsePayloadTemplateBuilder(Element messageElement) {
        PayloadTemplateMessageBuilder messageBuilder = null;
        
        messageBuilder = parsePayloadElement(messageElement);
        
        Element xmlDataElement = DomUtils.getChildElementByTagName(messageElement, "data");
        if (xmlDataElement != null) {
            messageBuilder = new PayloadTemplateMessageBuilder();
            messageBuilder.setPayloadData(DomUtils.getTextValue(xmlDataElement));
        }

        Element xmlResourceElement = DomUtils.getChildElementByTagName(messageElement, "resource");
        if (xmlResourceElement != null) {
            messageBuilder = new PayloadTemplateMessageBuilder();
            messageBuilder.setPayloadResource(FileUtils.getResourceFromFilePath(xmlResourceElement.getAttribute("file")));
        }
        
        if (messageBuilder != null) {
            Map<String, String> overwriteMessageValues = new HashMap<String, String>();
            List<?> messageValueElements = DomUtils.getChildElementsByTagName(messageElement, "element");
            for (Iterator<?> iter = messageValueElements.iterator(); iter.hasNext();) {
                Element messageValue = (Element) iter.next();
                overwriteMessageValues.put(messageValue.getAttribute("path"), messageValue.getAttribute("value"));
            }
            
            if (!overwriteMessageValues.isEmpty()) {
                XpathMessageConstructionInterceptor interceptor = new XpathMessageConstructionInterceptor(overwriteMessageValues);
                messageBuilder.addMessageConstructingInterceptor(interceptor);
            }
        } 
        
        return messageBuilder;
    }

    /**
     * Parses the xs:any payload elements nested in message element.
     * @param messageElement
     */
    private PayloadTemplateMessageBuilder parsePayloadElement(Element messageElement) {
        PayloadTemplateMessageBuilder messageBuilder = null;
        
        // parse payload with xs-any element
        Element payloadElement = DomUtils.getChildElementByTagName(messageElement, "payload");
        if (payloadElement != null) {
            messageBuilder = new PayloadTemplateMessageBuilder();
            messageBuilder.setPayloadData(PayloadElementParser.parseMessagePayload(payloadElement));
        }
        
        return messageBuilder;
    }

    /**
     * Parse message header elements in action and add headers to 
     * message content builder.
     * 
     * @param actionElement the action DOM element.
     * @param messageBuilder the message content builder.
     */
    protected void parseHeaderElements(Element actionElement, AbstractMessageContentBuilder<?> messageBuilder) {
        Element headerElement = DomUtils.getChildElementByTagName(actionElement, "header");
        Map<String, Object> messageHeaders = new HashMap<String, Object>();
        Map<String, Class> messageHeadersClasses = new HashMap<String, Class>();
        if (headerElement != null) {
            List<?> elements = DomUtils.getChildElementsByTagName(headerElement, "element");
            for (Iterator<?> iter = elements.iterator(); iter.hasNext();) {
                Element headerValue = (Element) iter.next();
                
                String name = headerValue.getAttribute("name");
                String value = headerValue.getAttribute("value");
                String type = headerValue.getAttribute("type");

                messageHeaders.put(name, value);
                messageHeadersClasses.put(name,String.class);
                if (type != null)
                {
                    try {
                        messageHeadersClasses.put(name, Class.forName("java.lang."+type));
                    } catch (ClassNotFoundException e) {
                        // keep String Default
                    }
                }

            }
            
            messageBuilder.setMessageHeaders(messageHeaders);
            messageBuilder.setMessageHeaderTypes(messageHeadersClasses);
        }
    }
    
    /**
     * Parses header extract information.
     * @param element the root action element.
     * @param variableExtractors the variable extractors to add new extractors to.
     */
    protected void parseExtractHeaderElements(Element element, List<VariableExtractor> variableExtractors) {
        Element extractElement = DomUtils.getChildElementByTagName(element, "extract");
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
        }
    }
}
