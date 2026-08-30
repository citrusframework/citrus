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

import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.playwright.endpoint.PlaywrightBrowser;
import org.citrusframework.playwright.page.PlaywrightPage;
import org.citrusframework.playwright.page.PlaywrightPageValidator;
import org.citrusframework.playwright.support.MockPlaywrightBrowser;
import org.citrusframework.playwright.support.PlaywrightBrowserScope;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

class PageObjectActionTest {

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
    void shouldInstantiatePageObjectAndInvokeMethod() {
        FixturePage.invocations = 0;

        new PageObjectAction.Builder().type(FixturePage.class).execute("open").build().execute(context);

        assertEquals(1, FixturePage.invocations);
    }

    @Test
    void shouldRunValidatorAgainstPageObject() {
        new PageObjectAction.Builder().type(FixturePage.class).validate(FixtureValidator.class)
                .build().execute(context);

        assertTrue(FixtureValidator.validated);
    }

    @Test
    void shouldFailValidationWhenPageIsNotAPlaywrightPage() {
        PageObjectAction action = new PageObjectAction.Builder().type(PlainClass.class).validate(FixtureValidator.class).build();

        CitrusRuntimeException exception = expectThrows(CitrusRuntimeException.class, () -> action.execute(context));
        assertTrue(exception.getMessage().contains("PlaywrightPage"));
    }

    @Test
    void shouldFailFastWhenPageTypeMissing() {
        expectThrows(CitrusRuntimeException.class,
                () -> new PageObjectAction.Builder().execute("open").build());
    }

    @Test
    void shouldFailFastWhenNoMethodOrValidatorConfigured() {
        expectThrows(CitrusRuntimeException.class,
                () -> new PageObjectAction.Builder().type(FixturePage.class).build());
    }

    public static class FixturePage implements PlaywrightPage {
        static int invocations;

        public void open() {
            invocations++;
        }
    }

    public static class PlainClass {
    }

    public static class FixtureValidator implements PlaywrightPageValidator<FixturePage> {
        static boolean validated;

        @Override
        public void validate(FixturePage page, PlaywrightBrowser browser, TestContext context) {
            validated = true;
        }
    }
}
