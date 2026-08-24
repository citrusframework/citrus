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

package org.citrusframework.playwright.xml;

import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlRootElement;
import org.citrusframework.TestActor;
import org.citrusframework.playwright.actions.AbstractPlaywrightAction;
import org.citrusframework.playwright.actions.ScreenshotAction;
import org.citrusframework.playwright.endpoint.PlaywrightBrowser;

/**
 * Captures a screenshot of the current page.
 */
@XmlRootElement(name = "screenshot")
public class Screenshot extends AbstractPlaywrightAction.Builder<ScreenshotAction, Screenshot> {

    private final ScreenshotAction.Builder delegate = new ScreenshotAction.Builder();

    @XmlAttribute
    public void setPath(String path) {
        delegate.path(path);
    }

    @XmlAttribute
    public void setVariable(String variable) {
        delegate.variable(variable);
    }

    @Override
    public Screenshot description(String description) {
        delegate.description(description);
        return this;
    }

    @Override
    public Screenshot actor(TestActor actor) {
        delegate.actor(actor);
        return this;
    }

    @Override
    public Screenshot browser(PlaywrightBrowser browser) {
        delegate.browser(browser);
        return this;
    }

    @Override
    public ScreenshotAction build() {
        return delegate.build();
    }
}
