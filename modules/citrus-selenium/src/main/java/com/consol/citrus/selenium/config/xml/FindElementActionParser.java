/*
 * Copyright 2006-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.consol.citrus.selenium.config.xml;

import com.consol.citrus.config.util.BeanDefinitionParserUtils;
import com.consol.citrus.selenium.actions.AbstractSeleniumAction;
import com.consol.citrus.selenium.actions.FindElementAction;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

import java.util.*;

/**
 * @author Tamer Erdogan, Christoph Deppisch
 * @since 2.7
 */
public class FindElementActionParser extends AbstractBrowserActionParser {

    @Override
    protected void parseAction(BeanDefinitionBuilder beanDefinition, Element element, ParserContext parserContext) {
        Element webElement = DomUtils.getChildElementByTagName(element, "element");
        if (webElement != null) {
            String propertyValue = null;
            String property = null;

            if (webElement.hasAttribute("id")) {
                property = "id";
                propertyValue = webElement.getAttribute("id");
            } else if (webElement.hasAttribute("name")) {
                property = "name";
                propertyValue = webElement.getAttribute("name");
            } else if (webElement.hasAttribute("class-name")) {
                property = "class-name";
                propertyValue = webElement.getAttribute("class-name");
            } else if (webElement.hasAttribute("css-selector")) {
                property = "css-selector";
                propertyValue = webElement.getAttribute("css-selector");
            } else if (webElement.hasAttribute("link-text")) {
                property = "link-text";
                propertyValue = webElement.getAttribute("link-text");
            } else if (webElement.hasAttribute("xpath")) {
                property = "xpath";
                propertyValue = webElement.getAttribute("xpath");
            } else if (webElement.hasAttribute("tag-name")) {
                property = "tag-name";
                propertyValue = webElement.getAttribute("tag-name");
            }

            beanDefinition.addPropertyValue("property", property);
            beanDefinition.addPropertyValue("propertyValue", propertyValue);

            BeanDefinitionParserUtils.setPropertyValue(beanDefinition, webElement.getAttribute("tag-name"), "tagName");
            BeanDefinitionParserUtils.setPropertyValue(beanDefinition, webElement.getAttribute("text"), "text");
            BeanDefinitionParserUtils.setPropertyValue(beanDefinition, webElement.getAttribute("displayed"), "displayed");
            BeanDefinitionParserUtils.setPropertyValue(beanDefinition, webElement.getAttribute("enabled"), "enabled");

            Element attributesContainerElement = DomUtils.getChildElementByTagName(webElement, "attributes");
            if (attributesContainerElement != null) {
                Map<String, String> attributes = new HashMap<>();
                List<Element> attributeElements = DomUtils.getChildElementsByTagName(attributesContainerElement, "attribute");
                for (Element attribute : attributeElements) {
                    attributes.put(attribute.getAttribute("name"), attribute.getAttribute("value"));
                }

                beanDefinition.addPropertyValue("attributes", attributes);
            }

            Element stylesContainerElement = DomUtils.getChildElementByTagName(webElement, "styles");
            if (stylesContainerElement != null) {
                Map<String, String> styles = new HashMap<>();
                List<Element> styleElements = DomUtils.getChildElementsByTagName(stylesContainerElement, "style");
                for (Element style : styleElements) {
                    styles.put(style.getAttribute("name"), style.getAttribute("value"));
                }

                beanDefinition.addPropertyValue("styles", styles);
            }
        }
    }

    @Override
    protected Class<? extends AbstractSeleniumAction> getBrowserActionClass() {
        return FindElementAction.class;
    }
}
