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

import static org.citrusframework.playwright.actions.PlaywrightActionBuilder.playwright;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.expectThrows;
import static org.testng.Assert.assertTrue;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.playwright.endpoint.PlaywrightBrowser;
import org.citrusframework.playwright.endpoint.PlaywrightHeaders;
import org.citrusframework.playwright.model.PlaywrightTarget;
import org.citrusframework.playwright.support.PlaywrightBrowserScope;
import org.citrusframework.spi.SimpleReferenceResolver;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

class PlaywrightActionBuilderTest {

    @AfterMethod
    void clearAmbientBrowserScope() {
        PlaywrightBrowserScope.clear();
    }

    @Test
    void shouldBuildEveryDelegatedMouseInputAndNavigationAction() {
        // Each of these registers itself with the enclosing builder, which is what makes
        // scoped blocks work; exercising them here keeps that wiring under test.
        assertEquals(playwright().navigate().back().build().getClass(), NavigateAction.class);
        assertEquals(playwright().doubleClick().locator("#row").build().getClass(), MouseAction.class);
        assertEquals(playwright().rightClick().locator("#row").build().getClass(), MouseAction.class);
        assertEquals(playwright().hover().locator("#row").build().getClass(), MouseAction.class);
        assertEquals(playwright().focus().locator("#row").build().getClass(), MouseAction.class);
        assertEquals(playwright().tap().locator("#row").build().getClass(), MouseAction.class);
        assertEquals(playwright().enter().locator("#name").value("alice").build().getClass(), InputAction.class);
        assertEquals(playwright().clear().locator("#name").build().getClass(), InputAction.class);
        assertEquals(playwright().press().locator("#name").value("Enter").build().getClass(), InputAction.class);
        assertEquals(playwright().check().locator("#agree").build().getClass(), InputAction.class);
        assertEquals(playwright().uncheck().locator("#agree").build().getClass(), InputAction.class);
    }

    @Test
    void shouldExposeTheDelegateItLastBuilt() {
        PlaywrightActionBuilder builder = playwright();
        builder.start();

        assertEquals(builder.getDelegate().getClass(), StartBrowserAction.Builder.class);
        assertEquals(builder.build().getClass(), StartBrowserAction.class);
    }

