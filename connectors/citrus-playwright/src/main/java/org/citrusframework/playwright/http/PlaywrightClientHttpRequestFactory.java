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

import com.microsoft.playwright.BrowserContext;

import java.net.URI;

import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.playwright.endpoint.PlaywrightBrowser;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpRequestFactory;

/**
 * Transport for the Citrus {@code HttpClient} that sends every request through a Playwright
 * browser context, so requests share the context's cookies (in both directions) and its HTTP
 * settings: extra headers, user agent, HTTP credentials, proxy, client certificates and HTTPS error
 * handling.
 *
 * <p>Plug it into any HTTP client with {@code HttpClientBuilder.requestFactory(...)}, or build a
 * ready client with {@code PlaywrightEndpoints.playwright().apiClient()}. Every request runs on
 * the browser's owner thread and fails fast on any other.</p>
 */
public class PlaywrightClientHttpRequestFactory implements ClientHttpRequestFactory {

    private final PlaywrightBrowser browser;
    private String contextAlias;
    private Double timeout;
    private Integer maxRedirects;
    private Integer maxRetries;
    private boolean ignoreHttpsErrors;

    /**
     * Creates a transport bound to a browser endpoint.
     *
     * @param browser browser whose contexts carry the requests
     */
    public PlaywrightClientHttpRequestFactory(PlaywrightBrowser browser) {
        if (browser == null) {
            throw new CitrusRuntimeException("Missing Playwright browser for the browser-session HTTP transport");
        }
        this.browser = browser;
    }

    /**
     * Binds requests to a named browser context. Without an alias, each request uses the
     * browser's current context at the time it is sent.
     *
     * @param alias context alias
     * @return this factory
     */
    public PlaywrightClientHttpRequestFactory context(String alias) {
        this.contextAlias = alias;
        return this;
    }

    /**
     * Sets the driver request timeout.
     *
     * @param timeoutMs timeout in milliseconds
     * @return this factory
     */
    public PlaywrightClientHttpRequestFactory timeout(double timeoutMs) {
        this.timeout = timeoutMs;
        return this;
    }

    /**
     * Sets how many redirects the driver follows; 0 returns the redirect response itself.
     *
     * @param maxRedirects redirect limit
     * @return this factory
     */
    public PlaywrightClientHttpRequestFactory maxRedirects(int maxRedirects) {
        this.maxRedirects = maxRedirects;
        return this;
    }

    /**
     * Retries a request after a connection reset, with the driver's backoff starting at 250 ms.
     *
     * @param maxRetries retry limit; 0 disables retries
     * @return this factory
     */
    public PlaywrightClientHttpRequestFactory maxRetries(int maxRetries) {
        this.maxRetries = maxRetries;
        return this;
    }

    /**
     * Accepts invalid TLS certificates for every request of this client. When false, the browser
     * context's own {@code ignoreHTTPSErrors} setting decides; this option can only widen it.
     *
     * @param ignoreHttpsErrors true to accept invalid certificates
     * @return this factory
     */
    public PlaywrightClientHttpRequestFactory ignoreHttpsErrors(boolean ignoreHttpsErrors) {
        this.ignoreHttpsErrors = ignoreHttpsErrors;
        return this;
    }

    @Override
    public ClientHttpRequest createRequest(URI uri, HttpMethod httpMethod) {
        return new PlaywrightClientHttpRequest(this, uri, httpMethod);
    }

    /**
     * Resolves the context a request is sent through, starting the browser first if needed.
     *
     * @return bound context, or the current one when no alias is set
     */
    BrowserContext resolveContext() {
        if (contextAlias == null) {
            return browser.getCurrentContext();
        }

        browser.start();
        return browser.findContext(contextAlias)
                .orElseThrow(() -> new CitrusRuntimeException("No Playwright browser context registered with alias '%s' - known aliases: %s"
                        .formatted(contextAlias, String.join(", ", browser.getContextAliases()))));
    }

    /**
     * Snapshot of the per-client driver settings for the next request.
     *
     * @return transport options
     */
    TransportOptions options() {
        return new TransportOptions(timeout, maxRedirects, maxRetries, ignoreHttpsErrors);
    }

    public PlaywrightBrowser getBrowser() {
        return browser;
    }

    public String getContextAlias() {
        return contextAlias;
    }

    public Double getTimeout() {
        return timeout;
    }

    public Integer getMaxRedirects() {
        return maxRedirects;
    }

    public Integer getMaxRetries() {
        return maxRetries;
    }

    public boolean isIgnoreHttpsErrors() {
        return ignoreHttpsErrors;
    }
}
