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
import java.util.List;

import org.citrusframework.endpoint.AbstractEndpointConfiguration;

/**
 * Endpoint configuration for a Citrus-managed Playwright browser.
 *
 * <p>The configuration keeps user-facing launch defaults, context defaults,
 * artifact locations, bounded observability limits, and failure-evidence flags
 * in one object so Java DSL, Spring configuration, and Citrus endpoint builder
 * usage share the same behavior.</p>
 */
public class PlaywrightBrowserConfiguration extends AbstractEndpointConfiguration {

    private String browserType = "chromium";
    private Boolean headless = true;
    private Double slowMo;
    private String channel;
    private String baseUrl;
    private String startPageUrl;
    private Long defaultTimeout = 30_000L;
    private Long defaultNavigationTimeout = 30_000L;
    private Path downloadsPath;
    private boolean tracingEnabled;
    private Path artifactDirectory = Path.of("target", "playwright");
    private int consoleMessageLimit = 200;
    private int networkRecordLimit = 500;
    private boolean captureFailureScreenshot;
    private boolean captureFailurePageSource;
    private boolean captureFailureTrace;
    private boolean captureFailureConsoleMessages;
    private boolean captureFailureNetworkRequests;
    private boolean captureFailureSummary;
    private Playwright.CreateOptions createOptions;
    private BrowserType.LaunchOptions launchOptions;
    private String connectWsEndpoint;
    private BrowserType.ConnectOptions connectOptions;
    private String connectOverCdpEndpoint;
    private BrowserType.ConnectOverCDPOptions connectOverCdpOptions;
    private Browser.NewContextOptions contextOptions;
    private List<String> secretPatterns = List.of();

    /**
     * Returns the configured Playwright browser type.
     *
     * @return browser type
     */
    public String getBrowserType() {
        return browserType;
    }

    /**
     * Sets the Playwright browser type.
     *
     * @param browserType browser type such as {@code chromium}, {@code firefox}, or {@code webkit}
     */
    public void setBrowserType(String browserType) {
        this.browserType = browserType;
    }

    /**
     * Returns whether the browser launches headless.
     *
     * @return headless flag
     */
    public Boolean getHeadless() {
        return headless;
    }

    /**
     * Sets whether the browser launches headless.
     *
     * @param headless headless flag
     */
    public void setHeadless(Boolean headless) {
        this.headless = headless;
    }

    /**
     * Returns the Playwright slow-motion delay.
     *
     * @return slow-motion delay in milliseconds
     */
    public Double getSlowMo() {
        return slowMo;
    }

    /**
     * Sets the Playwright slow-motion delay.
     *
     * @param slowMo delay in milliseconds
     */
    public void setSlowMo(Double slowMo) {
        this.slowMo = slowMo;
    }

    /**
     * Returns the configured browser channel.
     *
     * @return browser channel
     */
    public String getChannel() {
        return channel;
    }

    /**
     * Sets the browser channel.
     *
     * @param channel browser channel
     */
    public void setChannel(String channel) {
        this.channel = channel;
    }

    /**
     * Returns the base URL used by context/page navigation.
     *
     * @return base URL
     */
    public String getBaseUrl() {
        return baseUrl;
    }

    /**
     * Sets the base URL used by context/page navigation.
     *
     * @param baseUrl base URL
     */
    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    /**
     * Returns the URL opened immediately after browser startup.
     *
     * @return startup URL
     */
    public String getStartPageUrl() {
        return startPageUrl;
    }

    /**
     * Sets the URL opened immediately after browser startup.
     *
     * @param startPageUrl startup URL
     */
    public void setStartPageUrl(String startPageUrl) {
        this.startPageUrl = startPageUrl;
    }

    /**
     * Returns the default action timeout for created pages.
     *
     * @return timeout in milliseconds
     */
    public Long getDefaultTimeout() {
        return defaultTimeout;
    }

    /**
     * Sets the default action timeout for created pages.
     *
     * @param defaultTimeout timeout in milliseconds
     */
    public void setDefaultTimeout(Long defaultTimeout) {
        this.defaultTimeout = defaultTimeout;
    }

    /**
     * Returns the default navigation timeout for created pages.
     *
     * @return timeout in milliseconds
     */
    public Long getDefaultNavigationTimeout() {
        return defaultNavigationTimeout;
    }

    /**
     * Sets the default navigation timeout for created pages.
     *
     * @param defaultNavigationTimeout timeout in milliseconds
     */
    public void setDefaultNavigationTimeout(Long defaultNavigationTimeout) {
        this.defaultNavigationTimeout = defaultNavigationTimeout;
    }

