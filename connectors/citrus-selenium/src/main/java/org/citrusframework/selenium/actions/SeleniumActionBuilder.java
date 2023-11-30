/*
 * Copyright 2006-2017 the original author or authors.
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

import java.io.IOException;
import java.nio.charset.Charset;

import org.citrusframework.TestActionBuilder;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.selenium.endpoint.SeleniumBrowser;
import org.citrusframework.selenium.model.WebPage;
import org.citrusframework.spi.Resource;
import org.citrusframework.util.FileUtils;
import org.citrusframework.util.ObjectHelper;

/**
 * Action builds selenium related actions.
 *
 * @author Christoph Deppisch
 * @since 2.7
 */
public class SeleniumActionBuilder implements TestActionBuilder.DelegatingTestActionBuilder<SeleniumAction> {

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

    /**
     * Start browser instance.
     */
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

    /**
     * Stop browser instance.
     */
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

    /**
     * Alert element.
     */
    public AlertAction.Builder alert() {
        AlertAction.Builder builder = new AlertAction.Builder()
                .browser(seleniumBrowser);
        this.delegate = builder;
        return builder;
    }

    /**
     * Navigate action.
     */
    public NavigateAction.Builder navigate() {
        NavigateAction.Builder builder = new NavigateAction.Builder()
                .browser(seleniumBrowser);
        this.delegate = builder;
        return builder;
    }

    /**
     * Navigate action.
     */
    public NavigateAction.Builder navigate(String page) {
        NavigateAction.Builder builder = new NavigateAction.Builder()
                .page(page)
                .browser(seleniumBrowser);
        this.delegate = builder;
        return builder;
    }

    /**
     * Page action.
     */
    public PageAction.Builder page(WebPage page) {
        PageAction.Builder builder = new PageAction.Builder()
                .page(page)
                .browser(seleniumBrowser);
        this.delegate = builder;
        return builder;
    }

    /**
     * Page action.
     */
    public PageAction.Builder page(Class<? extends WebPage> pageType) {
        PageAction.Builder builder = new PageAction.Builder()
                .type(pageType)
                .browser(seleniumBrowser);
        this.delegate = builder;
        return builder;
    }

	/**
     * Finds element.
     */
    public FindElementAction.Builder find() {
		FindElementAction.Builder builder = new FindElementAction.Builder()
                .browser(seleniumBrowser);
        this.delegate = builder;
        return builder;
    }

    /**
     * Dropdown select single option action.
     */
    public DropDownSelectAction.Builder select(String option) {
        DropDownSelectAction.Builder builder = new DropDownSelectAction.Builder()
                .option(option)
                .browser(seleniumBrowser);
        this.delegate = builder;
        return builder;
    }

    /**
     * Dropdown select multiple options action.
     */
    public DropDownSelectAction.Builder select(String ... options) {
        DropDownSelectAction.Builder builder = new DropDownSelectAction.Builder()
                .options(options)
                .browser(seleniumBrowser);
        this.delegate = builder;
        return builder;
    }

    /**
     * Set input action.
     */
    public SetInputAction.Builder setInput(String value) {
		SetInputAction.Builder builder = new SetInputAction.Builder()
                .value(value)
                .browser(seleniumBrowser);
        this.delegate = builder;
        return builder;
    }

    /**
     * Set input action.
     */
    public SetInputAction.Builder setInput() {
		SetInputAction.Builder builder = new SetInputAction.Builder()
                .browser(seleniumBrowser);
        this.delegate = builder;
        return builder;
    }

    /**
     * Fill form action.
     */
    public FillFormAction.Builder fillForm() {
		FillFormAction.Builder builder = new FillFormAction.Builder()
                .browser(seleniumBrowser);
        this.delegate = builder;
        return builder;
    }

    /**
     * Check input action.
     */
    public CheckInputAction.Builder checkInput() {
		CheckInputAction.Builder builder = new CheckInputAction.Builder()
                .browser(seleniumBrowser);
        this.delegate = builder;
        return builder;
    }

    /**
     * Check input action.
     */
    public CheckInputAction.Builder checkInput(boolean checked) {
		CheckInputAction.Builder builder = new CheckInputAction.Builder()
                .checked(checked)
                .browser(seleniumBrowser);
        this.delegate = builder;
        return builder;
    }

    /**
     * Clicks element.
     */
    public ClickAction.Builder click() {
		ClickAction.Builder builder = new ClickAction.Builder()
                .browser(seleniumBrowser);
        this.delegate = builder;
        return builder;
    }

