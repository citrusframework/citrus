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

import org.citrusframework.api.actions.selenium.SeleniumClearBrowserCacheActionBuilder;
import org.citrusframework.context.TestContext;
import org.citrusframework.selenium.endpoint.SeleniumBrowser;
import org.openqa.selenium.JavascriptExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Clears the browser cache.
 *
 * <p>A WebDriver does not provide a portable way to clear the browser HTTP cache, so this action
 * clears everything that a WebDriver can reliably clear for the current session instead:</p>
 *
 * <ul>
 *   <li>all cookies of the current session</li>
 *   <li>the web storage of the current origin - {@code window.localStorage} and
 *       {@code window.sessionStorage} - when the web driver supports JavaScript execution</li>
 * </ul>
 *
 * <p>The browser HTTP cache (e.g. scripts, styles and images served from the browser cache) and
 * the web storage of other origins remain untouched because plain WebDriver has no portable API
 * for clearing them.</p>
 *
 * @since 2.7
 */
public class ClearBrowserCacheAction extends AbstractSeleniumAction {

    /** Logger */
    private static final Logger logger = LoggerFactory.getLogger(ClearBrowserCacheAction.class);

    /** Clears the web storage of the current origin */
    static final String CLEAR_WEB_STORAGE_SCRIPT = """
            window.localStorage.clear();
            window.sessionStorage.clear();
            """;

    /**
     * Default constructor.
     */
    public ClearBrowserCacheAction(Builder builder) {
        super("clear-cache", builder);
    }

    @Override
    protected void execute(SeleniumBrowser browser, TestContext context) {
        browser.getWebDriver().manage().deleteAllCookies();

        if (browser.getWebDriver() instanceof JavascriptExecutor jsEngine) {
            jsEngine.executeScript(CLEAR_WEB_STORAGE_SCRIPT, new Object[]{});
        } else {
            logger.warn("Skip clearing web storage because web driver is missing javascript features");
        }
    }

    /**
     * Action builder.
     */
    public static class Builder extends AbstractSeleniumAction.Builder<ClearBrowserCacheAction, Builder>
            implements SeleniumClearBrowserCacheActionBuilder<ClearBrowserCacheAction, Builder> {

        @Override
        public ClearBrowserCacheAction build() {
            return new ClearBrowserCacheAction(this);
        }
    }
}
