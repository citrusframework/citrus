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

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.ValidationException;
import org.citrusframework.playwright.endpoint.PlaywrightBrowser;
import org.citrusframework.playwright.model.BoundingBoxResult;
import org.citrusframework.playwright.model.ConsoleMessageRecord;
import org.citrusframework.playwright.model.DownloadMetadata;
import org.citrusframework.playwright.model.LocatorSpec;
import org.citrusframework.playwright.model.NetworkRecord;
import org.citrusframework.playwright.model.SecretPatternRedactor;
import org.citrusframework.playwright.state.BrowserStorageStateReader;
import org.citrusframework.playwright.state.LocatorStateReader;
import org.citrusframework.playwright.state.ObservabilityStateReader;
import org.citrusframework.playwright.state.PageStateReader;
import org.citrusframework.playwright.util.LocatorResolver;

/**
 * Citrus action that verifies Playwright browser, page, locator, storage,
 * download, console, and network state.
 *
 * <p>The action keeps the original scalar locator and page assertions while
 * adding Phase 3 hardening checks for CSS, ARIA snapshots, frame content,
 * storage/cookie state, captured observability records, and download metadata.
 * Assertion failures include endpoint alias context and sanitized expected/
 * actual values to make diagnostics actionable without exposing secrets.</p>
 */
public class VerifyAction extends AbstractPlaywrightAction {

    /**
     * Broad verification target retained for fluent API compatibility.
     */
    public enum Target {
        LOCATOR,
        PAGE
    }

    /**
     * Supported verification checks.
     */
    public enum Check {
        PRESENT,
        ABSENT,
        VISIBLE,
        HIDDEN,
        ENABLED,
        DISABLED,
        TEXT,
        VALUE,
        ATTRIBUTE,
        COUNT,
        CSS_VALUE,
        CSS_CLASS,
        OPTION_TEXT,
        SELECTED_OPTION_TEXT,
        SELECTED_OPTION_VALUE,
        INNER_HTML_CONTAINS,
        OUTER_HTML_CONTAINS,
        BOUNDING_BOX,
        BOUNDS,
        ARIA_SNAPSHOT_CONTAINS,
        URL,
        TITLE,
        PAGE_COUNT,
        FRAME_CONTENT_CONTAINS,
        STORAGE_LOCAL,
        STORAGE_SESSION,
        COOKIE,
        CONSOLE_CONTAINS,
        NETWORK_URL_CONTAINS,
        DOWNLOAD_FILENAME,
        DOWNLOAD_PATH;

        /**
         * Page scoped aliases accepted by the XML and YAML DSLs.
         */
        private static final Map<String, Check> ALIASES = Map.of("PAGE_URL", URL, "PAGE_TITLE", TITLE);

        /**
         * Resolves a check from its DSL name. Names are case insensitive and may
         * use hyphens instead of underscores.
         *
         * @param name DSL check name
         * @return the matching check
         * @throws IllegalArgumentException when the name matches no check
         */
        public static Check fromName(String name) {
            String normalized = name.trim().toUpperCase(Locale.ENGLISH).replace('-', '_');
            Check alias = ALIASES.get(normalized);
            if (alias != null) {
                return alias;
            }

            try {
                return valueOf(normalized);
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Unsupported Playwright verify check: " + name, e);
            }
        }

        /**
         * Reports whether this verification requires a locator to be configured.
         *
         * @return true when the verification reads locator state
         */
        boolean requiresLocator() {
            return switch (this) {
                case URL, TITLE, PAGE_COUNT, FRAME_CONTENT_CONTAINS, STORAGE_LOCAL, STORAGE_SESSION, COOKIE,
                     CONSOLE_CONTAINS, NETWORK_URL_CONTAINS, DOWNLOAD_FILENAME, DOWNLOAD_PATH -> false;
                default -> true;
            };
        }
    }

    private final Target target;
    private final Check check;
    private final LocatorSpec locator;
    private final String expected;
    private final String attribute;
    private final Integer count;
    private final String property;
    private final String key;
    private final String frameSelector;
    private final Double x;
    private final Double y;
    private final Double width;
    private final Double height;