    @Test
    void shouldBuildDelegatedActions() {
        PlaywrightTarget submit = PlaywrightTarget.the("submit button").locatedBy("#submit");

        assertEquals(playwright().start().build().getClass(), StartBrowserAction.class);
        assertEquals(playwright().stop().build().getClass(), StopBrowserAction.class);
        assertEquals(playwright().open().url("http://localhost").build().getClass(), OpenAction.class);
        assertEquals(playwright().click().locator("#save").build().getClass(), MouseAction.class);
        assertEquals(playwright().click().locator(submit).build().getClass(), MouseAction.class);
        assertEquals(playwright().fill().locator("#name").value("Citrus").build().getClass(), InputAction.class);
        assertEquals(playwright().waitFor().locator("#ready").visible().build().getClass(), WaitForAction.class);
        assertEquals(playwright().screenshot().path("target/s.png").build().getClass(), ScreenshotAction.class);
        assertEquals(playwright().javascript().script("() => 1").build().getClass(), JavaScriptAction.class);
        assertEquals(playwright().verify().locator("#message").visible().build().getClass(), VerifyAction.class);
        assertEquals(playwright().extract().locator("#message").text().variable("message").build().getClass(), ExtractAction.class);
        assertEquals(playwright().context().newContext("admin").build().getClass(), ContextAction.class);
        assertEquals(playwright().page().newPage("dashboard").build().getClass(), PageAction.class);
        assertEquals(playwright().frame().frame("#frame").fill("#name").value("Citrus").build().getClass(), FrameAction.class);
        assertEquals(playwright().dialog().accept().triggerScript("() => alert('ready')").build().getClass(), DialogAction.class);
        assertEquals(playwright().download().click("#export").saveAs("target/export.txt").build().getClass(), DownloadAction.class);
        assertEquals(playwright().cookies().read("session").variable("session").build().getClass(), CookieAction.class);
        assertEquals(playwright().storage().local().set("theme", "dark").build().getClass(), StorageAction.class);
        assertEquals(playwright().permissions().grant("geolocation").build().getClass(), PermissionAction.class);
        assertEquals(playwright().emulate().viewport(1280, 720).build().getClass(), EmulationAction.class);
        assertEquals(playwright().console().capture().build().getClass(), ConsoleAction.class);
        assertEquals(playwright().network().capture().build().getClass(), NetworkAction.class);
        assertEquals(playwright().network().route("**/api/**").abort().build().getClass(), NetworkAction.class);
        assertEquals(playwright().network()
                .route("**/api/**")
                .fulfillJson("{\"ok\":true}")
                .status(201)
                .header("x-test", "true")
                .build().getClass(), NetworkAction.class);
        assertEquals(playwright().network()
                .route("**/api/**")
                .continueWithHeader("x-test", "true")
                .build().getClass(), NetworkAction.class);
        assertEquals(playwright().network().unroute("**/api/**").build().getClass(), NetworkAction.class);
        assertEquals(playwright().network()
                .waitForResponse()
                .urlContains("/api")
                .status(200)
                .click(submit)
                .includeBody()
                .variable("response")
                .build().getClass(), NetworkAction.class);
        assertEquals(playwright().tracing().start().build().getClass(), TracingAction.class);
        assertEquals(playwright().pdf().path("target/page.pdf").build().getClass(), PdfAction.class);
        assertEquals(playwright().pageObject().type(TestPage.class).execute("open").build().getClass(), PageObjectAction.class);

        assertEquals(playwright().extract().locator(".row").allTextContents().variable("rows").build().getClass(), ExtractAction.class);
        assertEquals(playwright().extract().locator("#card").boundingBox().variable("box").build().getClass(), ExtractAction.class);
        assertEquals(playwright().extract().locator("#card").cssClasses().variable("classes").build().getClass(), ExtractAction.class);
        assertEquals(playwright().extract().locator("#card").cssValue("display").variable("display").build().getClass(), ExtractAction.class);
        assertEquals(playwright().extract().locator("#main").ariaSnapshot().variable("snapshot").build().getClass(), ExtractAction.class);
        assertEquals(playwright().extract().pageCount().variable("pages").build().getClass(), ExtractAction.class);
        assertEquals(playwright().extract().frameContent("#frame").variable("frame").build().getClass(), ExtractAction.class);
        assertEquals(playwright().extract().storageLocal("token").variable("token").build().getClass(), ExtractAction.class);
        assertEquals(playwright().extract().cookie("session").variable("session").build().getClass(), ExtractAction.class);
        assertEquals(playwright().extract().consoleMessages().variable("console").build().getClass(), ExtractAction.class);
        assertEquals(playwright().extract().networkRecords().variable("network").build().getClass(), ExtractAction.class);
        assertEquals(playwright().extract().downloadMetadata().variable("download").build().getClass(), ExtractAction.class);

        assertEquals(playwright().verify().locator("#card").cssValue("display", "block").build().getClass(), VerifyAction.class);
        assertEquals(playwright().verify().locator("#card").cssClass("primary").build().getClass(), VerifyAction.class);
        assertEquals(playwright().verify().locator("#status").optionText("Ready").build().getClass(), VerifyAction.class);
        assertEquals(playwright().verify().locator("#status").selectedOptionText("Ready").build().getClass(), VerifyAction.class);
        assertEquals(playwright().verify().locator("#card").innerHtmlContains("Inner").build().getClass(), VerifyAction.class);
        assertEquals(playwright().verify().locator("#main").ariaSnapshotContains("button").build().getClass(), VerifyAction.class);
        assertEquals(playwright().verify().pageCount(1).build().getClass(), VerifyAction.class);
        assertEquals(playwright().verify().frameContentContains("#frame", "Ready").build().getClass(), VerifyAction.class);
        assertEquals(playwright().verify().storageLocal("token", "value").build().getClass(), VerifyAction.class);
        assertEquals(playwright().verify().cookie("session", "abc").build().getClass(), VerifyAction.class);
        assertEquals(playwright().verify().consoleContains("ready").build().getClass(), VerifyAction.class);
        assertEquals(playwright().verify().networkUrlContains("/api").build().getClass(), VerifyAction.class);
        assertEquals(playwright().verify().downloadFilename("export.csv").build().getClass(), VerifyAction.class);
    }

    @Test
    void shouldFailFastWhenRequiredBuilderFieldsAreMissing() {
        expectThrows(CitrusRuntimeException.class, () -> playwright().open().build());
        expectThrows(CitrusRuntimeException.class, () -> playwright().click().build());
        expectThrows(CitrusRuntimeException.class, () -> playwright().fill().locator("#name").build());
        expectThrows(CitrusRuntimeException.class, () -> playwright().extract().locator("#message").text().build());
        expectThrows(CitrusRuntimeException.class, () -> playwright().javascript().build());
        expectThrows(CitrusRuntimeException.class, () -> playwright().context().build());
        expectThrows(CitrusRuntimeException.class, () -> playwright().page().build());
        expectThrows(CitrusRuntimeException.class, () -> playwright().frame().fill("#name").value("Citrus").build());
        expectThrows(CitrusRuntimeException.class, () -> playwright().download().build());
        expectThrows(CitrusRuntimeException.class, () -> playwright().pageObject().execute("open").build());
        expectThrows(CitrusRuntimeException.class, () -> playwright().network().abort().build());
        expectThrows(CitrusRuntimeException.class, () -> playwright().network().route("**/api/**").fulfill(null).build());
        expectThrows(CitrusRuntimeException.class, () -> playwright().network().route("**/api/**").continueWithHeader("x", null).build());
    }

