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

package org.citrusframework.playwright.http;

import static org.citrusframework.http.actions.HttpActionBuilder.http;
import static org.citrusframework.playwright.actions.PlaywrightActionBuilder.playwright;
import static org.citrusframework.playwright.endpoint.PlaywrightHeaders.PLAYWRIGHT_API_PREFIX;
import static org.citrusframework.playwright.endpoint.PlaywrightHeaders.PLAYWRIGHT_API_SERVER_IP;
import static org.citrusframework.playwright.endpoint.PlaywrightHeaders.PLAYWRIGHT_API_SERVER_PORT;
import static org.citrusframework.playwright.endpoint.PlaywrightHeaders.PLAYWRIGHT_API_TIMING_RESPONSE_END;
import static org.citrusframework.playwright.endpoint.PlaywrightHeaders.PLAYWRIGHT_API_TIMING_SECURE_CONNECTION_START;
import static org.citrusframework.playwright.endpoint.PlaywrightHeaders.PLAYWRIGHT_API_TLS_ISSUER;
import static org.citrusframework.playwright.endpoint.PlaywrightHeaders.PLAYWRIGHT_API_TLS_PROTOCOL;
import static org.citrusframework.playwright.endpoint.PlaywrightHeaders.PLAYWRIGHT_API_TLS_SUBJECT_NAME;
import static org.citrusframework.playwright.endpoint.PlaywrightHeaders.PLAYWRIGHT_API_TLS_VALID_FROM;
import static org.citrusframework.playwright.endpoint.PlaywrightHeaders.PLAYWRIGHT_API_TLS_VALID_TO;
import static org.citrusframework.playwright.endpoint.PlaywrightHeaders.PLAYWRIGHT_API_URL;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.expectThrows;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.options.HttpCredentials;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

import org.citrusframework.actions.SendMessageAction;
import org.citrusframework.base.annotations.CitrusAnnotations;
import org.citrusframework.base.DefaultTestCaseRunner;
import org.citrusframework.common.TestSourceHelper;
import org.citrusframework.context.TestContext;
import org.citrusframework.http.actions.HttpClientRequestActionBuilder;
import org.citrusframework.http.actions.HttpClientResponseActionBuilder;
import org.citrusframework.http.client.HttpClient;
import org.citrusframework.http.client.HttpClientBuilder;
import org.citrusframework.http.message.HttpMessageHeaders;
import org.citrusframework.message.Message;
import org.citrusframework.message.MessageType;
import org.citrusframework.playwright.dsl.AbstractDslLoaderTest;
import org.citrusframework.playwright.endpoint.PlaywrightBrowser;
import org.citrusframework.playwright.endpoint.PlaywrightEndpointBuilder;
import org.citrusframework.playwright.endpoint.builder.PlaywrightEndpoints;
import org.citrusframework.playwright.support.FixtureServer;
import org.citrusframework.playwright.support.FixtureServer.RecordedRequest;
import org.citrusframework.report.MessageListener;
import org.citrusframework.validation.context.json.JsonPathMessageValidationContext;
import org.citrusframework.xml.XmlTestLoader;
import org.citrusframework.yaml.YamlTestLoader;
import org.springframework.http.HttpStatus;
import org.testng.SkipException;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Browser-session HTTP requests against a real Chromium and the local fixture server: the
 * session travels both ways, context settings apply, and the standard {@code http()} DSL verifies
 * the exchange in Java, XML and YAML.
 */
class PlaywrightApiRequestIT extends AbstractDslLoaderTest {

    private static final String API_CLIENT_NAME = "browserApi";

    private FixtureServer server;
    private FixtureServer httpsServer;
    private PlaywrightBrowser browser;
    private HttpClient browserApi;

    @BeforeClass
    public void requireChromium() {
        if (!chromiumAvailable()) {
            throw new SkipException("Chromium is not installed for Playwright - install it with -Pplaywright-runtimes");
        }
    }

