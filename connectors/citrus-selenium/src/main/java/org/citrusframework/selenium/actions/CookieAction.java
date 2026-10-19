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

package org.citrusframework.selenium.actions;

import java.time.Instant;
import java.util.Date;

import org.citrusframework.api.actions.selenium.SeleniumCookieActionBuilder;
import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.exceptions.ValidationException;
import org.citrusframework.selenium.endpoint.SeleniumBrowser;
import org.citrusframework.util.StringUtils;
import org.openqa.selenium.Cookie;

/**
 * Manages browser cookies of the current page's browser session.
 *
 * @since 5.1
 */
public class CookieAction extends AbstractSeleniumAction {

    private static final String COMMAND_NAMES = "add, read, verify, verify-absent, delete, delete-all";

    private final String command;
    private final String name;
    private final String value;
    private final String domain;
    private final String path;
    private final String expiry;
    private final Date expiryDate;
    private final Boolean secure;
    private final Boolean httpOnly;
    private final String sameSite;
    private final String variable;
    private final String expected;

    public CookieAction(Builder builder) {
        super("cookies", builder);

        this.command = builder.command;
        this.name = builder.name;
        this.value = builder.value;
        this.domain = builder.domain;
        this.path = builder.path;
        this.expiry = builder.expiry;
        this.expiryDate = builder.expiryDate;
        this.secure = builder.secure;
        this.httpOnly = builder.httpOnly;
        this.sameSite = builder.sameSite;
        this.variable = builder.variable;
        this.expected = builder.expected;
    }

    @Override
    protected void execute(SeleniumBrowser browser, TestContext context) {
        if (!StringUtils.hasText(command)) {
            throw new CitrusRuntimeException(String.format("Missing cookies command - use one of %s", COMMAND_NAMES));
        }

        Command commandToRun = Command.from(command);

        switch (commandToRun) {
            case ADD -> addCookie(browser, context);
            case READ -> readCookie(browser, context);
            case VERIFY -> verifyCookie(browser, context, false);
            case VERIFY_ABSENT -> verifyCookie(browser, context, true);
            case DELETE -> browser.getWebDriver().manage().deleteCookieNamed(resolveName(context));
            case DELETE_ALL -> browser.getWebDriver().manage().deleteAllCookies();
            default -> throw new CitrusRuntimeException(String.format("Unsupported cookies command '%s' - use one of %s", command, COMMAND_NAMES));
        }
    }

    private void addCookie(SeleniumBrowser browser, TestContext context) {
        if (!StringUtils.hasText(name)) {
            throw new CitrusRuntimeException("Missing cookie name for cookies command 'add'");
        }

        String cookieName = context.replaceDynamicContentInString(name);
        String cookieValue = value != null ? context.replaceDynamicContentInString(value) : "";
        String cookieDomain = domain != null ? context.replaceDynamicContentInString(domain) : null;
        String cookiePath = path != null ? context.replaceDynamicContentInString(path) : null;
        Date cookieExpiry = resolveExpiry(context);
        boolean isSecure = secure != null && secure;
        boolean isHttpOnly = httpOnly != null && httpOnly;
        String cookieSameSite = sameSite != null ? context.replaceDynamicContentInString(sameSite) : null;

        Cookie.Builder cookieBuilder = new Cookie.Builder(cookieName, cookieValue);
        if (cookieDomain != null) {
            cookieBuilder.domain(cookieDomain);
        }
        if (cookiePath != null) {
            cookieBuilder.path(cookiePath);
        }
        if (cookieExpiry != null) {
            cookieBuilder.expiresOn(cookieExpiry);
        }
        cookieBuilder.isSecure(isSecure);
        cookieBuilder.isHttpOnly(isHttpOnly);
        if (cookieSameSite != null) {
            cookieBuilder.sameSite(cookieSameSite);
        }

        browser.getWebDriver().manage().addCookie(cookieBuilder.build());
    }

    private Date resolveExpiry(TestContext context) {
        if (expiryDate != null) {
            return expiryDate;
        }

        if (!StringUtils.hasText(expiry)) {
            return null;
        }

        String resolved = context.replaceDynamicContentInString(expiry).trim();
        try {
            long epochSeconds = Long.parseLong(resolved);
            return Date.from(Instant.ofEpochSecond(epochSeconds));
        } catch (NumberFormatException e) {
            throw new CitrusRuntimeException(String.format("Invalid cookie expiry '%s' - use epoch seconds", expiry));
        }
    }

    private void readCookie(SeleniumBrowser browser, TestContext context) {
        if (!StringUtils.hasText(variable)) {
            throw new CitrusRuntimeException("Missing variable name for cookies command 'read'");
        }

        Cookie cookie = requireCookie(browser, context);
        context.setVariable(variable, cookie.getValue());
    }

