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

import org.citrusframework.api.yaml.SchemaProperty;

import org.citrusframework.TestActor;
import org.citrusframework.playwright.actions.AbstractPlaywrightAction;
import org.citrusframework.playwright.actions.CredentialsAction;
import org.citrusframework.playwright.endpoint.PlaywrightBrowser;
import org.citrusframework.playwright.util.DslCommands;

/**
 * Manages virtual WebAuthn credentials on the current browser context.
 */
public class Credentials extends AbstractPlaywrightAction.Builder<CredentialsAction, Credentials> {

    private final CredentialsAction.Builder delegate = new CredentialsAction.Builder();

    private String command;
    private String origin;
    private String variable;

    @SchemaProperty
    public void setOrigin(String origin) {
        this.origin = origin;
    }

    @SchemaProperty
    public void setVariable(String variable) {
        this.variable = variable;
    }

    @SchemaProperty
    public void setCommand(String command) {
        this.command = command;
    }

    @SchemaProperty
    public void setId(String id) {
        delegate.id(id);
    }

    @SchemaProperty
    public void setUserHandle(String userHandle) {
        delegate.userHandle(userHandle);
    }

    @SchemaProperty
    public void setPrivateKey(String privateKey) {
        delegate.privateKey(privateKey);
    }

    @SchemaProperty
    public void setPublicKey(String publicKey) {
        delegate.publicKey(publicKey);
    }

    @Override
    public Credentials description(String description) {
        delegate.description(description);
        return this;
    }

    @Override
    public Credentials actor(TestActor actor) {
        delegate.actor(actor);
        return this;
    }

    @Override
    public Credentials browser(PlaywrightBrowser browser) {
        delegate.browser(browser);
        return this;
    }

    @Override
    public CredentialsAction build() {
        switch (DslCommands.normalize(command)) {
            case "create" -> delegate.create(origin);
            case "install" -> delegate.install();
            case "read" -> delegate.read(variable);
            case "delete" -> delegate.delete(origin);
            default -> throw DslCommands.unsupported("credentials", command);
        }
        return delegate.build();
    }
}
