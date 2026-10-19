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

package org.citrusframework.selenium.actions;

import org.citrusframework.api.actions.selenium.SeleniumFrameActionBuilder;
import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.selenium.endpoint.SeleniumBrowser;
import org.citrusframework.selenium.model.ElementLocator;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.NoSuchFrameException;
import org.openqa.selenium.WebElement;

/**
 * Switches the browser into a frame identified by a located frame element, a zero-based
 * index, or a frame name/id; to the parent frame; or to the top document.
 *
 * @since 5.1
 */
public class FrameAction extends AbstractSeleniumAction {

    private final ElementLocator element;
    private final Integer index;
    private final String nameOrId;
    private final boolean parent;
    private final boolean defaultContent;

    public FrameAction(Builder builder) {
        super("frame", builder);

        this.element = builder.element;
        this.index = builder.index;
        this.nameOrId = builder.nameOrId;
        this.parent = builder.parent;
        this.defaultContent = builder.defaultContent;
    }

    @Override
    protected void execute(SeleniumBrowser browser, TestContext context) {
        try {
            if (element != null) {
                By by = element.resolve(context);
                WebElement frameElement;
                try {
                    frameElement = browser.getWebDriver().findElement(by);
                } catch (NoSuchElementException e) {
                    throw new CitrusRuntimeException(String.format("Failed to find frame element '%s'", by), e);
                }
                browser.getWebDriver().switchTo().frame(frameElement);
            } else if (index != null) {
                browser.getWebDriver().switchTo().frame(index);
            } else if (nameOrId != null) {
                browser.getWebDriver().switchTo().frame(context.replaceDynamicContentInString(nameOrId));
            } else if (parent) {
                browser.getWebDriver().switchTo().parentFrame();
            } else if (defaultContent) {
                browser.getWebDriver().switchTo().defaultContent();
            } else {
                throw new CitrusRuntimeException("Missing frame target - use element, index, name, parent or default");
            }
        } catch (NoSuchFrameException e) {
            throw new CitrusRuntimeException("Failed to switch to frame", e);
        }
    }

    public ElementLocator getElement() {
        return element;
    }

    public Integer getIndex() {
        return index;
    }

    public String getNameOrId() {
        return nameOrId;
    }

    public boolean isParent() {
        return parent;
    }

    public boolean isDefaultContent() {
        return defaultContent;
    }

    public static class Builder extends AbstractSeleniumAction.Builder<FrameAction, Builder>
            implements SeleniumFrameActionBuilder<FrameAction, Builder> {

        private ElementLocator element;
        private Integer index;
        private String nameOrId;
        private boolean parent;
        private boolean defaultContent;

        @Override
        public Builder element(Object element) {
            if (element instanceof By byElement) {
                this.element = ElementLocator.of(byElement);
            } else {
                throw new CitrusRuntimeException(("Invalid element identifier object, expected By object but got %s").formatted(element.getClass().getName()));
            }
            return this;
        }

        @Override
        public Builder element(String property, String propertyValue) {
            this.element = ElementLocator.of(property, propertyValue);
            return this;
        }

        @Override
        public Builder index(int index) {
            this.index = index;
            return this;
        }

        @Override
        public Builder nameOrId(String nameOrId) {
            this.nameOrId = nameOrId;
            return this;
        }

        @Override
        public Builder parent() {
            this.parent = true;
            return this;
        }

        @Override
        public Builder defaultContent() {
            this.defaultContent = true;
            return this;
        }

        @Override
        public FrameAction build() {
            return new FrameAction(this);
        }
    }
}
