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
import org.citrusframework.playwright.actions.DialogAction;
import org.citrusframework.playwright.endpoint.PlaywrightBrowser;
import org.citrusframework.playwright.util.DslCommands;

/**
 * Accepts, dismisses or verifies the closing of the next JavaScript dialog.
 */
public class Dialog extends AbstractPlaywrightAction.Builder<DialogAction, Dialog> {

    private final DialogAction.Builder delegate = new DialogAction.Builder();

    private String command;

    @SchemaProperty
    public void setCommand(String command) {
        this.command = command;
    }

    @SchemaProperty
    public void setPromptText(String promptText) {
        delegate.promptText(promptText);
    }

    @SchemaProperty
    public void setMessage(String message) {
        delegate.message(message);
    }

    @SchemaProperty
    public void setType(String type) {
        delegate.type(type);
    }

    @SchemaProperty
    public void setTriggerScript(String triggerScript) {
        delegate.triggerScript(triggerScript);
    }

    @Override
    public Dialog description(String description) {
        delegate.description(description);
        return this;
    }

    @Override
    public Dialog actor(TestActor actor) {
        delegate.actor(actor);
        return this;
    }

    @Override
    public Dialog browser(PlaywrightBrowser browser) {
        delegate.browser(browser);
        return this;
    }

    @Override
    public DialogAction build() {
        switch (DslCommands.normalize(command)) {
            case "accept" -> delegate.accept();
            case "dismiss" -> delegate.dismiss();
            case "verify-closed" -> delegate.verifyClosed();
            default -> throw DslCommands.unsupported("dialog", command);
        }

        return delegate.build();
    }
}
