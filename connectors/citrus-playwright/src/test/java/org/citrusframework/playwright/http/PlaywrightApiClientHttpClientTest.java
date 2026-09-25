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
import static org.citrusframework.playwright.endpoint.PlaywrightHeaders.PLAYWRIGHT_API_SERVER_IP;
import static org.citrusframework.playwright.endpoint.PlaywrightHeaders.PLAYWRIGHT_API_SERVER_PORT;
import static org.citrusframework.playwright.endpoint.PlaywrightHeaders.PLAYWRIGHT_API_TIMING_RESPONSE_END;
import static org.citrusframework.playwright.endpoint.PlaywrightHeaders.PLAYWRIGHT_API_URL;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.expectThrows;

import com.microsoft.playwright.APIRequestContext;
import com.microsoft.playwright.APIResponse;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.options.HttpHeader;
import com.microsoft.playwright.options.RequestOptions;
import com.microsoft.playwright.options.ServerAddr;
import com.microsoft.playwright.options.Timing;

import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.citrusframework.exceptions.ValidationException;
import org.citrusframework.http.actions.HttpClientRequestActionBuilder;
import org.citrusframework.http.actions.HttpClientResponseActionBuilder;
import org.citrusframework.http.client.HttpClient;
import org.citrusframework.http.client.HttpClientBuilder;
import org.citrusframework.http.message.HttpMessage;
import org.citrusframework.log.CitrusLogSettings;
import org.citrusframework.log.DefaultLogModifier;
import org.citrusframework.message.Message;
import org.citrusframework.message.MessageType;
import org.citrusframework.playwright.model.SecretPatternRedactor;
import org.citrusframework.playwright.support.MockPlaywrightBrowser;
import org.citrusframework.testng.AbstractTestNGUnitTest;
import org.citrusframework.validation.context.json.JsonPathMessageValidationContext;
import jakarta.servlet.http.Cookie;
import org.mockito.ArgumentCaptor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Runs the real Citrus {@link HttpClient} and {@code http()} actions over the browser-session
 * transport, with only the driver mocked. Proves that everything above the transport — URL
 * composition, conversion, error strategy, validation — behaves exactly as for a plain client.
 */
class PlaywrightApiClientHttpClientTest extends AbstractTestNGUnitTest {

    private MockPlaywrightBrowser browser;
    private APIRequestContext api;
    private HttpClient client;

    @BeforeMethod
    void createClient() {
        api = mock(APIRequestContext.class);
        browser = new MockPlaywrightBrowser() {
            @Override
            protected BrowserContext createBrowserContext(Browser playwrightBrowser, Browser.NewContextOptions options) {
                BrowserContext browserContext = super.createBrowserContext(playwrightBrowser, options);
                when(browserContext.request()).thenReturn(api);
                return browserContext;
            }
        };
        client = new HttpClientBuilder()
                .requestUrl("http://localhost:8080")
                .requestFactory(new PlaywrightClientHttpRequestFactory(browser))
                .build();
    }

    @AfterMethod
    void stopBrowser() {
        browser.stop();
    }

    @Test
    void shouldComposeTheRequestAsTheHttpClientDoes() throws Exception {
        respond(201, "Created", "{}", List.of(header("Content-Type", "application/json")));
        context.setVariable("item", "book");

        HttpClientRequestActionBuilder send = http().client(client).send().post("/api/orders").queryParam("source", "ui");
        send.message().contentType("application/json").body("{\"item\":\"${item}\"}");
        send.build().execute(context);

        ArgumentCaptor<String> url = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<RequestOptions> options = ArgumentCaptor.forClass(RequestOptions.class);
        verify(api).fetch(url.capture(), options.capture());
        assertEquals(url.getValue(), "http://localhost:8080/api/orders?source=ui");
        assertEquals(field(options.getValue(), "method"), "POST");
        assertEquals(new String((byte[]) field(options.getValue(), "data"), StandardCharsets.UTF_8), "{\"item\":\"book\"}");
        @SuppressWarnings("unchecked")
        Map<String, String> headers = (Map<String, String>) field(options.getValue(), "headers");
        assertTrue(headers.get("Content-Type").startsWith("application/json"), String.valueOf(headers));
    }

