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
import org.citrusframework.yaml.SchemaProperty;

import static org.citrusframework.yaml.SchemaProperty.Kind.ACTION;

public class Selenium implements TestActionBuilder<SeleniumAction>, ReferenceResolverAware {

    private AbstractSeleniumAction.Builder<?, ?> builder;

    private String description;
    private String actor;

    private String seleniumBrowser;

    private ReferenceResolver referenceResolver;

    @SchemaProperty(advanced = true, description = "Test action description printed when the action is executed.")
    public void setDescription(String value) {
        this.description = value;
    }

    @SchemaProperty(advanced = true)
    public void setActor(String actor) {
        this.actor = actor;
    }

    /**
     * Use a custom selenium browser.
     */
    @SchemaProperty
    public void setBrowser(String browser) {
        this.seleniumBrowser = browser;
    }

    /**
     * Start browser instance.
     */
    @SchemaProperty(kind = ACTION, group = "selenium")
    public void setStart(StartBrowser builder) {
        this.builder = builder;
    }

    /**
     * Stop browser instance.
     */
    @SchemaProperty(kind = ACTION, group = "selenium")
    public void setStop(StopBrowser builder) {
        this.builder = builder;
    }

    /**
     * Alert element.
     */
    @SchemaProperty(kind = ACTION, group = "selenium")
    public void setAlert(Alert builder) {
        this.builder = builder;
    }

    /**
     * Navigate action.
     */
    @SchemaProperty(kind = ACTION, group = "selenium")
    public void setNavigate(Navigate builder) {
        this.builder = builder;
    }

    /**
     * Page action.
     */
    @SchemaProperty(kind = ACTION, group = "selenium")
    public void setPage(Page builder) {
        this.builder = builder;
    }

    /**
     * Finds element.
     */
    @SchemaProperty(kind = ACTION, group = "selenium")
    public void setFind(FindElement builder) {
        this.builder = builder;
    }

    /**
     * Dropdown select single option action.
     */
    @SchemaProperty(kind = ACTION, group = "selenium")
    public void setDropdownSelect(DropDownSelect builder) {
        this.builder = builder;
    }

    /**
     * Set input action.
     */
    @SchemaProperty(kind = ACTION, group = "selenium")
    public void setSetInput(SetInput builder) {
        this.builder = builder;
    }

    /**
     * Fill form action.
     */
    @SchemaProperty(kind = ACTION, group = "selenium")
    public void setFillForm(FillForm builder) {
        this.builder = builder;
    }

    /**
     * Check input action.
     */
    @SchemaProperty(kind = ACTION, group = "selenium")
    public void setCheckInput(CheckInput builder) {
        this.builder = builder;
    }

    /**
     * Clicks element.
     */
    @SchemaProperty(kind = ACTION, group = "selenium")
    public void setClick(Click builder) {
        this.builder = builder;
    }

    /**
     * Hover element.
     */
    @SchemaProperty(kind = ACTION, group = "selenium")
    public void setHover(Hover builder) {
        this.builder = builder;
    }

    /**
     * Clear browser cache.
     */
    @SchemaProperty(kind = ACTION, group = "selenium")
    public void setClearCache(ClearBrowserCache builder) {
        this.builder = builder;
    }

    /**
     * Make screenshot.
     */
    @SchemaProperty(kind = ACTION, group = "selenium")
    public void setScreenshot(MakeScreenshot builder) {
        this.builder = builder;
    }

    /**
     * Store file.
     */
    @SchemaProperty(kind = ACTION, group = "selenium")
    public void setStoreFile(StoreFile builder) {
        this.builder = builder;
    }

    /**
     * Get stored file.
     */
    @SchemaProperty(kind = ACTION, group = "selenium")
    public void setGetStoredFile(GetStoredFile builder) {
        this.builder = builder;
    }

    /**
     * Wait until element meets condition.
     */
    @SchemaProperty(kind = ACTION, group = "selenium")
    public void setWaitUntil(WaitUntil builder) {
        this.builder = builder;
    }

    /**
     * Execute JavaScript.
     */
    @SchemaProperty(kind = ACTION, group = "selenium")
    public void setJavaScript(JavaScript builder) {
        this.builder = builder;
    }

    /**
     * Open window.
     */
    @SchemaProperty(kind = ACTION, group = "selenium")
    public void setOpenWindow(OpenWindow builder) {
        this.builder = builder;
    }

    /**
     * Close window.
     */
    @SchemaProperty(kind = ACTION, group = "selenium")
    public void setCloseWindow(CloseWindow builder) {
        this.builder = builder;
    }

    /**
     * Focus window.
     */
    @SchemaProperty(kind = ACTION, group = "selenium")
    public void setFocusWindow(SwitchWindow builder) {
        this.builder = builder;
    }

    /**
     * Switch window.
     */
    @SchemaProperty(kind = ACTION, group = "selenium")
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