    /**
     * Returns the configured download directory.
     *
     * @return download directory
     */
    public Path getDownloadsPath() {
        return downloadsPath;
    }

    /**
     * Sets the configured download directory.
     *
     * @param downloadsPath download directory
     */
    public void setDownloadsPath(Path downloadsPath) {
        this.downloadsPath = downloadsPath;
    }

    /**
     * Returns whether endpoint lifecycle tracing is enabled.
     *
     * @return tracing flag
     */
    public boolean isTracingEnabled() {
        return tracingEnabled;
    }

    /**
     * Sets whether endpoint lifecycle tracing is enabled.
     *
     * @param tracingEnabled tracing flag
     */
    public void setTracingEnabled(boolean tracingEnabled) {
        this.tracingEnabled = tracingEnabled;
    }

    /**
     * Returns the failure-evidence artifact root directory.
     *
     * @return artifact directory
     */
    public Path getArtifactDirectory() {
        return artifactDirectory;
    }

    /**
     * Sets the failure-evidence artifact root directory.
     *
     * @param artifactDirectory artifact directory
     */
    public void setArtifactDirectory(Path artifactDirectory) {
        this.artifactDirectory = artifactDirectory;
    }

    /**
     * Returns the per-page console message capture limit.
     *
     * @return console message limit
     */
    public int getConsoleMessageLimit() {
        return consoleMessageLimit;
    }

    /**
     * Sets the per-page console message capture limit.
     *
     * @param consoleMessageLimit console message limit
     */
    public void setConsoleMessageLimit(int consoleMessageLimit) {
        this.consoleMessageLimit = consoleMessageLimit;
    }

    /**
     * Returns the per-page network record capture limit.
     *
     * @return network record limit
     */
    public int getNetworkRecordLimit() {
        return networkRecordLimit;
    }

    /**
     * Sets the per-page network record capture limit.
     *
     * @param networkRecordLimit network record limit
     */
    public void setNetworkRecordLimit(int networkRecordLimit) {
        this.networkRecordLimit = networkRecordLimit;
    }

    /**
     * Returns whether screenshots are captured on test failure.
     *
     * @return screenshot capture flag
     */
    public boolean isCaptureFailureScreenshot() {
        return captureFailureScreenshot;
    }

    /**
     * Sets whether screenshots are captured on test failure.
     *
     * @param captureFailureScreenshot screenshot capture flag
     */
    public void setCaptureFailureScreenshot(boolean captureFailureScreenshot) {
        this.captureFailureScreenshot = captureFailureScreenshot;
    }

    /**
     * Returns whether page source is captured on test failure.
     *
     * @return page-source capture flag
     */
    public boolean isCaptureFailurePageSource() {
        return captureFailurePageSource;
    }

    /**
     * Sets whether page source is captured on test failure.
     *
     * @param captureFailurePageSource page-source capture flag
     */
    public void setCaptureFailurePageSource(boolean captureFailurePageSource) {
        this.captureFailurePageSource = captureFailurePageSource;
    }

    /**
     * Returns whether trace archives are captured on test failure.
     *
     * @return trace capture flag
     */
    public boolean isCaptureFailureTrace() {
        return captureFailureTrace;
    }

    /**
     * Sets whether trace archives are captured on test failure.
     *
     * @param captureFailureTrace trace capture flag
     */
    public void setCaptureFailureTrace(boolean captureFailureTrace) {
        this.captureFailureTrace = captureFailureTrace;
    }

    /**
     * Returns whether console logs are captured on test failure.
     *
     * @return console capture flag
     */
    public boolean isCaptureFailureConsoleMessages() {
        return captureFailureConsoleMessages;
    }

    /**
     * Sets whether console logs are captured on test failure.
     *
     * @param captureFailureConsoleMessages console capture flag
     */
    public void setCaptureFailureConsoleMessages(boolean captureFailureConsoleMessages) {
        this.captureFailureConsoleMessages = captureFailureConsoleMessages;
    }

    /**
     * Returns whether network logs are captured on test failure.
     *
     * @return network capture flag
     */
    public boolean isCaptureFailureNetworkRequests() {
        return captureFailureNetworkRequests;
    }

    /**
     * Sets whether network logs are captured on test failure.
     *
     * @param captureFailureNetworkRequests network capture flag
     */
    public void setCaptureFailureNetworkRequests(boolean captureFailureNetworkRequests) {
        this.captureFailureNetworkRequests = captureFailureNetworkRequests;
    }