    @Test
    void shouldPassAMultipartBodyThrough() throws Exception {
        respond(200, "OK", "", List.of());
        MultiValueMap<String, Object> parts = new LinkedMultiValueMap<>();
        parts.add("name", "citrus");
        parts.add("file", new ClassPathResource("fixtures/phase3.txt"));

        HttpClientRequestActionBuilder send = http().client(client).send().post("/api/upload");
        send.message().contentType(MediaType.MULTIPART_FORM_DATA_VALUE).body(parts);
        send.build().execute(context);

        RequestOptions options = captureOptions();
        String contentType = headers(options).get("Content-Type");
        String data = new String((byte[]) field(options, "data"), StandardCharsets.UTF_8);
        assertTrue(contentType.startsWith("multipart/form-data;boundary="), contentType);
        assertTrue(data.contains("name=\"name\""), data);
        assertTrue(data.contains("citrus"), data);
        assertTrue(data.contains("filename=\"phase3.txt\""), data);
    }

    @Test
    void shouldPassAFormUrlencodedBodyThrough() throws Exception {
        respond(200, "OK", "", List.of());
        MultiValueMap<String, Object> form = new LinkedMultiValueMap<>();
        form.add("name", "citrus");
        form.add("item", "book");

        HttpClientRequestActionBuilder send = http().client(client).send().post("/api/form");
        send.message().contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE).body(form);
        send.build().execute(context);

