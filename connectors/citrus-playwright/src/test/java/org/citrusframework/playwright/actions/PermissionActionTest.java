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
import static org.mockito.Mockito.verify;

import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.playwright.support.MockPlaywrightBrowser;
import org.citrusframework.playwright.support.PlaywrightBrowserScope;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

class PermissionActionTest {

    private MockPlaywrightBrowser browser;
    private TestContext context;

    @BeforeMethod
    void setUp() {
        browser = new MockPlaywrightBrowser();
        context = new TestContext();
        browser.start();
        PlaywrightBrowserScope.bind(browser, context);
        browser.createContext("admin");
    }

    @AfterMethod
    void clearScope() {
        PlaywrightBrowserScope.clear();
    }

    @Test
    void shouldGrantPermissions() {
        new PermissionAction.Builder().grant("geolocation", "notifications").build().execute(context);

        verify(browser.context()).grantPermissions(java.util.List.of("geolocation", "notifications"));
    }

    @Test
    void shouldResolvePermissionsThroughContext() {
        context.setVariable("permission", "clipboard-read");

        new PermissionAction.Builder().grant("${permission}").build().execute(context);

        verify(browser.context()).grantPermissions(java.util.List.of("clipboard-read"));
    }

    @Test
    void shouldClearPermissions() {
        new PermissionAction.Builder().clear().build().execute(context);

        verify(browser.context()).clearPermissions();
    }

    @Test
    void shouldFailFastWhenCommandMissing() {
        expectThrows(CitrusRuntimeException.class, () -> new PermissionAction.Builder().build());
    }

    @Test
    void shouldFailFastWhenGrantHasNoPermissions() {
        expectThrows(CitrusRuntimeException.class, () -> new PermissionAction.Builder().grant(new String[0]).build());
    }
}
