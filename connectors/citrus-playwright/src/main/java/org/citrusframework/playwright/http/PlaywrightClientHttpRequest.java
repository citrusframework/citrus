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
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.PlaywrightException;

import java.io.IOException;
import java.net.URI;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.citrusframework.playwright.endpoint.PlaywrightBrowser;
import org.citrusframework.playwright.model.SecretPatternRedactor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.AbstractBufferingClientHttpRequest;
import org.springframework.http.client.ClientHttpResponse;

/**
 * Spring client request that is sent through a browser context's API request context, so it
 * carries the browser's cookies and HTTP settings.
 */
final class PlaywrightClientHttpRequest extends AbstractBufferingClientHttpRequest {

    /** The reason line of the driver's structured error, e.g. {@code message='Timeout 300ms exceeded.}. */
    private static final Pattern DRIVER_REASON = Pattern.compile("^\\s*message='(.*)$", Pattern.MULTILINE);

    private final PlaywrightClientHttpRequestFactory factory;
    private final URI uri;
    private final HttpMethod method;

    PlaywrightClientHttpRequest(PlaywrightClientHttpRequestFactory factory, URI uri, HttpMethod method) {
        this.factory = factory;
        this.uri = uri;
        this.method = method;
    }

    @Override
    public HttpMethod getMethod() {
        return method;
    }

    @Override
    public URI getURI() {
        return uri;
    }

    @Override
    protected ClientHttpResponse executeInternal(HttpHeaders headers, byte[] bufferedOutput) throws IOException {
        PlaywrightBrowser browser = factory.getBrowser();
        browser.assertActionThread();
        BrowserContext context = factory.resolveContext();
        FetchSpec fetch = FetchSpec.of(method, uri, headers, bufferedOutput, factory.options());

        try {
            APIResponse response = context.request().fetch(fetch.url(), fetch.toRequestOptions());
            return PlaywrightClientHttpResponse.snapshot(response);
        } catch (PlaywrightException e) {
            throw failure(browser.createRedactor(), e);
        }
    }

    /**
     * Reports a driver failure with only its reason. The driver message also carries the driver's
     * stack and a call log that lists the request headers it sent, including the browser's
     * cookies, so neither is copied and the driver exception is not chained. Its Java stack trace
     * is kept.
     */
    private IOException failure(SecretPatternRedactor redactor, PlaywrightException e) {
        String reason = reason(e);
        IOException failure = new IOException("Browser-session request %s %s failed: %s"
                .formatted(method.name(), redactor.sanitizeUrl(uri.toString()), redactor.sanitizeText(reason)));
        failure.setStackTrace(e.getStackTrace());
        return failure;
    }

    private static String reason(PlaywrightException e) {
        String message = e.getMessage();
        if (message == null || message.isBlank()) {
            return e.getClass().getSimpleName();
        }

        Matcher structured = DRIVER_REASON.matcher(message);
        if (structured.find()) {
            return structured.group(1).trim();
        }
        return message.lines().findFirst().orElse("").trim();
    }
}
