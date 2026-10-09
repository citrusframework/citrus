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

import static org.citrusframework.playwright.endpoint.PlaywrightHeaders.PLAYWRIGHT_API_PREFIX;
import static org.citrusframework.playwright.endpoint.PlaywrightHeaders.PLAYWRIGHT_API_SERVER_IP;
import static org.citrusframework.playwright.endpoint.PlaywrightHeaders.PLAYWRIGHT_API_SERVER_PORT;
import static org.citrusframework.playwright.endpoint.PlaywrightHeaders.PLAYWRIGHT_API_TIMING_CONNECT_END;
import static org.citrusframework.playwright.endpoint.PlaywrightHeaders.PLAYWRIGHT_API_TIMING_CONNECT_START;
import static org.citrusframework.playwright.endpoint.PlaywrightHeaders.PLAYWRIGHT_API_TIMING_DOMAIN_LOOKUP_END;
import static org.citrusframework.playwright.endpoint.PlaywrightHeaders.PLAYWRIGHT_API_TIMING_DOMAIN_LOOKUP_START;
import static org.citrusframework.playwright.endpoint.PlaywrightHeaders.PLAYWRIGHT_API_TIMING_REQUEST_START;
import static org.citrusframework.playwright.endpoint.PlaywrightHeaders.PLAYWRIGHT_API_TIMING_RESPONSE_END;
import static org.citrusframework.playwright.endpoint.PlaywrightHeaders.PLAYWRIGHT_API_TIMING_RESPONSE_START;
import static org.citrusframework.playwright.endpoint.PlaywrightHeaders.PLAYWRIGHT_API_TIMING_SECURE_CONNECTION_START;
import static org.citrusframework.playwright.endpoint.PlaywrightHeaders.PLAYWRIGHT_API_TIMING_START_TIME;
import static org.citrusframework.playwright.endpoint.PlaywrightHeaders.PLAYWRIGHT_API_TLS_ISSUER;
import static org.citrusframework.playwright.endpoint.PlaywrightHeaders.PLAYWRIGHT_API_TLS_PROTOCOL;
import static org.citrusframework.playwright.endpoint.PlaywrightHeaders.PLAYWRIGHT_API_TLS_SUBJECT_NAME;
import static org.citrusframework.playwright.endpoint.PlaywrightHeaders.PLAYWRIGHT_API_TLS_VALID_FROM;
import static org.citrusframework.playwright.endpoint.PlaywrightHeaders.PLAYWRIGHT_API_TLS_VALID_TO;
import static org.citrusframework.playwright.endpoint.PlaywrightHeaders.PLAYWRIGHT_API_URL;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import com.microsoft.playwright.options.SecurityDetails;
import com.microsoft.playwright.options.ServerAddr;
import com.microsoft.playwright.options.Timing;

import java.util.List;
import java.util.Map;

import org.testng.annotations.Test;

class TransportDetailHeadersTest {

    @Test
    void shouldReportServerAddressAndTimingOverPlainHttp() {
        Map<String, String> headers = TransportDetailHeaders.from("http://127.0.0.1:8080/api/me", plainHttpTiming(), serverAddr("127.0.0.1", 8080), null);

        assertEquals(List.copyOf(headers.keySet()), List.of(
                PLAYWRIGHT_API_URL,
                PLAYWRIGHT_API_SERVER_IP,
                PLAYWRIGHT_API_SERVER_PORT,
                PLAYWRIGHT_API_TIMING_START_TIME,
                PLAYWRIGHT_API_TIMING_CONNECT_START,
                PLAYWRIGHT_API_TIMING_CONNECT_END,
                PLAYWRIGHT_API_TIMING_REQUEST_START,
                PLAYWRIGHT_API_TIMING_RESPONSE_START,
                PLAYWRIGHT_API_TIMING_RESPONSE_END));
        assertEquals(headers.get(PLAYWRIGHT_API_URL), "http://127.0.0.1:8080/api/me");
        assertEquals(headers.get(PLAYWRIGHT_API_SERVER_IP), "127.0.0.1");
        assertEquals(headers.get(PLAYWRIGHT_API_SERVER_PORT), "8080");
        assertEquals(headers.get(PLAYWRIGHT_API_TIMING_CONNECT_START), "0");
        assertEquals(headers.get(PLAYWRIGHT_API_TIMING_RESPONSE_END), "3.38");
    }

