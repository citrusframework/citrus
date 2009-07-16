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

import com.consol.citrus.group.Template;

public class CallTemplateParser implements BeanDefinitionParser {

    public BeanDefinition parse(Element element, ParserContext parserContext) {
        BeanDefinitionBuilder beanDefinition;

        String parentBeanName = element.getAttribute("name");

        if (parentBeanName != null && parentBeanName.length() > 0) {
            beanDefinition = BeanDefinitionBuilder.childBeanDefinition(parentBeanName);
            beanDefinition.addPropertyValue("name", element.getLocalName() + ":" + parentBeanName);
        } else {
            beanDefinition = BeanDefinitionBuilder.rootBeanDefinition(Template.class);
            beanDefinition.addPropertyValue("name", element.getLocalName());
        }

        Element descriptionElement = DomUtils.getChildElementByTagName(element, "description");
        if (descriptionElement != null) {
            beanDefinition.addPropertyValue("description", DomUtils.getTextValue(descriptionElement).trim());
        }

        List parameterElements = DomUtils.getChildElementsByTagName(element, "parameter");

        if (parameterElements != null && parameterElements.size() > 0) {
            Map parameters = new LinkedHashMap();

            for (Iterator iter = parameterElements.iterator(); iter.hasNext();) {
                Element variableDefinition = (Element) iter.next();
                parameters.put(variableDefinition.getAttribute("name"), variableDefinition.getAttribute("value"));
            }

            beanDefinition.addPropertyValue("parameter", parameters);
        }

        return beanDefinition.getBeanDefinition();
    }
}
