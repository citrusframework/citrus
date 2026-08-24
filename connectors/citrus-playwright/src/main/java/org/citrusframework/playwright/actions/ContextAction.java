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

import com.microsoft.playwright.Browser;

import java.nio.file.Path;

import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.playwright.endpoint.PlaywrightBrowser;
import org.citrusframework.playwright.util.LocatorResolver;

/**
 * Citrus action for managing named Playwright browser contexts.
 *
 * <p>Contexts isolate storage, permissions, cookies, and emulation settings.
 * Creating or switching a context also updates the endpoint current context so
 * subsequent actions target the selected browser state.</p>
 */
public class ContextAction extends AbstractPlaywrightAction {

    public enum Command {
        CREATE,
        SWITCH,
        CLOSE
    }

    private final Command command;
    private final String alias;
    private final Browser.NewContextOptions options;

    public ContextAction(Builder builder) {
        super("context", builder);
        this.command = builder.command;
        this.alias = builder.alias;
        this.options = builder.options;
    }

    @Override
    protected void execute(PlaywrightBrowser browser, TestContext context) {
        String resolvedAlias = LocatorResolver.resolve(alias, context);
        switch (command) {
            case CREATE -> browser.createContext(resolvedAlias, options);
            case SWITCH -> browser.switchContext(resolvedAlias);
            case CLOSE -> browser.closeContext(resolvedAlias);
        }
    }

    /**
     * Fluent builder for context create, switch, and close commands.
     */
    public static class Builder extends AbstractPlaywrightAction.Builder<ContextAction, Builder> {
        private Command command;
        private String alias;
        private Browser.NewContextOptions options;

        /**
         * Creates a new context and registers it under the supplied alias.
         *
         * @param alias context alias, resolved through Citrus variables at runtime
         * @return this builder
         */
        public Builder newContext(String alias) {
            this.command = Command.CREATE;
            this.alias = alias;
            return this;
        }

        /**
         * Applies Playwright context options to a new context command.
         *
         * @param options Playwright context options
         * @return this builder
         */
        public Builder options(Browser.NewContextOptions options) {
            this.options = options;
            return this;
        }

        /**
         * Creates context options that restore browser storage state from a file.
         *
         * @param path storage-state JSON path
         * @return this builder
         */
        public Builder storageState(String path) {
            if (options == null) {
                options = new Browser.NewContextOptions();
            }
            options.setStorageStatePath(Path.of(path));
            return this;
        }

        /**
         * Switches the current endpoint context by alias.
         *
         * @param alias existing context alias
         * @return this builder
         */
        public Builder switchTo(String alias) {
            this.command = Command.SWITCH;
            this.alias = alias;
            return this;
        }

        /**
         * Closes and unregisters a context by alias.
         *
         * @param alias existing context alias
         * @return this builder
         */
        public Builder close(String alias) {
            this.command = Command.CLOSE;
            this.alias = alias;
            return this;
        }

        @Override
        public ContextAction build() {
            if (command == null || alias == null || alias.isBlank()) {
                throw new CitrusRuntimeException("Missing Playwright context command or alias");
            }
            return new ContextAction(this);
        }
    }
}
