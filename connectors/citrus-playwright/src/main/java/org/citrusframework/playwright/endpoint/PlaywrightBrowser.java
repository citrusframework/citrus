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
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.Tracing;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import org.citrusframework.api.common.ShutdownPhase;
import org.citrusframework.context.TestContext;
import org.citrusframework.endpoint.AbstractEndpoint;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.message.Message;
import org.citrusframework.messaging.Consumer;
import org.citrusframework.messaging.Producer;
import org.citrusframework.playwright.actions.PlaywrightAction;
import org.citrusframework.playwright.model.DownloadMetadata;
import org.citrusframework.playwright.model.SecretPatternRedactor;
import org.citrusframework.playwright.support.ActivePlaywrightBrowsers;
import org.citrusframework.playwright.support.ConsoleCaptureRegistry;
import org.citrusframework.playwright.support.NetworkCaptureRegistry;
import org.citrusframework.util.StringUtils;

/**
 * Citrus endpoint that owns a Playwright runtime, browser, contexts, pages, and
 * per-page observability state.
 *
 * <p>The endpoint keeps a current context/page for simple tests while also
 * exposing named context and page aliases for multi-user and multi-tab flows.
 * Lifecycle methods are synchronized because a Citrus test can start or stop
 * endpoints from different execution callbacks.</p>
 */
public class PlaywrightBrowser extends AbstractEndpoint implements Producer, ShutdownPhase {

    public static final String DEFAULT_ALIAS = "default";

    private Playwright playwright;
    private Browser browser;
    private BrowserContext currentContext;
    private Page currentPage;
    private final Map<String, BrowserContext> contexts = new LinkedHashMap<>();
    private final Map<String, Page> pages = new LinkedHashMap<>();
    private final Map<Page, BrowserContext> pageContexts = new LinkedHashMap<>();
    private final ConsoleCaptureRegistry consoleCaptureRegistry = new ConsoleCaptureRegistry();
    private final NetworkCaptureRegistry networkCaptureRegistry = new NetworkCaptureRegistry();
    private DownloadMetadata latestDownloadMetadata;
    private Long actionOwnerThreadId;
    private boolean tracingStopped;

    public PlaywrightBrowser() {
        this(new PlaywrightBrowserConfiguration());
    }

    public PlaywrightBrowser(PlaywrightBrowserConfiguration endpointConfiguration) {
        super(endpointConfiguration);
    }

    /**
     * Returns the strongly typed Playwright endpoint configuration.
     *
     * @return endpoint configuration
     */
    @Override
    public PlaywrightBrowserConfiguration getEndpointConfiguration() {
        return (PlaywrightBrowserConfiguration) super.getEndpointConfiguration();
    }

    /**
     * Creates the Citrus producer facade used to execute Playwright actions.
     *
     * @return this endpoint as a producer
     */
    @Override
    public Producer createProducer() {
        return this;
    }

    /**
     * Rejects consumer creation because the browser endpoint only sends actions.
     *
     * @return never returns
     */
    @Override
    public Consumer createConsumer() {
        throw new CitrusRuntimeException("Playwright browser does not support receiving messages");
    }

    /**
     * Executes a {@link PlaywrightAction} payload in the supplied Citrus context.
     *
     * @param message Citrus message containing a Playwright action payload
     * @param context active Citrus test context
     */
    @Override
    public void send(Message message, TestContext context) {
        message.getPayload(PlaywrightAction.class).execute(context);
    }

    /**
     * Reports whether Playwright and the browser process are currently running.
     *
     * @return true when the endpoint has a connected browser
     */
    public synchronized boolean isStarted() {
        return playwright != null && browser != null && browser.isConnected();
    }

