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

import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.exceptions.ValidationException;
import org.citrusframework.playwright.support.MockPlaywrightBrowser;
import org.citrusframework.playwright.support.PlaywrightBrowserScope;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.microsoft.playwright.Dialog;

class DialogActionTest {

    private MockPlaywrightBrowser browser;
    private TestContext context;
    private Dialog dialog;

    @BeforeMethod
    void setUp() {
        browser = new MockPlaywrightBrowser();
        context = new TestContext();
        dialog = mock(Dialog.class);
        browser.start();
        PlaywrightBrowserScope.bind(browser, context);
    }

    @AfterMethod
    void clearScope() {
        PlaywrightBrowserScope.clear();
    }

    @Test
    void shouldAcceptNextDialog() {
        new DialogAction.Builder().accept().build().execute(context);

        verify(browser.page()).onceDialog(any());
    }

    @Test
    void shouldDismissNextDialog() {
        new DialogAction.Builder().dismiss().build().execute(context);

        verify(browser.page()).onceDialog(any());
    }

    @Test
    void shouldTriggerDialogScript() {
        new DialogAction.Builder().dismiss().triggerScript("() => alert('ready')").build().execute(context);

        verify(browser.page()).evaluate("() => alert('ready')");
    }

    @Test
    void shouldVerifyExpectedMessageAndAccept() {
        new DialogAction.Builder().accept().message("Confirm?").type("confirm").build().execute(context);

        org.mockito.ArgumentCaptor<java.util.function.Consumer<Dialog>> captor =
                org.mockito.ArgumentCaptor.forClass(java.util.function.Consumer.class);
        verify(browser.page()).onceDialog(captor.capture());

        when(dialog.message()).thenReturn("Confirm?");
        when(dialog.type()).thenReturn("confirm");

        captor.getValue().accept(dialog);

        verify(dialog).accept();
    }

    @Test
    void shouldAcceptWithPromptText() {
        new DialogAction.Builder().accept().promptText("Citrus").build().execute(context);

        org.mockito.ArgumentCaptor<java.util.function.Consumer<Dialog>> captor =
                org.mockito.ArgumentCaptor.forClass(java.util.function.Consumer.class);
        verify(browser.page()).onceDialog(captor.capture());

        captor.getValue().accept(dialog);

        verify(dialog).accept("Citrus");
    }

    @Test
    void shouldDismissDialogViaHandler() {
        new DialogAction.Builder().dismiss().build().execute(context);

        org.mockito.ArgumentCaptor<java.util.function.Consumer<Dialog>> captor =
                org.mockito.ArgumentCaptor.forClass(java.util.function.Consumer.class);
        verify(browser.page()).onceDialog(captor.capture());

        captor.getValue().accept(dialog);

        verify(dialog).dismiss();
    }

    @Test
    void shouldFailFastWhenCommandMissing() {
        expectThrows(CitrusRuntimeException.class, () -> new DialogAction.Builder().build());
    }

    @Test
    void shouldFailValidationWhenMessageMismatches() {
        new DialogAction.Builder().accept().message("Confirm?").build().execute(context);

        org.mockito.ArgumentCaptor<java.util.function.Consumer<Dialog>> captor =
                org.mockito.ArgumentCaptor.forClass(java.util.function.Consumer.class);
        verify(browser.page()).onceDialog(captor.capture());

        when(dialog.message()).thenReturn("Different");

        ValidationException exception = expectThrows(ValidationException.class,
                () -> captor.getValue().accept(dialog));
        assertTrue(exception.getMessage().contains("Confirm?"));
    }
}
