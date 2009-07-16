package com.consol.citrus.schema;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

import com.consol.citrus.actions.InputBean;

public class InputActionParser implements BeanDefinitionParser {

    public BeanDefinition parse(Element element, ParserContext parserContext) {
        BeanDefinitionBuilder beanDefinition = BeanDefinitionBuilder.rootBeanDefinition(InputBean.class);

        Element descriptionElement = DomUtils.getChildElementByTagName(element, "description");
        if (descriptionElement != null) {
            beanDefinition.addPropertyValue("description", DomUtils.getTextValue(descriptionElement).trim());
        }

        String message = element.getAttribute("message");
        if (message != null && message.length() > 0) {
            beanDefinition.addPropertyValue("message", message);
        }

        String variable = element.getAttribute("variable");
        if (variable != null && variable.length() > 0) {
            beanDefinition.addPropertyValue("variable", variable);
        }

        String validAnswers = element.getAttribute("validAnswers");
        if (validAnswers != null && validAnswers.length() > 0) {
            beanDefinition.addPropertyValue("validAnswers", validAnswers);
        }

        beanDefinition.addPropertyValue("name", element.getLocalName());

        return beanDefinition.getBeanDefinition();
    }
}
