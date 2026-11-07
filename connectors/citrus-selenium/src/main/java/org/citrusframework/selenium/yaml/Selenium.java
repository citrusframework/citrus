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

package org.citrusframework.selenium.yaml;

import org.citrusframework.TestActionBuilder;
import org.citrusframework.TestActionContainerBuilder;
import org.citrusframework.TestActor;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.selenium.actions.AbstractSeleniumAction;
import org.citrusframework.selenium.actions.SeleniumAction;
import org.citrusframework.selenium.endpoint.SeleniumBrowser;
import org.citrusframework.spi.ReferenceResolver;
import org.citrusframework.spi.ReferenceResolverAware;
import org.citrusframework.api.yaml.SchemaProperty;

import static org.citrusframework.api.yaml.SchemaProperty.Kind.ACTION;

public class Selenium implements TestActionBuilder<SeleniumAction>, ReferenceResolverAware {

    private static final String SELENIUM_GROUP = "selenium";
    private static final String SELENIUM_MODULE = "citrus-selenium";

    private AbstractSeleniumAction.Builder<?, ?> builder;

    private String description;
    private String actor;

    private String seleniumBrowser;

    private ReferenceResolver referenceResolver;

    @SchemaProperty(advanced = true, description = "Test action description printed when the action is executed.")
    public void setDescription(String value) {
        this.description = value;
    }

    @SchemaProperty(advanced = true, description = "Reference to the test actor that executes this action.")
    public void setActor(String actor) {
        this.actor = actor;
    }

    /**
     * Use a custom selenium browser.
     */
    @SchemaProperty(description = "Sets the Selenium browser." +
            "Uses an endpoint URI or references an endpoint name.")
    public void setBrowser(String browser) {
        this.seleniumBrowser = browser;
    }

    /**
     * Start browser instance.
     */
    @SchemaProperty(kind = ACTION, group = SELENIUM_GROUP, module = SELENIUM_MODULE, description = "Starts the Selenium browser instance.")
    public void setStart(StartBrowser builder) {
        this.builder = builder;
    }

    /**
     * Stop browser instance.
     */
    @SchemaProperty(kind = ACTION, group = SELENIUM_GROUP, module = SELENIUM_MODULE, description = "Stops the Selenium browser instance.")
    public void setStop(StopBrowser builder) {
        this.builder = builder;
    }

    /**
     * Alert element.
     */
    @SchemaProperty(kind = ACTION, group = SELENIUM_GROUP, module = SELENIUM_MODULE, description = "Validates alert text and accepts or dismisses the current alert dialog.")
    public void setAlert(Alert builder) {
        this.builder = builder;
    }

    /**
     * Navigate action.
     */
    @SchemaProperty(kind = ACTION, group = SELENIUM_GROUP, module = SELENIUM_MODULE, description = "Navigates to a page URL, relative path, or browser history destination.")
    public void setNavigate(Navigate builder) {
        this.builder = builder;
    }

    /**
     * Page action.
     */
    @SchemaProperty(kind = ACTION, group = SELENIUM_GROUP, module = SELENIUM_MODULE, description = "Initializes a page object and executes an action or validates its state.")
    public void setPage(Page builder) {
        this.builder = builder;
    }

    /**
     * Finds element.
     */
    @SchemaProperty(kind = ACTION, group = SELENIUM_GROUP, module = SELENIUM_MODULE, description = "Finds an element on the current page and validates its expected properties.")
    public void setFind(FindElement builder) {
        this.builder = builder;
    }

    /**
     * Dropdown select single option action.
     */
    @SchemaProperty(kind = ACTION, group = SELENIUM_GROUP, module = SELENIUM_MODULE, description = "Selects or deselects options in a dropdown element.")
    public void setDropdownSelect(DropDownSelect builder) {
        this.builder = builder;
    }