    /**
     * Creates a verify action from its fluent builder.
     *
     * @param builder configured builder
     */
    public VerifyAction(Builder builder) {
        super("verify", builder);
        this.target = builder.target;
        this.check = builder.check;
        this.locator = builder.locator;
        this.expected = builder.expected;
        this.attribute = builder.attribute;
        this.count = builder.count;
        this.property = builder.property;
        this.key = builder.key;
        this.frameSelector = builder.frameSelector;
        this.x = builder.x;
        this.y = builder.y;
        this.width = builder.width;
        this.height = builder.height;
    }

    /**
     * Returns the verification check performed by this action.
     *
     * @return verification check
     */
    public Check getCheck() {
        return check;
    }

    @Override
    protected void execute(PlaywrightBrowser browser, TestContext context) {
        Page page = browser.getCurrentPage();
        SecretPatternRedactor redactor = browser.createRedactor();
        LocatorStateReader locatorReader = new LocatorStateReader(redactor);
        PageStateReader pageReader = new PageStateReader(redactor);
        BrowserStorageStateReader storageReader = new BrowserStorageStateReader(redactor);
        ObservabilityStateReader observabilityReader = new ObservabilityStateReader(redactor);
        Locator element = check.requiresLocator() ? locator(browser, context) : null;

        switch (check) {
            case PRESENT -> assertTrue(browser, "locator present", true, element.count() > 0);
            case ABSENT -> assertEquals(browser, "locator count", 0, element.count());
            case VISIBLE -> assertTrue(browser, "locator visible", true, element.isVisible());
            case HIDDEN -> assertTrue(browser, "locator hidden", true, element.count() == 0 || !element.isVisible());
            case ENABLED -> assertTrue(browser, "locator enabled", true, element.isEnabled());
            case DISABLED -> assertTrue(browser, "locator disabled", true, element.isDisabled());
            case TEXT -> assertEquals(browser, "locator text", resolve(expected, context), element.textContent());
            case VALUE -> assertEquals(browser, "locator value", resolve(expected, context), element.inputValue());
            case ATTRIBUTE -> assertEquals(browser, "locator attribute " + attribute, resolve(expected, context), element.getAttribute(attribute));
            case COUNT -> assertEquals(browser, "locator count", count, element.count());
            case CSS_VALUE -> assertEquals(browser, "locator CSS value " + property, resolve(expected, context),
                    locatorReader.cssValue(element, resolve(property, context)));
            case CSS_CLASS -> assertListContains(browser, "locator CSS classes", resolve(expected, context), locatorReader.cssClasses(element));
            case OPTION_TEXT -> assertListContains(browser, "locator option texts", resolve(expected, context), locatorReader.optionTexts(element));
            case SELECTED_OPTION_TEXT -> assertEquals(browser, "locator selected option text", resolve(expected, context),
                    locatorReader.selectedOptionText(element));
            case SELECTED_OPTION_VALUE -> assertListContains(browser, "locator selected option values", resolve(expected, context),
                    locatorReader.selectedOptionValues(element));
            case INNER_HTML_CONTAINS -> assertContains(browser, "locator inner HTML", resolve(expected, context), locatorReader.innerHtml(element));
            case OUTER_HTML_CONTAINS -> assertContains(browser, "locator outer HTML", resolve(expected, context), locatorReader.outerHtml(element));
            case BOUNDING_BOX -> assertBoundingBox(browser, locatorReader.boundingBox(element));
            case BOUNDS -> assertBounds(browser, locatorReader.boundingBox(element));
            case ARIA_SNAPSHOT_CONTAINS -> assertContains(browser, "locator ARIA snapshot", resolve(expected, context),
                    locatorReader.ariaSnapshot(element));
            case URL -> assertEquals(browser, "page URL", resolve(expected, context), page.url());
            case TITLE -> assertEquals(browser, "page title", resolve(expected, context), page.title());
            case PAGE_COUNT -> assertEquals(browser, "page count", count, pageReader.pageCount(browser));
            case FRAME_CONTENT_CONTAINS -> assertContains(browser, "frame content", resolve(expected, context),
                    pageReader.frameContent(page, resolve(frameSelector, context)));
            case STORAGE_LOCAL -> assertEquals(browser, "local storage " + key, resolve(expected, context),
                    page.localStorage().getItem(resolve(key, context)), resolve(key, context));
            case STORAGE_SESSION -> assertEquals(browser, "session storage " + key, resolve(expected, context),
                    page.sessionStorage().getItem(resolve(key, context)), resolve(key, context));
            case COOKIE -> assertEquals(browser, "cookie " + key, resolve(expected, context),
                    storageReader.rawCookie(browser.getCurrentContext(), resolve(key, context)).map(cookie -> cookie.value).orElse(null),
                    resolve(key, context));
            case CONSOLE_CONTAINS -> assertContains(browser, "console messages", resolve(expected, context),
                    observabilityReader.consoleMessages(browser, page).stream().map(ConsoleMessageRecord::text).toList());
            case NETWORK_URL_CONTAINS -> assertContains(browser, "network URLs", resolve(expected, context),
                    observabilityReader.networkRecords(browser, page).stream().map(NetworkRecord::url).toList());
            case DOWNLOAD_FILENAME -> assertEquals(browser, "download filename", resolve(expected, context),
                    latestDownload(browser).map(DownloadMetadata::suggestedFilename).orElse(null));
            case DOWNLOAD_PATH -> assertEquals(browser, "download path", resolve(expected, context),
                    latestDownload(browser).map(DownloadMetadata::path).orElse(null));
        }
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

    private void assertTrue(PlaywrightBrowser browser, String label, Object expectedValue, boolean actualValue) {
        if (!actualValue) {
            fail(browser, label, expectedValue, actualValue, null);
        }
    }

    private void assertEquals(PlaywrightBrowser browser, String label, Object expectedValue, Object actualValue) {
        assertEquals(browser, label, expectedValue, actualValue, null);
    }

    private void assertEquals(PlaywrightBrowser browser, String label, Object expectedValue, Object actualValue, String secretName) {
        if (!Objects.equals(expectedValue, actualValue)) {
            fail(browser, label, expectedValue, actualValue, secretName);
        }
    }

    private void assertContains(PlaywrightBrowser browser, String label, String expectedValue, String actualValue) {
        if (actualValue == null || expectedValue == null || !actualValue.contains(expectedValue)) {
            fail(browser, label, expectedValue, actualValue, null);
        }
    }

    private void assertContains(PlaywrightBrowser browser, String label, String expectedValue, List<String> actualValues) {
        if (expectedValue == null || actualValues.stream().noneMatch(value -> value != null && value.contains(expectedValue))) {
            fail(browser, label, expectedValue, actualValues, null);
        }
    }

    private void assertListContains(PlaywrightBrowser browser, String label, String expectedValue, List<String> actualValues) {
        if (expectedValue == null || !actualValues.contains(expectedValue)) {
            fail(browser, label, expectedValue, actualValues, null);
        }
    }

    private void assertBoundingBox(PlaywrightBrowser browser, BoundingBoxResult actualValue) {
        BoundingBoxResult expectedValue = new BoundingBoxResult(x, y, width, height);
        if (!Objects.equals(expectedValue, actualValue)) {
            fail(browser, "locator bounding box", expectedValue, actualValue, null);
        }
    }

    private void assertBounds(PlaywrightBrowser browser, BoundingBoxResult actualValue) {
        if (actualValue == null || !Objects.equals(width, actualValue.width()) || !Objects.equals(height, actualValue.height())) {
            fail(browser, "locator bounds", "width=%s height=%s".formatted(width, height), actualValue, null);
        }
    }

    private Optional<DownloadMetadata> latestDownload(PlaywrightBrowser browser) {
        return browser.getLatestDownloadMetadata();
    }

    private String resolve(String value, TestContext context) {
        return LocatorResolver.resolve(value, context);
    }

    private void fail(PlaywrightBrowser browser, String label, Object expectedValue, Object actualValue, String secretName) {
        throw new ValidationException(
                "Playwright verification failed: check=%s, label=%s, context=%s, page=%s, locator=%s, expected='%s', actual='%s'"
                        .formatted(
                                check,
                                label,
                                browser.getCurrentContextAlias().orElse("<none>"),
                                browser.getCurrentPageAlias().orElse("<none>"),
                                describeLocator(),
                                sanitize(browser, secretName, expectedValue),
                                sanitize(browser, secretName, actualValue)));
    }

    private String sanitize(PlaywrightBrowser browser, String secretName, Object value) {
        if (value == null) {
            return "<null>";
        }
        String text = String.valueOf(value);
        SecretPatternRedactor redactor = browser.createRedactor();
        return secretName == null ? redactor.sanitizeText(text) : redactor.sanitizeNamedValue(secretName, text);
    }

    private String describeLocator() {
        if (locator == null) {
            return "<none>";
        }
        StringBuilder description = new StringBuilder(locator.getType().name().toLowerCase()).append(":");
        description.append(locator.getSelector() == null ? "<raw>" : locator.getSelector());
        if (locator.getName() != null) {
            description.append("[name=").append(locator.getName()).append("]");
        }
        if (locator.getNth() != null) {
            description.append("[nth=").append(locator.getNth()).append("]");
        }
        if (locator.isFirst()) {
            description.append("[first]");
        }
        if (locator.isLast()) {
            description.append("[last]");
        }
        return description.toString();
    }

    /**
     * Fluent builder for Playwright verification actions.
     */
    public static class Builder extends ElementActionBuilder<VerifyAction, Builder> {
        private Target target = Target.LOCATOR;
        private Check check = Check.VISIBLE;
        private String expected;
        private String attribute;
        private Integer count;
        private String property;
        private String key;
        private String frameSelector;
        private Double x;
        private Double y;
        private Double width;
        private Double height;

        /**
         * Sets the verification check, deriving the target scope from it.
         *
         * @param check verification check
         * @return this builder
         */
        public Builder check(Check check) {
            this.check = check;
            this.target = check.requiresLocator() ? Target.LOCATOR : Target.PAGE;
            return this;
        }

        /**
         * Sets the expected value evaluated by the configured check.
         *
         * @param expected expected value
         * @return this builder
         */
        public Builder expected(String expected) {
            this.expected = expected;
            return this;
        }

        /**
         * Sets the attribute name read by the attribute check.
         *
         * @param attribute attribute name
         * @return this builder
         */
        public Builder attributeName(String attribute) {
            this.attribute = attribute;
            return this;
        }

        /**
         * Sets the expected element or page count.
         *
         * @param count expected count
         * @return this builder
         */
        public Builder expectedCount(Integer count) {
            this.count = count;
            return this;
        }

        /**
         * Sets the CSS property name read by the CSS value check.
         *
         * @param property CSS property name
         * @return this builder
         */
        public Builder cssProperty(String property) {
            this.property = property;
            return this;
        }

        /**
         * Sets the storage or cookie key read by the state checks.
         *
         * @param key storage or cookie key
         * @return this builder
         */
        public Builder stateKey(String key) {
            this.key = key;
            return this;
        }

        /**
         * Sets the frame selector read by the frame content check.
         *
         * @param frameSelector frame selector
         * @return this builder
         */
        public Builder frameSelector(String frameSelector) {
            this.frameSelector = frameSelector;
            return this;
        }

        /**
         * Sets the expected left coordinate of the bounding box check.
         *
         * @param x expected left coordinate
         * @return this builder
         */
        public Builder x(Double x) {
            this.x = x;
            return this;
        }

        /**
         * Sets the expected top coordinate of the bounding box check.
         *
         * @param y expected top coordinate
         * @return this builder
         */
        public Builder y(Double y) {
            this.y = y;
            return this;
        }

        /**
         * Sets the expected width of the bounding box and bounds checks.
         *
         * @param width expected width
         * @return this builder
         */
        public Builder width(Double width) {
            this.width = width;
            return this;
        }

        /**
         * Sets the expected height of the bounding box and bounds checks.
         *
         * @param height expected height
         * @return this builder
         */
        public Builder height(Double height) {
            this.height = height;
            return this;
        }

        /**
         * Targets a page-level verification.
         *
         * @return this builder
         */
        public Builder page() {
            this.target = Target.PAGE;
            return this;
        }

        /**
         * Verifies that the locator resolves to at least one element.
         *
         * @return this builder
         */
        public Builder present() {
            this.check = Check.PRESENT;
            return this;
        }

        /**
         * Verifies that the locator resolves to no elements.
         *
         * @return this builder
         */
        public Builder absent() {
            this.check = Check.ABSENT;
            return this;
        }

        /**
         * Verifies that the locator is visible.
         *
         * @return this builder
         */
        public Builder visible() {
            this.check = Check.VISIBLE;
            return this;
        }

        /**
         * Verifies that the locator is hidden or absent.
         *
         * @return this builder
         */
        public Builder hidden() {
            this.check = Check.HIDDEN;
            return this;
        }

        /**
         * Verifies that the locator is enabled.
         *
         * @return this builder
         */
        public Builder enabled() {
            this.check = Check.ENABLED;
            return this;
        }

        /**
         * Verifies that the locator is disabled.
         *
         * @return this builder
         */
        public Builder disabled() {
            this.check = Check.DISABLED;
            return this;
        }

        /**
         * Verifies the locator text content.
         *
         * @param expected expected text
         * @return this builder
         */
        public Builder text(String expected) {
            this.check = Check.TEXT;
            this.expected = expected;
            return this;
        }

        /**
         * Verifies the locator input value.
         *
         * @param expected expected value
         * @return this builder
         */
        public Builder value(String expected) {
            this.check = Check.VALUE;
            this.expected = expected;
            return this;
        }

        /**
         * Verifies a locator attribute value.
         *
         * @param attribute attribute name
         * @param expected expected attribute value
         * @return this builder
         */
        public Builder attribute(String attribute, String expected) {
            this.check = Check.ATTRIBUTE;
            this.attribute = attribute;
            this.expected = expected;
            return this;
        }

        /**
         * Verifies the locator match count.
         *
         * @param count expected count
         * @return this builder
         */
        public Builder count(int count) {
            this.check = Check.COUNT;
            this.count = count;
            return this;
        }

        /**
         * Verifies a computed CSS property value for the locator.
         *
         * @param property CSS property name
         * @param expected expected computed value
         * @return this builder
         */
        public Builder cssValue(String property, String expected) {
            this.check = Check.CSS_VALUE;
            this.property = property;
            this.expected = expected;
            return this;
        }

        /**
         * Verifies that the locator has a CSS class.
         *
         * @param expected expected class name
         * @return this builder
         */
        public Builder cssClass(String expected) {
            this.check = Check.CSS_CLASS;
            this.expected = expected;
            return this;
        }

        /**
         * Verifies that a select locator has an option with matching text.
         *
         * @param expected expected option text
         * @return this builder
         */
        public Builder optionText(String expected) {
            this.check = Check.OPTION_TEXT;
            this.expected = expected;
            return this;
        }

        /**
         * Verifies selected option text for a select locator.
         *
         * @param expected expected selected option text
         * @return this builder
         */
        public Builder selectedOptionText(String expected) {
            this.check = Check.SELECTED_OPTION_TEXT;
            this.expected = expected;
            return this;
        }

        /**
         * Verifies that a select locator has a selected option value.
         *
         * @param expected expected selected option value
         * @return this builder
         */
        public Builder selectedOptionValue(String expected) {
            this.check = Check.SELECTED_OPTION_VALUE;
            this.expected = expected;
            return this;
        }

        /**
         * Verifies that locator inner HTML contains text.
         *
         * @param expected expected HTML fragment
         * @return this builder
         */
        public Builder innerHtmlContains(String expected) {
            this.check = Check.INNER_HTML_CONTAINS;
            this.expected = expected;
            return this;
        }

        /**
         * Verifies that locator outer HTML contains text.
         *
         * @param expected expected HTML fragment
         * @return this builder
         */
        public Builder outerHtmlContains(String expected) {
            this.check = Check.OUTER_HTML_CONTAINS;
            this.expected = expected;
            return this;
        }

        /**
         * Verifies the full locator bounding box.
         *
         * @param x expected left coordinate
         * @param y expected top coordinate
         * @param width expected width
         * @param height expected height
         * @return this builder
         */
        public Builder boundingBox(double x, double y, double width, double height) {
            this.check = Check.BOUNDING_BOX;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            return this;
        }

        /**
         * Verifies only locator width and height.
         *
         * @param width expected width
         * @param height expected height
         * @return this builder
         */
        public Builder bounds(double width, double height) {
            this.check = Check.BOUNDS;
            this.width = width;
            this.height = height;
            return this;
        }

        /**
         * Verifies that Playwright's ARIA snapshot contains text.
         *
         * @param expected expected ARIA fragment
         * @return this builder
         */
        public Builder ariaSnapshotContains(String expected) {
            this.check = Check.ARIA_SNAPSHOT_CONTAINS;
            this.expected = expected;
            return this;
        }

        /**
         * Verifies the current page URL.
         *
         * @param expected expected URL
         * @return this builder
         */
        public Builder url(String expected) {
            this.check = Check.URL;
            this.expected = expected;
            return page();
        }

        /**
         * Verifies the current page title.
         *
         * @param expected expected title
         * @return this builder
         */
        public Builder title(String expected) {
            this.check = Check.TITLE;
            this.expected = expected;
            return page();
        }

        /**
         * Verifies the number of pages registered with the browser endpoint.
         *
         * @param count expected page count
         * @return this builder
         */
        public Builder pageCount(int count) {
            this.check = Check.PAGE_COUNT;
            this.count = count;
            return page();
        }

        /**
         * Verifies that a frame body contains text.
         *
         * @param frameSelector frame selector
         * @param expected expected content fragment
         * @return this builder
         */
        public Builder frameContentContains(String frameSelector, String expected) {
            this.target = Target.PAGE;
            this.check = Check.FRAME_CONTENT_CONTAINS;
            this.frameSelector = frameSelector;
            this.expected = expected;
            return this;
        }

        /**
         * Verifies a local-storage value on the current page.
         *
         * @param key storage key
         * @param expected expected value
         * @return this builder
         */
        public Builder storageLocal(String key, String expected) {
            this.target = Target.PAGE;
            this.check = Check.STORAGE_LOCAL;
            this.key = key;
            this.expected = expected;
            return this;
        }

        /**
         * Verifies a session-storage value on the current page.
         *
         * @param key storage key
         * @param expected expected value
         * @return this builder
         */
        public Builder storageSession(String key, String expected) {
            this.target = Target.PAGE;
            this.check = Check.STORAGE_SESSION;
            this.key = key;
            this.expected = expected;
            return this;
        }

        /**
         * Verifies a cookie value in the current browser context.
         *
         * @param name cookie name
         * @param expected expected cookie value
         * @return this builder
         */
        public Builder cookie(String name, String expected) {
            this.target = Target.PAGE;
            this.check = Check.COOKIE;
            this.key = name;
            this.expected = expected;
            return this;
        }

        /**
         * Verifies that captured console messages contain text.
         *
         * @param expected expected console fragment
         * @return this builder
         */
        public Builder consoleContains(String expected) {
            this.target = Target.PAGE;
            this.check = Check.CONSOLE_CONTAINS;
            this.expected = expected;
            return this;
        }

        /**
         * Verifies that captured network URLs contain text.
         *
         * @param expected expected URL fragment
         * @return this builder
         */
        public Builder networkUrlContains(String expected) {
            this.target = Target.PAGE;
            this.check = Check.NETWORK_URL_CONTAINS;
            this.expected = expected;
            return this;
        }

        /**
         * Verifies the latest download suggested filename.
         *
         * @param expected expected filename
         * @return this builder
         */
        public Builder downloadFilename(String expected) {
            this.target = Target.PAGE;
            this.check = Check.DOWNLOAD_FILENAME;
            this.expected = expected;
            return this;
        }

        /**
         * Verifies the latest download path.
         *
         * @param expected expected path
         * @return this builder
         */
        public Builder downloadPath(String expected) {
            this.target = Target.PAGE;
            this.check = Check.DOWNLOAD_PATH;
            this.expected = expected;
            return this;
        }

        @Override
        public VerifyAction build() {
            if (check.requiresLocator()) {
                requireLocator();
            }
            if (check == Check.ATTRIBUTE && attribute == null) {
                throw new ValidationException("Missing Playwright attribute name");
            }
            if ((check == Check.COUNT || check == Check.PAGE_COUNT) && count == null) {
                throw new ValidationException("Missing Playwright expected count");
            }
            if (check == Check.CSS_VALUE && property == null) {
                throw new ValidationException("Missing Playwright CSS property");
            }
            if (check == Check.FRAME_CONTENT_CONTAINS && frameSelector == null) {
                throw new ValidationException("Missing Playwright frame selector");
            }
            if ((check == Check.STORAGE_LOCAL || check == Check.STORAGE_SESSION || check == Check.COOKIE) && key == null) {
                throw new ValidationException("Missing Playwright state key");
            }
            if (check == Check.BOUNDING_BOX && (x == null || y == null || width == null || height == null)) {
                throw new ValidationException("Missing Playwright bounding box values");
            }
            if (check == Check.BOUNDS && (width == null || height == null)) {
                throw new ValidationException("Missing Playwright bounds values");
            }
            return new VerifyAction(this);
        }
    }
}
