/*
 * Copyright the original author or authors.
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

package org.citrusframework.selenium.xml;

import java.util.function.BiConsumer;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;
import org.citrusframework.api.actions.selenium.SeleniumElementActionBuilderBase;

public interface ElementAware {

    /**
     * Gets the element aware builder to work with.
     * @return the element aware builder.
     */
    SeleniumElementActionBuilderBase<?, ?> getElementBuilder();

    /**
     * Sets the element properties on the element aware builder. Locator values may use test variables
     * which are resolved when the action is executed.
     * @param element
     */
    default void setElement(Element element) {
        setElement(element, (name, value) -> getElementBuilder().element(name, value));
    }

    /**
     * Sets the element properties using the given consumer. Used by actions that work with
     * more than one element, e.g. drag and drop with source and target element.
     * @param element
     * @param propertyConsumer
     */
    static void setElement(Element element, BiConsumer<String, String> propertyConsumer) {
        if (element.getId() != null) {
            propertyConsumer.accept("id", element.id);
        } else if (element.getName() != null) {
            propertyConsumer.accept("name", element.name);
        } else if (element.getLinkText() != null) {
            propertyConsumer.accept("link-text", element.linkText);
        } else if (element.getPartialLinkText() != null) {
            propertyConsumer.accept("partial-link-text", element.partialLinkText);
        } else if (element.getXpath() != null) {
            propertyConsumer.accept("xpath", element.xpath);
        } else if (element.getCssSelector() != null) {
            propertyConsumer.accept("css-selector", element.cssSelector);
        } else if (element.getProperty() != null) {
            propertyConsumer.accept(element.getProperty().getName(), element.getProperty().getValue());
        } else if (element.getClassName() != null) {
            propertyConsumer.accept("class-name", element.className);
        } else if (element.getTagName() != null) {
            propertyConsumer.accept("tag-name", element.tagName);
        }
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {})
    class Element {
        @XmlAttribute
        private String id;
        @XmlAttribute
        private String name;
        @XmlAttribute(name = "tag-name")
        private String tagName;
        @XmlAttribute(name = "class-name")
        private String className;
        @XmlElement
        private Property property;

        @XmlAttribute(name = "link-text")
        private String linkText;
        @XmlAttribute(name = "partial-link-text")
        private String partialLinkText;
        @XmlAttribute(name = "xpath")
        private String xpath;
        @XmlAttribute(name = "css-selector")
        private String cssSelector;

        public void setId(String id) {
            this.id = id;
        }

        public String getId() {
            return id;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public String getTagName() {
            return tagName;
        }

        public void setTagName(String tagName) {
            this.tagName = tagName;
        }

        public String getClassName() {
            return className;
        }

        public void setClassName(String className) {
            this.className = className;
        }

        public void setLinkText(String linkText) {
            this.linkText = linkText;
        }

        public String getLinkText() {
            return linkText;
        }

        public void setPartialLinkText(String partialLinkText) {
            this.partialLinkText = partialLinkText;
        }

        public String getPartialLinkText() {
            return partialLinkText;
        }

        public void setXpath(String xpath) {
            this.xpath = xpath;
        }

        public String getXpath() {
            return xpath;
        }

        public void setCssSelector(String cssSelector) {
            this.cssSelector = cssSelector;
        }

        public String getCssSelector() {
            return cssSelector;
        }

        public void setProperty(Property property) {
            this.property = property;
        }

        public Property getProperty() {
            return property;
        }

        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "", propOrder = {})
        public static class Property {
            @XmlAttribute
            private String name;
            @XmlAttribute
            private String value;

            public void setName(String name) {
                this.name = name;
            }

            public String getName() {
                return name;
            }

            public void setValue(String value) {
                this.value = value;
            }

            public String getValue() {
                return value;
            }
        }
    }
}
