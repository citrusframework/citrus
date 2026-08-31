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

package org.citrusframework.playwright.actions;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.citrusframework.TestAction;
import org.citrusframework.TestActionBuilder;
import org.citrusframework.container.Sequence;
import org.citrusframework.endpoint.Endpoint;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.playwright.endpoint.PlaywrightBrowser;

/**
 * Root Java DSL builder for Citrus Playwright actions.
 *
 * <p>The builder binds an optional {@link PlaywrightBrowser} endpoint once and
 * then delegates to one action-specific builder. Each terminal Citrus action is
 * produced by the delegated builder selected through methods such as
 * {@link #open()}, {@link #context()}, {@link #network()}, or
 * {@link #pageObject()}.</p>
 */
public class PlaywrightActionBuilder implements TestActionBuilder.DelegatingTestActionBuilder<PlaywrightAction> {

    private PlaywrightBrowser browser;
    private String browserName;
    private TestActionBuilder<? extends PlaywrightAction> delegate;
    private final List<TestActionBuilder<?>> delegates = new ArrayList<>();

    /**
     * Creates a new root Playwright action builder.
     *
     * @return a fresh builder instance
     */
    public static PlaywrightActionBuilder playwright() {
        return new PlaywrightActionBuilder();
    }

    @SuppressWarnings("unchecked")
    private <B extends AbstractPlaywrightAction.Builder<?, ?>> B register(B builder) {
        this.delegate = (TestActionBuilder<? extends PlaywrightAction>) (TestActionBuilder<?>) builder;
        this.delegates.add(builder);
        if (browser == null && browserName != null) {
            builder.browser(browserName);
        }
        return builder;
    }

    /**
     * Binds the browser endpoint used by the next action builder.
     *
     * @param browser Playwright browser endpoint to drive
     * @return this builder
     */
    public PlaywrightActionBuilder browser(PlaywrightBrowser browser) {
        this.browser = browser;
        return this;
    }

    /**
     * Binds a browser endpoint by name. The name is resolved through the
     * reference resolver at execution time, so endpoints declared as beans can
     * be referenced without holding the object instance.
     *
     * @param browserName name of a {@link PlaywrightBrowser} endpoint
     * @return this builder
     */
    public PlaywrightActionBuilder browser(String browserName) {
        this.browserName = browserName;
        return this;
    }

    /**
     * Executes the given actions against the given browser as one sequential
     * scoped block. Actions inside the block use the explicit binding and do
     * not depend on or modify the ambient thread scope.
     *
     * @param browser browser endpoint bound for the block
     * @param actions consumer chaining terminal builders on a root builder
     * @return sequential container executing all produced actions in order
     */
    public static Sequence with(PlaywrightBrowser browser, Consumer<PlaywrightActionBuilder> actions) {
        PlaywrightActionBuilder root = new PlaywrightActionBuilder();
        root.browser(browser);
        actions.accept(root);

        Sequence.Builder sequence = new Sequence.Builder().name("playwright:scope");
        sequence.actions(root.delegates.stream().map(TestActionBuilder::build).toArray(TestAction[]::new));
        return sequence.build();
    }

    /**
     * Binds a generic Citrus endpoint after validating that it is a
     * {@link PlaywrightBrowser}.
     *
     * @param endpoint endpoint reference resolved by Citrus
     * @return this builder
     */
    public PlaywrightActionBuilder browser(Endpoint endpoint) {
        if (endpoint instanceof PlaywrightBrowser playwrightBrowser) {
            return browser(playwrightBrowser);
        }
        throw new CitrusRuntimeException("Invalid browser object, expected a PlaywrightBrowser, but got %s".formatted(endpoint.getClass().getName()));
    }

    /**
     * Starts the Playwright endpoint and browser runtime.
     *
     * @return start action builder
     */
    public StartBrowserAction.Builder start() {
        StartBrowserAction.Builder builder = new StartBrowserAction.Builder().browser(browser);
        register(builder);
        return builder;
    }

    /**
     * Stops the Playwright endpoint and closes owned browser resources.
     *
     * @return stop action builder
     */
    public StopBrowserAction.Builder stop() {
        StopBrowserAction.Builder builder = new StopBrowserAction.Builder().browser(browser);
        register(builder);
        return builder;
    }

    /**
     * Opens a URL in the current page.
     *
     * @return open action builder
     */
    public OpenAction.Builder open() {
        OpenAction.Builder builder = new OpenAction.Builder().browser(browser);
        register(builder);
        return builder;
    }

