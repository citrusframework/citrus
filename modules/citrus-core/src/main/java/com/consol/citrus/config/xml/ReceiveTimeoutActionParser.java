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

import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.StringUtils;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

import com.consol.citrus.actions.ReceiveTimeoutAction;

public class ReceiveTimeoutActionParser implements BeanDefinitionParser {

    public BeanDefinition parse(Element element, ParserContext parserContext) {
    	String messageReceiver = element.getAttribute("message-receiver");
        BeanDefinitionBuilder beanDefinition = BeanDefinitionBuilder.rootBeanDefinition(ReceiveTimeoutAction.class);
        beanDefinition.addPropertyValue("name", element.getLocalName()+ ":" + messageReceiver);
        
        if(StringUtils.hasText(messageReceiver)) {
        	beanDefinition.addPropertyReference("messageReceiver", messageReceiver);
        } else {
        	throw new BeanCreationException("Mandatory 'message-receiver' attribute has to be set");
        }
        DescriptionElementParser.doParse(element, beanDefinition);

        String wait = element.getAttribute("wait");
        if (wait != null) {
            beanDefinition.addPropertyValue("timeout", wait);
        }

        Element messageSelectorElement = DomUtils.getChildElementByTagName(element, "select");
        if (messageSelectorElement != null) {
            beanDefinition.addPropertyValue("messageSelector", DomUtils.getTextValue(messageSelectorElement));
        }

        return beanDefinition.getBeanDefinition();
    }
}
