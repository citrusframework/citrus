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

package com.consol.citrus.dsl.builder;

import com.consol.citrus.TestAction;
import com.consol.citrus.dsl.actions.DelegatingTestAction;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.selenium.actions.*;
import com.consol.citrus.selenium.endpoint.SeleniumBrowser;
import com.consol.citrus.selenium.model.PageValidator;
import com.consol.citrus.selenium.model.WebPage;
import com.consol.citrus.util.FileUtils;
import org.openqa.selenium.By;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;

/**
 * Action builds selenium related actions.
 * 
 * @author Christoph Deppisch
 * @since 2.7
 */
public class SeleniumActionBuilder extends AbstractTestActionBuilder<DelegatingTestAction<SeleniumAction>> {

    /** Selenium browser */
    private SeleniumBrowser seleniumBrowser;

	/**
	 * Constructor using action field.
	 * @param action
	 */
	public SeleniumActionBuilder(DelegatingTestAction<SeleniumAction> action) {
	    super(action);
    }

	/**
	 * Default constructor.
	 */
	public SeleniumActionBuilder() {
		super(new DelegatingTestAction<>());
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
    public SeleniumActionBuilder start() {
        action(new StartBrowserAction());
        return this;
    }

    /**
     * Start explicit browser instance.
     */
    public SeleniumActionBuilder start(SeleniumBrowser seleniumBrowser) {
        browser(seleniumBrowser);
        action(new StartBrowserAction());
        return this;
    }

    /**
     * Stop browser instance.
     */
    public SeleniumActionBuilder stop() {
        action(new StopBrowserAction());
        return this;
    }

    /**
     * Stop explicit browser instance.
     */
    public SeleniumActionBuilder stop(SeleniumBrowser seleniumBrowser) {
        browser(seleniumBrowser);
        action(new StopBrowserAction());
        return this;
    }

    /**
     * Alert element.
     */
    public AlertActionBuilder alert() {
        AlertAction action = new AlertAction();
        action(action);
        return new AlertActionBuilder(action);
    }

    /**
     * Navigate action.
     */
    public SeleniumActionBuilder navigate(String page) {
        NavigateAction action = new NavigateAction();
        action.setPage(page);
        action(action);
        return this;
    }

    /**
     * Page action.
     */
    public PageActionBuilder page(WebPage page) {
        PageAction action = new PageAction();
        action.setPage(page);
        action(action);
        return new PageActionBuilder(action);
    }

    /**
     * Page action.
     */
    public PageActionBuilder page(Class<? extends WebPage> pageType) {
        PageAction action = new PageAction();
        action.setType(pageType.getName());
        action(action);
        return new PageActionBuilder(action);
    }

	/**
     * Finds element.
     */
    public FindElementActionBuilder find() {
		FindElementAction action = new FindElementAction();
        action(action);
        return new FindElementActionBuilder(action);
    }

    /**
     * Dropdown select single option action.
     */
    public ElementActionBuilder select(String option) {
        DropDownSelectAction action = new DropDownSelectAction();
        action.setOption(option);
        action(action);
        return new ElementActionBuilder(action);
    }

    /**
     * Dropdown select multiple options action.
     */
    public ElementActionBuilder select(String ... options) {
        DropDownSelectAction action = new DropDownSelectAction();
        action.setOptions(Arrays.asList(options));
        action(action);
        return new ElementActionBuilder(action);
    }

    /**
     * Set input action.
     */
    public ElementActionBuilder setInput(String value) {
		SetInputAction action = new SetInputAction();
		action.setValue(value);
        action(action);
        return new ElementActionBuilder(action);
    }

    /**
     * Check input action.
     */
    public ElementActionBuilder checkInput(boolean checked) {
		CheckInputAction action = new CheckInputAction();
		action.setChecked(checked);
        action(action);
        return new ElementActionBuilder(action);
    }

    /**
     * Clicks element.
     */
    public ElementActionBuilder click() {
		ClickAction action = new ClickAction();
        action(action);
        return new ElementActionBuilder(action);
    }

    /**
     * Hover element.
     */
    public ElementActionBuilder hover() {
		HoverAction action = new HoverAction();
        action(action);
        return new ElementActionBuilder(action);
    }

    /**
     * Clear browser cache.
     */
    public SeleniumActionBuilder clearCache() {
        action(new ClearBrowserCacheAction());
        return this;
    }

    /**
     * Make screenshot.
     */
    public SeleniumActionBuilder screenshot() {
        action(new MakeScreenshotAction());
        return this;
    }

    /**
     * Make screenshot with custom output directory.
     */
    public SeleniumActionBuilder screenshot(String outputDir) {
        MakeScreenshotAction action = new MakeScreenshotAction();
        action.setOutputDir(outputDir);
        action(action);
        return this;
    }

    /**
     * Store file.
     * @param filePath
     */
    public SeleniumActionBuilder store(String filePath) {
        StoreFileAction action = new StoreFileAction();
        action.setFilePath(filePath);
        action(action);
        return this;
    }

    /**
     * Get stored file.
     * @param fileName
     */
    public SeleniumActionBuilder getStored(String fileName) {
        GetStoredFileAction action = new GetStoredFileAction();
        action.setFileName(fileName);
        action(action);
        return this;
    }

    /**
     * Wait until element meets condition.
     */
    public WaitUntilActionBuilder waitUntil() {
        WaitUntilAction action = new WaitUntilAction();
        action(action);
        return new WaitUntilActionBuilder(action);
    }

    /**
     * Execute JavaScript.
     */
    public JavaScriptActionBuilder javascript(String script) {
        JavaScriptAction action = new JavaScriptAction();
        action.setScript(script);
        action(action);
        return new JavaScriptActionBuilder(action);
    }

    /**
     * Execute JavaScript.
     */
    public JavaScriptActionBuilder javascript(Resource script) {
        return javascript(script, FileUtils.getDefaultCharset());
    }

    /**
     * Execute JavaScript.
     */
    public JavaScriptActionBuilder javascript(Resource script, Charset charset) {
        JavaScriptAction action = new JavaScriptAction();
        try {
            action.setScript(FileUtils.readToString(script, charset));
        } catch (IOException e) {
            throw new CitrusRuntimeException("Failed to read script resource", e);
        }
        action(action);
        return new JavaScriptActionBuilder(action);
    }

    /**
     * Open window.
     */
    public WindowActionBuilder open() {
        OpenWindowAction action = new OpenWindowAction();
        action(action);
        return new WindowActionBuilder(action);
    }

    /**
     * Close window.
     */
    public WindowActionBuilder close() {
        CloseWindowAction action = new CloseWindowAction();
        action(action);
        return new WindowActionBuilder(action);
    }

    /**
     * Switch window.
     */
    public WindowActionBuilder focus() {
        SwitchWindowAction action = new SwitchWindowAction();
        action(action);
        return new WindowActionBuilder(action);
    }

    /**
     * Prepare selenium action.
     */
    private <T extends SeleniumAction> T action(T delegate) {
        if (seleniumBrowser != null) {
            delegate.setBrowser(seleniumBrowser);
        }

        action.setDelegate(delegate);
        return delegate;
    }

    /**
     * Customize alert action.
     */
    public class AlertActionBuilder implements TestActionBuilder {

        /** Alert action */
        private final AlertAction action;

        /**
         * Default constructor.
         * @param action
         */
        public AlertActionBuilder(AlertAction action) {
            this.action = action;
        }

        /**
         * Add alert text validation.
         * @param text
         * @return
         */
        public AlertActionBuilder text(String text) {
            action.setText(text);
            return this;
        }

        /**
         * Accept alert dialog.
         * @return
         */
        public AlertActionBuilder accept() {
            action.setAccept(true);
            return this;
        }

        /**
         * Dismiss alert dialog.
         * @return
         */
        public AlertActionBuilder dismiss() {
            action.setAccept(false);
            return this;
        }

        @Override
        public TestAction build() {
            return SeleniumActionBuilder.this.build();
        }
    }

    /**
     * Customize page action.
     */
    public class PageActionBuilder implements TestActionBuilder {

        /** Alert action */
        private final PageAction action;

        /**
         * Default constructor.
         * @param action
         */
        public PageActionBuilder(PageAction action) {
            this.action = action;
        }

        /**
         * Perform page validation.
         * @return
         */
        public PageActionBuilder validate() {
            action.setAction("validate");
            return this;
        }

        /**
         * Set page validator.
         * @param validator
         * @return
         */
        public PageActionBuilder validator(PageValidator validator) {
            action.setValidator(validator);
            return this;
        }

        /**
         * Set page action method to execute.
         * @param method
         * @return
         */
        public PageActionBuilder execute(String method) {
            action.setAction(method);
            return this;
        }

        /**
         * Set page action argument.
         * @param arg
         * @return
         */
        public PageActionBuilder argument(String arg) {
            action.getArguments().add(arg);
            return this;
        }

        /**
         * Set page action arguments.
         * @param args
         * @return
         */
        public PageActionBuilder arguments(String ... args) {
            action.setArguments(Arrays.asList(args));
            return this;
        }

        /**
         * Set page action arguments.
         * @param args
         * @return
         */
        public PageActionBuilder arguments(List<String> args) {
            action.setArguments(args);
            return this;
        }

        @Override
        public TestAction build() {
            return SeleniumActionBuilder.this.build();
        }
    }

    /**
     * Customize javascript action.
     */
    public class JavaScriptActionBuilder implements TestActionBuilder {

        /** JavaScript action */
        private final JavaScriptAction action;

        /**
         * Default constructor.
         * @param action
         */
        public JavaScriptActionBuilder(JavaScriptAction action) {
            this.action = action;
        }

        /**
         * Add script argument.
         * @param arg
         * @return
         */
        public JavaScriptActionBuilder argument(Object arg) {
            action.getArguments().add(arg);
            return this;
        }

        /**
         * Add expected error.
         * @param errors
         * @return
         */
        public JavaScriptActionBuilder errors(String ... errors) {
            action.setExpectedErrors(Arrays.asList(errors));
            return this;
        }

        @Override
        public TestAction build() {
            return SeleniumActionBuilder.this.build();
        }
    }

    /**
     * Customize wait until action.
     */
    public class WaitUntilActionBuilder extends ElementActionBuilder<WaitUntilAction> {

        /** WaitUntil action */
        private final WaitUntilAction action;

        /**
         * Default constructor.
         * @param action
         */
        public WaitUntilActionBuilder(WaitUntilAction action) {
            super(action);
            this.action = action;
        }

        /**
         * Add visible condition.
         * @return
         */
        public WaitUntilActionBuilder visible() {
            action.setCondition("visible");
            return this;
        }

        /**
         * Add hidden condition.
         * @return
         */
        public WaitUntilActionBuilder hidden() {
            action.setCondition("hidden");
            return this;
        }

        /**
         * Add timeout condition.
         * @return
         */
        public WaitUntilActionBuilder timeout(Long timeout) {
            action.setTimeout(timeout);
            return this;
        }

        @Override
        public WaitUntilActionBuilder element(By by) {
            super.element(by);
            return this;
        }

        @Override
        public WaitUntilActionBuilder element(String property, String propertyValue) {
            super.element(property, propertyValue);
            return this;
        }
    }

    /**
     * Window accessing action.
     */
    public class WindowActionBuilder implements TestActionBuilder {

        /** Window action */
        private final SeleniumWindowAction action;

        /**
         * Default constructor.
         * @param action
         */
        public WindowActionBuilder(SeleniumWindowAction action) {
            this.action = action;
        }

        /**
         * Set window name.
         * @param name
         * @return
         */
        public WindowActionBuilder window(String name) {
            action.setWindowName(name);
            return this;
        }

        @Override
        public TestAction build() {
            return SeleniumActionBuilder.this.build();
        }
    }

    /**
     * Customize element selecting action.
     */
    public class ElementActionBuilder<T extends FindElementAction> implements TestActionBuilder {

        /** Element action */
        private final T action;

        /**
         * Default constructor.
         * @param action
         */
        public ElementActionBuilder(T action) {
            this.action = action;
        }

        /**
         * Add element selector.
         * @param by
         * @return
         */
        public ElementActionBuilder element(By by) {
            action.setBy(by);
            return this;
        }

        /**
         * Add element property and value selector.
         * @return
         */
        public ElementActionBuilder element(String property, String propertyValue) {
            action.setProperty(property);
            action.setPropertyValue(propertyValue);
            return this;
        }

        @Override
        public TestAction build() {
            return SeleniumActionBuilder.this.build();
        }
    }

    /**
     * Customize element selecting action.
     */
    public class FindElementActionBuilder extends ElementActionBuilder<FindElementAction> {

        /** Find element action */
        private final FindElementAction action;

        /**
         * Default constructor.
         * @param action
         */
        public FindElementActionBuilder(FindElementAction action) {
            super(action);
            this.action = action;
        }

        /**
         * Add tag name validation.
         * @param tagName
         * @return
         */
        public FindElementActionBuilder tagName(String tagName) {
            action.setTagName(tagName);
            return this;
        }

        /**
         * Add text validation.
         * @param text
         * @return
         */
        public FindElementActionBuilder text(String text) {
            action.setText(text);
            return this;
        }

        /**
         * Add attribute validation.
         * @param name
         * @param value
         * @return
         */
        public FindElementActionBuilder attribute(String name, String value) {
            action.getAttributes().put(name, value);
            return this;
        }

        /**
         * Add css style validation.
         * @param name
         * @param value
         * @return
         */
        public FindElementActionBuilder style(String name, String value) {
            action.getStyles().put(name, value);
            return this;
        }

        /**
         * Add enabled validation.
         * @param enabled
         * @return
         */
        public FindElementActionBuilder enabled(boolean enabled) {
            action.setEnabled(enabled);
            return this;
        }

        /**
         * Add displayed validation.
         * @param displayed
         * @return
         */
        public FindElementActionBuilder displayed(boolean displayed) {
            action.setDisplayed(displayed);
            return this;
        }

        @Override
        public FindElementActionBuilder element(By by) {
            super.element(by);
            return this;
        }

        @Override
        public FindElementActionBuilder element(String property, String propertyValue) {
            super.element(property, propertyValue);
            return this;
        }
    }
}