    /**
     * Navigates the current page backward, forward, or reloads it.
     *
     * @return navigation action builder
     */
    public NavigateAction.Builder navigate() {
        NavigateAction.Builder builder = new NavigateAction.Builder().browser(browser);
        register(builder);
        return builder;
    }

    /**
     * Clicks a locator in the current page.
     *
     * @return mouse action builder
     */
    public MouseAction.Builder click() {
        return mouse(MouseAction.Command.CLICK);
    }

    /**
     * Double-clicks a locator in the current page.
     *
     * @return mouse action builder
     */
    public MouseAction.Builder doubleClick() {
        return mouse(MouseAction.Command.DOUBLE_CLICK);
    }

    /**
     * Right-clicks a locator in the current page.
     *
     * @return mouse action builder
     */
    public MouseAction.Builder rightClick() {
        return mouse(MouseAction.Command.RIGHT_CLICK);
    }

    /**
     * Hovers a locator in the current page.
     *
     * @return mouse action builder
     */
    public MouseAction.Builder hover() {
        return mouse(MouseAction.Command.HOVER);
    }

    /**
     * Focuses a locator in the current page.
     *
     * @return mouse action builder
     */
    public MouseAction.Builder focus() {
        return mouse(MouseAction.Command.FOCUS);
    }

    /**
     * Taps a locator in the current page.
     *
     * @return mouse action builder
     */
    public MouseAction.Builder tap() {
        return mouse(MouseAction.Command.TAP);
    }

    /**
     * Fills a locator in the current page.
     *
     * @return input action builder
     */
    public InputAction.Builder fill() {
        return input(InputAction.Command.FILL);
    }

    /**
     * Alias for {@link #fill()}.
     *
     * @return input action builder
     */
    public InputAction.Builder enter() {
        return fill();
    }

    /**
     * Clears a locator in the current page.
     *
     * @return input action builder
     */
    public InputAction.Builder clear() {
        return input(InputAction.Command.CLEAR);
    }

    /**
     * Sends a keyboard key or shortcut to a locator.
     *
     * @return input action builder
     */
    public InputAction.Builder press() {
        return input(InputAction.Command.PRESS);
    }

    /**
     * Checks a checkbox or radio locator.
     *
     * @return input action builder
     */
    public InputAction.Builder check() {
        return input(InputAction.Command.CHECK);
    }

    /**
     * Unchecks a checkbox locator.
     *
     * @return input action builder
     */
    public InputAction.Builder uncheck() {
        return input(InputAction.Command.UNCHECK);
    }

    /**
     * Selects options in a select locator.
     *
     * @return input action builder
     */
    public InputAction.Builder select() {
        return input(InputAction.Command.SELECT);
    }

    /**
     * Uploads file paths into a file input locator.
     *
     * @return input action builder
     */
    public InputAction.Builder upload() {
        return input(InputAction.Command.UPLOAD);
    }

    /**
     * Waits for locator or page load state conditions.
     *
     * @return wait action builder
     */
    public WaitForAction.Builder waitFor() {
        WaitForAction.Builder builder = new WaitForAction.Builder().browser(browser);
        register(builder);
        return builder;
    }

    /**
     * Captures a screenshot of the current page.
     *
     * @return screenshot action builder
     */
    public ScreenshotAction.Builder screenshot() {
        ScreenshotAction.Builder builder = new ScreenshotAction.Builder().browser(browser);
        register(builder);
        return builder;
    }

    /**
     * Evaluates JavaScript in the current page.
     *
     * @return JavaScript action builder
     */
    public JavaScriptAction.Builder javascript() {
        JavaScriptAction.Builder builder = new JavaScriptAction.Builder().browser(browser);
        register(builder);
        return builder;
    }

    /**
     * Verifies page, locator, URL, title, or value state.
     *
     * @return verification action builder
     */
    public VerifyAction.Builder verify() {
        VerifyAction.Builder builder = new VerifyAction.Builder().browser(browser);
        register(builder);
        return builder;
    }

    /**
     * Extracts page or locator values into Citrus variables.
     *
     * @return extraction action builder
     */
    public ExtractAction.Builder extract() {
        ExtractAction.Builder builder = new ExtractAction.Builder().browser(browser);
        register(builder);
        return builder;
    }

    /**
     * Creates, switches, or closes named browser contexts.
     *
     * @return context action builder
     */
    public ContextAction.Builder context() {
        ContextAction.Builder builder = new ContextAction.Builder().browser(browser);
        register(builder);
        return builder;
    }

