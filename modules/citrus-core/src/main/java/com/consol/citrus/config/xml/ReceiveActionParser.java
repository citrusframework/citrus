package com.consol.citrus.config.xml;

import java.util.*;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.util.StringUtils;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

import com.consol.citrus.actions.ReceiveMessageBean;

public class ReceiveActionParser implements BeanDefinitionParser {

    public BeanDefinition parse(Element element, ParserContext parserContext) {
        String parentBeanName = element.getAttribute("type");
        BeanDefinitionBuilder beanDefinition;

        if (StringUtils.hasText(parentBeanName)) {
            beanDefinition = BeanDefinitionBuilder.childBeanDefinition(parentBeanName);
            beanDefinition.addPropertyValue("name", element.getLocalName() + ":" + parentBeanName);
        } else {
            beanDefinition = BeanDefinitionBuilder.rootBeanDefinition(ReceiveMessageBean.class);
            beanDefinition.addPropertyValue("name", element.getLocalName());
        }

        DescriptionElementParser.doParse(element, beanDefinition);

        Element messageSelectorElement = DomUtils.getChildElementByTagName(element, "selector");
        if (messageSelectorElement != null) {
            Element selectorStringElement = DomUtils.getChildElementByTagName(messageSelectorElement, "value");
            if (selectorStringElement != null) {
                beanDefinition.addPropertyValue("messageSelectorString", DomUtils.getTextValue(selectorStringElement));
            }

            Map messageSelector = new HashMap();
            List messageSelectorElements = DomUtils.getChildElementsByTagName(messageSelectorElement, "element");
            for (Iterator iter = messageSelectorElements.iterator(); iter.hasNext();) {
                Element selectorElement = (Element) iter.next();
                messageSelector.put(selectorElement.getAttribute("name"), selectorElement.getAttribute("value"));
            }
            beanDefinition.addPropertyValue("messageSelector", messageSelector);
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

            List ignoreValues = new ArrayList();
            List ignoreElements = DomUtils.getChildElementsByTagName(messageElement, "ignore");
            for (Iterator iter = ignoreElements.iterator(); iter.hasNext();) {
                Element ignoreValue = (Element) iter.next();
                ignoreValues.add(ignoreValue.getAttribute("path"));
            }
            beanDefinition.addPropertyValue("ignoreMessageElements", ignoreValues);

            Map validateValues = new HashMap();
            List validateElements = DomUtils.getChildElementsByTagName(messageElement, "validate");
            if (validateElements.size() > 0) {
                for (Iterator iter = validateElements.iterator(); iter.hasNext();) {
                    Element validateValue = (Element) iter.next();
                    validateValues.put(validateValue.getAttribute("path"), validateValue.getAttribute("value"));
                }
                beanDefinition.addPropertyValue("validateMessageElements", validateValues);
            }
            
            Map namespaces = new HashMap();
            List namespaceElements = DomUtils.getChildElementsByTagName(messageElement, "namespace");
            if (namespaceElements.size() > 0) {
                for (Iterator iter = namespaceElements.iterator(); iter.hasNext();) {
                    Element namespaceElement = (Element) iter.next();
                    namespaces.put(namespaceElement.getAttribute("prefix"), namespaceElement.getAttribute("value"));
                }
                beanDefinition.addPropertyValue("namespaces", namespaces);
            }
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

        Element extractElement = DomUtils.getChildElementByTagName(element, "extract");
        Map getMessageValues = new HashMap();
        Map getHeaderValues = new HashMap();
        if (extractElement != null) {
            List headerValueElements = DomUtils.getChildElementsByTagName(extractElement, "header");
            for (Iterator iter = headerValueElements.iterator(); iter.hasNext();) {
                Element headerValue = (Element) iter.next();
                getHeaderValues.put(headerValue.getAttribute("name"), headerValue.getAttribute("variable"));
            }
            beanDefinition.addPropertyValue("extractHeaderValues", getHeaderValues);

            List messageValueElements = DomUtils.getChildElementsByTagName(extractElement, "message");
            for (Iterator iter = messageValueElements.iterator(); iter.hasNext();) {
                Element messageValue = (Element) iter.next();
                getMessageValues.put(messageValue.getAttribute("path"), messageValue.getAttribute("variable"));
            }
            beanDefinition.addPropertyValue("extractMessageElements", getMessageValues);
        }

        return beanDefinition.getBeanDefinition();
    }

}