    /**
     * Starts Playwright, launches the configured browser, creates the default
     * context/page aliases, and registers the endpoint for failure evidence.
     */
    public synchronized void start() {
        if (isStarted()) {
            return;
        }

        try {
            PlaywrightBrowserConfiguration config = getEndpointConfiguration();
            playwright = createPlaywright(config);
            browser = launchBrowser(playwright, config);
            currentContext = createBrowserContext(browser, config);
            currentPage = createPage(currentContext);
            applyTimeouts(config, currentPage);
            registerContext(DEFAULT_ALIAS, currentContext);
            registerPage(DEFAULT_ALIAS, currentPage);

            if (config.isTracingEnabled()) {
                tracingStopped = false;
                currentContext.tracing().start(new Tracing.StartOptions().setScreenshots(true).setSnapshots(true));
            }

            if (StringUtils.hasText(config.getStartPageUrl())) {
                currentPage.navigate(config.getStartPageUrl());
            }
            ActivePlaywrightBrowsers.register(this);
        } catch (RuntimeException e) {
            stop();
            throw e;
        }
    }

    /**
     * Stops tracing on the current browser context and writes the trace archive
     * to the given path.
     *
     * <p>Playwright allows tracing to be stopped only once per context. Failure
     * evidence capture and browser teardown both want the trace, so the first
     * caller wins and later calls are no-ops - otherwise the teardown would
     * overwrite the failure trace with an empty archive.</p>
     *
     * @param tracePath target file for the trace archive
     */
    public synchronized void stopTracing(Path tracePath) {
        if (currentContext == null || tracingStopped) {
            return;
        }

        tracingStopped = true;
        currentContext.tracing().stop(new Tracing.StopOptions().setPath(tracePath));
    }

    /**
     * Stops tracing when enabled, closes pages, contexts, browser, and
     * Playwright resources, and unregisters the endpoint from failure evidence.
     */
    public synchronized void stop() {
        ActivePlaywrightBrowsers.unregister(this);
        if (currentContext != null && getEndpointConfiguration().isTracingEnabled()) {
            try {
                Path tracePath = Path.of("target", "playwright", "trace.zip");
                Files.createDirectories(tracePath.getParent());
                stopTracing(tracePath);
            } catch (Exception ignored) {
                // Browser teardown must continue even if trace persistence fails.
            }
        }

        for (Page page : new ArrayList<>(pages.values())) {
            removeCaptureBuffers(page);
            closePageQuietly(page);
        }
        pages.clear();
        pageContexts.clear();

        for (BrowserContext context : new ArrayList<>(contexts.values())) {
            try {
                context.close();
            } catch (RuntimeException ignored) {
                // Browser teardown must continue even if context close fails.
            }
        }
        contexts.clear();
        currentPage = null;
        currentContext = null;
        latestDownloadMetadata = null;
        actionOwnerThreadId = null;

        if (browser != null) {
            try {
                browser.close();
            } catch (RuntimeException ignored) {
                // Browser teardown must continue even if browser close fails.
            }
            browser = null;
        }
        if (playwright != null) {
            try {
                playwright.close();
            } catch (RuntimeException ignored) {
                // Browser teardown must continue even if Playwright close fails.
            }
            playwright = null;
        }
    }

    /**
     * Destroys the endpoint by delegating to {@link #stop()}.
     */
    @Override
    public void destroy() {
        stop();
    }

    /**
     * Returns the active Playwright browser, starting the endpoint if needed.
     *
     * @return active browser
     */
    public Browser getBrowser() {
        ensureStarted();
        return browser;
    }

    /**
     * Returns the current browser context, starting the endpoint if needed.
     *
     * @return current browser context
     */
    public BrowserContext getCurrentContext() {
        ensureStarted();
        return currentContext;
    }

    /**
     * Returns the current page, starting the endpoint if needed.
     *
     * @return current page
     */
    public Page getCurrentPage() {
        ensureStarted();
        return currentPage;
    }

    /**
     * Returns the current context without starting the endpoint.
     *
     * @return optional current context
     */
    public synchronized Optional<BrowserContext> currentContextIfAvailable() {
        return Optional.ofNullable(currentContext);
    }

