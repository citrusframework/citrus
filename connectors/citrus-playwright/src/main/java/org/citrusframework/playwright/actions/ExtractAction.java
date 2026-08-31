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

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.playwright.endpoint.PlaywrightBrowser;
import org.citrusframework.playwright.model.SecretPatternRedactor;
import org.citrusframework.playwright.state.BrowserStorageStateReader;
import org.citrusframework.playwright.state.LocatorStateReader;
import org.citrusframework.playwright.state.ObservabilityStateReader;
import org.citrusframework.playwright.state.PageStateReader;
import org.citrusframework.playwright.util.LocatorResolver;
import org.citrusframework.util.StringUtils;

/**
 * Citrus action that extracts Playwright browser, page, locator, storage, and
 * observability state into a Citrus test variable.
 *
 * <p>The action preserves the original scalar extraction operations and adds
 * structured Phase 3 values such as bounding boxes, text lists, console
 * messages, network records, download metadata, frame content, and storage
 * values. Diagnostic values pass through the endpoint redactor before they are
 * stored in the test context.</p>
 */
public class ExtractAction extends AbstractPlaywrightAction {

    /**
     * Supported extraction source values.
     */
    public enum Value {
        TEXT,
        VALUE,
        ATTRIBUTE,
        COUNT,
        ALL_TEXT_CONTENTS,
        ALL_INNER_TEXTS,
        INNER_HTML,
        OUTER_HTML,
        BOUNDING_BOX,
        CSS_CLASSES,
        CSS_VALUE,
        OPTION_TEXTS,
        SELECTED_OPTION_TEXT,
        SELECTED_OPTION_VALUES,
        ARIA_SNAPSHOT,
        URL,
        TITLE,
        PAGE_COUNT,
        FRAME_CONTENT,
        STORAGE_LOCAL,
        STORAGE_SESSION,
        COOKIE,
        CONSOLE_MESSAGES,
        NETWORK_RECORDS,
        DOWNLOAD_METADATA;

        /**
         * Reports whether this extraction requires a locator to be configured.
         *
         * @return true when the extraction reads locator state
         */
        boolean requiresLocator() {
            return switch (this) {
                case URL, TITLE, PAGE_COUNT, FRAME_CONTENT, STORAGE_LOCAL, STORAGE_SESSION, COOKIE,
                     CONSOLE_MESSAGES, NETWORK_RECORDS, DOWNLOAD_METADATA -> false;
                default -> true;
            };
        }
    }

    private final Value value;
    private final org.citrusframework.playwright.model.LocatorSpec locator;
    private final String attribute;
    private final String variable;
    private final String property;
    private final String key;
    private final String frameSelector;

    /**
     * Returns the extraction value read by this action.
     *
     * @return extraction value
     */
    public Value getValue() {
        return value;
    }

    /**
     * Returns the name of the variable receiving the extracted value.
     *
     * @return target variable name
     */
    public String getVariable() {
        return variable;
    }

    /**
     * Creates an extract action from its fluent builder.
     *
     * @param builder configured builder
     */
    public ExtractAction(Builder builder) {
        super("extract", builder);
        this.value = builder.value;
        this.locator = builder.locator;
        this.attribute = builder.attribute;
        this.variable = builder.variable;
        this.property = builder.property;
        this.key = builder.key;
        this.frameSelector = builder.frameSelector;
    }