        RequestOptions options = captureOptions();
        assertTrue(headers(options).get("Content-Type").startsWith(MediaType.APPLICATION_FORM_URLENCODED_VALUE));
        assertEquals(new String((byte[]) field(options, "data"), StandardCharsets.UTF_8), "name=citrus&item=book");
    }

    @Test
    void shouldExposeTheFinalUrlAfterRedirects() {
        APIResponse response = respond(200, "OK", "", List.of());
        when(response.url()).thenReturn("http://localhost:8080/final");

        http().client(client).send().get("/api/redirect").build().execute(context);
        HttpClientResponseActionBuilder receive = http().client(client).receive().response(HttpStatus.OK);
        receive.message().header(PLAYWRIGHT_API_URL, "http://localhost:8080/final");
        receive.build().execute(context);
    }

    @Test
    void shouldVerifyTheResponseStatus() {
        respond(201, "Created", "{}", List.of(header("Content-Type", "application/json")));

        http().client(client).send().post("/api/orders").build().execute(context);
        http().client(client).receive().response(HttpStatus.CREATED).build().execute(context);
    }

    @Test
    void shouldDeliverANonSuccessStatusAsAResponse() {
        respond(404, "Not Found", "", List.of());

        http().client(client).send().get("/api/missing").build().execute(context);
        http().client(client).receive().response(HttpStatus.NOT_FOUND).build().execute(context);
    }

    @Test
    void shouldFailTheStatusVerificationForAnotherStatus() {
        respond(404, "Not Found", "", List.of());

        http().client(client).send().get("/api/missing").build().execute(context);
        expectThrows(ValidationException.class,
                () -> http().client(client).receive().response(HttpStatus.OK).build().execute(context));
    }

    @Test
    void shouldValidateAJsonBody() {
        respond(200, "OK", "{\"user\":\"alice\"}", List.of(header("Content-Type", "application/json")));

        http().client(client).send().get("/api/me").build().execute(context);
        HttpClientResponseActionBuilder receive = http().client(client).receive().response(HttpStatus.OK);
        receive.message().type(MessageType.JSON)
                .validate(JsonPathMessageValidationContext.Builder.jsonPath().expression("$.user", "alice"));
        receive.build().execute(context);
    }

    @Test
    void shouldKeepBothValuesOfARepeatedSetCookieHeader() {
        respond(200, "OK", "", List.of(
                header("Set-Cookie", "SID=abc; Path=/"),
                header("Set-Cookie", "API_SID=xyz; Path=/")));

        http().client(client).send().get("/api/cookies").build().execute(context);
        Message received = client.createConsumer().receive(context, 1000L);

        // HttpClient joins repeated header values with a comma, exactly as for a plain client.
        assertEquals(received.getHeader("Set-Cookie"), "SID=abc; Path=/,API_SID=xyz; Path=/");
    }

    @Test
    void shouldTurnRepeatedSetCookieHeadersIntoCookiesWhenHandlingCookies() {
        HttpClient cookieClient = new HttpClientBuilder()
                .requestUrl("http://localhost:8080")
                .requestFactory(new PlaywrightClientHttpRequestFactory(browser))
                .handleCookies(true)
                .build();
        respond(200, "OK", "", List.of(
                header("Set-Cookie", "SID=abc; Path=/"),
                header("Set-Cookie", "API_SID=xyz; Path=/")));

        http().client(cookieClient).send().get("/api/cookies").build().execute(context);
        HttpMessage received = (HttpMessage) cookieClient.createConsumer().receive(context, 1000L);

        // citrus-http's cookie converter does not keep header order.
        assertEquals(received.getCookies().size(), 2);
        assertEquals(received.getCookies().stream().map(Cookie::getName).collect(Collectors.toSet()), Set.of("SID", "API_SID"));
    }

    @Test
    void shouldExposeTheTransportDetailHeadersToValidation() {
        APIResponse response = respond(200, "OK", "", List.of());
        ServerAddr serverAddr = new ServerAddr();
        serverAddr.ipAddress = "127.0.0.1";
        serverAddr.port = 8080;
        when(response.serverAddr()).thenReturn(serverAddr);
        Timing timing = new Timing();
        timing.startTime = 1.790324158581E12;
        timing.domainLookupStart = -1;
        timing.domainLookupEnd = -1;
        timing.secureConnectionStart = -1;
        timing.responseEnd = 3.38;
        when(response.timing()).thenReturn(timing);

        http().client(client).send().get("/api/me").build().execute(context);
        HttpClientResponseActionBuilder receive = http().client(client).receive().response(HttpStatus.OK);
        receive.message()
                .header(PLAYWRIGHT_API_SERVER_IP, "127.0.0.1")
                .header(PLAYWRIGHT_API_SERVER_PORT, "8080")
                .header(PLAYWRIGHT_API_TIMING_RESPONSE_END, "@isNumber()@");
        receive.build().execute(context);
    }

    @Test
    void shouldMaskResponseValuesByTheCitrusLogKeywords() {
        String original = System.getProperty(CitrusLogSettings.LOG_MASK_KEYWORDS_PROPERTY);
        System.setProperty(CitrusLogSettings.LOG_MASK_KEYWORDS_PROPERTY, "password,secret,token");
        try {
            respond(200, "OK", "{\"token\":\"t-1\"}", List.of(header("Content-Type", "application/json")));
            context.setLogModifier(new DefaultLogModifier());

            http().client(client).send().get("/api/login").build().execute(context);
            Message received = client.createConsumer().receive(context, 1000L);
            String printed = received.print(context);

            assertFalse(printed.contains("t-1"), printed);
            assertTrue(printed.contains(SecretPatternRedactor.MASK), printed);
        } finally {
            if (original == null) {
                System.clearProperty(CitrusLogSettings.LOG_MASK_KEYWORDS_PROPERTY);
            } else {
                System.setProperty(CitrusLogSettings.LOG_MASK_KEYWORDS_PROPERTY, original);
            }
        }
    }

    private APIResponse respond(int status, String statusText, String body, List<HttpHeader> headers) {
        APIResponse response = mock(APIResponse.class);
        when(response.status()).thenReturn(status);
        when(response.statusText()).thenReturn(statusText);
        when(response.headersArray()).thenReturn(new ArrayList<>(headers));
        when(response.body()).thenReturn(body.getBytes(StandardCharsets.UTF_8));
        when(api.fetch(anyString(), any(RequestOptions.class))).thenReturn(response);
        return response;
    }

    private RequestOptions captureOptions() {
        ArgumentCaptor<RequestOptions> options = ArgumentCaptor.forClass(RequestOptions.class);
        verify(api).fetch(anyString(), options.capture());
        return options.getValue();
    }

    @SuppressWarnings("unchecked")
    private static Map<String, String> headers(RequestOptions options) throws ReflectiveOperationException {
        return (Map<String, String>) field(options, "headers");
    }

    private static HttpHeader header(String name, String value) {
        HttpHeader header = new HttpHeader();
        header.name = name;
        header.value = value;
        return header;
    }

    private static Object field(RequestOptions options, String name) throws ReflectiveOperationException {
        Field field = options.getClass().getDeclaredField(name);
        field.setAccessible(true);
        return field.get(options);
    }
}
