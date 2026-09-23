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

package org.citrusframework.playwright.yaml;

import java.util.List;

import org.citrusframework.TestActor;
import org.citrusframework.api.yaml.SchemaProperty;
import org.citrusframework.playwright.actions.AbstractPlaywrightAction;
import org.citrusframework.playwright.actions.PermissionAction;
import org.citrusframework.playwright.endpoint.PlaywrightBrowser;
import org.citrusframework.playwright.util.DslCommands;

/**
 * Grants or clears browser permissions of the current browser context.
 */
public class Permissions extends AbstractPlaywrightAction.Builder<PermissionAction, Permissions> {

    private final PermissionAction.Builder delegate = new PermissionAction.Builder();

    private String command;
    private List<String> permissions;

    @SchemaProperty
    public void setCommand(String command) {
        this.command = command;
    }

    @SchemaProperty
    public void setPermissions(List<String> permissions) {
        this.permissions = permissions;
    }

    @Override
    public Permissions description(String description) {
        delegate.description(description);
        return this;
    }

    @Override
    public Permissions actor(TestActor actor) {
        delegate.actor(actor);
        return this;
    }

    @Override
    public Permissions browser(PlaywrightBrowser browser) {
        delegate.browser(browser);
        return this;
    }

    @Override
    public PermissionAction build() {
        switch (DslCommands.normalize(command)) {
            case "grant" -> delegate.grant(permissions == null ? new String[0] : permissions.toArray(String[]::new));
            case "clear" -> delegate.clear();
            default -> throw DslCommands.unsupported("permissions", command);
        }

        return delegate.build();
    }
}
