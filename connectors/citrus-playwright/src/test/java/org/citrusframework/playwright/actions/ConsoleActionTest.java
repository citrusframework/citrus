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
