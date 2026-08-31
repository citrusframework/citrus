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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.function.Consumer;

import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.exceptions.ValidationException;
import org.citrusframework.playwright.support.MockPlaywrightBrowser;
import org.citrusframework.playwright.support.PlaywrightBrowserScope;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.microsoft.playwright.Request;
import com.microsoft.playwright.Response;
import com.microsoft.playwright.Route;
import com.microsoft.playwright.Page;

class NetworkActionTest {

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
    void shouldStartCaptureOnCurrentPage() {
        new NetworkAction.Builder().capture().build().execute(context);

        verify(browser.page()).onRequest(any(Consumer.class));
    }

    @Test
    void shouldVerifyCapturedNetworkUrlContainsText() {
        seedNetwork("https://api.example.com/orders");

        new NetworkAction.Builder().verifyUrlContains("/orders").build().execute(context);
    }

    @Test
    void shouldFailValidationWhenUrlDoesNotContainText() {
        seedNetwork("https://api.example.com/x");

        NetworkAction action = new NetworkAction.Builder().verifyUrlContains("/orders").build();

        ValidationException exception = expectThrows(ValidationException.class, () -> action.execute(context));
        assertTrue(exception.getMessage().contains("/orders"));
    }

    @Test
    void shouldReportCapturedRecordsToVariable() {
        seedNetwork("https://api.example.com/orders");

        new NetworkAction.Builder().report().variable("networkReport").build().execute(context);

        String report = context.getVariable("networkReport");
        assertTrue(report.contains("https://api.example.com/orders"));
    }

    @Test
    void shouldAbortMatchedRoute() {
        Page page = browser.page();
        org.mockito.ArgumentCaptor<Consumer<Route>> captor =
                org.mockito.ArgumentCaptor.forClass(Consumer.class);

        new NetworkAction.Builder().route("**/api/**").abort().build().execute(context);

        verify(page).route(anyString(), captor.capture());
        captor.getValue().accept(browser.route());
        verify(browser.route()).abort();
    }

    @Test
    void shouldFulfillMatchedRoute() {
        Page page = browser.page();
        org.mockito.ArgumentCaptor<Consumer<Route>> captor =
                org.mockito.ArgumentCaptor.forClass(Consumer.class);

        new NetworkAction.Builder().route("**/api/**").fulfillJson("{\"ok\":true}").status(201)
                .header("x-test", "true").build().execute(context);

        verify(page).route(anyString(), captor.capture());
        captor.getValue().accept(browser.route());
        verify(browser.route()).fulfill(any(Route.FulfillOptions.class));
    }

    @Test
    void shouldContinueMatchedRouteWithHeader() {
        Page page = browser.page();
        org.mockito.ArgumentCaptor<Consumer<Route>> captor =
                org.mockito.ArgumentCaptor.forClass(Consumer.class);
        when(browser.route().request()).thenReturn(mock(Request.class));
        when(browser.route().request().headers()).thenReturn(Map.of("a", "1"));

        new NetworkAction.Builder().route("**/api/**").continueWithHeader("x-trace", "abc").build().execute(context);

        verify(page).route(anyString(), captor.capture());
        captor.getValue().accept(browser.route());
        verify(browser.route()).resume(any(Route.ResumeOptions.class));
    }

    @Test
    void shouldUnroutePattern() {
        new NetworkAction.Builder().unroute("**/api/**").build().execute(context);

        verify(browser.page()).unroute("**/api/**");
    }

    @Test
    void shouldWaitForResponseAndStoreResult() {
        Response response = browser.response();
        when(response.request()).thenReturn(mock(Request.class));
        when(response.request().method()).thenReturn("GET");
        when(response.url()).thenReturn("https://api.example.com/orders");
        when(response.status()).thenReturn(200);
        when(response.ok()).thenReturn(true);
        when(response.headers()).thenReturn(Map.of("content-type", "application/json"));
        when(response.text()).thenReturn("{\"ok\":true}");
        when(browser.page().waitForResponse(any(java.util.function.Predicate.class), any(), any())).thenReturn(response);

        new NetworkAction.Builder().waitForResponse().urlContains("/orders").includeBody().variable("resp")
                .build().execute(context);

        String result = String.valueOf(context.getVariable("resp"));
        assertTrue(result.contains("GET"));
        assertTrue(result.contains("200"));
        assertTrue(result.contains("\"ok\":true") || result.contains("ok"));
    }

    @Test
    void shouldFailFastWhenCommandMissing() {
        expectThrows(CitrusRuntimeException.class, () -> new NetworkAction.Builder().build());
    }

    @Test
    void shouldFailFastWhenRouteUrlPatternMissing() {
        expectThrows(CitrusRuntimeException.class, () -> new NetworkAction.Builder().abort().build());
    }

    @Test
    void shouldFailFastWhenVerifyTextMissing() {
        expectThrows(CitrusRuntimeException.class, () -> new NetworkAction.Builder().verifyUrlContains(null).build());
    }

    private void seedNetwork(String url) {
        browser.getNetworkCaptureRegistry().capture(browser.page(), 10, browser.createRedactor());
        org.mockito.ArgumentCaptor<Consumer<Request>> captor =
                org.mockito.ArgumentCaptor.forClass(Consumer.class);
        verify(browser.page()).onRequest(captor.capture());
        Request request = mock(Request.class);
        when(request.method()).thenReturn("GET");
        when(request.url()).thenReturn(url);
        when(request.headers()).thenReturn(Map.of());
        captor.getValue().accept(request);
    }
}