    @Override
    protected void execute(PlaywrightBrowser browser, TestContext context) {
        SecretPatternRedactor redactor = browser.createRedactor();
        LocatorStateReader locatorReader = new LocatorStateReader(redactor);
        PageStateReader pageReader = new PageStateReader(redactor);
        BrowserStorageStateReader storageReader = new BrowserStorageStateReader(redactor);
        ObservabilityStateReader observabilityReader = new ObservabilityStateReader(redactor);
        Page page = browser.getCurrentPage();

        Object result = switch (value) {
            case URL -> page.url();
            case TITLE -> page.title();
            case TEXT -> locator(browser, context).textContent();
            case VALUE -> locator(browser, context).inputValue();
            case ATTRIBUTE -> locator(browser, context).getAttribute(attribute);
            case COUNT -> locator(browser, context).count();
            case ALL_TEXT_CONTENTS -> locatorReader.allTextContents(locator(browser, context));
            case ALL_INNER_TEXTS -> locatorReader.allInnerTexts(locator(browser, context));
            case INNER_HTML -> locatorReader.innerHtml(locator(browser, context));
            case OUTER_HTML -> locatorReader.outerHtml(locator(browser, context));
            case BOUNDING_BOX -> locatorReader.boundingBox(locator(browser, context));
            case CSS_CLASSES -> locatorReader.cssClasses(locator(browser, context));
            case CSS_VALUE -> locatorReader.cssValue(locator(browser, context), LocatorResolver.resolve(property, context));
            case OPTION_TEXTS -> locatorReader.optionTexts(locator(browser, context));
            case SELECTED_OPTION_TEXT -> locatorReader.selectedOptionText(locator(browser, context));
            case SELECTED_OPTION_VALUES -> locatorReader.selectedOptionValues(locator(browser, context));
            case ARIA_SNAPSHOT -> locatorReader.ariaSnapshot(locator(browser, context));
            case PAGE_COUNT -> pageReader.pageCount(browser);
            case FRAME_CONTENT -> pageReader.frameContent(page, LocatorResolver.resolve(frameSelector, context));
            case STORAGE_LOCAL -> storageReader.localStorage(page, LocatorResolver.resolve(key, context));
            case STORAGE_SESSION -> storageReader.sessionStorage(page, LocatorResolver.resolve(key, context));
            case COOKIE -> storageReader.cookie(browser.getCurrentContext(), LocatorResolver.resolve(key, context)).orElse("");
            case CONSOLE_MESSAGES -> observabilityReader.consoleMessages(browser, page);
            case NETWORK_RECORDS -> observabilityReader.networkRecords(browser, page);
            case DOWNLOAD_METADATA -> browser.getLatestDownloadMetadata().orElse(null);
        };
        context.setVariable(variable, result == null ? "" : result);
    }

    /**
     * Resolves the configured locator against the current browser page.
     *
     * @param browser active endpoint
     * @param context Citrus test context
     * @return Playwright locator
     */
    private Locator locator(PlaywrightBrowser browser, TestContext context) {
        return LocatorResolver.resolve(browser.getCurrentPage(), locator, context);
    }

    /**
     * Fluent builder for Playwright extraction actions.
     */
    public static class Builder extends ElementActionBuilder<ExtractAction, Builder> {
        private Value value = Value.TEXT;
        private String attribute;
        private String variable;
        private String property;
        private String key;
        private String frameSelector;

        /**
         * Extracts the current locator text content.
         *
         * @return this builder
         */
        public Builder text() {
            this.value = Value.TEXT;
            return this;
        }

        /**
         * Extracts the current locator input value.
         *
         * @return this builder
         */
        public Builder value() {
            this.value = Value.VALUE;
            return this;
        }

        /**
         * Extracts an attribute from the current locator.
         *
         * @param attribute attribute name
         * @return this builder
         */
        public Builder attribute(String attribute) {
            this.value = Value.ATTRIBUTE;
            this.attribute = attribute;
            return this;
        }

        /**
         * Extracts the current locator match count.
         *
         * @return this builder
         */
        public Builder count() {
            this.value = Value.COUNT;
            return this;
        }

        /**
         * Extracts all text contents matched by the current locator.
         *
         * @return this builder
         */
        public Builder allTextContents() {
            this.value = Value.ALL_TEXT_CONTENTS;
            return this;
        }

        /**
         * Extracts all inner text values matched by the current locator.
         *
         * @return this builder
         */
        public Builder allInnerTexts() {
            this.value = Value.ALL_INNER_TEXTS;
            return this;
        }

        /**
         * Extracts the current locator inner HTML.
         *
         * @return this builder
         */
        public Builder innerHtml() {
            this.value = Value.INNER_HTML;
            return this;
        }

        /**
         * Extracts the current locator outer HTML.
         *
         * @return this builder
         */
        public Builder outerHtml() {
            this.value = Value.OUTER_HTML;
            return this;
        }

        /**
         * Extracts the current locator bounding box.
         *
         * @return this builder
         */
        public Builder boundingBox() {
            this.value = Value.BOUNDING_BOX;
            return this;
        }

        /**
         * Extracts the current locator CSS class list.
         *
         * @return this builder
         */
        public Builder cssClasses() {
            this.value = Value.CSS_CLASSES;
            return this;
        }

