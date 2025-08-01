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

package org.citrusframework.selenium.actions;

import org.citrusframework.TestActionBuilder;
import org.citrusframework.endpoint.Endpoint;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.selenium.endpoint.SeleniumBrowser;
import org.citrusframework.util.ObjectHelper;

/**
 * Action builds selenium related actions.
 *
 * @since 2.7
 */
public class SeleniumActionBuilder implements TestActionBuilder.DelegatingTestActionBuilder<SeleniumAction>,
        org.citrusframework.actions.selenium.SeleniumActionBuilder<SeleniumAction, SeleniumActionBuilder> {

    /** Selenium browser */
    private SeleniumBrowser seleniumBrowser;

    private AbstractSeleniumAction.Builder<? extends SeleniumAction, ?> delegate;

    /**
     * Fluent API action building entry method used in Java DSL.
     * @return
     */
    public static SeleniumActionBuilder selenium() {
        return new SeleniumActionBuilder();
    }

    /**
	 * Use a custom selenium browser.
	 */
	public SeleniumActionBuilder browser(SeleniumBrowser seleniumBrowser) {
		this.seleniumBrowser = seleniumBrowser;
		return this;
	}

    @Override
    public SeleniumActionBuilder browser(Endpoint endpoint) {
        if (endpoint instanceof SeleniumBrowser browser) {
            return browser(browser);
        } else {
            throw new CitrusRuntimeException(("Invalid browser object, expected a SeleniumBrowser, " +
                    "but got %s").formatted(endpoint.getClass().getName()));
        }
    }

    @Override
    public StartBrowserAction.Builder start() {
        StartBrowserAction.Builder builder = new StartBrowserAction.Builder()
                .browser(seleniumBrowser);
        this.delegate = builder;
        return builder;
    }

    /**
     * Start explicit browser instance.
     */
    public StartBrowserAction.Builder start(SeleniumBrowser seleniumBrowser) {
        browser(seleniumBrowser);
        StartBrowserAction.Builder builder = new StartBrowserAction.Builder()
                .browser(seleniumBrowser);
        this.delegate = builder;
        return builder;
    }

    @Override
    public StartBrowserAction.Builder start(Endpoint endpoint) {
        if (endpoint instanceof SeleniumBrowser browser) {
            return start(browser);
        } else {
            throw new CitrusRuntimeException(("Invalid browser object, expected a SeleniumBrowser, " +
                    "but got %s").formatted(endpoint.getClass().getName()));
        }
    }

    @Override
    public StopBrowserAction.Builder stop() {
        StopBrowserAction.Builder builder = new StopBrowserAction.Builder()
                .browser(seleniumBrowser);
        this.delegate = builder;
        return builder;
    }

    /**
     * Stop explicit browser instance.
     */
    public StopBrowserAction.Builder stop(SeleniumBrowser seleniumBrowser) {
        browser(seleniumBrowser);
        StopBrowserAction.Builder builder = new StopBrowserAction.Builder()
                .browser(seleniumBrowser);
        this.delegate = builder;
        return builder;
    }

    @Override
    public StopBrowserAction.Builder stop(Endpoint endpoint) {
        if (endpoint instanceof SeleniumBrowser browser) {
            return stop(browser);
        } else {
            throw new CitrusRuntimeException(("Invalid browser object, expected a SeleniumBrowser, " +
                    "but got %s").formatted(endpoint.getClass().getName()));
        }
    }

    @Override
    public AlertAction.Builder alert() {
        AlertAction.Builder builder = new AlertAction.Builder()
                .browser(seleniumBrowser);
        this.delegate = builder;
        return builder;
    }

    @Override
    public NavigateAction.Builder navigate() {
        NavigateAction.Builder builder = new NavigateAction.Builder()
                .browser(seleniumBrowser);
        this.delegate = builder;
        return builder;
    }

    @Override
    public PageAction.Builder page() {
        PageAction.Builder builder = new PageAction.Builder()
                .browser(seleniumBrowser);
        this.delegate = builder;
        return builder;
    }

	@Override
    public FindElementAction.Builder find() {
		FindElementAction.Builder builder = new FindElementAction.Builder()
                .browser(seleniumBrowser);
        this.delegate = builder;
        return builder;
    }

    @Override
    public DropDownSelectAction.Builder select() {
        DropDownSelectAction.Builder builder = new DropDownSelectAction.Builder()
                .browser(seleniumBrowser);
        this.delegate = builder;
        return builder;
    }

    @Override
    public SetInputAction.Builder setInput() {
		SetInputAction.Builder builder = new SetInputAction.Builder()
                .browser(seleniumBrowser);
        this.delegate = builder;
        return builder;
    }

    @Override
    public FillFormAction.Builder fillForm() {
		FillFormAction.Builder builder = new FillFormAction.Builder()
                .browser(seleniumBrowser);
        this.delegate = builder;
        return builder;
    }

    @Override
    public CheckInputAction.Builder checkInput() {
		CheckInputAction.Builder builder = new CheckInputAction.Builder()
                .browser(seleniumBrowser);
        this.delegate = builder;
        return builder;
    }

    @Override
    public ClickAction.Builder click() {
		ClickAction.Builder builder = new ClickAction.Builder()
                .browser(seleniumBrowser);
        this.delegate = builder;
        return builder;
    }

    @Override
    public HoverAction.Builder hover() {
        HoverAction.Builder builder = new HoverAction.Builder()
                .browser(seleniumBrowser);
        this.delegate = builder;
        return builder;
    }

    @Override
    public ClearBrowserCacheAction.Builder clearCache() {
        ClearBrowserCacheAction.Builder builder = new ClearBrowserCacheAction.Builder()
                .browser(seleniumBrowser);
        this.delegate = builder;
        return builder;
    }

    @Override
    public MakeScreenshotAction.Builder screenshot() {
        MakeScreenshotAction.Builder builder = new MakeScreenshotAction.Builder()
                .browser(seleniumBrowser);
        this.delegate = builder;
        return builder;
    }

    @Override
    public StoreFileAction.Builder store() {
        StoreFileAction.Builder builder = new StoreFileAction.Builder()
                .browser(seleniumBrowser);
        this.delegate = builder;
        return builder;
    }

    @Override
    public GetStoredFileAction.Builder getStored() {
        GetStoredFileAction.Builder builder = new GetStoredFileAction.Builder()
                .browser(seleniumBrowser);
        this.delegate = builder;
        return builder;
    }

    @Override
    public WaitUntilAction.Builder waitUntil() {
        WaitUntilAction.Builder builder = new WaitUntilAction.Builder()
                .browser(seleniumBrowser);
        this.delegate = builder;
        return builder;
    }

    @Override
    public JavaScriptAction.Builder javascript() {
        JavaScriptAction.Builder builder = new JavaScriptAction.Builder()
                .browser(seleniumBrowser);
        this.delegate = builder;
        return builder;
    }

    @Override
    public OpenWindowAction.Builder open() {
        OpenWindowAction.Builder builder = new OpenWindowAction.Builder()
                .browser(seleniumBrowser);
        this.delegate = builder;
        return builder;
    }

    @Override
    public CloseWindowAction.Builder close() {
        CloseWindowAction.Builder builder = new CloseWindowAction.Builder()
                .browser(seleniumBrowser);
        this.delegate = builder;
        return builder;
    }

    @Override
    public SwitchWindowAction.Builder focus() {
        SwitchWindowAction.Builder builder = new SwitchWindowAction.Builder()
                .browser(seleniumBrowser);
        this.delegate = builder;
        return builder;
    }

    @Override
    public SwitchWindowAction.Builder switchWindow() {
        SwitchWindowAction.Builder builder = new SwitchWindowAction.Builder()
                .browser(seleniumBrowser);
        this.delegate = builder;
        return builder;
    }

    @Override
    public SeleniumAction build() {
        ObjectHelper.assertNotNull(delegate, "Missing delegate action to build");
        if (seleniumBrowser != null) {
            delegate.browser(seleniumBrowser);
        }
        return delegate.build();
    }

    @Override
    public TestActionBuilder<?> getDelegate() {
        return delegate;
    }
}
