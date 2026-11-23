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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.TimeoutError;

import java.io.IOException;
import java.lang.reflect.Proxy;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.citrusframework.TestCase;
import org.citrusframework.container.Sequence;
import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.exceptions.ValidationException;
import org.citrusframework.playwright.endpoint.PlaywrightBrowser;
import org.citrusframework.playwright.endpoint.builder.PlaywrightEndpoints;
import org.citrusframework.playwright.model.SecretPatternRedactor;
import org.citrusframework.playwright.model.BoundingBoxResult;
import org.citrusframework.playwright.model.ConsoleMessageRecord;
import org.citrusframework.playwright.model.CookieSpec;
import org.citrusframework.playwright.model.DownloadMetadata;
import org.citrusframework.playwright.model.LocatorSpec;
import org.citrusframework.playwright.model.NetworkRecord;
import org.citrusframework.playwright.model.NetworkResponseResult;
import org.citrusframework.playwright.model.PlaywrightTarget;
import org.citrusframework.playwright.support.FailureEvidenceListener;
import org.citrusframework.playwright.support.FixtureServer;
import org.citrusframework.playwright.support.PlaywrightRuntime;
import org.citrusframework.spi.SimpleReferenceResolver;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;

@Isolated("Mutates global citrus.logger.mask.keywords system property")
class PlaywrightBrowserIT {

    private static String originalMaskKeywords;
    private static boolean maskKeywordsConfigured;

    @BeforeAll
    static void configureMaskKeywords() {
        if (!PlaywrightRuntime.chromiumAvailable()) {
            Assumptions.abort("Chromium is not installed for Playwright - install it with -Pplaywright-runtimes");
        }

        originalMaskKeywords = System.getProperty("citrus.logger.mask.keywords");
        maskKeywordsConfigured = true;
        System.setProperty("citrus.logger.mask.keywords",
                "authorization,x-api-key,api-key,password,token,api_key,apikey,secret,auth,cookie");
    }

    @AfterAll
    static void restoreMaskKeywords() {
        if (!maskKeywordsConfigured) {
            return;
        }

        if (originalMaskKeywords == null) {
            System.clearProperty("citrus.logger.mask.keywords");
        } else {
            System.setProperty("citrus.logger.mask.keywords", originalMaskKeywords);
        }
    }

    @Test
    void shouldDriveChromiumAgainstLocalFixture() throws Exception {
        URL fixture = getClass().getResource("/fixtures/form.html");
        Path screenshot = Path.of("target", "playwright-browser-it.png");
        Files.deleteIfExists(screenshot);

        PlaywrightBrowser browser = PlaywrightEndpoints.playwright()
                .browser()
                .browserType("chromium")
                .headless(true)
                .defaultTimeout(5000)
                .build();

        TestContext context = new TestContext();
        try {
            playwright().browser(browser).start().build().execute(context);
            playwright().browser(browser).open().url(fixture.toExternalForm()).build().execute(context);
            playwright().browser(browser).fill().locator("#name").value("Citrus").build().execute(context);
            playwright().browser(browser).select().locator("#status").value("ready").build().execute(context);
            playwright().browser(browser).click().locator("#submit").build().execute(context);
            playwright().browser(browser).waitFor().locator("#message").visible().build().execute(context);
            playwright().browser(browser).verify().locator("#message").text("Citrus:ready").build().execute(context);
            assertThrows(ValidationException.class,
                    () -> playwright().browser(browser).verify().locator("#message").text("Citrus:waiting").build().execute(context));
            playwright().browser(browser).verify().locator("#message").attribute("data-state", "ready").build().execute(context);
            playwright().browser(browser).extract().locator("#message").text().variable("message").build().execute(context);
            playwright().browser(browser).javascript().script("() => document.title").variable("title").build().execute(context);
            playwright().browser(browser).screenshot().path(screenshot.toString()).variable("screenshot").build().execute(context);

            assertEquals("Citrus:ready", context.getVariable("message"));
            assertEquals("Citrus Playwright Fixture", context.getVariable("title"));
            assertEquals(screenshot.toString(), context.getVariable("screenshot"));
            assertTrue(Files.exists(screenshot));
        } finally {
            browser.stop();
        }
    }

