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

package org.citrusframework.playwright.yaml;

import org.citrusframework.TestActionBuilder;
import org.citrusframework.TestActor;
import org.citrusframework.api.yaml.SchemaProperty;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.playwright.actions.AbstractPlaywrightAction;
import org.citrusframework.playwright.actions.PlaywrightAction;
import org.citrusframework.playwright.endpoint.PlaywrightBrowser;
import org.citrusframework.spi.ReferenceResolver;
import org.citrusframework.spi.ReferenceResolverAware;

import static org.citrusframework.api.yaml.SchemaProperty.Kind.ACTION;

/**
 * YAML DSL entry point for Playwright actions.
 *
 * <p>Registered via {@code META-INF/citrus/yaml/builder/playwright} so YAML
 * test cases can use the {@code playwright} action namespace.</p>
 */
public class Playwright implements TestActionBuilder<PlaywrightAction>, ReferenceResolverAware {

    private static final String PLAYWRIGHT_GROUP = "playwright";
    private static final String PLAYWRIGHT_MODULE = "citrus-playwright";

    private AbstractPlaywrightAction.Builder<?, ?> builder;

    private String description;
    private String actor;
    private String browserName;

    private ReferenceResolver referenceResolver;

    @SchemaProperty(advanced = true, description = "Test action description printed when the action is executed.")
    public void setDescription(String value) {
        this.description = value;
    }

    @SchemaProperty(advanced = true, description = "Test actor that executes the action.")
    public void setActor(String actor) {
        this.actor = actor;
    }

    /**
     * Use a custom Playwright browser endpoint referenced by name.
     */
    @SchemaProperty(description = "Sets the Playwright browser. " +
            "Uses an endpoint URI or references an endpoint name.")
    public void setBrowser(String browser) {
        this.browserName = browser;
    }

    @SchemaProperty(kind = ACTION, group = PLAYWRIGHT_GROUP, module = PLAYWRIGHT_MODULE, description = "Starts the Playwright browser.")
    public void setStart(Start builder) {
        this.builder = builder;
    }

    @SchemaProperty(kind = ACTION, group = PLAYWRIGHT_GROUP, module = PLAYWRIGHT_MODULE, description = "Stops the Playwright browser.")
    public void setStop(Stop builder) {
        this.builder = builder;
    }

    @SchemaProperty(kind = ACTION, group = PLAYWRIGHT_GROUP, module = PLAYWRIGHT_MODULE, description = "Opens a URL in the browser.")
    public void setOpen(Open builder) {
        this.builder = builder;
    }

    @SchemaProperty(kind = ACTION, group = PLAYWRIGHT_GROUP, module = PLAYWRIGHT_MODULE, description = "Navigates browser history or reloads the page.")
    public void setNavigate(Navigate builder) {
        this.builder = builder;
    }

    @SchemaProperty(kind = ACTION, group = PLAYWRIGHT_GROUP, module = PLAYWRIGHT_MODULE, description = "Clicks an element on the page.")
    public void setClick(Click builder) {
        this.builder = builder;
    }

    @SchemaProperty(kind = ACTION, group = PLAYWRIGHT_GROUP, module = PLAYWRIGHT_MODULE, description = "Double-clicks an element on the page.")
    public void setDoubleClick(DoubleClick builder) {
        this.builder = builder;
    }

    @SchemaProperty(kind = ACTION, group = PLAYWRIGHT_GROUP, module = PLAYWRIGHT_MODULE, description = "Right-clicks an element on the page.")
    public void setRightClick(RightClick builder) {
        this.builder = builder;
    }

    @SchemaProperty(kind = ACTION, group = PLAYWRIGHT_GROUP, module = PLAYWRIGHT_MODULE, description = "Hovers over an element on the page.")
    public void setHover(Hover builder) {
        this.builder = builder;
    }

    @SchemaProperty(kind = ACTION, group = PLAYWRIGHT_GROUP, module = PLAYWRIGHT_MODULE, description = "Focuses an element on the page.")
    public void setFocus(Focus builder) {
        this.builder = builder;
    }