        /**
         * Extracts a computed CSS property value from the current locator.
         *
         * @param property CSS property name
         * @return this builder
         */
        public Builder cssValue(String property) {
            this.value = Value.CSS_VALUE;
            this.property = property;
            return this;
        }

        /**
         * Extracts all option text values from a select locator.
         *
         * @return this builder
         */
        public Builder optionTexts() {
            this.value = Value.OPTION_TEXTS;
            return this;
        }

        /**
         * Extracts the selected option text from a select locator.
         *
         * @return this builder
         */
        public Builder selectedOptionText() {
            this.value = Value.SELECTED_OPTION_TEXT;
            return this;
        }

        /**
         * Extracts selected option values from a select locator.
         *
         * @return this builder
         */
        public Builder selectedOptionValues() {
            this.value = Value.SELECTED_OPTION_VALUES;
            return this;
        }

        /**
         * Extracts Playwright's ARIA snapshot for the current locator.
         *
         * @return this builder
         */
        public Builder ariaSnapshot() {
            this.value = Value.ARIA_SNAPSHOT;
            return this;
        }

        /**
         * Extracts the current page URL.
         *
         * @return this builder
         */
        public Builder url() {
            this.value = Value.URL;
            return this;
        }

        /**
         * Extracts the current page title.
         *
         * @return this builder
         */
        public Builder title() {
            this.value = Value.TITLE;
            return this;
        }

        /**
         * Extracts the number of pages registered with the browser endpoint.
         *
         * @return this builder
         */
        public Builder pageCount() {
            this.value = Value.PAGE_COUNT;
            return this;
        }

        /**
         * Extracts the body inner HTML from a frame selected on the current page.
         *
         * @param frameSelector frame selector
         * @return this builder
         */
        public Builder frameContent(String frameSelector) {
            this.value = Value.FRAME_CONTENT;
            this.frameSelector = frameSelector;
            return this;
        }

        /**
         * Extracts a local-storage value from the current page.
         *
         * @param key storage key
         * @return this builder
         */
        public Builder storageLocal(String key) {
            this.value = Value.STORAGE_LOCAL;
            this.key = key;
            return this;
        }

        /**
         * Extracts a session-storage value from the current page.
         *
         * @param key storage key
         * @return this builder
         */
        public Builder storageSession(String key) {
            this.value = Value.STORAGE_SESSION;
            this.key = key;
            return this;
        }

        /**
         * Extracts a cookie value from the current browser context.
         *
         * @param name cookie name
         * @return this builder
         */
        public Builder cookie(String name) {
            this.value = Value.COOKIE;
            this.key = name;
            return this;
        }

        /**
         * Extracts structured console message records captured for the current page.
         *
         * @return this builder
         */
        public Builder consoleMessages() {
            this.value = Value.CONSOLE_MESSAGES;
            return this;
        }

        /**
         * Extracts structured network records captured for the current page.
         *
         * @return this builder
         */
        public Builder networkRecords() {
            this.value = Value.NETWORK_RECORDS;
            return this;
        }

        /**
         * Extracts metadata for the latest download observed by the endpoint.
         *
         * @return this builder
         */
        public Builder downloadMetadata() {
            this.value = Value.DOWNLOAD_METADATA;
            return this;
        }

        /**
         * Stores the extracted value in a Citrus variable.
         *
         * @param variable variable name
         * @return this builder
         */
        public Builder variable(String variable) {
            this.variable = variable;
            return this;
        }

        @Override
        public ExtractAction build() {
            if (value.requiresLocator()) {
                requireLocator();
            }
            if (value == Value.CSS_VALUE && !StringUtils.hasText(property)) {
                throw new CitrusRuntimeException("Missing Playwright CSS property - call cssValue(...) before building extract action");
            }
            if ((value == Value.FRAME_CONTENT) && !StringUtils.hasText(frameSelector)) {
                throw new CitrusRuntimeException("Missing Playwright frame selector - call frameContent(...) before building extract action");
            }
            if ((value == Value.STORAGE_LOCAL || value == Value.STORAGE_SESSION || value == Value.COOKIE)
                    && !StringUtils.hasText(key)) {
                throw new CitrusRuntimeException("Missing Playwright state key - call storageLocal(...), storageSession(...), or cookie(...) before building extract action");
            }
            if (!StringUtils.hasText(variable)) {
                throw new CitrusRuntimeException("Missing Playwright extraction variable - call variable(...) before building extract action");
            }
            return new ExtractAction(this);
        }
    }
}
