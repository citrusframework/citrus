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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.expectThrows;

import com.microsoft.playwright.APIRequestContext;
import com.microsoft.playwright.APIResponse;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.options.RequestOptions;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.playwright.model.SecretPatternRedactor;
import org.citrusframework.playwright.support.MockPlaywrightBrowser;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpResponse;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

class PlaywrightClientHttpRequestFactoryTest {

    private static final URI ORDERS = URI.create("http://localhost:8080/api/orders?source=ui");

    private ApiBrowser browser;

    @BeforeMethod
    void createBrowser() {
        browser = new ApiBrowser();
    }

    @AfterMethod
    void stopBrowser() {
        browser.stop();
    }

    @Test
    void shouldRequireABrowser() {
        CitrusRuntimeException error = expectThrows(CitrusRuntimeException.class,
                () -> new PlaywrightClientHttpRequestFactory(null));

        assertTrue(error.getMessage().contains("browser"));
    }

    @Test
    void shouldRejectRequestsFromAThreadThatDoesNotOwnTheBrowser() throws Exception {
        browser.start();
        Thread owner = new Thread(browser::assertActionThread);
        owner.start();
        owner.join();

        ClientHttpRequest request = new PlaywrightClientHttpRequestFactory(browser).createRequest(ORDERS, HttpMethod.GET);
        CitrusRuntimeException error = expectThrows(CitrusRuntimeException.class, request::execute);

        assertTrue(error.getMessage().contains("cannot be shared with thread"));
        verify(browser.api(browser.getCurrentContext()), never()).fetch(anyString(), any(RequestOptions.class));
    }

    @Test
    void shouldSendThroughTheCurrentContextAtSendTime() throws IOException {
        browser.start();
        BrowserContext defaultContext = browser.getCurrentContext();
        BrowserContext adminContext = browser.createContext("admin");
        PlaywrightClientHttpRequestFactory factory = new PlaywrightClientHttpRequestFactory(browser);

        factory.createRequest(ORDERS, HttpMethod.GET).execute();
        browser.switchContext("default");
        factory.createRequest(ORDERS, HttpMethod.GET).execute();

        verify(browser.api(adminContext)).fetch(eq(ORDERS.toString()), any(RequestOptions.class));
        verify(browser.api(defaultContext)).fetch(eq(ORDERS.toString()), any(RequestOptions.class));
    }

    @Test
    void shouldSendThroughTheBoundContext() throws IOException {
        browser.start();
        BrowserContext defaultContext = browser.getCurrentContext();
        BrowserContext adminContext = browser.createContext("admin");
        browser.switchContext("default");

        new PlaywrightClientHttpRequestFactory(browser).context("admin").createRequest(ORDERS, HttpMethod.GET).execute();

        verify(browser.api(adminContext)).fetch(eq(ORDERS.toString()), any(RequestOptions.class));
        verify(browser.api(defaultContext), never()).fetch(anyString(), any(RequestOptions.class));
    }

    @Test
    void shouldFailForAnUnknownContextAliasListingTheKnownOnes() throws IOException {
        browser.start();
        browser.createContext("admin");

        ClientHttpRequest request = new PlaywrightClientHttpRequestFactory(browser).context("ops").createRequest(ORDERS, HttpMethod.GET);
        CitrusRuntimeException error = expectThrows(CitrusRuntimeException.class, request::execute);

        assertTrue(error.getMessage().contains("'ops'"), error.getMessage());
        assertTrue(error.getMessage().contains("default, admin"), error.getMessage());
        browser.apis().values().forEach(api -> verify(api, never()).fetch(anyString(), any(RequestOptions.class)));
    }

    @Test
    void shouldStartTheBrowserBeforeResolvingTheContextAlias() throws IOException {
        assertFalse(browser.isStarted());

        new PlaywrightClientHttpRequestFactory(browser).context("default").createRequest(ORDERS, HttpMethod.GET).execute();

        assertTrue(browser.isStarted());
        verify(browser.api(browser.getCurrentContext())).fetch(eq(ORDERS.toString()), any(RequestOptions.class));
    }

    @Test
    void shouldRejectAnExplicitCookieHeader() throws IOException {
        browser.start();
        ClientHttpRequest request = new PlaywrightClientHttpRequestFactory(browser).createRequest(ORDERS, HttpMethod.GET);
        request.getHeaders().add("cookie", "EXTRA=1");

        CitrusRuntimeException error = expectThrows(CitrusRuntimeException.class, request::execute);

        assertTrue(error.getMessage().contains("Cookie"), error.getMessage());
        assertTrue(error.getMessage().contains("playwright().cookies().add("), error.getMessage());
        verify(browser.api(browser.getCurrentContext()), never()).fetch(anyString(), any(RequestOptions.class));
    }

