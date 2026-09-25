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

import com.microsoft.playwright.options.SecurityDetails;
import com.microsoft.playwright.options.ServerAddr;
import com.microsoft.playwright.options.Timing;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Turns the transport details the driver reports for an API response into response headers.
 *
 * <p>A detail the driver does not report is omitted rather than faked: a {@code null} object or
 * field, and a timing phase of {@code -1}. Numbers are written in plain decimal notation so that
 * validation matchers compare them reliably.</p>
 */
final class TransportDetailHeaders {

    private TransportDetailHeaders() {
    }

    static Map<String, String> from(Timing timing, ServerAddr serverAddr, SecurityDetails securityDetails) {
        Map<String, String> headers = new LinkedHashMap<>();

        if (serverAddr != null) {
            putText(headers, PLAYWRIGHT_API_SERVER_IP, serverAddr.ipAddress);
            headers.put(PLAYWRIGHT_API_SERVER_PORT, String.valueOf(serverAddr.port));
        }

        if (securityDetails != null) {
            putText(headers, PLAYWRIGHT_API_TLS_PROTOCOL, securityDetails.protocol);
            putText(headers, PLAYWRIGHT_API_TLS_SUBJECT_NAME, securityDetails.subjectName);
            putText(headers, PLAYWRIGHT_API_TLS_ISSUER, securityDetails.issuer);
            if (securityDetails.validFrom != null) {
                headers.put(PLAYWRIGHT_API_TLS_VALID_FROM, plain(securityDetails.validFrom));
            }
            if (securityDetails.validTo != null) {
                headers.put(PLAYWRIGHT_API_TLS_VALID_TO, plain(securityDetails.validTo));
            }
        }

        if (timing != null) {
            putPhase(headers, PLAYWRIGHT_API_TIMING_START_TIME, timing.startTime);
            putPhase(headers, PLAYWRIGHT_API_TIMING_DOMAIN_LOOKUP_START, timing.domainLookupStart);
            putPhase(headers, PLAYWRIGHT_API_TIMING_DOMAIN_LOOKUP_END, timing.domainLookupEnd);
            putPhase(headers, PLAYWRIGHT_API_TIMING_CONNECT_START, timing.connectStart);
            putPhase(headers, PLAYWRIGHT_API_TIMING_SECURE_CONNECTION_START, timing.secureConnectionStart);
            putPhase(headers, PLAYWRIGHT_API_TIMING_CONNECT_END, timing.connectEnd);
            putPhase(headers, PLAYWRIGHT_API_TIMING_REQUEST_START, timing.requestStart);
            putPhase(headers, PLAYWRIGHT_API_TIMING_RESPONSE_START, timing.responseStart);
            putPhase(headers, PLAYWRIGHT_API_TIMING_RESPONSE_END, timing.responseEnd);
        }

        return headers;
    }

    private static void putText(Map<String, String> headers, String name, String value) {
        if (value != null && !value.isEmpty()) {
            headers.put(name, value);
        }
    }

    private static void putPhase(Map<String, String> headers, String name, double value) {
        if (value >= 0) {
            headers.put(name, plain(value));
        }
    }

    private static String plain(double value) {
        return BigDecimal.valueOf(value).stripTrailingZeros().toPlainString();
    }
}
