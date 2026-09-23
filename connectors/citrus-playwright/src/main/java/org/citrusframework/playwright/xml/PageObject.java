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

package org.citrusframework.playwright.xml;

import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlRootElement;

import org.citrusframework.TestActor;
import org.citrusframework.playwright.actions.AbstractPlaywrightAction;
import org.citrusframework.playwright.actions.PageObjectAction;
import org.citrusframework.playwright.endpoint.PlaywrightBrowser;
import org.citrusframework.playwright.page.PlaywrightPageValidator;
import org.citrusframework.playwright.util.DslCommands;

/**
 * Executes a method or a validator on a Playwright page object.
 *
 * <p>Page object and validator types are given as fully qualified class names.</p>
 */
@XmlRootElement(name = "page-object")
public class PageObject extends AbstractPlaywrightAction.Builder<PageObjectAction, PageObject> {

    private final PageObjectAction.Builder delegate = new PageObjectAction.Builder();

    @XmlAttribute
    public void setType(String type) {
        delegate.type(DslCommands.loadClass(type, Object.class));
    }

    @XmlAttribute
    public void setMethod(String method) {
        delegate.execute(method);
    }

    /**
     * Method arguments, separated by whitespace or commas.
     */
    @XmlAttribute
    public void setArguments(String arguments) {
        delegate.arguments(DslCommands.split(arguments));
    }

    @XmlAttribute
    @SuppressWarnings("unchecked")
    public void setValidator(String validator) {
        delegate.validate((Class<? extends PlaywrightPageValidator<?>>) DslCommands.loadClass(validator, PlaywrightPageValidator.class));
    }

    @Override
    public PageObject description(String description) {
        delegate.description(description);
        return this;
    }

    @Override
    public PageObject actor(TestActor actor) {
        delegate.actor(actor);
        return this;
    }

    @Override
    public PageObject browser(PlaywrightBrowser browser) {
        delegate.browser(browser);
        return this;
    }

    @Override
    public PageObjectAction build() {
        return delegate.build();
    }
}
