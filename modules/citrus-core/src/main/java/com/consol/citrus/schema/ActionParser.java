package com.consol.citrus.schema;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;

public class ActionParser implements BeanDefinitionParser {

    public BeanDefinition parse(Element element, ParserContext parserContext) {
        String beanName = element.getAttribute("reference");

        BeanDefinitionBuilder beanDefinition;

        if (StringUtils.hasText(beanName)) {
            beanDefinition = BeanDefinitionBuilder.childBeanDefinition(beanName);

            DescriptionElementParser.doParse(element, beanDefinition);
            
            beanDefinition.addPropertyValue("name", element.getLocalName() + ":" + beanName);

            return beanDefinition.getBeanDefinition();
        } else {
            throw new RuntimeException("No reference to parent action provided");
        }
    }
}
