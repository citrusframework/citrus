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
import static org.testng.Assert.expectThrows;
import static org.testng.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.file.Path;

import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.exceptions.ValidationException;
import org.citrusframework.playwright.support.MockPlaywrightBrowser;
import org.citrusframework.playwright.support.PlaywrightBrowserScope;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.WebStorage;

class StorageActionTest {

    private MockPlaywrightBrowser browser;
    private TestContext context;

    private WebStorage localStorage;
    private WebStorage sessionStorage;

    @BeforeMethod
    void setUp() {
        browser = new MockPlaywrightBrowser();
        context = new TestContext();
        localStorage = mock(WebStorage.class);
        sessionStorage = mock(WebStorage.class);
        browser.start();
        PlaywrightBrowserScope.bind(browser, context);
        when(browser.page().localStorage()).thenReturn(localStorage);
        when(browser.page().sessionStorage()).thenReturn(sessionStorage);
    }

    @AfterMethod
    void clearScope() {
        PlaywrightBrowserScope.clear();
    }

    @Test
    void shouldSetLocalStorageItem() {
        new StorageAction.Builder().local().set("theme", "dark").build().execute(context);

        verify(localStorage).setItem("theme", "dark");
    }

    @Test
    void shouldReadLocalStorageItemIntoVariable() {
        when(localStorage.getItem("token")).thenReturn("abc");

        new StorageAction.Builder().local().read("token").variable("stored").build().execute(context);

        assertEquals("abc", context.getVariable("stored"));
    }

    @Test
    void shouldVerifyLocalStorageItem() {
        when(localStorage.getItem("theme")).thenReturn("dark");

        new StorageAction.Builder().local().verify("theme", "dark").build().execute(context);
    }

    @Test
    void shouldFailValidationWhenLocalStorageMismatches() {
        when(localStorage.getItem("theme")).thenReturn("light");

        StorageAction action = new StorageAction.Builder().local().verify("theme", "dark").build();

        ValidationException exception = expectThrows(ValidationException.class, () -> action.execute(context));
        assertTrue(exception.getMessage().contains("dark"));
    }

    @Test
    void shouldRemoveLocalStorageItem() {
        new StorageAction.Builder().local().remove("theme").build().execute(context);

        verify(localStorage).removeItem("theme");
    }

    @Test
    void shouldClearLocalStorage() {
        new StorageAction.Builder().local().clear().build().execute(context);

        verify(localStorage).clear();
    }

    @Test
    void shouldTargetSessionStorage() {
        new StorageAction.Builder().session().set("sid", "xyz").build().execute(context);

        verify(sessionStorage).setItem("sid", "xyz");
    }

    @Test
    void shouldSaveStorageState() {
        browser.createContext("admin");

        new StorageAction.Builder().saveState("target/state.json").build().execute(context);

        verify(browser.context()).storageState(any(BrowserContext.StorageStateOptions.class));
    }

    @Test
    void shouldRestoreStorageState() {
        browser.createContext("admin");

        new StorageAction.Builder().restoreState("target/state.json").build().execute(context);

        verify(browser.context()).setStorageState(Path.of("target/state.json"));
    }

    @Test
    void shouldFailFastWhenCommandMissing() {
        expectThrows(CitrusRuntimeException.class, () -> new StorageAction.Builder().build());
    }

    @Test
    void shouldFailFastWhenSetValueMissing() {
        expectThrows(CitrusRuntimeException.class,
                () -> new StorageAction.Builder().local().set("theme", null).build());
    }

    @Test
    void shouldFailFastWhenReadVariableMissing() {
        expectThrows(CitrusRuntimeException.class,
                () -> new StorageAction.Builder().local().read("theme").build());
    }
}
