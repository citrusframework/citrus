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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.expectThrows;
import static org.testng.Assert.assertTrue;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.Tracing;

import java.lang.reflect.Proxy;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.playwright.support.FailureEvidenceWriter;
import org.citrusframework.playwright.support.MockPlaywrightBrowser;
import org.citrusframework.playwright.support.StubStartedBrowser;
import org.testng.annotations.Test;

class PlaywrightBrowserLifecycleTest {

    @Test
    void shouldCleanUpPartiallyStartedBrowserOnFailure() {
        TestLifecycleBrowser browser = new TestLifecycleBrowser();
        browser.failContextCreation = true;

        CitrusRuntimeException exception = expectThrows(CitrusRuntimeException.class, browser::start);

        assertTrue(exception.getMessage().contains("context failure"));
        assertFalse(browser.isStarted());
        assertEquals(1, browser.playwrightCreated.get());
        assertEquals(1, browser.browserCreated.get());
        assertEquals(1, browser.browserClosed.get());
        assertEquals(1, browser.playwrightClosed.get());
    }

    @Test
    void shouldStartOnlyOnceWhenCalledConcurrently() throws Exception {
        TestLifecycleBrowser browser = new TestLifecycleBrowser();

        runConcurrently(browser::start);

        assertTrue(browser.isStarted());
        assertEquals(1, browser.playwrightCreated.get());
        assertEquals(1, browser.browserCreated.get());
        assertEquals(1, browser.contextCreated.get());
        assertEquals(1, browser.pageCreated.get());

        runConcurrently(browser::stop);

        assertFalse(browser.isStarted());
        assertEquals(1, browser.pageClosed.get());
        assertEquals(1, browser.contextClosed.get());
        assertEquals(1, browser.browserClosed.get());
        assertEquals(1, browser.playwrightClosed.get());
    }

    @Test
    void shouldManageNamedContextsAndPages() {
        TestLifecycleBrowser browser = new TestLifecycleBrowser();

        browser.start();
        BrowserContext defaultContext = browser.getCurrentContext();
        Page defaultPage = browser.getCurrentPage();

        BrowserContext adminContext = browser.createContext("admin");
        Page adminPage = browser.createPage("admin-dashboard");

        assertSame(adminContext, browser.getCurrentContext());
        assertSame(adminPage, browser.getCurrentPage());
        assertSame(defaultContext, browser.switchContext("default"));
        assertSame(defaultPage, browser.switchPage("default"));
        assertSame(adminPage, browser.switchPage("admin-dashboard"));
        assertSame(adminPage, browser.switchPageByIndex(1));
        assertSame(adminPage, browser.switchPageByTitle("Admin Dashboard"));
        assertSame(adminPage, browser.switchPageByUrlContaining("/admin"));

        Optional<Page> missing = browser.findPage("missing");
        assertTrue(missing.isEmpty());

        browser.closePage("admin-dashboard");
        expectThrows(CitrusRuntimeException.class, () -> browser.switchPage("admin-dashboard"));

        browser.closeContext("admin");
        expectThrows(CitrusRuntimeException.class, () -> browser.switchContext("admin"));
    }

    @Test
    void shouldReleaseCaptureRegistryEntriesWhenPageCloses() {
        TestLifecycleBrowser browser = new TestLifecycleBrowser();

        browser.start();
        Page defaultPage = browser.getCurrentPage();
        browser.getConsoleCaptureRegistry().capture(defaultPage, 5);
        browser.getNetworkCaptureRegistry().capture(defaultPage, 5);

        assertEquals(1, browser.getConsoleCaptureRegistry().size());
        assertEquals(1, browser.getNetworkCaptureRegistry().size());

        browser.closePage("default");

        assertEquals(0, browser.getConsoleCaptureRegistry().size());
        assertEquals(0, browser.getNetworkCaptureRegistry().size());
    }

    @Test
    void shouldSkipFailureEvidenceWhenNoCurrentPageExists() {
        TestLifecycleBrowser browser = new TestLifecycleBrowser();
        browser.getEndpointConfiguration().setCaptureFailureScreenshot(true);
        browser.getEndpointConfiguration().setCaptureFailurePageSource(true);
        browser.getEndpointConfiguration().setCaptureFailureConsoleMessages(true);
        browser.getEndpointConfiguration().setCaptureFailureNetworkRequests(true);
        browser.getEndpointConfiguration().setCaptureFailureTrace(true);

        browser.start();
        browser.closePage("default");

        new FailureEvidenceWriter().write(browser, "no page");
    }

