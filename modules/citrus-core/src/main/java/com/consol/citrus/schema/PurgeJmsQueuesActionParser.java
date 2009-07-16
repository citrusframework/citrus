package com.consol.citrus.schema;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

import com.consol.citrus.actions.PurgeJmsQueuesBean;

public class PurgeJmsQueuesActionParser implements BeanDefinitionParser {

    public BeanDefinition parse(Element element, ParserContext parserContext) {
        String parentBeanName = element.getAttribute("connect");
        BeanDefinitionBuilder beanDefinition;

        if (parentBeanName != null && parentBeanName.length()>0) {
            beanDefinition = BeanDefinitionBuilder.childBeanDefinition(parentBeanName);
            beanDefinition.addPropertyValue("name", element.getLocalName() + ":" + parentBeanName);
        } else {
            beanDefinition = BeanDefinitionBuilder.rootBeanDefinition(PurgeJmsQueuesBean.class);
            beanDefinition.addPropertyValue("name", element.getLocalName());
        }

        Element descriptionElement = DomUtils.getChildElementByTagName(element, "description");
        if (descriptionElement != null) {
            beanDefinition.addPropertyValue("description", DomUtils.getTextValue(descriptionElement).trim());
        }

        List queueNames = new ArrayList();
        List queueElements = DomUtils.getChildElementsByTagName(element, "queue");
        for (Iterator iter = queueElements.iterator(); iter.hasNext();) {
            Element queue = (Element) iter.next();
            queueNames.add(queue.getAttribute("name"));
        }
        beanDefinition.addPropertyValue("queueNames", queueNames);

        return beanDefinition.getBeanDefinition();
    }
}
