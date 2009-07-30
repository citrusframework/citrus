package com.consol.citrus.config.xml;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

import com.consol.citrus.actions.TimeWatcherBean;

public class TimeWatcherActionParser implements BeanDefinitionParser {

    public BeanDefinition parse(Element element, ParserContext parserContext) {
        BeanDefinitionBuilder beanDefinition = BeanDefinitionBuilder.rootBeanDefinition(TimeWatcherBean.class);

        String id = element.getAttribute("id");
        if (id != null) {
            beanDefinition.addPropertyValue("id", id);
        }

        DescriptionElementParser.doParse(element, beanDefinition);

        beanDefinition.addPropertyValue("name", element.getLocalName());

        return beanDefinition.getBeanDefinition();
    }
}