    /**
     * Returns the current page without starting the endpoint.
     *
     * @return optional current page
     */
    public synchronized Optional<Page> currentPageIfAvailable() {
        return Optional.ofNullable(currentPage);
    }

    /**
     * Returns the alias for the current browser context without starting the endpoint.
     *
     * @return optional current context alias
     */
    public synchronized Optional<String> getCurrentContextAlias() {
        return contexts.entrySet().stream()
                .filter(entry -> entry.getValue() == currentContext)
                .map(Map.Entry::getKey)
                .findFirst();
    }

    /**
     * Returns the alias for the current page without starting the endpoint.
     *
     * @return optional current page alias
     */
    public synchronized Optional<String> getCurrentPageAlias() {
        return pages.entrySet().stream()
                .filter(entry -> entry.getValue() == currentPage)
                .map(Map.Entry::getKey)
                .findFirst();
    }

    /**
     * Returns the per-page console capture registry.
     *
     * @return console capture registry
     */
    public ConsoleCaptureRegistry getConsoleCaptureRegistry() {
        return consoleCaptureRegistry;
    }

    /**
     * Returns the per-page network capture registry.
     *
     * @return network capture registry
     */
    public NetworkCaptureRegistry getNetworkCaptureRegistry() {
        return networkCaptureRegistry;
    }

    /**
     * Creates a diagnostics redactor using endpoint-level secret pattern configuration.
     *
     * @return configured redactor
     */
    public SecretPatternRedactor createRedactor() {
        return new SecretPatternRedactor(getEndpointConfiguration().getSecretPatterns());
    }

    /**
     * Returns metadata for the latest download observed by a {@link org.citrusframework.playwright.actions.DownloadAction}.
     *
     * @return optional latest download metadata
     */
    public synchronized Optional<DownloadMetadata> getLatestDownloadMetadata() {
        return Optional.ofNullable(latestDownloadMetadata);
    }

    /**
     * Stores metadata for the latest download observed by the endpoint.
     *
     * @param latestDownloadMetadata download metadata
     */
    public synchronized void setLatestDownloadMetadata(DownloadMetadata latestDownloadMetadata) {
        this.latestDownloadMetadata = latestDownloadMetadata;
    }

    /**
     * Clears metadata for the latest download observed by the endpoint.
     */
    public synchronized void clearLatestDownloadMetadata() {
        latestDownloadMetadata = null;
    }

    /**
     * Lists registered browser context aliases in insertion order.
     *
     * @return immutable alias list
     */
    public synchronized List<String> getContextAliases() {
        return List.copyOf(contexts.keySet());
    }

    /**
     * Lists registered page aliases in insertion order.
     *
     * @return immutable alias list
     */
    public synchronized List<String> getPageAliases() {
        return List.copyOf(pages.keySet());
    }

    /**
     * Creates a new context with default endpoint context options and switches to it.
     *
     * @param alias alias for the created context
     * @return created context
     */
    public synchronized BrowserContext createContext(String alias) {
        return createContext(alias, null);
    }

    /**
     * Creates a new context with explicit options and switches to it.
     *
     * @param alias alias for the created context
     * @param options Playwright context options; when null, endpoint defaults are used
     * @return created context
     */
    public synchronized BrowserContext createContext(String alias, Browser.NewContextOptions options) {
        ensureStarted();
        BrowserContext context = options == null
                ? createBrowserContext(browser, getEndpointConfiguration())
                : createBrowserContext(browser, resolveContextOptions(getEndpointConfiguration(), options));
        registerContext(alias, context);
        currentContext = context;
        return context;
    }

    /**
     * Registers an externally created context under an alias and closes any
     * replaced context with the same alias.
     *
     * @param alias context alias
     * @param context context to register
     * @return registered context
     */
    public synchronized BrowserContext registerContext(String alias, BrowserContext context) {
        requireAlias(alias, "context");
        if (context == null) {
            throw new CitrusRuntimeException("Cannot register null Playwright context for alias: " + alias);
        }
        BrowserContext previous = contexts.put(alias, context);
        if (previous != null && previous != context) {
            closeContextQuietly(previous);
        }
        return context;
    }

