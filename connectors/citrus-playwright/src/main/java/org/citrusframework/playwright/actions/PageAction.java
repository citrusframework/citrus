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

import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.playwright.endpoint.PlaywrightBrowser;
import org.citrusframework.playwright.util.LocatorResolver;

/**
 * Citrus action for managing named Playwright pages.
 *
 * <p>Pages are registered under aliases so tests can model multiple tabs or
 * windows. Switching pages also switches to the owning browser context.</p>
 */
public class PageAction extends AbstractPlaywrightAction {

    public enum Command {
        CREATE,
        SWITCH_ALIAS,
        SWITCH_INDEX,
        SWITCH_TITLE,
        SWITCH_URL,
        CLOSE
    }

    private final Command command;
    private final String alias;
    private final String contextAlias;
    private final Integer index;

    public PageAction(Builder builder) {
        super("page", builder);
        this.command = builder.command;
        this.alias = builder.alias;
        this.contextAlias = builder.contextAlias;
        this.index = builder.index;
    }

    @Override
    protected void execute(PlaywrightBrowser browser, TestContext context) {
        switch (command) {
            case CREATE -> {
                if (contextAlias == null) {
                    browser.createPage(LocatorResolver.resolve(alias, context));
                } else {
                    browser.createPage(LocatorResolver.resolve(alias, context), LocatorResolver.resolve(contextAlias, context));
                }
            }
            case SWITCH_ALIAS -> browser.switchPage(LocatorResolver.resolve(alias, context));
            case SWITCH_INDEX -> browser.switchPageByIndex(index);
            case SWITCH_TITLE -> browser.switchPageByTitle(LocatorResolver.resolve(alias, context));
            case SWITCH_URL -> browser.switchPageByUrlContaining(LocatorResolver.resolve(alias, context));
            case CLOSE -> browser.closePage(LocatorResolver.resolve(alias, context));
        }
    }

    /**
     * Fluent builder for page create, switch, and close commands.
     */
    public static class Builder extends AbstractPlaywrightAction.Builder<PageAction, Builder> {
        private Command command;
        private String alias;
        private String contextAlias;
        private Integer index;

        /**
         * Creates a new page in the current context.
         *
         * @param alias alias for the created page
         * @return this builder
         */
        public Builder newPage(String alias) {
            this.command = Command.CREATE;
            this.alias = alias;
            return this;
        }

        /**
         * Creates the new page in a specific context instead of the current one.
         *
         * @param contextAlias context alias
         * @return this builder
         */
        public Builder inContext(String contextAlias) {
            this.contextAlias = contextAlias;
            return this;
        }

        /**
         * Switches to a page by alias.
         *
         * @param alias existing page alias
         * @return this builder
         */
        public Builder switchTo(String alias) {
            this.command = Command.SWITCH_ALIAS;
            this.alias = alias;
            return this;
        }

        /**
         * Switches to a page by insertion-order index.
         *
         * @param index zero-based page index
         * @return this builder
         */
        public Builder switchToIndex(int index) {
            this.command = Command.SWITCH_INDEX;
            this.index = index;
            return this;
        }

        /**
         * Switches to the first registered page with the given title.
         *
         * @param title expected page title
         * @return this builder
         */
        public Builder switchToTitle(String title) {
            this.command = Command.SWITCH_TITLE;
            this.alias = title;
            return this;
        }

        /**
         * Switches to the first registered page whose URL contains the text.
         *
         * @param urlPart URL fragment
         * @return this builder
         */
        public Builder switchToUrlContaining(String urlPart) {
            this.command = Command.SWITCH_URL;
            this.alias = urlPart;
            return this;
        }

        /**
         * Closes and unregisters a page.
         *
         * @param alias existing page alias
         * @return this builder
         */
        public Builder close(String alias) {
            this.command = Command.CLOSE;
            this.alias = alias;
            return this;
        }

        @Override
        public PageAction build() {
            if (command == null || (command == Command.SWITCH_INDEX && index == null)
                    || (command != Command.SWITCH_INDEX && (alias == null || alias.isBlank()))) {
                throw new CitrusRuntimeException("Missing Playwright page command or selector");
            }
            return new PageAction(this);
        }
    }
}
