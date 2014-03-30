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

package com.consol.citrus.ws.config.xml;

import com.consol.citrus.actions.SendMessageAction;
import com.consol.citrus.config.xml.DescriptionElementParser;
import com.consol.citrus.ws.message.builder.SoapFaultAwareMessageBuilder;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.StringUtils;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

import java.util.*;

/**
 * Bean definition parser for send soap fault action in test case.
 * 
 * @author Christoph Deppisch
 */
public class SendSoapFaultActionParser implements BeanDefinitionParser {

    /**
     * @see org.springframework.beans.factory.xml.BeanDefinitionParser#parse(org.w3c.dom.Element, org.springframework.beans.factory.xml.ParserContext)
     */
    public BeanDefinition parse(Element element, ParserContext parserContext) {
        String messageSenderReference = null;
        if (element.hasAttribute("with")) {
            messageSenderReference = element.getAttribute("with");
        } else if (element.hasAttribute("endpoint")) {
            messageSenderReference = element.getAttribute("endpoint");
        }

        BeanDefinitionBuilder builder;
        if (StringUtils.hasText(messageSenderReference)) {
            builder = BeanDefinitionBuilder.genericBeanDefinition(SendMessageAction.class);
            builder.addPropertyValue("name", element.getLocalName());

            builder.addPropertyReference("endpoint", messageSenderReference);
        } else {
            throw new BeanCreationException("Missing proper message endpoint reference for sending action - 'endpoint' attribute is required and should not be empty");
        }

        DescriptionElementParser.doParse(element, builder);

        SoapFaultAwareMessageBuilder messageBuilder = new SoapFaultAwareMessageBuilder();
        Element faultElement = DomUtils.getChildElementByTagName(element, "fault");
        if (faultElement != null) {
            Element faultCodeElement = DomUtils.getChildElementByTagName(faultElement, "fault-code");
            if (faultCodeElement != null) {
                messageBuilder.setFaultCode(DomUtils.getTextValue(faultCodeElement).trim());
            }
            
            Element faultStringElement = DomUtils.getChildElementByTagName(faultElement, "fault-string");
            if (faultStringElement != null) {
                messageBuilder.setFaultString(DomUtils.getTextValue(faultStringElement).trim());
            }
            
            Element faultActorElement = DomUtils.getChildElementByTagName(faultElement, "fault-actor");
            if (faultActorElement != null) {
                messageBuilder.setFaultActor(DomUtils.getTextValue(faultActorElement).trim());
            }
            
            parseFaultDetail(faultElement, messageBuilder);
        }

        Element headerElement = DomUtils.getChildElementByTagName(element, "header");
        Map<String, Object> headerValues = new HashMap<String, Object>();
        if (headerElement != null) {
            List<?> elements = DomUtils.getChildElementsByTagName(headerElement, "element");
            for (Iterator<?> iter = elements.iterator(); iter.hasNext();) {
                Element headerValue = (Element) iter.next();
                headerValues.put(headerValue.getAttribute("name"), headerValue.getAttribute("value"));
            }
            messageBuilder.setMessageHeaders(headerValues);
        }
        
        builder.addPropertyValue("messageBuilder", messageBuilder);
        
        return builder.getBeanDefinition();
    }

    /**
     * Parses the fault detail element.
     * 
     * @param faultElement the fault DOM element.
     * @param messageBuilder the soap fault aware message builder.
     */
    private void parseFaultDetail(Element faultElement, SoapFaultAwareMessageBuilder messageBuilder) {
        List<Element> faultDetails = DomUtils.getChildElementsByTagName(faultElement, "fault-detail");
    
        for (Element faultDetailElement : faultDetails) {
            if (faultDetailElement.hasAttribute("file")) {
                
                if (StringUtils.hasText(DomUtils.getTextValue(faultDetailElement).trim())) {
                    throw new BeanCreationException("You tried to set fault-detail by file resource attribute and inline text value at the same time! " +
                            "Please choose one of them.");
                }
                
                String filePath = faultDetailElement.getAttribute("file");
                messageBuilder.addFaultDetailResource(filePath);
            } else {
                String faultDetailData = DomUtils.getTextValue(faultDetailElement).trim();
                if (StringUtils.hasText(faultDetailData)) {
                    messageBuilder.addFaultDetail(faultDetailData);
                } else {
                    throw new BeanCreationException("Not content for fault-detail is set! Either use file attribute or inline text value for fault-detail element.");
                }
            }
        }
    }
}
