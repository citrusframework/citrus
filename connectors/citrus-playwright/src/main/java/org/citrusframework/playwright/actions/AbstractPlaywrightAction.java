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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.citrusframework.AbstractTestActionBuilder;
import org.citrusframework.actions.AbstractTestAction;
import org.citrusframework.context.TestContext;
import org.citrusframework.endpoint.Endpoint;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.playwright.endpoint.PlaywrightBrowser;
import org.citrusframework.playwright.endpoint.PlaywrightHeaders;
import org.citrusframework.playwright.support.PlaywrightBrowserScope;

public abstract class AbstractPlaywrightAction extends AbstractTestAction implements PlaywrightAction {

    private final PlaywrightBrowser browser;
    private final String browserName;

    protected AbstractPlaywrightAction(String name, Builder<?, ?> builder) {
        super("playwright:" + name, builder);
        this.browser = builder.browser;
        this.browserName = builder.browserName;
    }

    @Override
    public void doExecute(TestContext context) {
        PlaywrightBrowser resolvedBrowser = resolveBrowser(context);
        resolvedBrowser.assertActionThread();
        execute(resolvedBrowser, context);
    }

    protected abstract void execute(PlaywrightBrowser browser, TestContext context);

    /**
     * Returns the explicitly bound browser endpoint or null when the action
     * relies on ambient resolution at execution time.
     *
     * @return bound browser endpoint or null
     */
    public PlaywrightBrowser getBrowser() {
        return browser;
    }

    private PlaywrightBrowser resolveBrowser(TestContext context) {
        List<String> strategyReport = new ArrayList<>();

        if (browser != null) {
            return browser;
        }
        strategyReport.add("explicit .browser(<endpoint>) binding: not set on action");

        if (browserName != null) {
            if (context.getReferenceResolver() != null
                    && context.getReferenceResolver().isResolvable(browserName, PlaywrightBrowser.class)) {
                return context.getReferenceResolver().resolve(browserName, PlaywrightBrowser.class);
            }
            throw new CitrusRuntimeException(("Failed to resolve explicitly bound Playwright browser endpoint "
                    + "named '%s', no such %s registered in the reference resolver")
                    .formatted(browserName, PlaywrightBrowser.class.getSimpleName()));
        }
        strategyReport.add("explicit .browser(\"<name>\") binding: not set on action");

        Optional<PlaywrightBrowser> ambient = PlaywrightBrowserScope.current(context);
        if (ambient.isPresent()) {
            return ambient.get();
        }
        strategyReport.add("thread scope: no started browser bound to current thread");

        if (context.getVariables().containsKey(PlaywrightHeaders.PLAYWRIGHT_BROWSER)) {
            String variableBrowserName = context.getVariable(PlaywrightHeaders.PLAYWRIGHT_BROWSER);
            if (context.getReferenceResolver() != null
                    && context.getReferenceResolver().isResolvable(variableBrowserName, PlaywrightBrowser.class)) {
                return context.getReferenceResolver().resolve(variableBrowserName, PlaywrightBrowser.class);
            }
            strategyReport.add("variable '%s': set to '%s' but not resolvable as %s".formatted(
                    PlaywrightHeaders.PLAYWRIGHT_BROWSER, variableBrowserName, PlaywrightBrowser.class.getSimpleName()));
        } else {
            strategyReport.add("variable '%s': not set".formatted(PlaywrightHeaders.PLAYWRIGHT_BROWSER));
        }

        if (context.getReferenceResolver() != null
                && context.getReferenceResolver().isResolvable(PlaywrightBrowser.class)) {
            return context.getReferenceResolver().resolve(PlaywrightBrowser.class);
        }
        strategyReport.add("unique %s bean: none resolvable in application context"
                .formatted(PlaywrightBrowser.class.getSimpleName()));

        throw new CitrusRuntimeException(("Failed to get active Playwright browser instance for action '%s', "
                + "either set explicit browser for action or start browser instance. Attempted strategies:%n - %s")
                .formatted(getName(), String.join("\n - ", strategyReport)));
    }

    public abstract static class Builder<T extends PlaywrightAction, B extends Builder<T, B>>
            extends AbstractTestActionBuilder<T, B> {

        private PlaywrightBrowser browser;
        private String browserName;

        /**
         * Returns the currently bound browser endpoint or null when none was
         * set explicitly. Subclasses use this to decide whether a default
         * endpoint must be created lazily.
         *
         * @return bound browser endpoint or null
         */
        protected final PlaywrightBrowser boundBrowser() {
            return browser;
        }

        public B browser(PlaywrightBrowser browser) {
            this.browser = browser;
            return self;
        }

        public B browser(String browserName) {
            this.browserName = browserName;
            return self;
        }

        public B browser(Endpoint endpoint) {
            if (endpoint instanceof PlaywrightBrowser playwrightBrowser) {
                this.browser = playwrightBrowser;
                return self;
            }
            throw new CitrusRuntimeException("Invalid browser object, expected a PlaywrightBrowser, but got %s".formatted(endpoint.getClass().getName()));
        }
    }
}
