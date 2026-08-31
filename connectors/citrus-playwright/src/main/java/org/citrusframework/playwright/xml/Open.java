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
import org.citrusframework.playwright.actions.OpenAction;
import org.citrusframework.playwright.endpoint.PlaywrightBrowser;

/**
 * Opens a URL in the current page.
 */
@XmlRootElement(name = "open")
public class Open extends AbstractPlaywrightAction.Builder<OpenAction, Open> {

    private final OpenAction.Builder delegate = new OpenAction.Builder();

    @XmlAttribute
    public void setUrl(String url) {
        delegate.url(url);
    }

    @Override
    public Open description(String description) {
        delegate.description(description);
        return this;
    }

    @Override
    public Open actor(TestActor actor) {
        delegate.actor(actor);
        return this;
    }

    @Override
    public Open browser(PlaywrightBrowser browser) {
        delegate.browser(browser);
        return this;
    }

    @Override
    public OpenAction build() {
        return delegate.build();
    }
}
