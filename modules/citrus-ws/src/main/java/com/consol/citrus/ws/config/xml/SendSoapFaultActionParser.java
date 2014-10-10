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

import com.consol.citrus.ws.actions.SendSoapFaultAction;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.StringUtils;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.List;

/**
 * Bean definition parser for send soap fault action in test case.
 * 
 * @author Christoph Deppisch
 */
public class SendSoapFaultActionParser extends SendSoapMessageActionParser {

    @Override
    public BeanDefinitionBuilder parseComponent(Element element, ParserContext parserContext) {
        BeanDefinitionBuilder builder = super.parseComponent(element, parserContext);

        parseFault(builder, DomUtils.getChildElementByTagName(element, "fault"));

        return builder;
    }

    /**
     * Parses the SOAP fault information.
     * @param builder
     * @param faultElement
     */
    private void parseFault(BeanDefinitionBuilder builder, Element faultElement) {
        if (faultElement != null) {
            Element faultCodeElement = DomUtils.getChildElementByTagName(faultElement, "fault-code");
            if (faultCodeElement != null) {
                builder.addPropertyValue("faultCode", DomUtils.getTextValue(faultCodeElement).trim());
            }

            Element faultStringElement = DomUtils.getChildElementByTagName(faultElement, "fault-string");
            if (faultStringElement != null) {
                builder.addPropertyValue("faultString", DomUtils.getTextValue(faultStringElement).trim());
            }

            Element faultActorElement = DomUtils.getChildElementByTagName(faultElement, "fault-actor");
            if (faultActorElement != null) {
                builder.addPropertyValue("faultActor", DomUtils.getTextValue(faultActorElement).trim());
            }

            parseFaultDetail(builder, faultElement);
        }
    }

    /**
     * Parses the fault detail element.
     * 
     * @param builder
     * @param faultElement the fault DOM element.
     */
    private void parseFaultDetail(BeanDefinitionBuilder builder, Element faultElement) {
        List<Element> faultDetailElements = DomUtils.getChildElementsByTagName(faultElement, "fault-detail");
        List<String> faultDetails = new ArrayList<String>();
        List<String> faultDetailResourcePaths = new ArrayList<String>();

        for (Element faultDetailElement : faultDetailElements) {
            if (faultDetailElement.hasAttribute("file")) {
                
                if (StringUtils.hasText(DomUtils.getTextValue(faultDetailElement).trim())) {
                    throw new BeanCreationException("You tried to set fault-detail by file resource attribute and inline text value at the same time! " +
                            "Please choose one of them.");
                }
                
                String filePath = faultDetailElement.getAttribute("file");
                faultDetailResourcePaths.add(filePath);
            } else {
                String faultDetailData = DomUtils.getTextValue(faultDetailElement).trim();
                if (StringUtils.hasText(faultDetailData)) {
                    faultDetails.add(faultDetailData);
                } else {
                    throw new BeanCreationException("Not content for fault-detail is set! Either use file attribute or inline text value for fault-detail element.");
                }
            }
        }

        builder.addPropertyValue("faultDetails", faultDetails);
        builder.addPropertyValue("faultDetailResourcePaths", faultDetailResourcePaths);
    }

    @Override
    protected Class<?> getBeanDefinitionClass() {
        return SendSoapFaultAction.class;
    }
}
