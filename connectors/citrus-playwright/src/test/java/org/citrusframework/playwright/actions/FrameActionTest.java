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

import com.microsoft.playwright.FrameLocator;
import com.microsoft.playwright.Locator;

class FrameActionTest {

    private MockPlaywrightBrowser browser;
    private TestContext context;

    private FrameLocator frameLocator;
    private Locator element;

    @BeforeMethod
    void setUp() {
        browser = new MockPlaywrightBrowser();
        context = new TestContext();
        frameLocator = mock(FrameLocator.class);
        element = mock(Locator.class);
        browser.start();
        PlaywrightBrowserScope.bind(browser, context);
        when(browser.page().frameLocator("#frame")).thenReturn(frameLocator);
        when(frameLocator.locator("#name")).thenReturn(element);
    }

    @AfterMethod
    void clearScope() {
        PlaywrightBrowserScope.clear();
    }

    @Test
    void shouldFillElementInsideFrame() {
        new FrameAction.Builder().frame("#frame").fill("#name").value("Citrus").build().execute(context);

        verify(element).fill("Citrus");
    }

    @Test
    void shouldClickElementInsideFrame() {
        new FrameAction.Builder().frame("#frame").click("#name").build().execute(context);

        verify(element).click();
    }

    @Test
    void shouldVerifyTextInsideFrame() {
        when(element.textContent()).thenReturn("Ready");

        new FrameAction.Builder().frame("#frame").verifyText("#name", "Ready").build().execute(context);

        verify(element).textContent();
    }

    @Test
    void shouldFailWhenFrameTextMismatches() {
        when(element.textContent()).thenReturn("Loading");

        FrameAction action = new FrameAction.Builder().frame("#frame").verifyText("#name", "Ready").build();

        ValidationException exception = expectThrows(ValidationException.class, () -> action.execute(context));
        assertTrue(exception.getMessage().contains("Ready"));
    }

    @Test
    void shouldFailFastWhenSelectorLocatorCommandMissing() {
        expectThrows(CitrusRuntimeException.class, () -> new FrameAction.Builder().build());
    }

    @Test
    void shouldFailFastWhenFillValueMissing() {
        expectThrows(CitrusRuntimeException.class,
                () -> new FrameAction.Builder().frame("#frame").fill("#name").build());
    }
}
