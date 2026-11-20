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

import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.Cookie;

import java.util.List;
import java.util.Optional;

import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.exceptions.ValidationException;
import org.citrusframework.playwright.endpoint.PlaywrightBrowser;
import org.citrusframework.playwright.model.CookieSpec;
import org.citrusframework.playwright.util.LocatorResolver;
import org.citrusframework.util.StringUtils;

/**
 * Citrus action for browser cookie operations on the current context.
 *
 * <p>Cookie values, names, and URLs are resolved through Citrus variables. Add
 * commands use the configured action URL or current page URL as a default cookie
 * URL when no explicit URL or domain is configured on the cookie specification.
 * Read and verify commands filter context cookies with RFC 6265 URL matching
 * when an action URL is configured.</p>
 */
public class CookieAction extends AbstractPlaywrightAction {

    public enum Command {
        ADD,
        CLEAR,
        READ,
        VERIFY
    }

    private final Command command;
    private final CookieSpec cookie;
    private final String name;
    private final String domain;
    private final String path;
    private final String expectedValue;
    private final String url;
    private final String variable;

    public CookieAction(Builder builder) {
        super("cookies", builder);
        this.command = builder.command;
        this.cookie = builder.cookie;
        this.name = builder.name;
        this.domain = builder.domain;
        this.path = builder.path;
        this.expectedValue = builder.expectedValue;
        this.url = builder.url;
        this.variable = builder.variable;
    }

    @Override
    protected void execute(PlaywrightBrowser browser, TestContext context) {
        switch (command) {
            case ADD -> {
                BrowserContext currentContext = browser.getCurrentContext();
                String defaultUrl = StringUtils.hasText(url)
                        ? LocatorResolver.resolve(url, context)
                        : browser.currentPageIfAvailable().map(Page::url).orElse(null);
                currentContext.addCookies(List.of(cookie.toCookie(context, defaultUrl)));
            }
            case CLEAR -> clearCookies(browser.getCurrentContext(), context);
            case READ -> {
                Cookie actual = findCookie(browser, context);
                context.setVariable(variable, actual.value);
            }
            case VERIFY -> {
                Cookie actual = findCookie(browser, context);
                String expected = LocatorResolver.resolve(expectedValue, context);
                if (!expected.equals(actual.value)) {
                    throw new ValidationException("Expected cookie '%s' to have value '%s' but got '%s'"
                            .formatted(name, expected, actual.value));
                }
            }
        }
    }

    private void clearCookies(BrowserContext currentContext, TestContext context) {
        if (name == null && !StringUtils.hasText(domain) && !StringUtils.hasText(path)) {
            currentContext.clearCookies();
            return;
        }
        BrowserContext.ClearCookiesOptions options = new BrowserContext.ClearCookiesOptions();
        if (name != null) {
            options.setName(LocatorResolver.resolve(name, context));
        }
        if (StringUtils.hasText(domain)) {
            options.setDomain(LocatorResolver.resolve(domain, context));
        }
        if (StringUtils.hasText(path)) {
            options.setPath(LocatorResolver.resolve(path, context));
        }
        currentContext.clearCookies(options);
    }

    private Cookie findCookie(PlaywrightBrowser browser, TestContext context) {
        String resolvedName = LocatorResolver.resolve(name, context);
        if (StringUtils.hasText(url)) {
            String resolvedUrl = LocatorResolver.resolve(url, context);
            Optional<Cookie> cookie = browser.getCurrentContext().cookies(resolvedUrl).stream()
                    .filter(candidate -> resolvedName.equals(candidate.name))
                    .findFirst();
            return cookie.orElseThrow(() -> new CitrusRuntimeException(
                    "No Playwright cookie found with name '%s' for URL: %s".formatted(resolvedName, resolvedUrl)));
        }
        Optional<Cookie> cookie = browser.getCurrentContext().cookies().stream()
                .filter(candidate -> resolvedName.equals(candidate.name))
                .findFirst();
        return cookie.orElseThrow(() -> new CitrusRuntimeException("No Playwright cookie found with name: " + resolvedName));
    }

