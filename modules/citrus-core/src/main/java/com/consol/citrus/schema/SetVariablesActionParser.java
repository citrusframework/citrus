package com.consol.citrus.schema;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

import com.consol.citrus.actions.SetVariablesBean;

public class SetVariablesActionParser implements BeanDefinitionParser {

    public BeanDefinition parse(Element element, ParserContext parserContext) {
        BeanDefinitionBuilder beanDefinition = BeanDefinitionBuilder.rootBeanDefinition(SetVariablesBean.class);

        Element descriptionElement = DomUtils.getChildElementByTagName(element, "description");
        if (descriptionElement != null) {
            beanDefinition.addPropertyValue("description", DomUtils.getTextValue(descriptionElement).trim());
        }

        Map variables = new LinkedHashMap();
        List variableElements = DomUtils.getChildElementsByTagName(element, "variable");
        for (Iterator iter = variableElements.iterator(); iter.hasNext();) {
            Element variable = (Element) iter.next();
            variables.put(variable.getAttribute("name"), variable.getAttribute("value"));
        }
        beanDefinition.addPropertyValue("variables", variables);

        beanDefinition.addPropertyValue("name", element.getLocalName());

        return beanDefinition.getBeanDefinition();
    }
}
