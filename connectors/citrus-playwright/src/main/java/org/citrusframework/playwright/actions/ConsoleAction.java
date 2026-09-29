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
import java.util.Locale;
import java.util.stream.Collectors;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.ConsoleMessagesFilter;

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
        VERIFY_CONTAINS,
        VERIFY_NO_PAGE_ERRORS,
        VERIFY_PAGE_ERRORS_CONTAIN,
        PAGE_ERRORS_REPORT
    }

    private final Command command;
    private final String text;
    private final String variable;
    private final String filter;

    public ConsoleAction(Builder builder) {
        super("console", builder);
        this.command = builder.command;
        this.text = builder.text;
        this.variable = builder.variable;
        this.filter = builder.filter;
    }

    @Override
    protected void execute(PlaywrightBrowser browser, TestContext context) {
        switch (command) {
            case CAPTURE -> browser.getConsoleCaptureRegistry()
                    .capture(browser.getCurrentPage(), browser.getEndpointConfiguration().getConsoleMessageLimit());
            case CLEAR -> browser.getConsoleCaptureRegistry().clear(browser.getCurrentPage());
            case REPORT -> {
                String report = report(browser, context);
                if (variable != null) {
                    context.setVariable(variable, report);
                }
            }
            case VERIFY_CONTAINS -> {
                String expected = LocatorResolver.resolve(text, context);
                boolean found = messages(browser, context).stream()
                        .anyMatch(message -> message.text().contains(expected));
                if (!found) {
                    throw new ValidationException("No Playwright console message contains: " + expected);
                }
            }
            case VERIFY_NO_PAGE_ERRORS -> {
                List<String> errors = browser.getCurrentPage().pageErrors();
                if (!errors.isEmpty()) {
                    throw new ValidationException(
                            "Expected no Playwright page errors but found %d".formatted(errors.size()));
                }
            }
            case VERIFY_PAGE_ERRORS_CONTAIN -> {
                String expected = LocatorResolver.resolve(text, context);
                boolean found = browser.getCurrentPage().pageErrors().stream()
                        .anyMatch(error -> error != null && error.contains(expected));
                if (!found) {
                    throw new ValidationException("No Playwright page error contains: " + expected);
                }
            }
            case PAGE_ERRORS_REPORT -> {
                String report = String.join(System.lineSeparator(), browser.getCurrentPage().pageErrors());
                if (variable != null) {
                    context.setVariable(variable, report);
                }
            }
        }
    }

    private List<ConsoleMessageRecord> messages(PlaywrightBrowser browser, TestContext context) {
        String resolved = filter == null ? null : LocatorResolver.resolve(filter, context);
        if (resolved == null || resolved.isBlank() || "all".equals(normalizeFilter(resolved))) {
            return browser.getConsoleCaptureRegistry().messages(browser.getCurrentPage());
        }
        Page.ConsoleMessagesOptions options = new Page.ConsoleMessagesOptions()
                .setFilter(toConsoleMessagesFilter(resolved));
        return browser.getCurrentPage().consoleMessages(options).stream()
                .map(ConsoleMessageRecord::from)
                .collect(Collectors.toList());
    }

    private static String normalizeFilter(String filter) {
        return filter.trim().toLowerCase(Locale.ROOT).replace('_', '-');
    }

    private static ConsoleMessagesFilter toConsoleMessagesFilter(String filter) {
        if ("since-navigation".equals(normalizeFilter(filter))) {
            return ConsoleMessagesFilter.SINCE_NAVIGATION;
        }
        throw new CitrusRuntimeException(
                "Unsupported Playwright console filter: " + filter + " - use one of all, since-navigation");
    }

    private String report(PlaywrightBrowser browser, TestContext context) {
        List<ConsoleMessageRecord> messages = messages(browser, context);
        return messages.stream().map(ConsoleMessageRecord::format).collect(Collectors.joining(System.lineSeparator()));
    }

    public Command getCommand() {
        return command;
    }

    public String getText() {
        return text;
    }

    public String getFilter() {
        return filter;
    }

    /**
     * Fluent builder for console capture, report, and verification commands.
     */
    public static class Builder extends AbstractPlaywrightAction.Builder<ConsoleAction, Builder> {
        private Command command;
        private String text;
        private String variable;
        private String filter;

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
         * Verifies that the current page produced no uncaught JavaScript errors.
         *
         * @return this builder
         */
        public Builder verifyNoPageErrors() {
            this.command = Command.VERIFY_NO_PAGE_ERRORS;
            return this;
        }

        /**
         * Verifies that at least one uncaught page error contains text.
         *
         * @param text expected text fragment
         * @return this builder
         */
        public Builder verifyPageErrorsContain(String text) {
            this.command = Command.VERIFY_PAGE_ERRORS_CONTAIN;
            this.text = text;
            return this;
        }

        /**
         * Formats uncaught page errors as a text report.
         *
         * @return this builder
         */
        public Builder pageErrors() {
            this.command = Command.PAGE_ERRORS_REPORT;
            return this;
        }

        /**
         * Restricts retrieval to messages since the last navigation, read directly
         * from the page instead of the capture registry. Unset (or {@code all})
         * reads the captured messages as before.
         *
         * @param filter filter name: {@code all} or {@code since-navigation}
         * @return this builder
         */
        public Builder filter(String filter) {
            this.filter = filter;
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
            if (command == Command.VERIFY_PAGE_ERRORS_CONTAIN && text == null) {
                throw new CitrusRuntimeException("Missing Playwright page error verification text");
            }
            return new ConsoleAction(this);
        }
    }
}