    @Test
    void shouldReturnTheSnapshotOfTheDriverResponse() throws IOException {
        browser.start();
        APIResponse driverResponse = apiResponse(201, "{\"id\":1}");
        when(browser.api(browser.getCurrentContext()).fetch(anyString(), any(RequestOptions.class))).thenReturn(driverResponse);

        ClientHttpResponse response = new PlaywrightClientHttpRequestFactory(browser).createRequest(ORDERS, HttpMethod.POST).execute();

        assertEquals(response.getStatusCode().value(), 201);
        assertEquals(new String(response.getBody().readAllBytes(), StandardCharsets.UTF_8), "{\"id\":1}");
        verify(driverResponse).dispose();
    }

    @Test
    void shouldReportDriverFailuresWithoutTheDriverCallLog() throws IOException {
        browser.start();
        URI uri = URI.create("http://127.0.0.1:9/api?password=abc");
        when(browser.api(browser.getCurrentContext()).fetch(anyString(), any(RequestOptions.class))).thenThrow(new PlaywrightException(
                "Error: connect ECONNREFUSED 127.0.0.1:9\nCall log:\n  - → GET http://127.0.0.1:9/api?password=abc\n  -   cookie: SID=secret"));

        ClientHttpRequest request = new PlaywrightClientHttpRequestFactory(browser).createRequest(uri, HttpMethod.GET);
        IOException error = expectThrows(IOException.class, request::execute);

        assertTrue(error.getMessage().contains("http://127.0.0.1:9/api?password=" + SecretPatternRedactor.MASK), error.getMessage());
        assertTrue(error.getMessage().contains("ECONNREFUSED"), error.getMessage());
        assertFalse(error.getMessage().contains("password=abc"), error.getMessage());
        assertFalse(error.getMessage().contains("SID=secret"), error.getMessage());
        assertFalse(error.getMessage().contains("Call log"), error.getMessage());
        assertNull(error.getCause());
    }

    @Test
    void shouldReportTheReasonOfAStructuredDriverError() throws IOException {
        browser.start();
        // The format the 1.63 driver uses for a request timeout, call log included.
        when(browser.api(browser.getCurrentContext()).fetch(anyString(), any(RequestOptions.class))).thenThrow(new PlaywrightException(
                "Error {\n"
                        + "  message='Timeout 300ms exceeded.\n"
                        + "  name='TimeoutError\n"
                        + "  stack='TimeoutError: Timeout 300ms exceeded.\n"
                        + "    at _ProgressController.run (/tmp/playwright-java/package/lib/coreBundle.js:12360:32)\n"
                        + "}\n"
                        + "Call log:\n"
                        + "-   - → GET http://localhost:8080/api/orders?source=ui\n"
                        + "-     - cookie: SID=secret\n"));

        ClientHttpRequest request = new PlaywrightClientHttpRequestFactory(browser).createRequest(ORDERS, HttpMethod.GET);
        IOException error = expectThrows(IOException.class, request::execute);

        assertTrue(error.getMessage().endsWith("failed: Timeout 300ms exceeded."), error.getMessage());
        assertFalse(error.getMessage().contains("SID=secret"), error.getMessage());
        assertFalse(error.getMessage().contains("coreBundle"), error.getMessage());
    }

    @Test
    void shouldMapTheSpringRequestToAFetchSpec() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");
        headers.add("Accept", "application/json");
        headers.add("Accept", "text/plain");
        headers.add("Content-Length", "15");
        headers.add("Host", "localhost:8080");
        headers.add("Connection", "keep-alive");
        headers.add("Transfer-Encoding", "chunked");
        byte[] body = "{\"item\":\"book\"}".getBytes(StandardCharsets.UTF_8);

        FetchSpec spec = FetchSpec.of(HttpMethod.POST, ORDERS, headers, body, new TransportOptions(5000.0, 0, null, false));