    /**
     * Set input action.
     */
    @SchemaProperty(kind = ACTION, group = SELENIUM_GROUP, module = SELENIUM_MODULE, description = "Sets the value of an input element.")
    public void setSetInput(SetInput builder) {
        this.builder = builder;
    }

    /**
     * Fill form action.
     */
    @SchemaProperty(kind = ACTION, group = SELENIUM_GROUP, module = SELENIUM_MODULE, description = "Fills form fields and optionally submits the form.")
    public void setFillForm(FillForm builder) {
        this.builder = builder;
    }

    /**
     * Check input action.
     */
    @SchemaProperty(kind = ACTION, group = SELENIUM_GROUP, module = SELENIUM_MODULE, description = "Checks or unchecks a checkbox input element.")
    public void setCheckInput(CheckInput builder) {
        this.builder = builder;
    }

    /**
     * Clicks element.
     */
    @SchemaProperty(kind = ACTION, group = SELENIUM_GROUP, module = SELENIUM_MODULE, description = "Clicks an element on the current page.")
    public void setClick(Click builder) {
        this.builder = builder;
    }

    /**
     * Hover element.
     */
    @SchemaProperty(kind = ACTION, group = SELENIUM_GROUP, module = SELENIUM_MODULE, description = "Moves the mouse pointer over an element.")
    public void setHover(Hover builder) {
        this.builder = builder;
    }

    /**
     * Double click element.
     */
    @SchemaProperty(kind = ACTION, group = SELENIUM_GROUP, module = SELENIUM_MODULE, description = "Double-clicks an element on the current page.")
    public void setDoubleClick(DoubleClick builder) {
        this.builder = builder;
    }

    /**
     * Right click element.
     */
    @SchemaProperty(kind = ACTION, group = SELENIUM_GROUP, module = SELENIUM_MODULE, description = "Performs a right-click on an element to open its context menu.")
    public void setRightClick(RightClick builder) {
        this.builder = builder;
    }

    /**
     * Drag and drop element.
     */
    @SchemaProperty(kind = ACTION, group = SELENIUM_GROUP, module = SELENIUM_MODULE, description = "Drags a source element and drops it onto a target element.")
    public void setDragAndDrop(DragAndDrop builder) {
        this.builder = builder;
    }

    /**
     * Press keys.
     */
    @SchemaProperty(kind = ACTION, group = SELENIUM_GROUP, module = SELENIUM_MODULE, description = "Presses a key or key combination on an element or the focused element.")
    public void setPress(Press builder) {
        this.builder = builder;
    }

    /**
     * Verify page, element or value on the page.
     */
    @SchemaProperty(kind = ACTION, group = SELENIUM_GROUP, module = SELENIUM_MODULE, description = "Verifies the current page URL, title, or element state and values.")
    public void setVerify(Verify builder) {
        this.builder = builder;
    }

    /**
     * Extract page, element or value on the page into a variable.
     */
    @SchemaProperty(kind = ACTION, group = SELENIUM_GROUP, module = SELENIUM_MODULE, description = "Extracts a page or element value into a test variable.")
    public void setExtract(Extract builder) {
        this.builder = builder;
    }

    /**
     * Clear browser cache.
     */
    @SchemaProperty(kind = ACTION, group = SELENIUM_GROUP, module = SELENIUM_MODULE, description = "Deletes browser cookies and clears local and session web storage.")
    public void setClearCache(ClearBrowserCache builder) {
        this.builder = builder;
    }

    /**
     * Make screenshot.
     */
    @SchemaProperty(kind = ACTION, group = SELENIUM_GROUP, module = SELENIUM_MODULE, description = "Takes a screenshot of the current browser page.")
    public void setScreenshot(MakeScreenshot builder) {
        this.builder = builder;
    }

    /**
     * Store file.
     */
    @SchemaProperty(kind = ACTION, group = SELENIUM_GROUP, module = SELENIUM_MODULE, description = "Stores a file in the browser's temporary download directory.")
    public void setStoreFile(StoreFile builder) {
        this.builder = builder;
    }

