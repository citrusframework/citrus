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

package org.citrusframework.playwright.support;

import static org.mockito.Mockito.mock;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Download;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.Response;
import com.microsoft.playwright.Route;

import org.citrusframework.playwright.endpoint.PlaywrightBrowser;
import org.citrusframework.playwright.endpoint.PlaywrightBrowserConfiguration;

/**
 * Test double that starts a {@link PlaywrightBrowser} endpoint whose Playwright
 * driver objects are Mockito mocks.
 *
 * <p>Unlike {@link StubStartedBrowser} (which returns {@code null} from most
 * driver methods), this double returns typed mocks for the page, context, and
 * related objects so interaction-heavy actions can be exercised and verified
 * without a real browser process.</p>
 */
public class MockPlaywrightBrowser extends PlaywrightBrowser {

    private Playwright playwright;
    private Browser browser;
    private BrowserType browserType;
    private BrowserContext context;
    private Page page;
    private final Download download = mock(Download.class);
    private final Route route = mock(Route.class);
    private final Response response = mock(Response.class);

    public MockPlaywrightBrowser() {
        super(new PlaywrightBrowserConfiguration());
    }

    public BrowserType browserType() {
        return browserType;
    }

    public BrowserContext context() {
        return context;
    }

    public Page page() {
        return page;
    }

    public Download download() {
        return download;
    }

    public Route route() {
        return route;
    }

    public Response response() {
        return response;
    }

    @Override
    protected Playwright createPlaywright(PlaywrightBrowserConfiguration config) {
        if (playwright == null) {
            browserType = mock(BrowserType.class);
            browser = mock(Browser.class);
            playwright = mock(Playwright.class);
            org.mockito.Mockito.when(playwright.chromium()).thenReturn(browserType);
            org.mockito.Mockito.when(playwright.firefox()).thenReturn(browserType);
            org.mockito.Mockito.when(playwright.webkit()).thenReturn(browserType);
            org.mockito.Mockito.when(browser.isConnected()).thenReturn(true);
        }
        return playwright;
    }

    @Override
    protected Browser launchBrowser(Playwright playwright, PlaywrightBrowserConfiguration config) {
        return browser;
    }

    @Override
    protected BrowserContext createBrowserContext(Browser browser, Browser.NewContextOptions options) {
        context = mock(BrowserContext.class);
        org.mockito.Mockito.when(context.newPage()).thenReturn(mock(Page.class));
        return context;
    }

    @Override
    protected BrowserContext createBrowserContext(Browser browser, PlaywrightBrowserConfiguration config) {
        return createBrowserContext(browser, (Browser.NewContextOptions) null);
    }

    @Override
    protected Page createPage(BrowserContext context) {
        page = mock(Page.class);
        return page;
    }

    /**
     * Creates a fresh page mock and registers it under the supplied alias,
     * switching to it. Individual page mocks allow title/URL-based routing to be
     * stubbed per page.
     */
    public Page registerNewPage(String alias) {
        Page created = createPage(currentContextOrNull());
        registerPage(alias, created);
        page = created;
        return created;
    }

    private BrowserContext currentContextOrNull() {
        return currentContextIfAvailable().orElse(null);
    }
}