        assertEquals(spec.method(), "POST");
        assertEquals(spec.url(), "http://localhost:8080/api/orders?source=ui");
        assertEquals(spec.headers(), Map.of("Content-Type", "application/json", "Accept", "application/json, text/plain"));
        assertEquals(new String(spec.body(), StandardCharsets.UTF_8), "{\"item\":\"book\"}");
        assertEquals(spec.options(), new TransportOptions(5000.0, 0, null, false));
    }

    @Test
    void shouldLeaveOptionalFetchSettingsUnset() throws Exception {
        FetchSpec spec = FetchSpec.of(HttpMethod.GET, ORDERS, new HttpHeaders(), new byte[0], TransportOptions.DEFAULTS);

        RequestOptions options = spec.toRequestOptions();

        assertEquals(field(options, "method"), "GET");
        assertEquals(field(options, "failOnStatusCode"), Boolean.FALSE);
        assertNull(field(options, "data"));
        assertNull(field(options, "timeout"));
        assertNull(field(options, "maxRedirects"));
        assertNull(field(options, "maxRetries"));
        assertNull(field(options, "ignoreHTTPSErrors"));
    }

    @Test
    void shouldCarryEveryFetchSettingIntoTheDriverOptions() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");
        byte[] body = "{}".getBytes(StandardCharsets.UTF_8);

        RequestOptions options = FetchSpec.of(HttpMethod.PUT, ORDERS, headers, body, new TransportOptions(2500.0, 3, 2, true)).toRequestOptions();

        assertEquals(field(options, "method"), "PUT");
        assertEquals(field(options, "headers"), Map.of("Content-Type", "application/json"));
        assertEquals(field(options, "data"), body);
        assertEquals(field(options, "failOnStatusCode"), Boolean.FALSE);
        assertEquals(field(options, "timeout"), 2500.0);
        assertEquals(field(options, "maxRedirects"), 3);
        assertEquals(field(options, "maxRetries"), 2);
        assertEquals(field(options, "ignoreHTTPSErrors"), Boolean.TRUE);
    }

    @Test
    void shouldCarryTheFactorySettingsIntoEveryFetch() throws IOException {
        browser.start();
        PlaywrightClientHttpRequestFactory factory = new PlaywrightClientHttpRequestFactory(browser)
                .timeout(2500)
                .maxRedirects(0)
                .maxRetries(2)
                .ignoreHttpsErrors(true);

        factory.createRequest(ORDERS, HttpMethod.GET).execute();

        assertEquals(factory.options(), new TransportOptions(2500.0, 0, 2, true));
        assertEquals(factory.getMaxRetries(), Integer.valueOf(2));
        assertTrue(factory.isIgnoreHttpsErrors());
    }

    @Test
    void shouldReportFailuresWhileReadingTheResponseWithoutTheDriverCallLog() throws IOException {
        browser.start();
        APIResponse driverResponse = apiResponse(200, "");
        when(driverResponse.body()).thenThrow(new PlaywrightException("Error: socket hang up\nCall log:\n  -   cookie: SID=secret"));
        when(browser.api(browser.getCurrentContext()).fetch(anyString(), any(RequestOptions.class))).thenReturn(driverResponse);

        ClientHttpRequest request = new PlaywrightClientHttpRequestFactory(browser).createRequest(ORDERS, HttpMethod.GET);
        IOException error = expectThrows(IOException.class, request::execute);

        assertTrue(error.getMessage().contains("socket hang up"), error.getMessage());
        assertFalse(error.getMessage().contains("SID=secret"), error.getMessage());
        assertNull(error.getCause());
        verify(driverResponse).dispose();
    }

    /**
     * Reads a field of the driver's option implementation. The fields are package private, so
     * this is the only way to check the conversion without a browser; a driver upgrade that
     * renames them fails here first, which is intended.
     */
    private static Object field(RequestOptions options, String name) throws ReflectiveOperationException {
        Field field = options.getClass().getDeclaredField(name);
        field.setAccessible(true);
        return field.get(options);
    }

    private static APIResponse apiResponse(int status, String body) {
        APIResponse response = mock(APIResponse.class);
        when(response.status()).thenReturn(status);
        when(response.statusText()).thenReturn("");
        when(response.headersArray()).thenReturn(List.of());
        when(response.body()).thenReturn(body.getBytes(StandardCharsets.UTF_8));
        return response;
    }

    /**
     * Mock browser whose every context owns its own mocked API request context.
     */
    private static final class ApiBrowser extends MockPlaywrightBrowser {

        private final Map<BrowserContext, APIRequestContext> apis = new IdentityHashMap<>();

        @Override
        protected BrowserContext createBrowserContext(Browser browser, Browser.NewContextOptions options) {
            BrowserContext context = super.createBrowserContext(browser, options);
            APIRequestContext api = mock(APIRequestContext.class);
            APIResponse ok = apiResponse(200, "");
            when(api.fetch(anyString(), any(RequestOptions.class))).thenReturn(ok);
            when(context.request()).thenReturn(api);
            apis.put(context, api);
            return context;
        }

        APIRequestContext api(BrowserContext context) {
            return apis.get(context);
        }

        Map<BrowserContext, APIRequestContext> apis() {
            return apis;
        }
    }
}
