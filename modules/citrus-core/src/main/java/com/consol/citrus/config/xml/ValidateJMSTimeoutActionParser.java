package com.consol.citrus.config.xml;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.StringUtils;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

import com.consol.citrus.actions.ValidateJMSTimeoutBean;

public class ValidateJMSTimeoutActionParser implements BeanDefinitionParser {

    public BeanDefinition parse(Element element, ParserContext parserContext) {
        String parentBeanName = element.getAttribute("connect");
        BeanDefinitionBuilder beanDefinition;

        if (StringUtils.hasText(parentBeanName)) {
            beanDefinition = BeanDefinitionBuilder.childBeanDefinition(parentBeanName);
            beanDefinition.addPropertyValue("name", element.getLocalName() + ":" + parentBeanName);
        } else {
            beanDefinition = BeanDefinitionBuilder.rootBeanDefinition(ValidateJMSTimeoutBean.class);
            beanDefinition.addPropertyValue("name", element.getLocalName());
        }

        DescriptionElementParser.doParse(element, beanDefinition);

        String wait = element.getAttribute("wait");
        if (wait != null) {
            beanDefinition.addPropertyValue("timeout", wait);
        }

        Element messageSelectorElement = DomUtils.getChildElementByTagName(element, "select");
        if (messageSelectorElement != null) {
            beanDefinition.addPropertyValue("messageSelector", DomUtils.getTextValue(messageSelectorElement));
        }

        return beanDefinition.getBeanDefinition();
    }
}
