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

package org.citrusframework.selenium.yaml;

import java.util.function.BiConsumer;

import org.citrusframework.api.actions.selenium.SeleniumElementActionBuilderBase;
import org.citrusframework.api.yaml.SchemaProperty;

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

    class Element {
        private String id;
        private String name;
        private String tagName;
        private String className;
        private Property property;

        private String linkText;
        private String partialLinkText;
        private String xpath;
        private String cssSelector;

        @SchemaProperty
        public void setId(String id) {
            this.id = id;
        }

        public String getId() {
            return id;
        }

        @SchemaProperty
        public void setName(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public String getTagName() {
            return tagName;
        }

        @SchemaProperty
        public void setTagName(String tagName) {
            this.tagName = tagName;
        }

        public String getClassName() {
            return className;
        }

        @SchemaProperty
        public void setClassName(String className) {
            this.className = className;
        }

        @SchemaProperty
        public void setLinkText(String linkText) {
            this.linkText = linkText;
        }

        public String getLinkText() {
            return linkText;
        }

        @SchemaProperty
        public void setPartialLinkText(String partialLinkText) {
            this.partialLinkText = partialLinkText;
        }

        public String getPartialLinkText() {
            return partialLinkText;
        }

        @SchemaProperty
        public void setXpath(String xpath) {
            this.xpath = xpath;
        }

        public String getXpath() {
            return xpath;
        }

        @SchemaProperty
        public void setCssSelector(String cssSelector) {
            this.cssSelector = cssSelector;
        }

        public String getCssSelector() {
            return cssSelector;
        }

        @SchemaProperty
        public void setProperty(Property property) {
            this.property = property;
        }

        public Property getProperty() {
            return property;
        }

        public static class Property {
            private String name;
            private String value;

            @SchemaProperty
            public void setName(String name) {
                this.name = name;
            }

            public String getName() {
                return name;
            }

            @SchemaProperty
            public void setValue(String value) {
                this.value = value;
            }

            public String getValue() {
                return value;
            }
        }
    }
}
