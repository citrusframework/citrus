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
import com.consol.citrus.validation.builder.*;
import com.consol.citrus.validation.interceptor.XpathMessageConstructionInterceptor;
import com.consol.citrus.validation.script.GroovyScriptMessageBuilder;
import com.consol.citrus.variable.MessageHeaderVariableExtractor;
import com.consol.citrus.variable.VariableExtractor;

/**
 * Bean definition parser for send action in test case.
 * 
 * @author Christoph Deppisch
 */
public class SendMessageActionParser implements BeanDefinitionParser {

    /**
     * @see org.springframework.beans.factory.xml.BeanDefinitionParser#parse(org.w3c.dom.Element, org.springframework.beans.factory.xml.ParserContext)
     */
    public BeanDefinition parse(Element element, ParserContext parserContext) {
        String messageSenderReference = element.getAttribute("with");
        
        BeanDefinitionBuilder builder;

        if (StringUtils.hasText(messageSenderReference)) {
            builder = parseComponent(element, parserContext);
            builder.addPropertyValue("name", element.getLocalName());

            builder.addPropertyReference("messageSender", messageSenderReference);
        } else {
            throw new BeanCreationException("Mandatory 'with' attribute has to be set!");
        }
        
        DescriptionElementParser.doParse(element, builder);

        MessageContentBuilder<?> messageBuidler = getMessageBuilder(element);
        if (messageBuidler != null) {
            builder.addPropertyValue("messageBuilder", messageBuidler);
        }

        List<VariableExtractor> variableExtractors = new ArrayList<VariableExtractor>();
        
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
        
        if (!variableExtractors.isEmpty()) {
            builder.addPropertyValue("variableExtractors", variableExtractors);
        }

        return builder.getBeanDefinition();
    }

    /**
     * Constructs a message builder from the given information in send action.
     * @param messageElement
     * @return
     */
    private MessageContentBuilder<?> getMessageBuilder(Element element) {
        PayloadTemplateMessageBuilder payloadTemplateMessageBuilder = null;
        GroovyScriptMessageBuilder scriptMessageBuilder = null;
        
        Element messageElement = DomUtils.getChildElementByTagName(element, "message");
        
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
        
        AbstractMessageContentBuilder<String> messageBuilder;
        
        if (payloadTemplateMessageBuilder != null) {
            messageBuilder = payloadTemplateMessageBuilder;
        } else if (scriptMessageBuilder != null) {
            messageBuilder = scriptMessageBuilder;
        } else {
            messageBuilder = new PayloadTemplateMessageBuilder();
        }
        
        Element headerElement = DomUtils.getChildElementByTagName(element, "header");
        Map<String, Object> setHeaderValues = new HashMap<String, Object>();
        if (headerElement != null) {
            List<?> elements = DomUtils.getChildElementsByTagName(headerElement, "element");
            for (Iterator<?> iter = elements.iterator(); iter.hasNext();) {
                Element headerValue = (Element) iter.next();
                setHeaderValues.put(headerValue.getAttribute("name"), headerValue.getAttribute("value"));
            }
            messageBuilder.setMessageHeaders(setHeaderValues);
            
            Element headerDataElement = DomUtils.getChildElementByTagName(headerElement, "data");
            if (headerDataElement != null) {
                messageBuilder.setMessageHeaderData(DomUtils.getTextValue(headerDataElement));
            }

            Element headerResourceElement = DomUtils.getChildElementByTagName(headerElement, "resource");
            if (headerResourceElement != null) {
                messageBuilder.setMessageHeaderResource(FileUtils.getResourceFromFilePath(headerResourceElement.getAttribute("file")));
            }
        }
        
        return messageBuilder;
    }

    /**
     * Parse component returning generic bean definition.
     * @param element
     * @param parserContext
     * @return
     */
    protected BeanDefinitionBuilder parseComponent(Element element, ParserContext parserContext) {
        return BeanDefinitionBuilder.genericBeanDefinition("com.consol.citrus.actions.SendMessageAction");
    }
}
