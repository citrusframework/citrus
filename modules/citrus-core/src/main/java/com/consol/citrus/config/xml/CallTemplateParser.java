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

import com.consol.citrus.group.Template;

public class CallTemplateParser implements BeanDefinitionParser {

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

        List parameterElements = DomUtils.getChildElementsByTagName(element, "parameter");

        if (parameterElements != null && parameterElements.size() > 0) {
            Map<String, String> parameters = new LinkedHashMap<String, String>();

            for (Iterator iter = parameterElements.iterator(); iter.hasNext();) {
                Element variableDefinition = (Element) iter.next();
                parameters.put(variableDefinition.getAttribute("name"), variableDefinition.getAttribute("value"));
            }

            beanDefinition.addPropertyValue("parameter", parameters);
        }

        return beanDefinition.getBeanDefinition();
    }
}
