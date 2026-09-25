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

import static org.citrusframework.playwright.endpoint.PlaywrightHeaders.PLAYWRIGHT_API_SERVER_IP;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.expectThrows;

import com.microsoft.playwright.APIResponse;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.options.HttpHeader;
import com.microsoft.playwright.options.ServerAddr;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.testng.annotations.Test;

class PlaywrightClientHttpResponseTest {

    @Test
    void shouldCopyStatusAndStatusText() throws IOException {
        APIResponse response = response(201, "Created", "{}");

        PlaywrightClientHttpResponse snapshot = PlaywrightClientHttpResponse.snapshot(response);

        assertEquals(snapshot.getStatusCode().value(), 201);
        assertEquals(snapshot.getStatusText(), "Created");
    }

    @Test
    void shouldAcceptNonStandardStatusCodes() throws IOException {
        PlaywrightClientHttpResponse snapshot = PlaywrightClientHttpResponse.snapshot(response(599, "", ""));

        assertEquals(snapshot.getStatusCode().value(), 599);
    }

    @Test
    void shouldKeepEveryValueOfRepeatedHeaders() {
        APIResponse response = response(200, "OK", "");
        when(response.headersArray()).thenReturn(List.of(
                header("Content-Type", "text/plain"),
                header("Set-Cookie", "SID=abc; Path=/"),
                header("Set-Cookie", "API_SID=xyz; Path=/")));

        PlaywrightClientHttpResponse snapshot = PlaywrightClientHttpResponse.snapshot(response);

        assertEquals(snapshot.getHeaders().get("Set-Cookie"), List.of("SID=abc; Path=/", "API_SID=xyz; Path=/"));
        assertEquals(snapshot.getHeaders().getFirst("Content-Type"), "text/plain");
    }

    @Test
    void shouldDescribeTheDecodedBodyWhenTheDriverDecompressedIt() throws IOException {
        String json = "{\"items\":[\"a\",\"b\",\"c\",\"d\"]}";
        APIResponse response = response(200, "OK", json);
        when(response.headersArray()).thenReturn(List.of(
                header("Content-Type", "application/json"),
                header("Content-Encoding", "gzip"),
                header("Content-Length", "12")));

        PlaywrightClientHttpResponse snapshot = PlaywrightClientHttpResponse.snapshot(response);

        assertNull(snapshot.getHeaders().getFirst("Content-Encoding"));
        assertEquals(snapshot.getHeaders().getContentLength(), json.length());
        assertEquals(snapshot.getHeaders().getFirst("Content-Type"), "application/json");
    }

    @Test
    void shouldKeepTheEntityHeadersOfAResponseWithoutBody() {
        // HEAD, 204 and 304: the headers describe the entity, not a delivered body.
        APIResponse response = response(200, "OK", "");
        when(response.headersArray()).thenReturn(List.of(
                header("Content-Encoding", "gzip"),
                header("Content-Length", "48213")));

        PlaywrightClientHttpResponse snapshot = PlaywrightClientHttpResponse.snapshot(response);

        assertEquals(snapshot.getHeaders().getFirst("Content-Encoding"), "gzip");
        assertEquals(snapshot.getHeaders().getContentLength(), 48213L);
    }

    @Test
    void shouldDescribeADecodedChunkedBodyWithOneFramingHeader() throws IOException {
        String json = "{\"user\":\"alice\"}";
        APIResponse response = response(200, "OK", json);
        when(response.headersArray()).thenReturn(List.of(
                header("Content-Encoding", "gzip"),
                header("Transfer-Encoding", "chunked")));

        PlaywrightClientHttpResponse snapshot = PlaywrightClientHttpResponse.snapshot(response);

        assertNull(snapshot.getHeaders().getFirst("Content-Encoding"));
        assertNull(snapshot.getHeaders().getFirst("Transfer-Encoding"));
        assertEquals(snapshot.getHeaders().getContentLength(), json.length());
    }

    @Test
    void shouldDescribeABodyWhoseLengthDisagreesWhateverTheCoding() {
        // A coding the connector does not know, decoded by a future driver.
        String json = "{\"items\":[\"a\",\"b\",\"c\",\"d\"]}";
        APIResponse response = response(200, "OK", json);
        when(response.headersArray()).thenReturn(List.of(
                header("Content-Encoding", "zstd"),
                header("Content-Length", "11")));

        PlaywrightClientHttpResponse snapshot = PlaywrightClientHttpResponse.snapshot(response);

        assertNull(snapshot.getHeaders().getFirst("Content-Encoding"));
        assertEquals(snapshot.getHeaders().getContentLength(), json.length());
    }

