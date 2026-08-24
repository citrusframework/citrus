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

import com.microsoft.playwright.Locator;

import org.citrusframework.context.TestContext;
import org.citrusframework.playwright.endpoint.PlaywrightBrowser;
import org.citrusframework.playwright.util.LocatorResolver;

public class MouseAction extends AbstractPlaywrightAction {

    public enum Command {
        CLICK,
        DOUBLE_CLICK,
        RIGHT_CLICK,
        HOVER,
        FOCUS,
        TAP
    }

    private final Command command;
    private final org.citrusframework.playwright.model.LocatorSpec locator;

    public MouseAction(Builder builder) {
        super(builder.command.name().toLowerCase().replace('_', '-'), builder);
        this.command = builder.command;
        this.locator = builder.locator;
    }

    @Override
    protected void execute(PlaywrightBrowser browser, TestContext context) {
        Locator element = LocatorResolver.resolve(browser.getCurrentPage(), locator, context);
        switch (command) {
            case CLICK -> element.click();
            case DOUBLE_CLICK -> element.dblclick();
            case RIGHT_CLICK -> element.click(new Locator.ClickOptions().setButton(com.microsoft.playwright.options.MouseButton.RIGHT));
            case HOVER -> element.hover();
            case FOCUS -> element.focus();
            case TAP -> element.tap();
        }
    }

    public Command getCommand() {
        return command;
    }

    public static class Builder extends ElementActionBuilder<MouseAction, Builder> {
        private final Command command;

        public Builder(Command command) {
            this.command = command;
        }

        @Override
        public MouseAction build() {
            requireLocator();
            return new MouseAction(this);
        }
    }
}
