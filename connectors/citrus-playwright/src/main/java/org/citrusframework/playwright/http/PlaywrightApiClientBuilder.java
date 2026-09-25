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

import org.citrusframework.base.endpoint.AbstractEndpointBuilder;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.http.client.HttpClient;
import org.citrusframework.http.client.HttpEndpointConfiguration;
import org.citrusframework.message.ErrorHandlingStrategy;
import org.citrusframework.playwright.endpoint.PlaywrightBrowser;
import org.citrusframework.util.StringUtils;

/**
 * Builds a Citrus {@link HttpClient} whose requests travel through a Playwright browser
 * context, so they carry the browser's session. Tests use the result with the standard
 * {@code http()} actions, exactly like any other HTTP client.
 *
 * <p>The request URL defaults to the browser endpoint's base URL. Settings that configure the
 * replaced transport (request factory, rest template, HttpComponents client) are deliberately not
 * exposed; use {@link PlaywrightClientHttpRequestFactory} with the plain {@code HttpClientBuilder}
 * for full control.</p>
 */
public class PlaywrightApiClientBuilder extends AbstractEndpointBuilder<HttpClient> {

    private final HttpClient endpoint = new HttpClient();

    private PlaywrightBrowser browser;
    private String browserName;
    private String contextAlias;
    private String requestUrl;
    private TransportOptions transportOptions = TransportOptions.DEFAULTS;

    /**
     * Binds the client to a browser endpoint.
     *
     * @param browser browser endpoint
     * @return this builder
     */
    public PlaywrightApiClientBuilder browser(PlaywrightBrowser browser) {
        this.browser = browser;
        return this;
    }

    /**
     * Binds the client to a browser endpoint resolved by name through the reference resolver
     * when the client is built.
     *
     * @param browserName browser endpoint name
     * @return this builder
     */
    public PlaywrightApiClientBuilder browser(String browserName) {
        this.browserName = browserName;
        return this;
    }

    /**
     * Binds requests to a named browser context. Without an alias, each request uses the
     * browser's current context at the time it is sent.
     *
     * @param alias context alias
     * @return this builder
     */
    public PlaywrightApiClientBuilder context(String alias) {
        this.contextAlias = alias;
        return this;
    }

    /**
     * Sets the request URL that request paths are appended to. Defaults to the browser
     * endpoint's base URL.
     *
     * @param requestUrl request URL
     * @return this builder
     */
    public PlaywrightApiClientBuilder requestUrl(String requestUrl) {
        this.requestUrl = requestUrl;
        return this;
    }

    /**
     * Sets both the driver request timeout and the HTTP client timeout.
     *
     * @param timeout timeout in milliseconds
     * @return this builder
     */
    public PlaywrightApiClientBuilder timeout(long timeout) {
        this.transportOptions = transportOptions.withTimeout(timeout);
        endpoint.getEndpointConfiguration().setTimeout(timeout);
        return this;
    }

    /**
     * Sets how many redirects the driver follows; 0 returns the redirect response itself.
     *
     * @param maxRedirects redirect limit
     * @return this builder
     */
    public PlaywrightApiClientBuilder maxRedirects(int maxRedirects) {
        this.transportOptions = transportOptions.withMaxRedirects(maxRedirects);
        return this;
    }

    /**
     * Retries a request after a connection reset, with the driver's backoff starting at 250 ms.
     *
     * @param maxRetries retry limit; 0 disables retries
     * @return this builder
     */
    public PlaywrightApiClientBuilder maxRetries(int maxRetries) {
        this.transportOptions = transportOptions.withMaxRetries(maxRetries);
        return this;
    }

    /**
     * Accepts invalid TLS certificates for every request of this client. When false, the browser
     * context's own {@code ignoreHTTPSErrors} setting decides; this option can only widen it.
     *
     * @param ignoreHttpsErrors true to accept invalid certificates
     * @return this builder
     */
    public PlaywrightApiClientBuilder ignoreHttpsErrors(boolean ignoreHttpsErrors) {
        this.transportOptions = transportOptions.withIgnoreHttpsErrors(ignoreHttpsErrors);
        return this;
    }

    /**
     * Maps {@code Set-Cookie} response headers to message cookies, as on any HTTP client.
     *
     * @param handleCookies true to map response cookies
     * @return this builder
     */
    public PlaywrightApiClientBuilder handleCookies(boolean handleCookies) {
        endpoint.getEndpointConfiguration().setHandleCookies(handleCookies);
        return this;
    }

    /**
     * Sets how the client treats non-2xx responses, as on any HTTP client. The default
     * delivers them as responses for the test to verify.
     *
     * @param errorHandlingStrategy error handling strategy
     * @return this builder
     */
    public PlaywrightApiClientBuilder errorHandlingStrategy(ErrorHandlingStrategy errorHandlingStrategy) {
        endpoint.getEndpointConfiguration().setErrorHandlingStrategy(errorHandlingStrategy);
        return this;
    }

    @Override
    public HttpClient build() {
        PlaywrightBrowser resolvedBrowser = resolveBrowser();

        String url = StringUtils.hasText(requestUrl) ? requestUrl : resolvedBrowser.getEndpointConfiguration().getBaseUrl();
        if (!StringUtils.hasText(url)) {
            throw new CitrusRuntimeException(("Missing request URL for the Playwright API client - set requestUrl(...) "
                    + "or configure a baseUrl on the browser endpoint '%s'").formatted(resolvedBrowser.getName()));
        }

        HttpEndpointConfiguration configuration = endpoint.getEndpointConfiguration();
        configuration.setRequestUrl(url);
        configuration.setRequestFactory(new PlaywrightClientHttpRequestFactory(resolvedBrowser, contextAlias, transportOptions));

        return super.build();
    }

    /**
     * Never offers itself for a plain {@code HttpClient} endpoint: the endpoint factory picks the
     * first registered builder that supports the field type, and this one needs a browser.
     */
    @Override
    public boolean supports(Class<?> endpointType) {
        return false;
    }

    @Override
    protected HttpClient getEndpoint() {
        return endpoint;
    }

    private PlaywrightBrowser resolveBrowser() {
        if (browser != null) {
            return browser;
        }

        if (!StringUtils.hasText(browserName)) {
            throw new CitrusRuntimeException("Missing Playwright browser for the API client - bind one with browser(...)");
        }

        if (referenceResolver == null || !referenceResolver.isResolvable(browserName, PlaywrightBrowser.class)) {
            throw new CitrusRuntimeException("Failed to resolve Playwright browser endpoint '%s' for the API client%s"
                    .formatted(browserName, referenceResolver == null ? " - no reference resolver is set" : ""));
        }

        return referenceResolver.resolve(browserName, PlaywrightBrowser.class);
    }
}
