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

import com.microsoft.playwright.Dialog;

import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.exceptions.ValidationException;
import org.citrusframework.playwright.endpoint.PlaywrightBrowser;
import org.citrusframework.playwright.util.LocatorResolver;

/**
 * Citrus action that registers one-shot handling for the next page dialog.
 *
 * <p>The action can optionally execute JavaScript that triggers the dialog,
 * assert the dialog type/message, and accept or dismiss the dialog.</p>
 */
public class DialogAction extends AbstractPlaywrightAction {

    public enum Command {
        ACCEPT,
        DISMISS
    }

    private final Command command;
    private final String promptText;
    private final String expectedMessage;
    private final String expectedType;
    private final String triggerScript;

    public DialogAction(Builder builder) {
        super("dialog", builder);
        this.command = builder.command;
        this.promptText = builder.promptText;
        this.expectedMessage = builder.expectedMessage;
        this.expectedType = builder.expectedType;
        this.triggerScript = builder.triggerScript;
    }

    @Override
    protected void execute(PlaywrightBrowser browser, TestContext context) {
        browser.getCurrentPage().onceDialog(dialog -> handleDialog(dialog, context));
        if (triggerScript != null) {
            browser.getCurrentPage().evaluate(LocatorResolver.resolve(triggerScript, context));
        }
    }

    private void handleDialog(Dialog dialog, TestContext context) {
        if (expectedMessage != null && !LocatorResolver.resolve(expectedMessage, context).equals(dialog.message())) {
            throw new ValidationException("Expected dialog message '%s' but got '%s'"
                    .formatted(LocatorResolver.resolve(expectedMessage, context), dialog.message()));
        }
        if (expectedType != null && !LocatorResolver.resolve(expectedType, context).equals(dialog.type())) {
            throw new ValidationException("Expected dialog type '%s' but got '%s'"
                    .formatted(LocatorResolver.resolve(expectedType, context), dialog.type()));
        }
        if (command == Command.ACCEPT) {
            if (promptText == null) {
                dialog.accept();
            } else {
                dialog.accept(LocatorResolver.resolve(promptText, context));
            }
        } else {
            dialog.dismiss();
        }
    }

    /**
     * Fluent builder for dialog handling commands.
     */
    public static class Builder extends AbstractPlaywrightAction.Builder<DialogAction, Builder> {
        private Command command;
        private String promptText;
        private String expectedMessage;
        private String expectedType;
        private String triggerScript;

        /**
         * Accepts the next dialog.
         *
         * @return this builder
         */
        public Builder accept() {
            this.command = Command.ACCEPT;
            return this;
        }

        /**
         * Dismisses the next dialog.
         *
         * @return this builder
         */
        public Builder dismiss() {
            this.command = Command.DISMISS;
            return this;
        }

        /**
         * Supplies text when accepting a prompt dialog.
         *
         * @param promptText prompt response text
         * @return this builder
         */
        public Builder promptText(String promptText) {
            this.promptText = promptText;
            return this;
        }

        /**
         * Verifies the expected dialog message before handling it.
         *
         * @param expectedMessage expected message
         * @return this builder
         */
        public Builder message(String expectedMessage) {
            this.expectedMessage = expectedMessage;
            return this;
        }

        /**
         * Verifies the expected dialog type before handling it.
         *
         * @param expectedType expected Playwright dialog type
         * @return this builder
         */
        public Builder type(String expectedType) {
            this.expectedType = expectedType;
            return this;
        }

        /**
         * Evaluates JavaScript after registering the dialog handler.
         *
         * @param triggerScript JavaScript expression or function body for Playwright evaluation
         * @return this builder
         */
        public Builder triggerScript(String triggerScript) {
            this.triggerScript = triggerScript;
            return this;
        }

        @Override
        public DialogAction build() {
            if (command == null) {
                throw new CitrusRuntimeException("Missing Playwright dialog command");
            }
            return new DialogAction(this);
        }
    }
}
