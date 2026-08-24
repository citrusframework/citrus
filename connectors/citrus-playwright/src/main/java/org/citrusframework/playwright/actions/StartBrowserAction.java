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

import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.playwright.endpoint.PlaywrightBrowser;
import org.citrusframework.playwright.endpoint.PlaywrightBrowserConfiguration;
import org.citrusframework.playwright.endpoint.PlaywrightHeaders;
import org.citrusframework.playwright.support.PlaywrightBrowserScope;

/**
 * Starts a Playwright browser endpoint and makes it the executing thread's
 * ambient browser for the current test.
 *
 * <p>The bind happens on every execution, including when {@code allowAlreadyStarted}
 * is true and the browser was already running so the start itself was skipped.
 * Starting a browser is treated as the explicit statement of which one subsequent
 * actions should target, so starting a second browser replaces the first as the
 * ambient one. Multi-browser tests that need both at once should bind each chain
 * explicitly with {@code .browser(...)}, which always wins over the ambient
 * binding.</p>
 */
public class StartBrowserAction extends AbstractPlaywrightAction {

    private final boolean allowAlreadyStarted;

    public StartBrowserAction(Builder builder) {
        super("start", builder);
        this.allowAlreadyStarted = builder.allowAlreadyStarted;
    }

    @Override
    protected void execute(PlaywrightBrowser browser, TestContext context) {
        if (browser.isStarted() && !allowAlreadyStarted) {
            throw new CitrusRuntimeException("Playwright browser is already started and allowAlreadyStarted=false");
        }
        if (!browser.isStarted()) {
            browser.start();
        }
        PlaywrightBrowserScope.bind(browser, context);
        context.setVariable(PlaywrightHeaders.PLAYWRIGHT_BROWSER, browser.getName());
    }

    public boolean isAllowAlreadyStarted() {
        return allowAlreadyStarted;
    }

    public static class Builder extends AbstractPlaywrightAction.Builder<StartBrowserAction, Builder> {

        private boolean allowAlreadyStarted = true;

        /**
         * Allows starting an already started endpoint. Defaults to true so the
         * zero-configuration start idiom is idempotent within a test.
         *
         * @param allowAlreadyStarted false to fail fast on double start
         * @return this builder
         */
        public Builder allowAlreadyStarted(boolean allowAlreadyStarted) {
            this.allowAlreadyStarted = allowAlreadyStarted;
            return this;
        }

        /**
         * Configures the browser engine of a lazily created default endpoint.
         * Supported values are {@code chromium}, {@code firefox}, and
         * {@code webkit}.
         *
         * @param browserType Playwright browser engine name
         * @return this builder
         */
        public Builder browserType(String browserType) {
            defaultEndpoint().getEndpointConfiguration().setBrowserType(browserType);
            return this;
        }

        /**
         * Configures headless mode of a lazily created default endpoint.
         *
         * @param headless true to launch without visible window
         * @return this builder
         */
        public Builder headless(boolean headless) {
            defaultEndpoint().getEndpointConfiguration().setHeadless(headless);
            return this;
        }

        /**
         * Configures the base URL of a lazily created default endpoint.
         *
         * @param baseUrl base URL for relative navigation targets
         * @return this builder
         */
        public Builder baseUrl(String baseUrl) {
            defaultEndpoint().getEndpointConfiguration().setBaseUrl(baseUrl);
            return this;
        }

        /**
         * Configures the page opened right after start on a lazily created
         * default endpoint.
         *
         * @param startPageUrl initial page URL
         * @return this builder
         */
        public Builder startPageUrl(String startPageUrl) {
            defaultEndpoint().getEndpointConfiguration().setStartPageUrl(startPageUrl);
            return this;
        }

        /**
         * Configures the default action timeout of a lazily created default
         * endpoint.
         *
         * @param defaultTimeout timeout in milliseconds
         * @return this builder
         */
        public Builder defaultTimeout(long defaultTimeout) {
            defaultEndpoint().getEndpointConfiguration().setDefaultTimeout(defaultTimeout);
            return this;
        }

        /**
         * Configures the default navigation timeout of a lazily created
         * default endpoint.
         *
         * @param defaultNavigationTimeout timeout in milliseconds
         * @return this builder
         */
        public Builder defaultNavigationTimeout(long defaultNavigationTimeout) {
            defaultEndpoint().getEndpointConfiguration().setDefaultNavigationTimeout(defaultNavigationTimeout);
            return this;
        }

        /**
         * Configures the console message capture limit of a lazily created
         * default endpoint.
         *
         * @param consoleMessageLimit maximum buffered console messages
         * @return this builder
         */
        public Builder consoleMessageLimit(int consoleMessageLimit) {
            defaultEndpoint().getEndpointConfiguration().setConsoleMessageLimit(consoleMessageLimit);
            return this;
        }

        /**
         * Configures the network record capture limit of a lazily created
         * default endpoint.
         *
         * @param networkRecordLimit maximum buffered network records
         * @return this builder
         */
        public Builder networkRecordLimit(int networkRecordLimit) {
            defaultEndpoint().getEndpointConfiguration().setNetworkRecordLimit(networkRecordLimit);
            return this;
        }

        private PlaywrightBrowser defaultEndpoint() {
            if (boundBrowser() == null) {
                browser(new PlaywrightBrowser(new PlaywrightBrowserConfiguration()));
            }
            return boundBrowser();
        }

        @Override
        public StartBrowserAction build() {
            return new StartBrowserAction(this);
        }
    }
}