    private void verifyCookie(SeleniumBrowser browser, TestContext context, boolean absent) {
        String cookieName = resolveName(context);
        Cookie cookie = browser.getWebDriver().manage().getCookieNamed(cookieName);

        if (absent) {
            if (cookie != null) {
                throw new ValidationException(String.format("Selenium validation failed, cookie '%s' expected 'absent', but was present", cookieName));
            }
            return;
        }

        if (cookie == null) {
            throw new CitrusRuntimeException(String.format("Failed to find cookie '%s'", cookieName));
        }

        ValueAssertions.assertValue(String.format("cookie '%s'", cookieName), expected, cookie.getValue(), context);
    }

    private Cookie requireCookie(SeleniumBrowser browser, TestContext context) {
        String cookieName = resolveName(context);
        Cookie cookie = browser.getWebDriver().manage().getCookieNamed(cookieName);

        if (cookie == null) {
            throw new CitrusRuntimeException(String.format("Failed to find cookie '%s'", cookieName));
        }

        return cookie;
    }

    private String resolveName(TestContext context) {
        if (!StringUtils.hasText(name)) {
            throw new CitrusRuntimeException(String.format("Missing cookie name for cookies command '%s'", command));
        }

        return context.replaceDynamicContentInString(name);
    }

    public String getCommand() {
        return command;
    }

    public String getCookieName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    public String getVariable() {
        return variable;
    }

    public String getExpected() {
        return expected;
    }

    public String getDomain() {
        return domain;
    }

    public String getPath() {
        return path;
    }

    public String getExpiry() {
        return expiry;
    }

    public Boolean getSecure() {
        return secure;
    }

    public Boolean getHttpOnly() {
        return httpOnly;
    }

    public String getSameSite() {
        return sameSite;
    }

    private enum Command {
        ADD("add"),
        READ("read"),
        VERIFY("verify"),
        VERIFY_ABSENT("verify-absent"),
        DELETE("delete"),
        DELETE_ALL("delete-all");

        private final String name;

        Command(String name) {
            this.name = name;
        }

        private static Command from(String command) {
            for (Command candidate : values()) {
                if (candidate.name.equals(command)) {
                    return candidate;
                }
            }

            throw new CitrusRuntimeException(String.format("Unsupported cookies command '%s' - use one of %s", command, COMMAND_NAMES));
        }
    }

    public static class Builder extends AbstractSeleniumAction.Builder<CookieAction, Builder>
            implements SeleniumCookieActionBuilder<CookieAction, Builder> {

        private String command;
        private String name;
        private String value;
        private String domain;
        private String path;
        private String expiry;
        private Date expiryDate;
        private Boolean secure;
        private Boolean httpOnly;
        private String sameSite;
        private String variable;
        private String expected;

        @Override
        public Builder command(String command) {
            this.command = command;
            return this;
        }

        @Override
        public Builder add(String name, String value) {
            this.command = "add";
            this.name = name;
            this.value = value;
            return this;
        }

        @Override
        public Builder name(String name) {
            this.name = name;
            return this;
        }

        @Override
        public Builder value(String value) {
            this.value = value;
            return this;
        }

        @Override
        public Builder domain(String domain) {
            this.domain = domain;
            return this;
        }

        @Override
        public Builder path(String path) {
            this.path = path;
            return this;
        }

        @Override
        public Builder expiry(String expiry) {
            this.expiry = expiry;
            return this;
        }

        @Override
        public Builder expiry(Date expiry) {
            this.expiryDate = expiry;
            return this;
        }

        @Override
        public Builder secure(boolean secure) {
            this.secure = secure;
            return this;
        }

        @Override
        public Builder httpOnly(boolean httpOnly) {
            this.httpOnly = httpOnly;
            return this;
        }

        @Override
        public Builder sameSite(String sameSite) {
            this.sameSite = sameSite;
            return this;
        }

        @Override
        public Builder read(String name, String variable) {
            this.command = "read";
            this.name = name;
            this.variable = variable;
            return this;
        }

        @Override
        public Builder verify(String name, String expected) {
            this.command = "verify";
            this.name = name;
            this.expected = expected;
            return this;
        }

        @Override
        public Builder verifyAbsent(String name) {
            this.command = "verify-absent";
            this.name = name;
            return this;
        }

        @Override
        public Builder delete(String name) {
            this.command = "delete";
            this.name = name;
            return this;
        }

        @Override
        public Builder deleteAll() {
            this.command = "delete-all";
            return this;
        }

        @Override
        public Builder variable(String variable) {
            this.variable = variable;
            return this;
        }

        @Override
        public Builder expected(String expected) {
            this.expected = expected;
            return this;
        }

        @Override
        public CookieAction build() {
            return new CookieAction(this);
        }
    }
}
