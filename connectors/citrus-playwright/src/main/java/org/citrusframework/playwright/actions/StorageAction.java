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

import com.microsoft.playwright.WebStorage;

import java.nio.file.Path;

import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.exceptions.ValidationException;
import org.citrusframework.playwright.endpoint.PlaywrightBrowser;
import org.citrusframework.playwright.util.LocatorResolver;

/**
 * Citrus action for browser storage and context storage-state operations.
 *
 * <p>The action can target local storage or session storage on the current
 * page, and can also save or restore full Playwright context storage state
 * files for cross-test reuse.</p>
 */
public class StorageAction extends AbstractPlaywrightAction {

    public enum Scope {
        LOCAL,
        SESSION
    }

    public enum Command {
        SET,
        READ,
        VERIFY,
        REMOVE,
        CLEAR,
        SAVE_STATE,
        RESTORE_STATE
    }

    private final Scope scope;
    private final Command command;
    private final String key;
    private final String value;
    private final String variable;
    private final String path;

    public StorageAction(Builder builder) {
        super("storage", builder);
        this.scope = builder.scope;
        this.command = builder.command;
        this.key = builder.key;
        this.value = builder.value;
        this.variable = builder.variable;
        this.path = builder.path;
    }

    @Override
    protected void execute(PlaywrightBrowser browser, TestContext context) {
        switch (command) {
            case SET -> storage(browser).setItem(LocatorResolver.resolve(key, context), LocatorResolver.resolve(value, context));
            case READ -> context.setVariable(variable, storage(browser).getItem(LocatorResolver.resolve(key, context)));
            case VERIFY -> {
                String actual = storage(browser).getItem(LocatorResolver.resolve(key, context));
                String expected = LocatorResolver.resolve(value, context);
                if (!expected.equals(actual)) {
                    throw new ValidationException("Expected %s storage key '%s' to be '%s' but got '%s'"
                            .formatted(scope.name().toLowerCase(), key, expected, actual));
                }
            }
            case REMOVE -> storage(browser).removeItem(LocatorResolver.resolve(key, context));
            case CLEAR -> storage(browser).clear();
            case SAVE_STATE -> browser.getCurrentContext()
                    .storageState(new com.microsoft.playwright.BrowserContext.StorageStateOptions()
                            .setPath(Path.of(LocatorResolver.resolve(path, context))));
            case RESTORE_STATE -> browser.getCurrentContext().setStorageState(Path.of(LocatorResolver.resolve(path, context)));
        }
    }

    private WebStorage storage(PlaywrightBrowser browser) {
        return scope == Scope.SESSION ? browser.getCurrentPage().sessionStorage() : browser.getCurrentPage().localStorage();
    }

    /**
     * Fluent builder for local/session storage and storage-state commands.
     */
    public static class Builder extends AbstractPlaywrightAction.Builder<StorageAction, Builder> {
        private Scope scope = Scope.LOCAL;
        private Command command;
        private String key;
        private String value;
        private String variable;
        private String path;

        /**
         * Targets local storage.
         *
         * @return this builder
         */
        public Builder local() {
            this.scope = Scope.LOCAL;
            return this;
        }

        /**
         * Targets session storage.
         *
         * @return this builder
         */
        public Builder session() {
            this.scope = Scope.SESSION;
            return this;
        }

        /**
         * Sets a storage key to a value.
         *
         * @param key storage key
         * @param value storage value
         * @return this builder
         */
        public Builder set(String key, String value) {
            this.command = Command.SET;
            this.key = key;
            this.value = value;
            return this;
        }

        /**
         * Reads a storage key.
         *
         * @param key storage key
         * @return this builder
         */
        public Builder read(String key) {
            this.command = Command.READ;
            this.key = key;
            return this;
        }

        /**
         * Verifies a storage key value.
         *
         * @param key storage key
         * @param value expected value
         * @return this builder
         */
        public Builder verify(String key, String value) {
            this.command = Command.VERIFY;
            this.key = key;
            this.value = value;
            return this;
        }

        /**
         * Removes a storage key.
         *
         * @param key storage key
         * @return this builder
         */
        public Builder remove(String key) {
            this.command = Command.REMOVE;
            this.key = key;
            return this;
        }

        /**
         * Clears all keys from the selected storage scope.
         *
         * @return this builder
         */
        public Builder clear() {
            this.command = Command.CLEAR;
            return this;
        }

        /**
         * Saves full browser context storage state to a JSON file.
         *
         * @param path storage-state JSON target path
         * @return this builder
         */
        public Builder saveState(String path) {
            this.command = Command.SAVE_STATE;
            this.path = path;
            return this;
        }

        /**
         * Restores full browser context storage state from a JSON file.
         *
         * @param path storage-state JSON source path
         * @return this builder
         */
        public Builder restoreState(String path) {
            this.command = Command.RESTORE_STATE;
            this.path = path;
            return this;
        }

        /**
         * Stores the read storage value in a Citrus variable.
         *
         * @param variable variable name
         * @return this builder
         */
        public Builder variable(String variable) {
            this.variable = variable;
            return this;
        }

        @Override
        public StorageAction build() {
            if (command == null) {
                throw new CitrusRuntimeException("Missing Playwright storage command");
            }
            if ((command == Command.SET || command == Command.VERIFY) && (key == null || value == null)) {
                throw new CitrusRuntimeException("Missing Playwright storage key or value");
            }
            if ((command == Command.READ || command == Command.REMOVE) && key == null) {
                throw new CitrusRuntimeException("Missing Playwright storage key");
            }
            if (command == Command.READ && variable == null) {
                throw new CitrusRuntimeException("Missing Playwright storage target variable");
            }
            if ((command == Command.SAVE_STATE || command == Command.RESTORE_STATE) && path == null) {
                throw new CitrusRuntimeException("Missing Playwright storage-state path");
            }
            return new StorageAction(this);
        }
    }
}
