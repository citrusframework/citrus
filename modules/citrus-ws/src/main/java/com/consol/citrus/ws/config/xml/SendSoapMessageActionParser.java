/*
 * Copyright 2006-2009 ConSol* Software GmbH.
 * 
 * This file is part of Citrus.
 * 
 *  Citrus is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Citrus is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Citrus.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.consol.citrus.ws.config.xml;

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

import com.consol.citrus.config.xml.DescriptionElementParser;

public class SendSoapMessageActionParser implements BeanDefinitionParser {

    public BeanDefinition parse(Element element, ParserContext parserContext) {
        String messageSenderReference = element.getAttribute("with");
        
        BeanDefinitionBuilder builder;

        if (StringUtils.hasText(messageSenderReference)) {
            builder = BeanDefinitionBuilder.genericBeanDefinition("com.consol.citrus.ws.actions.SendSoapMessageAction");
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
        }
        
        Element attachmentElement = DomUtils.getChildElementByTagName(element, "attachment");
        
        String contentId = attachmentElement.getAttribute("content-id");
        if(StringUtils.hasText(contentId)) {
            builder.addPropertyValue("contentId", contentId);
        }
        
        String contentType = attachmentElement.getAttribute("content-type");
        if(StringUtils.hasText(contentType)) {
            builder.addPropertyValue("contentType", contentType);
        }
        
        if (attachmentElement != null) {
            Element attachmentResourceElement = DomUtils.getChildElementByTagName(attachmentElement, "resource");
            if (attachmentResourceElement != null) {
                String attachmentFilePath = attachmentResourceElement.getAttribute("file");
                if (attachmentFilePath.startsWith("classpath:")) {
                    builder.addPropertyValue("attachment", new ClassPathResource(attachmentFilePath.substring("classpath:".length())));
                } else if (attachmentFilePath.startsWith("file:")) {
                    builder.addPropertyValue("attachment", new FileSystemResource(attachmentFilePath.substring("file:".length())));
                } else {
                    builder.addPropertyValue("attachment", new FileSystemResource(attachmentFilePath));
                }
            }
        }

        return builder.getBeanDefinition();
    }
}
