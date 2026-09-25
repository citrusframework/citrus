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

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Playwright;

/**
 * Probes the Playwright browser runtime for integration tests, so every browser IT decides the
 * same way whether to skip.
 */
public final class PlaywrightRuntime {

    private PlaywrightRuntime() {
    }

    /**
     * Reports whether headless Chromium can be launched.
     *
     * @return true when the Chromium runtime is installed
     */
    public static boolean chromiumAvailable() {
        try (Playwright playwright = Playwright.create()) {
            try (Browser ignored = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true))) {
                return true;
            }
        } catch (RuntimeException e) {
            return false;
        }
    }
}
