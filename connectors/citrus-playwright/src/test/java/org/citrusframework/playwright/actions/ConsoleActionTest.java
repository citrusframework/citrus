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

import java.util.function.Consumer;

import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.exceptions.ValidationException;
import org.citrusframework.playwright.support.MockPlaywrightBrowser;
import org.citrusframework.playwright.support.PlaywrightBrowserScope;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.microsoft.playwright.ConsoleMessage;

class ConsoleActionTest {

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
    void shouldStartCaptureOnCurrentPage() {
        new ConsoleAction.Builder().capture().build().execute(context);

        verify(browser.page()).onConsoleMessage(any(Consumer.class));
    }

    @Test
    void shouldVerifyCapturedConsoleContainsText() {
        seedConsole("engine", "app ready");

        new ConsoleAction.Builder().verifyContains("app ready").build().execute(context);
    }

    @Test
    void shouldFailValidationWhenConsoleDoesNotContainText() {
        seedConsole("engine", "unrelated");

        ConsoleAction action = new ConsoleAction.Builder().verifyContains("ready").build();

        ValidationException exception = expectThrows(ValidationException.class, () -> action.execute(context));
        assertTrue(exception.getMessage().contains("ready"));
    }

    @Test
    void shouldReportCapturedMessagesToVariable() {
        seedConsole("engine", "app ready");

        new ConsoleAction.Builder().report().variable("consoleReport").build().execute(context);

        String report = context.getVariable("consoleReport");
        assertTrue(report.contains("app ready"));
    }

    @Test
    void shouldClearCapturedMessages() {
        seedConsole("engine", "app ready");

        new ConsoleAction.Builder().clear().build().execute(context);

        assertEquals(0, browser.getConsoleCaptureRegistry().messages(browser.page()).size());
    }

    @Test
    void shouldPassPageErrorCheckWhenNoErrors() {
        when(browser.page().pageErrors()).thenReturn(java.util.List.of());

        new ConsoleAction.Builder().verifyNoPageErrors().build().execute(context);
    }

    @Test
    void shouldFailPageErrorCheckWhenErrorsPresent() {
        when(browser.page().pageErrors()).thenReturn(java.util.List.of("TypeError: x is not a function"));

        ConsoleAction action = new ConsoleAction.Builder().verifyNoPageErrors().build();

        ValidationException exception = expectThrows(ValidationException.class, () -> action.execute(context));
        assertTrue(exception.getMessage().contains("1"));
    }

    @Test
    void shouldVerifyPageErrorsContainText() {
        when(browser.page().pageErrors()).thenReturn(java.util.List.of("TypeError: x is not a function"));

        new ConsoleAction.Builder().verifyPageErrorsContain("not a function").build().execute(context);
    }

    @Test
    void shouldFailValidationWhenPageErrorDoesNotContainText() {
        when(browser.page().pageErrors()).thenReturn(java.util.List.of("unrelated"));

        ConsoleAction action = new ConsoleAction.Builder().verifyPageErrorsContain("TypeError").build();

        ValidationException exception = expectThrows(ValidationException.class, () -> action.execute(context));
        assertTrue(exception.getMessage().contains("TypeError"));
    }

    @Test
    void shouldReportPageErrorsToVariable() {
        when(browser.page().pageErrors()).thenReturn(java.util.List.of("TypeError: boom"));

        new ConsoleAction.Builder().pageErrors().variable("pageErrors").build().execute(context);

        String report = context.getVariable("pageErrors");
        assertTrue(report.contains("TypeError: boom"));
    }

    @Test
    void shouldFailFastWhenPageErrorTextMissing() {
        CitrusRuntimeException exception = expectThrows(CitrusRuntimeException.class,
                () -> new ConsoleAction.Builder().verifyPageErrorsContain(null).build());
        assertTrue(exception.getMessage().contains("page error verification text"));
    }

    @Test
    void shouldReadRegistryWhenFilterUnset() {
        seedConsole("engine", "app ready");

        new ConsoleAction.Builder().verifyContains("app ready").build().execute(context);

        org.mockito.Mockito.verify(browser.page(), org.mockito.Mockito.never())
                .consoleMessages(any(com.microsoft.playwright.Page.ConsoleMessagesOptions.class));
    }

    @Test
    void shouldRetrieveSinceNavigationFromDriver() {
        ConsoleMessage recent = mock(ConsoleMessage.class);
        when(recent.type()).thenReturn("log");
        when(recent.text()).thenReturn("after navigation");
        when(recent.location()).thenReturn("app.js:2");
        when(recent.timestamp()).thenReturn(1.0);
        when(browser.page().consoleMessages(any(com.microsoft.playwright.Page.ConsoleMessagesOptions.class)))
                .thenReturn(java.util.List.of(recent));

        new ConsoleAction.Builder().report().filter("since-navigation").variable("consoleReport")
                .build().execute(context);

        String report = context.getVariable("consoleReport");
        assertTrue(report.contains("after navigation"));

        org.mockito.ArgumentCaptor<com.microsoft.playwright.Page.ConsoleMessagesOptions> captor =
                org.mockito.ArgumentCaptor.forClass(com.microsoft.playwright.Page.ConsoleMessagesOptions.class);
        org.mockito.Mockito.verify(browser.page()).consoleMessages(captor.capture());
        assertEquals(com.microsoft.playwright.options.ConsoleMessagesFilter.SINCE_NAVIGATION,
                captor.getValue().filter);
    }

    @Test
    void shouldRejectUnknownFilter() {
        ConsoleAction action = new ConsoleAction.Builder().report().filter("errors-only").build();

        CitrusRuntimeException exception = expectThrows(CitrusRuntimeException.class,
                () -> action.execute(context));
        assertTrue(exception.getMessage().contains("errors-only"));
    }

    @Test
    void shouldFailFastWhenCommandMissing() {
        expectThrows(CitrusRuntimeException.class, () -> new ConsoleAction.Builder().build());
    }

    @Test
    void shouldFailFastWhenVerifyTextMissing() {
        CitrusRuntimeException exception = expectThrows(CitrusRuntimeException.class,
                () -> new ConsoleAction.Builder().verifyContains(null).build());
        assertTrue(exception.getMessage().contains("verification text"));
    }

    private void seedConsole(String type, String text) {
        browser.getConsoleCaptureRegistry().capture(browser.page(), 10);
        org.mockito.ArgumentCaptor<Consumer<ConsoleMessage>> captor =
                org.mockito.ArgumentCaptor.forClass(Consumer.class);
        verify(browser.page()).onConsoleMessage(captor.capture());
        ConsoleMessage message = mock(ConsoleMessage.class);
        when(message.type()).thenReturn(type);
        when(message.text()).thenReturn(text);
        when(message.location()).thenReturn("file.js:1");
        when(message.timestamp()).thenReturn(0.0);
        captor.getValue().accept(message);
    }
}
