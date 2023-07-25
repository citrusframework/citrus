/*
 *  Copyright 2023 the original author or authors.
 *
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements. See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License. You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.citrusframework.selenium.yaml;

import org.citrusframework.selenium.actions.FindElementAction;
import org.openqa.selenium.By;

public interface ElementAware {

    /**
     * Gets the element aware builder to work with.
     * @return the element aware builder.
     */
    FindElementAction.ElementActionBuilder<?, ?> getElementBuilder();

    /**
     * Sets the element properties on the element aware builder.
     * @param element
     */
    default void setElement(Element element) {
        if (element.getId() != null) {
            getElementBuilder().element(By.id(element.id));
        } else if (element.getName() != null) {
            getElementBuilder().element(By.name(element.name));
        } else if (element.getLinkText() != null) {
            getElementBuilder().element(By.linkText(element.linkText));
        } else if (element.getPartialLinkText() != null) {
            getElementBuilder().element(By.partialLinkText(element.partialLinkText));
        } else if (element.getXpath() != null) {
            getElementBuilder().element(By.xpath(element.xpath));
        } else if (element.getCssSelector() != null) {
            getElementBuilder().element(By.cssSelector(element.cssSelector));
        } else if (element.getProperty() != null) {
            getElementBuilder().element(element.getProperty().getName(), element.getProperty().getValue());
        } else if (element.getClassName() != null) {
            getElementBuilder().element(By.className(element.className));
        } else if (element.getTagName() != null) {
            getElementBuilder().element(By.tagName(element.tagName));
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

        public static class Property {
            private String name;
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
