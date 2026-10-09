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
import org.citrusframework.playwright.actions.DownloadAction;
import org.citrusframework.playwright.endpoint.PlaywrightBrowser;

/**
 * Waits for a download triggered by clicking an element or running a script.
 */
public class Download extends AbstractPlaywrightAction.Builder<DownloadAction, Download> {

    private final DownloadAction.Builder delegate = new DownloadAction.Builder();

    @SchemaProperty
    public void setElement(Element element) {
        delegate.click(element.toLocatorSpec());
    }

    @SchemaProperty
    public void setTriggerScript(String triggerScript) {
        delegate.triggerScript(triggerScript);
    }

    @SchemaProperty
    public void setSaveAs(String saveAs) {
        delegate.saveAs(saveAs);
    }

    @SchemaProperty
    public void setPathVariable(String pathVariable) {
        delegate.pathVariable(pathVariable);
    }

    @SchemaProperty
    public void setFilenameVariable(String filenameVariable) {
        delegate.filenameVariable(filenameVariable);
    }

    @Override
    public Download description(String description) {
        delegate.description(description);
        return this;
    }

    @Override
    public Download actor(TestActor actor) {
        delegate.actor(actor);
        return this;
    }

    @Override
    public Download browser(PlaywrightBrowser browser) {
        delegate.browser(browser);
        return this;
    }

    @Override
    public DownloadAction build() {
        return delegate.build();
    }
}
