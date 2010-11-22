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

/**
 * Bean definition parser for receive-timeout action in test case.
 * 
 * @author Christoph Deppisch
 */
public class ReceiveTimeoutActionParser implements BeanDefinitionParser {

    /**
     * @see org.springframework.beans.factory.xml.BeanDefinitionParser#parse(org.w3c.dom.Element, org.springframework.beans.factory.xml.ParserContext)
     */
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
