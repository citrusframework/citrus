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

package org.citrusframework.selenium.config.xml;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.citrusframework.config.util.BeanDefinitionParserUtils;
import org.citrusframework.selenium.actions.FindElementAction;
import org.openqa.selenium.By;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

/**
 * @author Tamer Erdogan, Christoph Deppisch
 * @since 2.7
 */
public class FindElementActionParser extends AbstractBrowserActionParser {

    @Override
    protected final void parseAction(BeanDefinitionBuilder beanDefinition, Element element, ParserContext parserContext) {
        parseElementSelector(beanDefinition, element, parserContext);
        parseElement(beanDefinition, element, parserContext);
    }

    protected void parseElement(BeanDefinitionBuilder beanDefinition, Element element, ParserContext parserContext) {
        Element webElement = DomUtils.getChildElementByTagName(element, "element");
        if (webElement != null) {
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

    protected void parseElementSelector(BeanDefinitionBuilder beanDefinition, Element element, ParserContext parserContext) {
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
        }
    }

    @Override
    protected Class<? extends ElementActionFactoryBean<?, ?>> getBrowserActionClass() {
        return FindElementActionFactoryBean.class;
    }

    /**
     * Test action factory bean.
     */
    public static final class FindElementActionFactoryBean extends ElementActionFactoryBean<FindElementAction, FindElementAction.Builder> {

        private final FindElementAction.Builder builder = new FindElementAction.Builder();

        /**
         * Sets the attributes.
         *
         * @param attributes
         */
        public void setAttributes(Map<String, String> attributes) {
            attributes.forEach(builder::attribute);
        }

        /**
         * Sets the styles.
         *
         * @param styles
         */
        public void setStyles(Map<String, String> styles) {
            styles.forEach(builder::style);
        }

        /**
         * Sets the displayed.
         *
         * @param displayed
         */
        public void setDisplayed(boolean displayed) {
            builder.displayed(displayed);
        }

        /**
         * Sets the enabled.
         *
         * @param enabled
         */
        public void setEnabled(boolean enabled) {
            builder.enabled(enabled);
        }

        /**
         * Sets the text.
         *
         * @param text
         */
        public void setText(String text) {
            builder.text(text);
        }

        /**
         * Sets the tagName.
         *
         * @param tagName
         */
        public void setTagName(String tagName) {
            builder.tagName(tagName);
        }

        @Override
        public FindElementAction getObject() throws Exception {
            return getObject(builder);
        }

        @Override
        public Class<?> getObjectType() {
            return FindElementAction.class;
        }

        /**
         * Obtains the builder.
         * @return the builder implementation.
         */
        @Override
        public FindElementAction.Builder getBuilder() {
            return builder;
        }
    }

    /**
     * Abstract element action factory bean.
     * @param <T>
     * @param <B>
     */
    public static abstract class ElementActionFactoryBean<T extends FindElementAction, B extends FindElementAction.ElementActionBuilder<T, B>> extends AbstractSeleniumActionFactoryBean<T, B> {

        protected String property;
        protected String propertyValue;

        /**
         * Sets the property.
         * @param property
         */
        public void setProperty(String property) {
            this.property = property;
        }

        /**
         * Sets the propertyValue.
         * @param propertyValue
         */
        public void setPropertyValue(String propertyValue) {
            this.propertyValue = propertyValue;
        }

        /**
         * Sets the by.
         * @param by
         */
        public void setBy(By by) {
            getBuilder().element(by);
        }

        public T getObject(B builder) throws Exception {
            builder.element(property, propertyValue);
            return builder.build();
        }
    }
}
