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
        PayloadTemplateMessageBuilder payloadTemplateMessageBuilder = null;
        GroovyScriptMessageBuilder scriptMessageBuilder = null;
        
        if (messageElement != null) {
            // parse payload with xs-any element
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
        }
        
        if (payloadTemplateMessageBuilder != null) {
            return payloadTemplateMessageBuilder;
        } else if (scriptMessageBuilder != null) {
            return scriptMessageBuilder;
        } else {
            return new PayloadTemplateMessageBuilder();
        }
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
        if (headerElement != null) {
            List<?> elements = DomUtils.getChildElementsByTagName(headerElement, "element");
            for (Iterator<?> iter = elements.iterator(); iter.hasNext();) {
                Element headerValue = (Element) iter.next();
                messageHeaders.put(headerValue.getAttribute("name"), headerValue.getAttribute("value"));
            }
            
            messageBuilder.setMessageHeaders(messageHeaders);
        }
    }
}
