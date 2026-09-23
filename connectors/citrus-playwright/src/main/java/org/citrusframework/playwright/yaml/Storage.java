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

import org.citrusframework.TestActor;
import org.citrusframework.api.yaml.SchemaProperty;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.playwright.actions.AbstractPlaywrightAction;
import org.citrusframework.playwright.actions.StorageAction;
import org.citrusframework.playwright.endpoint.PlaywrightBrowser;
import org.citrusframework.playwright.util.DslCommands;

/**
 * Sets, reads, verifies or removes web storage entries, and saves or restores storage state.
 */
public class Storage extends AbstractPlaywrightAction.Builder<StorageAction, Storage> {

    private final StorageAction.Builder delegate = new StorageAction.Builder();

    private String command;
    private String key;
    private String value;
    private String path;

    @SchemaProperty
    public void setScope(String scope) {
        switch (DslCommands.normalize(scope)) {
            case "local" -> delegate.local();
            case "session" -> delegate.session();
            default -> throw new CitrusRuntimeException("Unsupported Playwright storage scope: " + scope);
        }
    }

    @SchemaProperty
    public void setCommand(String command) {
        this.command = command;
    }

    @SchemaProperty
    public void setKey(String key) {
        this.key = key;
    }

    @SchemaProperty
    public void setValue(String value) {
        this.value = value;
    }

    @SchemaProperty
    public void setPath(String path) {
        this.path = path;
    }

    @SchemaProperty
    public void setOpfs(Boolean opfs) {
        delegate.opfs(opfs);
    }

    @SchemaProperty
    public void setVariable(String variable) {
        delegate.variable(variable);
    }

    @Override
    public Storage description(String description) {
        delegate.description(description);
        return this;
    }

    @Override
    public Storage actor(TestActor actor) {
        delegate.actor(actor);
        return this;
    }

    @Override
    public Storage browser(PlaywrightBrowser browser) {
        delegate.browser(browser);
        return this;
    }

    @Override
    public StorageAction build() {
        switch (DslCommands.normalize(command)) {
            case "set" -> delegate.set(key, value);
            case "read" -> delegate.read(key);
            case "verify" -> delegate.verify(key, value);
            case "remove" -> delegate.remove(key);
            case "clear" -> delegate.clear();
            case "save-state" -> delegate.saveState(path);
            case "restore-state" -> delegate.restoreState(path);
            default -> throw DslCommands.unsupported("storage", command);
        }

        return delegate.build();
    }
}
