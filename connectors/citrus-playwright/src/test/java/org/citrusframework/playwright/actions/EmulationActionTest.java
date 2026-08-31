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

import static org.testng.Assert.expectThrows;
import static org.testng.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.playwright.support.MockPlaywrightBrowser;
import org.citrusframework.playwright.support.PlaywrightBrowserScope;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.microsoft.playwright.options.Geolocation;

class EmulationActionTest {

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
    void shouldSetViewportSize() {
        new EmulationAction.Builder().viewport(1280, 720).build().execute(context);

        verify(browser.page()).setViewportSize(1280, 720);
    }

    @Test
    void shouldSetGeolocation() {
        new EmulationAction.Builder().geolocation(47.0, 28.5).build().execute(context);

        verify(browser.context()).setGeolocation(any(Geolocation.class));
    }

    @Test
    void shouldEmulateColorScheme() {
        new EmulationAction.Builder().colorScheme("dark").build().execute(context);

        verify(browser.page()).emulateMedia(any());
    }

    @Test
    void shouldCreateEmulationContextForLocale() {
        new EmulationAction.Builder().locale("en-GB").build().execute(context);

        assertTrue(browser.getContextAliases().contains("emulation"));
    }

    @Test
    void shouldUseConfiguredContextAlias() {
        new EmulationAction.Builder().timezone("Europe/Bucharest").contextAlias("geo").build().execute(context);

        assertTrue(browser.getContextAliases().contains("geo"));
        assertTrue(browser.getPageAliases().contains("geo"));
    }

    @Test
    void shouldFailFastWhenNoEmulationSettingConfigured() {
        CitrusRuntimeException exception = expectThrows(CitrusRuntimeException.class,
                () -> new EmulationAction.Builder().build());
        assertTrue(exception.getMessage().contains("emulation setting"));
    }
}
