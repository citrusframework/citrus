package com.consol.citrus.config.xml;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;

import com.consol.citrus.actions.InputAction;

public class InputActionParser implements BeanDefinitionParser {

    public BeanDefinition parse(Element element, ParserContext parserContext) {
        BeanDefinitionBuilder beanDefinition = BeanDefinitionBuilder.rootBeanDefinition(InputAction.class);

        DescriptionElementParser.doParse(element, beanDefinition);

        String message = element.getAttribute("message");
        if (StringUtils.hasText(message)) {
            beanDefinition.addPropertyValue("message", message);
        }

        String variable = element.getAttribute("variable");
        if (StringUtils.hasText(variable)) {
            beanDefinition.addPropertyValue("variable", variable);
        }

        String validAnswers = element.getAttribute("validAnswers");
        if (StringUtils.hasText(validAnswers)) {
            beanDefinition.addPropertyValue("validAnswers", validAnswers);
        }

        beanDefinition.addPropertyValue("name", element.getLocalName());

        return beanDefinition.getBeanDefinition();
    }
}
