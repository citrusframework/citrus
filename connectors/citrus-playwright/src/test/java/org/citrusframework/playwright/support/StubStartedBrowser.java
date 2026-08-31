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

import java.lang.reflect.Proxy;
import java.util.concurrent.atomic.AtomicInteger;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;

import org.citrusframework.playwright.endpoint.PlaywrightBrowser;
import org.citrusframework.playwright.endpoint.PlaywrightBrowserConfiguration;

/**
 * Test double that starts a {@link PlaywrightBrowser} endpoint without a real
 * browser process by substituting dynamic proxies for the Playwright driver
 * objects.
 */
public class StubStartedBrowser extends PlaywrightBrowser {

    private final AtomicInteger stops = new AtomicInteger();

    public StubStartedBrowser() {
        super(new PlaywrightBrowserConfiguration());
    }

    public int stopCount() {
        return stops.get();
    }

    @Override
    public synchronized void stop() {
        stops.incrementAndGet();
        super.stop();
    }

    @Override
    protected Playwright createPlaywright(PlaywrightBrowserConfiguration config) {
        BrowserType browserType = proxy(BrowserType.class, (method, args) -> {
            if ("launch".equals(method)) {
                return proxy(Browser.class, (innerMethod, innerArgs) -> "isConnected".equals(innerMethod));
            }
            return null;
        });
        return proxy(Playwright.class, (method, args) -> {
            if ("chromium".equals(method) || "firefox".equals(method) || "webkit".equals(method)) {
                return browserType;
            }
            return null;
        });
    }

    @Override
    protected BrowserContext createBrowserContext(Browser browser, PlaywrightBrowserConfiguration config) {
        return proxy(BrowserContext.class, (method, args) -> null);
    }

    @Override
    protected Page createPage(BrowserContext context) {
        return proxy(Page.class, (method, args) -> {
            if ("isClosed".equals(method)) {
                return Boolean.FALSE;
            }
            if (LOCATOR_FACTORIES.contains(method)) {
                return proxy(com.microsoft.playwright.Locator.class, (locatorMethod, locatorArgs) -> null);
            }
            return null;
        });
    }

    private static final java.util.Set<String> LOCATOR_FACTORIES = java.util.Set.of(
            "locator",
            "getByText",
            "getByRole",
            "getByTestId",
            "getByLabel",
            "getByPlaceholder",
            "getByAltText",
            "getByTitle",
            "getFrameLocator");

    @SuppressWarnings("unchecked")
    private <T> T proxy(Class<T> type, MethodHandler handler) {
        return (T) Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[]{type}, (proxy, method, args) -> {
            Object result = handler.handle(method.getName(), args);
            if (result != null) {
                return result;
            }
            Class<?> returnType = method.getReturnType();
            if (!returnType.isPrimitive()) {
                return null;
            }
            if (returnType == boolean.class) {
                return false;
            }
            if (returnType == char.class) {
                return '\0';
            }
            return 0;
        });
    }

    @FunctionalInterface
    interface MethodHandler {
        Object handle(String method, Object[] args);
    }
}