    /**
     * Switches the current context by alias and selects the first page owned by
     * that context when one is registered.
     *
     * @param alias context alias
     * @return selected context
     */
    public synchronized BrowserContext switchContext(String alias) {
        BrowserContext context = contexts.get(alias);
        if (context == null) {
            throw new CitrusRuntimeException("No Playwright context registered with alias: " + alias);
        }
        currentContext = context;
        pages.entrySet().stream()
                .filter(entry -> pageContexts.get(entry.getValue()) == context)
                .findFirst()
                .ifPresent(entry -> currentPage = entry.getValue());
        return context;
    }

    /**
     * Closes and unregisters a context plus all pages registered to it.
     *
     * @param alias context alias
     */
    public synchronized void closeContext(String alias) {
        BrowserContext context = contexts.remove(alias);
        if (context == null) {
            throw new CitrusRuntimeException("No Playwright context registered with alias: " + alias);
        }
        List<String> aliases = pages.entrySet().stream()
                .filter(entry -> pageContexts.get(entry.getValue()) == context)
                .map(Map.Entry::getKey)
                .toList();
        aliases.stream().map(this::removePage).forEach(this::removeCaptureBuffers);
        closeContextQuietly(context);
        if (currentContext == context) {
            currentContext = contexts.values().stream().findFirst().orElse(null);
            currentPage = currentContext == null
                    ? null
                    : pages.values().stream().filter(page -> pageContexts.get(page) == currentContext).findFirst().orElse(null);
        }
    }

    /**
     * Creates a new page in the current context and switches to it.
     *
     * @param alias page alias
     * @return created page
     */
    public synchronized Page createPage(String alias) {
        ensureStarted();
        return registerPage(alias, createPage(currentContext));
    }

    /**
     * Creates a new page in the named context and switches to it.
     *
     * @param alias page alias
     * @param contextAlias context alias
     * @return created page
     */
    public synchronized Page createPage(String alias, String contextAlias) {
        BrowserContext context = switchContext(contextAlias);
        return registerPage(alias, createPage(context));
    }

    /**
     * Registers an externally created page under an alias, applies configured
     * timeouts, and switches to it.
     *
     * @param alias page alias
     * @param page page to register
     * @return registered page
     */
    public synchronized Page registerPage(String alias, Page page) {
        requireAlias(alias, "page");
        if (page == null) {
            throw new CitrusRuntimeException("Cannot register null Playwright page for alias: " + alias);
        }
        applyTimeouts(getEndpointConfiguration(), page);
        Page previous = pages.put(alias, page);
        if (previous != null && previous != page) {
            pageContexts.remove(previous);
            removeCaptureBuffers(previous);
            closePageQuietly(previous);
        }
        pageContexts.put(page, currentContext);
        currentPage = page;
        return page;
    }

    /**
     * Finds a registered page by alias without switching to it.
     *
     * @param alias page alias
     * @return optional page
     */
    public synchronized Optional<Page> findPage(String alias) {
        return Optional.ofNullable(pages.get(alias));
    }

    /**
     * Switches the current page by alias and also switches to its owning context.
     *
     * @param alias page alias
     * @return selected page
     */
    public synchronized Page switchPage(String alias) {
        Page page = pages.get(alias);
        if (page == null) {
            throw new CitrusRuntimeException("No Playwright page registered with alias: " + alias);
        }
        currentPage = page;
        BrowserContext context = pageContexts.get(page);
        if (context != null) {
            currentContext = context;
        }
        return page;
    }

    /**
     * Switches the current page by insertion-order index.
     *
     * @param index zero-based page index
     * @return selected page
     */
    public synchronized Page switchPageByIndex(int index) {
        if (index < 0 || index >= pages.size()) {
            throw new CitrusRuntimeException("No Playwright page registered at index: " + index);
        }
        return switchPage(new ArrayList<>(pages.keySet()).get(index));
    }