    @Test
    void shouldKeepTheHeadersOfABodyTheDriverDidNotTouch() {
        String data = "0123456789";
        APIResponse response = response(200, "OK", data);
        when(response.headersArray()).thenReturn(List.of(
                header("Content-Encoding", "zstd"),
                header("Content-Length", "10")));

        PlaywrightClientHttpResponse snapshot = PlaywrightClientHttpResponse.snapshot(response);

        assertEquals(snapshot.getHeaders().getFirst("Content-Encoding"), "zstd");
        assertEquals(snapshot.getHeaders().getContentLength(), 10L);
    }

    @Test
    void shouldFetchTheBodyFromTheDriverOnce() {
        APIResponse response = response(200, "OK", "{}");

        PlaywrightClientHttpResponse.snapshot(response);

        verify(response, times(1)).body();
    }

    @Test
    void shouldKeepTheContentLengthOfAnUncompressedResponse() {
        APIResponse response = response(200, "OK", "");
        when(response.headersArray()).thenReturn(List.of(header("Content-Length", "900")));

        PlaywrightClientHttpResponse snapshot = PlaywrightClientHttpResponse.snapshot(response);

        assertEquals(snapshot.getHeaders().getContentLength(), 900L);
    }

    @Test
    void shouldAddTransportDetailHeaders() {
        APIResponse response = response(200, "OK", "");
        ServerAddr serverAddr = new ServerAddr();
        serverAddr.ipAddress = "127.0.0.1";
        serverAddr.port = 8080;
        when(response.serverAddr()).thenReturn(serverAddr);

        PlaywrightClientHttpResponse snapshot = PlaywrightClientHttpResponse.snapshot(response);

        assertEquals(snapshot.getHeaders().getFirst(PLAYWRIGHT_API_SERVER_IP), "127.0.0.1");
    }

    @Test
    void shouldKeepTheBodyReadableAfterTheDriverResponseIsDisposed() throws IOException {
        APIResponse response = response(200, "OK", "{\"user\":\"alice\"}");

        PlaywrightClientHttpResponse snapshot = PlaywrightClientHttpResponse.snapshot(response);

        verify(response, times(1)).dispose();
        assertEquals(new String(snapshot.getBody().readAllBytes(), StandardCharsets.UTF_8), "{\"user\":\"alice\"}");
        assertEquals(new String(snapshot.getBody().readAllBytes(), StandardCharsets.UTF_8), "{\"user\":\"alice\"}");
    }

    @Test
    void shouldTreatAMissingBodyAsEmpty() throws IOException {
        APIResponse response = response(200, "OK", "");
        when(response.body()).thenReturn(null);

        PlaywrightClientHttpResponse snapshot = PlaywrightClientHttpResponse.snapshot(response);

        assertEquals(snapshot.getBody().readAllBytes().length, 0);
    }

    @Test
    void shouldNotDisposeAgainOnClose() {
        APIResponse response = response(200, "OK", "");

        PlaywrightClientHttpResponse.snapshot(response).close();

        verify(response, times(1)).dispose();
    }

    @Test
    void shouldDisposeTheDriverResponseWhenReadingFails() {
        APIResponse response = response(200, "OK", "");
        when(response.body()).thenThrow(new PlaywrightException("response body is unavailable"));

        expectThrows(PlaywrightException.class, () -> PlaywrightClientHttpResponse.snapshot(response));

        verify(response, times(1)).dispose();
    }

    private static APIResponse response(int status, String statusText, String body) {
        APIResponse response = mock(APIResponse.class);
        when(response.status()).thenReturn(status);
        when(response.statusText()).thenReturn(statusText);
        when(response.headersArray()).thenReturn(List.of());
        when(response.body()).thenReturn(body.getBytes(StandardCharsets.UTF_8));
        return response;
    }

    private static HttpHeader header(String name, String value) {
        HttpHeader header = new HttpHeader();
        header.name = name;
        header.value = value;
        return header;
    }
}
