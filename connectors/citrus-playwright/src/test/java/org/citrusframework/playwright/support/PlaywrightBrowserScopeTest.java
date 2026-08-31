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

package org.citrusframework.playwright.support;

import java.util.concurrent.atomic.AtomicReference;

import org.citrusframework.context.TestContext;
import org.citrusframework.playwright.endpoint.PlaywrightBrowser;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;

class PlaywrightBrowserScopeTest {

    private final TestContext context = new TestContext();

    private final StubStartedBrowser browser = new StubStartedBrowser();

    @AfterMethod
    void tearDown() {
        PlaywrightBrowserScope.clear();
    }

    @Test
    void shouldReturnEmptyWhenNothingBound() {
        assertTrue(PlaywrightBrowserScope.current(context).isEmpty());
    }

    @Test
    void shouldBindAndReturnCurrentBrowser() {
        browser.start();

        PlaywrightBrowserScope.bind(browser, context);

        assertContains(PlaywrightBrowserScope.current(context), browser);
        assertTrue(PlaywrightBrowserScope.isBoundTo(browser));
    }

    @Test
    void shouldDiscardStaleBindingOfStoppedBrowser() {
        PlaywrightBrowser neverStarted = new PlaywrightBrowser();
        PlaywrightBrowserScope.bind(neverStarted, context);

        assertTrue(PlaywrightBrowserScope.current(context).isEmpty());
        assertFalse(PlaywrightBrowserScope.isBoundTo(neverStarted));
    }

    @Test
    void shouldDiscardBindingAfterBrowserStops() {
        browser.start();
        PlaywrightBrowserScope.bind(browser, context);
        browser.stop();

        assertTrue(PlaywrightBrowserScope.current(context).isEmpty());
    }

    @Test
    void shouldClearBinding() {
        browser.start();
        PlaywrightBrowserScope.bind(browser, context);

        PlaywrightBrowserScope.clear();

        assertTrue(PlaywrightBrowserScope.current(context).isEmpty());
        assertFalse(PlaywrightBrowserScope.isBoundTo(browser));
    }

    @Test
    void shouldKeepBindingsThreadLocal() throws Exception {
        browser.start();
        PlaywrightBrowserScope.bind(browser, context);

        AtomicReference<Boolean> otherThreadView = new AtomicReference<>();
        Thread otherThread = new Thread(() -> {
            otherThreadView.set(PlaywrightBrowserScope.current(context).isPresent());
            PlaywrightBrowserScope.clear();
        });
        otherThread.start();
        otherThread.join();

        assertFalse(otherThreadView.get());
        assertTrue(PlaywrightBrowserScope.isBoundTo(browser));
    }

    private static void assertContains(java.util.Optional<PlaywrightBrowser> current, PlaywrightBrowser expected) {
        assertTrue(current.isPresent());
        assertSame(expected, current.get());
    }

    @Test
    void shouldNotHandABindingToADifferentTest() {
        browser.start();
        PlaywrightBrowserScope.bind(browser, context);

        // Citrus builds one context per test, so a second context is the next test
        // reusing this pooled thread after the first failed before stop().
        TestContext nextTest = new TestContext();

        assertTrue(PlaywrightBrowserScope.current(nextTest).isEmpty(),
                "a binding must never cross a test boundary, even on a reused thread");
        assertTrue(PlaywrightBrowserScope.current(context).isEmpty(),
                "the stale entry is dropped outright rather than left for its owner");
    }
}
