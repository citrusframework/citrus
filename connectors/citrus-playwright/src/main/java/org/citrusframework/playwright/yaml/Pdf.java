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

import org.citrusframework.TestActor;
import org.citrusframework.api.yaml.SchemaProperty;
import org.citrusframework.playwright.actions.AbstractPlaywrightAction;
import org.citrusframework.playwright.actions.PdfAction;
import org.citrusframework.playwright.endpoint.PlaywrightBrowser;

/**
 * Prints the current page to a PDF file (Chromium headless only).
 */
public class Pdf extends AbstractPlaywrightAction.Builder<PdfAction, Pdf> {

    private final PdfAction.Builder delegate = new PdfAction.Builder();

    @SchemaProperty
    public void setPath(String path) {
        delegate.path(path);
    }

    @SchemaProperty
    public void setVariable(String variable) {
        delegate.variable(variable);
    }

    @SchemaProperty
    public void setPrintBackground(Boolean printBackground) {
        delegate.printBackground(printBackground);
    }

    @Override
    public Pdf description(String description) {
        delegate.description(description);
        return this;
    }

    @Override
    public Pdf actor(TestActor actor) {
        delegate.actor(actor);
        return this;
    }

    @Override
    public Pdf browser(PlaywrightBrowser browser) {
        delegate.browser(browser);
        return this;
    }

    @Override
    public PdfAction build() {
        return delegate.build();
    }
}
