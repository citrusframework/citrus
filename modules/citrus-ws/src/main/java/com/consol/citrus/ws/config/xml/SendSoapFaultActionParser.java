/*
 * Copyright 2006-2010 ConSol* Software GmbH.
 * 
 * This file is part of Citrus.
 * 
 * Citrus is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Citrus is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Citrus. If not, see <http://www.gnu.org/licenses/>.
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

/**
 * Bean definition parser for send soap fault action in test case.
 * 
 * @author Christoph Deppisch
 * @since 2010
 */
public class SendSoapFaultActionParser implements BeanDefinitionParser {

    /**
     * @see org.springframework.beans.factory.xml.BeanDefinitionParser#parse(org.w3c.dom.Element, org.springframework.beans.factory.xml.ParserContext)
     */
    public BeanDefinition parse(Element element, ParserContext parserContext) {
        String messageSenderReference = element.getAttribute("with");
        
        if (!StringUtils.hasText(messageSenderReference)) {
            throw new BeanCreationException("Mandatory 'with' attribute has to be set!");
        }

        BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition("com.consol.citrus.ws.actions.SendSoapFaultAction");
        builder.addPropertyValue("name", element.getLocalName());

        builder.addPropertyReference("messageSender", messageSenderReference);
        
        DescriptionElementParser.doParse(element, builder);

        Element faultElement = DomUtils.getChildElementByTagName(element, "fault");
        if (faultElement != null) {
            Element faultCodeElement = DomUtils.getChildElementByTagName(faultElement, "fault-code");
            if (faultCodeElement != null) {
                builder.addPropertyValue("faultCode", DomUtils.getTextValue(faultCodeElement).trim());
            }
            
            Element faultStringElement = DomUtils.getChildElementByTagName(faultElement, "fault-string");
            if (faultStringElement != null) {
                builder.addPropertyValue("faultString", DomUtils.getTextValue(faultStringElement).trim());
            }
            
            Element faultDetailElement = DomUtils.getChildElementByTagName(faultElement, "fault-detail");
            if (faultDetailElement != null) {
                if(faultDetailElement.hasAttribute("file")) {
                    
                    if(StringUtils.hasText(DomUtils.getTextValue(faultDetailElement).trim())) {
                        throw new BeanCreationException("You tried to set fault-detail by file resource attribute and inline text value at the same time! " +
                        		"Please choose one of them.");
                    }
                    
                    String filePath = faultDetailElement.getAttribute("file");
                    
                    if (filePath.startsWith("classpath:")) {
                        builder.addPropertyValue("faultDetailResource", new ClassPathResource(filePath.substring("classpath:".length())));
                    } else if (filePath.startsWith("file:")) {
                        builder.addPropertyValue("faultDetailResource", new FileSystemResource(filePath.substring("file:".length())));
                    } else {
                        builder.addPropertyValue("faultDetailResource", new FileSystemResource(filePath));
                    }
                } else {
                    String faultDetailData = DomUtils.getTextValue(faultDetailElement).trim();
                    if(StringUtils.hasText(faultDetailData)) {
                        builder.addPropertyValue("faultDetail", faultDetailData);
                    } else {
                        throw new BeanCreationException("Not content for fault-detail is set! Either use file attribute or inline text value for fault-detail element.");
                    }
                }
            }
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
        
        return builder.getBeanDefinition();
    }
}