    /**
     * Switches to the first registered page whose title equals the supplied title.
     *
     * @param title expected page title
     * @return selected page
     */
    public synchronized Page switchPageByTitle(String title) {
        return switchPageBy(page -> title != null && title.equals(page.title()), "title", title);
    }

    /**
     * Switches to the first registered page whose URL contains the supplied text.
     *
     * @param urlPart URL fragment to match
     * @return selected page
     */
    public synchronized Page switchPageByUrlContaining(String urlPart) {
        return switchPageBy(page -> urlPart != null && page.url() != null && page.url().contains(urlPart), "URL containing", urlPart);
    }

    /**
     * Closes and unregisters a page by alias and releases captured observability buffers.
     *
     * @param alias page alias
     */
    public synchronized void closePage(String alias) {
        Page page = removePage(alias);
        removeCaptureBuffers(page);
        closePageQuietly(page);
        if (currentPage == page) {
            currentPage = pages.values().stream().findFirst().orElse(null);
            currentContext = currentPage == null ? currentContext : pageContexts.get(currentPage);
        }
    }

    private void closeCurrentPage() {
        if (currentPage != null) {
            removeCaptureBuffers(currentPage);
            closePageQuietly(currentPage);
        }
        currentPage = null;
    }

    private void ensureStarted() {
        if (!isStarted()) {
            start();
        }
    }

    /**
     * Ensures all actions using this endpoint execute from the same test thread.
     *
     * <p>Playwright page and context objects are not safe to interleave across
     * concurrent Citrus test threads. The first action claims the endpoint until
     * it is stopped; a different thread fails fast with a clear configuration
     * error instead of racing shared browser state.</p>
     */
    public synchronized void assertActionThread() {
        long currentThreadId = Thread.currentThread().getId();
        if (actionOwnerThreadId == null) {
            actionOwnerThreadId = currentThreadId;
            return;
        }
        if (actionOwnerThreadId != currentThreadId) {
            throw new CitrusRuntimeException(
                    "Playwright browser endpoint is already used by thread %d and cannot be shared with thread %d. "
                            .formatted(actionOwnerThreadId, currentThreadId)
                            + "Configure one PlaywrightBrowser endpoint per parallel test thread.");
        }
    }

    private Function<Playwright, BrowserType> resolveBrowserType(String browserType) {
        String normalized = browserType == null ? "chromium" : browserType.toLowerCase(Locale.ROOT);
        return switch (normalized) {
            case "chromium" -> Playwright::chromium;
            case "firefox" -> Playwright::firefox;
            case "webkit" -> Playwright::webkit;
            default -> throw new CitrusRuntimeException("Unsupported Playwright browser type: " + browserType);
        };
    }

    protected Playwright createPlaywright(PlaywrightBrowserConfiguration config) {
        return config.getCreateOptions() != null
                ? Playwright.create(config.getCreateOptions())
                : Playwright.create();
    }

    protected Browser launchBrowser(Playwright playwright, PlaywrightBrowserConfiguration config) {
        boolean hasWsEndpoint = StringUtils.hasText(config.getConnectWsEndpoint());
        boolean hasCdpEndpoint = StringUtils.hasText(config.getConnectOverCdpEndpoint());
        if (hasWsEndpoint && hasCdpEndpoint) {
            throw new CitrusRuntimeException(
                    "Configure either Playwright WebSocket connection or CDP connection, not both");
        }

        BrowserType browserType = resolveBrowserType(config.getBrowserType()).apply(playwright);
        if (hasWsEndpoint) {
            return browserType.connect(config.getConnectWsEndpoint(),
                    config.getConnectOptions() == null ? new BrowserType.ConnectOptions() : config.getConnectOptions());
        }
        if (hasCdpEndpoint) {
            return browserType.connectOverCDP(config.getConnectOverCdpEndpoint(),
                    config.getConnectOverCdpOptions() == null
                            ? new BrowserType.ConnectOverCDPOptions()
                            : config.getConnectOverCdpOptions());
        }
        return browserType.launch(resolveLaunchOptions(config));
    }

