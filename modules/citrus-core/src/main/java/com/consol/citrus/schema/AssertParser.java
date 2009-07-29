package com.consol.citrus.schema;

import java.util.Map;

import org.apache.xerces.util.DOMUtil;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;

import com.consol.citrus.container.Assert;

public class AssertParser implements BeanDefinitionParser {

    public BeanDefinition parse(Element element, ParserContext parserContext) {
        BeanDefinitionBuilder beanDefinition;

        beanDefinition = BeanDefinitionBuilder.rootBeanDefinition(Assert.class);

        String exception = element.getAttribute("exception");

        if (StringUtils.hasText(exception)) {
            beanDefinition.addPropertyValue("exception", exception);
        }

        DescriptionElementParser.doParse(element, beanDefinition);

        Map actionRegistry = TestActionRegistry.getRegisteredActionParser();

        Element action = DOMUtil.getFirstChildElement(element);

        if (action != null && action.getTagName().equals("description")) {
            action = DOMUtil.getNextSiblingElement(action);
        }

        if (action != null) {
            BeanDefinitionParser parser = (BeanDefinitionParser)actionRegistry.get(action.getTagName());
            beanDefinition.addPropertyValue("action", parser.parse(action, parserContext));
        }

        beanDefinition.addPropertyValue("name", element.getLocalName());

        return beanDefinition.getBeanDefinition();
    }
}
