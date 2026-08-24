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

import com.microsoft.playwright.Page;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Playwright;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Proxy;
import java.net.InetSocketAddress;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

import org.citrusframework.TestCase;
import org.citrusframework.container.Sequence;
import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.ValidationException;
import org.citrusframework.playwright.endpoint.PlaywrightBrowser;
import org.citrusframework.playwright.endpoint.builder.PlaywrightEndpoints;
import org.citrusframework.playwright.model.SecretPatternRedactor;
import org.citrusframework.playwright.model.BoundingBoxResult;
import org.citrusframework.playwright.model.ConsoleMessageRecord;
import org.citrusframework.playwright.model.CookieSpec;
import org.citrusframework.playwright.model.DownloadMetadata;
import org.citrusframework.playwright.model.NetworkRecord;
import org.citrusframework.playwright.model.NetworkResponseResult;
import org.citrusframework.playwright.model.PlaywrightTarget;
import org.citrusframework.playwright.support.FailureEvidenceListener;
import org.citrusframework.spi.SimpleReferenceResolver;
import org.testng.SkipException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

class PlaywrightBrowserIT {

    private static String originalMaskKeywords;

    @BeforeClass
    public void configureMaskKeywords() {
        originalMaskKeywords = System.getProperty("citrus.logger.mask.keywords");
        System.setProperty("citrus.logger.mask.keywords",
                "authorization,x-api-key,api-key,password,token,api_key,apikey,secret,auth,cookie");
    }

    @AfterClass
    public void restoreMaskKeywords() {
        if (originalMaskKeywords == null) {
            System.clearProperty("citrus.logger.mask.keywords");
        } else {
            System.setProperty("citrus.logger.mask.keywords", originalMaskKeywords);
        }
    }

    @Test
    void shouldDriveChromiumAgainstLocalFixture() throws Exception {
        if (!chromiumAvailable()) {
            throw new SkipException("Chromium is not installed for Playwright");
        }

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
            expectThrows(ValidationException.class,
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
        if (!chromiumAvailable()) {
            throw new SkipException("Chromium is not installed for Playwright");
        }

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
        if (!chromiumAvailable()) {
            throw new SkipException("Chromium is not installed for Playwright");
        }

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
        if (!chromiumAvailable()) {
            throw new SkipException("Chromium is not installed for Playwright");
        }

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
        if (!chromiumAvailable()) {
            throw new SkipException("Chromium is not installed for Playwright");
        }

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
        if (!chromiumAvailable()) {
            throw new SkipException("Chromium is not installed for Playwright");
        }

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

    static class FixtureServer implements AutoCloseable {
        private final HttpServer server;

        private FixtureServer(HttpServer server) {
            this.server = server;
        }

        static FixtureServer start() throws IOException {
            HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
            server.createContext("/", FixtureServer::handle);
            server.start();
            return new FixtureServer(server);
        }

        String url(String path) {
            return "http://127.0.0.1:%d%s".formatted(server.getAddress().getPort(), path);
        }

        @Override
        public void close() {
            server.stop(0);
        }

        private static void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();
            try (InputStream stream = PlaywrightBrowserIT.class.getResourceAsStream("/fixtures" + path)) {
                if (stream == null) {
                    exchange.sendResponseHeaders(404, -1);
                    return;
                }
                byte[] body = stream.readAllBytes();
                exchange.getResponseHeaders().set("Content-Type", "text/html; charset=utf-8");
                exchange.sendResponseHeaders(200, body.length);
                exchange.getResponseBody().write(body);
            } finally {
                exchange.close();
            }
        }
    }

    private TestCase testCase(String name) {
        return (TestCase) Proxy.newProxyInstance(TestCase.class.getClassLoader(), new Class<?>[]{TestCase.class},
                (proxy, method, args) -> "getName".equals(method.getName()) ? name : null);
    }

    private boolean chromiumAvailable() {
        try (Playwright playwright = Playwright.create()) {
            try (com.microsoft.playwright.Browser ignored = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true))) {
                return true;
            }
        } catch (RuntimeException e) {
            return false;
        }
    }
}
