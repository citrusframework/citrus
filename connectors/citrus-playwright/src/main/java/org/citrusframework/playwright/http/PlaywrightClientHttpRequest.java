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
        FetchSpec fetch = FetchSpec.of(method, uri, headers, bufferedOutput, factory.getTimeout(), factory.getMaxRedirects());

        APIResponse response;
        try {
            response = context.request().fetch(fetch.url(), fetch.toRequestOptions());
        } catch (PlaywrightException e) {
            throw failure(browser.createRedactor(), e);
        }
        return PlaywrightClientHttpResponse.snapshot(response);
    }

    /**
     * Reports a driver failure without its call log, which lists the request headers the driver
     * sent, including the browser's cookies. The driver exception is not chained for the same
     * reason; its stack trace is kept.
     */
    private IOException failure(SecretPatternRedactor redactor, PlaywrightException e) {
        String reason = e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage().lines().findFirst().orElse("");
        IOException failure = new IOException("Browser-session request %s %s failed: %s"
                .formatted(method.name(), redactor.sanitizeUrl(uri.toString()), redactor.sanitizeText(reason)));
        failure.setStackTrace(e.getStackTrace());
        return failure;
    }
}
