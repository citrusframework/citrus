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
import org.citrusframework.playwright.actions.AbstractPlaywrightAction;
import org.citrusframework.playwright.actions.ContextAction;
import org.citrusframework.playwright.endpoint.PlaywrightBrowser;
import org.citrusframework.playwright.util.DslCommands;

/**
 * Creates, switches or closes a named browser context.
 *
 * <p>Arbitrary {@code Browser.NewContextOptions} have no declarative equivalent; only the storage
 * state path is exposed here.</p>
 */
public class Context extends AbstractPlaywrightAction.Builder<ContextAction, Context> {

    private final ContextAction.Builder delegate = new ContextAction.Builder();

    private String command;
    private String alias;

    @SchemaProperty
    public void setCommand(String command) {
        this.command = command;
    }

    @SchemaProperty
    public void setAlias(String alias) {
        this.alias = alias;
    }

    @SchemaProperty
    public void setStorageState(String path) {
        delegate.storageState(path);
    }

    @Override
    public Context description(String description) {
        delegate.description(description);
        return this;
    }

    @Override
    public Context actor(TestActor actor) {
        delegate.actor(actor);
        return this;
    }

    @Override
    public Context browser(PlaywrightBrowser browser) {
        delegate.browser(browser);
        return this;
    }

    @Override
    public ContextAction build() {
        switch (DslCommands.normalize(command)) {
            case "create" -> delegate.newContext(alias);
            case "switch" -> delegate.switchTo(alias);
            case "close" -> delegate.close(alias);
            default -> throw DslCommands.unsupported("context", command);
        }

        return delegate.build();
    }
}
