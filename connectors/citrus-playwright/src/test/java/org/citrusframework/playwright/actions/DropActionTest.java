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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.expectThrows;

import java.util.Map;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.options.DropPayload;
import com.microsoft.playwright.options.FilePayload;

import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.playwright.support.MockPlaywrightBrowser;
import org.citrusframework.playwright.support.PlaywrightBrowserScope;
import org.mockito.ArgumentCaptor;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

class DropActionTest {

    private MockPlaywrightBrowser browser;
    private TestContext context;
    private Locator element;

    @BeforeMethod
    void setUp() {
        browser = new MockPlaywrightBrowser();
        context = new TestContext();
        element = mock(Locator.class);
        browser.start();
        PlaywrightBrowserScope.bind(browser, context);
        when(browser.page().locator("#dropzone")).thenReturn(element);
    }

    @AfterMethod
    void clearScope() {
        PlaywrightBrowserScope.clear();
    }

    @Test
    void shouldDropFilePayload() {
        new DropAction.Builder().locator("#dropzone")
                .file("note.txt", "text/plain", "hello").build().execute(context);

        FilePayload payload = (FilePayload) capturedPayload().files;
        assertEquals("note.txt", payload.name);
        assertEquals("text/plain", payload.mimeType);
        assertEquals("hello", new String(payload.buffer));
    }

    @Test
    void shouldDropDataPayload() {
        new DropAction.Builder().locator("#dropzone")
                .data("text/plain", "hello world")
                .data("text/uri-list", "https://example.com").build().execute(context);

        Map<String, String> data = capturedPayload().data;
        assertEquals("hello world", data.get("text/plain"));
        assertEquals("https://example.com", data.get("text/uri-list"));
    }

    @Test
    void shouldResolveVariablesInPayload() {
        context.setVariable("greeting", "hi");

        new DropAction.Builder().locator("#dropzone")
                .data("text/plain", "${greeting}").build().execute(context);

        assertEquals("hi", capturedPayload().data.get("text/plain"));
    }

    @Test
    void shouldFailFastWhenLocatorMissing() {
        expectThrows(CitrusRuntimeException.class,
                () -> new DropAction.Builder().data("text/plain", "x").build());
    }

    @Test
    void shouldFailFastWhenPayloadMissing() {
        expectThrows(CitrusRuntimeException.class,
                () -> new DropAction.Builder().locator("#dropzone").build());
    }

    private DropPayload capturedPayload() {
        ArgumentCaptor<DropPayload> captor = ArgumentCaptor.forClass(DropPayload.class);
        verify(element).drop(captor.capture());
        return captor.getValue();
    }
}
