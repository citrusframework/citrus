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

    @SchemaProperty(advanced = true)
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

    @SchemaProperty(kind = ACTION, group = PLAYWRIGHT_GROUP, module = PLAYWRIGHT_MODULE)
    public void setStart(Start builder) {
        this.builder = builder;
    }

    @SchemaProperty(kind = ACTION, group = PLAYWRIGHT_GROUP, module = PLAYWRIGHT_MODULE)
    public void setStop(Stop builder) {
        this.builder = builder;
    }

    @SchemaProperty(kind = ACTION, group = PLAYWRIGHT_GROUP, module = PLAYWRIGHT_MODULE)
    public void setOpen(Open builder) {
        this.builder = builder;
    }

    @SchemaProperty(kind = ACTION, group = PLAYWRIGHT_GROUP, module = PLAYWRIGHT_MODULE)
    public void setNavigate(Navigate builder) {
        this.builder = builder;
    }

    @SchemaProperty(kind = ACTION, group = PLAYWRIGHT_GROUP, module = PLAYWRIGHT_MODULE)
    public void setClick(Click builder) {
        this.builder = builder;
    }

    @SchemaProperty(kind = ACTION, group = PLAYWRIGHT_GROUP, module = PLAYWRIGHT_MODULE)
    public void setDoubleClick(DoubleClick builder) {
        this.builder = builder;
    }

    @SchemaProperty(kind = ACTION, group = PLAYWRIGHT_GROUP, module = PLAYWRIGHT_MODULE)
    public void setRightClick(RightClick builder) {
        this.builder = builder;
    }

    @SchemaProperty(kind = ACTION, group = PLAYWRIGHT_GROUP, module = PLAYWRIGHT_MODULE)
    public void setHover(Hover builder) {
        this.builder = builder;
    }

    @SchemaProperty(kind = ACTION, group = PLAYWRIGHT_GROUP, module = PLAYWRIGHT_MODULE)
    public void setFocus(Focus builder) {
        this.builder = builder;
    }

    @SchemaProperty(kind = ACTION, group = PLAYWRIGHT_GROUP, module = PLAYWRIGHT_MODULE)
    public void setTap(Tap builder) {
        this.builder = builder;
    }

    @SchemaProperty(kind = ACTION, group = PLAYWRIGHT_GROUP, module = PLAYWRIGHT_MODULE)
    public void setFill(Fill builder) {
        this.builder = builder;
    }

    @SchemaProperty(kind = ACTION, group = PLAYWRIGHT_GROUP, module = PLAYWRIGHT_MODULE)
    public void setClear(Clear builder) {
        this.builder = builder;
    }

    @SchemaProperty(kind = ACTION, group = PLAYWRIGHT_GROUP, module = PLAYWRIGHT_MODULE)
    public void setPress(Press builder) {
        this.builder = builder;
    }

    @SchemaProperty(kind = ACTION, group = PLAYWRIGHT_GROUP, module = PLAYWRIGHT_MODULE)
    public void setCheck(Check builder) {
        this.builder = builder;
    }

    @SchemaProperty(kind = ACTION, group = PLAYWRIGHT_GROUP, module = PLAYWRIGHT_MODULE)
    public void setUncheck(Uncheck builder) {
        this.builder = builder;
    }

    @SchemaProperty(kind = ACTION, group = PLAYWRIGHT_GROUP, module = PLAYWRIGHT_MODULE)
    public void setSelect(Select builder) {
        this.builder = builder;
    }

    @SchemaProperty(kind = ACTION, group = PLAYWRIGHT_GROUP, module = PLAYWRIGHT_MODULE)
    public void setUpload(Upload builder) {
        this.builder = builder;
    }

    @SchemaProperty(kind = ACTION, group = PLAYWRIGHT_GROUP, module = PLAYWRIGHT_MODULE)
    public void setWait(Wait builder) {
        this.builder = builder;
    }

    @SchemaProperty(kind = ACTION, group = PLAYWRIGHT_GROUP, module = PLAYWRIGHT_MODULE)
    public void setScreenshot(Screenshot builder) {
        this.builder = builder;
    }

    @SchemaProperty(kind = ACTION, group = PLAYWRIGHT_GROUP, module = PLAYWRIGHT_MODULE)
    public void setJavaScript(JavaScript builder) {
        this.builder = builder;
    }

    @SchemaProperty(kind = ACTION, group = PLAYWRIGHT_GROUP, module = PLAYWRIGHT_MODULE)
    public void setVerify(Verify builder) {
        this.builder = builder;
    }

    @SchemaProperty(kind = ACTION, group = PLAYWRIGHT_GROUP, module = PLAYWRIGHT_MODULE)
    public void setExtract(Extract builder) {
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
