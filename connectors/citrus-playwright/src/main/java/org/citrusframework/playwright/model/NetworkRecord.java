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

import com.microsoft.playwright.Request;
import com.microsoft.playwright.Response;

import java.util.Map;

/**
 * Immutable snapshot of a Playwright network event.
 *
 * <p>URLs and headers are sanitized when records are created so reports and
 * failure evidence do not expose obvious secret values.</p>
 *
 * @param event event kind such as request, response, or failed
 * @param method HTTP method
 * @param url sanitized request URL
 * @param status response status for response records
 * @param failure failure text for failed request records
 * @param headers sanitized headers captured for the event
 */
public record NetworkRecord(String event, String method, String url, Integer status,
                            String failure, Map<String, String> headers) {

    private static final SecretPatternRedactor DEFAULT_REDACTOR = new SecretPatternRedactor();

    /**
     * Creates a sanitized request record.
     *
     * @param request Playwright request event
     * @return network record
     */
    public static NetworkRecord request(Request request) {
        return request(request, DEFAULT_REDACTOR);
    }

    /**
     * Creates a sanitized request record with an explicit redactor.
     *
     * @param request Playwright request event
     * @param redactor diagnostic redactor
     * @return network record
     */
    public static NetworkRecord request(Request request, SecretPatternRedactor redactor) {
        return new NetworkRecord("request", request.method(), redactor.sanitizeUrl(request.url()), null, null,
                redactor.sanitizeHeaders(request.headers()));
    }

    /**
     * Creates a sanitized response record.
     *
     * @param response Playwright response event
     * @return network record
     */
    public static NetworkRecord response(Response response) {
        return response(response, DEFAULT_REDACTOR);
    }

    /**
     * Creates a sanitized response record with an explicit redactor.
     *
     * @param response Playwright response event
     * @param redactor diagnostic redactor
     * @return network record
     */
    public static NetworkRecord response(Response response, SecretPatternRedactor redactor) {
        return new NetworkRecord("response", response.request().method(), redactor.sanitizeUrl(response.url()), response.status(), null,
                redactor.sanitizeHeaders(response.headers()));
    }

    /**
     * Creates a sanitized failed-request record.
     *
     * @param request Playwright failed request event
     * @return network record
     */
    public static NetworkRecord failed(Request request) {
        return failed(request, DEFAULT_REDACTOR);
    }

    /**
     * Creates a sanitized failed-request record with an explicit redactor.
     *
     * @param request Playwright failed request event
     * @param redactor diagnostic redactor
     * @return network record
     */
    public static NetworkRecord failed(Request request, SecretPatternRedactor redactor) {
        return new NetworkRecord("failed", request.method(), redactor.sanitizeUrl(request.url()), null,
                redactor.sanitizeText(request.failure()), redactor.sanitizeHeaders(request.headers()));
    }

    /**
     * Formats the network event for reports and failure evidence logs.
     *
     * @return human-readable network event line
     */
    public String format() {
        String statusPart = status == null ? "" : " " + status;
        String failurePart = failure == null ? "" : " " + failure;
        String headersPart = headers == null || headers.isEmpty() ? "" : " " + headers;
        return "[%s] %s %s%s%s%s".formatted(event, method, url, statusPart, failurePart, headersPart);
    }

}
