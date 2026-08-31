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

import java.util.Arrays;
import java.util.List;

import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.playwright.endpoint.PlaywrightBrowser;
import org.citrusframework.playwright.util.LocatorResolver;

/**
 * Citrus action for granting or clearing permissions on the current context.
 */
public class PermissionAction extends AbstractPlaywrightAction {

    public enum Command {
        GRANT,
        CLEAR
    }

    private final Command command;
    private final List<String> permissions;

    public PermissionAction(Builder builder) {
        super("permissions", builder);
        this.command = builder.command;
        this.permissions = builder.permissions;
    }

    @Override
    protected void execute(PlaywrightBrowser browser, TestContext context) {
        if (command == Command.CLEAR) {
            browser.getCurrentContext().clearPermissions();
        } else {
            browser.getCurrentContext().grantPermissions(permissions.stream()
                    .map(permission -> LocatorResolver.resolve(permission, context))
                    .toList());
        }
    }

    /**
     * Fluent builder for permission commands.
     */
    public static class Builder extends AbstractPlaywrightAction.Builder<PermissionAction, Builder> {
        private Command command;
        private List<String> permissions;

        /**
         * Grants permissions such as {@code geolocation} to the current context.
         *
         * @param permissions permission names
         * @return this builder
         */
        public Builder grant(String... permissions) {
            this.command = Command.GRANT;
            this.permissions = Arrays.asList(permissions);
            return this;
        }

        /**
         * Clears all granted permissions from the current context.
         *
         * @return this builder
         */
        public Builder clear() {
            this.command = Command.CLEAR;
            return this;
        }

        @Override
        public PermissionAction build() {
            if (command == null) {
                throw new CitrusRuntimeException("Missing Playwright permission command");
            }
            if (command == Command.GRANT && (permissions == null || permissions.isEmpty())) {
                throw new CitrusRuntimeException("Missing Playwright permissions to grant");
            }
            return new PermissionAction(this);
        }
    }
}
