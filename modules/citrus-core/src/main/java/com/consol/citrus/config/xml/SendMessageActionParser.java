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
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.util.StringUtils;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

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

        Element messageElement = DomUtils.getChildElementByTagName(element, "message");
        if (messageElement != null) {
            Element xmlDataElement = DomUtils.getChildElementByTagName(messageElement, "data");
            if (xmlDataElement != null) {
                builder.addPropertyValue("messageData", DomUtils.getTextValue(xmlDataElement));
            }

            Element xmlResourceElement = DomUtils.getChildElementByTagName(messageElement, "resource");
            if (xmlResourceElement != null) {
                String filePath = xmlResourceElement.getAttribute("file");
                if (filePath.startsWith("classpath:")) {
                    builder.addPropertyValue("messageResource", new ClassPathResource(filePath.substring("classpath:".length())));
                } else if (filePath.startsWith("file:")) {
                    builder.addPropertyValue("messageResource", new FileSystemResource(filePath.substring("file:".length())));
                } else {
                    builder.addPropertyValue("messageResource", new FileSystemResource(filePath));
                }
            }
            
            Element scriptElement = DomUtils.getChildElementByTagName(messageElement, "script");
            if (scriptElement != null) {
                builder.addPropertyValue("scriptData", DomUtils.getTextValue(scriptElement));
            }
            
            Element scriptResourceElement = DomUtils.getChildElementByTagName(messageElement, "script-resource");
            if (scriptResourceElement != null) {
                String filePath = scriptResourceElement.getAttribute("file");
                if (filePath.startsWith("classpath:")) {
                    builder.addPropertyValue("scriptResource", new ClassPathResource(filePath.substring("classpath:".length())));
                } else if (filePath.startsWith("file:")) {
                    builder.addPropertyValue("scriptResource", new FileSystemResource(filePath.substring("file:".length())));
                } else {
                    builder.addPropertyValue("scriptResource", new FileSystemResource(filePath));
                }
            }

            Map<String, String> setMessageValues = new HashMap<String, String>();
            List<?> messageValueElements = DomUtils.getChildElementsByTagName(messageElement, "element");
            for (Iterator<?> iter = messageValueElements.iterator(); iter.hasNext();) {
                Element messageValue = (Element) iter.next();
                setMessageValues.put(messageValue.getAttribute("path"), messageValue.getAttribute("value"));
            }
            builder.addPropertyValue("messageElements", setMessageValues);
        }

        Element headerElement = DomUtils.getChildElementByTagName(element, "header");
        Map<String, String> setHeaderValues = new HashMap<String, String>();
        if (headerElement != null) {
            List<?> elements = DomUtils.getChildElementsByTagName(headerElement, "element");
            for (Iterator<?> iter = elements.iterator(); iter.hasNext();) {
                Element headerValue = (Element) iter.next();
                setHeaderValues.put(headerValue.getAttribute("name"), headerValue.getAttribute("value"));
            }
            builder.addPropertyValue("headerValues", setHeaderValues);
            
            Element headerDataElement = DomUtils.getChildElementByTagName(headerElement, "data");
            if (headerDataElement != null) {
                builder.addPropertyValue("headerData", DomUtils.getTextValue(headerDataElement));
            }

            Element headerResourceElement = DomUtils.getChildElementByTagName(headerElement, "resource");
            if (headerResourceElement != null) {
                String filePath = headerResourceElement.getAttribute("file");
                if (filePath.startsWith("classpath:")) {
                    builder.addPropertyValue("headerResource", new ClassPathResource(filePath.substring("classpath:".length())));
                } else if (filePath.startsWith("file:")) {
                    builder.addPropertyValue("headerResource", new FileSystemResource(filePath.substring("file:".length())));
                } else {
                    builder.addPropertyValue("headerResource", new FileSystemResource(filePath));
                }
            }
        }
        
        Element extractElement = DomUtils.getChildElementByTagName(element, "extract");
        Map<String, String> getHeaderValues = new HashMap<String, String>();
        if (extractElement != null) {
            List<?> headerValueElements = DomUtils.getChildElementsByTagName(extractElement, "header");
            for (Iterator<?> iter = headerValueElements.iterator(); iter.hasNext();) {
                Element headerValue = (Element) iter.next();
                getHeaderValues.put(headerValue.getAttribute("name"), headerValue.getAttribute("variable"));
            }
            builder.addPropertyValue("extractHeaderValues", getHeaderValues);
        }

        return builder.getBeanDefinition();
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
