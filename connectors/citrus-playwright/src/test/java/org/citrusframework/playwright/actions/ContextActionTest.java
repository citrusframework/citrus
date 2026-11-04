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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.playwright.support.MockPlaywrightBrowser;
import org.citrusframework.playwright.support.PlaywrightBrowserScope;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ContextActionTest {

    private MockPlaywrightBrowser browser;
    private TestContext context;

    @BeforeEach
    void setUp() {
        browser = new MockPlaywrightBrowser();
        context = new TestContext();
        browser.start();
        PlaywrightBrowserScope.bind(browser, context);
    }

    @AfterEach
    void clearScope() {
        PlaywrightBrowserScope.clear();
    }

    @Test
    void shouldCreateContextByAlias() {
        new ContextAction.Builder().newContext("admin").build().execute(context);

        assertTrue(browser.getContextAliases().contains("admin"));
    }

    @Test
    void shouldCreateContextWithStorageState() {
        new ContextAction.Builder().newContext("admin").storageState("target/state.json").build().execute(context);

        assertTrue(browser.getContextAliases().contains("admin"));
    }

    @Test
    void shouldSwitchToContextByAlias() {
        browser.createContext("admin");

        new ContextAction.Builder().switchTo("admin").build().execute(context);

        assertTrue(browser.getCurrentContextAlias().filter("admin"::equals).isPresent());
    }

    @Test
    void shouldCloseContextByAlias() {
        browser.createContext("admin");

        new ContextAction.Builder().close("admin").build().execute(context);

        assertFalse(browser.getContextAliases().contains("admin"));
    }

    @Test
    void shouldFailFastWhenCommandOrAliasMissing() {
        CitrusRuntimeException exception = assertThrows(CitrusRuntimeException.class,
                () -> new ContextAction.Builder().build());
        assertTrue(exception.getMessage().contains("command or alias"));
    }
}
