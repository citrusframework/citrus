package com.consol.citrus.schema;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

import com.consol.citrus.actions.SendMessageBean;

public class SendActionParser implements BeanDefinitionParser {

    public BeanDefinition parse(Element element, ParserContext parserContext) {
        String parentBeanName = element.getAttribute("type");
        BeanDefinitionBuilder beanDefinition;

        if (parentBeanName != null && parentBeanName.length() > 0) {
            beanDefinition = BeanDefinitionBuilder.childBeanDefinition(parentBeanName);
            beanDefinition.addPropertyValue("name", element.getLocalName() + ":" + parentBeanName);
        } else {
            beanDefinition = BeanDefinitionBuilder.rootBeanDefinition(SendMessageBean.class);
            beanDefinition.addPropertyValue("name", element.getLocalName());
        }

        Element descriptionElement = DomUtils.getChildElementByTagName(element, "description");
        if (descriptionElement != null) {
            beanDefinition.addPropertyValue("description", DomUtils.getTextValue(descriptionElement).trim());
        }

        Element messageElement = DomUtils.getChildElementByTagName(element, "message");
        if (messageElement != null) {
            Element xmlDataElement = DomUtils.getChildElementByTagName(messageElement, "data");
            if (xmlDataElement != null) {
                beanDefinition.addPropertyValue("messageData", DomUtils.getTextValue(xmlDataElement));
            }

            Element xmlResourceElement = DomUtils.getChildElementByTagName(messageElement, "resource");
            if (xmlResourceElement != null) {
                String filePath = xmlResourceElement.getAttribute("file");
                if (filePath.startsWith("classpath:")) {
                    beanDefinition.addPropertyValue("messageResource", new ClassPathResource(filePath.substring("classpath:".length())));
                } else if (filePath.startsWith("file:")) {
                    beanDefinition.addPropertyValue("messageResource", new FileSystemResource(filePath.substring("file:".length())));
                } else {
                    beanDefinition.addPropertyValue("messageResource", new FileSystemResource(filePath));
                }
            }

            Map setMessageValues = new HashMap();
            List messageValueElements = DomUtils.getChildElementsByTagName(messageElement, "element");
            for (Iterator iter = messageValueElements.iterator(); iter.hasNext();) {
                Element messageValue = (Element) iter.next();
                setMessageValues.put(messageValue.getAttribute("path"), messageValue.getAttribute("value"));
            }
            beanDefinition.addPropertyValue("messageElements", setMessageValues);
        }

        Element headerElement = DomUtils.getChildElementByTagName(element, "header");
        Map setHeaderValues = new HashMap();
        if (headerElement != null) {
            List elements = DomUtils.getChildElementsByTagName(headerElement, "element");
            for (Iterator iter = elements.iterator(); iter.hasNext();) {
                Element headerValue = (Element) iter.next();
                setHeaderValues.put(headerValue.getAttribute("name"), headerValue.getAttribute("value"));
            }
            beanDefinition.addPropertyValue("headerValues", setHeaderValues);
        }

        return beanDefinition.getBeanDefinition();
    }
}
