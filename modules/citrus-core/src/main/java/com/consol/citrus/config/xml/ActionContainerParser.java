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

package com.consol.citrus.config.xml;

import java.util.List;
import java.util.Map;

import org.apache.xerces.util.DOMUtil;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.ManagedList;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

/**
 * Abstract parser implementation that is aware of several embedded test actions of a container. Bean definitions that use
 * this parser component must have an 'actions' property of type {@link List} in order to receive the list of embedded test actions. 
 * 
 * @author Christoph Deppisch
 */
public abstract class ActionContainerParser implements BeanDefinitionParser {

    /**
     * @see org.springframework.beans.factory.xml.BeanDefinitionParser#parse(org.w3c.dom.Element, org.springframework.beans.factory.xml.ParserContext)
     */
    @SuppressWarnings("unchecked")
    public static void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
        Map<String, BeanDefinitionParser> actionRegistry = TestActionRegistry.getRegisteredActionParser();
        ManagedList actions = new ManagedList();

        Element action = DOMUtil.getFirstChildElement(element);

        if (action != null && action.getTagName().equals("description")) {
            builder.addPropertyValue("description", action.getNodeValue());
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
            builder.addPropertyValue("actions", actions);
        }
    }
}
