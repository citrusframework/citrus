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

import java.util.Locale;

import com.microsoft.playwright.options.SameSiteAttribute;

import org.citrusframework.TestActor;
import org.citrusframework.api.yaml.SchemaProperty;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.playwright.actions.AbstractPlaywrightAction;
import org.citrusframework.playwright.actions.CookieAction;
import org.citrusframework.playwright.endpoint.PlaywrightBrowser;
import org.citrusframework.playwright.model.CookieSpec;
import org.citrusframework.playwright.util.DslCommands;

/**
 * Adds, clears, reads or verifies cookies of the current browser context.
 */
public class Cookies extends AbstractPlaywrightAction.Builder<CookieAction, Cookies> {

    private final CookieAction.Builder delegate = new CookieAction.Builder();

    private String command;
    private String name;
    private String value;
    private String url;
    private String domain;
    private String path;
    private Double expires;
    private Boolean httpOnly;
    private Boolean secure;
    private String sameSite;

    @SchemaProperty
    public void setCommand(String command) {
        this.command = command;
    }

    @SchemaProperty
    public void setName(String name) {
        this.name = name;
    }

    @SchemaProperty
    public void setValue(String value) {
        this.value = value;
    }

    @SchemaProperty
    public void setUrl(String url) {
        this.url = url;
    }

    @SchemaProperty
    public void setDomain(String domain) {
        this.domain = domain;
    }

    @SchemaProperty
    public void setPath(String path) {
        this.path = path;
    }

    @SchemaProperty
    public void setExpires(Double expires) {
        this.expires = expires;
    }

    @SchemaProperty
    public void setHttpOnly(Boolean httpOnly) {
        this.httpOnly = httpOnly;
    }

    @SchemaProperty
    public void setSecure(Boolean secure) {
        this.secure = secure;
    }

    @SchemaProperty
    public void setSameSite(String sameSite) {
        this.sameSite = sameSite;
    }

    @SchemaProperty
    public void setVariable(String variable) {
        delegate.variable(variable);
    }

    private CookieSpec cookie() {
        if (name == null || value == null) {
            throw new CitrusRuntimeException("Missing Playwright cookie name or value");
        }

        CookieSpec cookie = CookieSpec.cookie(name, value);
        if (url != null) {
            cookie.url(url);
        }
        if (domain != null) {
            cookie.domain(domain);
        }
        if (path != null) {
            cookie.path(path);
        }
        if (expires != null) {
            cookie.expires(expires);
        }
        if (httpOnly != null) {
            cookie.httpOnly(httpOnly);
        }
        if (secure != null) {
            cookie.secure(secure);
        }
        if (sameSite != null) {
            cookie.sameSite(SameSiteAttribute.valueOf(DslCommands.normalize(sameSite).toUpperCase(Locale.ROOT)));
        }
        return cookie;
    }

    @Override
    public Cookies description(String description) {
        delegate.description(description);
        return this;
    }

    @Override
    public Cookies actor(TestActor actor) {
        delegate.actor(actor);
        return this;
    }

    @Override
    public Cookies browser(PlaywrightBrowser browser) {
        delegate.browser(browser);
        return this;
    }

    @Override
    public CookieAction build() {
        switch (DslCommands.normalize(command)) {
            case "add" -> delegate.add(cookie());
            case "clear" -> delegate.clear();
            case "read" -> delegate.read(name);
            case "verify" -> delegate.verify(name, value);
            default -> throw DslCommands.unsupported("cookies", command);
        }

        return delegate.build();
    }
}
