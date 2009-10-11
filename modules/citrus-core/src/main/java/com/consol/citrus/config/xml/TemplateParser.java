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

package com.consol.citrus.config.xml;

import java.util.Map;

import org.apache.xerces.util.DOMUtil;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.ManagedList;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;

import com.consol.citrus.group.Template;

public class TemplateParser implements BeanDefinitionParser {

    @SuppressWarnings("unchecked")
	public BeanDefinition parse(Element element, ParserContext parserContext) {
        BeanDefinitionBuilder beanDefinition;

        beanDefinition = BeanDefinitionBuilder.rootBeanDefinition(Template.class);

        DescriptionElementParser.doParse(element, beanDefinition);

        String name = element.getAttribute("name");

        if (StringUtils.hasText(name)) {
            beanDefinition.addPropertyValue("name", name);
        } else {
            beanDefinition.addPropertyValue("name", parserContext.getReaderContext().generateBeanName(beanDefinition.getBeanDefinition()));
        }

        Map<String, BeanDefinitionParser> actionRegistry = TestActionRegistry.getRegisteredActionParser();
        ManagedList actions = new ManagedList();

        Element action = DOMUtil.getFirstChildElement(element);

        if (action != null && action.getTagName().equals("description")) {
            beanDefinition.addPropertyValue("description", action.getNodeValue());
            action = DOMUtil.getNextSiblingElement(action);
        }

        if (action != null) {
            do {
                BeanDefinitionParser parser = (BeanDefinitionParser)actionRegistry.get(action.getTagName());

                if(parser ==  null) {
                	actions.add(parserContext.getReaderContext().getNamespaceHandlerResolver().resolve(action.getNamespaceURI()).parse(action, parserContext));
                } else {
                	actions.add(parser.parse(action, parserContext));
                }
            } while ((action = DOMUtil.getNextSiblingElement(action)) != null);
        }

        if (actions.size() > 0) {
            beanDefinition.addPropertyValue("actions", actions);
        }

        beanDefinition.addPropertyValue("name", element.getLocalName() + "(" + element.getAttribute("name") + ")");

        parserContext.getRegistry().registerBeanDefinition(name, beanDefinition.getBeanDefinition());
        return parserContext.getRegistry().getBeanDefinition(name);
    }
}
