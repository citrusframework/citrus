package com.consol.citrus.config.xml;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

import com.consol.citrus.actions.LoadPropertiesAction;

public class LoadPropertiesActionParser implements BeanDefinitionParser {

    public BeanDefinition parse(Element element, ParserContext parserContext) {
        BeanDefinitionBuilder beanDefinition = BeanDefinitionBuilder.rootBeanDefinition(LoadPropertiesAction.class);

        DescriptionElementParser.doParse(element, beanDefinition);

        Element propertiesElement = DomUtils.getChildElementByTagName(element, "properties");
        if (propertiesElement != null) {
            String fileName = propertiesElement.getAttribute("file");
            beanDefinition.addPropertyValue("file", fileName);
        }

        beanDefinition.addPropertyValue("name", element.getLocalName());

        return beanDefinition.getBeanDefinition();
    }
}
