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
import org.citrusframework.playwright.actions.FrameAction;
import org.citrusframework.playwright.endpoint.PlaywrightBrowser;
import org.citrusframework.playwright.model.LocatorSpec;
import org.citrusframework.playwright.util.DslCommands;

/**
 * Fills, clicks or verifies an element inside an iframe.
 */
public class Frame extends AbstractPlaywrightAction.Builder<FrameAction, Frame> {

    private final FrameAction.Builder delegate = new FrameAction.Builder();

    private String command;
    private LocatorSpec locator;
    private String value;

    @SchemaProperty
    public void setCommand(String command) {
        this.command = command;
    }

    @SchemaProperty
    public void setSelector(String selector) {
        delegate.frame(selector);
    }

    @SchemaProperty
    public void setElement(Element element) {
        this.locator = element.toLocatorSpec();
    }

    @SchemaProperty
    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public Frame description(String description) {
        delegate.description(description);
        return this;
    }

    @Override
    public Frame actor(TestActor actor) {
        delegate.actor(actor);
        return this;
    }

    @Override
    public Frame browser(PlaywrightBrowser browser) {
        delegate.browser(browser);
        return this;
    }

    @Override
    public FrameAction build() {
        if (locator == null) {
            throw new CitrusRuntimeException("Missing Playwright frame element");
        }

        switch (DslCommands.normalize(command)) {
            case "fill" -> delegate.fill(locator).value(value);
            case "click" -> delegate.click(locator);
            case "verify-text" -> delegate.verifyText(locator, value);
            default -> throw DslCommands.unsupported("frame", command);
        }

        return delegate.build();
    }
}
