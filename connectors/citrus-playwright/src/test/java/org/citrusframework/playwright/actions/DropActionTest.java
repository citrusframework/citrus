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

import static org.mockito.ArgumentMatchers.any;
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
    void shouldDropWithTimeoutOption() {
        new DropAction.Builder().locator("#dropzone")
                .file("note.txt", "text/plain", "hello").timeout(5000).build().execute(context);

        ArgumentCaptor<DropPayload> payloadCaptor = ArgumentCaptor.forClass(DropPayload.class);
        ArgumentCaptor<Locator.DropOptions> optionsCaptor = ArgumentCaptor.forClass(Locator.DropOptions.class);
        verify(element).drop(payloadCaptor.capture(), optionsCaptor.capture());
        assertEquals(Double.valueOf(5000), optionsCaptor.getValue().timeout);
    }

    @Test
    void shouldDropAtPosition() {
        new DropAction.Builder().locator("#dropzone")
                .data("text/plain", "hello").position(10, 20).build().execute(context);

        ArgumentCaptor<DropPayload> payloadCaptor = ArgumentCaptor.forClass(DropPayload.class);
        ArgumentCaptor<Locator.DropOptions> optionsCaptor = ArgumentCaptor.forClass(Locator.DropOptions.class);
        verify(element).drop(payloadCaptor.capture(), optionsCaptor.capture());
        assertEquals(10.0, optionsCaptor.getValue().position.x);
        assertEquals(20.0, optionsCaptor.getValue().position.y);
    }

    @Test
    void shouldDropWithoutOptionsByDefault() {
        new DropAction.Builder().locator("#dropzone")
                .file("note.txt", "text/plain", "hello").build().execute(context);

        verify(element).drop(any(DropPayload.class));
        verify(element, org.mockito.Mockito.never()).drop(any(DropPayload.class), any(Locator.DropOptions.class));
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

    @Test
    void shouldFailFastWhenPositionIncomplete() throws Exception {
        DropAction.Builder builder = new DropAction.Builder().locator("#dropzone")
                .data("text/plain", "x");
        java.lang.reflect.Field field = DropAction.Builder.class.getDeclaredField("positionX");
        field.setAccessible(true);
        field.set(builder, Double.valueOf(10));

        expectThrows(CitrusRuntimeException.class, builder::build);
    }

    private DropPayload capturedPayload() {
        ArgumentCaptor<DropPayload> captor = ArgumentCaptor.forClass(DropPayload.class);
        verify(element).drop(captor.capture());
        return captor.getValue();
    }
}
