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

package org.citrusframework.playwright.xml;

import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlRootElement;

import org.citrusframework.TestActor;
import org.citrusframework.playwright.actions.AbstractPlaywrightAction;
import org.citrusframework.playwright.actions.ConsoleAction;
import org.citrusframework.playwright.endpoint.PlaywrightBrowser;
import org.citrusframework.playwright.util.DslCommands;

/**
 * Captures, clears, reports or verifies browser console messages.
 */
@XmlRootElement(name = "console")
public class Console extends AbstractPlaywrightAction.Builder<ConsoleAction, Console> {

    private final ConsoleAction.Builder delegate = new ConsoleAction.Builder();

    private String command;
    private String text;

    @XmlAttribute
    public void setCommand(String command) {
        this.command = command;
    }

    @XmlAttribute
    public void setText(String text) {
        this.text = text;
    }

    @XmlAttribute
    public void setVariable(String variable) {
        delegate.variable(variable);
    }

    @Override
    public Console description(String description) {
        delegate.description(description);
        return this;
    }

    @Override
    public Console actor(TestActor actor) {
        delegate.actor(actor);
        return this;
    }

    @Override
    public Console browser(PlaywrightBrowser browser) {
        delegate.browser(browser);
        return this;
    }

    @Override
    public ConsoleAction build() {
        switch (DslCommands.normalize(command)) {
            case "capture" -> delegate.capture();
            case "clear" -> delegate.clear();
            case "report" -> delegate.report();
            case "verify-contains" -> delegate.verifyContains(text);
            default -> throw DslCommands.unsupported("console", command);
        }

        return delegate.build();
    }
}
