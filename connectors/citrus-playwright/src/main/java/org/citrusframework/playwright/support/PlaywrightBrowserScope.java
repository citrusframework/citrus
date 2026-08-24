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

import java.lang.ref.WeakReference;
import java.util.Optional;

import org.citrusframework.context.TestContext;
import org.citrusframework.playwright.endpoint.PlaywrightBrowser;

/**
 * Thread-scoped holder for the ambient {@link PlaywrightBrowser} endpoint.
 *
 * <p>{@code StartBrowserAction} binds the started endpoint to the executing
 * thread and {@code StopBrowserAction} unbinds it, so subsequent actions may
 * omit the explicit {@code .browser(...)} binding. The scope holds at most one
 * browser per thread which mirrors the one-endpoint-per-thread rule enforced by
 * {@code PlaywrightBrowser.assertActionThread()}.</p>
 *
 * <p>The scope cleans up after itself; nothing has to be registered for it to be
 * safe on pooled threads. A binding records the {@link TestContext} it was
 * created for, and Citrus builds one context per test, so a binding left behind
 * by a test that failed before {@code stop()} is never handed to the next test
 * that reuses the thread — the contexts differ and the stale entry is dropped.
 * The context is held weakly, so a finished test's context is not kept alive by
 * a pooled thread, and a binding whose test is gone reports itself unusable
 * without waiting for that thread's next Playwright action.</p>
 */
public final class PlaywrightBrowserScope {

    private static final ThreadLocal<Binding> CURRENT = new ThreadLocal<>();

    private PlaywrightBrowserScope() {
    }

    /**
     * Binds the given browser endpoint as the current thread's ambient browser
     * for the duration of the given test context.
     *
     * @param browser started browser endpoint bound to this thread
     * @param context test context the binding belongs to
     */
    public static void bind(PlaywrightBrowser browser, TestContext context) {
        CURRENT.set(new Binding(browser, new WeakReference<>(context)));
    }

    /**
     * Returns the current thread's ambient browser if it belongs to the given
     * test context and is still usable. A binding that points to a stopped
     * browser, to a different test, or to a test that has already been collected
     * is discarded so a stale entry never routes actions to a closed endpoint or
     * across a test boundary.
     *
     * @param context test context asking for the ambient browser
     * @return ambient browser or empty when unbound, foreign or stale
     */
    public static Optional<PlaywrightBrowser> current(TestContext context) {
        Binding binding = CURRENT.get();
        if (binding == null) {
            return Optional.empty();
        }
        if (binding.owner().get() != context || !binding.browser().isStarted()) {
            CURRENT.remove();
            return Optional.empty();
        }
        return Optional.of(binding.browser());
    }

    /**
     * Checks whether the given browser instance is the current thread's ambient
     * binding regardless of its lifecycle state or owning test.
     *
     * @param browser candidate browser endpoint
     * @return true when this exact instance is bound to the thread
     */
    public static boolean isBoundTo(PlaywrightBrowser browser) {
        Binding binding = CURRENT.get();
        return binding != null && binding.browser() == browser;
    }

    /**
     * Removes any ambient binding from the current thread.
     */
    public static void clear() {
        CURRENT.remove();
    }

    private record Binding(PlaywrightBrowser browser, WeakReference<TestContext> owner) {
    }
}
