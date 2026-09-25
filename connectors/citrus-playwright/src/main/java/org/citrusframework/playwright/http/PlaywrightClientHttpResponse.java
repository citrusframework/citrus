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

import com.microsoft.playwright.APIResponse;
import com.microsoft.playwright.options.HttpHeader;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.ClientHttpResponse;

/**
 * Immutable snapshot of a Playwright API response.
 *
 * <p>The driver response is read completely and disposed while the request still runs on the
 * browser's owner thread, so nothing touches the driver once the response is handed to Spring.
 * Headers come from {@link APIResponse#headersArray()}, which keeps every value of a repeated
 * header such as {@code Set-Cookie}.</p>
 */
final class PlaywrightClientHttpResponse implements ClientHttpResponse {

    private static final byte[] EMPTY_BODY = new byte[0];

    private final HttpStatusCode statusCode;
    private final String statusText;
    private final HttpHeaders headers;
    private final byte[] body;

    private PlaywrightClientHttpResponse(HttpStatusCode statusCode, String statusText, HttpHeaders headers, byte[] body) {
        this.statusCode = statusCode;
        this.statusText = statusText;
        this.headers = HttpHeaders.readOnlyHttpHeaders(headers);
        this.body = body;
    }

    /**
     * Reads the driver response completely and disposes it.
     *
     * @param response driver response
     * @return detached response snapshot
     */
    static PlaywrightClientHttpResponse snapshot(APIResponse response) {
        try {
            HttpHeaders headers = new HttpHeaders();
            for (HttpHeader header : response.headersArray()) {
                headers.add(header.name, header.value);
            }
            TransportDetailHeaders.from(response.timing(), response.serverAddr(), response.securityDetails())
                    .forEach(headers::set);

            byte[] body = response.body();
            return new PlaywrightClientHttpResponse(HttpStatusCode.valueOf(response.status()), response.statusText(),
                    headers, body == null ? EMPTY_BODY : body);
        } finally {
            response.dispose();
        }
    }

    @Override
    public HttpStatusCode getStatusCode() {
        return statusCode;
    }

    @Override
    public String getStatusText() {
        return statusText;
    }

    @Override
    public HttpHeaders getHeaders() {
        return headers;
    }

    @Override
    public InputStream getBody() {
        return new ByteArrayInputStream(body);
    }

    @Override
    public void close() {
        // The driver response was disposed when the snapshot was taken.
    }
}
