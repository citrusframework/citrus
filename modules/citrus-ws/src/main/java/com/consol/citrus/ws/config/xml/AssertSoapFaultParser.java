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

import java.util.Map;

import org.apache.xerces.util.DOMUtil;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;

import com.consol.citrus.config.xml.DescriptionElementParser;
import com.consol.citrus.config.xml.TestActionRegistry;
import com.consol.citrus.ws.actions.AssertSoapFault;

public class AssertSoapFaultParser implements BeanDefinitionParser {

    public BeanDefinition parse(Element element, ParserContext parserContext) {
        BeanDefinitionBuilder beanDefinition;

        beanDefinition = BeanDefinitionBuilder.rootBeanDefinition(AssertSoapFault.class);

        String faultCode = element.getAttribute("fault-code");

        if (StringUtils.hasText(faultCode)) {
            beanDefinition.addPropertyValue("faultCode", faultCode);
        }
        
        String faultString = element.getAttribute("fault-string");

        if (StringUtils.hasText(faultString)) {
            beanDefinition.addPropertyValue("faultString", faultString);
        }

        DescriptionElementParser.doParse(element, beanDefinition);

        Map<String, BeanDefinitionParser> actionRegistry = TestActionRegistry.getRegisteredActionParser();

        Element action = DOMUtil.getFirstChildElement(element);

        if (action != null && action.getTagName().equals("description")) {
            action = DOMUtil.getNextSiblingElement(action);
        }

        if (action != null) {
            BeanDefinitionParser parser = actionRegistry.get(action.getTagName());
            
            if(parser ==  null) {
            	beanDefinition.addPropertyValue("action", parserContext.getReaderContext().getNamespaceHandlerResolver().resolve(action.getNamespaceURI()).parse(action, parserContext));
            } else {
            	beanDefinition.addPropertyValue("action", parser.parse(action, parserContext));
            }
        }

        beanDefinition.addPropertyValue("name", element.getLocalName());

        return beanDefinition.getBeanDefinition();
    }
}
