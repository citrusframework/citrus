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

import org.citrusframework.api.actions.selenium.SeleniumDragAndDropActionBuilder;
import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.selenium.endpoint.SeleniumBrowser;
import org.citrusframework.selenium.model.ElementLocator;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;

/**
 * Drags a source element onto a target element on the current page. Both elements get located
 * with the given selectors and the drag and drop is performed with WebDriver mouse actions.
 *
 * <p>Note: WebDriver mouse actions do not fire the native HTML5 drag and drop events
 * ({@code dragstart}, {@code dragover}, {@code drop}). Pages relying on those events need
 * a real browser and may not be testable with pure WebDriver actions.</p>
 *
 * @since 5.1
 */
public class DragAndDropAction extends FindElementAction {

    /** Target element locator */
    private final ElementLocator target;

    public DragAndDropAction(Builder builder) {
        super("drag-and-drop", builder);

        this.target = builder.target;
    }

    @Override
    protected void execute(WebElement source, SeleniumBrowser browser, TestContext context) {
        super.execute(source, browser, context);

        if (target == null) {
            throw new CitrusRuntimeException("Missing target element for drag and drop action");
        }

        By targetBy = target.resolve(context);

        WebElement targetElement;
        try {
            targetElement = browser.getWebDriver().findElement(targetBy);
        } catch (NoSuchElementException e) {
            throw new CitrusRuntimeException(String.format("Failed to find element '%s' on page", targetBy), e);
        }

        Actions builder = new Actions(browser.getWebDriver());
        builder.dragAndDrop(source, targetElement).perform();
    }

    /**
     * Gets the target element locator.
     * @return
     */
    public ElementLocator getTarget() {
        return target;
    }

    public static class Builder extends ElementActionBuilder<DragAndDropAction, Builder>
            implements SeleniumDragAndDropActionBuilder<DragAndDropAction, Builder> {

        private ElementLocator target;

        @Override
        public Builder target(Object target) {
            if (target instanceof By targetBy) {
                this.target = ElementLocator.of(targetBy);
            } else {
                throw new CitrusRuntimeException(("Invalid element identifier object, expected By object but got %s")
                        .formatted(target != null ? target.getClass().getName() : "null"));
            }

            return this;
        }

        @Override
        public Builder target(String property, String propertyValue) {
            this.target = ElementLocator.of(property, propertyValue);
            return this;
        }

        @Override
        public DragAndDropAction build() {
            return new DragAndDropAction(this);
        }
    }
}
