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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.file.Path;

import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.playwright.actions.InputAction.Command;
import org.citrusframework.playwright.support.MockPlaywrightBrowser;
import org.citrusframework.playwright.support.PlaywrightBrowserScope;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.microsoft.playwright.Locator;

class InputActionTest {

    private MockPlaywrightBrowser browser;
    private TestContext context;

    @BeforeMethod
    void setUp() {
        browser = new MockPlaywrightBrowser();
        context = new TestContext();
        browser.start();
        PlaywrightBrowserScope.bind(browser, context);
        when(browser.page().locator("#name")).thenReturn(locator());
    }

    private Locator locator() {
        return mock(Locator.class);
    }

    @AfterMethod
    void clearScope() {
        PlaywrightBrowserScope.clear();
    }

    @Test
    void shouldFillElementWithResolvedValue() {
        Locator element = browser.page().locator("#name");
        context.setVariable("userName", "alice");

        new InputAction.Builder(Command.FILL).locator("#name").value("${userName}").build().execute(context);

        verify(element).fill("alice");
    }

    @Test
    void shouldClearElement() {
        Locator element = browser.page().locator("#name");

        new InputAction.Builder(Command.CLEAR).locator("#name").build().execute(context);

        verify(element).clear();
    }

    @Test
    void shouldPressKey() {
        Locator element = browser.page().locator("#name");

        new InputAction.Builder(Command.PRESS).locator("#name").key("Enter").build().execute(context);

        verify(element).press("Enter");
    }

    @Test
    void shouldCheckElement() {
        Locator element = browser.page().locator("#name");

        new InputAction.Builder(Command.CHECK).locator("#name").build().execute(context);

        verify(element).check();
    }

    @Test
    void shouldUncheckElement() {
        Locator element = browser.page().locator("#name");

        new InputAction.Builder(Command.UNCHECK).locator("#name").build().execute(context);

        verify(element).uncheck();
    }

    @Test
    void shouldSelectSingleValue() {
        Locator element = browser.page().locator("#name");

        new InputAction.Builder(Command.SELECT).locator("#name").value("two").build().execute(context);

        verify(element).selectOption(any(String[].class));
    }

    @Test
    void shouldSelectMultipleValues() {
        Locator element = browser.page().locator("#name");

        new InputAction.Builder(Command.SELECT).locator("#name").values("a", "b").build().execute(context);

        verify(element).selectOption(any(String[].class));
    }

    @Test
    void shouldUploadFileFromResolvedPath() {
        Locator element = browser.page().locator("#name");

        new InputAction.Builder(Command.UPLOAD).locator("#name").file("target/upload.txt").build().execute(context);

        verify(element).setInputFiles(any(Path.class));
    }

    @Test
    void shouldFailFastWhenFillValueMissing() {
        expectThrows(CitrusRuntimeException.class,
                () -> new InputAction.Builder(Command.FILL).locator("#name").build());
    }

    @Test
    void shouldFailFastWhenSelectValueAndValuesMissing() {
        CitrusRuntimeException exception = expectThrows(CitrusRuntimeException.class,
                () -> new InputAction.Builder(Command.SELECT).locator("#name").build());
        assertTrue(exception.getMessage().contains("select value"));
    }

    @Test
    void shouldFailFastWhenLocatorMissing() {
        expectThrows(CitrusRuntimeException.class,
                () -> new InputAction.Builder(Command.CLEAR).build());
    }
}