    public Command getCommand() {
        return command;
    }

    public CookieSpec getCookie() {
        return cookie;
    }

    public String getCookieName() {
        return name;
    }

    public String getDomain() {
        return domain;
    }

    public String getPath() {
        return path;
    }

    public String getUrl() {
        return url;
    }

    public String getVariable() {
        return variable;
    }

    /**
     * Fluent builder for cookie add, clear, read, and verification commands.
     */
    public static class Builder extends AbstractPlaywrightAction.Builder<CookieAction, Builder> {
        private Command command;
        private CookieSpec cookie;
        private String name;
        private String domain;
        private String path;
        private String expectedValue;
        private String url;
        private String variable;

        /**
         * Adds a fully configured cookie specification.
         *
         * @param cookie cookie specification
         * @return this builder
         */
        public Builder add(CookieSpec cookie) {
            this.command = Command.ADD;
            this.cookie = cookie;
            return this;
        }

        /**
         * Adds a simple name/value cookie.
         *
         * @param name cookie name
         * @param value cookie value
         * @return this builder
         */
        public Builder add(String name, String value) {
            return add(CookieSpec.cookie(name, value));
        }

        /**
         * Clears cookies from the current context, optionally narrowed with
         * {@link #name(String)}, {@link #domain(String)}, and/or {@link #path(String)}.
         *
         * @return this builder
         */
        public Builder clear() {
            this.command = Command.CLEAR;
            return this;
        }

        /**
         * Filters {@link #clear()} by cookie name, or sets the target cookie name.
         *
         * @param name cookie name
         * @return this builder
         */
        public Builder name(String name) {
            this.name = name;
            return this;
        }

        /**
         * Filters {@link #clear()} by cookie domain.
         *
         * @param domain cookie domain filter
         * @return this builder
         */
        public Builder domain(String domain) {
            this.domain = domain;
            return this;
        }

        /**
         * Filters {@link #clear()} by cookie path.
         *
         * @param path cookie path filter
         * @return this builder
         */
        public Builder path(String path) {
            this.path = path;
            return this;
        }

        /**
         * Reads a cookie by name.
         *
         * @param name cookie name
         * @return this builder
         */
        public Builder read(String name) {
            this.command = Command.READ;
            this.name = name;
            return this;
        }

        /**
         * Verifies a cookie value by name.
         *
         * @param name cookie name
         * @param value expected value
         * @return this builder
         */
        public Builder verify(String name, String value) {
            this.command = Command.VERIFY;
            this.name = name;
            this.expectedValue = value;
            return this;
        }

        /**
         * Associates a URL with cookie lookup (RFC 6265 domain/path filter for read and verify)
         * or provides a fallback cookie URL for add.
         *
         * @param url target URL
         * @return this builder
         */
        public Builder url(String url) {
            this.url = url;
            return this;
        }

        /**
         * Stores the read cookie value in a Citrus variable.
         *
         * @param variable variable name
         * @return this builder
         */
        public Builder variable(String variable) {
            this.variable = variable;
            return this;
        }

        @Override
        public CookieAction build() {
            if (command == null) {
                throw new CitrusRuntimeException("Missing Playwright cookie command");
            }
            if (command == Command.ADD && cookie == null) {
                throw new CitrusRuntimeException("Missing Playwright cookie to add");
            }
            if (command == Command.READ && (name == null || variable == null)) {
                throw new CitrusRuntimeException("Missing Playwright cookie name or target variable");
            }
            if (command == Command.VERIFY && (name == null || expectedValue == null)) {
                throw new CitrusRuntimeException("Missing Playwright cookie verification name or value");
            }
            if ((domain != null || path != null) && command != Command.CLEAR) {
                throw new CitrusRuntimeException("Playwright cookie domain and path filters only apply to the clear command");
            }
            if (StringUtils.hasText(url) && command == Command.CLEAR) {
                throw new CitrusRuntimeException(
                        "Playwright cookie clear does not accept url - filter with name, domain, or path instead");
            }
            return new CookieAction(this);
        }
    }
}
