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
import com.microsoft.playwright.options.ScrollMode;

import java.util.EnumSet;
import java.util.Set;

import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.playwright.util.ScrollModes;
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
    private final ScrollMode scroll;

    public MouseAction(Builder builder) {
        super(builder.command.name().toLowerCase().replace('_', '-'), builder);
        this.command = builder.command;
        this.locator = builder.locator;
        this.scroll = builder.scroll;
    }

    @Override
    protected void execute(PlaywrightBrowser browser, TestContext context) {
        Locator element = LocatorResolver.resolve(browser.getCurrentPage(), locator, context);
        boolean suppressScroll = scroll == ScrollMode.NONE;
        switch (command) {
            case CLICK -> {
                if (suppressScroll) {
                    element.click(new Locator.ClickOptions().setScroll(scroll));
                } else {
                    element.click();
                }
            }
            case DOUBLE_CLICK -> {
                if (suppressScroll) {
                    element.dblclick(new Locator.DblclickOptions().setScroll(scroll));
                } else {
                    element.dblclick();
                }
            }
            case RIGHT_CLICK -> {
                Locator.ClickOptions options = new Locator.ClickOptions()
                        .setButton(com.microsoft.playwright.options.MouseButton.RIGHT);
                element.click(suppressScroll ? options.setScroll(scroll) : options);
            }
            case HOVER -> {
                if (suppressScroll) {
                    element.hover(new Locator.HoverOptions().setScroll(scroll));
                } else {
                    element.hover();
                }
            }
            case FOCUS -> element.focus();
            case TAP -> {
                if (suppressScroll) {
                    element.tap(new Locator.TapOptions().setScroll(scroll));
                } else {
                    element.tap();
                }
            }
        }
    }

    public Command getCommand() {
        return command;
    }

    public static class Builder extends ElementActionBuilder<MouseAction, Builder> {

        private static final Set<Command> SCROLL_AWARE = EnumSet.of(Command.CLICK, Command.DOUBLE_CLICK,
                Command.RIGHT_CLICK, Command.HOVER, Command.TAP);

        private final Command command;
        private ScrollMode scroll = ScrollMode.AUTO;

        public Builder(Command command) {
            this.command = command;
        }

        /**
         * Controls whether Playwright scrolls the element into view before acting on it.
         * Supported values are {@code auto} (default) and {@code none}.
         *
         * @param mode scroll mode name
         * @return this builder
         */
        public Builder scroll(String mode) {
            this.scroll = ScrollModes.parse(mode);
            return this;
        }

        @Override
        public MouseAction build() {
            requireLocator();
            if (scroll != ScrollMode.AUTO && !SCROLL_AWARE.contains(command)) {
                throw new CitrusRuntimeException(
                        "Playwright command %s does not support the scroll option".formatted(command));
            }
            return new MouseAction(this);
        }
    }
}
