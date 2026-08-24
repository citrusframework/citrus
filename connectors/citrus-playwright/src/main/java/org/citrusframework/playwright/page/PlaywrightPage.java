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

package org.citrusframework.playwright.page;

import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;

import org.citrusframework.context.TestContext;
import org.citrusframework.playwright.endpoint.PlaywrightBrowser;

/**
 * Optional contract for page objects managed by the Citrus Playwright DSL.
 */
public interface PlaywrightPage {

    /**
     * Initializes a page object after construction.
     *
     * @param page current Playwright page
     * @param browserContext current Playwright browser context
     * @param browser Citrus Playwright endpoint
     * @param testContext active Citrus test context
     */
    default void initialize(Page page, BrowserContext browserContext, PlaywrightBrowser browser, TestContext testContext) {
        // Optional page-object initialization hook.
    }
}
