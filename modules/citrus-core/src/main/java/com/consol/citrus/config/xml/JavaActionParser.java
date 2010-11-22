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

/**
 * Bean definition parser for java action in test case.
 * 
 * @author Christoph Deppisch
 */
public class JavaActionParser implements BeanDefinitionParser {

    /**
     * @see org.springframework.beans.factory.xml.BeanDefinitionParser#parse(org.w3c.dom.Element, org.springframework.beans.factory.xml.ParserContext)
     */
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
