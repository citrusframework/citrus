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

package org.citrusframework.playwright.endpoint;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Playwright;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import org.citrusframework.base.endpoint.AbstractEndpointBuilder;

/**
 * Fluent endpoint builder for the Citrus Playwright browser endpoint.
 *
 * <p>The builder configures Playwright creation, browser launch, browser
 * context defaults, observability limits, and failure-evidence artifact
 * settings before producing a {@link PlaywrightBrowser} endpoint.</p>
 */
public class PlaywrightEndpointBuilder extends AbstractEndpointBuilder<PlaywrightBrowser> {

    private final PlaywrightBrowser endpoint = new PlaywrightBrowser();

    /**
     * Selects the Playwright browser engine.
     *
     * @param browserType supported value such as {@code chromium}, {@code firefox}, or {@code webkit}
     * @return this builder
     */
    public PlaywrightEndpointBuilder browserType(String browserType) {
        endpoint.getEndpointConfiguration().setBrowserType(browserType);
        return this;
    }

    /**
     * Configures whether the browser launches in headless mode.
     *
     * @param headless true for headless mode
     * @return this builder
     */
    public PlaywrightEndpointBuilder headless(boolean headless) {
        endpoint.getEndpointConfiguration().setHeadless(headless);
        return this;
    }

    /**
     * Adds Playwright slow-motion delay in milliseconds.
     *
     * @param slowMo delay passed to Playwright launch options
     * @return this builder
     */
    public PlaywrightEndpointBuilder slowMo(double slowMo) {
        endpoint.getEndpointConfiguration().setSlowMo(slowMo);
        return this;
    }

    /**
     * Selects a Playwright browser channel such as Chrome or Edge.
     *
     * @param channel browser channel name
     * @return this builder
     */
    public PlaywrightEndpointBuilder channel(String channel) {
        endpoint.getEndpointConfiguration().setChannel(channel);
        return this;
    }

    /**
     * Sets the base URL used by context/page navigation.
     *
     * @param baseUrl base URL for relative navigation
     * @return this builder
     */
    public PlaywrightEndpointBuilder baseUrl(String baseUrl) {
        endpoint.getEndpointConfiguration().setBaseUrl(baseUrl);
        return this;
    }

    /**
     * Sets the optional URL opened immediately after browser startup.
     *
     * @param startPageUrl startup URL
     * @return this builder
     */
    public PlaywrightEndpointBuilder startPageUrl(String startPageUrl) {
        endpoint.getEndpointConfiguration().setStartPageUrl(startPageUrl);
        return this;
    }

    /**
     * Sets the default locator/action timeout for pages created by the endpoint.
     *
     * @param timeout timeout in milliseconds
     * @return this builder
     */
    public PlaywrightEndpointBuilder defaultTimeout(long timeout) {
        endpoint.getEndpointConfiguration().setDefaultTimeout(timeout);
        return this;
    }

    /**
     * Sets the default navigation timeout for pages created by the endpoint.
     *
     * @param timeout timeout in milliseconds
     * @return this builder
     */
    public PlaywrightEndpointBuilder defaultNavigationTimeout(long timeout) {
        endpoint.getEndpointConfiguration().setDefaultNavigationTimeout(timeout);
        return this;
    }

    /**
     * Sets the directory used by Playwright for downloaded files.
     *
     * @param downloadsPath download directory
     * @return this builder
     */
    public PlaywrightEndpointBuilder downloadsPath(Path downloadsPath) {
        endpoint.getEndpointConfiguration().setDownloadsPath(downloadsPath);
        return this;
    }

    /**
     * Enables automatic context tracing for the endpoint lifecycle.
     *
     * @param tracingEnabled true to start tracing when the endpoint starts
     * @return this builder
     */
    public PlaywrightEndpointBuilder tracingEnabled(boolean tracingEnabled) {
        endpoint.getEndpointConfiguration().setTracingEnabled(tracingEnabled);
        return this;
    }

    /**
     * Sets the base directory for failure evidence artifacts.
     *
     * @param artifactDirectory artifact root directory
     * @return this builder
     */
    public PlaywrightEndpointBuilder artifactDirectory(Path artifactDirectory) {
        endpoint.getEndpointConfiguration().setArtifactDirectory(artifactDirectory);
        return this;
    }

    /**
     * Sets the maximum console messages retained per captured page.
     *
     * @param limit bounded buffer size
     * @return this builder
     */
    public PlaywrightEndpointBuilder consoleMessageLimit(int limit) {
        endpoint.getEndpointConfiguration().setConsoleMessageLimit(limit);
        return this;
    }

    /**
     * Sets the maximum network events retained per captured page.
     *
     * @param limit bounded buffer size
     * @return this builder
     */
    public PlaywrightEndpointBuilder networkRecordLimit(int limit) {
        endpoint.getEndpointConfiguration().setNetworkRecordLimit(limit);
        return this;
    }

    /**
     * Enables screenshot capture when a Citrus test fails.
     *
     * @param enabled true to write {@code failure.png}
     * @return this builder
     */
    public PlaywrightEndpointBuilder captureFailureScreenshot(boolean enabled) {
        endpoint.getEndpointConfiguration().setCaptureFailureScreenshot(enabled);
        return this;
    }