    @Test
    void shouldParameterizeReusableTargets() {
        PlaywrightTarget row = PlaywrightTarget.the("row {0}")
                .inFrame("#frame-{0}")
                .locatedBy(".row-{0}")
                .of("42");

        assertEquals("row 42", row.toString());
        assertEquals(List.of("#frame-42"), row.getFramePath());
        assertEquals(".row-42", row.getLocator().getSelector());
    }

    @Test
    void shouldResolveExplicitBrowser() {
        TestBrowser browser = new TestBrowser();
        browser.setName("playwrightBrowser");

        TestContext context = new TestContext();

        playwright().browser(browser).start().build().execute(context);

        assertTrue(browser.started);
        assertEquals("playwrightBrowser", context.getVariable(PlaywrightHeaders.PLAYWRIGHT_BROWSER));
    }

    @Test
    void shouldResolveBrowserFromContextVariable() {
        TestBrowser browser = new TestBrowser();
        browser.setName("playwrightBrowser");

        SimpleReferenceResolver referenceResolver = new SimpleReferenceResolver();
        referenceResolver.bind("playwrightBrowser", browser);

        TestContext context = new TestContext();
        context.setReferenceResolver(referenceResolver);
        context.setVariable(PlaywrightHeaders.PLAYWRIGHT_BROWSER, "playwrightBrowser");

        playwright().start().build().execute(context);

        assertTrue(browser.started);
        assertEquals("playwrightBrowser", context.getVariable(PlaywrightHeaders.PLAYWRIGHT_BROWSER));
    }

    @Test
    void shouldResolveSingleBrowserBean() {
        TestBrowser browser = new TestBrowser();
        browser.setName("playwrightBrowser");

        SimpleReferenceResolver referenceResolver = new SimpleReferenceResolver();
        referenceResolver.bind("playwrightBrowser", browser);

        TestContext context = new TestContext();
        context.setReferenceResolver(referenceResolver);

        playwright().start().build().execute(context);

        assertTrue(browser.started);
        assertEquals("playwrightBrowser", context.getVariable(PlaywrightHeaders.PLAYWRIGHT_BROWSER));
    }

    @Test
    void shouldThrowWhenBrowserAlreadyStartedAndNotAllowed() {
        TestBrowser browser = new TestBrowser();
        browser.setName("playwrightBrowser");
        browser.start();

        CitrusRuntimeException exception = expectThrows(CitrusRuntimeException.class,
                () -> playwright().browser(browser)
                        .start()
                        .allowAlreadyStarted(false)
                        .build()
                        .execute(new TestContext()));

        assertTrue(exception.getMessage().contains("already started"));
        assertTrue(browser.started);
        assertEquals(1, browser.starts);
        assertEquals(0, browser.stops);
    }

    @Test
    void shouldFailWhenBrowserIsMissing() {
        CitrusRuntimeException exception = expectThrows(CitrusRuntimeException.class,
                () -> playwright().start().build().execute(new TestContext()));

        assertTrue(exception.getMessage().contains("Failed to get active Playwright browser instance"));
    }

    @Test
    void shouldRejectSameBrowserFromDifferentActionThreads() throws Exception {
        TestBrowser browser = new TestBrowser();
        browser.setName("playwrightBrowser");

        playwright().browser(browser).start().build().execute(new TestContext());

        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            Future<CitrusRuntimeException> failure = executor.submit(() -> expectThrows(CitrusRuntimeException.class,
                    () -> playwright().browser(browser).start().build().execute(new TestContext())));

            CitrusRuntimeException exception = failure.get(5, TimeUnit.SECONDS);

            assertTrue(exception.getMessage().contains("cannot be shared"));
            assertTrue(exception.getMessage().contains("one PlaywrightBrowser endpoint per parallel test thread"));
        } finally {
            executor.shutdownNow();
        }
    }

    static class TestBrowser extends PlaywrightBrowser {
        boolean started;
        int starts;
        int stops;

        @Override
        public boolean isStarted() {
            return started;
        }

        @Override
        public void start() {
            started = true;
            starts++;
        }

        @Override
        public void stop() {
            started = false;
            stops++;
        }
    }

    public static class TestPage {
        public void open() {
            // Test fixture for page object builder validation.
        }
    }
}
