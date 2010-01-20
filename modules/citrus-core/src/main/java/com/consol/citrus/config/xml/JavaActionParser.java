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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

import com.consol.citrus.actions.JavaAction;

public class JavaActionParser implements BeanDefinitionParser {

	@SuppressWarnings("unchecked")
	public BeanDefinition parse(Element element, ParserContext parserContext) {
        BeanDefinitionBuilder beanDefinition = BeanDefinitionBuilder.rootBeanDefinition(JavaAction.class);

        DescriptionElementParser.doParse(element, beanDefinition);

        String className = element.getAttribute("class");
        beanDefinition.addPropertyValue("className", className);

        Element constructorElement = DomUtils.getChildElementByTagName(element, "constructor");
        List<Object> arguments = new ArrayList<Object>();
        if (constructorElement != null) {
            List<Element> argumentList = DomUtils.getChildElementsByTagName(constructorElement, "argument");
            for (Iterator<Element> iter = argumentList.iterator(); iter.hasNext();) {
                Element arg = iter.next();
                arguments.add(resolveArgument(arg.getAttribute("type"), arg.getTextContent()));
            }
            beanDefinition.addPropertyValue("constructorArgs", arguments);
        }

        Element methodElement = DomUtils.getChildElementByTagName(element, "method");
        arguments = new ArrayList<Object>();
        if (methodElement != null) {
            String methodName = methodElement.getAttribute("name");
            beanDefinition.addPropertyValue("methodName", methodName);

            List<Element> argumentList = DomUtils.getChildElementsByTagName(methodElement, "argument");
            for (Iterator<Element> iter = argumentList.iterator(); iter.hasNext();) {
                Element arg = iter.next();
                arguments.add(resolveArgument(arg.getAttribute("type"), DomUtils.getTextValue(arg)));
            }
            beanDefinition.addPropertyValue("methodArgs", arguments);
        }

        beanDefinition.addPropertyValue("name", element.getLocalName());

        return beanDefinition.getBeanDefinition();
    }

    private Object resolveArgument(String type, String value) {
        if (type == null || type.equals("")) {
            return value;
        } else if (type.equals("String[]")) {
            return value.split(",");
        } else if (type.equals("boolean")) {
            return Boolean.valueOf(value).booleanValue();
        }  else if (type.equals("int")) {
            return Integer.valueOf(value).intValue();
        } //TODO: add other data types

        return null;
    }

}
