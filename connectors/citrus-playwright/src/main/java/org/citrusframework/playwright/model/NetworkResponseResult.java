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

package org.citrusframework.playwright.model;

import com.microsoft.playwright.Response;

import java.util.Map;

/**
 * Stable Citrus-facing snapshot of a Playwright response observed by a wait action.
 *
 * @param method request method
 * @param url sanitized response URL
 * @param status HTTP status code
 * @param ok Playwright response success flag
 * @param headers sanitized response headers
 * @param body sanitized response body when body capture is enabled
 * @param serverIp response server IP address, or null when the driver reports none
 * @param serverPort response server port, or null when the driver reports none
 * @param tlsProtocol TLS protocol, or null on plain HTTP
 * @param tlsSubjectName TLS certificate subject, or null on plain HTTP
 * @param tlsIssuer TLS certificate issuer, or null on plain HTTP
 * @param tlsValidFrom TLS certificate validity start in epoch seconds, or null on plain HTTP
 * @param tlsValidTo TLS certificate validity end in epoch seconds, or null on plain HTTP
 */
public record NetworkResponseResult(String method, String url, int status, boolean ok,
                                    Map<String, String> headers, String body,
                                    String serverIp, Integer serverPort,
                                    String tlsProtocol, String tlsSubjectName, String tlsIssuer,
                                    Double tlsValidFrom, Double tlsValidTo) {

    /**
     * Creates a response snapshot from a Playwright response.
     *
     * @param response Playwright response
     * @param redactor diagnostics redactor
     * @param includeBody true to read and include the response body text
     * @return response snapshot
     */
    public static NetworkResponseResult from(Response response, SecretPatternRedactor redactor, boolean includeBody) {
        String body = null;
        if (includeBody) {
            try {
                body = redactor.sanitizeText(response.text());
            } catch (RuntimeException ignored) {
                body = "<unavailable>";
            }
        }

        String serverIp = null;
        Integer serverPort = null;
        if (response.serverAddr() != null) {
            serverIp = response.serverAddr().ipAddress;
            serverPort = response.serverAddr().port;
        }

        String tlsProtocol = null;
        String tlsSubjectName = null;
        String tlsIssuer = null;
        Double tlsValidFrom = null;
        Double tlsValidTo = null;
        if (response.securityDetails() != null) {
            tlsProtocol = response.securityDetails().protocol;
            tlsSubjectName = redactor.sanitizeText(response.securityDetails().subjectName);
            tlsIssuer = redactor.sanitizeText(response.securityDetails().issuer);
            tlsValidFrom = response.securityDetails().validFrom;
            tlsValidTo = response.securityDetails().validTo;
        }

        return new NetworkResponseResult(
                response.request().method(),
                redactor.sanitizeUrl(response.url()),
                response.status(),
                response.ok(),
                redactor.sanitizeHeaders(response.headers()),
                body,
                serverIp,
                serverPort,
                tlsProtocol,
                tlsSubjectName,
                tlsIssuer,
                tlsValidFrom,
                tlsValidTo);
    }
}
