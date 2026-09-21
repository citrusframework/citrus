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
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.expectThrows;
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
import com.microsoft.playwright.options.ScreenshotType;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.mockito.ArgumentCaptor;

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
    void shouldInferWebpTypeFromPathExtension() {
        when(browser.page().screenshot(any(Page.ScreenshotOptions.class))).thenReturn(new byte[]{1});

        new ScreenshotAction.Builder().path("target/capture.webp").build().execute(context);

        assertEquals(ScreenshotType.WEBP, capturedOptions().type);
    }

    @Test
    void shouldInferJpegTypeFromPathExtension() {
        when(browser.page().screenshot(any(Page.ScreenshotOptions.class))).thenReturn(new byte[]{1});

        new ScreenshotAction.Builder().path("target/capture.jpeg").build().execute(context);

        assertEquals(ScreenshotType.JPEG, capturedOptions().type);
    }

    @Test
    void shouldPreferExplicitTypeOverPathExtension() {
        when(browser.page().screenshot(any(Page.ScreenshotOptions.class))).thenReturn(new byte[]{1});

        new ScreenshotAction.Builder().path("target/capture.png").type("webp").build().execute(context);

        assertEquals(ScreenshotType.WEBP, capturedOptions().type);
    }

    @Test
    void shouldApplyQualityAndFullPage() {
        when(browser.page().screenshot(any(Page.ScreenshotOptions.class))).thenReturn(new byte[]{1});

        new ScreenshotAction.Builder().path("target/capture.webp").quality(50).fullPage(true)
                .build().execute(context);

        Page.ScreenshotOptions options = capturedOptions();
        assertEquals(Integer.valueOf(50), options.quality);
        assertEquals(Boolean.TRUE, options.fullPage);
    }

    @Test
    void shouldNotSetTypeForDefaultPngPath() {
        when(browser.page().screenshot(any(Page.ScreenshotOptions.class))).thenReturn(new byte[]{1});

        new ScreenshotAction.Builder().build().execute(context);

        Page.ScreenshotOptions options = capturedOptions();
        assertNull(options.quality);
        assertNull(options.fullPage);
    }

    @Test
    void shouldRejectQualityForPngScreenshots() {
        CitrusRuntimeException exception = expectThrows(CitrusRuntimeException.class,
                () -> new ScreenshotAction.Builder().path("target/capture.png").quality(50).build());

        assertTrue(exception.getMessage().toLowerCase().contains("quality"), exception.getMessage());
    }

    @Test
    void shouldRejectUnsupportedScreenshotType() {
        expectThrows(CitrusRuntimeException.class,
                () -> new ScreenshotAction.Builder().type("tiff").build());
    }

    private Page.ScreenshotOptions capturedOptions() {
        ArgumentCaptor<Page.ScreenshotOptions> captor = ArgumentCaptor.forClass(Page.ScreenshotOptions.class);
        verify(browser.page()).screenshot(captor.capture());
        return captor.getValue();
    }

    @Test
    void shouldStorePathInVariableWhenConfigured() {
        when(browser.page().screenshot(any(Page.ScreenshotOptions.class)))
                .thenReturn(new byte[]{1, 2, 3});

        new ScreenshotAction.Builder().path("target/capture.png").variable("shot").build().execute(context);

        assertEquals("target/capture.png", context.getVariable("shot"));
    }
}