    @Test
    void shouldExposePhaseTwoConfigurationDefaultsAndBuilderProperties() {
        PlaywrightBrowser browser = new PlaywrightEndpointBuilder()
                .consoleMessageLimit(25)
                .networkRecordLimit(50)
                .artifactDirectory(Path.of("target", "custom-playwright"))
                .captureFailureScreenshot(true)
                .captureFailurePageSource(true)
                .captureFailureTrace(true)
                .captureFailureConsoleMessages(true)
                .captureFailureNetworkRequests(true)
                .captureFailureSummary(true)
                .connectWsEndpoint("ws://localhost:3000/playwright")
                .connectOptions(new BrowserType.ConnectOptions())
                .connectOverCdpEndpoint("http://localhost:9222")
                .connectOverCdpOptions(new BrowserType.ConnectOverCDPOptions())
                .secretPatterns("session_id", "private-token")
                .build();

        PlaywrightBrowserConfiguration configuration = browser.getEndpointConfiguration();

        assertEquals(25, configuration.getConsoleMessageLimit());
        assertEquals(50, configuration.getNetworkRecordLimit());
        assertEquals(Path.of("target", "custom-playwright"), configuration.getArtifactDirectory());
        assertTrue(configuration.isCaptureFailureScreenshot());
        assertTrue(configuration.isCaptureFailurePageSource());
        assertTrue(configuration.isCaptureFailureTrace());
        assertTrue(configuration.isCaptureFailureConsoleMessages());
        assertTrue(configuration.isCaptureFailureNetworkRequests());
        assertTrue(configuration.isCaptureFailureSummary());
        assertEquals("ws://localhost:3000/playwright", configuration.getConnectWsEndpoint());
        assertTrue(configuration.getConnectOptions() != null);
        assertEquals("http://localhost:9222", configuration.getConnectOverCdpEndpoint());
        assertTrue(configuration.getConnectOverCdpOptions() != null);
        assertEquals(List.of("session_id", "private-token"), configuration.getSecretPatterns());
    }

    @Test
    void shouldConnectToRemoteWebSocketEndpointWhenConfigured() {
        ConnectionModeBrowser browser = new ConnectionModeBrowser();
        browser.getEndpointConfiguration().setConnectWsEndpoint("ws://localhost:3000/playwright");

        browser.start();

        assertEquals("connect", browser.connectionMode);
        assertEquals("ws://localhost:3000/playwright", browser.connectionEndpoint);
        assertEquals(1, browser.connects.get());
        assertEquals(0, browser.cdpConnects.get());
        assertEquals(0, browser.launches.get());
    }

    @Test
    void shouldConnectToRemoteCdpEndpointWhenConfigured() {
        ConnectionModeBrowser browser = new ConnectionModeBrowser();
        browser.getEndpointConfiguration().setConnectOverCdpEndpoint("http://localhost:9222");

        browser.start();

        assertEquals("connectOverCDP", browser.connectionMode);
        assertEquals("http://localhost:9222", browser.connectionEndpoint);
        assertEquals(0, browser.connects.get());
        assertEquals(1, browser.cdpConnects.get());
        assertEquals(0, browser.launches.get());
    }

    @Test
    void shouldRejectConflictingRemoteConnectionModes() {
        ConnectionModeBrowser browser = new ConnectionModeBrowser();
        browser.getEndpointConfiguration().setConnectWsEndpoint("ws://localhost:3000/playwright");
        browser.getEndpointConfiguration().setConnectOverCdpEndpoint("http://localhost:9222");

        CitrusRuntimeException exception = expectThrows(CitrusRuntimeException.class, browser::start);

        assertTrue(exception.getMessage().contains("not both"));
        assertEquals(0, browser.connects.get());
        assertEquals(0, browser.cdpConnects.get());
        assertEquals(0, browser.launches.get());
        assertEquals(1, browser.playwrightClosed.get());
    }