    /**
     * Returns whether a Markdown failure summary is captured on test failure.
     *
     * @return failure-summary capture flag
     */
    public boolean isCaptureFailureSummary() {
        return captureFailureSummary;
    }

    /**
     * Sets whether a Markdown failure summary is captured on test failure.
     *
     * @param captureFailureSummary failure-summary capture flag
     */
    public void setCaptureFailureSummary(boolean captureFailureSummary) {
        this.captureFailureSummary = captureFailureSummary;
    }

    /**
     * Returns low-level Playwright create options.
     *
     * @return create options
     */
    public Playwright.CreateOptions getCreateOptions() {
        return createOptions;
    }

    /**
     * Sets low-level Playwright create options.
     *
     * @param createOptions create options
     */
    public void setCreateOptions(Playwright.CreateOptions createOptions) {
        this.createOptions = createOptions;
    }

    /**
     * Returns low-level browser launch options.
     *
     * @return launch options
     */
    public BrowserType.LaunchOptions getLaunchOptions() {
        return launchOptions;
    }

    /**
     * Sets low-level browser launch options.
     *
     * @param launchOptions launch options
     */
    public void setLaunchOptions(BrowserType.LaunchOptions launchOptions) {
        this.launchOptions = launchOptions;
    }

    /**
     * Returns the remote Playwright browser WebSocket endpoint.
     *
     * @return browser WebSocket endpoint
     */
    public String getConnectWsEndpoint() {
        return connectWsEndpoint;
    }

    /**
     * Sets the remote Playwright browser WebSocket endpoint.
     *
     * @param connectWsEndpoint browser WebSocket endpoint
     */
    public void setConnectWsEndpoint(String connectWsEndpoint) {
        this.connectWsEndpoint = connectWsEndpoint;
    }

    /**
     * Returns low-level Playwright connect options for {@code BrowserType.connect(...)}.
     *
     * @return connect options
     */
    public BrowserType.ConnectOptions getConnectOptions() {
        return connectOptions;
    }

    /**
     * Sets low-level Playwright connect options for {@code BrowserType.connect(...)}.
     *
     * @param connectOptions connect options
     */
    public void setConnectOptions(BrowserType.ConnectOptions connectOptions) {
        this.connectOptions = connectOptions;
    }

    /**
     * Returns the remote Chrome DevTools Protocol endpoint.
     *
     * @return CDP endpoint
     */
    public String getConnectOverCdpEndpoint() {
        return connectOverCdpEndpoint;
    }

    /**
     * Sets the remote Chrome DevTools Protocol endpoint.
     *
     * @param connectOverCdpEndpoint CDP endpoint
     */
    public void setConnectOverCdpEndpoint(String connectOverCdpEndpoint) {
        this.connectOverCdpEndpoint = connectOverCdpEndpoint;
    }

    /**
     * Returns low-level Playwright connect options for {@code BrowserType.connectOverCDP(...)}.
     *
     * @return CDP connect options
     */
    public BrowserType.ConnectOverCDPOptions getConnectOverCdpOptions() {
        return connectOverCdpOptions;
    }

    /**
     * Sets low-level Playwright connect options for {@code BrowserType.connectOverCDP(...)}.
     *
     * @param connectOverCdpOptions CDP connect options
     */
    public void setConnectOverCdpOptions(BrowserType.ConnectOverCDPOptions connectOverCdpOptions) {
        this.connectOverCdpOptions = connectOverCdpOptions;
    }

    /**
     * Returns default browser context options.
     *
     * @return context options
     */
    public Browser.NewContextOptions getContextOptions() {
        return contextOptions;
    }

    /**
     * Sets default browser context options.
     *
     * @param contextOptions context options
     */
    public void setContextOptions(Browser.NewContextOptions contextOptions) {
        this.contextOptions = contextOptions;
    }

    /**
     * Returns additional case-insensitive secret name/value patterns used in diagnostics.
     *
     * @return immutable configured secret patterns
     */
    public List<String> getSecretPatterns() {
        return secretPatterns;
    }

    /**
     * Sets additional case-insensitive secret name/value patterns used in diagnostics.
     *
     * @param secretPatterns configured secret patterns
     */
    public void setSecretPatterns(List<String> secretPatterns) {
        this.secretPatterns = secretPatterns == null ? List.of() : List.copyOf(secretPatterns);
    }
}