    /**
     * Creates, switches, or closes named pages.
     *
     * @return page action builder
     */
    public PageAction.Builder page() {
        PageAction.Builder builder = new PageAction.Builder().browser(browser);
        register(builder);
        return builder;
    }

    /**
     * Targets locators inside an iframe or frame locator.
     *
     * @return frame action builder
     */
    public FrameAction.Builder frame() {
        FrameAction.Builder builder = new FrameAction.Builder().browser(browser);
        register(builder);
        return builder;
    }

    /**
     * Registers one-shot dialog handling for the current page.
     *
     * @return dialog action builder
     */
    public DialogAction.Builder dialog() {
        DialogAction.Builder builder = new DialogAction.Builder().browser(browser);
        register(builder);
        return builder;
    }

    /**
     * Waits for and optionally persists a download from the current page.
     *
     * @return download action builder
     */
    public DownloadAction.Builder download() {
        DownloadAction.Builder builder = new DownloadAction.Builder().browser(browser);
        register(builder);
        return builder;
    }

    /**
     * Adds, clears, reads, or verifies browser cookies.
     *
     * @return cookie action builder
     */
    public CookieAction.Builder cookies() {
        CookieAction.Builder builder = new CookieAction.Builder().browser(browser);
        register(builder);
        return builder;
    }

    /**
     * Reads and mutates local/session storage or context storage state.
     *
     * @return storage action builder
     */
    public StorageAction.Builder storage() {
        StorageAction.Builder builder = new StorageAction.Builder().browser(browser);
        register(builder);
        return builder;
    }

    /**
     * Grants or clears permissions on the current context.
     *
     * @return permission action builder
     */
    public PermissionAction.Builder permissions() {
        PermissionAction.Builder builder = new PermissionAction.Builder().browser(browser);
        register(builder);
        return builder;
    }

    /**
     * Applies viewport, geolocation, color-scheme, locale, timezone, or
     * user-agent emulation to the endpoint.
     *
     * @return emulation action builder
     */
    public EmulationAction.Builder emulate() {
        EmulationAction.Builder builder = new EmulationAction.Builder().browser(browser);
        register(builder);
        return builder;
    }

    /**
     * Captures, clears, reports, or verifies console messages.
     *
     * @return console action builder
     */
    public ConsoleAction.Builder console() {
        ConsoleAction.Builder builder = new ConsoleAction.Builder().browser(browser);
        register(builder);
        return builder;
    }

    /**
     * Captures, clears, reports, or verifies network events.
     *
     * @return network action builder
     */
    public NetworkAction.Builder network() {
        NetworkAction.Builder builder = new NetworkAction.Builder().browser(browser);
        register(builder);
        return builder;
    }

    /**
     * Starts or stops Playwright tracing for the current context.
     *
     * @return tracing action builder
     */
    public TracingAction.Builder tracing() {
        TracingAction.Builder builder = new TracingAction.Builder().browser(browser);
        register(builder);
        return builder;
    }

    /**
     * Exports the current Chromium page as a PDF.
     *
     * @return PDF action builder
     */
    public PdfAction.Builder pdf() {
        PdfAction.Builder builder = new PdfAction.Builder().browser(browser);
        register(builder);
        return builder;
    }

    /**
     * Instantiates and invokes a Citrus Playwright page object.
     *
     * @return page-object action builder
     */
    public PageObjectAction.Builder pageObject() {
        PageObjectAction.Builder builder = new PageObjectAction.Builder().browser(browser);
        register(builder);
        return builder;
    }

    private MouseAction.Builder mouse(MouseAction.Command command) {
        MouseAction.Builder builder = new MouseAction.Builder(command).browser(browser);
        register(builder);
        return builder;
    }

    private InputAction.Builder input(InputAction.Command command) {
        InputAction.Builder builder = new InputAction.Builder(command).browser(browser);
        register(builder);
        return builder;
    }

    /**
     * Returns the selected action-specific builder for Citrus delegation.
     *
     * @return delegated test action builder
     */
    @Override
    public TestActionBuilder<?> getDelegate() {
        return delegate;
    }

    /**
     * Builds the selected Playwright action.
     *
     * @return configured Playwright action
     */
    @Override
    public PlaywrightAction build() {
        if (delegate == null) {
            throw new CitrusRuntimeException("Missing Playwright action delegate - call an action builder method first");
        }
        return delegate.build();
    }
}
