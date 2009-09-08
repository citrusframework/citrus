package com.consol.citrus.config.xml;

import java.util.*;

import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.util.StringUtils;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

public class SendMessageActionParser implements BeanDefinitionParser {

    public BeanDefinition parse(Element element, ParserContext parserContext) {
        String parent = element.getAttribute("parent");
        String messageSenderReference = element.getAttribute("with");
        
        BeanDefinitionBuilder builder;

        if (StringUtils.hasText(parent)) {
            builder = BeanDefinitionBuilder.childBeanDefinition(parent);
            builder.addPropertyValue("name", element.getLocalName() + ":" + parent);
        } else if (StringUtils.hasText(messageSenderReference)) {
            builder = BeanDefinitionBuilder.genericBeanDefinition("com.consol.citrus.actions.SendMessageAction");
            builder.addPropertyValue("name", element.getLocalName());

            builder.addPropertyReference("messageSender", messageSenderReference);
        } else {
            throw new BeanCreationException("Either 'parent' or 'with' attribute has to be set!");
        }
        
        DescriptionElementParser.doParse(element, builder);

        Element messageElement = DomUtils.getChildElementByTagName(element, "message");
        if (messageElement != null) {
            Element xmlDataElement = DomUtils.getChildElementByTagName(messageElement, "data");
            if (xmlDataElement != null) {
                builder.addPropertyValue("messageData", DomUtils.getTextValue(xmlDataElement));
            }

            Element xmlResourceElement = DomUtils.getChildElementByTagName(messageElement, "resource");
            if (xmlResourceElement != null) {
                String filePath = xmlResourceElement.getAttribute("file");
                if (filePath.startsWith("classpath:")) {
                    builder.addPropertyValue("messageResource", new ClassPathResource(filePath.substring("classpath:".length())));
                } else if (filePath.startsWith("file:")) {
                    builder.addPropertyValue("messageResource", new FileSystemResource(filePath.substring("file:".length())));
                } else {
                    builder.addPropertyValue("messageResource", new FileSystemResource(filePath));
                }
            }

            Map setMessageValues = new HashMap();
            List messageValueElements = DomUtils.getChildElementsByTagName(messageElement, "element");
            for (Iterator iter = messageValueElements.iterator(); iter.hasNext();) {
                Element messageValue = (Element) iter.next();
                setMessageValues.put(messageValue.getAttribute("path"), messageValue.getAttribute("value"));
            }
            builder.addPropertyValue("messageElements", setMessageValues);
        }

        Element headerElement = DomUtils.getChildElementByTagName(element, "header");
        Map setHeaderValues = new HashMap();
        if (headerElement != null) {
            List elements = DomUtils.getChildElementsByTagName(headerElement, "element");
            for (Iterator iter = elements.iterator(); iter.hasNext();) {
                Element headerValue = (Element) iter.next();
                setHeaderValues.put(headerValue.getAttribute("name"), headerValue.getAttribute("value"));
            }
            builder.addPropertyValue("headerValues", setHeaderValues);
        }

        return builder.getBeanDefinition();
    }
}
