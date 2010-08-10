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

import java.util.Map;

import org.apache.xerces.util.DOMUtil;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;

import com.consol.citrus.container.Assert;

/**
 * Bean definition parser for assert action in test case.
 * 
 * @author Christoph Deppisch
 */
public class AssertParser implements BeanDefinitionParser {

    /**
     * @see org.springframework.beans.factory.xml.BeanDefinitionParser#parse(org.w3c.dom.Element, org.springframework.beans.factory.xml.ParserContext)
     */
    public BeanDefinition parse(Element element, ParserContext parserContext) {
        BeanDefinitionBuilder beanDefinition;

        beanDefinition = BeanDefinitionBuilder.rootBeanDefinition(Assert.class);

        String exception = element.getAttribute("exception");

        if (StringUtils.hasText(exception)) {
            beanDefinition.addPropertyValue("exception", exception);
        }
        
        String message = element.getAttribute("message");

        if (StringUtils.hasText(message)) {
            beanDefinition.addPropertyValue("message", message);
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