    /**
     * Get stored file.
     */
    @SchemaProperty(kind = ACTION, group = SELENIUM_GROUP, module = SELENIUM_MODULE, description = "Retrieves a file from the browser's temporary download directory.")
    public void setGetStoredFile(GetStoredFile builder) {
        this.builder = builder;
    }

    /**
     * Wait until element meets condition.
     */
    @SchemaProperty(kind = ACTION, group = SELENIUM_GROUP, module = SELENIUM_MODULE, description = "Waits until an element or page satisfies the specified condition.")
    public void setWaitUntil(WaitUntil builder) {
        this.builder = builder;
    }

    /**
     * Execute JavaScript.
     */
    @SchemaProperty(kind = ACTION, group = SELENIUM_GROUP, module = SELENIUM_MODULE, description = "Executes JavaScript in the browser and optionally validates errors or stores the result.")
    public void setJavaScript(JavaScript builder) {
        this.builder = builder;
    }

    /**
     * Switch frame.
     */
    @SchemaProperty(kind = ACTION, group = SELENIUM_GROUP, module = SELENIUM_MODULE, description = "Switches browser focus to a frame, parent frame, or default content.")
    public void setFrame(Frame builder) {
        this.builder = builder;
    }

    /**
     * Cookie action.
     */
    @SchemaProperty(kind = ACTION, group = SELENIUM_GROUP, module = SELENIUM_MODULE, description = "Adds, reads, verifies, or deletes browser cookies.")
    public void setCookies(Cookies builder) {
        this.builder = builder;
    }

    /**
     * Open window.
     */
    @SchemaProperty(kind = ACTION, group = SELENIUM_GROUP, module = SELENIUM_MODULE, description = "Opens a new browser window and stores its handle under the given name.")
    public void setOpenWindow(OpenWindow builder) {
        this.builder = builder;
    }

    /**
     * Close window.
     */
    @SchemaProperty(kind = ACTION, group = SELENIUM_GROUP, module = SELENIUM_MODULE, description = "Closes the named browser window.")
    public void setCloseWindow(CloseWindow builder) {
        this.builder = builder;
    }

    /**
     * Focus window.
     */
    @SchemaProperty(kind = ACTION, group = SELENIUM_GROUP, module = SELENIUM_MODULE, description = "Switches browser focus to the named window.")
    public void setFocusWindow(SwitchWindow builder) {
        this.builder = builder;
    }

    /**
     * Switch window.
     */
    @SchemaProperty(kind = ACTION, group = SELENIUM_GROUP, module = SELENIUM_MODULE, description = "Switches browser focus to the named window.")
    public void setSwitchWindow(SwitchWindow builder) {
        this.builder = builder;
    }

    @Override
    public SeleniumAction build() {
        if (builder == null) {
            throw new CitrusRuntimeException("Missing Selenium action - please provide proper action details");
        }

        if (builder instanceof TestActionContainerBuilder<?,?>) {
            ((TestActionContainerBuilder<?,?>) builder).getActions().stream()
                    .filter(action -> action instanceof ReferenceResolverAware)
                    .forEach(action -> ((ReferenceResolverAware) action).setReferenceResolver(referenceResolver));
        }

        if (builder instanceof ReferenceResolverAware) {
            ((ReferenceResolverAware) builder).setReferenceResolver(referenceResolver);
        }

        builder.description(description);

        if (referenceResolver != null) {
            if (seleniumBrowser != null) {
                builder.browser(referenceResolver.resolve(seleniumBrowser, SeleniumBrowser.class));
            }

            if (actor != null) {
                builder.actor(referenceResolver.resolve(actor, TestActor.class));
            }
        }

        return builder.build();
    }

    @Override
    public void setReferenceResolver(ReferenceResolver referenceResolver) {
        this.referenceResolver = referenceResolver;
    }
}