    @SchemaProperty(kind = ACTION, group = PLAYWRIGHT_GROUP, module = PLAYWRIGHT_MODULE, description = "Taps an element on the page.")
    public void setTap(Tap builder) {
        this.builder = builder;
    }

    @SchemaProperty(kind = ACTION, group = PLAYWRIGHT_GROUP, module = PLAYWRIGHT_MODULE, description = "Fills an input element with a value.")
    public void setFill(Fill builder) {
        this.builder = builder;
    }

    @SchemaProperty(kind = ACTION, group = PLAYWRIGHT_GROUP, module = PLAYWRIGHT_MODULE, description = "Clears an input element.")
    public void setClear(Clear builder) {
        this.builder = builder;
    }

    @SchemaProperty(kind = ACTION, group = PLAYWRIGHT_GROUP, module = PLAYWRIGHT_MODULE, description = "Presses a key or shortcut on an element.")
    public void setPress(Press builder) {
        this.builder = builder;
    }

    @SchemaProperty(kind = ACTION, group = PLAYWRIGHT_GROUP, module = PLAYWRIGHT_MODULE, description = "Checks a checkbox or radio element.")
    public void setCheck(Check builder) {
        this.builder = builder;
    }

    @SchemaProperty(kind = ACTION, group = PLAYWRIGHT_GROUP, module = PLAYWRIGHT_MODULE, description = "Unchecks a checkbox element.")
    public void setUncheck(Uncheck builder) {
        this.builder = builder;
    }

    @SchemaProperty(kind = ACTION, group = PLAYWRIGHT_GROUP, module = PLAYWRIGHT_MODULE, description = "Selects options in a select element.")
    public void setSelect(Select builder) {
        this.builder = builder;
    }

    @SchemaProperty(kind = ACTION, group = PLAYWRIGHT_GROUP, module = PLAYWRIGHT_MODULE, description = "Uploads files to a file input element.")
    public void setUpload(Upload builder) {
        this.builder = builder;
    }

    @SchemaProperty(kind = ACTION, group = PLAYWRIGHT_GROUP, module = PLAYWRIGHT_MODULE, description = "Waits for a condition, element state, or page load.")
    public void setWait(Wait builder) {
        this.builder = builder;
    }

    @SchemaProperty(description = "Screencast action configuration.")
    public void setScreencast(Screencast builder) {
        this.builder = builder;
    }

    @SchemaProperty(description = "WebAuthn credentials action configuration.")
    public void setCredentials(Credentials builder) {
        this.builder = builder;
    }

    @SchemaProperty(description = "File drop action configuration.")
    public void setDrop(Drop builder) {
        this.builder = builder;
    }

    @SchemaProperty(kind = ACTION, group = PLAYWRIGHT_GROUP, module = PLAYWRIGHT_MODULE, description = "Captures a screenshot of the page or element.")
    public void setScreenshot(Screenshot builder) {
        this.builder = builder;
    }

    @SchemaProperty(kind = ACTION, group = PLAYWRIGHT_GROUP, module = PLAYWRIGHT_MODULE, description = "Executes JavaScript in the browser context.")
    public void setJavaScript(JavaScript builder) {
        this.builder = builder;
    }

    @SchemaProperty(kind = ACTION, group = PLAYWRIGHT_GROUP, module = PLAYWRIGHT_MODULE, description = "Verifies page or element state.")
    public void setVerify(Verify builder) {
        this.builder = builder;
    }

    @SchemaProperty(kind = ACTION, group = PLAYWRIGHT_GROUP, module = PLAYWRIGHT_MODULE, description = "Extracts data from the page into a test variable.")
    public void setExtract(Extract builder) {
        this.builder = builder;
    }

    @SchemaProperty(kind = ACTION, group = PLAYWRIGHT_GROUP, module = PLAYWRIGHT_MODULE, description = "Interacts with browser console messages.")
    public void setConsole(Console builder) {
        this.builder = builder;
    }

