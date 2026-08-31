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

import static org.testng.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.citrusframework.context.TestContext;
import org.citrusframework.playwright.support.MockPlaywrightBrowser;
import org.citrusframework.playwright.support.PlaywrightBrowserScope;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.microsoft.playwright.Page;

class ScreenshotActionTest {

    private MockPlaywrightBrowser browser;
    private TestContext context;

    @BeforeMethod
    void setUp() {
        browser = new MockPlaywrightBrowser();
        context = new TestContext();
        browser.start();
        PlaywrightBrowserScope.bind(browser, context);
    }

    @AfterMethod
    void clearScope() {
        PlaywrightBrowserScope.clear();
    }

    @Test
    void shouldStoreDefaultPath() {
        assertEquals("target/playwright/screenshot.png", new ScreenshotAction.Builder().build().getPath());
    }

    @Test
    void shouldCaptureScreenshotToConfiguredPath() {
        when(browser.page().screenshot(any(Page.ScreenshotOptions.class)))
                .thenReturn(new byte[]{1, 2, 3});

        new ScreenshotAction.Builder().path("target/capture.png").build().execute(context);

        verify(browser.page()).screenshot(any(Page.ScreenshotOptions.class));
    }

    @Test
    void shouldStorePathInVariableWhenConfigured() {
        when(browser.page().screenshot(any(Page.ScreenshotOptions.class)))
                .thenReturn(new byte[]{1, 2, 3});

        new ScreenshotAction.Builder().path("target/capture.png").variable("shot").build().execute(context);

        assertEquals("target/capture.png", context.getVariable("shot"));
    }
}