    @Test
    void shouldStopTracingOnlyOnce() {
        MockPlaywrightBrowser browser = new MockPlaywrightBrowser();
        browser.start();

        Tracing tracing = mock(Tracing.class);
        when(browser.context().tracing()).thenReturn(tracing);

        browser.stopTracing(Path.of("target", "playwright", "failure", "trace.zip"));
        browser.stopTracing(Path.of("target", "playwright", "trace.zip"));

        verify(tracing).stop(any(Tracing.StopOptions.class));
    }

    @Test
    void shouldPinTheBrowserToTheFirstActionThread() throws Exception {
        StubStartedBrowser browser = new StubStartedBrowser();
        browser.start();

        browser.assertActionThread();
        browser.assertActionThread();

        Throwable failure = runOnOtherThread(browser::assertActionThread);

        assertTrue(failure instanceof CitrusRuntimeException,
                "expected a Citrus failure but got " + failure);
        assertTrue(failure.getMessage().contains("already used by thread " + Thread.currentThread().getId()),
                failure.getMessage());
        assertTrue(failure.getMessage().contains("one PlaywrightBrowser endpoint per parallel test thread"),
                failure.getMessage());
    }

    @Test
    void shouldReleaseTheThreadPinOnStop() throws Exception {
        StubStartedBrowser browser = new StubStartedBrowser();
        browser.start();
        browser.assertActionThread();

        browser.stop();

        assertNull(runOnOtherThread(browser::assertActionThread),
                "a stopped endpoint is free for the next test thread to claim");
    }

    private Throwable runOnOtherThread(Runnable task) throws Exception {
        AtomicReference<Throwable> failure = new AtomicReference<>();
        Thread thread = new Thread(() -> {
            try {
                task.run();
            } catch (Throwable t) {
                failure.set(t);
            }
        });

        thread.start();
        thread.join(5000);
        assertFalse(thread.isAlive(), "worker thread did not finish in time");
        return failure.get();
    }

    private void runConcurrently(Runnable task) throws Exception {
        int threads = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        CountDownLatch ready = new CountDownLatch(threads);
        CountDownLatch start = new CountDownLatch(1);
        List<Future<?>> futures = new ArrayList<>();

        for (int i = 0; i < threads; i++) {
            futures.add(executor.submit(() -> {
                ready.countDown();
                start.await(5, TimeUnit.SECONDS);
                task.run();
                return null;
            }));
        }

        assertTrue(ready.await(5, TimeUnit.SECONDS));
        start.countDown();
        for (Future<?> future : futures) {
            future.get(5, TimeUnit.SECONDS);
        }
        executor.shutdownNow();
    }

    static class TestLifecycleBrowser extends PlaywrightBrowser {
        final AtomicInteger playwrightCreated = new AtomicInteger();
        final AtomicInteger playwrightClosed = new AtomicInteger();
        final AtomicInteger browserCreated = new AtomicInteger();
        final AtomicInteger browserClosed = new AtomicInteger();
        final AtomicInteger contextCreated = new AtomicInteger();
        final AtomicInteger contextClosed = new AtomicInteger();
        final AtomicInteger pageCreated = new AtomicInteger();
        final AtomicInteger pageClosed = new AtomicInteger();

        final List<Page> pages = new ArrayList<>();

        boolean failContextCreation;

        @Override
        protected Playwright createPlaywright(PlaywrightBrowserConfiguration config) {
            playwrightCreated.incrementAndGet();
            sleep();
            return proxy(Playwright.class, (method, args) -> {
                if ("close".equals(method)) {
                    playwrightClosed.incrementAndGet();
                }
                return null;
            });
        }

        @Override
        protected Browser launchBrowser(Playwright playwright, PlaywrightBrowserConfiguration config) {
            browserCreated.incrementAndGet();
            sleep();
            return proxy(Browser.class, (method, args) -> {
                if ("isConnected".equals(method)) {
                    return browserClosed.get() == 0;
                }
                if ("close".equals(method)) {
                    browserClosed.incrementAndGet();
                }
                return null;
            });
        }

        @Override
        protected BrowserContext createBrowserContext(Browser browser, PlaywrightBrowserConfiguration config) {
            if (failContextCreation) {
                throw new CitrusRuntimeException("context failure");
            }
            contextCreated.incrementAndGet();
            sleep();
            return proxy(BrowserContext.class, (method, args) -> {
                if ("close".equals(method)) {
                    contextClosed.incrementAndGet();
                }
                return null;
            });
        }

