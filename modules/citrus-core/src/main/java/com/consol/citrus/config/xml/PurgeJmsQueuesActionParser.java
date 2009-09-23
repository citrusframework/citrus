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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.StringUtils;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

import com.consol.citrus.actions.PurgeJmsQueuesAction;

public class PurgeJmsQueuesActionParser implements BeanDefinitionParser {

    public BeanDefinition parse(Element element, ParserContext parserContext) {
        BeanDefinitionBuilder beanDefinition = BeanDefinitionBuilder.rootBeanDefinition(PurgeJmsQueuesAction.class);
        beanDefinition.addPropertyValue("name", element.getLocalName());

        DescriptionElementParser.doParse(element, beanDefinition);

        String connectionFactory = "connectionFactory"; //default value
        
        if(element.hasAttribute("connection-factory")) {
            connectionFactory = element.getAttribute("connection-factory");
        }
        
        if(!StringUtils.hasText(connectionFactory)) {
            parserContext.getReaderContext().error(
                    "'connection-factory' attribute must not be empty for this element", element);
        }
        
        beanDefinition.addPropertyReference("connectionFactory", connectionFactory);
        
        List queueNames = new ArrayList();
        List queueElements = DomUtils.getChildElementsByTagName(element, "queue");
        for (Iterator iter = queueElements.iterator(); iter.hasNext();) {
            Element queue = (Element) iter.next();
            queueNames.add(queue.getAttribute("name"));
        }
        beanDefinition.addPropertyValue("queueNames", queueNames);

        return beanDefinition.getBeanDefinition();
    }
}
