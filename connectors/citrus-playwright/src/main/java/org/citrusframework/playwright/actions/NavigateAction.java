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

import org.citrusframework.context.TestContext;
import org.citrusframework.playwright.endpoint.PlaywrightBrowser;

public class NavigateAction extends AbstractPlaywrightAction {

    public enum Command {
        BACK,
        FORWARD,
        RELOAD
    }

    private final Command command;

    public NavigateAction(Builder builder) {
        super("navigate", builder);
        this.command = builder.command;
    }

    @Override
    protected void execute(PlaywrightBrowser browser, TestContext context) {
        switch (command) {
            case BACK -> browser.getCurrentPage().goBack();
            case FORWARD -> browser.getCurrentPage().goForward();
            case RELOAD -> browser.getCurrentPage().reload();
        }
    }

    public Command getCommand() {
        return command;
    }

    public static class Builder extends AbstractPlaywrightAction.Builder<NavigateAction, Builder> {
        private Command command = Command.RELOAD;

        public Builder back() {
            this.command = Command.BACK;
            return this;
        }

        public Builder forward() {
            this.command = Command.FORWARD;
            return this;
        }

        public Builder reload() {
            this.command = Command.RELOAD;
            return this;
        }

        @Override
        public NavigateAction build() {
            return new NavigateAction(this);
        }
    }
}
