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

package org.citrusframework.playwright.actions;

import com.microsoft.playwright.FrameLocator;
import com.microsoft.playwright.Locator;

import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.exceptions.ValidationException;
import org.citrusframework.playwright.endpoint.PlaywrightBrowser;
import org.citrusframework.playwright.model.LocatorSpec;
import org.citrusframework.playwright.util.LocatorResolver;

/**
 * Citrus action for interacting with locators inside a Playwright frame locator.
 *
 * <p>The frame selector and inner locator are resolved with Citrus variables at
 * execution time, allowing reusable tests to target dynamic frame content.</p>
 */
public class FrameAction extends AbstractPlaywrightAction {

    public enum Command {
        FILL,
        CLICK,
        VERIFY_TEXT
    }

    private final String frameSelector;
    private final LocatorSpec locator;
    private final Command command;
    private final String value;

    public FrameAction(Builder builder) {
        super("frame", builder);
        this.frameSelector = builder.frameSelector;
        this.locator = builder.locator;
        this.command = builder.command;
        this.value = builder.value;
    }

    @Override
    protected void execute(PlaywrightBrowser browser, TestContext context) {
        FrameLocator frame = browser.getCurrentPage().frameLocator(LocatorResolver.resolve(frameSelector, context));
        Locator element = LocatorResolver.resolve(frame, locator, context);
        switch (command) {
            case FILL -> element.fill(LocatorResolver.resolve(value, context));
            case CLICK -> element.click();
            case VERIFY_TEXT -> {
                String actual = element.textContent();
                String expected = LocatorResolver.resolve(value, context);
                if (!expected.equals(actual)) {
                    throw new ValidationException("Expected frame locator text '%s' but got '%s'".formatted(expected, actual));
                }
            }
        }
    }

    /**
     * Fluent builder for frame-scoped locator commands.
     */
    public static class Builder extends AbstractPlaywrightAction.Builder<FrameAction, Builder> {
        private String frameSelector;
        private LocatorSpec locator;
        private Command command;
        private String value;

        /**
         * Selects the frame locator that contains the target element.
         *
         * @param selector CSS selector for the frame locator
         * @return this builder
         */
        public Builder frame(String selector) {
            this.frameSelector = selector;
            return this;
        }

        /**
         * Fills an element inside the selected frame.
         *
         * @param locator CSS locator inside the frame
         * @return this builder
         */
        public Builder fill(String locator) {
            this.command = Command.FILL;
            this.locator = LocatorSpec.css(locator);
            return this;
        }

        /**
         * Clicks an element inside the selected frame.
         *
         * @param locator CSS locator inside the frame
         * @return this builder
         */
        public Builder click(String locator) {
            this.command = Command.CLICK;
            this.locator = LocatorSpec.css(locator);
            return this;
        }

        /**
         * Verifies text content for an element inside the selected frame.
         *
         * @param locator CSS locator inside the frame
         * @param text expected text
         * @return this builder
         */
        public Builder verifyText(String locator, String text) {
            this.command = Command.VERIFY_TEXT;
            this.locator = LocatorSpec.css(locator);
            this.value = text;
            return this;
        }

        /**
         * Sets the value used by the fill command.
         *
         * @param value value to type into the frame element
         * @return this builder
         */
        public Builder value(String value) {
            this.value = value;
            return this;
        }

        @Override
        public FrameAction build() {
            if (frameSelector == null || frameSelector.isBlank() || locator == null || command == null) {
                throw new CitrusRuntimeException("Missing Playwright frame selector, locator, or command");
            }
            if (command == Command.FILL && (value == null || value.isBlank())) {
                throw new CitrusRuntimeException("Missing Playwright frame fill value");
            }
            return new FrameAction(this);
        }
    }
}