        @Override
        protected Page createPage(BrowserContext context) {
            pageCreated.incrementAndGet();
            sleep();
            int pageIndex = pageCreated.get();
            Page page = proxy(Page.class, (method, args) -> {
                if ("isClosed".equals(method)) {
                    return false;
                }
                if ("close".equals(method)) {
                    pageClosed.incrementAndGet();
                }
                if ("title".equals(method)) {
                    return pageIndex == 1 ? "Default" : "Admin Dashboard";
                }
                if ("url".equals(method)) {
                    return pageIndex == 1 ? "http://localhost/default" : "http://localhost/admin";
                }
                return null;
            });
            pages.add(page);
            return page;
        }

        private void sleep() {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        @SuppressWarnings("unchecked")
        private <T> T proxy(Class<T> type, MethodHandler handler) {
            return (T) Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[]{type}, (proxy, method, args) -> {
                Object result = handler.handle(method.getName(), args);
                return result == null ? defaultValue(method.getReturnType()) : result;
            });
        }

        private Object defaultValue(Class<?> returnType) {
            if (!returnType.isPrimitive()) {
                return null;
            }
            if (returnType == boolean.class) {
                return false;
            }
            if (returnType == char.class) {
                return '\0';
            }
            return 0;
        }

        @FunctionalInterface
        interface MethodHandler {
            Object handle(String method, Object[] args);
        }
    }

    static class ConnectionModeBrowser extends PlaywrightBrowser {
        final AtomicInteger playwrightClosed = new AtomicInteger();
        final AtomicInteger browserClosed = new AtomicInteger();
        final AtomicInteger connects = new AtomicInteger();
        final AtomicInteger cdpConnects = new AtomicInteger();
        final AtomicInteger launches = new AtomicInteger();
        String connectionMode;
        String connectionEndpoint;

        @Override
        protected Playwright createPlaywright(PlaywrightBrowserConfiguration config) {
            BrowserType browserType = proxy(BrowserType.class, (method, args) -> {
                if ("connect".equals(method)) {
                    connects.incrementAndGet();
                    connectionMode = "connect";
                    connectionEndpoint = String.valueOf(args[0]);
                    return browserProxy();
                }
                if ("connectOverCDP".equals(method)) {
                    cdpConnects.incrementAndGet();
                    connectionMode = "connectOverCDP";
                    connectionEndpoint = String.valueOf(args[0]);
                    return browserProxy();
                }
                if ("launch".equals(method)) {
                    launches.incrementAndGet();
                    connectionMode = "launch";
                    return browserProxy();
                }
                return null;
            });
            return proxy(Playwright.class, (method, args) -> {
                if ("chromium".equals(method) || "firefox".equals(method) || "webkit".equals(method)) {
                    return browserType;
                }
                if ("close".equals(method)) {
                    playwrightClosed.incrementAndGet();
                }
                return null;
            });
        }

        @Override
        protected BrowserContext createBrowserContext(Browser browser, PlaywrightBrowserConfiguration config) {
            return proxy(BrowserContext.class, (method, args) -> null);
        }

        @Override
        protected Page createPage(BrowserContext context) {
            return proxy(Page.class, (method, args) -> {
                if ("isClosed".equals(method)) {
                    return false;
                }
                return null;
            });
        }

        private Browser browserProxy() {
            return proxy(Browser.class, (method, args) -> {
                if ("isConnected".equals(method)) {
                    return browserClosed.get() == 0;
                }
                if ("close".equals(method)) {
                    browserClosed.incrementAndGet();
                }
                return null;
            });
        }

        @SuppressWarnings("unchecked")
        private <T> T proxy(Class<T> type, TestLifecycleBrowser.MethodHandler handler) {
            return (T) Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[]{type}, (proxy, method, args) -> {
                Object result = handler.handle(method.getName(), args);
                return result == null ? defaultValue(method.getReturnType()) : result;
            });
        }

        private Object defaultValue(Class<?> returnType) {
            if (!returnType.isPrimitive()) {
                return null;
            }
            if (returnType == boolean.class) {
                return false;
            }
            if (returnType == char.class) {
                return '\0';
            }
            return 0;
        }
    }
}
