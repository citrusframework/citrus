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

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.StringUtils;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

import com.consol.citrus.container.Template;

/**
 * Bean definition parser for call template action in test case.
 * 
 * @author Christoph Deppisch
 */
public class CallTemplateParser implements BeanDefinitionParser {

    /**
     * @see org.springframework.beans.factory.xml.BeanDefinitionParser#parse(org.w3c.dom.Element, org.springframework.beans.factory.xml.ParserContext)
     */
	public BeanDefinition parse(Element element, ParserContext parserContext) {
        BeanDefinitionBuilder beanDefinition;

        String parentBeanName = element.getAttribute("name");

        if (StringUtils.hasText(parentBeanName)) {
            beanDefinition = BeanDefinitionBuilder.childBeanDefinition(parentBeanName);
            beanDefinition.addPropertyValue("name", element.getLocalName() + ":" + parentBeanName);
        } else {
            beanDefinition = BeanDefinitionBuilder.rootBeanDefinition(Template.class);
            beanDefinition.addPropertyValue("name", element.getLocalName());
        }

        DescriptionElementParser.doParse(element, beanDefinition);

        List<?> parameterElements = DomUtils.getChildElementsByTagName(element, "parameter");

        if (parameterElements != null && parameterElements.size() > 0) {
            Map<String, String> parameters = new LinkedHashMap<String, String>();

            for (Iterator<?> iter = parameterElements.iterator(); iter.hasNext();) {
                Element parameterElement = (Element) iter.next();
                final String name = parameterElement.getAttribute("name");
                String value = null;
                if (parameterElement.hasAttribute("value")) {
                	value = parameterElement.getAttribute("value");
                } else {
                	Element valueElement = DomUtils.getChildElementByTagName(parameterElement, "value");
                	if (valueElement != null) {
                		value = valueElement.getTextContent(); 
                	}
                }
                if (value != null) {
                	parameters.put(name, value);
                } else {
                	throw new IllegalArgumentException("Please supply either a value attribute or a value child node");
                }
            }

            beanDefinition.addPropertyValue("parameter", parameters);
        }

        return beanDefinition.getBeanDefinition();
    }
}
