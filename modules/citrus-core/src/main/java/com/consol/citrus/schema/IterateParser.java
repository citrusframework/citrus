package com.consol.citrus.schema;

import java.util.Map;

import org.apache.xerces.util.DOMUtil;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.ManagedList;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

import com.consol.citrus.group.Iterate;

public class IterateParser implements BeanDefinitionParser {

    public BeanDefinition parse(Element element, ParserContext parserContext) {
        BeanDefinitionBuilder beanDefinition = BeanDefinitionBuilder.rootBeanDefinition(Iterate.class);

        Element descriptionElement = DomUtils.getChildElementByTagName(element, "description");
        if (descriptionElement != null) {
            beanDefinition.addPropertyValue("description", DomUtils.getTextValue(descriptionElement).trim());
        }

        String indexName = element.getAttribute("indexName");
        if (indexName != null && indexName.length() > 0) {
            beanDefinition.addPropertyValue("indexName", indexName);
        }

        String condition = element.getAttribute("condition");
        beanDefinition.addPropertyValue("condition", condition);

        String start = element.getAttribute("start");
        if (start != null && start.length() > 0) {
            beanDefinition.addPropertyValue("index", new Integer(start).intValue());
        }

        String step = element.getAttribute("step");
        if (step != null && step.length() > 0) {
            beanDefinition.addPropertyValue("step", new Integer(step).intValue());
        }

        Map actionRegistry = TestActionRegistry.getRegisteredActionParser();
        ManagedList actions = new ManagedList();

        Element action = DOMUtil.getFirstChildElement(element);

        if (action != null && action.getTagName().equals("description")) {
            beanDefinition.addPropertyValue("description", action.getNodeValue());
            action = DOMUtil.getNextSiblingElement(action);
        }

        if (action != null) {
            do {
                BeanDefinitionParser parser = (BeanDefinitionParser)actionRegistry.get(action.getTagName());

                actions.add(parser.parse(action, parserContext));
            } while ((action = DOMUtil.getNextSiblingElement(action)) != null);
        }

        if (actions.size() > 0) {
            beanDefinition.addPropertyValue("actions", actions);
        }

        beanDefinition.addPropertyValue("name", element.getLocalName());

        return beanDefinition.getBeanDefinition();
    }
}
