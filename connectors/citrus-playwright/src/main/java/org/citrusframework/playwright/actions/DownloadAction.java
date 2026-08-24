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

import com.microsoft.playwright.Download;

import java.nio.file.Files;
import java.nio.file.Path;

import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.playwright.endpoint.PlaywrightBrowser;
import org.citrusframework.playwright.model.DownloadMetadata;
import org.citrusframework.playwright.model.LocatorSpec;
import org.citrusframework.playwright.util.LocatorResolver;

/**
 * Citrus action that waits for a browser download and optionally persists it.
 *
 * <p>The download is triggered either by clicking a locator or by evaluating a
 * script in the current page. The resulting file path and suggested file name
 * can be exposed as Citrus variables.</p>
 */
public class DownloadAction extends AbstractPlaywrightAction {

    private final LocatorSpec triggerLocator;
    private final String triggerScript;
    private final String saveAs;
    private final String pathVariable;
    private final String filenameVariable;

    public DownloadAction(Builder builder) {
        super("download", builder);
        this.triggerLocator = builder.triggerLocator;
        this.triggerScript = builder.triggerScript;
        this.saveAs = builder.saveAs;
        this.pathVariable = builder.pathVariable;
        this.filenameVariable = builder.filenameVariable;
    }

    @Override
    protected void execute(PlaywrightBrowser browser, TestContext context) {
        Download download = browser.getCurrentPage().waitForDownload(() -> {
            if (triggerScript != null) {
                browser.getCurrentPage().evaluate(LocatorResolver.resolve(triggerScript, context));
            } else {
                LocatorResolver.resolve(browser.getCurrentPage(), triggerLocator, context).click();
            }
        });

        Path savedPath = null;
        if (saveAs != null) {
            savedPath = Path.of(LocatorResolver.resolve(saveAs, context));
            createParentDirectories(savedPath);
            download.saveAs(savedPath);
        }
        if (pathVariable != null) {
            context.setVariable(pathVariable, savedPath == null ? download.path().toString() : savedPath.toString());
        }
        if (filenameVariable != null) {
            context.setVariable(filenameVariable, download.suggestedFilename());
        }
        Path metadataPath = savedPath == null ? download.path() : savedPath;
        browser.setLatestDownloadMetadata(DownloadMetadata.of(
                metadataPath,
                download.suggestedFilename(),
                download.url(),
                download.failure()));
    }

    private void createParentDirectories(Path path) {
        try {
            if (path.getParent() != null) {
                Files.createDirectories(path.getParent());
            }
        } catch (Exception e) {
            throw new CitrusRuntimeException("Failed to create download target directory: " + path.getParent(), e);
        }
    }

    /**
     * Fluent builder for download trigger and artifact settings.
     */
    public static class Builder extends AbstractPlaywrightAction.Builder<DownloadAction, Builder> {
        private LocatorSpec triggerLocator;
        private String triggerScript;
        private String saveAs;
        private String pathVariable;
        private String filenameVariable;

        /**
         * Triggers the download by clicking a CSS locator.
         *
         * @param locator CSS locator to click
         * @return this builder
         */
        public Builder click(String locator) {
            this.triggerLocator = LocatorSpec.css(locator);
            return this;
        }

        /**
         * Triggers the download by evaluating JavaScript.
         *
         * @param triggerScript JavaScript expression or function body for Playwright evaluation
         * @return this builder
         */
        public Builder triggerScript(String triggerScript) {
            this.triggerScript = triggerScript;
            return this;
        }

        /**
         * Saves the downloaded file to the supplied path.
         *
         * @param saveAs target file path
         * @return this builder
         */
        public Builder saveAs(String saveAs) {
            this.saveAs = saveAs;
            return this;
        }

        /**
         * Stores the final download path in a Citrus variable.
         *
         * @param pathVariable variable name
         * @return this builder
         */
        public Builder pathVariable(String pathVariable) {
            this.pathVariable = pathVariable;
            return this;
        }

        /**
         * Stores the Playwright suggested filename in a Citrus variable.
         *
         * @param filenameVariable variable name
         * @return this builder
         */
        public Builder filenameVariable(String filenameVariable) {
            this.filenameVariable = filenameVariable;
            return this;
        }

        @Override
        public DownloadAction build() {
            if (triggerLocator == null && (triggerScript == null || triggerScript.isBlank())) {
                throw new CitrusRuntimeException("Missing Playwright download trigger - call click(...) or triggerScript(...)");
            }
            return new DownloadAction(this);
        }
    }
}