    @Test
    void shouldOmitPhasesTheDriverDoesNotReport() {
        Map<String, String> headers = TransportDetailHeaders.from("http://127.0.0.1:8080/api/me", plainHttpTiming(), serverAddr("127.0.0.1", 8080), null);

        assertFalse(headers.containsKey(PLAYWRIGHT_API_TIMING_DOMAIN_LOOKUP_START));
        assertFalse(headers.containsKey(PLAYWRIGHT_API_TIMING_DOMAIN_LOOKUP_END));
        assertFalse(headers.containsKey(PLAYWRIGHT_API_TIMING_SECURE_CONNECTION_START));
        assertTrue(headers.keySet().stream().noneMatch(name -> name.startsWith(PLAYWRIGHT_API_PREFIX + "tls_")));
    }

    @Test
    void shouldReportTlsDetails() {
        SecurityDetails security = new SecurityDetails();
        security.protocol = "TLS 1.3";
        security.subjectName = "api.example.com";
        security.issuer = "Example CA";
        security.validFrom = 1.7e9;
        security.validTo = 1.8e9;

        Map<String, String> headers = TransportDetailHeaders.from(null, null, null, security);

        assertEquals(headers, Map.of(
                PLAYWRIGHT_API_TLS_PROTOCOL, "TLS 1.3",
                PLAYWRIGHT_API_TLS_SUBJECT_NAME, "api.example.com",
                PLAYWRIGHT_API_TLS_ISSUER, "Example CA",
                PLAYWRIGHT_API_TLS_VALID_FROM, "1700000000",
                PLAYWRIGHT_API_TLS_VALID_TO, "1800000000"));
    }

    @Test
    void shouldSkipTlsFieldsTheDriverLeavesEmpty() {
        SecurityDetails security = new SecurityDetails();
        security.protocol = "TLS 1.2";

        Map<String, String> headers = TransportDetailHeaders.from(null, null, null, security);

        assertEquals(headers, Map.of(PLAYWRIGHT_API_TLS_PROTOCOL, "TLS 1.2"));
    }

    @Test
    void shouldSkipAnEmptyUrl() {
        assertFalse(TransportDetailHeaders.from("", null, null, null).containsKey(PLAYWRIGHT_API_URL));
    }

    @Test
    void shouldSkipBlankTextDetails() {
        SecurityDetails security = new SecurityDetails();
        security.protocol = "   ";

        assertTrue(TransportDetailHeaders.from(" ", null, null, security).isEmpty());
    }

    @Test
    void shouldReturnNoHeadersWhenTheDriverReportsNothing() {
        assertTrue(TransportDetailHeaders.from(null, null, null, null).isEmpty());
    }

    @Test
    void shouldWriteNumbersWithoutExponent() {
        Timing timing = new Timing();
        timing.startTime = 1.790324158581E12;
        timing.domainLookupStart = -1;
        timing.domainLookupEnd = -1;
        timing.connectStart = -1;
        timing.secureConnectionStart = -1;
        timing.connectEnd = -1;
        timing.requestStart = 1.9209999999999923;
        timing.responseStart = 5.0;
        timing.responseEnd = -1;

        Map<String, String> headers = TransportDetailHeaders.from(null, timing, null, null);

        assertEquals(headers.get(PLAYWRIGHT_API_TIMING_START_TIME), "1790324158581");
        assertEquals(headers.get(PLAYWRIGHT_API_TIMING_REQUEST_START), "1.9209999999999923");
        assertEquals(headers.get(PLAYWRIGHT_API_TIMING_RESPONSE_START), "5");
        assertFalse(headers.containsKey(PLAYWRIGHT_API_TIMING_RESPONSE_END));
    }

    /**
     * Timing as the 1.63 driver reported it for a plain HTTP request to an IP literal: no DNS
     * phase and no TLS handshake.
     */
    private static Timing plainHttpTiming() {
        Timing timing = new Timing();
        timing.startTime = 1.790324158581E12;
        timing.domainLookupStart = -1;
        timing.domainLookupEnd = -1;
        timing.connectStart = 0.0;
        timing.secureConnectionStart = -1;
        timing.connectEnd = 1.9209999999999923;
        timing.requestStart = 1.9209999999999923;
        timing.responseStart = 2.9569999999999936;
        timing.responseEnd = 3.38;
        return timing;
    }

    private static ServerAddr serverAddr(String ip, int port) {
        ServerAddr serverAddr = new ServerAddr();
        serverAddr.ipAddress = ip;
        serverAddr.port = port;
        return serverAddr;
    }
}