    @SchemaProperty(kind = ACTION, group = PLAYWRIGHT_GROUP, module = PLAYWRIGHT_MODULE, description = "Manages browser contexts.")
    public void setContext(Context builder) {
        this.builder = builder;
    }

    @SchemaProperty(kind = ACTION, group = PLAYWRIGHT_GROUP, module = PLAYWRIGHT_MODULE, description = "Manages browser cookies.")
    public void setCookies(Cookies builder) {
        this.builder = builder;
    }

    @SchemaProperty(kind = ACTION, group = PLAYWRIGHT_GROUP, module = PLAYWRIGHT_MODULE, description = "Handles browser dialogs.")
    public void setDialog(Dialog builder) {
        this.builder = builder;
    }

    @SchemaProperty(kind = ACTION, group = PLAYWRIGHT_GROUP, module = PLAYWRIGHT_MODULE, description = "Handles file downloads.")
    public void setDownload(Download builder) {
        this.builder = builder;
    }

    @SchemaProperty(kind = ACTION, group = PLAYWRIGHT_GROUP, module = PLAYWRIGHT_MODULE, description = "Emulates device, viewport, or environment settings.")
    public void setEmulate(Emulate builder) {
        this.builder = builder;
    }

    @SchemaProperty(kind = ACTION, group = PLAYWRIGHT_GROUP, module = PLAYWRIGHT_MODULE, description = "Fills an input element with a value.")
    public void setEnter(Fill builder) {
        this.builder = builder;
    }

    @SchemaProperty(kind = ACTION, group = PLAYWRIGHT_GROUP, module = PLAYWRIGHT_MODULE, description = "Interacts with page frames.")
    public void setFrame(Frame builder) {
        this.builder = builder;
    }

    @SchemaProperty(kind = ACTION, group = PLAYWRIGHT_GROUP, module = PLAYWRIGHT_MODULE, description = "Intercepts and inspects network traffic.")
    public void setNetwork(Network builder) {
        this.builder = builder;
    }

    @SchemaProperty(kind = ACTION, group = PLAYWRIGHT_GROUP, module = PLAYWRIGHT_MODULE, description = "Manages browser pages or tabs.")
    public void setPage(Page builder) {
        this.builder = builder;
    }

    @SchemaProperty(kind = ACTION, group = PLAYWRIGHT_GROUP, module = PLAYWRIGHT_MODULE, description = "Executes a Playwright page object.")
    public void setPageObject(PageObject builder) {
        this.builder = builder;
    }

    @SchemaProperty(kind = ACTION, group = PLAYWRIGHT_GROUP, module = PLAYWRIGHT_MODULE, description = "Saves the page as a PDF document.")
    public void setPdf(Pdf builder) {
        this.builder = builder;
    }

    @SchemaProperty(kind = ACTION, group = PLAYWRIGHT_GROUP, module = PLAYWRIGHT_MODULE, description = "Grants or clears browser permissions.")
    public void setPermissions(Permissions builder) {
        this.builder = builder;
    }

    @SchemaProperty(kind = ACTION, group = PLAYWRIGHT_GROUP, module = PLAYWRIGHT_MODULE, description = "Manages browser storage.")
    public void setStorage(Storage builder) {
        this.builder = builder;
    }

    @SchemaProperty(kind = ACTION, group = PLAYWRIGHT_GROUP, module = PLAYWRIGHT_MODULE, description = "Controls Playwright tracing.")
    public void setTracing(Tracing builder) {
        this.builder = builder;
    }

    @Override
    public PlaywrightAction build() {
        if (builder == null) {
            throw new CitrusRuntimeException("Missing Playwright action - please provide proper action details");
        }

        builder.description(description);

        if (referenceResolver != null) {
            if (browserName != null) {
                builder.browser(referenceResolver.resolve(browserName, PlaywrightBrowser.class));
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