    protected BrowserContext createBrowserContext(Browser browser, PlaywrightBrowserConfiguration config) {
        return createBrowserContext(browser, resolveContextOptions(config));
    }

    protected BrowserContext createBrowserContext(Browser browser, Browser.NewContextOptions options) {
        return browser.newContext(options);
    }

    protected Page createPage(BrowserContext context) {
        return context.newPage();
    }

    private Page switchPageBy(java.util.function.Predicate<Page> predicate, String selector, String value) {
        return pages.entrySet().stream()
                .filter(entry -> predicate.test(entry.getValue()))
                .findFirst()
                .map(entry -> switchPage(entry.getKey()))
                .orElseThrow(() -> new CitrusRuntimeException("No Playwright page registered with " + selector + ": " + value));
    }

    private Page removePage(String alias) {
        Page page = pages.remove(alias);
        if (page == null) {
            throw new CitrusRuntimeException("No Playwright page registered with alias: " + alias);
        }
        pageContexts.remove(page);
        return page;
    }

    private void removeCaptureBuffers(Page page) {
        consoleCaptureRegistry.remove(page);
        networkCaptureRegistry.remove(page);
    }

    private void closePageQuietly(Page page) {
        try {
            if (page != null && !page.isClosed()) {
                page.close();
            }
        } catch (RuntimeException ignored) {
            // Browser teardown must continue even if page close fails.
        }
    }

    private void closeContextQuietly(BrowserContext context) {
        try {
            context.close();
        } catch (RuntimeException ignored) {
            // Browser teardown must continue even if context close fails.
        }
    }

    private void requireAlias(String alias, String type) {
        if (!StringUtils.hasText(alias)) {
            throw new CitrusRuntimeException("Missing Playwright " + type + " alias");
        }
    }

    private BrowserType.LaunchOptions resolveLaunchOptions(PlaywrightBrowserConfiguration config) {
        BrowserType.LaunchOptions options = config.getLaunchOptions() != null
                ? config.getLaunchOptions()
                : new BrowserType.LaunchOptions();

        if (config.getHeadless() != null && options.headless == null) {
            options.setHeadless(config.getHeadless());
        }
        if (config.getSlowMo() != null && options.slowMo == null) {
            options.setSlowMo(config.getSlowMo());
        }
        if (StringUtils.hasText(config.getChannel()) && options.channel == null) {
            options.setChannel(config.getChannel());
        }
        if (config.getDownloadsPath() != null && options.downloadsPath == null) {
            options.setDownloadsPath(config.getDownloadsPath());
        }

        return options;
    }

    protected Browser.NewContextOptions resolveContextOptions(PlaywrightBrowserConfiguration config) {
        return resolveContextOptions(config, config.getContextOptions());
    }

    protected Browser.NewContextOptions resolveContextOptions(PlaywrightBrowserConfiguration config, Browser.NewContextOptions overrides) {
        Browser.NewContextOptions options = config.getContextOptions() != null
                ? config.getContextOptions()
                : new Browser.NewContextOptions();
        if (overrides != null) {
            options = overrides;
        }
        if (StringUtils.hasText(config.getBaseUrl()) && options.baseURL == null) {
            options.setBaseURL(config.getBaseUrl());
        }
        return options;
    }

    private void applyTimeouts(PlaywrightBrowserConfiguration config, Page page) {
        if (config.getDefaultTimeout() != null) {
            page.setDefaultTimeout(config.getDefaultTimeout());
        }
        if (config.getDefaultNavigationTimeout() != null) {
            page.setDefaultNavigationTimeout(config.getDefaultNavigationTimeout());
        }
    }
}
