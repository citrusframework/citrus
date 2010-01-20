/*
 * Copyright 2006-2010 ConSol* Software GmbH.
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

import java.util.*;

import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.ManagedList;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.StringUtils;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

import com.consol.citrus.actions.PurgeJmsQueuesAction;

public class PurgeJmsQueuesActionParser implements BeanDefinitionParser {

    @SuppressWarnings("unchecked")
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
        
        if(element.hasAttribute("receive-timeout")) {
            beanDefinition.addPropertyValue("receiveTimeout", element.getAttribute("receive-timeout"));
        }
        
        List<String> queueNames = new ArrayList<String>();
        ManagedList queueRefs = new ManagedList();
        
        List<?> queueElements = DomUtils.getChildElementsByTagName(element, "queue");
        for (Iterator<?> iter = queueElements.iterator(); iter.hasNext();) {
            Element queue = (Element) iter.next();
            String queueName = queue.getAttribute("name");
            String queueRef = queue.getAttribute("ref");
            
            if(StringUtils.hasText(queueName)) {
                queueNames.add(queueName);
            } else if(StringUtils.hasText(queueRef)) {
                queueRefs.add(BeanDefinitionBuilder.childBeanDefinition(queueRef).getBeanDefinition());
            } else {
                throw new BeanCreationException("Element 'queue' must set one of the attributes 'name' or 'ref'");
            }
        }
        
        beanDefinition.addPropertyValue("queueNames", queueNames);
        beanDefinition.addPropertyValue("queues", queueRefs);

        return beanDefinition.getBeanDefinition();
    }
}
