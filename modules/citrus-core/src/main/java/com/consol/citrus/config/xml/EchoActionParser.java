package com.consol.citrus.config.xml;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

import com.consol.citrus.actions.EchoBean;

public class EchoActionParser implements BeanDefinitionParser {

    public BeanDefinition parse(Element element, ParserContext parserContext) {
        BeanDefinitionBuilder beanDefinition = BeanDefinitionBuilder.rootBeanDefinition(EchoBean.class);

        DescriptionElementParser.doParse(element, beanDefinition);

        Element messageElement = DomUtils.getChildElementByTagName(element, "message");
        if (messageElement != null) {
            beanDefinition.addPropertyValue("message", DomUtils.getTextValue(messageElement));
        }

        beanDefinition.addPropertyValue("name", element.getLocalName());

        return beanDefinition.getBeanDefinition();
    }
}