    /**
     * Enables page-source capture when a Citrus test fails.
     *
     * @param enabled true to write {@code page.html}
     * @return this builder
     */
    public PlaywrightEndpointBuilder captureFailurePageSource(boolean enabled) {
        endpoint.getEndpointConfiguration().setCaptureFailurePageSource(enabled);
        return this;
    }

    /**
     * Enables trace capture when a Citrus test fails.
     *
     * @param enabled true to write {@code trace.zip}
     * @return this builder
     */
    public PlaywrightEndpointBuilder captureFailureTrace(boolean enabled) {
        endpoint.getEndpointConfiguration().setCaptureFailureTrace(enabled);
        return this;
    }

    /**
     * Enables console log capture when a Citrus test fails.
     *
     * @param enabled true to write {@code console.log}
     * @return this builder
     */
    public PlaywrightEndpointBuilder captureFailureConsoleMessages(boolean enabled) {
        endpoint.getEndpointConfiguration().setCaptureFailureConsoleMessages(enabled);
        return this;
    }

    /**
     * Enables network log capture when a Citrus test fails.
     *
     * @param enabled true to write {@code network.log}
     * @return this builder
     */
    public PlaywrightEndpointBuilder captureFailureNetworkRequests(boolean enabled) {
        endpoint.getEndpointConfiguration().setCaptureFailureNetworkRequests(enabled);
        return this;
    }

    /**
     * Enables Markdown summary capture when a Citrus test fails.
     *
     * @param enabled true to write {@code failure-summary.md}
     * @return this builder
     */
    public PlaywrightEndpointBuilder captureFailureSummary(boolean enabled) {
        endpoint.getEndpointConfiguration().setCaptureFailureSummary(enabled);
        return this;
    }

    /**
     * Supplies low-level Playwright create options.
     *
     * @param options Playwright create options
     * @return this builder
     */
    public PlaywrightEndpointBuilder createOptions(Playwright.CreateOptions options) {
        endpoint.getEndpointConfiguration().setCreateOptions(options);
        return this;
    }

    /**
     * Supplies low-level browser launch options.
     *
     * @param options Playwright launch options
     * @return this builder
     */
    public PlaywrightEndpointBuilder launchOptions(BrowserType.LaunchOptions options) {
        endpoint.getEndpointConfiguration().setLaunchOptions(options);
        return this;
    }

    /**
     * Connects to a remote Playwright browser WebSocket endpoint instead of launching locally.
     *
     * @param wsEndpoint browser WebSocket endpoint
     * @return this builder
     */
    public PlaywrightEndpointBuilder connectWsEndpoint(String wsEndpoint) {
        endpoint.getEndpointConfiguration().setConnectWsEndpoint(wsEndpoint);
        return this;
    }

    /**
     * Supplies low-level Playwright connect options for remote WebSocket browser connections.
     *
     * @param options Playwright connect options
     * @return this builder
     */
    public PlaywrightEndpointBuilder connectOptions(BrowserType.ConnectOptions options) {
        endpoint.getEndpointConfiguration().setConnectOptions(options);
        return this;
    }

    /**
     * Connects to a remote Chrome DevTools Protocol endpoint instead of launching locally.
     *
     * @param cdpEndpoint Chrome DevTools Protocol endpoint
     * @return this builder
     */
    public PlaywrightEndpointBuilder connectOverCdpEndpoint(String cdpEndpoint) {
        endpoint.getEndpointConfiguration().setConnectOverCdpEndpoint(cdpEndpoint);
        return this;
    }

    /**
     * Supplies low-level Playwright connect options for remote Chrome DevTools Protocol connections.
     *
     * @param options Playwright CDP connect options
     * @return this builder
     */
    public PlaywrightEndpointBuilder connectOverCdpOptions(BrowserType.ConnectOverCDPOptions options) {
        endpoint.getEndpointConfiguration().setConnectOverCdpOptions(options);
        return this;
    }

    /**
     * Supplies default context options for contexts created by this endpoint.
     *
     * @param options Playwright context options
     * @return this builder
     */
    public PlaywrightEndpointBuilder contextOptions(Browser.NewContextOptions options) {
        endpoint.getEndpointConfiguration().setContextOptions(options);
        return this;
    }

    /**
     * Adds extra case-insensitive secret name/value patterns used by diagnostics redaction.
     *
     * @param patterns secret patterns
     * @return this builder
     */
    public PlaywrightEndpointBuilder secretPatterns(String... patterns) {
        endpoint.getEndpointConfiguration().setSecretPatterns(patterns == null ? List.of() : Arrays.asList(patterns));
        return this;
    }

    /**
     * Adds extra case-insensitive secret name/value patterns used by diagnostics redaction.
     *
     * @param patterns secret patterns
     * @return this builder
     */
    public PlaywrightEndpointBuilder secretPatterns(List<String> patterns) {
        endpoint.getEndpointConfiguration().setSecretPatterns(patterns);
        return this;
    }

    @Override
    protected PlaywrightBrowser getEndpoint() {
        return endpoint;
    }
}