    /**
     * Hover element.
     */
    public HoverAction.Builder hover() {
        HoverAction.Builder builder = new HoverAction.Builder()
                .browser(seleniumBrowser);
        this.delegate = builder;
        return builder;
    }

    /**
     * Clear browser cache.
     */
    public ClearBrowserCacheAction.Builder clearCache() {
        ClearBrowserCacheAction.Builder builder = new ClearBrowserCacheAction.Builder()
                .browser(seleniumBrowser);
        this.delegate = builder;
        return builder;
    }

    /**
     * Make screenshot.
     */
    public MakeScreenshotAction.Builder screenshot() {
        MakeScreenshotAction.Builder builder = new MakeScreenshotAction.Builder()
                .browser(seleniumBrowser);
        this.delegate = builder;
        return builder;
    }

    /**
     * Make screenshot with custom output directory.
     */
    public MakeScreenshotAction.Builder screenshot(String outputDir) {
        MakeScreenshotAction.Builder builder = new MakeScreenshotAction.Builder()
                .outputDir(outputDir)
                .browser(seleniumBrowser);
        this.delegate = builder;
        return builder;
    }

    /**
     * Store file.
     */
    public StoreFileAction.Builder store() {
        StoreFileAction.Builder builder = new StoreFileAction.Builder()
                .browser(seleniumBrowser);
        this.delegate = builder;
        return builder;
    }

    /**
     * Store file.
     * @param filePath
     */
    public StoreFileAction.Builder store(String filePath) {
        StoreFileAction.Builder builder = new StoreFileAction.Builder()
                .filePath(filePath)
                .browser(seleniumBrowser);
        this.delegate = builder;
        return builder;
    }

    /**
     * Get stored file.
     */
    public GetStoredFileAction.Builder getStored() {
        GetStoredFileAction.Builder builder = new GetStoredFileAction.Builder()
                .browser(seleniumBrowser);
        this.delegate = builder;
        return builder;
    }

    /**
     * Get stored file.
     * @param fileName
     */
    public GetStoredFileAction.Builder getStored(String fileName) {
        GetStoredFileAction.Builder builder = new GetStoredFileAction.Builder()
                .fileName(fileName)
                .browser(seleniumBrowser);
        this.delegate = builder;
        return builder;
    }

    /**
     * Wait until element meets condition.
     */
    public WaitUntilAction.Builder waitUntil() {
        WaitUntilAction.Builder builder = new WaitUntilAction.Builder()
                .browser(seleniumBrowser);
        this.delegate = builder;
        return builder;
    }

    /**
     * Execute JavaScript.
     */
    public JavaScriptAction.Builder javascript() {
        JavaScriptAction.Builder builder = new JavaScriptAction.Builder()
                .browser(seleniumBrowser);
        this.delegate = builder;
        return builder;
    }

    /**
     * Execute JavaScript.
     */
    public JavaScriptAction.Builder javascript(String script) {
        JavaScriptAction.Builder builder = new JavaScriptAction.Builder()
                .script(script)
                .browser(seleniumBrowser);
        this.delegate = builder;
        return builder;
    }

    /**
     * Execute JavaScript.
     */
    public JavaScriptAction.Builder javascript(Resource script) {
        return javascript(script, FileUtils.getDefaultCharset());
    }

    /**
     * Execute JavaScript.
     */
    public JavaScriptAction.Builder javascript(Resource scriptResource, Charset charset) {
        try {
            JavaScriptAction.Builder builder = new JavaScriptAction.Builder()
                    .script(FileUtils.readToString(scriptResource, charset))
                    .browser(seleniumBrowser);
            this.delegate = builder;
            return builder;
        } catch (IOException e) {
            throw new CitrusRuntimeException("Failed to read script resource", e);
        }
    }

    /**
     * Open window.
     */
    public OpenWindowAction.Builder open() {
        OpenWindowAction.Builder builder = new OpenWindowAction.Builder()
                .browser(seleniumBrowser);
        this.delegate = builder;
        return builder;
    }

    /**
     * Close window.
     */
    public CloseWindowAction.Builder close() {
        CloseWindowAction.Builder builder = new CloseWindowAction.Builder()
                .browser(seleniumBrowser);
        this.delegate = builder;
        return builder;
    }

    /**
     * Switch window.
     */
    public SwitchWindowAction.Builder focus() {
        SwitchWindowAction.Builder builder = new SwitchWindowAction.Builder()
                .browser(seleniumBrowser);
        this.delegate = builder;
        return builder;
    }

    /**
     * Switch window.
     */
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