    @BeforeMethod
    public void startFixture() throws IOException {
        server = FixtureServer.start()
                .route("/login", (request, response) -> response
                        .header("Set-Cookie", "SID=ui-session; Path=/; HttpOnly")
                        .send(200, "text/html", "<html><body><h1>Logged in</h1></body></html>"))
                .route("/login-admin", (request, response) -> response
                        .header("Set-Cookie", "ADMIN_SID=admin-session; Path=/; HttpOnly")
                        .send(200, "text/html", "<html><body><h1>Admin</h1></body></html>"))
                .route("/api/login", (request, response) -> response
                        .header("Set-Cookie", "API_SID=api-session; Path=/")
                        .send(200, "application/json", "{\"ok\":true}"))
                .route("/api/me", (request, response) -> {
                    if (request.hasCookie("SID") || request.hasCookie("API_SID")) {
                        response.send(200, "application/json", "{\"user\":\"alice\"}");
                    } else {
                        response.send(401, "application/json", "{\"error\":\"unauthenticated\"}");
                    }
                })
                .route("/api/orders", (request, response) -> response.send(201, "application/json", request.body()))
                .route("/api/cookies", (request, response) -> response
                        .header("Set-Cookie", "FIRST=1; Path=/")
                        .header("Set-Cookie", "SECOND=2; Path=/")
                        .send(200, "text/plain", "two cookies"))
                .route("/api/missing", (request, response) -> response.send(404, "text/plain", "missing"))
                .route("/protected", (request, response) -> {
                    if (request.header("Authorization") == null) {
                        response.header("WWW-Authenticate", "Basic realm=\"fixture\"").send(401, "text/plain", "challenge");
                    } else {
                        response.send(200, "text/plain", "authorized");
                    }
                })
                .route("/echo", (request, response) -> response.send(200, "text/plain", "echo"))
                .route("/redirect", (request, response) -> response.header("Location", "/echo").send(302, null, ""))
                .route("/slow", (request, response) -> {
                    try {
                        Thread.sleep(1500);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    response.send(200, "text/plain", "slow");
                })
                .route("/whoami", (request, response) -> response.send(200, "text/html",
                        "<html><body><pre id=\"cookies\">%s</pre></body></html>".formatted(
                                request.header("Cookie") == null ? "" : request.header("Cookie"))));
    }

    @AfterMethod(alwaysRun = true)
    public void stopBrowserAndFixture() {
        if (browser != null) {
            browser.stop();
            browser = null;
        }
        if (server != null) {
            server.close();
        }
        if (httpsServer != null) {
            httpsServer.close();
            httpsServer = null;
        }
    }

    @Test
    void shouldCarryTheUiSessionToApiRequests() {
        startBrowser(builder -> { });

        open("/login");
        send(browserApi, "/api/me");

        HttpClientResponseActionBuilder receive = http().client(browserApi).receive().response(HttpStatus.OK);
        receive.message().type(MessageType.JSON)
                .validate(JsonPathMessageValidationContext.Builder.jsonPath().expression("$.user", "alice"));
        receive.build().execute(context);
        assertTrue(lastRequest("/api/me").hasCookie("SID"));
    }

    @Test
    void shouldNotLeakCookiesIntoAnotherContext() {
        startBrowser(builder -> { });
        open("/login");
        playwright().browser(browser).context().newContext("guest").build().execute(context);
        playwright().browser(browser).context().switchTo("default").build().execute(context);
        HttpClient guestApi = PlaywrightEndpoints.playwright().apiClient().browser(browser).context("guest").build();

        exchange(guestApi, "/echo");

        assertNull(lastRequest("/echo").header("Cookie"));
    }

    @Test
    void shouldHandAnApiSessionBackToTheBrowser() {
        startBrowser(builder -> { });

        http().client(browserApi).send().post("/api/login").build().execute(context);
        http().client(browserApi).receive().response(HttpStatus.OK).build().execute(context);
        open("/whoami");
        playwright().browser(browser).extract().locator("#cookies").text().variable("cookies").build().execute(context);

        assertTrue(context.getVariable("cookies").contains("API_SID=api-session"), context.getVariable("cookies"));
    }

    @Test
    void shouldApplyTheContextHeadersAndUserAgent() {
        startBrowser(builder -> builder.contextOptions(new Browser.NewContextOptions()
                .setExtraHTTPHeaders(Map.of("X-Extra", "from-context"))
                .setUserAgent("citrus-it")));

        exchange(browserApi, "/echo");

        RecordedRequest request = lastRequest("/echo");
        assertEquals(request.header("X-Extra"), "from-context");
        assertEquals(request.header("User-Agent"), "citrus-it");
    }

    @Test
    void shouldAnswerACredentialsChallenge() {
        startBrowser(builder -> builder.httpCredentials(List.of(new HttpCredentials("alice", "s3cret"))));

        send(browserApi, "/protected");

        http().client(browserApi).receive().response(HttpStatus.OK).build().execute(context);
    }

    @Test
    void shouldFollowTheCurrentContextAcrossSwitches() {
        startBrowser(builder -> { });
        open("/login");
        playwright().browser(browser).context().newContext("admin").build().execute(context);
        playwright().browser(browser).page().newPage("admin-page").build().execute(context);
        open("/login-admin");

        exchange(browserApi, "/echo");

        RecordedRequest request = lastRequest("/echo");
        assertTrue(request.hasCookie("ADMIN_SID"), String.valueOf(request.header("Cookie")));
        assertFalse(request.hasCookie("SID"), String.valueOf(request.header("Cookie")));
    }

    @Test
    void shouldFailForAnUnknownContextAliasWithoutSending() {
        startBrowser(builder -> { });
        playwright().browser(browser).context().newContext("admin").build().execute(context);
        HttpClient opsApi = PlaywrightEndpoints.playwright().apiClient().browser(browser).context("ops").build();

        RuntimeException error = expectThrows(RuntimeException.class,
                () -> http().client(opsApi).send().get("/echo").build().execute(context));

        assertTrue(messages(error).contains("'ops'"), messages(error));
        assertTrue(messages(error).contains("default, admin"), messages(error));
        assertTrue(server.lastRequest("/echo").isEmpty());
    }

    @Test
    void shouldComposeTheRequestOnTheWire() {
        startBrowser(builder -> { });
        context.setVariable("item", "book");

        HttpClientRequestActionBuilder send = http().client(browserApi).send().post("/api/orders").queryParam("source", "ui");
        send.message().contentType("application/json").body("{\"item\":\"${item}\"}");
        send.build().execute(context);

        RecordedRequest request = lastRequest("/api/orders");
        assertEquals(request.method(), "POST");
        assertEquals(request.query(), "source=ui");
        assertTrue(request.header("Content-Type").startsWith("application/json"), request.header("Content-Type"));
        assertEquals(request.body(), "{\"item\":\"book\"}");
    }

    @Test
    void shouldRejectAnExplicitCookieHeaderWithoutSending() {
        startBrowser(builder -> { });

        HttpClientRequestActionBuilder send = http().client(browserApi).send().get("/echo");
        send.message().header("Cookie", "EXTRA=1");
        RuntimeException error = expectThrows(RuntimeException.class, () -> send.build().execute(context));

        assertTrue(messages(error).contains("playwright().cookies().add("), messages(error));
        assertTrue(server.lastRequest("/echo").isEmpty());
    }

    @Test
    void shouldDeliverRepeatedCookiesAndNonSuccessStatuses() {
        startBrowser(builder -> { });

        Message cookies = exchange(browserApi, "/api/cookies");
        send(browserApi, "/api/missing");

        // The JDK fixture server sends the name as "Set-cookie"; Citrus message headers keep the
        // name as received, exactly as for a plain HttpClient.
        assertEquals(header(cookies, "Set-Cookie"), "FIRST=1; Path=/,SECOND=2; Path=/");
        http().client(browserApi).receive().response(HttpStatus.NOT_FOUND).build().execute(context);
    }

    private static String header(Message message, String name) {
        return message.getHeaders().entrySet().stream()
                .filter(entry -> entry.getKey().equalsIgnoreCase(name))
                .map(entry -> String.valueOf(entry.getValue()))
                .findFirst()
                .orElse(null);
    }

    @Test
    void shouldExposeTheTransportDetails() {
        startBrowser(builder -> { });

        Message response = exchange(browserApi, "/echo");

        assertEquals(String.valueOf(response.getHeader(PLAYWRIGHT_API_SERVER_IP)), "127.0.0.1");
        assertEquals(String.valueOf(response.getHeader(PLAYWRIGHT_API_SERVER_PORT)), server.url("").replaceAll(".*:", ""));
        assertTrue(Double.parseDouble(String.valueOf(response.getHeader(PLAYWRIGHT_API_TIMING_RESPONSE_END))) >= 0);
        assertTrue(response.getHeaders().keySet().stream().noneMatch(name -> name.startsWith(PLAYWRIGHT_API_PREFIX + "tls_")),
                String.valueOf(response.getHeaders()));
        assertTrue(response.getHeaders().keySet().stream().noneMatch(name -> name.startsWith(PLAYWRIGHT_API_PREFIX + "timing_domain_lookup")),
                String.valueOf(response.getHeaders()));
    }

    @Test
    void shouldKeepTheBrowserSessionOutOfCitrusLogs() {
        startBrowser(builder -> { });
        List<String> logged = new CopyOnWriteArrayList<>();
        context.getMessageListeners().addMessageListener(new MessageListener() {
            @Override
            public void onInboundMessage(Message message, TestContext context) {
                logged.add(message.getPayload(String.class));
            }

            @Override
            public void onOutboundMessage(Message message, TestContext context) {
                logged.add(message.getPayload(String.class));
            }
        });

        open("/login");
        exchange(browserApi, "/api/me");

        assertTrue(lastRequest("/api/me").hasCookie("SID"));
        assertTrue(logged.stream().anyMatch(entry -> entry.contains("/api/me")), String.valueOf(logged));
        assertTrue(logged.stream().noneMatch(entry -> entry.contains("ui-session")), String.valueOf(logged));
    }

    @Test
    void shouldRejectAForkedSend() throws InterruptedException {
        startBrowser(builder -> { });

        SendMessageAction forked = http().client(browserApi).send().get("/echo").fork(true).build();
        forked.execute(context);
        for (int i = 0; i < 100 && !context.hasExceptions(); i++) {
            Thread.sleep(50);
        }

        assertTrue(context.hasExceptions());
        assertTrue(messages(context.getExceptions().get(0)).contains("cannot be shared with thread"),
                messages(context.getExceptions().get(0)));
        assertTrue(server.lastRequest("/echo").isEmpty());
        context.getExceptions().clear();
    }

    @Test
    void shouldValidateLikeAPlainHttpClient() {
        startBrowser(builder -> { });
        HttpClient plain = new HttpClientBuilder().requestUrl(server.url("")).build();
        context.setVariable("item", "book");

        for (HttpClient client : List.of(plain, browserApi)) {
            HttpClientRequestActionBuilder send = http().client(client).send().post("/api/orders");
            send.message().contentType("application/json").body("{\"item\":\"${item}\"}");
            send.build().execute(context);

            HttpClientResponseActionBuilder receive = http().client(client).receive().response(HttpStatus.CREATED);
            receive.message().contentType("application/json").type(MessageType.JSON)
                    .validate(JsonPathMessageValidationContext.Builder.jsonPath().expression("$.item", "book"));
            receive.build().execute(context);
        }
    }

    @Test
    void shouldRejectAnInvalidCertificateByDefault() throws IOException {
        startBrowser(builder -> { });
        HttpClient client = PlaywrightEndpoints.playwright().apiClient().browser(browser).requestUrl(startHttps()).build();

        RuntimeException error = expectThrows(RuntimeException.class, () -> send(client, "/echo"));

        assertTrue(messages(error).contains("Browser-session request GET https://127.0.0.1"), messages(error));
        assertTrue(httpsServer.lastRequest("/echo").isEmpty());
    }

    @Test
    void shouldAcceptAnInvalidCertificateWhenTheClientIgnoresHttpsErrors() throws IOException {
        startBrowser(builder -> { });
        HttpClient client = PlaywrightEndpoints.playwright().apiClient().browser(browser)
                .requestUrl(startHttps())
                .ignoreHttpsErrors(true)
                .build();

        Message response = exchange(client, "/echo");

        assertEquals(String.valueOf(response.getHeader(HttpMessageHeaders.HTTP_STATUS_CODE)), "200");
        assertEquals(String.valueOf(response.getHeader(PLAYWRIGHT_API_TLS_SUBJECT_NAME)), "Citrus");
        assertEquals(String.valueOf(response.getHeader(PLAYWRIGHT_API_TLS_ISSUER)), "Citrus");
        assertEquals(String.valueOf(response.getHeader(PLAYWRIGHT_API_TLS_VALID_FROM)), "1475601682");
        assertEquals(String.valueOf(response.getHeader(PLAYWRIGHT_API_TLS_VALID_TO)), "1483377682");
        assertTrue(String.valueOf(response.getHeader(PLAYWRIGHT_API_TLS_PROTOCOL)).startsWith("TLS"),
                String.valueOf(response.getHeader(PLAYWRIGHT_API_TLS_PROTOCOL)));
        assertTrue(response.getHeaders().containsKey(PLAYWRIGHT_API_TIMING_SECURE_CONNECTION_START), String.valueOf(response.getHeaders()));
    }

    @Test
    void shouldHonourTheBrowserContextIgnoreHttpsErrors() throws IOException {
        startBrowser(builder -> builder.contextOptions(new Browser.NewContextOptions().setIgnoreHTTPSErrors(true)));
        HttpClient client = PlaywrightEndpoints.playwright().apiClient().browser(browser).requestUrl(startHttps()).build();

        Message response = exchange(client, "/echo");

        assertEquals(String.valueOf(response.getHeader(HttpMessageHeaders.HTTP_STATUS_CODE)), "200");
    }

    @Test
    void shouldReturnTheRedirectItselfWhenRedirectsAreDisabled() {
        startBrowser(builder -> { });
        HttpClient noRedirects = PlaywrightEndpoints.playwright().apiClient().browser(browser).maxRedirects(0).build();

        Message redirect = exchange(noRedirects, "/redirect");
        Message followed = exchange(browserApi, "/redirect");

        assertEquals(String.valueOf(redirect.getHeader(HttpMessageHeaders.HTTP_STATUS_CODE)), "302");
        assertEquals(header(redirect, "Location"), "/echo");
        assertEquals(String.valueOf(followed.getHeader(HttpMessageHeaders.HTTP_STATUS_CODE)), "200");
        assertEquals(String.valueOf(followed.getHeader(PLAYWRIGHT_API_URL)), server.url("/echo"));
    }

    @Test
    void shouldTimeOutASlowRequest() {
        startBrowser(builder -> { });
        HttpClient impatient = PlaywrightEndpoints.playwright().apiClient().browser(browser).timeout(300L).build();

        RuntimeException error = expectThrows(RuntimeException.class, () -> send(impatient, "/slow"));

        assertTrue(messages(error).contains("300"), messages(error));
    }

    @Test
    void shouldRunTheXmlSource() {
        startBrowser(builder -> { });
        context.setVariable("loginUrl", server.url("/login"));

        XmlTestLoader loader = new XmlTestLoader(getClass(), "PlaywrightApiXmlTest", "org.citrusframework.playwright.xml");
        CitrusAnnotations.injectAll(loader, citrus, context);
        CitrusAnnotations.injectTestRunner(loader, new DefaultTestCaseRunner(context));
        loader.setSource(TestSourceHelper.create("classpath:org/citrusframework/playwright/xml/playwright-api.citrus.it.xml"));
        loader.load();

        assertTrue(loader.getTestCase().getTestResult().isSuccess(), String.valueOf(loader.getTestCase().getTestResult()));
        assertTrue(lastRequest("/api/me").hasCookie("SID"));
    }

    @Test
    void shouldRunTheYamlSource() {
        startBrowser(builder -> { });
        context.setVariable("loginUrl", server.url("/login"));

        YamlTestLoader loader = new YamlTestLoader(getClass(), "PlaywrightApiYamlTest", "org.citrusframework.playwright.yaml");
        CitrusAnnotations.injectAll(loader, citrus, context);
        CitrusAnnotations.injectTestRunner(loader, new DefaultTestCaseRunner(context));
        loader.setSource(TestSourceHelper.create("classpath:org/citrusframework/playwright/yaml/playwright-api.citrus.it.yaml"));
        loader.load();

        assertTrue(loader.getTestCase().getTestResult().isSuccess(), String.valueOf(loader.getTestCase().getTestResult()));
        assertTrue(lastRequest("/api/me").hasCookie("SID"));
    }

    private void startBrowser(Consumer<PlaywrightEndpointBuilder> customizer) {
        PlaywrightEndpointBuilder builder = PlaywrightEndpoints.playwright()
                .browser()
                .browserType("chromium")
                .headless(true)
                .defaultTimeout(5000)
                .baseUrl(server.url(""));
        customizer.accept(builder);
        browser = builder.build();
        playwright().browser(browser).start().build().execute(context);

        browserApi = PlaywrightEndpoints.playwright().apiClient().browser(browser).timeout(5000L).build();
        browserApi.setName(API_CLIENT_NAME);
        context.getReferenceResolver().bind(BROWSER_NAME, browser);
        context.getReferenceResolver().bind(API_CLIENT_NAME, browserApi);
    }

    private String startHttps() throws IOException {
        httpsServer = FixtureServer.startHttps()
                .route("/echo", (request, response) -> response.send(200, "text/plain", "secure echo"));
        return httpsServer.url("");
    }

    private void open(String path) {
        playwright().browser(browser).open().url(server.url(path)).build().execute(context);
    }

    private void send(HttpClient client, String path) {
        http().client(client).send().get(path).build().execute(context);
    }

    private Message exchange(HttpClient client, String path) {
        send(client, path);
        return client.createConsumer().receive(context, 5000L);
    }

    private RecordedRequest lastRequest(String path) {
        return server.lastRequest(path).orElseThrow(() -> new AssertionError("The fixture received no request for " + path));
    }

    private static String messages(Throwable error) {
        StringBuilder messages = new StringBuilder();
        for (Throwable current = error; current != null; current = current.getCause()) {
            messages.append(current.getMessage()).append(" | ");
        }
        return messages.toString();
    }

    private static boolean chromiumAvailable() {
        try (Playwright playwright = Playwright.create()) {
            try (Browser ignored = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true))) {
                return true;
            }
        } catch (RuntimeException e) {
            return false;
        }
    }
}
