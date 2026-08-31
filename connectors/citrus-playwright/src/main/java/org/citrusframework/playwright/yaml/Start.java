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

import org.citrusframework.TestActor;
import org.citrusframework.api.yaml.SchemaProperty;
import org.citrusframework.playwright.actions.AbstractPlaywrightAction;
import org.citrusframework.playwright.actions.StartBrowserAction;
import org.citrusframework.playwright.endpoint.PlaywrightBrowser;

/**
 * Starts a Playwright browser endpoint and binds it as the ambient browser.
 */
public class Start extends AbstractPlaywrightAction.Builder<StartBrowserAction, Start> {

    private final StartBrowserAction.Builder delegate = new StartBrowserAction.Builder();

    @SchemaProperty
    public void setAllowAlreadyStarted(Boolean allowAlreadyStarted) {
        if (allowAlreadyStarted != null) {
            delegate.allowAlreadyStarted(allowAlreadyStarted);
        }
    }

    @SchemaProperty
    public void setBrowserType(String browserType) {
        delegate.browserType(browserType);
    }

    @SchemaProperty
    public void setHeadless(Boolean headless) {
        if (headless != null) {
            delegate.headless(headless);
        }
    }

    @SchemaProperty
    public void setBaseUrl(String baseUrl) {
        delegate.baseUrl(baseUrl);
    }

    @SchemaProperty
    public void setStartPageUrl(String startPageUrl) {
        delegate.startPageUrl(startPageUrl);
    }

    @SchemaProperty
    public void setDefaultTimeout(Long defaultTimeout) {
        if (defaultTimeout != null) {
            delegate.defaultTimeout(defaultTimeout);
        }
    }

    @SchemaProperty
    public void setDefaultNavigationTimeout(Long defaultNavigationTimeout) {
        if (defaultNavigationTimeout != null) {
            delegate.defaultNavigationTimeout(defaultNavigationTimeout);
        }
    }

    @Override
    public Start description(String description) {
        delegate.description(description);
        return this;
    }

    @Override
    public Start actor(TestActor actor) {
        delegate.actor(actor);
        return this;
    }

    @Override
    public Start browser(PlaywrightBrowser browser) {
        delegate.browser(browser);
        return this;
    }

    @Override
    public StartBrowserAction build() {
        return delegate.build();
    }
}
