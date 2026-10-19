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

package org.citrusframework.selenium.xml;

import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlRootElement;
import org.citrusframework.TestActor;
import org.citrusframework.selenium.actions.AbstractSeleniumAction;
import org.citrusframework.selenium.actions.CookieAction;
import org.citrusframework.selenium.endpoint.SeleniumBrowser;

@XmlRootElement(name = "cookies")
public class Cookies extends AbstractSeleniumAction.Builder<CookieAction, Cookies> {

    private final CookieAction.Builder delegate = new CookieAction.Builder();

    @XmlAttribute
    public void setCommand(String command) {
        this.delegate.command(command);
    }

    @XmlAttribute
    public void setName(String name) {
        this.delegate.name(name);
    }

    @XmlAttribute
    public void setValue(String value) {
        this.delegate.value(value);
    }

    @XmlAttribute
    public void setVariable(String variable) {
        this.delegate.variable(variable);
    }

    @XmlAttribute
    public void setExpected(String expected) {
        this.delegate.expected(expected);
    }

    @XmlAttribute
    public void setDomain(String domain) {
        this.delegate.domain(domain);
    }

    @XmlAttribute
    public void setPath(String path) {
        this.delegate.path(path);
    }

    @XmlAttribute
    public void setExpiry(String expiry) {
        this.delegate.expiry(expiry);
    }

    @XmlAttribute
    public void setSecure(boolean secure) {
        this.delegate.secure(secure);
    }

    @XmlAttribute(name = "http-only")
    public void setHttpOnly(boolean httpOnly) {
        this.delegate.httpOnly(httpOnly);
    }

    @XmlAttribute(name = "same-site")
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
