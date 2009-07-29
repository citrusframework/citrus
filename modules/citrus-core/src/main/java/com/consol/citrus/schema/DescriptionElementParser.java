package com.consol.citrus.schema;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

public class DescriptionElementParser {
    public static void doParse(Element element, BeanDefinitionBuilder builder) {
        Element descriptionElement = DomUtils.getChildElementByTagName(element, "description");
        if (descriptionElement != null) {
            builder.addPropertyValue("description", DomUtils.getTextValue(descriptionElement).trim());
        }
    }
}
