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

package org.citrusframework.selenium.yaml;

import org.citrusframework.TestActor;
import org.citrusframework.api.yaml.SchemaProperty;
import org.citrusframework.selenium.actions.AbstractSeleniumAction;
import org.citrusframework.selenium.actions.CookieAction;
import org.citrusframework.selenium.endpoint.SeleniumBrowser;

public class Cookies extends AbstractSeleniumAction.Builder<CookieAction, Cookies> {

    private final CookieAction.Builder delegate = new CookieAction.Builder();

    @SchemaProperty
    public void setCommand(String command) {
        this.delegate.command(command);
    }

    @SchemaProperty
    public void setName(String name) {
        this.delegate.name(name);
    }

    @SchemaProperty
    public void setValue(String value) {
        this.delegate.value(value);
    }

    @SchemaProperty
    public void setVariable(String variable) {
        this.delegate.variable(variable);
    }

    @SchemaProperty
    public void setExpected(String expected) {
        this.delegate.expected(expected);
    }

    @SchemaProperty
    public void setDomain(String domain) {
        this.delegate.domain(domain);
    }

    @SchemaProperty
    public void setPath(String path) {
        this.delegate.path(path);
    }

    @SchemaProperty
    public void setExpiry(String expiry) {
        this.delegate.expiry(expiry);
    }

    @SchemaProperty
    public void setSecure(boolean secure) {
        this.delegate.secure(secure);
    }

    @SchemaProperty(name = "http-only")
    public void setHttpOnly(boolean httpOnly) {
        this.delegate.httpOnly(httpOnly);
    }

    @SchemaProperty(name = "same-site")
    public void setSameSite(String sameSite) {
        this.delegate.sameSite(sameSite);
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
    public Cookies browser(SeleniumBrowser seleniumBrowser) {
        delegate.browser(seleniumBrowser);
        return this;
    }

    @Override
    public CookieAction build() {
        return delegate.build();
    }
}
