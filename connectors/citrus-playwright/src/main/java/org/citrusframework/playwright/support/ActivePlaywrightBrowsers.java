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

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

import org.citrusframework.playwright.endpoint.PlaywrightBrowser;

/**
 * Process-local registry of started Playwright browser endpoints.
 *
 * <p>The failure evidence listener uses this registry because Citrus test
 * listener callbacks do not directly expose endpoint instances. The registry is
 * weakly referenced so it does not prevent endpoint garbage collection.</p>
 */
public final class ActivePlaywrightBrowsers {

    private static final Set<PlaywrightBrowser> BROWSERS = Collections.synchronizedSet(
            Collections.newSetFromMap(new WeakHashMap<>()));

    private ActivePlaywrightBrowsers() {
    }

    /**
     * Registers a started browser endpoint for failure evidence capture.
     *
     * @param browser started browser endpoint
     */
    public static void register(PlaywrightBrowser browser) {
        BROWSERS.add(browser);
    }

    /**
     * Unregisters a stopped browser endpoint.
     *
     * @param browser browser endpoint to remove
     */
    public static void unregister(PlaywrightBrowser browser) {
        BROWSERS.remove(browser);
    }

    /**
     * Returns a stable snapshot of currently active browser endpoints.
     *
     * @return active browser snapshot
     */
    public static Set<PlaywrightBrowser> activeBrowsers() {
        synchronized (BROWSERS) {
            return Set.copyOf(BROWSERS);
        }
    }
}
