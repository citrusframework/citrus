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

import com.microsoft.playwright.Page;

import java.nio.file.Files;
import java.nio.file.Path;

import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.playwright.endpoint.PlaywrightBrowser;
import org.citrusframework.playwright.util.LocatorResolver;

/**
 * Citrus action that exports the current Chromium page to PDF.
 *
 * <p>Playwright supports PDF generation for Chromium. The action creates the
 * target parent directory and can expose the generated path as a Citrus
 * variable.</p>
 */
public class PdfAction extends AbstractPlaywrightAction {

    private final String path;
    private final String variable;
    private final boolean printBackground;

    public PdfAction(Builder builder) {
        super("pdf", builder);
        this.path = builder.path;
        this.variable = builder.variable;
        this.printBackground = builder.printBackground;
    }

    @Override
    protected void execute(PlaywrightBrowser browser, TestContext context) {
        Path pdfPath = Path.of(LocatorResolver.resolve(path, context));
        try {
            if (pdfPath.getParent() != null) {
                Files.createDirectories(pdfPath.getParent());
            }
        } catch (Exception e) {
            throw new CitrusRuntimeException("Failed to create Playwright PDF directory: " + pdfPath.getParent(), e);
        }
        browser.getCurrentPage().pdf(new Page.PdfOptions().setPath(pdfPath).setPrintBackground(printBackground));
        if (variable != null) {
            context.setVariable(variable, pdfPath.toString());
        }
    }

    /**
     * Fluent builder for PDF export settings.
     */
    public static class Builder extends AbstractPlaywrightAction.Builder<PdfAction, Builder> {
        private String path;
        private String variable;
        private boolean printBackground = true;

        /**
         * Sets the target PDF path.
         *
         * @param path target PDF path
         * @return this builder
         */
        public Builder path(String path) {
            this.path = path;
            return this;
        }

        /**
         * Stores the generated PDF path in a Citrus variable.
         *
         * @param variable variable name
         * @return this builder
         */
        public Builder variable(String variable) {
            this.variable = variable;
            return this;
        }

        /**
         * Configures whether CSS backgrounds are printed.
         *
         * @param printBackground true to include backgrounds
         * @return this builder
         */
        public Builder printBackground(boolean printBackground) {
            this.printBackground = printBackground;
            return this;
        }

        @Override
        public PdfAction build() {
            if (path == null || path.isBlank()) {
                throw new CitrusRuntimeException("Missing Playwright PDF path");
            }
            return new PdfAction(this);
        }
    }
}
