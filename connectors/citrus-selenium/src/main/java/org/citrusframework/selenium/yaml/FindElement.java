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

import java.util.ArrayList;
import java.util.List;

import org.citrusframework.TestActor;
import org.citrusframework.selenium.actions.AbstractSeleniumAction;
import org.citrusframework.selenium.actions.FindElementAction;
import org.citrusframework.selenium.endpoint.SeleniumBrowser;

public class FindElement extends AbstractSeleniumAction.Builder<FindElementAction, FindElement> implements ElementAware {

    private final FindElementAction.Builder delegate = new FindElementAction.Builder();

    @Override
    public void setElement(Element element) {
        ElementAware.super.setElement(element);
    }

    public void setValidate(Validate validate) {
        if (validate.getText() != null) {
            delegate.text(validate.text);
        }

        if (validate.getTagName() != null) {
            delegate.tagName(validate.tagName);
        }

        delegate.enabled(validate.enabled);
        delegate.displayed(validate.displayed);

        for (Attribute attribute : validate.getAttributes()) {
            delegate.attribute(attribute.getName(), attribute.getValue());
        }

        for (Style style : validate.getStyles()) {
            delegate.style(style.getName(), style.getValue());
        }
    }

    @Override
    public FindElementAction.ElementActionBuilder<?, ?> getElementBuilder() {
        return delegate;
    }

    @Override
    public FindElement description(String description) {
        delegate.description(description);
        return this;
    }

    @Override
    public FindElement actor(TestActor actor) {
        delegate.actor(actor);
        return this;
    }

    @Override
    public FindElement browser(SeleniumBrowser seleniumBrowser) {
        delegate.browser(seleniumBrowser);
        return this;
    }

    @Override
    public FindElementAction build() {
        return delegate.build();
    }

    public static class Validate {
        private String text;
        private String tagName;
        private boolean displayed = true;
        private boolean enabled = true;

        private List<Attribute> attributes;
        private List<Style> styles;

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        public String getTagName() {
            return tagName;
        }

        public void setTagName(String tagName) {
            this.tagName = tagName;
        }

        public List<Attribute> getAttributes() {
            if (attributes == null) {
                attributes = new ArrayList<>();
            }

            return attributes;
        }

        public void setAttributes(List<Attribute> attributes) {
            this.attributes = attributes;
        }

        public List<Style> getStyles() {
            if (styles == null) {
                styles = new ArrayList<>();
            }

            return styles;
        }

        public void setStyles(List<Style> styles) {
            this.styles = styles;
        }

        public boolean isDisplayed() {
            return displayed;
        }

        public void setDisplayed(boolean displayed) {
            this.displayed = displayed;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }

    public static class Style {
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

    public static class Attribute {
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
