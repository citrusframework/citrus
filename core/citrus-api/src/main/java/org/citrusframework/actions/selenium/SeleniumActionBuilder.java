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

package org.citrusframework.actions.selenium;

import java.nio.charset.Charset;

import org.citrusframework.TestAction;
import org.citrusframework.TestActionBuilder;
import org.citrusframework.endpoint.Endpoint;
import org.citrusframework.spi.Resource;

public interface SeleniumActionBuilder<T extends TestAction, B extends TestActionBuilder.DelegatingTestActionBuilder<T>> {

    /**
     * Use a custom selenium browser.
     */
    SeleniumActionBuilder<T, B> browser(Endpoint endpoint);

    /**
     * Start browser instance.
     */
    SeleniumStartBrowserActionBuilder<?, ?> start();

    /**
     * Start explicit browser instance.
     */
    SeleniumStartBrowserActionBuilder<?, ?> start(Endpoint browser);

    /**
     * Stop browser instance.
     */
    SeleniumStopBrowserActionBuilder<?, ?> stop();

    /**
     * Stop explicit browser instance.
     */
    SeleniumStopBrowserActionBuilder<?, ?> stop(Endpoint browser);

    /**
     * Alert element.
     */
    SeleniumAlertActionBuilder<?, ?> alert();

    /**
     * Navigate action.
     */
    SeleniumNavigateActionBuilder<?, ?> navigate();

    /**
     * Navigate action.
     */
    default SeleniumNavigateActionBuilder<?, ?> navigate(String page) {
        return navigate().page(page);
    }

    /**
     * Page action.
     */
    SeleniumPageActionBuilder<?, ?> page();

    /**
     * Page action.
     */
    default SeleniumPageActionBuilder<?, ?> page(WebPage page) {
        return page().page(page);
    }

    /**
     * Page action with given web page type.
     */
    default SeleniumPageActionBuilder<?, ?> page(Class<? extends WebPage> pageType) {
        return page().type(pageType);
    }

    /**
     * Finds element.
     */
    SeleniumFindElementActionBuilder<?, ?> find();

    /**
     * Dropdown select single option action.
     */
    SeleniumDropDownSelectActionBuilder<?, ?> select();

    /**
     * Dropdown select single option action.
     */
    default SeleniumDropDownSelectActionBuilder<?, ?> select(String option) {
        return select().option(option);
    }

    /**
     * Dropdown select multiple options action.
     */
    default SeleniumDropDownSelectActionBuilder<?, ?> select(String... options) {
        return select().options(options);
    }

    /**
     * Set input action.
     */
    SeleniumSetInputActionBuilder<?, ?> setInput();

    /**
     * Set input action.
     */
    default SeleniumSetInputActionBuilder<?, ?> setInput(String value) {
        return setInput().value(value);
    }

    /**
     * Fill form action.
     */
    SeleniumFillFormActionBuilder<?, ?> fillForm();

    /**
     * Check input action.
     */
    SeleniumCheckInputActionBuilder<?, ?> checkInput();

    /**
     * Check input action.
     */
    default SeleniumCheckInputActionBuilder<?, ?> checkInput(boolean checked) {
        return checkInput().checked(checked);
    }

    /**
     * Clicks element.
     */
    SeleniumClickActionBuilder<?, ?> click();

    /**
     * Hover element.
     */
    SeleniumHoverActionBuilder<?, ?> hover();

    /**
     * Clear browser cache.
     */
    SeleniumClearBrowserCacheActionBuilder<?, ?> clearCache();

    /**
     * Make screenshot.
     */
    SeleniumMakeScreenshotActionBuilder<?, ?> screenshot();

    /**
     * Make screenshot with custom output directory.
     */
    default SeleniumMakeScreenshotActionBuilder<?, ?> screenshot(String outputDir) {
        return screenshot().outputDir(outputDir);
    }

    /**
     * Store file.
     */
    SeleniumStoreFileActionBuilder<?, ?> store();

    /**
     * Store file.
     */
    default SeleniumStoreFileActionBuilder<?, ?> store(String filePath) {
        return store().filePath(filePath);
    }

    /**
     * Get stored file.
     */
    SeleniumGetStoredFileActionBuilder<?, ?> getStored();

    /**
     * Get stored file.
     */
    default SeleniumGetStoredFileActionBuilder<?, ?> getStored(String fileName) {
        return getStored().fileName(fileName);
    }

    /**
     * Wait until element meets condition.
     */
    SeleniumWaitUntilActionBuilder<?, ?> waitUntil();

    /**
     * Execute JavaScript.
     */
    SeleniumJavaScriptActionBuilder<?, ?> javascript();

    /**
     * Execute JavaScript.
     */
    default SeleniumJavaScriptActionBuilder<?, ?> javascript(String script) {
        return javascript().script(script);
    }

    /**
     * Execute JavaScript.
     */
    default SeleniumJavaScriptActionBuilder<?, ?> javascript(Resource scriptResource) {
        return javascript().script(scriptResource);
    }

    /**
     * Execute JavaScript.
     */
    default SeleniumJavaScriptActionBuilder<?, ?> javascript(Resource scriptResource, Charset charset) {
        return javascript().script(scriptResource, charset);
    }

    /**
     * Open window.
     */
    SeleniumOpenWindowActionBuilder<?, ?> open();

    /**
     * Close window.
     */
    SeleniumCloseWindowActionBuilder<?, ?> close();

    /**
     * Switch window.
     */
    SeleniumSwitchWindowActionBuilder<?, ?> focus();

    /**
     * Switch window.
     */
    SeleniumSwitchWindowActionBuilder<?, ?> switchWindow();

    interface BuilderFactory {

        SeleniumActionBuilder<?, ?> selenium();

    }
}
