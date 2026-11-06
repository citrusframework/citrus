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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class PlaywrightActionBuilderTest {

    @AfterEach
    void clearAmbientBrowserScope() {
        PlaywrightBrowserScope.clear();
    }

    @Test
    void shouldBuildEveryDelegatedMouseInputAndNavigationAction() {
        // Each of these registers itself with the enclosing builder, which is what makes
        // scoped blocks work; exercising them here keeps that wiring under test.
        assertEquals(NavigateAction.class, playwright().navigate().back().build().getClass());
        assertEquals(MouseAction.class, playwright().doubleClick().locator("#row").build().getClass());
        assertEquals(MouseAction.class, playwright().rightClick().locator("#row").build().getClass());
        assertEquals(MouseAction.class, playwright().hover().locator("#row").build().getClass());
        assertEquals(MouseAction.class, playwright().focus().locator("#row").build().getClass());
        assertEquals(MouseAction.class, playwright().tap().locator("#row").build().getClass());
        assertEquals(InputAction.class, playwright().enter().locator("#name").value("alice").build().getClass());
        assertEquals(InputAction.class, playwright().clear().locator("#name").build().getClass());
        assertEquals(InputAction.class, playwright().press().locator("#name").value("Enter").build().getClass());
        assertEquals(InputAction.class, playwright().check().locator("#agree").build().getClass());
        assertEquals(InputAction.class, playwright().uncheck().locator("#agree").build().getClass());
    }

    @Test
    void shouldExposeTheDelegateItLastBuilt() {
        PlaywrightActionBuilder builder = playwright();
        builder.start();

        assertEquals(StartBrowserAction.Builder.class, builder.getDelegate().getClass());
        assertEquals(StartBrowserAction.class, builder.build().getClass());
    }

    @Test
    void shouldBuildDelegatedActions() {
        PlaywrightTarget submit = PlaywrightTarget.the("submit button").locatedBy("#submit");

        assertEquals(StartBrowserAction.class, playwright().start().build().getClass());
        assertEquals(StopBrowserAction.class, playwright().stop().build().getClass());
        assertEquals(OpenAction.class, playwright().open().url("http://localhost").build().getClass());
        assertEquals(MouseAction.class, playwright().click().locator("#save").build().getClass());
        assertEquals(MouseAction.class, playwright().click().locator(submit).build().getClass());
        assertEquals(InputAction.class, playwright().fill().locator("#name").value("Citrus").build().getClass());
        assertEquals(WaitForAction.class, playwright().waitFor().locator("#ready").visible().build().getClass());
        assertEquals(ScreenshotAction.class, playwright().screenshot().path("target/s.png").build().getClass());
        assertEquals(JavaScriptAction.class, playwright().javascript().script("() => 1").build().getClass());
        assertEquals(VerifyAction.class, playwright().verify().locator("#message").visible().build().getClass());
        assertEquals(ExtractAction.class, playwright().extract().locator("#message").text().variable("message").build().getClass());
        assertEquals(ContextAction.class, playwright().context().newContext("admin").build().getClass());
        assertEquals(PageAction.class, playwright().page().newPage("dashboard").build().getClass());
        assertEquals(FrameAction.class, playwright().frame().frame("#frame").fill("#name").value("Citrus").build().getClass());
        assertEquals(DialogAction.class, playwright().dialog().accept().triggerScript("() => alert('ready')").build().getClass());
        assertEquals(DownloadAction.class, playwright().download().click("#export").saveAs("target/export.txt").build().getClass());
        assertEquals(CookieAction.class, playwright().cookies().read("session").variable("session").build().getClass());
        assertEquals(StorageAction.class, playwright().storage().local().set("theme", "dark").build().getClass());
        assertEquals(PermissionAction.class, playwright().permissions().grant("geolocation").build().getClass());
        assertEquals(EmulationAction.class, playwright().emulate().viewport(1280, 720).build().getClass());
        assertEquals(ConsoleAction.class, playwright().console().capture().build().getClass());
        assertEquals(NetworkAction.class, playwright().network().capture().build().getClass());
        assertEquals(NetworkAction.class, playwright().network().route("**/api/**").abort().build().getClass());
        assertEquals(NetworkAction.class, playwright().network()
                .route("**/api/**")
                .fulfillJson("{\"ok\":true}")
                .status(201)
                .header("x-test", "true")
                .build().getClass());
        assertEquals(NetworkAction.class, playwright().network()
                .route("**/api/**")
                .continueWithHeader("x-test", "true")
                .build().getClass());
        assertEquals(NetworkAction.class, playwright().network().unroute("**/api/**").build().getClass());
        assertEquals(NetworkAction.class, playwright().network()
                .waitForResponse()
                .urlContains("/api")
                .status(200)
                .click(submit)
                .includeBody()
                .variable("response")
                .build().getClass());
        assertEquals(TracingAction.class, playwright().tracing().start().build().getClass());
        assertEquals(PdfAction.class, playwright().pdf().path("target/page.pdf").build().getClass());
        assertEquals(PageObjectAction.class, playwright().pageObject().type(TestPage.class).execute("open").build().getClass());

        assertEquals(ExtractAction.class, playwright().extract().locator(".row").allTextContents().variable("rows").build().getClass());
        assertEquals(ExtractAction.class, playwright().extract().locator("#card").boundingBox().variable("box").build().getClass());
        assertEquals(ExtractAction.class, playwright().extract().locator("#card").cssClasses().variable("classes").build().getClass());
        assertEquals(ExtractAction.class, playwright().extract().locator("#card").cssValue("display").variable("display").build().getClass());
        assertEquals(ExtractAction.class, playwright().extract().locator("#main").ariaSnapshot().variable("snapshot").build().getClass());
        assertEquals(ExtractAction.class, playwright().extract().pageCount().variable("pages").build().getClass());
        assertEquals(ExtractAction.class, playwright().extract().frameContent("#frame").variable("frame").build().getClass());
        assertEquals(ExtractAction.class, playwright().extract().storageLocal("token").variable("token").build().getClass());
        assertEquals(ExtractAction.class, playwright().extract().cookie("session").variable("session").build().getClass());
        assertEquals(ExtractAction.class, playwright().extract().consoleMessages().variable("console").build().getClass());
        assertEquals(ExtractAction.class, playwright().extract().networkRecords().variable("network").build().getClass());
        assertEquals(ExtractAction.class, playwright().extract().downloadMetadata().variable("download").build().getClass());

        assertEquals(VerifyAction.class, playwright().verify().locator("#card").cssValue("display", "block").build().getClass());
        assertEquals(VerifyAction.class, playwright().verify().locator("#card").cssClass("primary").build().getClass());
        assertEquals(VerifyAction.class, playwright().verify().locator("#status").optionText("Ready").build().getClass());
        assertEquals(VerifyAction.class, playwright().verify().locator("#status").selectedOptionText("Ready").build().getClass());
        assertEquals(VerifyAction.class, playwright().verify().locator("#card").innerHtmlContains("Inner").build().getClass());
        assertEquals(VerifyAction.class, playwright().verify().locator("#main").ariaSnapshotContains("button").build().getClass());
        assertEquals(VerifyAction.class, playwright().verify().pageCount(1).build().getClass());
        assertEquals(VerifyAction.class, playwright().verify().frameContentContains("#frame", "Ready").build().getClass());
        assertEquals(VerifyAction.class, playwright().verify().storageLocal("token", "value").build().getClass());
        assertEquals(VerifyAction.class, playwright().verify().cookie("session", "abc").build().getClass());
        assertEquals(VerifyAction.class, playwright().verify().consoleContains("ready").build().getClass());
        assertEquals(VerifyAction.class, playwright().verify().networkUrlContains("/api").build().getClass());
        assertEquals(VerifyAction.class, playwright().verify().downloadFilename("export.csv").build().getClass());
    }

    @Test
    void shouldFailFastWhenRequiredBuilderFieldsAreMissing() {
        assertThrows(CitrusRuntimeException.class, () -> playwright().open().build());
        assertThrows(CitrusRuntimeException.class, () -> playwright().click().build());
        assertThrows(CitrusRuntimeException.class, () -> playwright().fill().locator("#name").build());
        assertThrows(CitrusRuntimeException.class, () -> playwright().extract().locator("#message").text().build());
        assertThrows(CitrusRuntimeException.class, () -> playwright().javascript().build());
        assertThrows(CitrusRuntimeException.class, () -> playwright().context().build());
        assertThrows(CitrusRuntimeException.class, () -> playwright().page().build());
        assertThrows(CitrusRuntimeException.class, () -> playwright().frame().build());
        assertThrows(CitrusRuntimeException.class, () -> playwright().download().build());
        assertThrows(CitrusRuntimeException.class, () -> playwright().pageObject().execute("open").build());
        assertThrows(CitrusRuntimeException.class, () -> playwright().network().abort().build());
        assertThrows(CitrusRuntimeException.class, () -> playwright().network().route("**/api/**").fulfill(null).build());
        assertThrows(CitrusRuntimeException.class, () -> playwright().network().route("**/api/**").continueWithHeader("x", null).build());
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

        CitrusRuntimeException exception = assertThrows(CitrusRuntimeException.class,
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
        CitrusRuntimeException exception = assertThrows(CitrusRuntimeException.class,
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
            Future<CitrusRuntimeException> failure = executor.submit(() -> assertThrows(CitrusRuntimeException.class,
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
