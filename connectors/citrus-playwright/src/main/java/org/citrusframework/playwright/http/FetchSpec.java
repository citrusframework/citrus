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

import com.microsoft.playwright.options.RequestOptions;

import java.net.URI;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.citrusframework.exceptions.CitrusRuntimeException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;

/**
 * The request a browser-session client sends through the driver, built from the Spring request.
 *
 * <p>This is the only place a driver {@link RequestOptions} is created. It exists so the mapping
 * can be tested without a browser, because the driver keeps its option fields private.</p>
 */
final class FetchSpec {

    /** Framing headers the driver computes itself. */
    private static final Set<String> DRIVER_OWNED_HEADERS = Set.of("content-length", "host", "connection", "transfer-encoding");

    private final String method;
    private final String url;
    private final Map<String, String> headers;
    private final byte[] body;
    private final Double timeout;
    private final Integer maxRedirects;

    private FetchSpec(String method, String url, Map<String, String> headers, byte[] body, Double timeout, Integer maxRedirects) {
        this.method = method;
        this.url = url;
        this.headers = Collections.unmodifiableMap(headers);
        this.body = body;
        this.timeout = timeout;
        this.maxRedirects = maxRedirects;
    }

    /**
     * Builds the fetch from a buffered Spring request.
     *
     * @param method request method
     * @param uri absolute request URI
     * @param headers request headers
     * @param body buffered request body
     * @param timeout driver request timeout in milliseconds, or null for the driver default
     * @param maxRedirects redirect limit, or null for the driver default
     * @return fetch specification
     * @throws CitrusRuntimeException when the request sets a {@code Cookie} header
     */
    static FetchSpec of(HttpMethod method, URI uri, HttpHeaders headers, byte[] body, Double timeout, Integer maxRedirects) {
        Map<String, String> forwarded = new LinkedHashMap<>();
        headers.forEach((name, values) -> {
            String normalized = name.toLowerCase(Locale.ROOT);
            if ("cookie".equals(normalized)) {
                throw new CitrusRuntimeException("A browser-session request must not set a Cookie header, because it would "
                        + "replace the cookies of the browser context. Add cookies to the browser context with "
                        + "playwright().cookies().add(...) instead");
            }
            if (!DRIVER_OWNED_HEADERS.contains(normalized)) {
                forwarded.put(name, String.join(", ", values));
            }
        });

        return new FetchSpec(method.name(), uri.toString(), forwarded, body, timeout, maxRedirects);
    }

    /**
     * Creates the driver options for this fetch. A non-2xx status never fails the fetch itself,
     * so the Citrus HTTP client's error handling strategy decides what happens with it.
     *
     * @return driver request options
     */
    RequestOptions toRequestOptions() {
        RequestOptions options = RequestOptions.create()
                .setMethod(method)
                .setFailOnStatusCode(false);
        headers.forEach(options::setHeader);
        if (body.length > 0) {
            options.setData(body);
        }
        if (timeout != null) {
            options.setTimeout(timeout);
        }
        if (maxRedirects != null) {
            options.setMaxRedirects(maxRedirects);
        }
        return options;
    }

    String method() {
        return method;
    }

    String url() {
        return url;
    }

    Map<String, String> headers() {
        return headers;
    }

    byte[] body() {
        return body;
    }

    Double timeout() {
        return timeout;
    }

    Integer maxRedirects() {
        return maxRedirects;
    }
}
