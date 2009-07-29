package com.consol.citrus.schema;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

import com.consol.citrus.actions.ClassRunnerBean;

public class ClassRunnerActionParser implements BeanDefinitionParser {

    public BeanDefinition parse(Element element, ParserContext parserContext) {
        BeanDefinitionBuilder beanDefinition = BeanDefinitionBuilder.rootBeanDefinition(ClassRunnerBean.class);

        DescriptionElementParser.doParse(element, beanDefinition);

        String className = element.getAttribute("class");
        beanDefinition.addPropertyValue("className", className);

        Element constructorElement = DomUtils.getChildElementByTagName(element, "constructor");
        List arguments = new ArrayList();
        if (constructorElement != null) {
            List argumentList = DomUtils.getChildElementsByTagName(constructorElement, "argument");
            for (Iterator iter = argumentList.iterator(); iter.hasNext();) {
                Element arg = (Element) iter.next();
                arguments.add(resolveArgument(arg.getAttribute("type"), arg.getTextContent()));
            }
            beanDefinition.addPropertyValue("constructorArgs", arguments);
        }

        Element methodElement = DomUtils.getChildElementByTagName(element, "method");
        arguments = new ArrayList();
        if (methodElement != null) {
            String methodName = methodElement.getAttribute("name");
            beanDefinition.addPropertyValue("methodName", methodName);

            List argumentList = DomUtils.getChildElementsByTagName(methodElement, "argument");
            for (Iterator iter = argumentList.iterator(); iter.hasNext();) {
                Element arg = (Element) iter.next();
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
