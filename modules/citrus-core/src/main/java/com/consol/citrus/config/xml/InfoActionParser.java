package com.consol.citrus.config.xml;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

import com.consol.citrus.actions.InfoBean;

public class InfoActionParser implements BeanDefinitionParser {

    public BeanDefinition parse(Element element, ParserContext parserContext) {
        BeanDefinitionBuilder beanDefinition = BeanDefinitionBuilder.rootBeanDefinition(InfoBean.class);

        DescriptionElementParser.doParse(element, beanDefinition);

        List infoValues = new ArrayList();
        List variableElements = DomUtils.getChildElementsByTagName(element, "variable");
        for (Iterator iter = variableElements.iterator(); iter.hasNext();) {
            Element variable = (Element) iter.next();
            infoValues.add(variable.getAttribute("name"));
        }
        beanDefinition.addPropertyValue("infoValues", infoValues);

        beanDefinition.addPropertyValue("name", element.getLocalName());

        return beanDefinition.getBeanDefinition();
    }
}
