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

import java.util.List;
import java.util.stream.Collectors;

import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.exceptions.ValidationException;
import org.citrusframework.playwright.endpoint.PlaywrightBrowser;
import org.citrusframework.playwright.model.ConsoleMessageRecord;
import org.citrusframework.playwright.util.LocatorResolver;

/**
 * Citrus action for bounded console-message capture on the current page.
 *
 * <p>Capture is opt-in per page. Captured messages can be cleared, reported to
 * a Citrus variable, or verified by text fragment.</p>
 */
public class ConsoleAction extends AbstractPlaywrightAction {

    public enum Command {
        CAPTURE,
        CLEAR,
        REPORT,
        VERIFY_CONTAINS
    }

    private final Command command;
    private final String text;
    private final String variable;

    public ConsoleAction(Builder builder) {
        super("console", builder);
        this.command = builder.command;
        this.text = builder.text;
        this.variable = builder.variable;
    }

    @Override
    protected void execute(PlaywrightBrowser browser, TestContext context) {
        switch (command) {
            case CAPTURE -> browser.getConsoleCaptureRegistry()
                    .capture(browser.getCurrentPage(), browser.getEndpointConfiguration().getConsoleMessageLimit());
            case CLEAR -> browser.getConsoleCaptureRegistry().clear(browser.getCurrentPage());
            case REPORT -> {
                String report = report(browser);
                if (variable != null) {
                    context.setVariable(variable, report);
                }
            }
            case VERIFY_CONTAINS -> {
                String expected = LocatorResolver.resolve(text, context);
                boolean found = browser.getConsoleCaptureRegistry().messages(browser.getCurrentPage()).stream()
                        .anyMatch(message -> message.text().contains(expected));
                if (!found) {
                    throw new ValidationException("No Playwright console message contains: " + expected);
                }
            }
        }
    }

    private String report(PlaywrightBrowser browser) {
        List<ConsoleMessageRecord> messages = browser.getConsoleCaptureRegistry().messages(browser.getCurrentPage());
        return messages.stream().map(ConsoleMessageRecord::format).collect(Collectors.joining(System.lineSeparator()));
    }

    /**
     * Fluent builder for console capture, report, and verification commands.
     */
    public static class Builder extends AbstractPlaywrightAction.Builder<ConsoleAction, Builder> {
        private Command command;
        private String text;
        private String variable;

        /**
         * Starts bounded console capture for the current page.
         *
         * @return this builder
         */
        public Builder capture() {
            this.command = Command.CAPTURE;
            return this;
        }

        /**
         * Clears captured console messages for the current page.
         *
         * @return this builder
         */
        public Builder clear() {
            this.command = Command.CLEAR;
            return this;
        }

        /**
         * Formats captured console messages as a text report.
         *
         * @return this builder
         */
        public Builder report() {
            this.command = Command.REPORT;
            return this;
        }

        /**
         * Verifies that at least one captured console message contains text.
         *
         * @param text expected text fragment
         * @return this builder
         */
        public Builder verifyContains(String text) {
            this.command = Command.VERIFY_CONTAINS;
            this.text = text;
            return this;
        }

        /**
         * Stores the console report in a Citrus variable.
         *
         * @param variable variable name
         * @return this builder
         */
        public Builder variable(String variable) {
            this.variable = variable;
            return this;
        }

        @Override
        public ConsoleAction build() {
            if (command == null) {
                throw new CitrusRuntimeException("Missing Playwright console command");
            }
            if (command == Command.VERIFY_CONTAINS && text == null) {
                throw new CitrusRuntimeException("Missing Playwright console verification text");
            }
            return new ConsoleAction(this);
        }
    }
}