    @Test
    void shouldUsePhaseTwoBrowserNativeActionsAgainstLocalFixture() throws Exception {
        Path download = Path.of("target", "playwright", "phase2.txt");
        Path trace = Path.of("target", "playwright", "phase2-trace.zip");
        Path pdf = Path.of("target", "playwright", "phase2.pdf");
        Files.deleteIfExists(download);
        Files.deleteIfExists(trace);
        Files.deleteIfExists(pdf);

        PlaywrightBrowser browser = PlaywrightEndpoints.playwright()
                .browser()
                .browserType("chromium")
                .headless(true)
                .defaultTimeout(5000)
                .consoleMessageLimit(5)
                .networkRecordLimit(10)
                .build();

        TestContext context = new TestContext();
        try (FixtureServer server = FixtureServer.start()) {
            String fixtureUrl = server.url("/phase2.html");
            playwright().browser(browser).start().build().execute(context);
            playwright().browser(browser).console().capture().build().execute(context);
            playwright().browser(browser).network().capture().build().execute(context);
            playwright().browser(browser).open().url(fixtureUrl).build().execute(context);

            playwright().browser(browser).context().newContext("admin").build().execute(context);
            playwright().browser(browser).page().newPage("admin-page").build().execute(context);
            playwright().browser(browser).open().url(fixtureUrl).build().execute(context);
            playwright().browser(browser).page().switchTo("default").build().execute(context);

            playwright().browser(browser).frame().frame("#details-frame").verifyText("#frame-label", "Frame Ready").build().execute(context);
            playwright().browser(browser).frame().frame("#details-frame").fill("#frame-input").value("inside").build().execute(context);
            playwright().browser(browser).dialog().accept().message("phase-2-dialog").triggerScript("() => alert('phase-2-dialog')").build().execute(context);
            playwright().browser(browser).download().click("#download").saveAs(download.toString()).pathVariable("download").filenameVariable("downloadName").build().execute(context);

            playwright().browser(browser).cookies().add(CookieSpec.cookie("phase", "two").url(fixtureUrl)).build().execute(context);
            playwright().browser(browser).cookies().read("phase").variable("phaseCookie").build().execute(context);
            playwright().browser(browser).cookies().verify("phase", "two").build().execute(context);
            playwright().browser(browser).storage().local().set("theme", "dark").build().execute(context);
            playwright().browser(browser).storage().local().read("theme").variable("theme").build().execute(context);
            playwright().browser(browser).storage().local().verify("theme", "dark").build().execute(context);
            playwright().browser(browser).permissions().grant("geolocation").build().execute(context);
            playwright().browser(browser).emulate().viewport(640, 480).geolocation(47.0105, 28.8638).build().execute(context);

            playwright().browser(browser).javascript().script("() => console.log('phase-2-console')").build().execute(context);
            playwright().browser(browser).console().verifyContains("phase-2-console").build().execute(context);
            playwright().browser(browser).network().verifyUrlContains("phase2.html").build().execute(context);

            playwright().browser(browser).tracing().start().screenshots(true).snapshots(true).build().execute(context);
            playwright().browser(browser).pageObject().type(Phase2Page.class).execute("mark").build().execute(context);
            playwright().browser(browser).verify().locator("#page-object-target").text("done").build().execute(context);
            playwright().browser(browser).tracing().stop().path(trace.toString()).variable("trace").build().execute(context);
            playwright().browser(browser).pdf().path(pdf.toString()).variable("pdf").build().execute(context);

            assertEquals("two", context.getVariable("phaseCookie"));
            assertEquals("dark", context.getVariable("theme"));
            assertEquals(download.toString(), context.getVariable("download"));
            assertEquals("phase2.txt", context.getVariable("downloadName"));
            assertEquals(trace.toString(), context.getVariable("trace"));
            assertEquals(pdf.toString(), context.getVariable("pdf"));
            assertTrue(Files.exists(download));
            assertTrue(Files.exists(trace));
            assertTrue(Files.exists(pdf));
        } finally {
            browser.stop();
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldUsePhaseThreeHardeningActionsAgainstLocalFixture() throws Exception {
        Path download = Path.of("target", "playwright", "phase3.txt");
        Files.deleteIfExists(download);

        PlaywrightBrowser browser = PlaywrightEndpoints.playwright()
                .browser()
                .browserType("chromium")
                .headless(true)
                .defaultTimeout(5000)
                .consoleMessageLimit(10)
                .networkRecordLimit(20)
                .secretPatterns("secret-token")
                .build();

        TestContext context = new TestContext();
        try (FixtureServer server = FixtureServer.start()) {
            String fixtureUrl = server.url("/phase3.html");
            PlaywrightTarget card = PlaywrightTarget.the("phase 3 card").locatedBy("#card");

            playwright().browser(browser).start().build().execute(context);
            playwright().browser(browser).console().capture().build().execute(context);
            playwright().browser(browser).network().capture().build().execute(context);
            playwright().browser(browser).open().url(fixtureUrl).build().execute(context);

            playwright().browser(browser).verify().locator(card).cssValue("display", "block").build().execute(context);
            playwright().browser(browser).verify().locator("#card").cssClass("primary").build().execute(context);
            playwright().browser(browser).verify().locator("#card").innerHtmlContains("Inner").build().execute(context);
            playwright().browser(browser).verify().locator("#main").ariaSnapshotContains("Launch").build().execute(context);
            playwright().browser(browser).verify().locator("#status").optionText("Ready").build().execute(context);
            playwright().browser(browser).verify().locator("#status").selectedOptionText("Ready").build().execute(context);
            playwright().browser(browser).verify().locator("#status").selectedOptionValue("ready").build().execute(context);

            playwright().browser(browser).extract().locator(".row").allTextContents().variable("rows").build().execute(context);
            playwright().browser(browser).extract().locator("#card").boundingBox().variable("box").build().execute(context);
            playwright().browser(browser).extract().locator("#card").cssClasses().variable("classes").build().execute(context);
            playwright().browser(browser).extract().locator("#card").cssValue("display").variable("display").build().execute(context);
            playwright().browser(browser).extract().locator("#card").innerHtml().variable("innerHtml").build().execute(context);
            playwright().browser(browser).extract().locator("#card").outerHtml().variable("outerHtml").build().execute(context);
            playwright().browser(browser).extract().locator("#status").optionTexts().variable("optionTexts").build().execute(context);
            playwright().browser(browser).extract().locator("#status").selectedOptionText().variable("selectedText").build().execute(context);
            playwright().browser(browser).extract().locator("#status").selectedOptionValues().variable("selectedValues").build().execute(context);
            playwright().browser(browser).extract().locator("#main").ariaSnapshot().variable("aria").build().execute(context);

            playwright().browser(browser).verify().pageCount(1).build().execute(context);
            playwright().browser(browser).extract().pageCount().variable("pageCount").build().execute(context);
            playwright().browser(browser).verify().frameContentContains("#phase3-frame", "Frame Ready").build().execute(context);
            playwright().browser(browser).extract().frameContent("#phase3-frame").variable("frame").build().execute(context);

            playwright().browser(browser).verify().storageLocal("phase3", "local-ready").build().execute(context);
            playwright().browser(browser).extract().storageLocal("phase3").variable("localStorage").build().execute(context);
            playwright().browser(browser).verify().storageSession("phase3-session", "session-ready").build().execute(context);
            playwright().browser(browser).extract().storageSession("phase3-session").variable("sessionStorage").build().execute(context);
            playwright().browser(browser).cookies().add(CookieSpec.cookie("phase3-cookie", "cookie-ready").url(fixtureUrl)).build().execute(context);
            playwright().browser(browser).verify().cookie("phase3-cookie", "cookie-ready").build().execute(context);
            playwright().browser(browser).extract().cookie("phase3-cookie").variable("cookie").build().execute(context);

            playwright().browser(browser).javascript().script("() => console.log('phase-3-verify')").build().execute(context);
            playwright().browser(browser).verify().consoleContains("phase-3-verify").build().execute(context);
            playwright().browser(browser).extract().consoleMessages().variable("console").build().execute(context);
            playwright().browser(browser).verify().networkUrlContains("phase3-api.json").build().execute(context);
            playwright().browser(browser).extract().networkRecords().variable("network").build().execute(context);

            playwright().browser(browser).network()
                    .route("**/phase3-mocked.json**")
                    .fulfillJson("{\"mocked\":\"secret-token\"}")
                    .header("x-mocked", "true")
                    .build()
                    .execute(context);
            playwright().browser(browser).network()
                    .waitForResponse()
                    .urlContains("phase3-mocked.json")
                    .status(200)
                    .includeBody()
                    .triggerScript("() => fetch('/phase3-mocked.json?token=secret-token').then(response => response.text())")
                    .variable("mockResponse")
                    .build()
                    .execute(context);
            playwright().browser(browser).network().unroute("**/phase3-mocked.json**").build().execute(context);

            playwright().browser(browser).download().click("#download").saveAs(download.toString()).build().execute(context);
            playwright().browser(browser).verify().downloadFilename("phase3.txt").build().execute(context);
            playwright().browser(browser).extract().downloadMetadata().variable("download").build().execute(context);

            assertEquals(List.of("One", "Two"), context.getVariableObject("rows"));
            BoundingBoxResult box = (BoundingBoxResult) context.getVariableObject("box");
            assertTrue(box.width() > 0);
            assertTrue(box.height() > 0);
            assertEquals(List.of("primary", "selected"), context.getVariableObject("classes"));
            assertEquals("block", context.getVariableObject("display"));
            assertTrue(String.valueOf(context.getVariableObject("innerHtml")).contains("Inner"));
            assertTrue(!String.valueOf(context.getVariableObject("innerHtml")).contains("secret-token"));
            assertTrue(String.valueOf(context.getVariableObject("innerHtml")).contains(SecretPatternRedactor.MASK));
            assertTrue(String.valueOf(context.getVariableObject("outerHtml")).contains("primary"));
            assertEquals(List.of("Waiting", "Ready"), context.getVariableObject("optionTexts"));
            assertEquals("Ready", context.getVariableObject("selectedText"));
            assertEquals(List.of("ready"), context.getVariableObject("selectedValues"));
            assertTrue(String.valueOf(context.getVariableObject("aria")).contains("Launch"));
            assertEquals(1, context.getVariableObject("pageCount"));
            assertTrue(String.valueOf(context.getVariableObject("frame")).contains("Frame Ready"));
            assertEquals("local-ready", context.getVariableObject("localStorage"));
            assertEquals("session-ready", context.getVariableObject("sessionStorage"));
            assertEquals(SecretPatternRedactor.MASK, context.getVariableObject("cookie"));

            List<ConsoleMessageRecord> consoleMessages = (List<ConsoleMessageRecord>) context.getVariableObject("console");
            assertTrue(consoleMessages.stream().anyMatch(message -> message.text().contains("phase-3-verify")));
            List<NetworkRecord> networkRecords = (List<NetworkRecord>) context.getVariableObject("network");
            String networkReport = networkRecords.stream().map(NetworkRecord::format).collect(Collectors.joining("\n"));
            assertTrue(networkReport.contains("phase3-api.json"));
            assertTrue(networkReport.contains("token=" + SecretPatternRedactor.MASK + ""));
            assertTrue(!networkReport.contains("secret-token"));
            NetworkResponseResult mockResponse = (NetworkResponseResult) context.getVariableObject("mockResponse");
            assertEquals(200, mockResponse.status());
            assertTrue(mockResponse.ok());
            assertTrue(mockResponse.url().contains("token=" + SecretPatternRedactor.MASK + ""));
            assertTrue(mockResponse.body().contains(SecretPatternRedactor.MASK));
            assertTrue(!mockResponse.body().contains("secret-token"));

            DownloadMetadata metadata = (DownloadMetadata) context.getVariableObject("download");
            assertEquals(download.toString(), metadata.path());
            assertEquals("phase3.txt", metadata.suggestedFilename());
            assertTrue(Files.exists(download));
        } finally {
            browser.stop();
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldResolveBrowserImplicitlyThroughAmbientScope() throws Exception {
        TestContext context = new TestContext();
        try (FixtureServer server = FixtureServer.start()) {
            String fixtureUrl = server.url("/phase3.html");

            playwright().start()
                    .browserType("chromium")
                    .defaultTimeout(5000)
                    .consoleMessageLimit(10)
                    .networkRecordLimit(20)
                    .build()
                    .execute(context);

            playwright().console().capture().build().execute(context);
            playwright().network().capture().build().execute(context);
            playwright().open().url(fixtureUrl).build().execute(context);

            playwright().extract().pageCount().variable("pageCount").build().execute(context);
            playwright().extract().frameContent("#phase3-frame").variable("frameHtml").build().execute(context);
            playwright().extract().storageLocal("phase3").variable("theme").build().execute(context);

            playwright().cookies().add(CookieSpec.cookie("session", "implicit-ready").url(fixtureUrl)).build().execute(context);
            playwright().extract().cookie("session").variable("session").build().execute(context);

            playwright().javascript().script("() => console.log('implicit-verify')").build().execute(context);
            playwright().verify().consoleContains("implicit-verify").build().execute(context);
            playwright().extract().consoleMessages().variable("console").build().execute(context);
            playwright().verify().networkUrlContains("phase3-api.json").build().execute(context);
            playwright().extract().networkRecords().variable("network").build().execute(context);

            assertEquals(1, context.getVariableObject("pageCount"));
            assertTrue(String.valueOf(context.getVariableObject("frameHtml")).contains("Frame Ready"));
            assertEquals("local-ready", context.getVariableObject("theme"));
            assertEquals("implicit-ready", context.getVariableObject("session"));

            List<ConsoleMessageRecord> consoleMessages = (List<ConsoleMessageRecord>) context.getVariableObject("console");
            assertTrue(consoleMessages.stream().anyMatch(message -> message.text().contains("implicit-verify")));
            List<NetworkRecord> networkRecords = (List<NetworkRecord>) context.getVariableObject("network");
            String networkReport = networkRecords.stream().map(NetworkRecord::format).collect(Collectors.joining("\n"));
            assertTrue(networkReport.contains("phase3-api.json"));

            playwright().stop().build().execute(context);
            assertTrue(org.citrusframework.playwright.support.PlaywrightBrowserScope.current(context).isEmpty(),
                    "ambient binding must be cleared after implicit stop");
        }
    }

    @Test
    void shouldSupportNamedEndpointBindingAndScopedBlock() throws Exception {
        PlaywrightBrowser browser = PlaywrightEndpoints.playwright()
                .browser()
                .browserType("chromium")
                .headless(true)
                .defaultTimeout(5000)
                .build();

        TestContext context = new TestContext();
        SimpleReferenceResolver referenceResolver = new SimpleReferenceResolver();
        referenceResolver.bind("it-browser", browser);
        context.setReferenceResolver(referenceResolver);

        try {
            try (FixtureServer server = FixtureServer.start()) {
                String fixtureUrl = server.url("/phase3.html");

                playwright().browser("it-browser").open().url(fixtureUrl).build().execute(context);
                playwright().browser("it-browser").extract().pageCount().variable("pageCount").build().execute(context);

                Sequence block = playwright().with(browser, pw -> {
                    pw.extract().frameContent("#phase3-frame").variable("frameHtml");
                    pw.extract().storageLocal("phase3").variable("theme");
                });
                block.execute(context);

                assertEquals(1, context.getVariableObject("pageCount"));
                assertTrue(String.valueOf(context.getVariableObject("frameHtml")).contains("Frame Ready"));
                assertEquals("local-ready", context.getVariableObject("theme"));
            }
        } finally {
            browser.stop();
        }
    }

    @Test
    void shouldCaptureFailureEvidenceForActiveBrowser() throws Exception {
        Path artifactDirectory = Path.of("target", "playwright", "evidence-it");

        PlaywrightBrowser browser = PlaywrightEndpoints.playwright()
                .browser()
                .browserType("chromium")
                .headless(true)
                .defaultTimeout(5000)
                .artifactDirectory(artifactDirectory)
                .secretPatterns("secret-token")
                .captureFailureScreenshot(true)
                .captureFailurePageSource(true)
                .captureFailureTrace(true)
                .captureFailureConsoleMessages(true)
                .captureFailureNetworkRequests(true)
                .captureFailureSummary(true)
                .build();

        TestContext context = new TestContext();
        try (FixtureServer server = FixtureServer.start()) {
            String fixtureUrl = server.url("/phase2.html");
            playwright().browser(browser).start().build().execute(context);
            playwright().browser(browser).console().capture().build().execute(context);
            playwright().browser(browser).network().capture().build().execute(context);
            playwright().browser(browser).tracing().start().screenshots(true).snapshots(true).build().execute(context);
            playwright().browser(browser).open().url(fixtureUrl).build().execute(context);
            playwright().browser(browser).javascript().script("() => console.log('evidence-console')").build().execute(context);
            playwright().browser(browser).javascript().script("() => console.error('evidence-error token=secret-token')").build().execute(context);
            playwright().browser(browser).network()
                    .waitForResponse()
                    .urlContains("missing-token")
                    .triggerScript("() => fetch('/missing-token?token=secret-token')")
                    .build()
                    .execute(context);

            new FailureEvidenceListener().onTestFailure(testCase("phase 2 evidence"), new AssertionError("expected"));

            Path evidenceDirectory = artifactDirectory.resolve("phase_2_evidence");
            assertTrue(Files.exists(evidenceDirectory.resolve("failure.png")));
            assertTrue(Files.exists(evidenceDirectory.resolve("page.html")));
            assertTrue(Files.exists(evidenceDirectory.resolve("console.log")));
            assertTrue(Files.exists(evidenceDirectory.resolve("network.log")));
            assertTrue(Files.exists(evidenceDirectory.resolve("failure-summary.md")));
            assertTrue(Files.exists(evidenceDirectory.resolve("trace.zip")));
            String summary = Files.readString(evidenceDirectory.resolve("failure-summary.md"));
            assertTrue(summary.contains("evidence-error token=" + SecretPatternRedactor.MASK + ""));
            assertTrue(summary.contains("Failed Or Error Network Records"));
            assertTrue(summary.contains("token=" + SecretPatternRedactor.MASK + ""));
            assertTrue(!summary.contains("secret-token"));
        } finally {
            browser.stop();
        }
    }

    public static class Phase2Page {
        public void mark(Page page) {
            page.locator("#page-object-target").evaluate("element => element.textContent = 'done'");
        }
    }

    private TestCase testCase(String name) {
        return (TestCase) Proxy.newProxyInstance(TestCase.class.getClassLoader(), new Class<?>[]{TestCase.class},
                (proxy, method, args) -> "getName".equals(method.getName()) ? name : null);
    }

    @Test
    void shouldDriveUpliftCapabilitiesAgainstLocalFixture() throws Exception {
        URL fixture = getClass().getResource("/fixtures/uplift.html");
        Path screenshot = Path.of("target", "playwright-uplift-it.webp");
        Path har = Path.of("target", "playwright", "uplift-it.har");
        Path video = Path.of("target", "playwright", "uplift-it.webm");
        Files.deleteIfExists(screenshot);
        Files.deleteIfExists(har);

        PlaywrightBrowser browser = PlaywrightEndpoints.playwright()
                .browser()
                .browserType("chromium")
                .headless(true)
                .defaultTimeout(5000)
                .build();

        TestContext context = new TestContext();
        try {
            playwright().browser(browser).start().build().execute(context);

            // HAR and screencast recording bracket the run.
            playwright().browser(browser).tracing().startHar(har.toString()).build().execute(context);
            playwright().browser(browser).screencast().start(video.toString()).build().execute(context);

            playwright().browser(browser).open().url(fixture.toExternalForm()).build().execute(context);

            // G5 - a plain ".action" locator is ambiguous; visible() disambiguates it.
            playwright().browser(browser).click()
                    .locator(org.citrusframework.playwright.model.LocatorSpec.css(".action").visible())
                    .build().execute(context);
            playwright().browser(browser).verify().locator("#saved").text("visible-save-clicked")
                    .build().execute(context);

            // G2 - acting without scrolling the element into view.
            playwright().browser(browser).click()
                    .locator(org.citrusframework.playwright.model.LocatorSpec.css(".action").visible())
                    .scroll("none").build().execute(context);

            // G3 - wait until the grid has grown, using a predicate on the element.
            playwright().browser(browser).waitFor().locator("#grid")
                    .function("el => el.children.length >= 5").build().execute(context);

            // G4 - reach into the nested frame without naming the iframe.
            playwright().browser(browser).frame().click("#framed").build().execute(context);
            playwright().browser(browser).frame().verifyText("#framed-result", "framed-clicked")
                    .build().execute(context);

            // G12 - synthetic drag and drop of clipboard data onto the upload zone.
            playwright().browser(browser).drop().locator("#dropzone")
                    .data("text/plain", "dropped-payload").build().execute(context);
            playwright().browser(browser).verify().locator("#dropped").text("dropped-payload")
                    .build().execute(context);

            // G1 - WebP screenshot inferred from the file extension.
            playwright().browser(browser).screenshot().path(screenshot.toString()).quality(60)
                    .build().execute(context);

            playwright().browser(browser).screencast().stop().build().execute(context);
            playwright().browser(browser).tracing().stopHar().build().execute(context);

            assertTrue(Files.exists(screenshot), "expected a WebP screenshot at " + screenshot);
            assertTrue(Files.size(screenshot) > 0, "expected a non-empty WebP screenshot");
            assertTrue(isWebp(screenshot), "expected WebP magic bytes in " + screenshot);
            assertTrue(Files.exists(har), "expected a HAR recording at " + har);
        } finally {
            playwright().browser(browser).stop().build().execute(context);
        }
    }

    @Test
    void shouldDriveDriverOptionCoverageAgainstLocalFixture() throws Exception {
        URL fixture = getClass().getResource("/fixtures/option-coverage.html");
        Path video = Path.of("target", "playwright", "option-coverage.webm");
        Path state = Path.of("target", "playwright", "option-coverage-state.json");
        Files.deleteIfExists(video);
        Files.deleteIfExists(state);

        PlaywrightBrowser browser = PlaywrightEndpoints.playwright()
                .browser()
                .browserType("chromium")
                .headless(true)
                .defaultTimeout(5000)
                .build();

        TestContext context = new TestContext();
        try (FixtureServer server = FixtureServer.start()) {
            String apiUrl = server.url("/phase2.html");
            int apiPort = java.net.URI.create(apiUrl).getPort();

            playwright().browser(browser).start().build().execute(context);
            playwright().browser(browser).open().url(fixture.toExternalForm()).build().execute(context);

            // O1 - the fixture throws an uncaught error on load.
            playwright().browser(browser).console().verifyPageErrorsContain("option-coverage-boom")
                    .build().execute(context);
            playwright().browser(browser).console().pageErrors().variable("pageErrors")
                    .build().execute(context);
            assertTrue(context.getVariable("pageErrors").contains("option-coverage-boom"));
            assertThrows(ValidationException.class,
                    () -> playwright().browser(browser).console().verifyNoPageErrors()
                            .build().execute(context));

            // O2 + O3 - sized recording with a custom overlay.
            playwright().browser(browser).screencast().start(video.toString()).size(640, 480)
                    .build().execute(context);
            playwright().browser(browser).screencast().showOverlay("<div>Option coverage</div>")
                    .build().execute(context);
            playwright().browser(browser).screencast().showOverlays().build().execute(context);
            playwright().browser(browser).screencast().hideOverlays().build().execute(context);
            playwright().browser(browser).screencast().stop().build().execute(context);
            assertTrue(Files.exists(video), "expected a screencast recording at " + video);

            // O4 - storage state with the full browser state flags.
            playwright().browser(browser).storage().saveState(state.toString()).indexedDB(true)
                    .credentials(true).build().execute(context);
            assertTrue(Files.exists(state), "expected a storage state at " + state);

            // O9 - page-side response carries server address; plain HTTP has no TLS details.
            playwright().browser(browser).open().url(apiUrl).build().execute(context);
            playwright().browser(browser).network().waitForResponse().urlContains("/phase2.html")
                    .triggerScript("() => fetch('/phase2.html')")
                    .verifyServerAddress("127.0.0.1", apiPort)
                    .variable("resp").build().execute(context);

            NetworkResponseResult result =
                    (NetworkResponseResult) context.getVariableObject("resp");
            assertEquals("127.0.0.1", result.serverIp());
            assertEquals(Integer.valueOf(apiPort), result.serverPort());
            assertEquals(null, result.tlsProtocol());
        } finally {
            playwright().browser(browser).stop().build().execute(context);
        }
    }

    @Test
    void shouldEnforcePlaywright165BrowserSemanticsAgainstLocalFixture() {
        URL fixture = getClass().getResource("/fixtures/playwright-165.html");

        PlaywrightBrowser browser = PlaywrightEndpoints.playwright()
                .browser()
                .browserType("chromium")
                .headless(true)
                .defaultTimeout(600)
                .build();

        TestContext context = new TestContext();
        try {
            playwright().browser(browser).start().build().execute(context);
            playwright().browser(browser).open().url(fixture.toExternalForm()).build().execute(context);

            // 1. Inert actionability (Playwright 1.65): pointer and input actions time out while subtree is inert.
            TimeoutError clickInertError = assertThrows(TimeoutError.class,
                    () -> playwright().browser(browser).click().locator("#inert-btn").build().execute(context));
            assertTrue(clickInertError.getMessage().contains("inert"), clickInertError.getMessage());

            TimeoutError fillInertError = assertThrows(TimeoutError.class,
                    () -> playwright().browser(browser).fill().locator("#inert-input").value("blocked").build().execute(context));
            assertTrue(fillInertError.getMessage().contains("inert"), fillInertError.getMessage());

            playwright().browser(browser).javascript()
                    .script("() => document.getElementById('inert-zone').removeAttribute('inert')")
                    .build().execute(context);
            playwright().browser(browser).fill().locator("#inert-input").value("active").build().execute(context);
            playwright().browser(browser).check().locator("#inert-check").build().execute(context);
            playwright().browser(browser).select().locator("#inert-select").value("beta").build().execute(context);
            playwright().browser(browser).click().locator("#inert-btn").build().execute(context);
            playwright().browser(browser).verify().locator("#inert-result").text("active:true:beta").build().execute(context);

            // 2. RFC 6265 URL-scoped cookie matching (Playwright 1.65): host-only and strict path boundaries.
            playwright().browser(browser).cookies()
                    .add(CookieSpec.cookie("hostOnly", "v1").url("https://example.com/"))
                    .build().execute(context);
            playwright().browser(browser).cookies()
                    .verify("hostOnly", "v1").url("https://example.com/dashboard")
                    .build().execute(context);
            assertThrows(CitrusRuntimeException.class,
                    () -> playwright().browser(browser).cookies()
                            .verify("hostOnly", "v1").url("https://sub.example.com/dashboard")
                            .build().execute(context));

            playwright().browser(browser).cookies()
                    .add(CookieSpec.cookie("apiScoped", "v2").domain("example.com").path("/api"))
                    .build().execute(context);
            playwright().browser(browser).cookies()
                    .verify("apiScoped", "v2").url("https://example.com/api/orders")
                    .build().execute(context);
            assertThrows(CitrusRuntimeException.class,
                    () -> playwright().browser(browser).cookies()
                            .verify("apiScoped", "v2").url("https://example.com/apiv2")
                            .build().execute(context));

            playwright().browser(browser).cookies()
                    .add(CookieSpec.cookie("slashScoped", "v3").domain("example.com").path("/foo/"))
                    .build().execute(context);
            playwright().browser(browser).cookies()
                    .verify("slashScoped", "v3").url("https://example.com/foo/bar")
                    .build().execute(context);
            assertThrows(CitrusRuntimeException.class,
                    () -> playwright().browser(browser).cookies()
                            .verify("slashScoped", "v3").url("https://example.com/foo")
                            .build().execute(context));

            // 3. ARIA snapshots & accessible names (Playwright 1.65): unrendered text is excluded.
            playwright().browser(browser).verify().locator("#aria-panel")
                    .ariaSnapshotContains("Details Summary").build().execute(context);
            playwright().browser(browser).verify().locator("#aria-panel")
                    .ariaSnapshotContains("Rendered Label").build().execute(context);
            assertThrows(ValidationException.class,
                    () -> playwright().browser(browser).verify().locator("#aria-panel")
                            .ariaSnapshotContains("Direct closed text").build().execute(context));
            assertThrows(ValidationException.class,
                    () -> playwright().browser(browser).verify().locator("#aria-panel")
                            .ariaSnapshotContains("Unslotted Fallback").build().execute(context));

            playwright().browser(browser).verify()
                    .locator(LocatorSpec.role("button").name("Unslotted Fallback"))
                    .count(0).build().execute(context);
            playwright().browser(browser).click()
                    .locator(LocatorSpec.role("button").name("Rendered Label"))
                    .build().execute(context);
            playwright().browser(browser).verify().locator("#shadow-clicked").text("shadow-clicked").build().execute(context);

            playwright().browser(browser).javascript()
                    .script("() => { document.getElementById('closed-details').open = true; }")
                    .build().execute(context);
            playwright().browser(browser).verify().locator("#aria-panel")
                    .ariaSnapshotContains("Direct closed text").build().execute(context);

            // 4. Hidden iframes (Playwright 1.64+): elements inside visibility:hidden / display:none iframes are considered hidden.
            LocatorSpec frameButtonSpec = LocatorSpec.raw(page -> page.frameLocator("#hidden-frame").locator("#secret-frame-btn"));
            Locator frameButton = browser.getCurrentPage().frameLocator("#hidden-frame").locator("#secret-frame-btn");
            assertFalse(frameButton.isVisible());
            playwright().browser(browser).verify().locator(frameButtonSpec).hidden().build().execute(context);

            CitrusRuntimeException hiddenFrameError = assertThrows(CitrusRuntimeException.class,
                    () -> playwright().browser(browser).frame().frame("#hidden-frame")
                            .click("#secret-frame-btn").build().execute(context));
            assertTrue(hiddenFrameError.getCause() instanceof TimeoutError,
                    String.valueOf(hiddenFrameError.getCause()));

            playwright().browser(browser).javascript()
                    .script("() => { const f = document.getElementById('hidden-frame'); f.style.visibility = 'visible'; f.style.display = 'none'; }")
                    .build().execute(context);
            assertFalse(frameButton.isVisible());
            playwright().browser(browser).verify().locator(frameButtonSpec).hidden().build().execute(context);

            playwright().browser(browser).javascript()
                    .script("() => { document.getElementById('hidden-frame').style.display = 'block'; }")
                    .build().execute(context);
            assertTrue(frameButton.isVisible());
            playwright().browser(browser).verify().locator(frameButtonSpec).visible().build().execute(context);
            playwright().browser(browser).frame().frame("#hidden-frame")
                    .click("#secret-frame-btn").build().execute(context);
            playwright().browser(browser).frame().frame("#hidden-frame")
                    .verifyText("#frame-out", "frame-clicked").build().execute(context);
        } finally {
            playwright().browser(browser).stop().build().execute(context);
        }
    }

    @Test
    void shouldExerciseRefLocatorsWithinScopingAiAriaSnapshotsAndNormalizedLocators() {
        URL fixture = getClass().getResource("/fixtures/playwright-locators-ai.html");

        PlaywrightBrowser browser = PlaywrightEndpoints.playwright()
                .browser()
                .browserType("chromium")
                .headless(true)
                .defaultTimeout(5000)
                .build();

        TestContext context = new TestContext();
        try {
            playwright().browser(browser).start().build().execute(context);
            playwright().browser(browser).open().url(fixture.toExternalForm()).build().execute(context);

            // 1. Page-level and locator-level AI ARIA snapshots emit [ref=eN] identifiers and boxes.
            playwright().browser(browser).extract()
                    .ariaSnapshot("ai").boxes(true).depth(4)
                    .variable("pageAiTree")
                    .build().execute(context);
            String pageAiTree = context.getVariable("pageAiTree");
            assertTrue(pageAiTree.contains("Alpha Tier"), pageAiTree);
            assertTrue(pageAiTree.contains("Beta Tier"), pageAiTree);
            assertTrue(pageAiTree.contains("[ref="), pageAiTree);

            playwright().browser(browser).extract()
                    .locator("#card-beta")
                    .ariaSnapshot("ai")
                    .variable("betaCardTree")
                    .build().execute(context);
            String betaCardTree = context.getVariable("betaCardTree");
            Matcher betaRefMatcher = Pattern.compile("button \"Apply\"[^\n]*\\[ref=(e\\d+)]").matcher(betaCardTree);
            assertTrue(betaRefMatcher.find(), betaCardTree);
            context.setVariable("betaApplyRef", betaRefMatcher.group(1));

            // 2. LocatorSpec.ref("${betaApplyRef}") clicks via Page.getByRef(...) and normalizes to Java code.
            playwright().browser(browser).click()
                    .locator(LocatorSpec.ref("${betaApplyRef}"))
                    .build().execute(context);
            playwright().browser(browser).verify()
                    .locator("#apply-output").text("beta")
                    .build().execute(context);

            playwright().browser(browser).extract()
                    .locator(LocatorSpec.ref("${betaApplyRef}"))
                    .normalizedLocator()
                    .variable("betaNormalized")
                    .build().execute(context);
            String betaNormalized = context.getVariable("betaNormalized");
            assertTrue(betaNormalized.contains("getByRole") && betaNormalized.contains("Apply"), betaNormalized);

            // 3. LocatorSpec.within(...) scopes matches to a container before clicking.
            playwright().browser(browser).click()
                    .locator(LocatorSpec.role("button").name("Apply").within(LocatorSpec.css("#card-alpha")))
                    .build().execute(context);
            playwright().browser(browser).verify()
                    .locator("#apply-output").text("alpha")
                    .build().execute(context);

            playwright().browser(browser).click()
                    .locator(LocatorSpec.role("button").name("Apply").within(LocatorSpec.css("#card-beta")))
                    .build().execute(context);
            playwright().browser(browser).verify()
                    .locator("#apply-output").text("beta")
                    .build().execute(context);

            // 4. REF locator inside an iframe resolves via FrameLocator("aria-ref=...").
            playwright().browser(browser).extract()
                    .locator(LocatorSpec.raw(page -> page.frameLocator("#ref-frame").locator("body")))
                    .ariaSnapshot("ai")
                    .variable("frameAiTree")
                    .build().execute(context);
            String frameAiTree = context.getVariable("frameAiTree");
            Matcher frameRefMatcher = Pattern.compile("button \"Frame Confirm\"[^\n]*\\[ref=([a-z0-9]+)]").matcher(frameAiTree);
            assertTrue(frameRefMatcher.find(), frameAiTree);
            context.setVariable("frameConfirmRef", frameRefMatcher.group(1));

            playwright().browser(browser).frame().frame("#ref-frame")
                    .click(LocatorSpec.ref("${frameConfirmRef}"))
                    .build().execute(context);
            playwright().browser(browser).frame().frame("#ref-frame")
                    .verifyText("#frame-ref-out", "frame-confirmed")
                    .build().execute(context);
        } finally {
            playwright().browser(browser).stop().build().execute(context);
        }
    }

    @Test
    void shouldDriveScreencastStylingCredentialsSignCountShadowPageSourceAndFilteredCookieClear() throws Exception {
        URL fixture = getClass().getResource("/fixtures/playwright-165.html");
        Path video = Path.of("target", "playwright", "screencast-styled.webm");
        Files.deleteIfExists(video);

        PlaywrightBrowser browser = PlaywrightEndpoints.playwright()
                .browser()
                .browserType("chromium")
                .headless(true)
                .defaultTimeout(5000)
                .secretPatterns("Direct closed text")
                .build();

        TestContext context = new TestContext();
        try {
            playwright().browser(browser).start().build().execute(context);
            playwright().browser(browser).open().url(fixture.toExternalForm()).build().execute(context);

            // 1. Screencast start with fps + showActions with style & cursor.
            playwright().browser(browser).screencast()
                    .start(video.toString())
                    .size(640, 480)
                    .fps(15)
                    .variable("recordedVideoPath")
                    .build().execute(context);
            playwright().browser(browser).screencast()
                    .showActions()
                    .position("top-right")
                    .cursor("pointer")
                    .stylePoint("background: #ff3b30")
                    .styleHighlight("border: 2px solid #007aff")
                    .styleTitle("font-size: 18px; color: #ffffff")
                    .build().execute(context);
            playwright().browser(browser).click()
                    .locator(LocatorSpec.role("button").name("Rendered Label"))
                    .build().execute(context);
            playwright().browser(browser).screencast().hideActions().build().execute(context);
            playwright().browser(browser).screencast().stop().build().execute(context);

            assertEquals(video.toString(), context.getVariable("recordedVideoPath"));
            assertTrue(Files.exists(video), "expected a screencast recording at " + video);
            assertTrue(Files.size(video) > 0, "expected a non-empty screencast recording");

            // 2. Shadow DOM page-source serialization (light-only default vs includeShadow=true) + redaction.
            playwright().browser(browser).extract()
                    .pageSource()
                    .variable("lightPageHtml")
                    .build().execute(context);
            String lightPageHtml = context.getVariable("lightPageHtml");
            assertFalse(lightPageHtml.contains("shadowrootmode=\"open\""), lightPageHtml);
            assertFalse(lightPageHtml.contains("<template shadowrootmode=\"open\"><span>Rendered Label</span></template>"),
                    lightPageHtml);
            assertTrue(lightPageHtml.contains(SecretPatternRedactor.MASK), lightPageHtml);
            assertFalse(lightPageHtml.contains("Direct closed text"), lightPageHtml);

            playwright().browser(browser).extract()
                    .pageSource(true)
                    .variable("shadowPageHtml")
                    .build().execute(context);
            String shadowPageHtml = context.getVariable("shadowPageHtml");
            assertTrue(shadowPageHtml.contains("<template shadowrootmode=\"open\"><span>Rendered Label</span></template>"),
                    shadowPageHtml);
            assertTrue(shadowPageHtml.contains(SecretPatternRedactor.MASK), shadowPageHtml);
            assertFalse(shadowPageHtml.contains("Direct closed text"), shadowPageHtml);

            // 3. WebAuthn virtual authenticator credential seeding with initial signCount.
            playwright().browser(browser).credentials().install().build().execute(context);
            playwright().browser(browser).credentials()
                    .create("example.com")
                    .signCount(7)
                    .build().execute(context);
            playwright().browser(browser).credentials()
                    .read("registeredPassId")
                    .build().execute(context);
            assertFalse(context.getVariable("registeredPassId").isBlank());
            assertEquals(7, browser.getCurrentContext().credentials().get().get(0).signCount);
            playwright().browser(browser).credentials()
                    .delete("${registeredPassId}")
                    .build().execute(context);
            assertTrue(browser.getCurrentContext().credentials().get().isEmpty());

            // 4. Filtered cookie clearing by name, domain, and path.
            playwright().browser(browser).cookies()
                    .add(CookieSpec.cookie("keepRoot", "v1").domain("example.com").path("/"))
                    .build().execute(context);
            playwright().browser(browser).cookies()
                    .add(CookieSpec.cookie("dropNamed", "v2").domain("example.com").path("/"))
                    .build().execute(context);
            playwright().browser(browser).cookies()
                    .add(CookieSpec.cookie("dropOrders", "v3").domain("example.com").path("/orders"))
                    .build().execute(context);

            playwright().browser(browser).cookies()
                    .clear().name("dropNamed")
                    .build().execute(context);
            playwright().browser(browser).cookies().verify("keepRoot", "v1").build().execute(context);
            playwright().browser(browser).cookies().verify("dropOrders", "v3").build().execute(context);
            assertThrows(CitrusRuntimeException.class,
                    () -> playwright().browser(browser).cookies().verify("dropNamed", "v2").build().execute(context));

            playwright().browser(browser).cookies()
                    .clear().domain("example.com").path("/orders")
                    .build().execute(context);
            playwright().browser(browser).cookies().verify("keepRoot", "v1").build().execute(context);
            assertThrows(CitrusRuntimeException.class,
                    () -> playwright().browser(browser).cookies().verify("dropOrders", "v3").build().execute(context));
        } finally {
            playwright().browser(browser).stop().build().execute(context);
        }
    }

    /**
     * Checks the RIFF/WEBP container signature so the assertion proves the format, not just that
     * some bytes were written.
     *
     * @param file screenshot file to inspect
     * @return true when the file carries WebP magic bytes
     * @throws IOException when the file cannot be read
     */
    private static boolean isWebp(Path file) throws IOException {
        byte[] header = Files.readAllBytes(file);
        if (header.length < 12) {
            return false;
        }
        return header[0] == 'R' && header[1] == 'I' && header[2] == 'F' && header[3] == 'F'
                && header[8] == 'W' && header[9] == 'E' && header[10] == 'B' && header[11] == 'P';
    }
}
